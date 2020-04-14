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
package com.redhat.devtools.intellij.common.utils;

import java.time.Duration;
import java.time.Instant;

public class DateHelper {

    public static String humanizeDate(Instant start) {
        return humanizeDate(start,  Instant.now());
    }

    public static String humanizeDate(Instant start, Instant end) {
        long seconds = Duration.between(start, end).getSeconds();
        int days = (int) (seconds / (24 * 3600));
        seconds = seconds % (24 * 3600);
        int hours = (int) (seconds / 3600);
        seconds %= 3600;
        int minutes = (int) (seconds / 60);
        seconds %= 60;

        String date = "";
        if (days > 0) {
            date += days + " d ";
        }
        if (date.length() < 6) {
            if (hours > 0 || days > 0) {
                date += hours + " h ";
            }
        }
        if (date.length() < 6) {
            if (minutes > 0 || hours > 0) {
                date += minutes + " m ";
            }
        }
        if (date.length() < 6) {
            date += seconds + " s ";
        }
        return date;
    }
}
