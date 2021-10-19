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
package com.redhat.devtools.intellij.tektoncd.ui.hub;

import com.redhat.devtools.alizer.api.Language;
import com.redhat.devtools.alizer.api.LanguageRecognizer;
import com.redhat.devtools.alizer.api.RecognizerFactory;
import com.redhat.devtools.intellij.tektoncd.BaseTest;
import com.redhat.devtools.intellij.tektoncd.hub.api.ResourceApi;
import com.redhat.devtools.intellij.tektoncd.hub.model.ResourceData;
import com.redhat.devtools.intellij.tektoncd.hub.model.ResourceVersionData;
import com.redhat.devtools.intellij.tektoncd.hub.model.Resources;
import com.redhat.devtools.intellij.tektoncd.tree.ClusterTasksNode;
import com.redhat.devtools.intellij.tektoncd.tree.PipelinesNode;
import io.fabric8.kubernetes.client.Watch;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_PIPELINE;
import static com.redhat.devtools.intellij.tektoncd.Constants.KIND_TASK;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class HubModelTest extends BaseTest {

    private PipelinesNode pipelinesNode;
    private ClusterTasksNode clusterTasksNode;
    private RecognizerFactory recognizerFactory;
    private LanguageRecognizer languageRecognizer;
    private Language languageJAVA, languageUnknown;
    private ResourceData resourceDataJAVA, resourceDataGO, resourceDataNET;
    private ResourceVersionData resourceVersionDataJAVA, resourceVersionDataGO, resourceVersionDataNET;
    private ResourceApi resourceApi;
    private Resources resources;
    private Watch watch;
    @Before
    public void setUp() throws Exception {
        super.setUp();
        watch = mock(Watch.class);
        when(tkn.getNamespace()).thenReturn("namespace");
        when(tkn.watchPipelines(anyString(), any())).thenReturn(watch);
        when(tkn.watchTasks(anyString(), any())).thenReturn(watch);
        when(tkn.watchClusterTasks(any())).thenReturn(watch);
        pipelinesNode = mock(PipelinesNode.class);
        clusterTasksNode = mock(ClusterTasksNode.class);
        recognizerFactory = mock(RecognizerFactory.class);
        languageRecognizer = mock(LanguageRecognizer.class);
        when(recognizerFactory.createLanguageRecognizer()).thenReturn(languageRecognizer);
        languageJAVA = new Language("java", Collections.emptyList(), 1.0, true);
        languageUnknown = new Language("Unknown", Collections.emptyList(), 1.0, false);
        resourceApi = mock(ResourceApi.class);

        resourceDataJAVA = mock(ResourceData.class);
        resourceVersionDataJAVA = mock(ResourceVersionData.class);
        when(resourceDataJAVA.getName()).thenReturn("java");
        when(resourceDataJAVA.getKind()).thenReturn(KIND_TASK);
        when(resourceDataJAVA.getLatestVersion()).thenReturn(resourceVersionDataJAVA);
        when(resourceVersionDataJAVA.getVersion()).thenReturn("v1");

        resourceDataGO = mock(ResourceData.class);
        resourceVersionDataGO = mock(ResourceVersionData.class);
        when(resourceDataGO.getName()).thenReturn("go");
        when(resourceDataGO.getKind()).thenReturn(KIND_TASK);
        when(resourceDataGO.getLatestVersion()).thenReturn(resourceVersionDataGO);
        when(resourceVersionDataGO.getVersion()).thenReturn("v1");

        resourceDataNET = mock(ResourceData.class);
        resourceVersionDataNET = mock(ResourceVersionData.class);
        when(resourceDataNET.getName()).thenReturn("net");
        when(resourceDataNET.getKind()).thenReturn(KIND_PIPELINE);
        when(resourceDataNET.getLatestVersion()).thenReturn(resourceVersionDataNET);
        when(resourceVersionDataNET.getVersion()).thenReturn("v1");

        when(project.getBasePath()).thenReturn(".");

        resources = new Resources();
        resources.addDataItem(resourceDataJAVA);
        resources.addDataItem(resourceDataGO);
    }

    @Test
    public void GetAllPipelineHubItems_RemoteHubContainsPipelineResources_ListWithAllPipelines() throws IOException {
        resources.addDataItem(resourceDataNET);
        try(MockedConstruction<ResourceApi> resourceApiMockedConstruction = mockConstruction(ResourceApi.class,
                (mock, context) -> when(mock.resourceList(anyInt())).thenReturn(resources))) {
            try(MockedConstruction<RecognizerFactory> languageRecognizerBuilderMockedConstruction = mockConstruction(RecognizerFactory.class,
                    (mock, context) -> when(mock.createLanguageRecognizer()).thenReturn(languageRecognizer))) {

                List<Language> languages = Arrays.asList(languageJAVA);
                when(languageRecognizer.analyze(anyString())).thenReturn(languages);

                HubModel model = new HubModel(project, tkn, null);
                List<HubItem> pipelineItems = model.getAllPipelineHubItems();
                assertEquals(1, pipelineItems.size());
                assertEquals("net", pipelineItems.get(0).getResource().getName());
            }
        }
    }

    @Test
    public void GetAllPipelineHubItems_RemoteHubHasNoPipelineResources_EmptyList() throws IOException {
        try(MockedConstruction<ResourceApi> resourceApiMockedConstruction = mockConstruction(ResourceApi.class,
                (mock, context) -> when(mock.resourceList(anyInt())).thenReturn(resources))) {
            try(MockedConstruction<RecognizerFactory> languageRecognizerBuilderMockedConstruction = mockConstruction(RecognizerFactory.class,
                    (mock, context) -> when(mock.createLanguageRecognizer()).thenReturn(languageRecognizer))) {

                List<Language> languages = Arrays.asList(languageJAVA);
                when(languageRecognizer.analyze(anyString())).thenReturn(languages);

                HubModel model = new HubModel(project, tkn, null);
                List<HubItem> pipelineItems = model.getAllPipelineHubItems();
                assertEquals(0, pipelineItems.size());
            }
        }
    }

    @Test
    public void GetAllTaskHubItems_RemoteHubContainsTaskResources_ListWithAllTasks() throws IOException {
        try(MockedConstruction<ResourceApi> resourceApiMockedConstruction = mockConstruction(ResourceApi.class,
                (mock, context) -> when(mock.resourceList(anyInt())).thenReturn(resources))) {
            try(MockedConstruction<RecognizerFactory> languageRecognizerBuilderMockedConstruction = mockConstruction(RecognizerFactory.class,
                    (mock, context) -> when(mock.createLanguageRecognizer()).thenReturn(languageRecognizer))) {

                List<Language> languages = Arrays.asList(languageJAVA);
                when(languageRecognizer.analyze(anyString())).thenReturn(languages);

                HubModel model = new HubModel(project, tkn, null);
                List<HubItem> taskItems = model.getAllTaskHubItems();
                assertEquals(2, taskItems.size());
                assertEquals("java", taskItems.get(0).getResource().getName());
                assertEquals("go", taskItems.get(1).getResource().getName());
            }
        }
    }

    @Test
    public void GetAllTaskHubItems_RemoteHubHasNoTaskResources_EmptyList() throws IOException {
        Resources resources = new Resources();
        resources.addDataItem(resourceDataNET);
        try(MockedConstruction<ResourceApi> resourceApiMockedConstruction = mockConstruction(ResourceApi.class,
                (mock, context) -> when(mock.resourceList(anyInt())).thenReturn(resources))) {
            try(MockedConstruction<RecognizerFactory> languageRecognizerBuilderMockedConstruction = mockConstruction(RecognizerFactory.class,
                    (mock, context) -> when(mock.createLanguageRecognizer()).thenReturn(languageRecognizer))) {

                List<Language> languages = Arrays.asList(languageJAVA);
                when(languageRecognizer.analyze(anyString())).thenReturn(languages);

                HubModel model = new HubModel(project, tkn, null);
                List<HubItem> taskItems = model.getAllTaskHubItems();
                assertEquals(0, taskItems.size());
            }
        }
    }

    @Test
    public void GetRecommendedHubItems_LanguagesHasHubItemsRelated_FilteredList() throws IOException {
        try(MockedConstruction<ResourceApi> resourceApiMockedConstruction = mockConstruction(ResourceApi.class,
                (mock, context) -> when(mock.resourceList(anyInt())).thenReturn(resources))) {
            try(MockedConstruction<RecognizerFactory> languageRecognizerBuilderMockedConstruction = mockConstruction(RecognizerFactory.class,
                    (mock, context) -> when(mock.createLanguageRecognizer()).thenReturn(languageRecognizer))) {

                List<Language> languages = Arrays.asList(languageJAVA);
                when(languageRecognizer.analyze(anyString())).thenReturn(languages);

                HubModel model = new HubModel(project, tkn, null);
                List<HubItem> recommendedItems = model.getRecommendedHubItems();
                assertEquals(1, recommendedItems.size());
                assertEquals("java", recommendedItems.get(0).getResource().getName());
            }
        }
    }

    @Test
    public void GetRecommendedHubItems_LanguagesHasNoHubItemsRelated_EmptyList() throws IOException {
        try(MockedConstruction<ResourceApi> resourceApiMockedConstruction = mockConstruction(ResourceApi.class,
                (mock, context) -> when(mock.resourceList(anyInt())).thenReturn(resources))) {
            try(MockedConstruction<RecognizerFactory> languageRecognizerBuilderMockedConstruction = mockConstruction(RecognizerFactory.class,
                    (mock, context) -> when(mock.createLanguageRecognizer()).thenReturn(languageRecognizer))) {

                List<Language> languages = Arrays.asList(languageUnknown);
                when(languageRecognizer.analyze(anyString())).thenReturn(languages);

                HubModel model = new HubModel(project, tkn, null);
                List<HubItem> recommendedItems = model.getRecommendedHubItems();
                assertEquals(0, recommendedItems.size());
            }
        }
    }

    @Test
    public void GetInstalledHubItems_NoInstalledItems_EmptyList() {
        try(MockedConstruction<ResourceApi> resourceApiMockedConstruction = mockConstruction(ResourceApi.class,
                (mock, context) -> when(mock.resourceList(anyInt())).thenReturn(resources))) {
            HubModel model = new HubModel(project, tkn, null);
            List<HubItem> recommendedItems = model.getInstalledHubItems();
            assertEquals(0, recommendedItems.size());
        }
    }

    @Test
    public void GetIsClusterTaskView_CallerIsNull_False() {
        HubModel model = new HubModel(project, tkn, null);
        assertFalse(model.getIsClusterTaskView());
    }

    @Test
    public void GetIsClusterTaskView_CallerIsNotNullAndNotAClusterTasksNode_False() {
        HubModel model = new HubModel(project, tkn, pipelinesNode);
        assertFalse(model.getIsClusterTaskView());
    }

    @Test
    public void GetIsClusterTaskView_CallerIsAClusterTasksNode_True() {
        HubModel model = new HubModel(project, tkn, clusterTasksNode);
        assertTrue(model.getIsClusterTaskView());
    }

    @Test
    public void GetIsPipelineView_CallerIsNull_False() {
        HubModel model = new HubModel(project, tkn, null);
        assertFalse(model.getIsPipelineView());
    }

    @Test
    public void GetIsPipelineView_CallerIsNotNullAndNotAClusterTasksNode_False() {
        HubModel model = new HubModel(project, tkn, clusterTasksNode);
        assertFalse(model.getIsPipelineView());
    }

    @Test
    public void GetIsPipelineView_CallerIsAClusterTasksNode_True() {
        HubModel model = new HubModel(project, tkn, pipelinesNode);
        assertTrue(model.getIsPipelineView());
    }

}