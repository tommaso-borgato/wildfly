package org.jboss.as.test.integration.ejb.access.log;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.as.test.integration.common.HttpRequest;
import org.jboss.as.test.shared.CLIServerSetupTask;
import org.jboss.as.test.shared.ServerReload;
import org.jboss.as.test.shared.TimeoutUtil;
import org.jboss.as.test.shared.integration.ejb.security.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REMOVE;
import static org.junit.Assert.assertEquals;

/**
 * Test EJB access logs;
 * <p>
 * Various kinds of EJBs are deployed on the server and then accessed via a Servlet where the EJB is injected;
 * After access the logs are verified;
 *
 * This test case focuses on ejb access logs written to multiple locations:
 * <ul>
 *     <li>the server console output</li>
 *     <li>server log file "standalone/log/server.log"</li>
 * </ul>
 *
 * @author tborgato <a href="mailto:tborgato@redhat.com">Tommaso Borgato</a>
 */
@RunWith(Arquillian.class)
@ServerSetup(ConsoleAndServerLogAccessLogTestCase.EjbAccessLogSetupTask.class)
public class ConsoleAndServerLogAccessLogTestCase extends AbstractServelLogAccessLogTestCase {
    private static final AccessLogFormat ACCESS_LOG_FORMAT = AccessLogFormat.LONG;
    private static final int DFT_TIMEOUT = 60;
    protected ServerStdout serverStdout;
    private PrintStream currentStdout;

    @ArquillianResource
    private URL url;

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
    }

    private String performCall(String urlPattern) throws Exception {
        return HttpRequest.get(url.toExternalForm() + urlPattern, 10, SECONDS);
    }

    @Override
    @Ignore("Test is ignored as must run as client to capture console output")
    public void testSFSB() throws Exception {
    }

    @Override
    @Test
    @InSequence(1)
    @RunAsClient
    public void testSLSB() throws Exception {
        String result = performCall("slsbremote");
        assertEquals("ECHO[TUTTO A POSTO A FERRAGOSTO]", result);
    }

    @Override
    @Ignore("Test is ignored as must run as client to capture console output")
    public void testSLSBSecured() throws Exception {
    }

    @Override
    @Ignore("Test is ignored as must run as client to capture console output")
    public void testSFSBSecured() throws Exception {
    }

    @Override
    @Ignore("Test is ignored as must run as client to capture console output")
    public void testRemoteSLSB() throws Exception {
    }

    @Override
    @Ignore("Test is ignored as must run as client to capture console output")
    public void testRemoteSLSBSecured() throws Exception {
    }

    @Override
    @Ignore("Test is ignored as must run as client to capture console output")
    public void testRemoteSFSB() throws Exception {
    }

    @Override
    @Ignore("Test is ignored as must run as client to capture console output")
    public void testRemoteSFSBSecured() throws Exception {
    }

    @Deployment
    public static Archive createDeployment() {
        final JavaArchive ejbJarOne = ShrinkWrap.create(JavaArchive.class, MODULE_NAME + ".jar")
                .addClasses(SLSBRemote.class, SLSBLocal.class, SLSB.class)
                .addClasses(SFSBRemote.class, SFSBLocal.class, SFSB.class);
        final JavaArchive libJar = ShrinkWrap.create(JavaArchive.class, "bean-interfaces.jar")
                .addClasses(AccessLog.class, AccessLogFormat.class, ServerStdout.class, ServerLog.class, TimeoutUtil.class)
                .addClasses(EjbAccessLogSetupTask.class, CLIServerSetupTask.class, ServerSetupTask.class)
                .addClasses(Util.class, AbstractAccessLogTestCase.class, AbstractServelLogAccessLogTestCase.class, ConsoleAndServerLogAccessLogTestCase.class);
        final WebArchive war = ShrinkWrap.create(WebArchive.class, MODULE_NAME + ".war")
                .addClasses(CallSLSBLocalServlet.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebInfResource(new StringAsset(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<web-app version=\"3.1\"\n" +
                                "         xmlns=\"http://xmlns.jcp.org/xml/ns/javaee\"\n" +
                                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                "         xsi:schemaLocation=\"\n" +
                                "      http://xmlns.jcp.org/xml/ns/javaee\n" +
                                "      http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd\">\n" +
                                "</web-app>\n"), "web.xml")
                ;
        final EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, APP_NAME + ".ear")
                .addAsModules(ejbJarOne, war)
                .addAsLibrary(libJar);
        return ear;
    }

    private void checkAccessLogConsole(Class ejbInterface, Class ejbClass, String ejbMethod, String user) {
        String[] lines = serverStdout.getNewLines();
        Assert.assertNotNull("No access log messages generated in server console!", lines);

        //TODO: remove this code
        appendToFile("/tmp/ConsoleAccessLogTestCase.txt", lines);

        // get access logs
        List<AccessLog> accessLogs = getAccessLogs(lines, ACCESS_LOG_FORMAT, ejbInterface.getSimpleName(), ejbClass.getSimpleName(), ejbMethod, user);

        //Assert.assertTrue("New ejb access log not found!", accessLogs != null && accessLogs.size() == 1);
    }

    private void checkAccessLogServerLog(Class ejbInterface, Class ejbClass, String ejbMethod, String user) throws IOException, InterruptedException {
        // read the tmp file holding location and offset of the server log file
        Map.Entry<Path, Long> entry = getTmpFileContent();
        // access the server log file
        ServerLog serverLog = new ServerLog(entry.getKey(), entry.getValue());
        // and read the chunk added since the last read
        String[] lines = serverLog.getNewLines();

        //TODO: remove this code
        appendToFile("/tmp/ServelLogAccessLogTestCase.txt", lines);

        Assert.assertNotNull("No access log messages generated in server log file!", lines);

        // get access logs in the expected number and format
        List<AccessLog> accessLogs = getAccessLogs(lines, ACCESS_LOG_FORMAT, ejbInterface.getSimpleName(), ejbClass.getSimpleName(), ejbMethod, user);

        //Assert.assertTrue("New ejb access log not found!", accessLogs != null && accessLogs.size() == 1);

        // mark where the log file was last accessed
        writeTmpFile(entry.getKey(), serverLog.getOffset());
    }

    @Override
    protected void checkAccessLog(Class ejbInterface, Class ejbClass, String ejbMethod, String user) throws IOException, InterruptedException {
        checkAccessLogConsole(ejbInterface, ejbClass, ejbMethod, user);
        checkAccessLogServerLog(ejbInterface, ejbClass, ejbMethod, user);
    }

        /* ==============================================
                        Server config
       ============================================== */

    static class EjbAccessLogSetupTask implements ServerSetupTask {

        ModelNode address, operation, result;

        @Override
        public void setup(ManagementClient managementClient, String s) throws Exception {
            System.out.println("\n\nsetup\n\n");

            // /subsystem=ejb3/service=access-log:add
            address = new ModelNode();
            address.add("subsystem", "ejb3");
            address.add("service", "access-log");

            operation = new ModelNode();
            operation.get(OP).set(ADD);
            operation.get(OP_ADDR).set(address);
            result = managementClient.getControllerClient().execute(operation);
            if (!Operations.isSuccessfulOutcome(result)) {
                throw new Exception("Can't configure server: " + result.asString());
            }

            // /subsystem=ejb3/service=access-log/pattern-formatter=p1:add(name=p1, pattern=\"short\")
            address = new ModelNode();
            address.add("subsystem", "ejb3");
            address.add("service", "access-log");
            address.add("pattern-formatter", "p1");

            operation = new ModelNode();
            operation.get(OP).set(ADD);
            operation.get(OP_ADDR).set(address);
            operation.get("name").set("p1");
            // short: date time ip user ejb method
            // e.g. 2019-05-05 12:23:27,003 127.0.0.1 admin hello/helloBean hello
            operation.get("pattern").set(ACCESS_LOG_FORMAT.getPattern());
            result = managementClient.getControllerClient().execute(operation);
            if (!Operations.isSuccessfulOutcome(result)) {
                throw new Exception("Can't configure server: " + result.asString());
            }

            // /subsystem=ejb3/service=access-log/console-handler=console1:add(name=console1,formatter=p1)
            address = new ModelNode();
            address.add("subsystem", "ejb3");
            address.add("service", "access-log");
            address.add("console-handler", "console1");

            operation = new ModelNode();
            operation.get(OP).set(ADD);
            operation.get(OP_ADDR).set(address);
            operation.get("name").set("console1");
            operation.get("formatter").set("p1");
            result = managementClient.getControllerClient().execute(operation);
            if (!Operations.isSuccessfulOutcome(result)) {
                throw new Exception("Can't configure server: " + result.asString());
            }

            // /subsystem=ejb3/service=access-log/server-log-handler=server1:add(name=server1,formatter=p1)
            address = new ModelNode();
            address.add("subsystem", "ejb3");
            address.add("service", "access-log");
            address.add("server-log-handler", "server1");

            operation = new ModelNode();
            operation.get(OP).set(ADD);
            operation.get(OP_ADDR).set(address);
            operation.get("name").set("server1");
            operation.get("formatter").set("p1");
            result = managementClient.getControllerClient().execute(operation);
            if (!Operations.isSuccessfulOutcome(result)) {
                throw new Exception("Can't configure server: " + result.asString());
            }

            ServerReload.executeReloadAndWaitForCompletion(managementClient.getControllerClient(), 50000);
        }

        @Override
        public void tearDown(ManagementClient managementClient, String s) throws Exception {
            System.out.println("\n\ntearDown\n\n");

            // /subsystem=ejb3/service=access-log/console-handler=console1:remove
            address = new ModelNode();
            address.add("subsystem", "ejb3");
            address.add("service", "access-log");
            address.add("console-handler", "console1");

            operation = new ModelNode();
            operation.get(OP).set(REMOVE);
            operation.get(OP_ADDR).set(address);
            result = managementClient.getControllerClient().execute(operation);
            if (!Operations.isSuccessfulOutcome(result)) {
                throw new Exception("Can't configure server: " + result.asString());
            }

            // /subsystem=ejb3/service=access-log/server-log-handler=server1:remove
            address = new ModelNode();
            address.add("subsystem", "ejb3");
            address.add("service", "access-log");
            address.add("server-log-handler", "server1");

            operation = new ModelNode();
            operation.get(OP).set(REMOVE);
            operation.get(OP_ADDR).set(address);
            result = managementClient.getControllerClient().execute(operation);
            if (!Operations.isSuccessfulOutcome(result)) {
                throw new Exception("Can't configure server: " + result.asString());
            }

            // /subsystem=ejb3/service=access-log/pattern-formatter=p1:remove
            address = new ModelNode();
            address.add("subsystem", "ejb3");
            address.add("service", "access-log");
            address.add("pattern-formatter", "p1");

            operation = new ModelNode();
            operation.get(OP).set(REMOVE);
            operation.get(OP_ADDR).set(address);
            result = managementClient.getControllerClient().execute(operation);
            if (!Operations.isSuccessfulOutcome(result)) {
                throw new Exception("Can't configure server: " + result.asString());
            }

            // /subsystem=ejb3/service=access-log:remove
            address = new ModelNode();
            address.add("subsystem", "ejb3");
            address.add("service", "access-log");

            operation = new ModelNode();
            operation.get(OP).set(REMOVE);
            operation.get(OP_ADDR).set(address);
            result = managementClient.getControllerClient().execute(operation);
            if (!Operations.isSuccessfulOutcome(result)) {
                throw new Exception("Can't configure server: " + result.asString());
            }

            ServerReload.executeReloadAndWaitForCompletion(managementClient.getControllerClient(), 50000);
        }
    }
}
