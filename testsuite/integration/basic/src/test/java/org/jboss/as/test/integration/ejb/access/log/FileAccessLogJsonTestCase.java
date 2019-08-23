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

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

@RunWith(Arquillian.class)
@ServerSetup(FileAccessLogJsonTestCase.EjbAccessLogSetupTask.class)
public class FileAccessLogJsonTestCase extends AbstractFileAccessLogTestCase {

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
                .addClasses(EjbAccessLogSetupTask.class, CLIServerSetupTask.class, ServerSetupTask.class)
                .addClasses(Util.class, ServerLog.class, AbstractAccessLogTestCase.class, AbstractFileAccessLogTestCase.class, FileAccessLogJsonTestCase.class);
        final EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, APP_NAME + ".ear")
                .addAsModules(ejbJarOne)
                .addAsLibrary(libJar);
        return ear;
    }

    @Override
    protected void checkAccessLog() throws IOException {
        Map.Entry<Path, Long> entry = getTmpFileContent();
        ServerLog serverLog = new ServerLog(entry.getKey(), entry.getValue());
        String chunck = serverLog.readNext();

        Assert.assertTrue(chunck != null && chunck.length()>0);

        //TODO: add server log specific code

        // mark where the log file was last accessed
        writeTmpFile(entry.getKey(), serverLog.getOffset());
    }

    /* ==============================================
                        Server config
       ============================================== */

    public static class EjbAccessLogSetupTask extends CLIServerSetupTask {
        public EjbAccessLogSetupTask() {
            //TODO: configure to log on server lo file
            this.builder
                    .node(DEFAULT_CONNECTION_SERVER)
                    .setup("/subsystem=ejb3/service=access-log:add")
                    .setup("/subsystem=ejb3/service=access-log/json-formatter=j1:add(name=j1,pattern=\"date time\")")
                    .setup("/subsystem=ejb3/service=access-log/file-handler=file1:add(name=file1,path=" + EJB_ACCESS_LOG_FILE + ",relative-to=jboss.server.log.dir,formatter=j1)")
                    .teardown("/subsystem=ejb3/service=access-log/file-handler=file1:remove")
                    .teardown("/subsystem=ejb3/service=access-log/json-formatter=j1:remove")
                    .teardown("/subsystem=ejb3/service=access-log:remove");
        }
    }
}
