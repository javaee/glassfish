package com.sun.enterprise.configapi.tests.extensibility;

import com.sun.enterprise.configapi.tests.ConfigApiTest;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import org.jvnet.hk2.component.Habitat;
import org.glassfish.tests.utils.Utils;
import org.glassfish.api.admin.config.Container;
import org.junit.Test;
import org.junit.Ignore;
import static org.junit.Assert.*;

import java.util.List;

/**
 * @author Jerome Dochez
 */
public class ContainerExtensionTest extends ConfigApiTest {


    Habitat habitat = Utils.getNewHabitat(this);

    public String getFileName() {
        return "Extensibility";
    }
    
    @Test
    public void existenceTest() {

        Config config = habitat.getComponent(Domain.class).getConfigs().getConfig().get(0);
        List<Container> containers = config.getContainers();
        assertTrue(containers.size()==1);
        RandomContainer container = (RandomContainer) containers.get(0);
        assertEquals("random", container.getName());
        assertEquals("1243", container.getNumberOfRuntime());
        RandomElement element = container.getRandomElement();
        assertNotNull(element);
        assertEquals("foo", element.getAttr1());
    }
}
