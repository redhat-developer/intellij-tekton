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

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.junit.Before;
import org.junit.Test;


import static com.redhat.devtools.intellij.tektoncd.Constants.NOTIFICATION_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TaskSpecToTaskRefActionTest {

    private TaskSpecToTaskRefAction taskSpecToTaskRefAction;
    private Project project;
    private PsiElement psiElement, taskSpecPsiElement, firstParentPsiElement, secondParentPsiElement, namePsiElement;
    private Editor editor;

    @Before
    public void setUp() {
        taskSpecToTaskRefAction = new TaskSpecToTaskRefAction();

        project = mock(Project.class);
        psiElement = mock(PsiElement.class);
        taskSpecPsiElement = mock(PsiElement.class);
        firstParentPsiElement = mock(PsiElement.class);
        secondParentPsiElement = mock(PsiElement.class);
        namePsiElement = mock(PsiElement.class);
        editor = mock(Editor.class);
    }

    @Test
    public void GetFamilyName_ID() {
        assertEquals(NOTIFICATION_ID, taskSpecToTaskRefAction.getFamilyName());
    }

    @Test
    public void IsAvailable_PsiElementIsNotTaskSpec_False() {
        assertFalse(taskSpecToTaskRefAction.isAvailable(project, editor, psiElement));
    }

    @Test
    public void IsAvailable_PsiElementIsSpec_False() {
        when(taskSpecPsiElement.getText()).thenReturn("spec");
        when(psiElement.getFirstChild()).thenReturn(taskSpecPsiElement);

        assertFalse(taskSpecToTaskRefAction.isAvailable(project, editor, psiElement));
    }

    @Test
    public void IsAvailable_PsiElementIsTaskSpecWithoutName_False() {
        when(taskSpecPsiElement.getParent()).thenReturn(firstParentPsiElement);
        when(firstParentPsiElement.getParent()).thenReturn(secondParentPsiElement);
        when(secondParentPsiElement.getChildren()).thenReturn(new PsiElement[] {psiElement});
        when(taskSpecPsiElement.getText()).thenReturn("taskSpec");
        when(psiElement.getFirstChild()).thenReturn(taskSpecPsiElement);

        assertFalse(taskSpecToTaskRefAction.isAvailable(project, editor, psiElement));
    }

    @Test
    public void IsAvailable_PsiElementIsTaskSpecWithName_True() {
        when(taskSpecPsiElement.getParent()).thenReturn(firstParentPsiElement);
        when(firstParentPsiElement.getParent()).thenReturn(secondParentPsiElement);
        when(secondParentPsiElement.getChildren()).thenReturn(new PsiElement[] {psiElement});
        when(taskSpecPsiElement.getText()).thenReturn("taskSpec").thenReturn("name");
        when(psiElement.getFirstChild()).thenReturn(taskSpecPsiElement);

        assertFalse(taskSpecToTaskRefAction.isAvailable(project, editor, psiElement));
    }
}
