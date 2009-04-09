package com.sun.enterprise.configapi.tests.model;

import com.sun.enterprise.configapi.tests.ConfigApiTest;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Resource;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import org.jvnet.hk2.config.ConfigModel;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.component.Habitat;

import java.util.List;
import java.util.logging.Logger;


/**
 * Test the Document allModelsImplementing API.
 */
public class AllModelsImplementingTest extends ConfigApiTest {

    public String getFileName() {
        return "DomainTest";
    }

    @Test
    public void checkResources() throws ClassNotFoundException {

        Habitat habitat = getHabitat();
        Resources resources = habitat.getComponent(Resources.class);
        Dom dom = Dom.unwrap(resources);
        List <ConfigModel> models = dom.document.getAllModelsImplementing(Resource.class);
        for (ConfigModel model : models) {
            Logger.getAnonymousLogger().fine(model.targetTypeName);
        }
        assertTrue(models.size()>0);

    }

}
