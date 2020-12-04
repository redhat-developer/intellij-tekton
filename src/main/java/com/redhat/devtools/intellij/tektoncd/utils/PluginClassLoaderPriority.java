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
package com.redhat.devtools.intellij.tektoncd.utils;

import com.intellij.ide.plugins.cl.PluginClassLoader;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;

import java.lang.reflect.Field;

public class PluginClassLoaderPriority {

	/**
	 * Prioritizes the classloader with the given id within the list of parent class loaders for the given class loader.
	 * Does nothing if there's no parent class loader with the given id.
	 * This is required bcs of a {@link <a href="https://youtrack.jetbrains.com/issue/IJSDK-849">IJSDK-849</a>}.
	 *
	 * @param id     id of the parent classloader
	 * @param loader the class loader who's parent classloaders should be re-prioritized
	 */
	public static void preferParent(String id, ClassLoader loader) {
		if (!(loader instanceof PluginClassLoader)) {
			return;
		}
		try {
			ClassLoader[] parents = getParents((PluginClassLoader) loader);
			int indexOf = indexOfIn(id, parents);
			if (indexOf == -1) {
				return;
			}
			ClassLoader preferred = parents[indexOf];
			shiftRight(indexOf, parents);
			parents[0] = preferred;

		} catch (NoSuchFieldException | IllegalAccessException e) {
			Logger.getInstance(PluginClassLoaderPriority.class).error(e.getLocalizedMessage(), e);
		}
	}

	private static int indexOfIn(String id, ClassLoader[] parents) throws NoSuchFieldException, IllegalAccessException {
		if (parents == null
			|| parents.length == 0) {
			return -1;
		}
		for (int i = 0; i < parents.length; i++) {
			if (isPluginClassLoader(id, parents[i])) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Shifts the elements at index 0 up to the given upper bound to the right
	 *
	 * @param upperBound
	 * @param parents
	 */
	private static void shiftRight(int upperBound, ClassLoader[] parents) {
		if (upperBound == 0) {
			return;
		}
		for (int j = upperBound; j > 0; j--) {
			parents[j] = parents[j - 1];
		}
	}

	private static ClassLoader[] getParents(PluginClassLoader loader) throws NoSuchFieldException, IllegalAccessException {
		return getPrivateField("myParents", loader);
	}

	private static Boolean isPluginClassLoader(String id, ClassLoader loader) throws NoSuchFieldException, IllegalAccessException {
		if (!(loader instanceof PluginClassLoader)) {
			return false;
		}
		PluginId pluginId = getPrivateField("myPluginId", (PluginClassLoader) loader);
		return pluginId == null
				|| id.equals(pluginId.getIdString());
	}

	private static <T> T getPrivateField(String name, PluginClassLoader loader) throws NoSuchFieldException, IllegalAccessException {
		Field field = loader.getClass().getDeclaredField(name);
		field.setAccessible(true);
		return (T) field.get(loader);
	}
}
