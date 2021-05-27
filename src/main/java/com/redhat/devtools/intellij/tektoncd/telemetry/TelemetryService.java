/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.telemetry;

import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;
import com.redhat.devtools.intellij.telemetry.core.util.Lazy;

public class TelemetryService {

    public static final String NAME_PREFIX_CRUD = "crud-";
    public static final String NAME_PREFIX_DIAG = "diag-";
    public static final String NAME_PREFIX_START_STOP = "start_stop-";
    public static final String NAME_PREFIX_MISC = "misc-";
    public static final String PROP_RESOURCE_KIND = "resource_kind";
    public static final String PROP_RESOURCE_VERSION = "resource_version";
    public static final String PROP_RESOURCE_CRUD = "resource_crud";
    public static final String VALUE_RESOURCE_CRUD_CREATE = "create";
    public static final String VALUE_RESOURCE_CRUD_UPDATE = "update";
    public static final String VALUE_ABORTED = "aborted";
    public static final String PROP_RESOURCE_RELATED = "resource_related";
    public static final String KUBERNETES_VERSION = "kubernetes_version";
    public static final String IS_OPENSHIFT = "is_openshift";
    public static final String OPENSHIFT_VERSION = "openshift_version";

    private static final TelemetryService INSTANCE = new TelemetryService();

    private final Lazy<TelemetryMessageBuilder> builder = new Lazy<>(() -> new TelemetryMessageBuilder(TelemetryService.class.getClassLoader()));

    public static TelemetryMessageBuilder instance() {
        return INSTANCE.builder.get();
    }
}
