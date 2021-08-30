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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Strings;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.messages.MessageDialog;
import com.redhat.devtools.intellij.common.model.GenericResource;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.Status;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.intellij.openapi.ui.Messages.NO_BUTTON;
import static com.intellij.openapi.ui.Messages.YES_BUTTON;
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.NAME_PREFIX_CRUD;
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.PROP_RESOURCE_CRUD;
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

        GenericResource resource = getResource(yaml, telemetry);

        Tkn tknCli = TreeHelper.getTkn(project);
        if (tknCli == null) {
            telemetry.error("tkn not found")
                    .send();
            return false;
        }

        if (namespace.isEmpty()) {
            namespace = tknCli.getNamespace();
        }

        if (confirmationMessage.isEmpty()) {
            confirmationMessage = getDefaultConfirmationMessage(resource.getName(), resource.getKind());
        }

        if (!skipConfirmatioDialog && !isSaveConfirmed(confirmationMessage)) {
            telemetry.result(VALUE_ABORTED)
                    .send();
            return false;
        }

        try {
            String resourceNamespace = CRDHelper.isClusterScopedResource(resource.getKind()) ? "" : namespace;
            boolean isNewResource = doSave(resourceNamespace, yaml, updateLabels, resource, tknCli);
            telemetry.property(PROP_RESOURCE_CRUD, (isNewResource ? VALUE_RESOURCE_CRUD_CREATE : VALUE_RESOURCE_CRUD_UPDATE))
                    .send();
        } catch (KubernetesClientException e) {
            String errorMsg = createErrorMessage(resource, e);
            telemetry.error(anonymizeResource(resource.getName(), namespace, errorMsg))
                    .send();
            logger.warn(errorMsg, e);
            // give a visual notification to user if an error occurs during saving
            throw new IOException(errorMsg, e);
        }
        return true;
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
        GenericKubernetesResource resource = tknCli.getCustomResource(tknCli.getNamespace(), name, crdContext);
        return resource != null;
    }

    private static boolean isSaveConfirmed(String confirmationMessage) {

        int resultDialog = UIHelper.executeInUI(() -> {
            MessageDialog messageDialog = new MessageDialog(confirmationMessage,
                    "Save to Cluster",
                    new String[]{YES_BUTTON, NO_BUTTON},
                    -1,
                    null);
            messageDialog.show();
            return messageDialog.getExitCode();
        });

        return resultDialog == Messages.OK;
    }

    private static boolean doSave(String namespace, String yaml, boolean updateLabels, GenericResource resource, Tkn tknCli) throws KubernetesClientException, IOException {
        CustomResourceDefinitionContext crdContext = CRDHelper.getCRDContext(resource.getApiVersion(), TreeHelper.getPluralKind(resource.getKind()));
        if (crdContext == null) {
            throw new IOException("Tekton file has not a valid format. ApiVersion field contains an invalid value.");
        }
        boolean newResource = true;
        if (CRDHelper.isRunResource(resource.getKind())) {
            tknCli.createCustomResource(namespace, crdContext, yaml);
        } else {
            namespace = CRDHelper.isClusterScopedResource(resource.getKind()) ? "" : namespace;
            GenericKubernetesResource customResourceMap = tknCli.getCustomResource(namespace, resource.getName(), crdContext);
            if (customResourceMap == null) {
                tknCli.createCustomResource(namespace, crdContext, yaml);
            } else {
                JsonNode customResource = CRDHelper.convertToJsonNode(customResourceMap);
                JsonNode labels = resource.getMetadata().get("labels");
                if (updateLabels && labels != null) {
                    ((ObjectNode) customResource.get("metadata")).set("labels", labels);
                }
                ((ObjectNode) customResource).set("spec", resource.getSpec());
                tknCli.editCustomResource(namespace, resource.getName(), crdContext, customResource.toString());
                newResource = false;
            }
        }
        return newResource;
    }

    public static void saveResource(String resourceAsYAML, String namespace, Tkn tkncli) throws IOException {
        try {
            GenericResource resource = getResource(resourceAsYAML);
            doSave(namespace, resourceAsYAML, false, resource, tkncli);
        } catch (KubernetesClientException e) {
            throw new IOException(e);
        }
    }

    private static String createErrorMessage(GenericResource resource, KubernetesClientException e) {
        Status errorStatus = e.getStatus();
        String errorMsg = "An error occurred while saving " + StringUtils.capitalize(resource.getKind()) + " " + resource.getName() + "\n";
        if (errorStatus != null && !Strings.isNullOrEmpty(errorStatus.getMessage())) {
            errorMsg += errorStatus.getMessage() + "\n";
        }
        return errorMsg;
    }

    public static GenericResource getResource(String yaml) throws IOException {
        return getResource(yaml, null);
    }

    private static GenericResource getResource(String yaml, ActionMessage telemetry) throws IOException {
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            return mapper.readValue(yaml, GenericResource.class);
        } catch (IOException e) {
            if (telemetry != null) {
                telemetry.error(e).send();
            }
            throw e;
        }
    }

    private static String getDefaultConfirmationMessage(String name, String kind) {
        return "Push changes for " + kind + " " + name + "?";
    }
}
