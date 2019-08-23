package org.jboss.as.test.integration.ejb.access.log;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class AbstractServelLogAccessLogTestCase extends AbstractAccessLogTestCase {

    /**
     * Return path to WildFly server log file
     * <p>
     * WARNING: only works when invoked by {@link org.jboss.arquillian.container.test.api.RunAsClient} tests
     *
     * @return e.g. jboss-as/standalone/log/server.log
     */
    protected Path getServerLogPath() {
        String jbossHome = System.getProperty("jboss.home", null);
        if (jbossHome != null) return Paths.get(jbossHome, "standalone", "log", "server.log");
        return Paths.get(System.getProperty("jboss.inst", null), "standalone", "log", "server.log");
    }

    /**
     * Retrieve the path to server log file and store it so info can be shared by tests run in container / out of container
     */
    @Test
    @InSequence(-1)
    @RunAsClient
    public void testServerLog() throws IOException {
        Path serverLogPath = getServerLogPath();
        Assert.assertTrue(String.format("Server log file '%s' does not exist!", serverLogPath.toFile().getAbsolutePath()), serverLogPath.toFile().exists());
        writeTmpFile(
                serverLogPath,
                serverLogPath.toFile().length() // discard output generated so far
        );
    }

    @Test
    @InSequence(Integer.MAX_VALUE)
    @RunAsClient
    public void removeTmpFile() {
        Assert.assertTrue(getTmpFilePath().toFile().delete());
    }
}
