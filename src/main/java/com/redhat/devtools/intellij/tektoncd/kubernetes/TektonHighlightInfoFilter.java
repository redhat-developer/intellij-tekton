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

import com.intellij.psi.PsiFile;
import com.redhat.devtools.intellij.common.kubernetes.KubernetesHighlightInfoFilter;
import com.redhat.devtools.intellij.common.validation.KubernetesTypeInfo;

public class TektonHighlightInfoFilter extends KubernetesHighlightInfoFilter {
    @Override
    public boolean isCustomFile(PsiFile file) {
        KubernetesTypeInfo info = KubernetesTypeInfo.extractMeta(file);
        return info.getApiGroup().startsWith("tekton.dev") || info.getApiGroup().startsWith("triggers.tekton.dev");
    }
}
