package org.jboss.as.test.integration.ejb.access.log;

import org.jboss.logging.Logger;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Stateless;

@Stateless
@Remote(SLSBRemote.class)
@Local(SLSBLocal.class)
@LocalBean
public class SLSB implements SLSBLocal {
    protected static final Logger logger = Logger.getLogger(SLSB.class);

    @Override
    @PermitAll
    public String echo(String msg) {
        logger.info("SLSB");
        return String.format("ECHO[%s]", msg);
    }

    @Override
    @RolesAllowed("Role1")
    public String echoSecuredRole1(String msg) {
        logger.info("SLSB Role1");
        return String.format("ECHO_SECURED_1[%s]", msg);
    }

    @Override
    @RolesAllowed("Role2")
    public String echoSecuredRole2(String msg) {
        logger.info("SLSB Role2");
        return String.format("ECHO_SECURED_2[%s]", msg);
    }
}
