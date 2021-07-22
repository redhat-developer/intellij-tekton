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
package com.redhat.devtools.intellij.tektoncd.ui.hub;

import java.util.Optional;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;

public class HubMarketplaceTab extends HubDialogTab {

    public HubMarketplaceTab(HubModel model) {
        super(model);
    }

    @NotNull
    @Override
    protected JComponent createContentPanel() {
        HubItemPanelsBoard board = new HubItemPanelsBoard(model, (item, callback) -> myDetailsPage.show(item, callback));
        if (model.getIsPipelineView()) {
            return board.withAllPipelines().build(Optional.empty());
        } else {
            return board.withAllTasks().build(Optional.empty());
        }
    }

}
