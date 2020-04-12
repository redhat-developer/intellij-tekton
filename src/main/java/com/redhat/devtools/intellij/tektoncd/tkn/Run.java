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

import java.time.Instant;
import java.util.Optional;

public interface Run {
    String getName();
    String getKind();
    Optional<Boolean> isCompleted();
    Instant getStartTime();
    Instant getCompletionTime();

    public static Run of(String name, String kind, Optional<Boolean> completed, Instant startTime, Instant completionTime) {
        return new Run() {

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getKind() {
                return kind;
            }

            @Override
            public Optional<Boolean> isCompleted() {
                return completed;
            }

            @Override
            public Instant getStartTime() {
                return startTime;
            }

            @Override
            public Instant getCompletionTime() {
                return completionTime;
            }
        };
    }
}

