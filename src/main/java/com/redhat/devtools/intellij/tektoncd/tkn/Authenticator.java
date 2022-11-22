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
package com.redhat.devtools.intellij.tektoncd.tkn;

public class Authenticator {
    private String username, password, token;
    private boolean skipTls;

    public Authenticator(String username, String password, boolean skipTls) {
        this(username, password, "", skipTls);
    }

    public Authenticator(String token, boolean skipTls) {
        this("", "", token, skipTls);
    }

    public Authenticator(String username, String password, String token, boolean skipTls) {
        this.username = username;
        this.password = password;
        this.token = token;
        this.skipTls = skipTls;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getToken() {
        return token;
    }

    public boolean isSkipTls() {
        return skipTls;
    }
}
