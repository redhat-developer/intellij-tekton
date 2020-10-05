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

import java.io.IOException;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class UtilsTest {

    @Test
    public void checkStringReturnedHasCorrectLength() throws IOException {
        String randomString = Utils.getRandomString(8);
        assertFalse(randomString.contains("-"));
        assertEquals(randomString.length(), 8);
    }

    @Test
    public void checkStringReturnedIsNotCutIfItsLengthIsLessThanAsked() throws IOException {
        String randomString = Utils.getRandomString(800);
        assertFalse(randomString.contains("-"));
        assertTrue(randomString.length() < 800);
    }

    @Test
    public void checkStringReturnedIsAlwaysRandom() throws IOException {
        String randomString = Utils.getRandomString(8);
        assertFalse(randomString.contains("-"));
        assertEquals(randomString.length(), 8);
        String randomString2 = Utils.getRandomString(8);
        assertFalse(randomString2.contains("-"));
        assertEquals(randomString2.length(), 8);

        assertNotEquals(randomString, randomString2);
    }


}
