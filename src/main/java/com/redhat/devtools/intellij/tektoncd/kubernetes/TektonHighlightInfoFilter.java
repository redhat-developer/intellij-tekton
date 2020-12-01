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
package com.redhat.devtools.intellij.tektoncd.kubernetes;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoFilter;
import com.intellij.psi.PsiFile;
import com.redhat.devtools.intellij.tektoncd.validation.KubernetesTypeInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class TektonHighlightInfoFilter implements HighlightInfoFilter {
    /*
     * as there is no way to find the originating plugin from an highlight info, we are looking at the
     * tooltip that relates to the inspection tool that generated it.
     */
    private static final Pattern TOOLTIP_REGEXP = Pattern.compile(".*href=\"#inspection\\/Kubernetes.*\".*");

    @Override
    public boolean accept(@NotNull HighlightInfo highlightInfo, @Nullable PsiFile file) {
        return !isKubernetesHighlight(highlightInfo) || !isTektonFile(file);
    }

    private boolean isTektonFile(PsiFile file) {
        KubernetesTypeInfo info = KubernetesTypeInfo.extractMeta(file);
        return info.getApiGroup().startsWith("tekton.dev") || info.getApiGroup().startsWith("trigger.tekton.dev");
    }

    private boolean isKubernetesHighlight(HighlightInfo highlightInfo) {
        return highlightInfo.getToolTip() != null && TOOLTIP_REGEXP.matcher(highlightInfo.getToolTip()).matches();
    }
}
