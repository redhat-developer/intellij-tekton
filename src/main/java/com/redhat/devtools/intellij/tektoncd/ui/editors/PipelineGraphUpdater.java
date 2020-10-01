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
package com.redhat.devtools.intellij.tektoncd.ui.editors;

import com.mxgraph.view.mxGraph;
import io.fabric8.tekton.pipeline.v1beta1.Pipeline;

import java.io.IOException;

public class PipelineGraphUpdater extends AbstractPipelineGraphUpdater<Pipeline> {

    @Override
    public Pipeline adapt(String content) {
        try {
            return MAPPER.readValue(content, Pipeline.class);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void update(Pipeline content, mxGraph graph) {
        update(content, content.getSpec(), graph);
    }
}
