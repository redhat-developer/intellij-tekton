/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.inspector;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.psi.PsiFile;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.utils.model.ConfigurationModel;
import com.redhat.devtools.intellij.tektoncd.utils.model.ConfigurationModelFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseInspector extends LocalInspectionTool {

    protected static final String START_ROW = "\n\\s*";

    protected boolean isPipeline(PsiFile file) {
        List<Integer> pipelineIndex = indexesOfByPattern(Pattern.compile("kind:\\s*Pipeline"), file.getText());
        return !pipelineIndex.isEmpty();
    }

    protected List<Integer> indexesOfByPattern(Pattern pattern, String textWhereToSearch) {
        List<Integer> indexes = new ArrayList<>();
        Matcher matcher = pattern.matcher(textWhereToSearch);
        while (matcher.find()) {
            indexes.add(matcher.start());
        }
        return indexes;
    }

    protected ConfigurationModel getTektonModelFromFile(@NotNull PsiFile file) {
        if (!file.getLanguage().getID().equalsIgnoreCase("yaml")) {
            return null;
        }

        String content = file.getText();
        try {
            content = YAMLHelper.JSONToYAML(YAMLHelper.YAMLToJsonNode(content), true);
        } catch (IOException e) {
            return null;
        }

        ConfigurationModel model = ConfigurationModelFactory.getModel(content);
        if (model == null) {
            return null;
        }

        return model;
    }
}
