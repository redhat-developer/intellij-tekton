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
package com.redhat.devtools.intellij.tektoncd.actions.editor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.utils.YAMLBuilder;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class RestoreYAMLClutterActionHandler extends YAMLClutterActionHandler {
    @Override
    public String getUpdatedContent(String originalContent, String currentContent) {
        return restoreYAMLClutter(originalContent, currentContent);
    }

    @Override
    public boolean isCleaned() {
        return false;
    }

    private String restoreYAMLClutter(String originalContent, String currentContent) {
        if (originalContent.isEmpty()) {
            return currentContent;
        }

        try {
            JsonNode originalContentNode = YAMLHelper.YAMLToJsonNode(originalContent);
            ObjectNode currentContentNode = (ObjectNode) YAMLHelper.YAMLToJsonNode(currentContent);
            JsonNode originalContentMetadata = originalContentNode.has("metadata") ? (ObjectNode) originalContentNode.get("metadata") : null;
            ObjectNode currentContentMetadata = currentContentNode.has("metadata") ? (ObjectNode) currentContentNode.get("metadata") : null;
            if (currentContentMetadata == null) {
                currentContentNode.set("metadata", originalContentMetadata);
            } else {
                setMetadataFieldsValues(Arrays.asList(
                        "clusterName",
                        "creationTimestamp",
                        "deletionGracePeriodSeconds",
                        "deletionTimestamp",
                        "finalizers",
                        "generation",
                        "managedFields",
                        "ownerReferences",
                        "resourceVersion",
                        "selfLink",
                        "uid"
                ), originalContentMetadata, currentContentMetadata);
                currentContentNode.set("metadata", currentContentMetadata);
            }
            currentContent = YAMLBuilder.writeValueAsString(currentContentNode);
        } catch (IOException e) {

        }
        return currentContent;
    }

    private void setMetadataFieldsValues(List<String> fields, JsonNode originalContentMetadata, ObjectNode currentContentMetadata) {
        fields.forEach(field -> {
            if (originalContentMetadata.has(field)) {
                currentContentMetadata.put(field, originalContentMetadata.get(field));
            }
        });
    }
}
