package com.redhat.devtools.intellij.tektoncd.utils;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.redhat.devtools.intellij.common.editor.AllowNonProjectEditing;
import com.redhat.devtools.intellij.tektoncd.settings.SettingsState;
import com.redhat.devtools.intellij.tektoncd.tree.ParentableNode;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;


import static com.redhat.devtools.intellij.common.CommonConstants.CLEANED;
import static com.redhat.devtools.intellij.common.CommonConstants.CONTENT;
import static com.redhat.devtools.intellij.common.CommonConstants.PROJECT;
import static com.redhat.devtools.intellij.common.utils.VirtualFileHelper.cleanContent;
import static com.redhat.devtools.intellij.common.utils.VirtualFileHelper.createTempFile;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINERUN;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PLURAL;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASKRUN;
import static com.redhat.devtools.intellij.tektoncd.Constants.NAMESPACE;
import static com.redhat.devtools.intellij.tektoncd.Constants.TARGET_NODE;

public class VirtualFileHelper {

    public static void openVirtualFileInEditor(Project project, String name, String content, boolean readOnly, boolean clean) throws IOException {
        innerOpenVirtualFileInEditor(project, "", name, content, "", true, readOnly, clean);
    }

    public static void openVirtualFileInEditor(Project project, String name, String content, boolean clean) throws IOException {
        openVirtualFileInEditor(project, name, content, false, clean);
    }

    public static void openVirtualFileInEditor(Project project, String name, String content) throws IOException {
        openVirtualFileInEditor(project, name, content, false);
    }

    public static void openVirtualFileInEditor(Project project, String namespace, String name, String content, String kind, boolean forceWritable) throws IOException {
        innerOpenVirtualFileInEditor(project, namespace, namespace + "-" + name + ".yaml", content, kind, false, !forceWritable && (KIND_PIPELINERUN.equals(kind) || KIND_TASKRUN.equals(kind)), false);
    }

    public static void innerOpenVirtualFileInEditor(Project project, String namespace, String name, String content, String kind, boolean edit, boolean readOnly, boolean clean) throws IOException {
        Optional<FileEditor> editor = Arrays.stream(FileEditorManager.getInstance(project).getAllEditors()).
                filter(fileEditor -> fileEditor.getFile().getName().startsWith(name)).findFirst();
        if (!clean) {
            clean = SettingsState.getInstance().displayCleanedYAMLInEditor;
        }
        if (!editor.isPresent()) {
            createAndOpenVirtualFile(project, namespace, name, content, kind, null, readOnly, clean);
        } else {
            Editor openedEditor = FileEditorManager.getInstance(project).openTextEditor(new OpenFileDescriptor(project, editor.get().getFile()), true);
            if (edit) {
                content = cleanContent(content);
                openedEditor.getDocument().setText(content);
            }
        }
    }

    public static void createAndOpenVirtualFile(Project project, String namespace, String name, String content, String kind, ParentableNode<?> targetNode) throws IOException {
        createAndOpenVirtualFile(project, namespace, name, content, kind, targetNode, KIND_PIPELINERUN.equals(kind) || KIND_TASKRUN.equals(kind), false);
    }

    public static void  createAndOpenVirtualFile(Project project, String namespace, String name, String content, String kind, ParentableNode<?> targetNode, boolean isReadOnly, boolean clean) throws IOException {
        String originalContent = content;
        if (clean) {
            content = cleanContent(content);
        }
        VirtualFile vf = createVirtualFile(name, content, isReadOnly);
        vf.putUserData(AllowNonProjectEditing.ALLOW_NON_PROJECT_EDITING, true);
        vf.putUserData(PROJECT, project);
        if (!kind.isEmpty()) vf.putUserData(KIND_PLURAL, kind);
        if (!namespace.isEmpty()) vf.putUserData(NAMESPACE, namespace);
        if (targetNode != null) vf.putUserData(TARGET_NODE, targetNode);
        vf.putUserData(CONTENT, originalContent);
        vf.putUserData(CLEANED, clean);
        FileEditorManager.getInstance(project).openFile(vf, true);
    }

    public static VirtualFile createVirtualFile(String name, String content, boolean isReadOnly) throws IOException {
        VirtualFile vf;
        if (isReadOnly) {
            vf = new LightVirtualFile(name, content);
            vf.setWritable(false);
        } else {
            vf = createTempFile(name, content);
        }
        return vf;
    }
}
