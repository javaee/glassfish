/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
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
