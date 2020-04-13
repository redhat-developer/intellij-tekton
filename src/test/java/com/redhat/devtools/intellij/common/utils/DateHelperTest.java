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

import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.assertEquals;

public class DateHelperTest {

    private static Instant startTime;

    @BeforeClass
    public static void init() {
        startTime = Instant.parse("2020-04-09T07:52:37Z");
    }

    @Test
    public void shouldOnlyPrintSeconds() {
        Instant endTime = Instant.parse("2020-04-09T07:52:57Z");
        String res = DateHelper.humanizeDate(startTime, endTime);
        assertEquals(res, "20 s ");
    }

    @Test
    public void shouldOnlyPrintMinutesAndSeconds() {
        Instant endTime = Instant.parse("2020-04-09T07:55:49Z");
        String res = DateHelper.humanizeDate(startTime, endTime);
        assertEquals(res, "3 m 12 s ");
    }

    @Test
    public void shouldOnlyPrintHoursAndMinutes() {
        Instant endTime = Instant.parse("2020-04-09T18:23:49Z");
        String res = DateHelper.humanizeDate(startTime, endTime);
        assertEquals(res, "10 h 31 m ");
    }

    @Test
    public void shouldOnlyPrintHoursAndMinutesWith0Minutes() {
        Instant endTime = Instant.parse("2020-04-09T15:52:49Z");
        String res = DateHelper.humanizeDate(startTime, endTime);
        assertEquals(res, "8 h 0 m ");
    }

    @Test
    public void shouldOnlyPrintDaysAndHours() {
        Instant endTime = Instant.parse("2020-04-19T04:22:11Z");
        String res = DateHelper.humanizeDate(startTime, endTime);
        assertEquals(res, "9 d 20 h ");
    }

    @Test
    public void shouldOnlyPrintDaysAndHoursWith0Hours() {
        Instant endTime = Instant.parse("2020-04-19T07:55:11Z");
        String res = DateHelper.humanizeDate(startTime, endTime);
        assertEquals(res, "10 d 0 h ");
    }

    @Test
    public void shouldOnlyPrintDaysAndHoursIfDifferenceIsXXDays() {
        Instant endTime = Instant.parse("2020-06-19T04:22:11Z");
        String res = DateHelper.humanizeDate(startTime, endTime);
        assertEquals(res, "70 d 20 h ");
    }

    @Test
    public void shouldOnlyPrintDaysIfDifferenceIsXXXDays() {
        Instant endTime = Instant.parse("2021-04-19T04:22:11Z");
        String res = DateHelper.humanizeDate(startTime, endTime);
        assertEquals(res, "374 d ");
    }
}
