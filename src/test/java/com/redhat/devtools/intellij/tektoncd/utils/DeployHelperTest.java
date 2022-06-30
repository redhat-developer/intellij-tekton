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
package com.redhat.devtools.intellij.tektoncd.utils;

import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.FixtureBaseTest;
import com.redhat.devtools.intellij.tektoncd.tkn.TknCli;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.util.function.Supplier;

import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINERUNS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeployHelperTest extends FixtureBaseTest {

    private static final String RESOURCE_PATH = "utils/deployHelper/";
    private String pipeline_yaml;
    private TknCli tkn;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        tkn = mock(TknCli.class);
        pipeline_yaml = load(RESOURCE_PATH + "pipeline.yaml");
    }

    @Test
    public void SaveOnCluster_CannotRetrieveTkn_False() throws IOException {
        String yaml = load(RESOURCE_PATH + "pipeline.yaml");
        try(MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            treeHelperMockedStatic.when(() -> TreeHelper.getTkn(any())).thenReturn(null);
            boolean result = DeployHelper.saveOnCluster(project, "namespace", yaml, "message", false, false);
            assertFalse(result);
        }
    }

    @Test
    public void SaveOnCluster_SkipConfirmationIsTrue_IsSaveConfirmedNotCalled() {
        try(MockedStatic<UIHelper> uiHelperMockedStatic = mockStatic(UIHelper.class)) {
            DeployHelper.saveOnCluster(null, pipeline_yaml, true);
            uiHelperMockedStatic.verify(() -> UIHelper.executeInUI(any(Runnable.class)), times(0));
        } catch (IOException e) { }
    }

    @Test
    public void SaveOnCluster_SaveIsNotConfirmedByUser_False() throws IOException {
        when(tkn.getNamespace()).thenReturn("namespace");
        try(MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            try (MockedStatic<UIHelper> uiHelperMockedStatic = mockStatic(UIHelper.class)) {
                uiHelperMockedStatic.when(() -> UIHelper.executeInUI(any(Supplier.class))).thenReturn(Messages.CANCEL);
                treeHelperMockedStatic.when(() -> TreeHelper.getTkn(any())).thenReturn(tkn);
                boolean returningValue = DeployHelper.saveOnCluster(null, pipeline_yaml, false);
                assertFalse(returningValue);
                uiHelperMockedStatic.verify(() -> UIHelper.executeInUI(any(Supplier.class)), times(1));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void SaveOnCluster_NamespaceIsEmptyAndResourceIsNotClusterScoped_ActiveNamespaceIsUsed() throws IOException {
        String yaml = load(RESOURCE_PATH + "pipeline.yaml");
        when(tkn.getNamespace()).thenReturn("namespace");
        try(MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            try(MockedStatic<UIHelper> uiHelperMockedStatic = mockStatic(UIHelper.class)) {
                uiHelperMockedStatic.when(() -> UIHelper.executeInUI(any(Supplier.class))).thenReturn(Messages.NO);
                treeHelperMockedStatic.when(() -> TreeHelper.getTkn(any())).thenReturn(tkn);
                boolean result = DeployHelper.saveOnCluster(project, "", yaml, "message", false, false);
                verify(tkn, times(1)).getNamespace();
                assertFalse(result);
            }
        }
    }

    @Test
    public void SaveOnCluster_TkncliNotFound_False() {
        try(MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            treeHelperMockedStatic.when(() -> TreeHelper.getTkn(any())).thenReturn(null);
            boolean returningValue = DeployHelper.saveOnCluster(null, pipeline_yaml, true);
            assertFalse(returningValue);
        } catch (IOException e) { }
    }

    @Test
    public void SaveOnCluster_ResourceToBeSavedIsRun_CreateCustomResource() {
        try(MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            treeHelperMockedStatic.when(() -> TreeHelper.getTkn(any())).thenReturn(tkn);
            try(MockedStatic<CRDHelper> crdHelperMockedStatic = mockStatic(CRDHelper.class)) {
                crdHelperMockedStatic.when(() -> CRDHelper.isClusterScopedResource(anyString())).thenReturn(true);
                crdHelperMockedStatic.when(() -> CRDHelper.isRunResource(anyString())).thenReturn(true);
                boolean returningValue = DeployHelper.saveOnCluster(null, pipeline_yaml, true);
                assertTrue(returningValue);
                verify(tkn).createCustomResource(anyString(), any(), anyString());
            }
        } catch (IOException e) { }
    }

    @Test
    public void SaveOnCluster_ResourceToBeSavedIsNotRunAndDoesNotExists_CreateCustomResource() {
        try(MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            treeHelperMockedStatic.when(() -> TreeHelper.getTkn(any())).thenReturn(tkn);
            try(MockedStatic<CRDHelper> crdHelperMockedStatic = mockStatic(CRDHelper.class)) {
                crdHelperMockedStatic.when(() -> CRDHelper.isClusterScopedResource(anyString())).thenReturn(true);
                crdHelperMockedStatic.when(() -> CRDHelper.isRunResource(anyString())).thenReturn(false);
                when(tkn.getCustomResource(anyString(), anyString(), any())).thenReturn(null);
                boolean returningValue = DeployHelper.saveOnCluster(null, pipeline_yaml, true);
                assertTrue(returningValue);
                verify(tkn).getCustomResource(anyString(), anyString(), any());
                verify(tkn).createCustomResource(anyString(), any(), anyString());
            }
        } catch (IOException e) { }
    }

    @Test
    public void SaveOnCluster_ResourceToBeSavedIsNotRunAndAlreadyExists_CreateCustomResource() {
        try(MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            treeHelperMockedStatic.when(() -> TreeHelper.getTkn(any())).thenReturn(tkn);
            try(MockedStatic<CRDHelper> crdHelperMockedStatic = mockStatic(CRDHelper.class)) {
                crdHelperMockedStatic.when(() -> CRDHelper.isClusterScopedResource(anyString())).thenReturn(true);
                crdHelperMockedStatic.when(() -> CRDHelper.isRunResource(anyString())).thenReturn(false);
                GenericKubernetesResource genericKubernetesResource = mock(GenericKubernetesResource.class);
                when(tkn.getCustomResource(anyString(), anyString(), any())).thenReturn(genericKubernetesResource);
                boolean returningValue = DeployHelper.saveOnCluster(null, pipeline_yaml, true);
                assertTrue(returningValue);
                verify(tkn).getCustomResource(anyString(), anyString(), any());
                verify(tkn).editCustomResource(anyString(), anyString(), any(), anyString());
            }
        } catch (IOException e) { }
    }

    @Test
    public void SaveOnCluster_ResourceHasAnInvalidApiVersion_Throws() throws IOException {
        String yaml = load(RESOURCE_PATH + "pipeline_invalid_apiversion.yaml");
        try(MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            try(MockedStatic<UIHelper> uiHelperMockedStatic = mockStatic(UIHelper.class)) {
                treeHelperMockedStatic.when(() -> TreeHelper.getTkn(any())).thenReturn(tkn);
                uiHelperMockedStatic.when(() -> UIHelper.executeInUI(any(Supplier.class))).thenReturn(Messages.OK);
                try {
                    DeployHelper.saveOnCluster(project, "namespace", yaml, "message", false, false);
                } catch (IOException e) {
                    assertEquals("Tekton file has not a valid format. ApiVersion field contains an invalid value.", e.getLocalizedMessage());
                }
            }
        }
    }

    @Test
    public void SaveOnCluster_ResourceIsRun_CreateNew() throws IOException {
        String yaml = load(RESOURCE_PATH + "run.yaml");
        try(MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            try(MockedStatic<UIHelper> uiHelperMockedStatic = mockStatic(UIHelper.class)) {
                treeHelperMockedStatic.when(() -> TreeHelper.getTkn(any())).thenReturn(tkn);
                treeHelperMockedStatic.when(() -> TreeHelper.getPluralKind(anyString())).thenReturn(KIND_PIPELINERUNS);
                uiHelperMockedStatic.when(() -> UIHelper.executeInUI(any(Supplier.class))).thenReturn(Messages.OK);
                boolean result = DeployHelper.saveOnCluster(project, "namespace", yaml, "message", false, false);
                verify(tkn, times(1)).createCustomResource(anyString(), any(), anyString());
                assertTrue(result);
            }
        }
    }

    @Test
    public void SaveOnCluster_ResourceNotExists_CreateNew() throws IOException {
        String yaml = load(RESOURCE_PATH + "pipeline.yaml");
        when(tkn.getCustomResource(anyString(), anyString(), any())).thenReturn(null);
        try(MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            try(MockedStatic<UIHelper> uiHelperMockedStatic = mockStatic(UIHelper.class)) {
                treeHelperMockedStatic.when(() -> TreeHelper.getTkn(any())).thenReturn(tkn);
                treeHelperMockedStatic.when(() -> TreeHelper.getPluralKind(anyString())).thenReturn(KIND_PIPELINERUNS);
                uiHelperMockedStatic.when(() -> UIHelper.executeInUI(any(Supplier.class))).thenReturn(Messages.OK);
                boolean result = DeployHelper.saveOnCluster(project, "namespace", yaml, "message", false, false);
                verify(tkn, times(1)).createCustomResource(anyString(), any(), anyString());
                assertTrue(result);
            }
        }
    }

    @Test
    public void SaveOnCluster_ResourceExists_UpdateExisting() throws IOException {
        String yaml = load(RESOURCE_PATH + "pipeline.yaml");
        GenericKubernetesResource genericKubernetesResource = mock(GenericKubernetesResource.class);
        when(tkn.getCustomResource(anyString(), anyString(), any())).thenReturn(genericKubernetesResource);
        try(MockedStatic<TreeHelper> treeHelperMockedStatic = mockStatic(TreeHelper.class)) {
            try(MockedStatic<UIHelper> uiHelperMockedStatic = mockStatic(UIHelper.class)) {
                treeHelperMockedStatic.when(() -> TreeHelper.getTkn(any())).thenReturn(tkn);
                treeHelperMockedStatic.when(() -> TreeHelper.getPluralKind(anyString())).thenReturn(KIND_PIPELINERUNS);
                uiHelperMockedStatic.when(() -> UIHelper.executeInUI(any(Supplier.class))).thenReturn(Messages.OK);
                boolean result = DeployHelper.saveOnCluster(project, "namespace", yaml, "message", false, false);
                verify(tkn, times(0)).createCustomResource(anyString(), any(), anyString());
                verify(tkn, times(1)).editCustomResource(anyString(), anyString(), any(), anyString());
                assertTrue(result);
            }
        }
    }
}
