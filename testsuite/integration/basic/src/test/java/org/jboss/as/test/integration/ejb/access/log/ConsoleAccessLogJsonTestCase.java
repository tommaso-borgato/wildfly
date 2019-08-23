package org.jboss.as.test.integration.ejb.access.log;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.test.shared.CLIServerSetupTask;
import org.jboss.as.test.shared.integration.ejb.security.Util;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.runner.RunWith;

/**
 * Counterpart of {@link org.jboss.as.test.integration.web.access.log.ConsoleAccessLogTestCase} for EJB access logs
 */
@RunWith(Arquillian.class)
@ServerSetup(ConsoleAccessLogJsonTestCase.EjbAccessLogSetupTask.class)
public class ConsoleAccessLogJsonTestCase extends AbstractConsoleAccessLogTestCase {

    @Deployment
    public static Archive createDeployment() {
        final JavaArchive ejbJarOne = ShrinkWrap.create(JavaArchive.class, MODULE_NAME + ".jar")
                .addClasses(SLSBRemote.class, SLSBLocal.class, SLSB.class)
                .addClasses(SFSBRemote.class, SFSBLocal.class, SFSB.class);
        final JavaArchive libJar = ShrinkWrap.create(JavaArchive.class, "bean-interfaces.jar")
                .addClasses(EjbAccessLogSetupTask.class, CLIServerSetupTask.class, ServerSetupTask.class)
                .addClasses(Util.class, AbstractAccessLogTestCase.class, AbstractConsoleAccessLogTestCase.class, ConsoleAccessLogJsonTestCase.class);
        final EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, APP_NAME + ".ear")
                .addAsModules(ejbJarOne)
                .addAsLibrary(libJar);
        return ear;
    }

    @Override
    protected void checkAccessLog() {
        String[] lines = stdout.getNewLines();
        Assert.assertNotNull("No access log messages generated in server console!", lines);

        StringBuilder chunck = new StringBuilder();
        for (String line: lines) {
            chunck.append(line);
            chunck.append("\n");
        }
        System.out.println("\n\n<SERVER_LOG_CHUNK>\n" + chunck.toString() + "\n</SERVER_LOG_CHUNK>\n\n");
        //TODO: add server console specific code
    }

    public static class EjbAccessLogSetupTask extends CLIServerSetupTask {
        public EjbAccessLogSetupTask() {
            this.builder
                    .node(DEFAULT_CONNECTION_SERVER)
                    .setup("/subsystem=ejb3/service=access-log:add")
                    .setup("/subsystem=ejb3/service=access-log/json-formatter=j1:add(name=j1,pattern=\"date time\")")
                    .setup("/subsystem=ejb3/service=access-log/console-handler=console1:add(name=console1, formatter=j1)")
                    .teardown("/subsystem=ejb3/service=access-log/console-handler=console1:remove")
                    .teardown("/subsystem=ejb3/service=access-log/json-formatter=j1:remove")
                    .teardown("/subsystem=ejb3/service=access-log:remove");
        }
    }
}
