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

import com.google.common.base.Strings;
import com.redhat.devtools.alizer.api.Language;
import com.redhat.devtools.intellij.tektoncd.hub.model.ResourceData;
import com.redhat.devtools.intellij.tektoncd.hub.model.ResourceVersionData;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HubItemScore implements Comparator<HubItem> {

    private static final int NAME_WEIGHT = 10;
    private static final int TAG_WEIGHT = 5;
    private static final int DESCRIPTION_WEIGHT = 3;

    private static final int[] LANGUAGES_MULTIPLIER = new int[] { 4, 2, 1 };

    private List<Language> languages;

    public HubItemScore(List<Language> languages) {
        this.languages = languages;
    }

    private boolean stringContainsItemFromList(String inputStr, List<String> items) {
        return items.stream().map(item -> item.toLowerCase()).anyMatch(inputStr::contains);
    }

    private boolean listsHaveAnElementInCommon(List<String> list1, List<String> list2) {
        return list1.stream().anyMatch(list2::contains);
    }

    private boolean matches(String strInput, Language language) {
        return !Strings.isNullOrEmpty(strInput) &&
                (strInput.contains(language.getName().toLowerCase()) ||
                stringContainsItemFromList(strInput, language.getTools()) ||
                stringContainsItemFromList(strInput, language.getFrameworks()) ||
                stringContainsItemFromList(strInput, language.getAliases()));
    }

    private boolean matches(List<String> listInput, Language language) {
        return listsHaveAnElementInCommon(listInput, language.getTools()) ||
                listsHaveAnElementInCommon(listInput, language.getFrameworks()) ||
                listsHaveAnElementInCommon(listInput, language.getAliases());
    }

    private int computeScore(HubItem hubItem) {
        int score = 0;
        ResourceData resourceData = hubItem.getResource();
        ResourceVersionData resourceVersionData = resourceData.getLatestVersion();

        for(int i=0; i<languages.size(); i++) {
            Language language = languages.get(i);
            int multiplier = i >= LANGUAGES_MULTIPLIER.length ? LANGUAGES_MULTIPLIER[LANGUAGES_MULTIPLIER.length - 1] : LANGUAGES_MULTIPLIER[i];
            if (matches(resourceData.getName(), language)) {
                score += NAME_WEIGHT * multiplier;
            }

            if (resourceVersionData != null && matches(resourceVersionData.getDescription(), language)) {
                score += DESCRIPTION_WEIGHT * multiplier;
            }

            List<String> tags = resourceData.getTags().stream().map(c -> c.getName()).collect(Collectors.toList());
            if (resourceData.getTags().contains(language.getName()) || matches(tags, language)) {
                score += TAG_WEIGHT * multiplier;
            }
        }

        return score;
    }

    @Override
    public int compare(HubItem o1, HubItem o2) {
        return computeScore(o1) - computeScore(o2);
    }
}
