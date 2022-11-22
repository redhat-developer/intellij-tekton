/*******************************************************************************
 *  Copyright (c) 2022 Red Hat, Inc.
 *  Distributed under license by Red Hat, Inc. All rights reserved.
 *  This program is made available under the terms of the
 *  Eclipse Public License v2.0 which accompanies this distribution,
 *  and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.actions.bundle.toolbar;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.ui.components.JBList;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import com.redhat.devtools.intellij.tektoncd.tkn.Authenticator;
import com.redhat.devtools.intellij.tektoncd.tkn.Resource;
import com.redhat.devtools.intellij.tektoncd.tkn.Tkn;
import com.redhat.devtools.intellij.tektoncd.ui.bundle.BundleCacheItem;
import com.redhat.devtools.intellij.tektoncd.ui.bundle.BundleUtils;
import com.redhat.devtools.intellij.tektoncd.utils.DeployHelper;
import com.redhat.devtools.intellij.tektoncd.utils.NotificationHelper;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.Icon;
import java.io.IOException;
import java.util.Map;

import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.NAME_PREFIX_CRUD;
import static com.redhat.devtools.intellij.tektoncd.telemetry.TelemetryService.instance;
import static com.redhat.devtools.intellij.telemetry.core.util.AnonymizeUtils.anonymizeResource;

public class ImportBundleResourceAction extends DumbAwareAction {

    private static final Logger logger = LoggerFactory.getLogger(ImportBundleResourceAction.class);

    private Tkn tkn;
    private JBList<String> bundles;
    private JBList<Resource> resourcePanel;
    private Map<String, BundleCacheItem> bundleCache;
    private Map<String, String> cache;

    public ImportBundleResourceAction(String text, String description, Icon icon, JBList<String> bundles, JBList<Resource> resourcePanel, Map<String, BundleCacheItem> bundleCache, Map<String, String> cache, Tkn tkn) {
        super(text, description, icon);
        this.tkn = tkn;
        this.bundles = bundles;
        this.resourcePanel = resourcePanel;
        this.bundleCache = bundleCache;
        this.cache = cache;
    }
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        TelemetryMessageBuilder.ActionMessage telemetry = instance().action(NAME_PREFIX_CRUD + "import bundle");
        Resource resource = resourcePanel.getSelectedValue();
        String bundleName = bundles.getSelectedValue();
        String keyCache = BundleUtils.createCacheKey(bundleName, resource);
        final String[] resourceAsYaml = {cache.getOrDefault(keyCache, "")};

        ExecHelper.submit(() -> {
            try {
                if (resourceAsYaml[0].isEmpty()) {
                    BundleCacheItem item = bundleCache.getOrDefault(bundleName, null);
                    Authenticator authenticator = null;
                    if (item != null) {
                        authenticator = item.getAuthenticator();
                    }
                    resourceAsYaml[0] = tkn.getBundleResourceYAML(bundleName, resource, authenticator);
                }
                DeployHelper.saveResource(resourceAsYaml[0], tkn.getNamespace(), tkn);
                telemetry
                        .success()
                        .send();
                NotificationHelper.notify(e.getProject(), "Resource from Bundle imported successfully",
                        "The resource " + resource.type() + " " + resource.name() + " from bundle " + bundleName + " has been imported successfully.",
                        NotificationType.INFORMATION,
                        true);
            } catch (IOException ex) {
                logger.warn(ex.getLocalizedMessage(), ex);
                telemetry
                        .error(anonymizeResource(bundleName, null, ex.getMessage()))
                        .send();
                NotificationHelper.notify(e.getProject(), "Error importing bundle",
                        "An error occurred while importing the resource " + resource.type() + " " + resource.name() + ". Please check that your cluster is working fine and try again.",
                        NotificationType.ERROR,
                        true);
            }
        });
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Resource selected = resourcePanel.getSelectedValue();
        e.getPresentation().setEnabled(selected!=null);
    }
}
