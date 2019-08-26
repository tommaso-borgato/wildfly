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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;

public abstract class AbstractAccessLogTestCase {
    protected static final Logger logger = Logger.getLogger(AbstractAccessLogTestCase.class);

    protected static final String APP_NAME = "ejb-access-logs-test-app";

    protected static final String MODULE_NAME = "ejb-access-logs-test-app-ejb-jar";

    private static final int DFT_TIMEOUT = 60;

    @ArquillianResource
    protected ManagementClient mgmtClient;

    protected abstract void checkAccessLog(Class ejbInterface, Class ejbClass, String ejbMethod, String user) throws IOException, InterruptedException;

    @Deprecated
    protected void appendToFile(String filename, String[] lines) {
        StringBuilder chunck = new StringBuilder();
        for (String line : lines) {
            chunck.append(line);
            chunck.append("\n");
        }
        appendToFile(filename, chunck.toString());
    }

    @Deprecated
    protected void appendToFile(String filename, String chunck) {
        //TODO: remove this code
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(
                    new FileWriter(filename, true)  //Set true for append mode
            );
            writer.newLine();   //Add new line
            writer.write("\n\n<SERVER_LOG_CHUNK>\n" + chunck + "\n</SERVER_LOG_CHUNK>\n\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            if (writer != null) try {
                writer.close();
            } catch (Exception ignore) {
            }
        }
    }

    private boolean containsAllStrings(String line, String... specificStrings) {
        if (line == null) return false;
        for (String str : specificStrings) {
            if (str != null && !line.contains(str)) return false;
        }
        return true;
    }

    /* ==============================================
                    LOG EXTRACTORS
       ============================================== */
    protected List<AccessLog> getAccessLogs(String chunck, AccessLogFormat accessLogFormat, String... specificStrings) {
        List<AccessLog> retval = null;
        if (chunck != null) {
            String[] lines = chunck.split("\\r?\\n");
            retval = getAccessLogs(lines, accessLogFormat, specificStrings);
        }
        return retval;
    }

    protected List<AccessLog> getAccessLogs(String[] lines, AccessLogFormat accessLogFormat, String... specificStrings) {
        List<AccessLog> retval = null;
        if (lines != null && lines.length > 0) {
            retval = new ArrayList<>();
            for (String line : lines) {
                Matcher matcher = accessLogFormat.getRegexp().matcher(line);
                if (
                        containsAllStrings(line, specificStrings)
                                &&
                                matcher.matches()
                ) {
                    AccessLog accessLog = new AccessLog(line);
                    retval.add(accessLog);
                }
            }
        }
        return retval;
    }

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
        checkAccessLog(SLSBRemote.class, SLSB.class, "echo", null);
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
        checkAccessLog(SFSBRemote.class, SFSB.class, "echo", null);
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
        checkAccessLog(SLSBRemote.class, SLSB.class, "echoSecuredRole1", "user1");
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
        checkAccessLog(SFSBRemote.class, SFSB.class, "echoSecuredRole2", "user2");
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
        checkAccessLog(SLSBRemote.class, SLSB.class, "echo", null);
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
        checkAccessLog(SLSBRemote.class, SLSB.class, "echoSecuredRole1", "user1");
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
        checkAccessLog(SFSBRemote.class, SFSB.class, "echo", null);
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
        checkAccessLog(SFSBRemote.class, SFSB.class, "echoSecuredRole1", "user1");
    }
}
