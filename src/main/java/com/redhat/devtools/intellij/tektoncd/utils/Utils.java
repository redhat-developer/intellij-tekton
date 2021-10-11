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

import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Version;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static String getRandomString(int length) {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        if (uuid.length() > length) {
            return uuid.substring(0, length);
        }
        return uuid;
    }

    /**
     * Extract the digits and format from a unique string (e.g 10Mi -> Pair(10, Mi))
     * @param value string representing a size
     * @return a pair having as first value the digits and as second value the format
     */
    public static Pair<String, String> getDigitsAndFormatAsPair(String value) {
        Pattern p = Pattern.compile("[0-9]+");
        Matcher m = p.matcher(value);
        if (m.find()) {
            String size = m.group();
            String format = value.replace(size, "");
            return Pair.create(size, format);
        }
        return Pair.empty();
    }

    public static boolean isActiveTektonVersionOlder(String activeVersionS, String versionS) {
        Version activeVersion = Version.parseVersion(activeVersionS);
        Version version = Version.parseVersion(versionS);
        if (activeVersion == null || version == null) {
            return false;
        }
        return activeVersion.compareTo(version) > 0;
    }
}
