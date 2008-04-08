package com.sun.enterprise.v3.deployment;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import java.util.Properties;
import com.sun.enterprise.v3.common.ActionReporter;
import com.sun.enterprise.v3.common.HTMLActionReporter;
import com.sun.enterprise.v3.deployment.ReDeployCommand;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Module;
import com.sun.enterprise.config.serverbeans.Engine;
import com.sun.enterprise.config.serverbeans.Property;
import com.sun.enterprise.config.serverbeans.WebServiceEndpoint;
import java.beans.PropertyVetoException;
import java.util.List;

/**
 * junit test to test ReDeployCommand class
 */
public class ReDeployCommandTest {
    private ReDeployCommand rdc = null;
    private ApplicationTest app = null;

    @Test
    public void validateParametersTest() {
        try {
            ActionReporter report = new HTMLActionReporter();
            rdc.validateParameters(null, report);
            assertFalse("app is not registered", rdc.validateParameters(null, report));
            assertTrue("app is registered and directory deployed is true", rdc.validateParameters(app, report));
            app.setDirectoryDeployed("false");
                //reinitialize rdc so path is null
            rdc = new ReDeployCommand();
            assertFalse("app is registered and directory deployed is false", rdc.validateParameters(app, report));
        }
        catch(PropertyVetoException pve) {
                //ignore exception
        }
    }

    @Test
    public void getPathFromDomainXMLTest() {
        assertEquals("app is null", null, rdc.getPathFromDomainXML(null));
        assertEquals("path is /temp", "/temp", rdc.getPathFromDomainXML(app));
    }


    @Before
    public void setup() {
        rdc = new ReDeployCommand();
        app = new ApplicationTest();        
    }

        //mock-up Application object
    public class ApplicationTest implements Application {
        private String dirDeployed = "true";
        public String getName() {
            return "hello";
        }
        public void setName(String value) throws PropertyVetoException {}
        public String getContextRoot() { return "hello";}
        public void setContextRoot(String value) throws PropertyVetoException {}
        public String getLocation(){ return "file:/temp";}
        public void setLocation(String value) throws PropertyVetoException{}
        public String getObjectType(){ return "";}
        public void setObjectType(String value) throws PropertyVetoException{}
        public String getEnabled(){ return "";}
        public void setEnabled(String value) throws PropertyVetoException{}
        public String getLibraries(){ return "";}
        public void setLibraries(String value) throws PropertyVetoException{}
        public String getAvailabilityEnabled(){ return "";}
        public void setAvailabilityEnabled(String value) throws PropertyVetoException{}
        public String getDirectoryDeployed(){ return dirDeployed;}
        public void setDirectoryDeployed(String value) throws PropertyVetoException{
            dirDeployed = value;
        }
        public String getDescription(){ return "";}
        public void setDescription(String value) throws PropertyVetoException{}
        public List<Engine> getEngine(){ return null; }
        
        public List<Property> getProperty(){ return null;}
        public List<WebServiceEndpoint> getWebServiceEndpoint() {return null;}
        public void setEngines(List<Engine> engines) {}

        //hk2's Injectable class
        public void injectedInto(Object target){}
    }
    
}
