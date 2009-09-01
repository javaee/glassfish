package org.glassfish.connectors.admin.cli;

import com.sun.enterprise.config.serverbeans.ExternalJndiResource;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.v3.common.PropsFileActionReporter;
import com.sun.logging.LogDomains;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.tests.utils.ConfigApiTest;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.DomDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ListJndiResourcesTest extends ConfigApiTest {

    private Habitat habitat;
    private int origNum = 0;
    private Properties parameters;
    AdminCommandContext context;
    CommandRunner cr;

    public DomDocument getDocument(Habitat habitat) {
        return new TestDocument(habitat);
    }

    public String getFileName() {
        return "DomainTest";
    }

    @Before
    public void setUp() {
        habitat = getHabitat();
        cr = habitat.getComponent(CommandRunner.class);
        context = new AdminCommandContext(
                LogDomains.getLogger(ListJndiResourcesTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());
        parameters = new Properties();
        Resources resources = habitat.getComponent(Resources.class);
        for (Resource resource : resources.getResources()) {
            if (resource instanceof ExternalJndiResource) {
                origNum = origNum + 1;
            }
        }
    }

    @After
    public void tearDown() {

    }

    /**
     * Test of execute method, of class ListJndiResources.
     * list-jndi-resources
     */
    @Test
    public void testExecuteSuccessListOriginal() {
        ListJndiResources listCommand = habitat.getComponent(ListJndiResources.class);
        cr.doCommand("list-jndi-resources", listCommand, parameters, context.getActionReport());
        List<ActionReport.MessagePart> list = context.getActionReport().getTopMessagePart().getChildren();
        if (origNum == 0) {
            //Nothing to list.
        } else {
            assertEquals(origNum, list.size());
        }
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
    }

    /**
     * Test of execute method, of class ListJndiResources.
     * create-jndi-resource ---restype=topic --factoryclass=javax.naming.spi.ObjectFactory --jndilookupname=sample_jndi
     * resource
     * list-jndi-resources
     */
    @Test
    public void testExecuteSuccessListResource() {
        parameters.setProperty("restype", "topic");
        parameters.setProperty("jndilookupname", "sample_jndi");
        parameters.setProperty("factoryclass", "javax.naming.spi.ObjectFactory");
        parameters.setProperty("jndi_name", "resource");
        CreateJndiResource createCommand = habitat.getComponent(CreateJndiResource.class);
        cr.doCommand("create-jndi-resource", createCommand, parameters, context.getActionReport());
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
        parameters.clear();
        ListJndiResources listCommand = habitat.getComponent(ListJndiResources.class);
        cr.doCommand("list-jndi-resources", listCommand, parameters, context.getActionReport());
        List<ActionReport.MessagePart> list = context.getActionReport().getTopMessagePart().getChildren();
        assertEquals(origNum + 1, list.size());
        List<String> listStr = new ArrayList<String>();
        for (ActionReport.MessagePart mp : list) {
            listStr.add(mp.getMessage());
        }
        assertTrue(listStr.contains("resource"));
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
    }

    /**
     * Test of execute method, of class ListJndiResource.
     * delete-jndi-resource resource
     * list-jndi-resources
     */
    @Test
    public void testExecuteSuccessListNoResource() {
        parameters.setProperty("jndi_name", "resource");
        DeleteJndiResource deleteCommand = habitat.getComponent(DeleteJndiResource.class);
        cr.doCommand("delete-jndi-resource", deleteCommand, parameters, context.getActionReport());
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
        parameters.clear();
        ListJndiResources listCommand = habitat.getComponent(ListJndiResources.class);
        cr.doCommand("list-jndi-resources", listCommand, parameters, context.getActionReport());
        List<ActionReport.MessagePart> list = context.getActionReport().getTopMessagePart().getChildren();
        if ((origNum - 1) == 0) {
            //Nothing to list.
        } else {
            assertEquals(origNum - 1, list.size());
        }
        List<String> listStr = new ArrayList<String>();
        for (ActionReport.MessagePart mp : list) {
            listStr.add(mp.getMessage());
        }
        assertFalse(listStr.contains("resource"));
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
    }
}
