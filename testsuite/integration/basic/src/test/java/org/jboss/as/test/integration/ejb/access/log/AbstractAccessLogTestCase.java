package org.jboss.as.test.integration.ejb.access.log;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.test.integration.security.common.Utils;
import org.jboss.as.test.shared.integration.ejb.security.Util;
import org.jboss.logging.Logger;
import org.junit.Assert;
import org.junit.Test;

import javax.naming.InitialContext;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

public abstract class AbstractAccessLogTestCase {
    protected static final Logger logger = Logger.getLogger(AbstractAccessLogTestCase.class);

    protected static final String APP_NAME = "ejb-access-logs-test-app";

    protected static final String MODULE_NAME = "ejb-access-logs-test-app-ejb-jar";


    protected static final String DEFAULT_CONNECTION_SERVER = "jboss";

    @ArquillianResource
    protected ManagementClient mgmtClient;

    protected abstract void checkAccessLog() throws IOException;

    /* ==============================================
                    LOG DIGEST FILE
       ============================================== */

    protected Path getTmpFilePath() {
        return Paths.get(System.getProperty("java.io.tmpdir"), String.format("%s.tmp", this.getClass().getSimpleName()));
    }

    protected void writeTmpFile(Path path, long offset) throws IOException {
        Path tmp = getTmpFilePath();
        BufferedWriter writer = new BufferedWriter(new FileWriter(tmp.toFile(), false));
        writer.write(String.format("%s:%d", path.toFile().getAbsolutePath(), offset));
        writer.close();

        System.out.println("\n\twrote to " + getTmpFilePath() + " " + path + ":" + offset + "\n");
    }

    protected Map.Entry<Path, Long> getTmpFileContent() throws IOException {
        List<String> lines = Files.readAllLines(getTmpFilePath());
        Path path = Paths.get(lines.get(0).split(":")[0]);
        Long offset = Long.parseLong(lines.get(0).split(":")[1]);

        System.out.println("\n\tread from " + getTmpFilePath() + " " + path + ":" + offset + "\n");

        return new AbstractMap.SimpleEntry<>(path, offset);
    }

    /* ==============================================
                    LOCAL EJB CLIENT
       ============================================== */

    /**
     * Access unsecured Stateless EJB method
     */
    @Test
    @InSequence(1)
    public void testSLSB() throws Exception {
        Callable<Void> callable = () -> {
            final SLSBRemote allowAccessBean = InitialContext.doLookup("java:global/" + APP_NAME + "/" + MODULE_NAME + "/" + SLSB.class.getSimpleName() + "!" + SLSBRemote.class.getName());
            final String echo = allowAccessBean.echo("TUTTO A POSTO A FERRAGOSTO");
            Assert.assertTrue("Could not access unsecured SLSB", echo != null && echo.contains("TUTTO A POSTO A FERRAGOSTO"));
            return null;
        };
        // establish an identity using the security domain associated with the beans in the JARs in the EAR deployment
        Util.switchIdentity(null, null, callable, SLSB.class.getClassLoader());
        checkAccessLog();
    }

    /**
     * Access unsecured Stateful EJB method
     */
    @Test
    @InSequence(2)
    public void testSFSB() throws Exception {
        Callable<Void> callable = () -> {
            final SFSBRemote allowAccessBean = InitialContext.doLookup("java:global/" + APP_NAME + "/" + MODULE_NAME + "/" + SFSB.class.getSimpleName() + "!" + SFSBRemote.class.getName());
            final String echo = allowAccessBean.echo("TUTTO A POSTO A FERRAGOSTO");
            Assert.assertTrue("Could not access unsecured SFSB", echo != null && echo.contains("TUTTO A POSTO A FERRAGOSTO"));
            return null;
        };
        // establish an identity using the security domain associated with the beans in the JARs in the EAR deployment
        Util.switchIdentity(null, null, callable, SFSB.class.getClassLoader());
        checkAccessLog();
    }

    /**
     * Access secured Stateless EJB method
     */
    @Test
    @InSequence(3)
    public void testSLSBSecured() throws Exception {
        Callable<Void> callable = () -> {
            final SLSBRemote allowAccessBean = InitialContext.doLookup("java:global/" + APP_NAME + "/" + MODULE_NAME + "/" + SLSB.class.getSimpleName() + "!" + SLSBRemote.class.getName());
            final String echo = allowAccessBean.echoSecuredRole1("TUTTO A POSTO A FERRAGOSTO");
            Assert.assertTrue("Could not access unsecured SLSB", echo != null && echo.contains("TUTTO A POSTO A FERRAGOSTO"));
            return null;
        };
        Util.switchIdentity("user1", "password1", callable, SLSB.class.getClassLoader());
        checkAccessLog();
    }

    /**
     * Access secured Stateful EJB method
     */
    @Test
    @InSequence(4)
    public void testSFSBSecured() throws Exception {
        Callable<Void> callable = () -> {
            final SFSBRemote allowAccessBean = InitialContext.doLookup("java:global/" + APP_NAME + "/" + MODULE_NAME + "/" + SFSB.class.getSimpleName() + "!" + SFSBRemote.class.getName());
            final String echo = allowAccessBean.echoSecuredRole2("TUTTO A POSTO A FERRAGOSTO");
            Assert.assertTrue("Could not access role secured SFSB", echo != null && echo.contains("TUTTO A POSTO A FERRAGOSTO"));
            return null;
        };
        Util.switchIdentity("user2", "password2", callable, SFSB.class.getClassLoader());
        checkAccessLog();
    }

    /* ==============================================
                    REMOTE EJB CLIENT
       ============================================== */

    /**
     * Remote access unsecured Stateless EJB method
     */
    @Test
    @InSequence(5)
    @RunAsClient
    public void testRemoteSLSB() throws Exception {
        final Properties ejbClientConfiguration = EJBUtil.createEjbClientConfiguration(Utils.getHost(mgmtClient), null, null);
        final SLSBRemote targetBean = EJBUtil.lookupEJB(SLSB.class, SLSBRemote.class, ejbClientConfiguration, APP_NAME, MODULE_NAME, false);
        String echo = targetBean.echo("TUTTO A POSTO A FERRAGOSTO");
        Assert.assertTrue("Could not access role secured SLSB", echo != null && echo.contains("TUTTO A POSTO A FERRAGOSTO"));
        checkAccessLog();
    }

    /**
     * Remote access secured Stateless EJB method
     */
    @Test
    @InSequence(6)
    @RunAsClient
    public void testRemoteSLSBSecured() throws Exception {
        final Properties ejbClientConfiguration = EJBUtil.createEjbClientConfiguration(Utils.getHost(mgmtClient), "user1", "password1");
        final SLSBRemote targetBean = EJBUtil.lookupEJB(SLSB.class, SLSBRemote.class, ejbClientConfiguration, APP_NAME, MODULE_NAME, false);
        String echo = targetBean.echoSecuredRole1("TUTTO A POSTO A FERRAGOSTO");
        Assert.assertTrue("Could not access role secured SLSB", echo != null && echo.contains("TUTTO A POSTO A FERRAGOSTO"));
        checkAccessLog();
    }

    /**
     * Remote access unsecured Stateful EJB method
     */
    @Test
    @InSequence(7)
    @RunAsClient
    public void testRemoteSFSB() throws Exception {
        final Properties ejbClientConfiguration = EJBUtil.createEjbClientConfiguration(Utils.getHost(mgmtClient), null, null);
        final SFSBRemote targetBean = EJBUtil.lookupEJB(SFSB.class, SFSBRemote.class, ejbClientConfiguration, APP_NAME, MODULE_NAME, true);
        String echo = targetBean.echo("TUTTO A POSTO A FERRAGOSTO");
        Assert.assertTrue("Could not access role secured SFSB", echo != null && echo.contains("TUTTO A POSTO A FERRAGOSTO"));
        checkAccessLog();
    }

    /**
     * Remote access secured Stateful EJB method
     */
    @Test
    @InSequence(8)
    @RunAsClient
    public void testRemoteSFSBSecured() throws Exception {
        final Properties ejbClientConfiguration = EJBUtil.createEjbClientConfiguration(Utils.getHost(mgmtClient), "user1", "password1");
        final SFSBRemote targetBean = EJBUtil.lookupEJB(SFSB.class, SFSBRemote.class, ejbClientConfiguration, APP_NAME, MODULE_NAME, true);
        String echo = targetBean.echoSecuredRole1("TUTTO A POSTO A FERRAGOSTO");
        Assert.assertTrue("Could not access role secured SFSB", echo != null && echo.contains("TUTTO A POSTO A FERRAGOSTO"));
        checkAccessLog();
    }
}
