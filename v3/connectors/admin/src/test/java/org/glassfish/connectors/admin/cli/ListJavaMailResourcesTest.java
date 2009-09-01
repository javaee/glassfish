

package org.glassfish.connectors.admin.cli;


import com.sun.enterprise.config.serverbeans.MailResource;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ListJavaMailResourcesTest extends ConfigApiTest {

    private Habitat habitat;
    private int origNum = 0;
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
        parameters = new Properties();
        cr = habitat.getComponent(CommandRunner.class);
        assertTrue(cr != null);
        Resources resources = habitat.getComponent(Resources.class);
        context = new AdminCommandContext(
                LogDomains.getLogger(ListJavaMailResourcesTest.class, LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter());
        for (Resource resource : resources.getResources()) {
            if (resource instanceof MailResource) {
                origNum = origNum + 1;
            }
        }
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of execute method, of class ListJavaMailResources.
     * list-javamail-resources
     */
    @Test
    public void testExecuteSuccessListOriginal() {
        ListJavaMailResources listCommand = habitat.getComponent(ListJavaMailResources.class);
        cr.doCommand("list-javamail-resources", listCommand, parameters, context.getActionReport());
        List<MessagePart> list = context.getActionReport().getTopMessagePart().getChildren();
        if (origNum == 0) {
            //Nothing to list
        } else {
            assertEquals(origNum, list.size());
        }
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
    }


    /**
     * Test of execute method, of class ListJavaMailResource.
     * create-javamail-resource --mailuser=test --mailhost=localhost
     * --fromaddress=test@sun.com mailresource
     * list-javamail-resources
     */
    @Test
    public void testExecuteSuccessListMailResource() {
        parameters.setProperty("mailhost", "localhost");
        parameters.setProperty("mailuser", "test");
        parameters.setProperty("fromaddress", "test@sun.com");
        parameters.setProperty("jndi_name", "mailresource");
        CreateJavaMailResource createCommand = habitat.getComponent(CreateJavaMailResource.class);
        assertTrue(createCommand != null);
        cr.doCommand("create-javamail-resource", createCommand, parameters, context.getActionReport());
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());

        parameters.clear();
        ListJavaMailResources listCommand = habitat.getComponent(ListJavaMailResources.class);
        assertTrue(listCommand != null);
        cr.doCommand("list-javamail-resources", listCommand, parameters, context.getActionReport());
        List<MessagePart> list = context.getActionReport().getTopMessagePart().getChildren();
        assertEquals(origNum + 1, list.size());
        List<String> listStr = new ArrayList<String>();
        for (MessagePart mp : list) {
            listStr.add(mp.getMessage());
        }
        assertTrue(listStr.contains("mailresource"));
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
    }


    /**
     * Test of execute method, of class ListJdbcResource.
     * delete-javamail-resource mailresource
     * list-javamail-resources
     */
    @Test
    public void testExecuteSuccessListNoMailResource() {
        parameters.setProperty("jndi_name", "mailresource");
        DeleteJavaMailResource deleteCommand = habitat.getComponent(DeleteJavaMailResource.class);
        assertTrue(deleteCommand != null);
        cr.doCommand("delete-javamail-resource", deleteCommand, parameters, context.getActionReport());
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());

        parameters.clear();
        ListJavaMailResources listCommand = habitat.getComponent(ListJavaMailResources.class);
        cr.doCommand("list-javamail-resources", listCommand, parameters, context.getActionReport());
        List<MessagePart> list = context.getActionReport().getTopMessagePart().getChildren();
        assertEquals(origNum - 1, list.size());
        List<String> listStr = new ArrayList<String>();
        for (MessagePart mp : list) {
            listStr.add(mp.getMessage());
        }
        assertFalse(listStr.contains("mailresource"));
        assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
    }


}