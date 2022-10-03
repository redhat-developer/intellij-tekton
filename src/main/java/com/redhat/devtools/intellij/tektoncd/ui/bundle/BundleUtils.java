/*******************************************************************************
 *  Copyright (c) 2022 Red Hat, Inc.
 *  Distributed under license by Red Hat, Inc. All rights reserved.
 *  This program is made available under the terms of the
 *  Eclipse Public License v2.0 which accompanies this distribution,
 *  and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.ui.bundle;

import com.redhat.devtools.intellij.tektoncd.tkn.Resource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BundleUtils {

    private static final Pattern TAG_PATTERN = Pattern.compile(":.+$");

    public static String createCacheKey(String bundle, Resource resource) {
        return bundle + "-" + resource.type() + "-" + resource.name();
    }

    public static String cleanImage(String image) {
        Matcher matcher = TAG_PATTERN.matcher(image);
        if (!matcher.find()) {
            return image + ":latest";
        }
        return image;
    }
}
