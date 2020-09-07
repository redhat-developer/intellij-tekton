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
package com.redhat.devtools.intellij.tektoncd.utils.model;

import com.redhat.devtools.intellij.common.utils.YAMLHelper;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ConfigurationModel {
    Logger logger = LoggerFactory.getLogger(ConfigurationModel.class);
    protected String namespace, name, kind;

    public ConfigurationModel() {
        this.name = "";
        this.kind = "";
        this.namespace = "";
    }

    public ConfigurationModel(String configuration) {
        this();

        try {
            this.name = YAMLHelper.getStringValueFromYAML(configuration, new String[] {"metadata", "name"});
            this.kind = YAMLHelper.getStringValueFromYAML(configuration, new String[] {"kind"});
            this.namespace = YAMLHelper.getStringValueFromYAML(configuration, new String[] {"metadata", "namespace"});
        } catch (IOException e) {
            logger.warn(e.getLocalizedMessage());
        }
    }

    public String getName() {
        return name;
    }

    public String getKind() {
        return kind;
    }

    public String getNamespace() {
        return namespace;
    }
}
