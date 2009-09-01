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


public class CreateJndiResourceTest extends ConfigApiTest {

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
        context = new AdminCommandContext(
                LogDomains.getLogger(CreateJndiResourceTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());
        cr = habitat.getComponent(CommandRunner.class);
    }

    @After
    public void tearDown() throws TransactionFailure {
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
     * Test of execute method, of class CreateJndiResource.
     * asadmin create-jndi-resource --restype=queue --factoryclass=sampleClass --jndilookupname=sample_jndi
     * sample_jndi_resource
     */
    @Test
    public void testExecuteSuccess() {
        parameters.setProperty("jndilookupname", "sample_jndi");
        parameters.setProperty("restype", "queue");
        parameters.setProperty("factoryclass", "sampleClass");
        parameters.setProperty("jndi_name", "sample_jndi_resource");
        CreateJndiResource command = habitat.getComponent(CreateJndiResource.class);
        cr.doCommand("create-jndi-resource", command, parameters, context.getActionReport());
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
        boolean isCreated = false;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof ExternalJndiResource) {
                ExternalJndiResource r = (ExternalJndiResource) resource;
                if (r.getJndiName().equals("sample_jndi_resource")) {
                    assertEquals("queue", r.getResType());
                    assertEquals("sample_jndi", r.getJndiLookupName());
                    assertEquals("sampleClass", r.getFactoryClass());
                    assertEquals("true", r.getEnabled());
                    isCreated = true;
                    logger.fine("Jndi Resource config bean sample_jndi_resource is created.");
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
                    if (ref.getRef().equals("sample_jndi_resource")) {
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
     * Test of execute method, of class CreateJndiResource.
     * asadmin create-jndi-resource --restype=queue --factoryclass=sampleClass --jndilookupname=sample_jndi
     * dupRes
     * asadmin create-jndi-resource --restype=queue --factoryclass=sampleClass --jndilookupname=sample_jndi
     * dupRes
     */
    @Test
    public void testExecuteFailDuplicateResource() {
        parameters.setProperty("jndilookupname", "sample_jndi");
        parameters.setProperty("restype", "queue");
        parameters.setProperty("factoryclass", "sampleClass");
        parameters.setProperty("jndi_name", "dupRes");
        CreateJndiResource command1 = habitat.getComponent(CreateJndiResource.class);
        cr.doCommand("create-jndi-resource", command1, parameters, context.getActionReport());
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
        boolean isCreated = false;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof BindableResource) {
                BindableResource jr = (BindableResource) resource;
                if (jr.getJndiName().equals("dupRes")) {
                    isCreated = true;
                    logger.fine("Jndi Resource config bean dupRes is created.");
                    break;
                }
            }
        }
        assertTrue(isCreated);

        CreateJndiResource command2 = habitat.getComponent(CreateJndiResource.class);
        cr.doCommand("create-jndi-resource", command2, parameters, context.getActionReport());
        assertEquals(ActionReport.ExitCode.FAILURE, context.getActionReport().getActionExitCode());
        int numDupRes = 0;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof BindableResource) {
                BindableResource jr = (BindableResource) resource;
                if (jr.getJndiName().equals("dupRes")) {
                    numDupRes = numDupRes + 1;
                }
            }
        }
        assertEquals(1, numDupRes);
        logger.fine("msg: " + context.getActionReport().getMessage());
    }

    /**
     * Test of execute method, of class CreateJndiResource.
     * asadmin create-jndi-resource --restype=queue --factoryclass=sampleClass --jndilookupname=sample_jndi
     * --enabled=false --description=External JNDI Resource
     * sample_jndi_resource
     */
    @Test
    public void testExecuteWithOptionalValuesSet() {
        parameters.setProperty("jndilookupname", "sample_jndi");
        parameters.setProperty("restype", "queue");
        parameters.setProperty("factoryclass", "sampleClass");
        parameters.setProperty("enabled", "false");
        parameters.setProperty("description", "External JNDI Resource");
        parameters.setProperty("jndi_name", "sample_jndi_resource");
        CreateJndiResource command = habitat.getComponent(CreateJndiResource.class);
        cr.doCommand("create-jndi-resource", command, parameters, context.getActionReport());
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
        boolean isCreated = false;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof ExternalJndiResource) {
                ExternalJndiResource r = (ExternalJndiResource) resource;
                if (r.getJndiName().equals("sample_jndi_resource")) {
                    assertEquals("queue", r.getResType());
                    assertEquals("sampleClass", r.getFactoryClass());
                    assertEquals("sample_jndi", r.getJndiLookupName());
                    assertEquals("false", r.getEnabled());
                    assertEquals("External JNDI Resource", r.getDescription());
                    isCreated = true;
                    logger.fine("Jndi Resource config bean sample_jndi_resource is created.");
                    break;
                }
            }
        }
        assertTrue(isCreated);
        logger.fine("msg: " + context.getActionReport().getMessage());
    }

    /**
     * Test of execute method, of class CreateJndiResource.
     * asadmin create-jndi-resource --factoryclass=sampleClass --jndilookupname=sample_jndi
     * sample_jndi_resource
     */
    @Test
    public void testExecuteFailInvalidResType() {
        parameters.setProperty("factoryclass", "sampleClass");
        parameters.setProperty("jndilookupname", "sample_jndi");
        parameters.setProperty("jndi_name", "sample_jndi_resource");
        CreateJndiResource command = habitat.getComponent(CreateJndiResource.class);
        cr.doCommand("create-jndi-resource", command, parameters, context.getActionReport());
        assertEquals(ActionReport.ExitCode.FAILURE, context.getActionReport().getActionExitCode());
    }

    /**
     * Test of execute method, of class CreateJndiResource.
     * asadmin create-jndi-resource --factoryclass=sampleClass --restype=queue
     * sample_jndi_resource
     */
    @Test
    public void testExecuteFailInvalidJndiLookupName() {
        parameters.setProperty("factoryclass", "sampleClass");
        parameters.setProperty("restype", "queue");
        parameters.setProperty("jndi_name", "sample_jndi_resource");
        CreateJndiResource command = habitat.getComponent(CreateJndiResource.class);
        cr.doCommand("create-jndi-resource", command, parameters, context.getActionReport());
        assertEquals(ActionReport.ExitCode.FAILURE, context.getActionReport().getActionExitCode());
    }
}
