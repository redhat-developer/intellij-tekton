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

public class PipelineSchemasTest extends SchemasTest {

    public void testPipelineWithStringParameter() {
        myFixture.configureByFile("schemas/pipeline1.yaml");
        myFixture.checkHighlighting();
    }

    public void testPipelineWithArrayStringParameter() {
        myFixture.configureByFile("schemas/pipeline2.yaml");
        myFixture.checkHighlighting();
    }

    public void testPipelineWithTimeoutInHours() {
        myFixture.configureByFile("schemas/pipeline-timeout-hours.yaml");
        myFixture.checkHighlighting();
    }

    public void testPipelineWithTimeoutInMinutes() {
        myFixture.configureByFile("schemas/pipeline-timeout-minutes.yaml");
        myFixture.checkHighlighting();
    }

    public void testPipelineWithTimeoutInSeconds() {
        myFixture.configureByFile("schemas/pipeline-timeout-seconds.yaml");
        myFixture.checkHighlighting();
    }

    public void testPipelineWithTimeoutInMillisSeconds() {
        myFixture.configureByFile("schemas/pipeline-timeout-milliseconds.yaml");
        myFixture.checkHighlighting();
    }

    public void testPipelineWithTimeoutInMicroSeconds() {
        myFixture.configureByFile("schemas/pipeline-timeout-microseconds.yaml");
        myFixture.checkHighlighting();
    }

    public void testPipelineWithTimeoutInMicroSeconds1() {
        myFixture.configureByFile("schemas/pipeline-timeout-microseconds1.yaml");
        myFixture.checkHighlighting();
    }

    public void testPipelineWithTimeoutInMicroSeconds2() {
        myFixture.configureByFile("schemas/pipeline-timeout-microseconds2.yaml");
        myFixture.checkHighlighting();
    }

    public void testPipelineWithTimeoutInNanoSeconds() {
        myFixture.configureByFile("schemas/pipeline-timeout-nanoseconds.yaml");
        myFixture.checkHighlighting();
    }

    public void testPipelineWithTimeoutWithSeveralUnits() {
        myFixture.configureByFile("schemas/pipeline-timeout-several-units.yaml");
        myFixture.checkHighlighting();
    }

    public void testPipelineWithFinally() {
        myFixture.configureByFile("schemas/pipeline-finally.yaml");
        myFixture.checkHighlighting();
    }

    public void testPipelineWithWhen() {
        myFixture.configureByFile("schemas/pipeline-when.yaml");
        myFixture.checkHighlighting();
    }

    public void testPipelineWithCustomTask() {
        myFixture.configureByFile("schemas/pipeline-custom-task.yaml");
        myFixture.checkHighlighting();
    }
}
