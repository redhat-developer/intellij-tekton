package com.redhat.devtools.intellij.common.utils;

import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TestDialog;
import com.intellij.testFramework.LightPlatformTestCase;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class DownloadHelperTest extends LightPlatformTestCase {
    private TestDialog previous;
    @Override
    public void setUp() throws Exception {
        super.setUp();
        previous = Messages.setTestDialog(TestDialog.OK);
        FileUtils.deleteDirectory(new File("cache"));
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        Messages.setTestDialog(previous);
        FileUtils.deleteDirectory(new File("cache"));
    }

    public void testThatGZIsDownloaded() throws IOException {
        String cmd = DownloadHelper.getInstance().downloadIfRequired("tkn", DownloadHelperTest.class.getResource("/tkn-test-gz.json"));
        assertNotNull(cmd);
        assertEquals("." + File.separatorChar + "cache" + File.separatorChar + "0.5.0" + File.separatorChar + "tkn", cmd);
        assertEquals(17, new File(cmd).length());
    }

    public void testThatTarGZIsDownloaded() throws IOException {
        String cmd = DownloadHelper.getInstance().downloadIfRequired("tkn", DownloadHelperTest.class.getResource("/tkn-test-tar.gz.json"));
        assertNotNull(cmd);
        assertEquals("." + File.separatorChar + "cache" + File.separatorChar + "0.5.0" + File.separatorChar + "tkn", cmd);
        assertEquals(17, new File(cmd).length());
    }
}
