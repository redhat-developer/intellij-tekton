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
import com.redhat.devtools.intellij.common.utils.JSONHelper;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import io.fabric8.kubernetes.api.model.Status;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.NAME_PREFIX_CRUD;
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.PROP_RESOURCE_CRUD;
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.PROP_RESOURCE_KIND;
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.PROP_RESOURCE_VERSION;
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.VALUE_ABORTED;
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.VALUE_RESOURCE_CRUD_CREATE;
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.VALUE_RESOURCE_CRUD_UPDATE;
import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder.ActionMessage;
import static com.redhat.devtools.intellij.telemetry.core.util.AnonymizeUtils.anonymizeResource;

public class DeployHelper {
    private static final Logger logger = LoggerFactory.getLogger(DeployHelper.class);

    private DeployHelper() {}

    public static boolean saveOnCluster(Project project, String namespace, String yaml, String confirmationMessage, boolean updateLabels, boolean skipConfirmatioDialog) throws IOException {
        ActionMessage telemetry = TelemetryService.instance().action(NAME_PREFIX_CRUD + "save to cluster");

        DeployModel model = createModel(yaml, telemetry);

        Tkn tknCli = TreeHelper.getTkn(project);
        if (tknCli == null) {
            telemetry.error("tkn not found")
                    .send();
            return false;
        }

        if (namespace.isEmpty()) {
            namespace = getNamespace(tknCli, model);
        }

        if (confirmationMessage.isEmpty()) {
            confirmationMessage = getDefaultConfirmationMessage(namespace, model.getName(), model.getKind());
        }

        if (!skipConfirmatioDialog && !isSaveConfirmed(confirmationMessage)) {
            telemetry.result(VALUE_ABORTED)
                    .send();
            return false;
        }

        try {
            String resourceNamespace = CRDHelper.isClusterScopedResource(model.getKind()) ? "" : namespace;
            boolean isNewResource = executeTkn(namespace, resourceNamespace, yaml, updateLabels, model, tknCli);
            telemetry.property(PROP_RESOURCE_CRUD, (isNewResource ? VALUE_RESOURCE_CRUD_CREATE : VALUE_RESOURCE_CRUD_UPDATE))
                    .send();
        } catch (KubernetesClientException e) {
            String errorMsg = createErrorMessage(model, e);
            telemetry.error(anonymizeResource(model.getName(), namespace, errorMsg))
                    .send();
            logger.warn(errorMsg, e);
            // give a visual notification to user if an error occurs during saving
            throw new IOException(errorMsg, e);
        }
        return true;
    }

    public static boolean saveOnCluster(Project project, String yaml, String confirmationMessage) throws IOException {
        return saveOnCluster(project, "", yaml, confirmationMessage, false, false);
    }

    public static boolean saveOnCluster(Project project, String yaml, boolean skipConfirmationDialog) throws IOException {
        return saveOnCluster(project, "", yaml, "", false, skipConfirmationDialog);
    }

    public static boolean saveOnCluster(Project project, String yaml) throws IOException {
        return saveOnCluster(project, "", yaml, "", false, false);
    }

    public static boolean saveTaskOnClusterFromHub(Project project, String name, String version, boolean overwrite, String confirmationMessage) throws IOException {
        if (!isSaveConfirmed(confirmationMessage)) {
            return false;
        }

        Tkn tknCli = TreeHelper.getTkn(project);
        if (tknCli == null) {
            return false;
        }

        tknCli.installTaskFromHub(name, version, overwrite);

        return true;
    }

    public static boolean existsResource(Project project, String name, CustomResourceDefinitionContext crdContext) throws IOException {
        Tkn tknCli = TreeHelper.getTkn(project);
        if (tknCli == null) {
            return false;
        }
        Map<String, Object> resource = tknCli.getCustomResource(tknCli.getNamespace(), name, crdContext);
        return resource != null;
    }

    private static boolean isSaveConfirmed(String confirmationMessage) {
        int resultDialog = UIHelper.executeInUI(() ->
                Messages.showYesNoDialog(
                        confirmationMessage,
                        "Save to cluster",
                        null
                ));

        return resultDialog == Messages.OK;
    }

    private static boolean executeTkn(String namespace, String resourceNamespace, String yaml, boolean updateLabels, DeployModel model, Tkn tknCli) throws IOException {
        boolean newResource = true;
        if (CRDHelper.isRunResource(model.getKind())) {
            tknCli.createCustomResource(resourceNamespace, model.getCrdContext(), yaml);
        } else {
            Map<String, Object> resource = tknCli.getCustomResource(namespace, model.getName(), model.getCrdContext());
            if (resource == null) {
                tknCli.createCustomResource(resourceNamespace, model.getCrdContext(), yaml);
            } else {
                JsonNode customResource = JSONHelper.MapToJSON(resource);
                JsonNode labels = model.getLabels();
                if (updateLabels && labels != null) {
                    ((ObjectNode) customResource.get("metadata")).set("labels", labels);
                }
                ((ObjectNode) customResource).set("spec", model.getSpec());
                tknCli.editCustomResource(resourceNamespace, model.getName(), model.getCrdContext(), customResource.toString());
                newResource = false;
            }
        }
        return newResource;
    }

    private static String createErrorMessage(DeployModel model, KubernetesClientException e) {
        Status errorStatus = e.getStatus();
        String errorMsg = "An error occurred while saving " + StringUtils.capitalize(model.getKind()) + " " + model.getName() + "\n";
        if (errorStatus != null && !Strings.isNullOrEmpty(errorStatus.getMessage())) {
            errorMsg += errorStatus.getMessage() + "\n";
        }
        return errorMsg;
    }

    private static DeployModel createModel(String yaml, ActionMessage telemetry) throws IOException {
        try {
            String name = YAMLHelper.getStringValueFromYAML(yaml, new String[]{"metadata", "name"});
            String generateName = YAMLHelper.getStringValueFromYAML(yaml, new String[]{"metadata", "generateName"});
            if (Strings.isNullOrEmpty(name) && Strings.isNullOrEmpty(generateName)) {
                throw new IOException("Tekton file has not a valid format. Name field is not valid or found.");
            }
            String apiVersion = YAMLHelper.getStringValueFromYAML(yaml, new String[]{"apiVersion"});
            if (Strings.isNullOrEmpty(apiVersion)) {
                throw new IOException("Tekton file has not a valid format. ApiVersion field is not found.");
            }
            String kind = YAMLHelper.getStringValueFromYAML(yaml, new String[]{"kind"});
            if (Strings.isNullOrEmpty(kind)) {
                throw new IOException("Tekton file has not a valid format. Kind field is not found.");
            }
            CustomResourceDefinitionContext crdContext = CRDHelper.getCRDContext(apiVersion, TreeHelper.getPluralKind(kind));
            if (crdContext == null) {
                throw new IOException("Tekton file has not a valid format. ApiVersion field contains an invalid value.");
            }
            JsonNode spec = YAMLHelper.getValueFromYAML(yaml, new String[]{"spec"});
            if (spec == null) {
                throw new IOException("Tekton file has not a valid format. Spec field is not found.");
            }
            String namespace = YAMLHelper.getStringValueFromYAML(yaml, new String[]{"metadata", "namespace"});
            JsonNode labels = YAMLHelper.getValueFromYAML(yaml, new String[]{"metadata", "labels"});
            DeployModel model = new DeployModel(namespace, name, kind, apiVersion, spec, labels, crdContext);
            telemetry.property(PROP_RESOURCE_KIND, model.getKind())
                    .property(PROP_RESOURCE_VERSION, model.getApiVersion());
            return model;
        } catch (IOException e) {
            telemetry.error(e)
                    .send();
            throw e;
        }
    }

    private static String getNamespace(Tkn tkn, DeployModel model) throws IOException {
        String namespace = model.getNamespace();
        if (Strings.isNullOrEmpty(namespace)) {
            return tkn.getNamespace();
        }
        return namespace;
    }

    private static String getDefaultConfirmationMessage(String namespace, String name, String kind) {
        return "Push changes for " + kind + " " + name + " in namespace " + namespace + "?";
    }
}
