
package org.glassfish.connectors.admin.cli;

import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.v3.common.PropsFileActionReporter;
import com.sun.logging.LogDomains;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandRunner;
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


public class DeleteJavaMailResourceTest extends ConfigApiTest {
    private Habitat habitat;
    private Resources resources;
    private Properties parameters;
    private AdminCommandContext context;
    private CommandRunner cr;

    @Before
    public void setUp() {
        habitat = getHabitat();
        parameters = new Properties();
        cr = habitat.getComponent(CommandRunner.class);
        resources = habitat.getComponent(Resources.class);
        context = new AdminCommandContext(
                LogDomains.getLogger(ListJavaMailResourcesTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());
    }

    @After
    public void tearDown() throws TransactionFailure {
        ConfigSupport.apply(new SingleConfigCode<Resources>() {
            public Object run(Resources param) throws PropertyVetoException, TransactionFailure {
                Resource target = null;
                for (Resource resource : param.getResources()) {
                    if (resource instanceof MailResource) {
                        MailResource r = (MailResource) resource;
                        if (r.getJndiName().equals("mail/MyMailSession")) {
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
        parameters.clear();
    }

    public DomDocument getDocument(Habitat habitat) {
        return new TestDocument(habitat);
    }

    public String getFileName() {
        return "DomainTest";
    }

    /**
     * Test of execute method, of class DeleteJavaMailResource.
     * asadmin create-javamail-resource --mailuser=test --mailhost=localhost
     * --fromaddress=test@sun.com mail/MyMailSession
     * delete-javamail-resource mail/MyMailSession
     */
    @Test
    public void testExecuteSuccessDefaultTarget() {
        parameters.setProperty("mailhost", "localhost");
        parameters.setProperty("mailuser", "test");
        parameters.setProperty("fromaddress", "test@sun.com");
        parameters.setProperty("jndi_name", "mail/MyMailSession");
        CreateJavaMailResource createCommand = habitat.getComponent(CreateJavaMailResource.class);
        assertTrue(createCommand != null);
        cr.getCommandInvocation("create-javamail-resource", context.getActionReport()).parameters(parameters).execute(createCommand);
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());

        parameters.clear();
        parameters.setProperty("jndi_name", "mail/MyMailSession");
        DeleteJavaMailResource deleteCommand = habitat.getComponent(DeleteJavaMailResource.class);
        assertTrue(deleteCommand != null);
        cr.getCommandInvocation("delete-javamail-resource", context.getActionReport()).parameters(parameters).execute(deleteCommand);
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
        boolean isDeleted = true;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof MailResource) {
                MailResource jr = (MailResource) resource;
                if (jr.getJndiName().equals("mail/MyMailSession")) {
                    isDeleted = false;
                    logger.fine("JavaMailResource config bean mail/MyMailSession is deleted.");
                    break;
                }
            }
        }
        assertTrue(isDeleted);
        logger.fine("msg: " + context.getActionReport().getMessage());
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
        Servers servers = habitat.getComponent(Servers.class);
        boolean isRefDeleted = true;
        for (Server server : servers.getServer()) {
            if (server.getName().equals(SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME)) {
                for (ResourceRef ref : server.getResourceRef()) {
                    if (ref.getRef().equals("mail/MyMailSession")) {
                        isRefDeleted = false;
                        break;
                    }
                }
            }
        }
        assertTrue(isRefDeleted);
    }

    /**
     * Test of execute method, of class DeleteJavaMailResource.
     * delete-javamail-resource doesnotexist
     */
    @Test
    public void testExecuteFailDoesNotExist() {
        parameters.setProperty("jndi_name", "doesnotexist");
        DeleteJavaMailResource deleteCommand = habitat.getComponent(DeleteJavaMailResource.class);
        cr.getCommandInvocation("delete-javamail-resource", context.getActionReport()).parameters(parameters).execute(deleteCommand);
        assertEquals(ActionReport.ExitCode.FAILURE, context.getActionReport().getActionExitCode());
        logger.fine("msg: " + context.getActionReport().getMessage());
    }
}