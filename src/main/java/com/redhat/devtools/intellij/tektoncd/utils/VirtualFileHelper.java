package com.redhat.devtools.intellij.tektoncd.utils;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.redhat.devtools.intellij.common.CommonConstants.PROJECT;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PLURAL;
import static com.redhat.devtools.intellij.tektoncd.Constants.NAMESPACE;

public class VirtualFileHelper {
    static Logger logger = LoggerFactory.getLogger(VirtualFileHelper.class);

    public static void createAndOpenVirtualFile(Project project, String namespace, String name, String content, String kind) {
        try {
            VirtualFile vf = createTempFile(name, content);
            vf.putUserData(KIND_PLURAL, kind);
            vf.putUserData(PROJECT, project);
            vf.putUserData(NAMESPACE, namespace);
            File fileToDelete = new File(vf.getPath());
            fileToDelete.deleteOnExit();
            FileEditorManager.getInstance(project).openFile(vf, true);
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }
    }

    private static VirtualFile createTempFile(String name, String content) throws IOException {
        File file = new File(System.getProperty("java.io.tmpdir"), name);
        FileUtils.write(file, content, StandardCharsets.UTF_8);
        return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
    }
}
