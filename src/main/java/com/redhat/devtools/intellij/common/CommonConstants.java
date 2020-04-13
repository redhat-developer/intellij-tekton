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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;

public class CommonConstants {
    public static final String HOME_FOLDER = System.getProperty("user.home");

    public static final Key<Project> PROJECT = Key.create("com.redhat.devtools.intellij.common.project");
    public static final Key<Long> LAST_MODIFICATION_STAMP = Key.create("com.redhat.devtools.intellij.common.last.modification.stamp");

}
