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

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.io.HttpRequests;
import com.redhat.devtools.intellij.common.CommonConstants;
import com.twelvemonkeys.lang.Platform;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class DownloadHelper {
    private static final UnaryOperator<InputStream> UNCOMPRESSOR = (input -> {
        try {
            return new CompressorStreamFactory().createCompressorInputStream(input);
        } catch (CompressorException e) {
            throw new RuntimeException(e);
        }
    });

    private static final UnaryOperator<InputStream> UNTAR = (input ->  new TarArchiveInputStream(input));

    private static final UnaryOperator<InputStream> UNZIP = (input ->  new ZipArchiveInputStream(input));

    private static final Map<String, UnaryOperator<InputStream>> MAPPERS = new HashMap<>();

    static {
        MAPPERS.put("gz", UNCOMPRESSOR);
        MAPPERS.put("zip", UNZIP);
        MAPPERS.put("tar", UNTAR);
    }

    private DownloadHelper() {
    }

    private static DownloadHelper INSTANCE;

    public static DownloadHelper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DownloadHelper();
        }
        return INSTANCE;
    }

    /**
     * Download tool if required. First look at PATH then use the configuration file provided by the url to download the
     * tool.
     * The format of the file is the following:
     * <pre>
     * {
     *   "tools": {
     *     "tool": {
     *       "version": "1.0.0",
     *       "versionCmd": "version", //the argument(s) to add to cmdFileName to get the version
     *       "versionExtractRegExp": "", //the regular expression to extract the version string from the version command
     *       "versionMatchRegExpr": "", //the regular expression use to match the extracted version to decide if download if required
     *       "baseDir": "" //the basedir to install to, a sub folder named after version will be created, can use $HOME
     *       "platforms": {
     *         "win": {
     *           "url": "https://tool.com/tool/v1.0.0/odo-windows-amd64.exe.tar.gz",
     *           "cmdFileName": "tool.exe",
     *           "dlFileName": "tool-windows-amd64.exe.gz"
     *         },
     *         "osx": {
     *           "url": "https://tool.com/tool/v1.0.0/odo-darwin-amd64.tar.gz",
     *           "cmdFileName": "tool",
     *           "dlFileName": "tool-darwin-amd64.gz"
     *         },
     *         "lnx": {
     *           "url": "https://tool.com/tool/v1.0.0/odo-linux-amd64.tar.gz",
     *           "cmdFileName": "odo",
     *           "dlFileName": "odo-linux-amd64.gz"
     *         }
     *       }
     *     }
     *   }
     * }
     * </pre>
     *
     * @param toolName
     * @param url
     * @return
     * @throws IOException
     */
    public String downloadIfRequired(String toolName, URL url) throws IOException {
        ToolsConfig config = ConfigHelper.loadToolsConfig(url);
        ToolsConfig.Tool tool = config.getTools().get(toolName);
        if (tool == null) {
            throw new IOException("Tool " + toolName + " not found in config file " + url);
        }
        ToolsConfig.Platform platform = tool.getPlatforms().get(Platform.os().id());
        String command = platform.getCmdFileName();
        String version = getVersionFromPath(tool, platform);
        if (!areCompatible(version, tool.getVersionMatchRegExpr())) {
            Path path = Paths.get(tool.getBaseDir().replace("$HOME", CommonConstants.HOME_FOLDER), "cache", tool.getVersion(), command);
            if (!Files.exists(path)) {
                final Path dlFilePath = path.resolveSibling(platform.getDlFileName());
                final String cmd = path.toString();
                if (isDownloadAllowed(toolName, version, tool.getVersion())) {
                    command = ProgressManager.getInstance().run(new Task.WithResult<String, IOException>(null, "Downloading " + toolName, true) {
                        @Override
                        public String compute(@NotNull ProgressIndicator progressIndicator) throws IOException {
                            return HttpRequests.request(platform.getUrl().toString()).connect(request -> {
                               downloadFile(request.getInputStream(), dlFilePath, progressIndicator, request.getConnection().getContentLength());
                               if (progressIndicator.isCanceled()) {
                                   throw new IOException("Cancelled");
                               } else {
                                   uncompress(dlFilePath, path);
                                   return cmd;
                               }
                            });
                        }
                    });
                }
            } else {
                command = path.toString();
            }
        }
        return command;
    }

    private boolean isDownloadAllowed(String tool, String currentVersion, String requiredVersion) {
        return Messages.showYesNoCancelDialog(StringUtils.isEmpty(currentVersion) ? tool + " not found , do you want to download " + tool + " " + requiredVersion + " ?" : tool + " " + currentVersion + " found, required version is " + requiredVersion + ", do you want to download " + tool + " ?", tool + " tool required", Messages.getQuestionIcon()) == Messages.YES;
    }

    private boolean areCompatible(String version, String versionMatchRegExpr) {
        boolean compatible = true;
        if (StringUtils.isNotBlank(versionMatchRegExpr)) {
            Pattern pattern = Pattern.compile(versionMatchRegExpr);
            compatible = pattern.matcher(version).matches();
        } else if (StringUtils.isBlank(version)) {
            compatible = false;
        }
        return compatible;
    }

    private String getVersionFromPath(ToolsConfig.Tool tool, ToolsConfig.Platform platform) {
        String version = "";
        try {
            Pattern pattern = Pattern.compile(tool.getVersionExtractRegExp());
            String[] arguments = tool.getVersionCmd().split(" ");
            String output = ExecHelper.execute(platform.getCmdFileName(), false, arguments);
            try (BufferedReader reader = new BufferedReader(new StringReader(output))) {
                version = reader.lines().
                        map(line -> pattern.matcher(line)).
                        filter(matcher -> matcher.matches()).
                        map(matcher -> matcher.group(1)).
                        findFirst().orElse("");
            }
        } catch (IOException e) {
        }
        return version;

    }

    private static void downloadFile(InputStream input, Path dlFileName, ProgressIndicator progressIndicator, long size) throws IOException {
        byte[] buffer = new byte[4096];
        Files.createDirectories(dlFileName.getParent());
        try (OutputStream output = Files.newOutputStream(dlFileName)) {
            int lg;
            long accumulated = 0;
            while (((lg = input.read(buffer)) > 0) && !progressIndicator.isCanceled()) {
                output.write(buffer, 0, lg);
                accumulated += lg;
                progressIndicator.setFraction((double) accumulated / size);
            }
        }
    }

    private InputStream checkTar(InputStream stream) throws IOException {
            TarArchiveInputStream tarStream = new TarArchiveInputStream(stream);
            if (tarStream.getNextTarEntry() != null) {
                return tarStream;
            } else {
                throw new IOException("No TAR entry found in " + stream);
            }
    }

    private InputStream mapStream(String filename, InputStream input) {
        String extension;
        while (((extension = FilenameUtils.getExtension(filename)) != null) && MAPPERS.containsKey(extension)) {
            filename = FilenameUtils.removeExtension(filename);
            input = MAPPERS.get(extension).apply(input);
        }
        return input;
    }

    private void uncompress(Path dlFilePath, Path cmd) throws IOException {
        try (InputStream input = new BufferedInputStream(Files.newInputStream(dlFilePath))) {
            InputStream subStream = mapStream(dlFilePath.toString(), input);
            if (subStream instanceof ArchiveInputStream) {
                ArchiveEntry entry;

                while ((entry = ((ArchiveInputStream)subStream).getNextEntry()) != null) {
                    save(subStream, cmd.resolveSibling(entry.getName()), entry.getSize());
                }
            } else {
                save(subStream, cmd, -1L);
            }
            } catch (RuntimeException e) {
            throw new IOException(e);
        }
    }

    private void save(InputStream source, Path destination, long length) throws IOException {
        try (OutputStream stream = Files.newOutputStream(destination)) {
            if (length == -1L) {
                IOUtils.copy(source, stream);
            } else {
                IOUtils.copyLarge(source, stream, 0L, length);
            }
        }
        destination.toFile().setExecutable(true);
    }
}
