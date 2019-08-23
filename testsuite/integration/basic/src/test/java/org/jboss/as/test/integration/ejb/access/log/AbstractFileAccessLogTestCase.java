package org.jboss.as.test.integration.ejb.access.log;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class AbstractFileAccessLogTestCase extends AbstractAccessLogTestCase {

    protected static final String EJB_ACCESS_LOG_FILE = "ejb-access.log";

    protected Path getLogPath() {
        String jbossHome = System.getProperty("jboss.home", null);
        if (jbossHome != null) return Paths.get(jbossHome, "standalone", "log", EJB_ACCESS_LOG_FILE);
        return Paths.get(System.getProperty("jboss.inst", null), "standalone", "log", EJB_ACCESS_LOG_FILE);
    }

    /* ==============================================
                        PRE-REQUISITE
       ============================================== */

    /**
     * Retrieve the path to server log file and store it so info can be shared by tests run in container / out of container
     */
    @Test
    @InSequence(-1)
    @RunAsClient
    public void testServerLog() throws IOException {
        Path serverLogPath = getLogPath();
        Assert.assertFalse(String.format("EJB access log file '%s' does already exist!", serverLogPath.toFile().getAbsolutePath()), serverLogPath.toFile().exists());
        writeTmpFile(
                serverLogPath,
                serverLogPath.toFile().length() // discard output generated so far
        );
    }

    @Test
    @InSequence(Integer.MAX_VALUE)
    @RunAsClient
    public void removeTmpFileAndEjbAccessLogFile() {
        Assert.assertTrue(getTmpFilePath().toFile().delete());
        Assert.assertTrue(getLogPath().toFile().delete());
    }
}
