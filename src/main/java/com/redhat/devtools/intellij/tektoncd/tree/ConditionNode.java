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
package com.redhat.devtools.intellij.tektoncd.tree;

import io.fabric8.tekton.pipeline.v1alpha1.Condition;

public class ConditionNode extends ParentableNode<ConditionsNode> {
    private final Condition condition;
    public ConditionNode(TektonRootNode root, ConditionsNode parent, Condition condition) {
        super(root, parent, condition.getMetadata().getName());
        this.condition = condition;
    }

    public Condition getCondition() {
        return condition;
    }
}


