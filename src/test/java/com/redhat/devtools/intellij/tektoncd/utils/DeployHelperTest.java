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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;


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

    private static final String RESOURCE_PATH = "utils/deployhelper/";
    private String pipeline_yaml;
    private TknCli tkn;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        tkn = mock(TknCli.class);
        pipeline_yaml = load(RESOURCE_PATH + "pipeline.yaml");
    }


    @Test
    public void SaveOnCluster_InvalidYamlMissingKind_Throw() throws IOException {
        String yaml = load(RESOURCE_PATH + "missingkind_pipeline.yaml");
        assertIsCorrectErrorWhenInvalidYaml(yaml, "Tekton file has not a valid format. Kind field is not found.");
    }

    @Test
    public void SaveOnCluster_InvalidYamlMissingName_Throw() throws IOException {
        String yaml = load(RESOURCE_PATH + "missingname_pipeline.yaml");
        assertIsCorrectErrorWhenInvalidYaml(yaml, "Tekton file has not a valid format. Name field is not valid or found.");
    }

    @Test
    public void SaveOnCluster_InvalidYamlMissingApiVersion_Throw() throws IOException {
        String yaml = load(RESOURCE_PATH + "missingapiversion_pipeline.yaml");
        assertIsCorrectErrorWhenInvalidYaml(yaml, "Tekton file has not a valid format. ApiVersion field is not found.");
    }

    @Test
    public void SaveOnCluster_InvalidYamlInvalidApiVersion_Throw() throws IOException {
        String yaml = load(RESOURCE_PATH + "missingcrd_pipeline.yaml");
        assertIsCorrectErrorWhenInvalidYaml(yaml, "Tekton file has not a valid format. ApiVersion field contains an invalid value.");
    }

    private void assertIsCorrectErrorWhenInvalidYaml(String yaml, String expectedError) {
        try (MockedStatic<TreeHelper> theMock = mockStatic(TreeHelper.class)) {
            theMock.when(() -> TreeHelper.getTkn(any())).thenReturn(null);
            try {
                DeployHelper.saveOnCluster(null, yaml, "", false, true);
            } catch (IOException e) {
                assertEquals(expectedError, e.getLocalizedMessage());
            }
        }
    }

    @Test
    public void SaveOnCluster_SkipConfirmationIsTrue_IsSaveConfirmedNotCalled() {
        try(MockedStatic<UIHelper> uiHelperMockedStatic = mockStatic(UIHelper.class)) {
            DeployHelper.saveOnCluster(null, pipeline_yaml, true);
            uiHelperMockedStatic.verify(times(0), () -> UIHelper.executeInUI(any(Runnable.class)));
        } catch (IOException e) { }
    }

    @Test
    public void SaveOnCluster_SaveIsNotConfirmedByUser_False() {
        try(MockedStatic<UIHelper> uiHelperMockedStatic = mockStatic(UIHelper.class)) {
            uiHelperMockedStatic.when(() -> UIHelper.executeInUI(any(Supplier.class))).thenReturn(Messages.CANCEL);
            boolean returningValue = DeployHelper.saveOnCluster(null, pipeline_yaml, false);
            assertFalse(returningValue);
            uiHelperMockedStatic.verify(times(1), () -> UIHelper.executeInUI(any(Supplier.class)));
        } catch (IOException e) {
            e.printStackTrace();
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
                Map<String, Object> customResourceMap = new HashMap<>();
                customResourceMap.put("apiVersion", "api");
                customResourceMap.put("spec", null);
                when(tkn.getCustomResource(anyString(), anyString(), any())).thenReturn(customResourceMap);
                boolean returningValue = DeployHelper.saveOnCluster(null, pipeline_yaml, true);
                assertTrue(returningValue);
                verify(tkn).getCustomResource(anyString(), anyString(), any());
                verify(tkn).editCustomResource(anyString(), anyString(), any(), anyString());
            }
        } catch (IOException e) { }
    }
}
