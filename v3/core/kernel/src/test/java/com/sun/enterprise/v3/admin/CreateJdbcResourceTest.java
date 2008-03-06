/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.v3.admin;

import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.v3.common.PropsFileActionReporter;
//import com.sun.enterprise.configapi.tests.Utils;
import com.sun.logging.LogDomains;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommandContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.InjectionManager;
import org.jvnet.hk2.component.UnsatisfiedDepedencyException;

/**
 *
 * @author Jennifer
 */
public class CreateJdbcResourceTest {

    public CreateJdbcResourceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of execute method, of class CreateJdbcResource.
     */
    @Test
    public void execute() {
        System.out.println("execute");
        
        //How to inject resources?
        //Habitat habitat = Utils.instance.getHabitat("DomainTest");
        //final Resources resources = habitat.getComponent(Resources.class);
        //assertTrue(resources!=null);
        
        //Inject parameters?
        final Properties parameters = new Properties();
        parameters.setProperty("connectionpoolid", "cpa");
        parameters.setProperty("enabled", "true");
        parameters.setProperty("description", "my resource");
        parameters.setProperty("jndi_name", "jdbc/foo");
        AdminCommandContext context = new AdminCommandContext(
                LogDomains.getLogger(LogDomains.ADMIN_LOGGER),
                new PropsFileActionReporter(), parameters);
        
        //Get an instance of the CreateJdbcResource command
        //CreateJdbcResource adminCommand = new CreateJdbcResource();
        //Habitat defaultHabitat = Utils.instance.getHabitat("default");
        //CreateJdbcResource command = defaultHabitat.getComponent(CreateJdbcResource.class);
        
        //Call CommandRunner.doCommand(..) to execute the command
        //command.execute(context);     
        CommandRunner cr = new CommandRunner();
        //cr.doCommand("create-jdbc-resource", command, parameters, context.getActionReport());
        
        //Check that the resource was created
        //assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
    }

}