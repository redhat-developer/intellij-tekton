/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.schemas;

import org.junit.Test;

public class TriggerTemplateSchemasTest extends SchemasTest {

    @Test
    public void testTriggerTemplate() {
        myFixture.configureByFile("schemas/trigger-template1.yaml");
        myFixture.checkHighlighting();
    }
}
