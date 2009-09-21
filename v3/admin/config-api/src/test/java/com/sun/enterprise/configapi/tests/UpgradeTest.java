package com.sun.enterprise.configapi.tests;

import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.J2eeApplication;
import com.sun.enterprise.config.serverbeans.Module;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.grizzly.config.dom.ThreadPool;
import org.glassfish.api.admin.config.ConfigurationUpgrade;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

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
    public void threadPools() {
        List<String> names = new ArrayList<String>();
        for (ThreadPool pool : getHabitat().getComponent(Config.class).getThreadPools().getThreadPool()) {
            names.add(pool.getName());
        }
        assertTrue(names.contains("http-thread-pool") && names.contains("thread-pool-1"));
    }

    private void verify(String name) {
        assertTrue("Should find thread pool named " + name, getHabitat().getComponent(ThreadPool.class, name) != null);
    }
    @Test
    public void applicationUpgrade() {
        Applications apps = getHabitat().getComponent(Applications.class);
        assertTrue(apps!=null);
        for (Application app : apps.getApplications()) {
            assertTrue(app.getEngine().isEmpty());
            assertTrue(app.getModule().size()==1);
            for (Module module : app.getModule()) {
                assertTrue(module.getName().equals(app.getName()));
                assertTrue(!module.getEngines().isEmpty());
            }
        }
    }

    @Test
    public void j2eeApplicationUpgrade() {
        J2eeApplication application = getHabitat().getComponent(J2eeApplication.class);
        assertTrue("/foo/bar".equals(application.getLocation()));
    }
}
