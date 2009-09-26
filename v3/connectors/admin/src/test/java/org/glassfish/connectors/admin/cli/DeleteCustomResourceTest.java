

package org.glassfish.connectors.admin.cli;

import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.v3.common.PropsFileActionReporter;
import com.sun.logging.LogDomains;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.AdminCommandContext;
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

public class DeleteCustomResourceTest extends ConfigApiTest {
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
                LogDomains.getLogger(DeleteCustomResourceTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());
    }

    @After
    public void tearDown() throws TransactionFailure {
        ConfigSupport.apply(new SingleConfigCode<Resources>() {
            public Object run(Resources param) throws PropertyVetoException, TransactionFailure {
                Resource target = null;
                for (Resource resource : param.getResources()) {
                    if (resource instanceof CustomResource) {
                        CustomResource r = (CustomResource) resource;
                        if (r.getJndiName().equals("sample_custom_resource")) {
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

    /**
     * Test of execute method, of class DeleteCustomResource.
     * delete-custom-resource sample_custom_resource
     */
    @Test
    public void testExecuteSuccessDefaultTarget() {
        CreateCustomResource createCommand = habitat.getComponent(CreateCustomResource.class);
        assertTrue(createCommand != null);
        parameters.setProperty("restype", "topic");
        parameters.setProperty("factoryclass", "javax.naming.spi.ObjectFactory");
        parameters.setProperty("jndi_name", "sample_custom_resource");
        cr.getCommandInvocation("create-custom-resource", context.getActionReport()).parameters(parameters).execute(createCommand);
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());

        parameters.clear();
        DeleteCustomResource deleteCommand = habitat.getComponent(DeleteCustomResource.class);
        assertTrue(deleteCommand != null);
        parameters.setProperty("jndi_name", "sample_custom_resource");
        cr.getCommandInvocation("delete-custom-resource", context.getActionReport()).parameters(parameters).execute(deleteCommand);
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
        boolean isDeleted = true;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof CustomResource) {
                CustomResource jr = (CustomResource) resource;
                if (jr.getJndiName().equals("sample_custom_resource")) {
                    isDeleted = false;
                    logger.fine("CustomResource config bean sample_custom_resource is deleted.");
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
                    if (ref.getRef().equals("sample_custom_resource")) {
                        isRefDeleted = false;
                        break;
                    }
                }
            }
        }
        assertTrue(isRefDeleted);
    }

    /**
     * Test of execute method, of class DeleteCustomResource.
     * delete-custom-resource doesnotexist
     */
    @Test
    public void testExecuteFailDoesNotExist() {
        DeleteCustomResource deleteCommand = habitat.getComponent(DeleteCustomResource.class);
        assertTrue(deleteCommand != null);
        parameters.setProperty("jndi_name", "doesnotexist");
        cr.getCommandInvocation("delete-custom-resource", context.getActionReport()).parameters(parameters).execute(deleteCommand);
        assertEquals(ActionReport.ExitCode.FAILURE, context.getActionReport().getActionExitCode());
        logger.fine("msg: " + context.getActionReport().getMessage());
    }

}