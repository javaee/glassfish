/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.connectors.admin.cli;

import com.sun.enterprise.config.serverbeans.CustomResource;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.v3.common.PropsFileActionReporter;
import com.sun.logging.LogDomains;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.MessagePart;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.tests.utils.ConfigApiTest;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.DomDocument;

import java.util.List;
import java.util.Properties;

public class ListCustomResourcesTest extends ConfigApiTest {

    private Habitat habitat = getHabitat();
    private AdminCommandContext context ;
    private CommandRunner cr;
    private int origNum;
    private Properties parameters;


    public DomDocument getDocument(Habitat habitat) {
        return new TestDocument(habitat);
    }

    public String getFileName() {
        return "DomainTest";
    }

    @Before
    public void setUp() {
        parameters = new Properties();
        cr = habitat.getComponent(CommandRunner.class);
        context = new AdminCommandContext(
                LogDomains.getLogger(ListCustomResourcesTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());
        Resources resources = habitat.getComponent(Resources.class);
        assertTrue(resources != null);
        for (Resource resource : resources.getResources()) {
            if (resource instanceof CustomResource) {
                origNum = origNum + 1;
            }
        }
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of execute method, of class ListCustomResources.
     * list-custom-resources
     */
    @Test
    public void testExecuteSuccessListOriginal() {
        ListCustomResources listCommand = habitat.getComponent(ListCustomResources.class);
        cr.doCommand("list-custom-resources", listCommand, parameters, context.getActionReport());
        List<MessagePart> list = context.getActionReport().getTopMessagePart().getChildren();
        if (origNum == 0) {
            //Nothing to list.
        } else {
            assertEquals(origNum, list.size());
        }
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
    }

    /**
     * Test of execute method, of class ListCustomResources.
     * create-custom-resource ---restype=topic --factoryclass=javax.naming.spi.ObjectFactory
     * Resource1
     * list-custom-resources
     */
    @Test
    public void testExecuteSuccessListResource1() {

        CreateCustomResource createCommand = habitat.getComponent(CreateCustomResource.class);
        assertTrue(createCommand != null);
        parameters.setProperty("restype", "topic");
        parameters.setProperty("factoryclass", "javax.naming.spi.ObjectFactory");
        parameters.setProperty("jndi_name", "custom_resource1");
        cr.doCommand("create-custom-resource", createCommand, parameters, context.getActionReport());
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());

        parameters.clear();
        ListCustomResources listCommand = habitat.getComponent(ListCustomResources.class);
        cr.doCommand("list-custom-resources", listCommand, parameters, context.getActionReport());

        List<MessagePart> list = context.getActionReport().getTopMessagePart().getChildren();
        assertEquals(origNum + 1, list.size());
        List<String> listStr = new java.util.ArrayList<String>();
        for (MessagePart mp : list) {
            listStr.add(mp.getMessage());
        }
        assertTrue(listStr.contains("custom_resource1"));
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
    }

    /**
     * Test of execute method, of class ListCustomResources.
     * delete-custom-resource Resource1
     * list-Custom-resources
     */
    @Test
    public void testExecuteSuccessListNoResource1() {

        DeleteCustomResource deleteCommand = habitat.getComponent(DeleteCustomResource.class);
        assertTrue(deleteCommand != null);
        parameters.setProperty("jndi_name", "custom_resource1");
        cr.doCommand("delete-custom-resource", deleteCommand, parameters, context.getActionReport());
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());

        parameters.clear();
        ListCustomResources listCommand = habitat.getComponent(ListCustomResources.class);
        cr.doCommand("list-custom-resources", listCommand, parameters, context.getActionReport());

        List<MessagePart> list = context.getActionReport().getTopMessagePart().getChildren();
        if ((origNum - 1) == 0) {
            //Nothing to list.
        } else {
            assertEquals(origNum - 1, list.size());
        }
        List<String> listStr = new java.util.ArrayList<String>();
        for (MessagePart mp : list) {
            listStr.add(mp.getMessage());
        }
        assertFalse(listStr.contains("custom_resource1"));
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
    }
}