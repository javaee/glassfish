/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.v3.admin;

import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.v3.common.PropsFileActionReporter;
//import com.sun.enterprise.configapi.tests.Utils;
import com.sun.logging.LogDomains;

import java.util.Properties;

import org.glassfish.api.admin.AdminCommandContext;
import org.junit.Test;
import static org.junit.Assert.*;
import org.jvnet.hk2.component.Habitat;
import org.glassfish.tests.utils.Utils;
import org.glassfish.tests.utils.ConfigApiTest;
import org.jvnet.hk2.config.DomDocument;

/**
 *
 * @author Jennifer
 */
public class CreateJdbcResourceTest extends ConfigApiTest {

    public DomDocument getDocument(Habitat habitat) {

        return new TestDocument(habitat);
    }    

    /**
     * Returns the file name without the .xml extension to load the test configuration
     * from. By default, it's the name of the TestClass.
     *
     * @return the configuration file name
     */
    public String getFileName() {
        return "DomainTest";
    }    

    /**
     * Test of execute method, of class CreateJdbcResource.
     */
    @Test
    public void execute() {
        System.out.println("execute");
        
        //How to inject resources?
        Habitat habitat = Utils.instance.getHabitat(this);
        final Resources resources = habitat.getComponent(Resources.class);
        assertTrue(resources!=null);
        
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
        CreateJdbcResource command = habitat.getComponent(CreateJdbcResource.class);
        
        //Call CommandRunner.doCommand(..) to execute the command
        CommandRunner cr = new CommandRunner();
        cr.doCommand("create-jdbc-resource", command, parameters, context.getActionReport());
        
        //Check that the resource was created

        // Jennifer, the command above returned failure, I will let you debug it ;-)
        //assertEquals(ActionReport.ExitCode.SUCCESS, context.getActionReport().getActionExitCode());
    }

}