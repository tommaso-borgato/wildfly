package org.jboss.as.test.integration.ejb.access.log;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;

import java.io.IOException;
import java.io.PrintStream;

public abstract class AbstractConsoleAccessLogTestCase extends AbstractAccessLogTestCase {
    private static final int DFT_TIMEOUT = 60;
    protected ServerStdout serverStdout;
    private PrintStream currentStdout;

    @Before
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public void setup() {
        // Capture the current stdout to be replaced then replace stdout
        currentStdout = System.out;
        serverStdout = new ServerStdout(currentStdout);
        System.setOut(new PrintStream(serverStdout));
        // discard output generated so far
        serverStdout.getNewLinesSync();
    }

    @After
    public void tearDown() throws IOException {
        // Replaced with the captured stdout
        System.setOut(currentStdout);
        //TODO: executeOperation(client.getControllerClient(), Operations.createRemoveOperation(CONSOLE_ACCESS_LOG_ADDRESS), false);
    }

    @Override
    @Ignore("Test is ignored as must run as client to capture console output")
    public void testSFSB() throws Exception {
    }

    @Override
    @Ignore("Test is ignored as must run as client to capture console output")
    public void testSLSB() throws Exception {
    }

    @Override
    @Ignore("Test is ignored as must run as client to capture console output")
    public void testSLSBSecured() throws Exception {
    }

    @Override
    @Ignore("Test is ignored as must run as client to capture console output")
    public void testSFSBSecured() throws Exception {
    }
}
