/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.intention;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.ui.CreateTaskFromTaskSpecDialog;
import com.redhat.devtools.intellij.tektoncd.utils.DeployHelper;
import com.redhat.devtools.intellij.tektoncd.utils.YAMLBuilder;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.redhat.devtools.intellij.tektoncd.Constants.NOTIFICATION_ID;

public class TaskSpecToTaskRefAction extends PsiElementBaseIntentionAction {

    private static final Logger logger = LoggerFactory.getLogger(TaskSpecToTaskRefAction.class);

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        if (project.isDisposed()) {
            return;
        }
        PsiElement taskSpec = getTaskSpecElement(element);
        if (taskSpec == null) {
            return;
        }

        ExecHelper.submit(() -> {
            CreateTaskFromTaskSpecDialog dialog = createTaskFromTaskSpecDialog();
            if (!dialog.isOK()) {
                return;
            }
            String name = dialog.getName();
            String kind = dialog.getKind();
            String taskSpecText = getTaskSpecText(taskSpec);
            if (name.isEmpty() || kind.isEmpty() || taskSpecText.isEmpty()) {
                return;
            }
            try {
                String taskToSave = createTaskYAMLFromTaskSpec(taskSpecText, name, kind);
                boolean isCreated = DeployHelper.saveOnCluster(project, taskToSave, true);
                if (isCreated) {
                    String textWithTaskRef = createUpdatedTextWithTaskRef(editor.getDocument().getText(), taskSpecText, name, kind);
                    UIHelper.executeInUI(() -> setDocumentText(editor, textWithTaskRef));
                }
            } catch (IOException e) {
                logger.warn(e.getLocalizedMessage());
            }
        });
    }

    private String getTaskSpecText(PsiElement taskSpec) {
        return ApplicationManager.getApplication().runReadAction((Computable<String>)() -> taskSpec.getContext().getText());
    }

    private void setDocumentText(Editor editor, String text) {
        ApplicationManager.getApplication().runWriteAction(() -> editor.getDocument().setText(text));
    }

    private CreateTaskFromTaskSpecDialog createTaskFromTaskSpecDialog() {
        return UIHelper.executeInUI(() -> {
            CreateTaskFromTaskSpecDialog ctdialog = new CreateTaskFromTaskSpecDialog();
            ctdialog.show();
            return ctdialog;
        });
    }

    private String createUpdatedTextWithTaskRef(String documentText, String taskSpec, String task, String kind) throws IOException {
        int indexTaskSpec = documentText.indexOf(taskSpec) - 1;
        int whiteSpaceForTaskRefIndentation = indexTaskSpec - documentText.substring(0, indexTaskSpec).lastIndexOf("\n");
        String taskRefIndentationPlaceholder = String.format("%" + whiteSpaceForTaskRefIndentation + "s", " ");
        return documentText.replace(taskSpec, createTaskRefBlock(task, kind, taskRefIndentationPlaceholder));
    }

    private String createTaskYAMLFromTaskSpec(String taskSpecText, String name, String kind) throws IOException {
        String bodyOfTaskToBeCreated = taskSpecText.replace("taskSpec:\n", "");
        int whiteSpacesForIndentation = bodyOfTaskToBeCreated.indexOf(bodyOfTaskToBeCreated.replaceAll("^\\s+", ""));
        String whiteSpacesPlaceholder = String.format("%" + whiteSpacesForIndentation + "s", " ");
        bodyOfTaskToBeCreated = bodyOfTaskToBeCreated.replace(whiteSpacesPlaceholder, "").replaceAll("\n" + whiteSpacesPlaceholder, "\n");
        ObjectNode taskToSave = YAMLBuilder.convertToObjectNode(bodyOfTaskToBeCreated);
        taskToSave = YAMLBuilder.createTask(name, kind, taskToSave);
        return YAMLBuilder.writeValueAsString(taskToSave);
    }

    private String createTaskRefBlock(String name, String kind, String whiteSpacesPlaceholder) throws IOException {
        String taskRef = YAMLBuilder.writeValueAsString(YAMLBuilder.createTaskRef(name, kind));
        taskRef = taskRef.replace("---\n", "");
        taskRef = taskRef.replace("\n", "\n" +  whiteSpacesPlaceholder);
        return taskRef;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        PsiElement taskSpec = getTaskSpecElement(element);
        if (taskSpec == null) {
            return false;
        }
        String taskSpecName = getTaskSpecName(taskSpec);
        if (taskSpecName.isEmpty()) {
            return false;
        }
        super.setText("Create task from taskSpec " + taskSpecName);
        return true;
    }

    private PsiElement getTaskSpecElement(PsiElement element) {
        PsiElement tmpElement = element;
        PsiElement taskSpecElement = null;
        boolean doParse = true;
        while (doParse) {
            if (tmpElement == null) {
                break;
            }
            PsiElement currentNode = tmpElement.getFirstChild();
            if (currentNode != null) {
                if (currentNode.getText().equals("spec")) {
                    break;
                } else if(currentNode.getText().equals("taskSpec")) {
                    doParse = false;
                    taskSpecElement = currentNode;
                }
            }
            tmpElement = tmpElement.getParent();
        }

        return taskSpecElement;
    }

    private String getTaskSpecName(PsiElement taskSpecElement) {
        AtomicReference<String> name = new AtomicReference<>("");
        PsiElement[] taskSpecSiblings = taskSpecElement.getParent().getParent().getChildren();
        Arrays.stream(taskSpecSiblings).forEach(element -> {
            if (element.getFirstChild().getText().equals("name")) {
                name.set(element.getLastChild().getText());
            }
        });
        return name.get();
    }

    @Override
    public @NotNull String getFamilyName() {
        return NOTIFICATION_ID;
    }

    @Override
    public boolean checkFile(@Nullable PsiFile file) {
        return true;
    }
}
