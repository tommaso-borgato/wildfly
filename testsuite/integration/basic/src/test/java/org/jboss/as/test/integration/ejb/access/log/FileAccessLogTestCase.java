package org.jboss.as.test.integration.ejb.access.log;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.as.test.shared.CLIServerSetupTask;
import org.jboss.as.test.shared.ServerReload;
import org.jboss.as.test.shared.TimeoutUtil;
import org.jboss.as.test.shared.integration.ejb.security.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REMOVE;

/**
 * Test EJB access logs;
 * <p>
 * Various kinds of EJBs are deployed on the server and then accessed both locally and remotely;
 * After each access the logs are verified;
 *
 * This test case focuses on ejb access logs written to a custom log file "standalone/log/ejb-access.log"
 *
 * @author tborgato <a href="mailto:tborgato@redhat.com">Tommaso Borgato</a>
 */
@RunWith(Arquillian.class)
@ServerSetup(FileAccessLogTestCase.EjbAccessLogSetupTask.class)
public class FileAccessLogTestCase extends AbstractFileAccessLogTestCase {
    private static final AccessLogFormat ACCESS_LOG_FORMAT = AccessLogFormat.CUSTOM;

    // mvn -Dtest=ServelLogAccessLogTestCase clean test

    /**
     * Deploy one Stateful EJB and one Stateless EJB both with a local and remote interface;
     * Both EJBs have some methods secured;
     *
     * @return
     */
    @Deployment
    public static Archive createDeployment() {
        final JavaArchive ejbJarOne = ShrinkWrap.create(JavaArchive.class, MODULE_NAME + ".jar")
                .addClasses(SLSBRemote.class, SLSBLocal.class, SLSB.class)
                .addClasses(SFSBRemote.class, SFSBLocal.class, SFSB.class);
        final JavaArchive libJar = ShrinkWrap.create(JavaArchive.class, "bean-interfaces.jar")
                .addClasses(AccessLog.class, AccessLogFormat.class, ServerStdout.class, ServerLog.class, TimeoutUtil.class)
                .addClasses(EjbAccessLogSetupTask.class, CLIServerSetupTask.class, ServerSetupTask.class)
                .addClasses(Util.class, ServerLog.class, AbstractAccessLogTestCase.class, AbstractFileAccessLogTestCase.class, FileAccessLogTestCase.class);
        final EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, APP_NAME + ".ear")
                .addAsModules(ejbJarOne)
                .addAsLibrary(libJar);
        return ear;
    }

    @Override
    protected void checkAccessLog(Class ejbInterface, Class ejbClass, String ejbMethod, String user) throws IOException, InterruptedException {
        // read the tmp file holding location and offset of the server log file
        Map.Entry<Path, Long> entry = getTmpFileContent();
        // access the server log file
        ServerLog serverLog = new ServerLog(entry.getKey(), entry.getValue());
        // and read the chunk added since the last read
        String[] lines = serverLog.getNewLines();

        //TODO: remove this code
        appendToFile("/tmp/FileAccessLogTestCase.txt", lines);

        Assert.assertNotNull("No access log messages generated in custom log file!", lines);

        // get access logs
        List<AccessLog> accessLogs = getAccessLogs(lines, ACCESS_LOG_FORMAT, ejbInterface.getSimpleName(), ejbClass.getSimpleName(), ejbMethod, user);

        Assert.assertTrue("New ejb access log not found!", accessLogs != null && accessLogs.size() == 1);

        // mark where the log file was last accessed
        writeTmpFile(entry.getKey(), serverLog.getOffset());
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

            // /subsystem=ejb3/service=access-log/pattern-formatter=p1:add(name=p1, pattern=\"date time\")
            address = new ModelNode();
            address.add("subsystem", "ejb3");
            address.add("service", "access-log");
            address.add("pattern-formatter", "p1");

            operation = new ModelNode();
            operation.get(OP).set(ADD);
            operation.get(OP_ADDR).set(address);
            operation.get("name").set("p1");
            operation.get("pattern").set(ACCESS_LOG_FORMAT.getPattern());
            result = managementClient.getControllerClient().execute(operation);
            if (!Operations.isSuccessfulOutcome(result)) {
                throw new Exception("Can't configure server: " + result.asString());
            }

            // /subsystem=ejb3/service=access-log/file-handler=file1:add(name=file1,path=" + EJB_ACCESS_LOG_FILE + ",relative-to=jboss.server.log.dir,formatter=j1)
            address = new ModelNode();
            address.add("subsystem", "ejb3");
            address.add("service", "access-log");
            address.add("file-handler", "file1");

            operation = new ModelNode();
            operation.get(OP).set(ADD);
            operation.get(OP_ADDR).set(address);
            operation.get("name").set("file1");
            operation.get("formatter").set("p1");
            operation.get("path").set(EJB_ACCESS_LOG_FILE);
            operation.get("relative-to").set("jboss.server.log.dir");
            result = managementClient.getControllerClient().execute(operation);
            if (!Operations.isSuccessfulOutcome(result)) {
                throw new Exception("Can't configure server: " + result.asString());
            }

            ServerReload.executeReloadAndWaitForCompletion(managementClient.getControllerClient(), 50000);
        }

        @Override
        public void tearDown(ManagementClient managementClient, String s) throws Exception {
            System.out.println("\n\ntearDown\n\n");

            // /subsystem=ejb3/service=access-log/file-handler=file1:remove
            address = new ModelNode();
            address.add("subsystem", "ejb3");
            address.add("service", "access-log");
            address.add("file-handler", "file1");

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
