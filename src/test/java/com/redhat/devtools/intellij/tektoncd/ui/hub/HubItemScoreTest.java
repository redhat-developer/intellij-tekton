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
import com.redhat.devtools.intellij.tektoncd.hub.model.Category;
import com.redhat.devtools.intellij.tektoncd.hub.model.ResourceData;
import com.redhat.devtools.intellij.tektoncd.hub.model.ResourceVersionData;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HubItemScoreTest {

    @Test
    public void Compare_2HubItems_VerifyCalculatedScoreWithOnlyName() {
        Language lang = new Language("First", Collections.emptyList(), 99.0);
        Language lang2 = new Language("Second", Collections.emptyList(), 1.0);
        List<Language> languages = Arrays.asList(lang, lang2);

        HubItem hubItem1 = buildHubItem("Second", "", Collections.emptyList());
        HubItem hubItem2 = buildHubItem("First", "", Collections.emptyList());

        HubItemScore hubItemScore = new HubItemScore(languages);

        assertEquals(-20, hubItemScore.compare(hubItem1, hubItem2));
    }

    @Test
    public void Compare_2HubItems_VerifyCalculatedScoreWithAliases() {
        Language lang = new Language("First", Arrays.asList("alias1", "alias2"), 99.0);
        Language lang2 = new Language("Second", Arrays.asList("alias3", "alias4"), 1.0);
        List<Language> languages = Arrays.asList(lang, lang2);

        HubItem hubItem1 = buildHubItem("alias1", "", Collections.emptyList());
        HubItem hubItem2 = buildHubItem("alias4", "", Collections.emptyList());

        HubItemScore hubItemScore = new HubItemScore(languages);

        assertEquals(20, hubItemScore.compare(hubItem1, hubItem2));
    }

    @Test
    public void Compare_2HubItems_VerifyCalculatedScoreWithFrameworks() {
        Language lang = new Language("First", Arrays.asList("alias1", "alias2"), 99.0, Arrays.asList("framework1"), Collections.emptyList());
        Language lang2 = new Language("Second", Arrays.asList("alias3", "alias4"), 1.0, Arrays.asList("framework2"), Collections.emptyList());
        List<Language> languages = Arrays.asList(lang, lang2);

        Category category = buildCategory("framework1");
        Category category2 = buildCategory("framework2");
        Category category3 = buildCategory("framework3");

        HubItem hubItem1 = buildHubItem("name", "", Arrays.asList(category));
        HubItem hubItem2 = buildHubItem("name2", "", Arrays.asList(category2, category3));

        HubItemScore hubItemScore = new HubItemScore(languages);

        assertEquals(10, hubItemScore.compare(hubItem1, hubItem2));
    }

    @Test
    public void Compare_2HubItems_VerifyCalculatedScoreWithDescription() {
        Language lang = new Language("First", Arrays.asList("alias1", "alias2"), 99.0, Arrays.asList("framework1"), Collections.emptyList());
        Language lang2 = new Language("Second", Arrays.asList("alias3", "alias4"), 1.0, Arrays.asList("framework2"), Collections.emptyList());
        List<Language> languages = Arrays.asList(lang, lang2);

        Category category2 = buildCategory("framework2");
        Category category3 = buildCategory("framework3");

        HubItem hubItem1 = buildHubItem("name", "this is the first item for the first language", Collections.emptyList());
        HubItem hubItem2 = buildHubItem("name2", "blabla", Arrays.asList(category2, category3));

        HubItemScore hubItemScore = new HubItemScore(languages);

        assertEquals(2, hubItemScore.compare(hubItem1, hubItem2));
    }

    @Test
    public void Compare_ListWith3HubItems_OrderedList() {
        Language lang = new Language("First", Collections.emptyList(), 50.0);
        Language lang2 = new Language("Second", Collections.emptyList(), 1.0);
        Language lang3 = new Language("Third", Collections.emptyList(), 49.0);
        List<Language> languages = Arrays.asList(lang, lang2, lang3);

        HubItem hubItem1 = buildHubItem("Second", "", Collections.emptyList());
        HubItem hubItem2 = buildHubItem("First", "", Collections.emptyList());
        HubItem hubItem3 = buildHubItem("Third", "", Collections.emptyList());

        List<HubItem> hubItems = Arrays.asList(hubItem1, hubItem2, hubItem3).stream().sorted(new HubItemScore(languages).reversed())
                .collect(Collectors.toList());;

        assertEquals(3, hubItems.size());
        assertEquals("First", hubItems.get(0).getResource().getName());
        assertEquals("Second", hubItems.get(1).getResource().getName());
        assertEquals("Third", hubItems.get(2).getResource().getName());
    }

    private Category buildCategory(String name) {
        Category category1 = mock(Category.class);
        when(category1.getName()).thenReturn(name);
        return category1;
    }

    private HubItem buildHubItem(String name, String description, List<Category> tags) {
        ResourceData resourceData = mock(ResourceData.class);
        ResourceVersionData resourceVersionData = mock(ResourceVersionData.class);
        HubItem hubItem = new HubItem(resourceData);
        when(resourceData.getLatestVersion()).thenReturn(resourceVersionData);
        when(resourceVersionData.getDescription()).thenReturn(description);
        when(resourceData.getName()).thenReturn(name);
        when(resourceData.getTags()).thenReturn(tags);

        return hubItem;
    }
}
