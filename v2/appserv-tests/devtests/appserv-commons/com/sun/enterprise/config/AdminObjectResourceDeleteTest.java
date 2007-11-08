package com.sun.enterprise.config;

import com.sun.enterprise.config.serverbeans.AdminObjectResource;
import com.sun.enterprise.config.serverbeans.AdminService;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.JmxConnector;
import com.sun.enterprise.config.serverbeans.NodeAgent;
import com.sun.enterprise.config.serverbeans.NodeAgents;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import java.io.File;
import java.io.FileReader;
import junit.framework.*;
import org.netbeans.modules.schema2beans.BaseProperty;
import org.netbeans.modules.schema2beans.BeanProp;
import org.netbeans.modules.schema2beans.Common;
import org.xml.sax.InputSource;

/**
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 * @version $Revision: 1.2 $
 */

public class AdminObjectResourceDeleteTest extends TestCase {
    public void testWithClonedContext() throws Exception {
        // Set up the domain.xml file - adding an admin object resource to it
        AdminObjectResource aor = new AdminObjectResource();
        aor.setJndiName("toby/aor");
        aor.setObjectType("user");
        aor.setResAdapter("generic-ra");
        aor.setResType("connector.MyAdminObject");
        final ConfigContext configCtx =ConfigFactory.createConfigContext("domain.xml", new MyHandler());
        final Resources r = (Resources) ServerBeansFactory.getDomainBean(configCtx).getResources(); 
        assertNotNull("Unexpected null Resources object", r);
        r.addAdminObjectResource(aor);
        assertEquals(aor, r.getAdminObjectResourceByJndiName("toby/aor"));
        configCtx.flush();

        // Construct the deleter:
        final String xpath=aor.getXPath();      
        assertEquals("/domain/resources/admin-object-resource[@jndi-name='toby/aor']", xpath);
        ConfigDelete configDelete=ConfigChangeFactory.createConfigDelete(xpath); 
        assertEquals("delete xpath=/domain/resources/admin-object-resource[@jndi-name='toby/aor']", ""+configDelete);
  
        final ConfigContext ctx2 = (ConfigContext)configCtx.clone();
        ctx2.updateFromConfigChange(configDelete);
        
          // Demonstrate that its no longer there for ctx2
        Resources r2 = (Resources) ServerBeansFactory.getDomainBean(ctx2).getResources(); 
        assertNotNull("Unexpected null Resources object", r2);
        assertNull("Unexpected AdminObjectResource from ctx2", r2.getAdminObjectResourceByJndiName("toby/aor"));
 
        // If we don't do a flush()/refresh() we find that the AdminObject is still available
        // from the original context
        
        r2 = (Resources) ServerBeansFactory.getDomainBean(configCtx).getResources(); 
        assertNotNull("Unexpected null Resources object", r2);
        
        // WE DONT WANT THIS - THIS IS THE PROBLEM
        assertNotNull("Unexpected AdminObjectResource from configCtx", r2.getAdminObjectResourceByJndiName("toby/aor"));

        
        // The flush()/refresh() is necessary to get the changed clone to write to disc
        // and the original to read from disc, thus communicating the change between them
        ctx2.flush();
      configCtx.refresh(true);
        
          // and now its gone away!
        r2 = (Resources) ServerBeansFactory.getDomainBean(configCtx).getResources(); 
        assertNotNull("Unexpected null Resources object", r2);
        assertNull("Unexpected AdminObjectResource from configCtx", r2.getAdminObjectResourceByJndiName("toby/aor"));
    }

    public AdminObjectResourceDeleteTest(String name){
        super(name);
    }

    protected void setUp() throws Exception {
        Runtime.getRuntime().exec("rm -f domain.xml").waitFor();
        Runtime.getRuntime().exec("cp domain.orig.xml domain.xml").waitFor();
    }

    protected void tearDown() throws Exception {
        //Runtime.getRuntime().exec("rm -f domain.xml").waitFor();
    }

    private void nyi(){
        fail("Not Yet Implemented");
    }

    public static void main(String args[]){
        if (args.length == 0){
            junit.textui.TestRunner.run(AdminObjectResourceDeleteTest.class);
        } else {
            junit.textui.TestRunner.run(makeSuite(args));
        }
    }
    private static TestSuite makeSuite(String args[]){
        final TestSuite ts = new TestSuite();
        for (int i = 0; i < args.length; i++){
            ts.addTest(new AdminObjectResourceDeleteTest(args[i]));
        }
        return ts;
    }

        
}
