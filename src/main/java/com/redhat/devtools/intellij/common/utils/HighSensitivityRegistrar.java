/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.common.utils;

import com.intellij.openapi.util.SystemInfo;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchService;

/**
 * A class that allows testing and grabbing SensitivityWatchEventModifier#HIGH
 * reflectively. This allows the code to run in jdks where it does not exists
 * (ex. IBM, etc.). SensitivityWatchEventModifier#HIGH is required for the
 * {@link WatchService} to work ~reliably on MacOS where no native hooks but
 * polling is used.
 * 
 * @see "https://bugs.openjdk.java.net/browse/JDK-7133447"
 * @see "https://stackoverflow.com/questions/9588737/is-java-7-watchservice-slow-for-anyone-else"
 * 
 * @author Andre Dietisheim
 */
public class HighSensitivityRegistrar {

	private static final String HIGH = "HIGH";
	private static final String QUALIFIED_CLASSNAME = "com.sun.nio.file.SensitivityWatchEventModifier";

	public boolean isRequired() {
		return isMac();
	}

	/**
	 * for testing purposes
	 **/
	protected boolean isMac() {
		return SystemInfo.isMac;
	}

	public boolean exists() {
		return get() != null;
	}

	/**
	 * Returns the SensitivityWatchEventModifier#HIGH if it exists (on sun jvms or
	 * openjdk), {@code null} otherwise.
	 *
	 * @return
	 */
	public Modifier get() {
		try {
			Class<Modifier> sensitivityClass = getSensitivityWatchEventModifierClass();
			return getEnumConstant(HIGH, sensitivityClass.getEnumConstants());
		} catch (ClassNotFoundException | SecurityException e) {
			return null;
		}
	}

	public void registerService(Path path, WatchEvent.Kind<Path>[] kinds, WatchService service) throws IOException {
		if (isRequired()
				&& exists()) {
			path.register(service, kinds, get());
		} else {
			path.register(service, kinds);
		}

	}

	/**
	 * for testing purposes
	 **/
	protected Class<Modifier> getSensitivityWatchEventModifierClass() throws ClassNotFoundException {
		return (Class<Modifier>) Class.forName(QUALIFIED_CLASSNAME);
	}

	private Modifier getEnumConstant(String name, Modifier[] modifiers) {
		for (Modifier modifier : modifiers) {
			if (name.equals(modifier.name())) {
				return modifier;
			}
		}
		return null;
	}
}
