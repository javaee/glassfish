package com.sun.enterprise.configapi.tests;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertTrue;
import org.glassfish.api.admin.config.ConfigurationUpgrade;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Module;
import com.sun.enterprise.config.serverbeans.J2eeApplication;

import java.util.logging.Logger;

/**
 * Simple test for the domain.xml upgrade scenario
 *
 * @author Jerome Dochez
 */
public class UpgradeTest extends ConfigApiTest {

    @Before
    public void setup() {
        Domain domain = getHabitat().getComponent(Domain.class);
        assertTrue(domain!=null);
        
        // perform upgrade
        for (ConfigurationUpgrade upgrade : getHabitat().getAllByContract(ConfigurationUpgrade.class)) {
            Logger.getAnonymousLogger().info("running upgrade " + upgrade.getClass());    
        }
    }

    @Test
    public void applicationUpgrade() {
        Applications apps = getHabitat().getComponent(Applications.class);
        assertTrue(apps!=null);
        for (Application app : apps.getApplications()) {
            assertTrue(app.getEngine().size()==0);
            assertTrue(app.getModule().size()==1);
            for (Module module : app.getModule()) {
                assertTrue(module.getName().equals(app.getName()));
                assertTrue(module.getEngines().size()>0);
            }
        }
    }

    @Test
    public void j2eeApplicationUpgrade() {
        J2eeApplication application = getHabitat().getComponent(J2eeApplication.class);
        assertTrue("/foo/bar".equals(application.getLocation()));
    }
}
