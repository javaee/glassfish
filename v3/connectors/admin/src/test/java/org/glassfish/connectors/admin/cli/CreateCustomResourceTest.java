package org.glassfish.connectors.admin.cli;

import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.v3.common.PropsFileActionReporter;
import com.sun.logging.LogDomains;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.tests.utils.ConfigApiTest;
import org.glassfish.tests.utils.Utils;
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


public class CreateCustomResourceTest extends ConfigApiTest {

    Habitat habitat = Utils.instance.getHabitat(this);
    private Properties parameters;
    private Resources resources = null;
    private CreateCustomResource command = null;
    private AdminCommandContext context = null;
    private CommandRunner cr = null;

    public DomDocument getDocument(Habitat habitat) {
        return new TestDocument(habitat);
    }

    public String getFileName() {
        return "DomainTest";
    }

    @Before
    public void setUp() {
        parameters = new Properties();
        resources = habitat.getComponent(Resources.class);
        assertTrue(resources != null);
        command = habitat.getComponent(CreateCustomResource.class);
        assertTrue(command != null);
        context = new AdminCommandContext(
                LogDomains.getLogger(CreateCustomResourceTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());
        cr = habitat.getComponent(CommandRunner.class);
    }

    @After
    public void tearDown() throws TransactionFailure {
        ConfigSupport.apply(new SingleConfigCode<Resources>() {
            public Object run(Resources param) throws PropertyVetoException, TransactionFailure {
                Resource target = null;
                for (Resource resource : param.getResources()) {
                    if (resource instanceof CustomResource) {
                        CustomResource r = (CustomResource) resource;
                        if (r.getJndiName().equals("sample_custom_resource") ||
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
     * Test of execute method, of class CreateCustomResource.
     * asadmin create-custom-resource --restype=topic --factoryclass=javax.naming.spi.ObjectFactory
     * sample_custom_resource
     */
    @Test
    public void testExecuteSuccess() {
        parameters.setProperty("restype", "topic");
        parameters.setProperty("factoryclass", "javax.naming.spi.ObjectFactory");
        parameters.setProperty("jndi_name", "sample_custom_resource");

        //Call CommandRunnerImpl.doCommand(..) to execute the command
        cr.getCommandInvocation("create-custom-resource", context.getActionReport()).parameters(parameters).execute(command);

        // Check the exit code is SUCCESS
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());

        //Check that the resource was created
        boolean isCreated = false;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof CustomResource) {
                CustomResource r = (CustomResource) resource;
                if (r.getJndiName().equals("sample_custom_resource")) {
                    assertEquals("topic", r.getResType());
                    assertEquals("javax.naming.spi.ObjectFactory", r.getFactoryClass());
                    assertEquals("true", r.getEnabled());
                    isCreated = true;
                    logger.fine("Custom Resource config bean sample_custom_resource is created.");
                    break;
                }
            }
        }
        assertTrue(isCreated);

        logger.fine("msg: " + context.getActionReport().getMessage());

        // Check resource-ref created
        Servers servers = habitat.getComponent(Servers.class);
        boolean isRefCreated = false;
        for (Server server : servers.getServer()) {
            if (server.getName().equals(SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME)) {
                for (ResourceRef ref : server.getResourceRef()) {
                    if (ref.getRef().equals("sample_custom_resource")) {
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
     * Test of execute method, of class CreateCustomResource.
     * asadmin create-custom-resource --restype=topic --factoryclass=javax.naming.spi.ObjectFactory
     * dupRes
     * asadmin create-custom-resource --restype=topic --factoryclass=javax.naming.spi.ObjectFactory
     * dupRes
     */
    @Test
    public void testExecuteFailDuplicateResource() {
        parameters.setProperty("restype", "topic");
        parameters.setProperty("factoryclass", "javax.naming.spi.ObjectFactory");
        parameters.setProperty("jndi_name", "dupRes");

        //Call CommandRunnerImpl.doCommand(..) to execute the command
        cr.getCommandInvocation("create-custom-resource", context.getActionReport()).parameters(parameters).execute(command);

        // Check the exit code is SUCCESS
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());

        //Check that the resource was created
        boolean isCreated = false;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof CustomResource) {
                CustomResource jr = (CustomResource) resource;
                if (jr.getJndiName().equals("dupRes")) {
                    isCreated = true;
                    logger.fine("Custom Resource config bean dupRes is created.");
                    break;
                }
            }
        }
        assertTrue(isCreated);

        //Try to create a duplicate resource dupRes. Get a new instance of the command.
        CreateCustomResource command2 = habitat.getComponent(CreateCustomResource.class);
        cr.getCommandInvocation("create-custom-resource", context.getActionReport()).parameters(parameters).execute(command2);

        // Check the exit code is FAILURE
        assertEquals(ActionReport.ExitCode.FAILURE, context.getActionReport().getActionExitCode());

        //Check that the 2nd resource was NOT created
        int numDupRes = 0;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof CustomResource) {
                CustomResource jr = (CustomResource) resource;
                if (jr.getJndiName().equals("dupRes")) {
                    numDupRes = numDupRes + 1;
                }
            }
        }
        assertEquals(1, numDupRes);
        logger.fine("msg: " + context.getActionReport().getMessage());
    }


    /**
     * Test of execute method, of class CreateCustomResource.
     * asadmin create-custom-resource --restype=topic --factoryclass=javax.naming.spi.ObjectFactory
     * --enabled=false --description=Administered Object sample_custom_resource
     */
    @Test
    public void testExecuteWithOptionalValuesSet() {
        parameters.setProperty("restype", "topic");
        parameters.setProperty("factoryclass", "javax.naming.spi.ObjectFactory");
        parameters.setProperty("enabled", "false");
        parameters.setProperty("description", "Administered Object");
        parameters.setProperty("jndi_name", "sample_custom_resource");

        //Call CommandRunnerImpl.doCommand(..) to execute the command
        cr.getCommandInvocation("create-custom-resource", context.getActionReport()).parameters(parameters).execute(command);

        // Check the exit code is SUCCESS
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());

        //Check that the resource was created
        boolean isCreated = false;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof CustomResource) {
                CustomResource r = (CustomResource) resource;
                if (r.getJndiName().equals("sample_custom_resource")) {
                    assertEquals("topic", r.getResType());
                    assertEquals("javax.naming.spi.ObjectFactory", r.getFactoryClass());
                    assertEquals("false", r.getEnabled());
                    assertEquals("Administered Object", r.getDescription());
                    isCreated = true;
                    logger.fine("Custom Resource config bean sample_custom_resource is created.");
                    break;
                }
            }
        }
        assertTrue(isCreated);
        logger.fine("msg: " + context.getActionReport().getMessage());
    }

    /**
     * Test of execute method, of class CreateCustomResource.
     * asadmin create-custom-resource --factoryclass=javax.naming.spi.ObjectFactory
     * sample_custom_resource
     */
    @Test
    public void testExecuteFailInvalidResType() throws TransactionFailure {
        parameters.setProperty("factoryclass", "javax.naming.spi.ObjectFactory");
        parameters.setProperty("jndi_name", "sample_custom_resource");

        //Call CommandRunnerImpl.doCommand(..) to execute the command
        cr.getCommandInvocation("create-custom-resource", context.getActionReport()).parameters(parameters).execute(command);

        // Check the exit code is SUCCESS
        assertEquals(ActionReport.ExitCode.FAILURE, context.getActionReport().getActionExitCode());
    }


}