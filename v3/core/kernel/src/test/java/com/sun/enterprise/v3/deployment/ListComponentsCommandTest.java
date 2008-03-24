package com.sun.enterprise.v3.deployment;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import java.util.Properties;
import com.sun.enterprise.v3.deployment.ListComponentsCommand;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.WebServiceEndpoint;
import com.sun.enterprise.config.serverbeans.Engine;
import com.sun.enterprise.config.serverbeans.Module;
import com.sun.enterprise.config.serverbeans.Property;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeSupport;
import java.util.List;
import java.util.ArrayList;


/**
 * junit test to test ListComponentsCommand class
 */
public class ListComponentsCommandTest {
    private ListComponentsCommand lcc = null;

    @Test
    public void isApplicationOfThisTypeTest() {
        try {
            ApplicationTest app = new ApplicationTest();
            Engine eng = new EngineTest();
            eng.setSniffer("web");
            List<Engine> engines = new ArrayList<Engine>();
            engines.add(eng);
            app.setEngines(engines);
        
            boolean ret = lcc.isApplicationOfThisType(app, "web");
            assertTrue("test app with sniffer engine=web", true==lcc.isApplicationOfThisType(app, "web"));
            //negative testcase
            assertFalse("test app with sniffer engine=web", true==lcc.isApplicationOfThisType(app, "ejb"));
        }
        catch (Exception ex) {
            //ignore exception
        } 
    }

        @Test
    public void getSnifferEnginesTest() {
        try {
            Engine eng1 = new EngineTest();
            eng1.setSniffer("web");
            Engine eng2 = new EngineTest();
            eng2.setSniffer("security");
            List<Engine> engines = new ArrayList<Engine>();
            engines.add(eng1);
            engines.add(eng2);
            
            ApplicationTest app = new ApplicationTest();            
            app.setEngines(engines);
            String snifferEngines = lcc.getSnifferEngines(app);
            assertEquals("compare all sniffer engines", "<web, security>",
                        snifferEngines);
        }
        catch (Exception ex) {
            //ignore exception
        } 
    }


    @Before
    public void setup() {
        lcc = new ListComponentsCommand();
    }

        //mock-up Application object
    public class ApplicationTest implements Application {
        private List<Engine> engineList = null;
        
        public String getName() {
            return "hello";
        }
        public void setName(String value) throws PropertyVetoException {}
        public String getContextRoot() { return "hello";}
        public void setContextRoot(String value) throws PropertyVetoException {}
        public String getLocation(){ return "";}
        public void setLocation(String value) throws PropertyVetoException{}
        public String getObjectType(){ return "";}
        public void setObjectType(String value) throws PropertyVetoException{}
        public String getEnabled(){ return "";}
        public void setEnabled(String value) throws PropertyVetoException{}
        public String getLibraries(){ return "";}
        public void setLibraries(String value) throws PropertyVetoException{}
        public String getAvailabilityEnabled(){ return "";}
        public void setAvailabilityEnabled(String value) throws PropertyVetoException{}
        public String getDirectoryDeployed(){ return "";}
        public void setDirectoryDeployed(String value) throws PropertyVetoException{}
        public String getDescription(){ return "";}
        public void setDescription(String value) throws PropertyVetoException{}
        public List<Engine> getEngine(){ return engineList;}
        public List<Property> getProperty(){ return null;}
        public List<WebServiceEndpoint> getWebServiceEndpoint() {return null;}
        
        public void setEngines(List<Engine> engines) {
            engineList = engines;
        }
        //hk2's Injectable class
        public void injectedInto(Object target){}
    }

            //mock-up Engine object
    public class EngineTest implements Engine {
        private String sniffer = "";
        public String getSniffer() {return sniffer;}
        public void setSniffer(String value) throws PropertyVetoException {
            sniffer = value;
        }
        public String getDescription() {return "";}
        public void setDescription(String value) {}
        public List<Property> getProperty() {return null;}

        //hk2's Injectable class
        public void injectedInto(Object target){}

            //config.serverbeans.Modules
        public String getName() { 
            return "hello";
        }
        public void setName(String value) throws PropertyVetoException {}
    }
    
}
