
package org.glassfish.connectors.admin.cli;

import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.v3.common.PropsFileActionReporter;
import com.sun.logging.LogDomains;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.server.ServerEnvironmentImpl;
import org.glassfish.tests.utils.ConfigApiTest;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.DomDocument;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import java.beans.PropertyVetoException;
import java.util.Properties;


public class CreateJavaMailResourceTest extends ConfigApiTest {

    private Habitat habitat;
    private Resources resources;
    private Properties parameters;
    private AdminCommandContext context;
    private CommandRunner cr;

    public DomDocument getDocument(Habitat habitat) {
        return new TestDocument(habitat);
    }

    public String getFileName() {
        return "DomainTest";
    }

    @Before
    public void setUp() {
        habitat = getHabitat();
        resources = habitat.getComponent(Resources.class);
        assertTrue(resources != null);
        parameters = new Properties();
        context = new AdminCommandContext(
                LogDomains.getLogger(ServerEnvironmentImpl.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());
        cr = habitat.getComponent(CommandRunner.class);
        assertTrue(cr != null);
    }

    @After
    public void tearDown() throws TransactionFailure {
        ConfigSupport.apply(new SingleConfigCode<Resources>() {
            public Object run(Resources param) throws PropertyVetoException, TransactionFailure {
                Resource target = null;
                for (Resource resource : param.getResources()) {
                    if (resource instanceof MailResource) {
                        MailResource r = (MailResource) resource;
                        if (r.getJndiName().equals("mail/MyMailSession") ||
                                r.getJndiName().equals("dupRes")) {
                            target = resource;
                            break;
                        }
                    }
                }
                if (target != null) {
                    param.getResources().remove(target);
                }
                return null;
            }
        }, resources);
    }

    /**
     * Test of execute method, of class CreateJavaMailResource.
     * asadmin create-javamail-resource --mailuser=test --mailhost=localhost
     * --fromaddress=test@sun.com mail/MyMailSession
     */
    @Test
    public void testExecuteSuccess() {
        parameters.setProperty("mailhost", "localhost");
        parameters.setProperty("mailuser", "test");
        parameters.setProperty("fromaddress", "test@sun.com");
        parameters.setProperty("jndi_name", "mail/MyMailSession");
        CreateJavaMailResource command = habitat.getComponent(CreateJavaMailResource.class);
        assertTrue(command != null);
        cr.doCommand("create-javamail-resource", command, parameters, context.getActionReport());
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
        boolean isCreated = false;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof MailResource) {
                MailResource r = (MailResource) resource;
                if (r.getJndiName().equals("mail/MyMailSession")) {
                    assertEquals("localhost", r.getHost());
                    assertEquals("test", r.getUser());
                    assertEquals("test@sun.com", r.getFrom());
                    assertEquals("true", r.getEnabled());
                    assertEquals("false", r.getDebug());
                    assertEquals("imap", r.getStoreProtocol());
                    assertEquals("com.sun.mail.imap.IMAPStore", r.getStoreProtocolClass());
                    assertEquals("smtp", r.getTransportProtocol());
                    assertEquals("com.sun.mail.smtp.SMTPTransport", r.getTransportProtocolClass());
                    isCreated = true;
                    logger.fine("MailResource config bean mail/MyMailSession is created.");
                    break;
                }
            }
        }
        assertTrue(isCreated);
        logger.fine("msg: " + context.getActionReport().getMessage());
        Servers servers = habitat.getComponent(Servers.class);
        boolean isRefCreated = false;
        for (Server server : servers.getServer()) {
            if (server.getName().equals(SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME)) {
                for (ResourceRef ref : server.getResourceRef()) {
                    if (ref.getRef().equals("mail/MyMailSession")) {
                        assertEquals("true", ref.getEnabled());
                        isRefCreated = true;
                        break;
                    }
                }
            }
        }
        assertTrue(isRefCreated);
    }

    /**
     * Test of execute method, of class CreateJavaMailResource.
     * asadmin create-javamail-resource --mailuser=test --mailhost=localhost
     * --fromaddress=test@sun.com dupRes
     * asadmin create-javamail-resource --mailuser=test --mailhost=localhost
     * --fromaddress=test@sun.com dupRes
     */
    @Test
    public void testExecuteFailDuplicateResource() {
        parameters.setProperty("mailhost", "localhost");
        parameters.setProperty("mailuser", "test");
        parameters.setProperty("fromaddress", "test@sun.com");
        parameters.setProperty("jndi_name", "dupRes");
        CreateJavaMailResource command1 = habitat.getComponent(CreateJavaMailResource.class);
        assertTrue(command1 != null);
        cr.doCommand("create-javamail-resource", command1, parameters, context.getActionReport());
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
        boolean isCreated = false;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof MailResource) {
                MailResource jr = (MailResource) resource;
                if (jr.getJndiName().equals("dupRes")) {
                    isCreated = true;
                    logger.fine("MailResource config bean dupRes is created.");
                    break;
                }
            }
        }
        assertTrue(isCreated);

        CreateJavaMailResource command2 = habitat.getComponent(CreateJavaMailResource.class);
        cr.doCommand("create-javamail-resource", command2, parameters, context.getActionReport());
        assertEquals(ActionReport.ExitCode.FAILURE, context.getActionReport().getActionExitCode());
        int numDupRes = 0;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof MailResource) {
                MailResource jr = (MailResource) resource;
                if (jr.getJndiName().equals("dupRes")) {
                    numDupRes = numDupRes + 1;
                }
            }
        }
        assertEquals(1, numDupRes);
        logger.fine("msg: " + context.getActionReport().getMessage());
    }

    /**
     * Test of execute method, of class CreateJavaMailResource when enabled has no value
     * asadmin create-javamail-resource --mailuser=test --mailhost=localhost
     * --fromaddress=test@sun.com  --enabled=false --debug=true
     * --storeprotocol=pop
     * --storeprotocolclass=com.sun.mail.pop.POPStore
     * --transprotocol=lmtp
     * --transprotocolclass=com.sun.mail.lmtop.LMTPTransport
     * mail/MyMailSession
     */
    @Test
    public void testExecuteWithOptionalValuesSet() {
        parameters.setProperty("mailhost", "localhost");
        parameters.setProperty("mailuser", "test");
        parameters.setProperty("fromaddress", "test@sun.com");
        parameters.setProperty("enabled", "false");
        parameters.setProperty("debug", "true");
        parameters.setProperty("storeprotocol", "pop");
        parameters.setProperty("storeprotocolclass", "com.sun.mail.pop.POPStore");
        parameters.setProperty("transprotocol", "lmtp");
        parameters.setProperty("transprotocolclass", "com.sun.mail.lmtp.LMTPTransport");
        parameters.setProperty("jndi_name", "mail/MyMailSession");
        CreateJavaMailResource command = habitat.getComponent(CreateJavaMailResource.class);
        assertTrue(command != null);
        cr.doCommand("create-javamail-resource", command, parameters, context.getActionReport());
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
        boolean isCreated = false;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof MailResource) {
                MailResource r = (MailResource) resource;
                if (r.getJndiName().equals("mail/MyMailSession")) {
                    assertEquals("false", r.getEnabled());
                    assertEquals("true", r.getDebug());
                    assertEquals("pop", r.getStoreProtocol());
                    assertEquals("com.sun.mail.pop.POPStore", r.getStoreProtocolClass());
                    assertEquals("lmtp", r.getTransportProtocol());
                    assertEquals("com.sun.mail.lmtp.LMTPTransport", r.getTransportProtocolClass());
                    isCreated = true;
                    break;
                }
            }
        }
        assertTrue(isCreated);
        logger.fine("msg: " + context.getActionReport().getMessage());
    }
}