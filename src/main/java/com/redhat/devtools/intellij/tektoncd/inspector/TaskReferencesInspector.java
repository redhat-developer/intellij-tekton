/*******************************************************************************
 *  Copyright (c) 2021 Red Hat, Inc.
 *  Distributed under license by Red Hat, Inc. All rights reserved.
 *  This program is made available under the terms of the
 *  Eclipse Public License v2.0 which accompanies this distribution,
 *  and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.inspector;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.utils.TreeHelper;
import com.redhat.devtools.intellij.tektoncd.utils.model.ConfigurationModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.resources.PipelineConfigurationModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class TaskReferencesInspector extends BaseInspector {

    private static final Logger logger = LoggerFactory.getLogger(TaskReferencesInspector.class);
    private static final String NAME_TAG = "name:";
    private static final String TASKREF_TAG = "taskRef:";
    private static final Pattern NAME_TAG_PATTERN = Pattern.compile(START_ROW + NAME_TAG);

    @Nullable
    @Override
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
        ConfigurationModel model = getTektonModelFromFile(file);
        if (model == null) {
            return ProblemDescriptor.EMPTY_ARRAY;
        }

        List<PsiElement> tasKNotFoundPsiElements = new ArrayList<>();
        if (model instanceof PipelineConfigurationModel) {
            List<String> tasksOnCluster = getTasksOnCluster(file.getProject(), model.getNamespace());
            tasKNotFoundPsiElements = findTaskElementsNotFoundOnCluster(file, tasksOnCluster);
        }

        return tasKNotFoundPsiElements.stream().map(item ->
                manager.createProblemDescriptor(item, item, "No task named " + item.getText() + " found on cluster.", ProblemHighlightType.GENERIC_ERROR, isOnTheFly)
        ).toArray(ProblemDescriptor[]::new);
    }

    private List<String> getTasksOnCluster(Project project, String namespace) {
        if (project == null) {
            return Collections.emptyList();
        }
        List<String> tasks = new ArrayList<>();
        try {
            Tkn tkn = TreeHelper.getTkn(project);
            if (tkn != null) {
                tkn.getTasks(namespace).forEach(task -> tasks.add(task.getMetadata().getName()));
                tkn.getClusterTasks().forEach(ctask -> tasks.add(ctask.getMetadata().getName()));
            }
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage(), e);
        }
        return tasks;
    }

    private List<PsiElement> findTaskElementsNotFoundOnCluster(PsiFile file, List<String> tasksOnCluster) {
        if (!file.getText().contains(NAME_TAG)) {
            return Collections.emptyList();
        }

        List<PsiElement> taskElementsNotFound = new ArrayList<>();
        List<Integer> nameNodeIndexMatches = indexesOfByPattern(NAME_TAG_PATTERN, file.getText());
        for (int index: nameNodeIndexMatches) {
            // if this name node is a direct child of taskRef
            ASTNode node = file.getNode().findLeafElementAt(index);
            if (isChildOfTaskRef(node)) {
                PsiElement taskNameElement = getTaskNameElement(node);
                if (taskNameElement != null) {
                    String name = taskNameElement.getText();
                    if (!tasksOnCluster.contains(name)) {
                        taskElementsNotFound.add(taskNameElement);
                    }
                }
            }
        }
        return taskElementsNotFound;
    }

    private boolean isChildOfTaskRef(ASTNode node) {
        if (node == null) {
            return false;
        }

        // taskref can be direct parent of node -> taskRef:\n name: blabla
        // or parent of parent of node -> taskRef:\n kind: bla\n name: blabla
        ASTNode parent = node.getTreeParent();
        if (parent != null) {
            if (parent.getText().startsWith(TASKREF_TAG)) {
                return true;
            }
            parent = parent.getTreeParent();
            if (parent.getText().startsWith(TASKREF_TAG)) {
                return true;
            }
        }
        return false;
    }

    private PsiElement getTaskNameElement(ASTNode node) {
        // starts from \n we need to go next two siblings (\n -> indent -> task)
        PsiElement nextSibling = node.getPsi().getNextSibling();
        if (nextSibling != null) {
            nextSibling = nextSibling.getNextSibling();
            if (nextSibling != null) {
                PsiElement[] children = nextSibling.getChildren();
                if (nextSibling.getText().startsWith(NAME_TAG)
                    && children.length == 1) {
                    PsiElement lastChild = nextSibling.getLastChild();
                    if (lastChild != null
                            && lastChild.getText().startsWith(NAME_TAG)) {
                        lastChild = lastChild.getLastChild();
                    }
                    return lastChild;
                }

                for (PsiElement child: children) {
                    if (child.getText().startsWith(NAME_TAG)) {
                        return child.getLastChild();
                    }
                }


            }
        }
        return null;
    }
}
