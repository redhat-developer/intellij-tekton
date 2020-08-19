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
package com.redhat.devtools.intellij.tektoncd.tkn;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TestDialog;
import com.redhat.devtools.intellij.common.utils.JSONHelper;
import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import com.redhat.devtools.intellij.tektoncd.BaseTest;
import com.redhat.devtools.intellij.tektoncd.utils.CRDHelper;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import java.io.IOException;
import java.util.Map;
import org.junit.After;
import org.junit.Before;

public class TknCliTest extends BaseTest {

    private TestDialog previousTestDialog;
    protected Tkn tkn;
    protected static final String NAMESPACE = "testns";

    @Before
    public void init() throws Exception {
        previousTestDialog = Messages.setTestDialog(new TestDialog() {
            @Override
            public int show(String message) {
                return 0;
            }
        });
        tkn = TknCliFactory.getInstance().getTkn(project).get();
    }

    @After
    public void shutdown() {
        Messages.setTestDialog(previousTestDialog);
    }

    protected void saveResource(String resourceBody, String namespace, String kind_plural) throws IOException{
        String name = YAMLHelper.getStringValueFromYAML(resourceBody, new String[] {"metadata", "name"});
        String apiVersion = YAMLHelper.getStringValueFromYAML(resourceBody, new String[] {"apiVersion"});
        JsonNode spec = YAMLHelper.getValueFromYAML(resourceBody, new String[] {"spec"});
        CustomResourceDefinitionContext crdContext = CRDHelper.getCRDContext(apiVersion, kind_plural);

        try {
            String resourceNamespace = CRDHelper.isClusterScopedResource(kind_plural) ? "" : namespace;
            Map<String, Object> resource = tkn.getCustomResource(resourceNamespace, name, crdContext);
            if (resource == null) {
                tkn.createCustomResource(resourceNamespace, crdContext, resourceBody);
            } else {
                JsonNode customResource = JSONHelper.MapToJSON(resource);
                ((ObjectNode) customResource).set("spec", spec);
                tkn.editCustomResource(resourceNamespace, name, crdContext, customResource.toString());
            }
        } catch (KubernetesClientException e) {
            throw new IOException(e.getLocalizedMessage());
        }
    }

    protected JsonNode getSpecFromResource(String resourceBody) throws IOException {
        return YAMLHelper.getValueFromYAML(resourceBody, new String[] {"spec"});
    }
}
