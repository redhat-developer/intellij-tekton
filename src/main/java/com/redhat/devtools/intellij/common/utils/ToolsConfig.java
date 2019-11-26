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
package com.redhat.devtools.intellij.common.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ToolsConfig {
  public static class Tool {
    private Map<String, Platform> platforms = new HashMap<>();

    private String version;
    private String versionCmd;
    private String versionExtractRegExp;
    private String versionMatchRegExpr;
    private String baseDir;

    public Map<String, Platform> getPlatforms() {
      return platforms;
    }

    public void setPlatforms(Map<String, Platform> platforms) {
      this.platforms = platforms;
    }

    public String getVersion() {
      return version;
    }

    public void setVersion(String version) {
      this.version = version;
    }

    public String getVersionCmd() {
      return versionCmd;
    }

    public void setVersionCmd(String versionCmd) {
      this.versionCmd = versionCmd;
    }

    public String getVersionExtractRegExp() {
      return versionExtractRegExp;
    }

    public void setVersionExtractRegExp(String versionExtractRegExp) {
      this.versionExtractRegExp = versionExtractRegExp;
    }

    public String getVersionMatchRegExpr() {
      return versionMatchRegExpr;
    }

    public void setVersionMatchRegExpr(String versionMatchRegExpr) {
      this.versionMatchRegExpr = versionMatchRegExpr;
    }

    public String getBaseDir() {
      return baseDir;
    }

    public void setBaseDir(String baseDir) {
      this.baseDir = baseDir;
    }
  }

  public static class Platform {
    private URL url;
    private String cmdFileName;
    private String dlFileName;

    public URL getUrl() {
      return url;
    }

    public void setUrl(URL url) {
      this.url = url;
    }

    public String getCmdFileName() {
      return cmdFileName;
    }

    public void setCmdFileName(String cmdFileName) {
      this.cmdFileName = cmdFileName;
    }

    public String getDlFileName() {
      return dlFileName;
    }

    public void setDlFileName(String dlFileName) {
      this.dlFileName = dlFileName;
    }
  }

  private Map<String, Tool> tools = new HashMap<>();

  public Map<String, Tool> getTools() {
    return tools;
  }

  public void setTools(Map<String, Tool> tools) {
    this.tools = tools;
  }
}
