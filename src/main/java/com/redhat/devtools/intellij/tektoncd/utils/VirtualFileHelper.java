package com.redhat.devtools.intellij.tektoncd.utils;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.testFramework.LightVirtualFile;
import com.redhat.devtools.intellij.common.actions.StructureTreeAction;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelineRunNode;
import com.redhat.devtools.intellij.tektoncd.tree.TaskRunNode;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import javax.swing.tree.TreePath;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.redhat.devtools.intellij.common.CommonConstants.PROJECT;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINERUN;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PLURAL;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASKRUN;
import static com.redhat.devtools.intellij.tektoncd.Constants.NAMESPACE;
import static com.redhat.devtools.intellij.tektoncd.Constants.TARGET_NODE;

public class VirtualFileHelper {
    static Logger logger = LoggerFactory.getLogger(VirtualFileHelper.class);

    public static void openTektonVirtualFileInEditor(TreePath path) {
        Object component = path.getLastPathComponent();
        ParentableNode node = StructureTreeAction.getElement(component);
        String url = TreeHelper.getTektonResourceUrl(node, true);
        TektonVirtualFile file = (TektonVirtualFile) VirtualFileManager.getInstance().findFileByUrl(url);

        if (node instanceof PipelineRunNode || node instanceof TaskRunNode) {
            file.setWritable(false);
        }
        FileEditorManager.getInstance(node.getRoot().getProject()).openFile(file, true);
    }

    public static void createAndOpenVirtualFile(Project project, String namespace, String name, String content, String kind, ParentableNode<?> targetNode) {
        try {
            VirtualFile vf;

            //open TaskRun and PipelineRun in read only mode
            if (KIND_PIPELINERUN.equals(kind) || KIND_TASKRUN.equals(kind)) {
                vf = new LightVirtualFile(name, content);
                vf.setWritable(false);
            } else {
                vf = createTempFile(name, content);
            }
            vf.putUserData(KIND_PLURAL, kind);
            vf.putUserData(PROJECT, project);
            vf.putUserData(NAMESPACE, namespace);
            vf.putUserData(TARGET_NODE, targetNode);

            FileEditorManager.getInstance(project).openFile(vf, true);
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }
    }

    private static VirtualFile createTempFile(String name, String content) throws IOException {
        File file = new File(System.getProperty("java.io.tmpdir"), name);
        if (file.exists()){
            file.delete();
            LocalFileSystem.getInstance().refreshIoFiles(Arrays.asList(file));
        }
        FileUtils.write(file, content, StandardCharsets.UTF_8);
        file.deleteOnExit();
        return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
    }
}
