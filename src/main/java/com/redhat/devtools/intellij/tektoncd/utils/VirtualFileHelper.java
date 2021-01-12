package com.redhat.devtools.intellij.tektoncd.utils;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
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

    public static void openVirtualFileInEditor(Project project, String name, String content) {
        Optional<FileEditor> editor = Arrays.stream(FileEditorManager.getInstance(project).getAllEditors()).
                filter(fileEditor -> fileEditor.getFile().getName().startsWith(name)).findFirst();
        if (!editor.isPresent()) {
            VirtualFileHelper.createAndOpenVirtualFile(project, "", name, content, "", null, true);
        } else {
            FileEditorManager.getInstance(project).openTextEditor(new OpenFileDescriptor(project, editor.get().getFile()), true);
        }
    }

    public static void openVirtualFileInEditor(Project project, String namespace, String name, String content, String kind, boolean forceWritable) {
        Optional<FileEditor> editor = Arrays.stream(FileEditorManager.getInstance(project).getAllEditors()).
                filter(fileEditor -> fileEditor.getFile().getName().startsWith(namespace + "-" + name + ".yaml")).findFirst();
        if (!editor.isPresent()) {
            VirtualFileHelper.createAndOpenVirtualFile(project, namespace, namespace + "-" + name + ".yaml", content, kind, null, !forceWritable && (KIND_PIPELINERUN.equals(kind) || KIND_TASKRUN.equals(kind)));
        } else {
            FileEditorManager.getInstance(project).openTextEditor(new OpenFileDescriptor(project, editor.get().getFile()), true);
        }
    }

    public static void createAndOpenVirtualFile(Project project, String namespace, String name, String content, String kind, ParentableNode<?> targetNode) {
        createAndOpenVirtualFile(project, namespace, name, content, kind, targetNode, KIND_PIPELINERUN.equals(kind) || KIND_TASKRUN.equals(kind));
    }

    public static void  createAndOpenVirtualFile(Project project, String namespace, String name, String content, String kind, ParentableNode<?> targetNode, boolean isReadOnly) {
        try {
            VirtualFile vf;

            if (isReadOnly) {
                vf = new LightVirtualFile(name, content);
                vf.setWritable(false);
            } else {
                vf = createTempFile(name, content);
            }
            vf.putUserData(PROJECT, project);
            if (!kind.isEmpty()) vf.putUserData(KIND_PLURAL, kind);
            if (!namespace.isEmpty()) vf.putUserData(NAMESPACE, namespace);
            if (targetNode != null) vf.putUserData(TARGET_NODE, targetNode);

            FileEditorManager.getInstance(project).openFile(vf, true);

            //vf.getFileSystem().addVirtualFileListener(new FileChangedListener());
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
