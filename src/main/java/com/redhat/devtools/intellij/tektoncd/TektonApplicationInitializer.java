/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd;

import com.intellij.ide.ApplicationInitializedListener;
import com.redhat.devtools.intellij.tektoncd.utils.PluginClassLoaderPriority;
import io.fabric8.kubernetes.client.Adapters;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;
import io.fabric8.tekton.TektonV1alpha1ResourceMappingProvider;
import io.fabric8.tekton.TektonV1beta1ResourceMappingProvider;
import io.fabric8.tekton.client.TektonExtensionAdapter;

public class TektonApplicationInitializer implements ApplicationInitializedListener {
	@Override
	public void componentsInitialized() {
		PluginClassLoaderPriority.preferParent("org.jboss.tools.intellij.kubernetes", getClass().getClassLoader());
		Adapters.register(new TektonExtensionAdapter());
		KubernetesDeserializer.registerProvider(new TektonV1alpha1ResourceMappingProvider());
		KubernetesDeserializer.registerProvider(new TektonV1beta1ResourceMappingProvider());
	}
}
