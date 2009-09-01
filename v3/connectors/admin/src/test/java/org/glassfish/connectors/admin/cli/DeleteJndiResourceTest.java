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


public class DeleteJndiResourceTest extends ConfigApiTest {
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
        parameters = new Properties();
        cr = habitat.getComponent(CommandRunner.class);
        context = new AdminCommandContext(
                LogDomains.getLogger(DeleteJndiResourceTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());
    }

    @After
    public void tearDown() throws TransactionFailure {
        parameters.clear();
        ConfigSupport.apply(new SingleConfigCode<Resources>() {
            public Object run(Resources param) throws PropertyVetoException, TransactionFailure {
                Resource target = null;
                for (Resource resource : param.getResources()) {
                    if (resource instanceof BindableResource) {
                        BindableResource r = (BindableResource) resource;
                        if (r.getJndiName().equals("sample_jndi_resource") ||
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
     * Test of execute method, of class DeleteJndiResource.
     * asadmin create-jndi-resource --restype=queue --factoryclass=sampleClass --jndilookupname=sample_jndi
     * sample_jndi_resource
     * delete-jndi-resource sample_jndi_resource
     */
    @Test
    public void testExecuteSuccessDefaultTarget() {
        parameters.setProperty("restype", "topic");
        parameters.setProperty("jndilookupname", "sample_jndi");
        parameters.setProperty("factoryclass", "javax.naming.spi.ObjectFactory");
        parameters.setProperty("jndi_name", "sample_jndi_resource");
        CreateJndiResource createCommand = habitat.getComponent(CreateJndiResource.class);
        cr.doCommand("create-jndi-resource", createCommand, parameters, context.getActionReport());
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
        parameters.clear();
        parameters.setProperty("jndi_name", "sample_jndi_resource");
        DeleteJndiResource deleteCommand = habitat.getComponent(DeleteJndiResource.class);
        cr.doCommand("delete-jndi-resource", deleteCommand, parameters, context.getActionReport());
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
        boolean isDeleted = true;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof BindableResource) {
                BindableResource jr = (BindableResource) resource;
                if (jr.getJndiName().equals("sample_jndi_resource")) {
                    isDeleted = false;
                    logger.fine("Jndi Resource config bean sample_jndi_resource is created.");
                    break;
                }
            }
        }
        assertTrue(isDeleted);
        logger.fine("msg: " + context.getActionReport().getMessage());
        Servers servers = habitat.getComponent(Servers.class);
        boolean isRefDeleted = true;
        for (Server server : servers.getServer()) {
            if (server.getName().equals(SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME)) {
                for (ResourceRef ref : server.getResourceRef()) {
                    if (ref.getRef().equals("sample_jndi_resource")) {
                        isRefDeleted = false;
                        break;
                    }
                }
            }
        }
        assertTrue(isRefDeleted);
    }

    /**
     * Test of execute method, of class DeleteJndiResource.
     * delete-jndi-resource doesnotexist
     */
    @Test
    public void testExecuteFailDoesNotExist() {
        parameters.setProperty("jndi_name", "doesnotexist");
        DeleteJndiResource deleteCommand = habitat.getComponent(DeleteJndiResource.class);
        cr.doCommand("delete-jndi-resource", deleteCommand, parameters, context.getActionReport());
        assertEquals(ActionReport.ExitCode.FAILURE, context.getActionReport().getActionExitCode());
        logger.fine("msg: " + context.getActionReport().getMessage());
    }
}
