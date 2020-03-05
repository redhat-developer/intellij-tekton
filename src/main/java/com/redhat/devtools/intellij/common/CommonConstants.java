/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.common;

import com.intellij.openapi.util.Key;

public class CommonConstants {
    public static final String HOME_FOLDER = System.getProperty("user.home");
    public static final Key<String> TEKTON_RS = Key.create("tekton.resource");
    public static final Key<String> TEKTON_NS = Key.create("tekton.namespace");
    public static final Key<String> TEKTON_PLURAL = Key.create("tekton.plural");
}
