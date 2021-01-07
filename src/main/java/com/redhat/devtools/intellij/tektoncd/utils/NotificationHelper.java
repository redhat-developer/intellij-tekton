/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc.
 ******************************************************************************/
package com.redhat.devtools.intellij.tektoncd.utils;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import com.redhat.devtools.intellij.common.utils.UIHelper;


import static com.redhat.devtools.intellij.tektoncd.Constants.NOTIFICATION_ID;

public class NotificationHelper {

    public static void notify(Project project, String title, String content, NotificationType type, boolean withBalloon) {
            Notification notification;
            notification = new Notification(NOTIFICATION_ID, title, content, type);
            Notifications.Bus.notify(notification);
            if (withBalloon) {
                notifyWithBalloon(project, content, type);
            }
    }

    public static void notifyWithBalloon(Project project, String content, NotificationType type) {
        UIHelper.executeInUI(() -> {
                StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
                JBPopupFactory.getInstance()
                    .createHtmlTextBalloonBuilder(content, (type == NotificationType.ERROR ? MessageType.ERROR : MessageType.INFO), null)
                    .setFadeoutTime(7500)
                    .createBalloon()
                    .show(RelativePoint.getNorthEastOf(statusBar.getComponent()), Balloon.Position.atRight);
        });

    }

}
