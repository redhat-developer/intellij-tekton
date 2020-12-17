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
package com.redhat.devtools.intellij.tektoncd.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.treeStructure.Tree;
import com.redhat.devtools.intellij.common.utils.JSONHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.Constants;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.tree.TektonRootNode;
import com.redhat.devtools.intellij.tektoncd.tree.TektonTreeStructure;
import io.fabric8.kubernetes.api.model.Status;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeployHelper {
    private static Logger logger = LoggerFactory.getLogger(DeployHelper.class);

    public static boolean saveOnCluster(Project project, String namespace, String yaml, String confirmationMessage) throws IOException {
        DeployModel model = isValid(yaml);

        int resultDialog = UIHelper.executeInUI(() ->
                Messages.showYesNoDialog(
                        confirmationMessage,
                        "Save to cluster",
                        null
                ));

        if (resultDialog != Messages.OK) return false;

        Tree tree;
        TektonTreeStructure treeStructure;
        Tkn tknCli;
        try {
            tree = TreeHelper.getTree(project);
            treeStructure = (TektonTreeStructure)tree.getClientProperty(Constants.STRUCTURE_PROPERTY);
            TektonRootNode root = (TektonRootNode) treeStructure.getRootElement();
            tknCli = root.getTkn();
        } catch (Exception e) {
            logger.warn("Error: " + e.getLocalizedMessage(), e);
            return false;
        }

        try {
            String resourceNamespace = CRDHelper.isClusterScopedResource(model.getKind()) ? "" : namespace;
            if (CRDHelper.isRunResource(model.getKind())) {
                tknCli.createCustomResource(resourceNamespace, model.getCrdContext(), yaml);
            } else {
                Map<String, Object> resource = tknCli.getCustomResource(namespace, model.getName(), model.getCrdContext());
                if (resource == null) {
                    tknCli.createCustomResource(resourceNamespace, model.getCrdContext(), yaml);
                } else {
                    JsonNode customResource = JSONHelper.MapToJSON(resource);
                    ((ObjectNode) customResource).set("spec", model.getSpec());
                    tknCli.editCustomResource(resourceNamespace, model.getName(), model.getCrdContext(), customResource.toString());
                }
            }
        } catch (KubernetesClientException e) {
            Status errorStatus = e.getStatus();
            String errorMsg = "An error occurred while saving " + StringUtils.capitalize(model.getKind()) + " " + model.getName() + "\n";;
            if (errorStatus != null && !Strings.isNullOrEmpty(errorStatus.getMessage())) {
                errorMsg += errorStatus.getMessage() + "\n";
            }
            // give a visual notification to user if an error occurs during saving
            throw new IOException(errorMsg + e.getLocalizedMessage());
        }
        return true;
    }

    public static DeployModel isValid(String yaml) throws IOException {
        String name = YAMLHelper.getStringValueFromYAML(yaml, new String[] {"metadata", "name"});
        String generateName = YAMLHelper.getStringValueFromYAML(yaml, new String[] {"metadata", "generateName"});
        if (Strings.isNullOrEmpty(name) && Strings.isNullOrEmpty(generateName)) {
            throw new IOException("Tekton file has not a valid format. Name field is not valid or found.");
        }
        String apiVersion = YAMLHelper.getStringValueFromYAML(yaml, new String[] {"apiVersion"});
        if (Strings.isNullOrEmpty(apiVersion)) {
            throw new IOException("Tekton file has not a valid format. ApiVersion field is not found.");
        }
        String kind = YAMLHelper.getStringValueFromYAML(yaml, new String[] {"kind"});
        if (Strings.isNullOrEmpty(kind)) {
            throw new IOException("Tekton file has not a valid format. Kind field is not found.");
        }
        CustomResourceDefinitionContext crdContext = CRDHelper.getCRDContext(apiVersion, TreeHelper.getPluralKind(kind));
        if (crdContext == null) {
            throw new IOException("Tekton file has not a valid format. ApiVersion field contains an invalid value.");
        }
        JsonNode spec = YAMLHelper.getValueFromYAML(yaml, new String[] {"spec"});
        if (spec == null) {
            throw new IOException("Tekton file has not a valid format. Spec field is not found.");
        }
        return new DeployModel(name, kind, apiVersion, spec, crdContext);
    }
}

class DeployModel {
    private String name, apiVersion, kind;
    private JsonNode spec;
    private CustomResourceDefinitionContext crdContext;

    public DeployModel(String name, String kind, String apiVersion, JsonNode spec, CustomResourceDefinitionContext crdContext) {
        this.name = name;
        this.apiVersion = apiVersion;
        this.kind = kind;
        this.spec = spec;
        this.crdContext = crdContext;
    }

    public String getName() {
        return name;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public String getKind() {
        return kind;
    }

    public JsonNode getSpec() {
        return spec;
    }

    public CustomResourceDefinitionContext getCrdContext() {
        return crdContext;
    }
}
