package com.redhat.devtools.intellij.tektoncd.utils;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.redhat.devtools.intellij.common.editor.AllowNonProjectEditing;
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
    private static final Logger logger = LoggerFactory.getLogger(VirtualFileHelper.class);

    public static void openVirtualFileInEditor(Project project, String name, String content) throws IOException {
        innerOpenVirtualFileInEditor(project, "", name, content, "", true, false);
    }

    public static void openVirtualFileInEditor(Project project, String namespace, String name, String content, String kind, boolean forceWritable) throws IOException {
        innerOpenVirtualFileInEditor(project, namespace, namespace + "-" + name + ".yaml", content, kind, false, !forceWritable && (KIND_PIPELINERUN.equals(kind) || KIND_TASKRUN.equals(kind)));
    }

    public static void innerOpenVirtualFileInEditor(Project project, String namespace, String name, String content, String kind, boolean edit, boolean readOnly) throws IOException {
        Optional<FileEditor> editor = Arrays.stream(FileEditorManager.getInstance(project).getAllEditors()).
                filter(fileEditor -> fileEditor.getFile().getName().startsWith(name)).findFirst();
        if (!editor.isPresent()) {
            createAndOpenVirtualFile(project, namespace, name, content, kind, null, readOnly);
        } else {
            Editor openedEditor = FileEditorManager.getInstance(project).openTextEditor(new OpenFileDescriptor(project, editor.get().getFile()), true);
            if (edit) {
                openedEditor.getDocument().setText(content);
            }
        }
    }

    public static void createAndOpenVirtualFile(Project project, String namespace, String name, String content, String kind, ParentableNode<?> targetNode) throws IOException {
        createAndOpenVirtualFile(project, namespace, name, content, kind, targetNode, KIND_PIPELINERUN.equals(kind) || KIND_TASKRUN.equals(kind));
    }

    public static void  createAndOpenVirtualFile(Project project, String namespace, String name, String content, String kind, ParentableNode<?> targetNode, boolean isReadOnly) throws IOException {
        VirtualFile vf = createVirtualFile(name, content, isReadOnly);
        vf.putUserData(AllowNonProjectEditing.ALLOW_NON_PROJECT_EDITING, true);
        vf.putUserData(PROJECT, project);
        if (!kind.isEmpty()) vf.putUserData(KIND_PLURAL, kind);
        if (!namespace.isEmpty()) vf.putUserData(NAMESPACE, namespace);
        if (targetNode != null) vf.putUserData(TARGET_NODE, targetNode);
        FileEditorManager.getInstance(project).openFile(vf, true);
    }

    private static VirtualFile createVirtualFile(String name, String content, boolean isReadOnly) throws IOException {
        VirtualFile vf = null;
        if (isReadOnly) {
            vf = new LightVirtualFile(name, content);
            vf.setWritable(false);
        } else {
            vf = createTempFile(name, content);
        }
        return vf;
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
