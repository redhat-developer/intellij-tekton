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
package com.redhat.devtools.intellij.tektoncd.tkn;

import java.util.Optional;

public interface PipelineRun {
    String getName();
    Optional<Boolean> isCompleted();
    String getStartTimeText();
    String getCompletionTimeText();

    public static PipelineRun of(String name, Optional<Boolean> completed, String startTimeText, String completionTimeText) {
        return new PipelineRun() {

            @Override
            public String getName() {
                return name;
            }

            @Override
            public Optional<Boolean> isCompleted() {
                return completed;
            }

            @Override
            public String getStartTimeText() { return startTimeText; }

            @Override
            public String getCompletionTimeText() {
                return completionTimeText;
            }
        };
    }
}
