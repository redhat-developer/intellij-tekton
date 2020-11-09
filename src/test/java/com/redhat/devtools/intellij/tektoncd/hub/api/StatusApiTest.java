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
package com.redhat.devtools.intellij.tektoncd.hub.api;

import com.redhat.devtools.intellij.tektoncd.hub.model.HubService;
import com.redhat.devtools.intellij.tektoncd.hub.invoker.ApiException;
import com.redhat.devtools.intellij.tektoncd.hub.model.StatusResponseBody;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class StatusApiTest {

    @Test
    public void checkStatusCall() throws ApiException {
        StatusApi statusApi = new StatusApi();
        StatusResponseBody response = statusApi.statusStatus();
        assertNotNull(response);
        assertNotNull(response.getServices());
        assertFalse(response.getServices().isEmpty());
        for(HubService service : response.getServices()) {
            assertNotNull(service);
            assertEquals(HubService.StatusEnum.OK, service.getStatus());
        }
    }
}
