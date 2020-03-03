package com.redhat.devtools.intellij.common.listener;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import com.redhat.devtools.intellij.common.actions.component.OpenEditorTask;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileEditorListener implements FileEditorManagerListener {
    @Override
    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        String tknProperty = file.getUserData(OpenEditorTask.TEKTON);
        if (tknProperty != null && tknProperty.equals("file")) {
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (Files.exists(Paths.get(file.getPath()))) {
                            file.delete(this);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
