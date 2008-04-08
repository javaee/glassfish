package com.sun.enterprise.configapi.tests;

import org.junit.Test;
import static org.junit.Assert.*;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.component.Habitat;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.JdbcResource;
import com.sun.enterprise.config.serverbeans.Resource;

import java.beans.PropertyVetoException;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Feb 5, 2008
 * Time: 10:50:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConcurrentModificationsTest extends ConfigApiTest {

    /**
     * Returns the file name without the .xml extension to load the test configuration
     * from. By default, it's the name of the TestClass.
     *
     * @return the configuration file name
     */
    public String getFileName() {
        return "DomainTest";
    }

    @Test(expected= TransactionFailure.class)
    public void collectionTest() throws TransactionFailure {

        Habitat habitat = super.getHabitat();
        final Resources resources = habitat.getComponent(Resources.class);
        assertTrue(resources!=null);

        ConfigSupport.apply(new SingleConfigCode<Resources>() {

            public Object run(Resources writeableResources) throws PropertyVetoException, TransactionFailure {

                assertTrue(writeableResources!=null);
                JdbcResource newResource = ConfigSupport.createChildOf(writeableResources, JdbcResource.class);
                newResource.setJndiName("foo");
                newResource.setDescription("Random ");
                newResource.setPoolName("bar");
                newResource.setEnabled("true");
                writeableResources.getResources().add(newResource);

                // now let's check I have my copy...
                boolean found=false;
                for (Resource resource : writeableResources.getResources()) {
                    if (resource instanceof JdbcResource) {
                        JdbcResource jdbc = (JdbcResource) resource;
                        if (jdbc.getJndiName().equals("foo")) {
                            found = true;
                            break;
                        }
                    }
                }
                assertTrue(found);

                // now let's check that my readonly copy does not see it...
                boolean shouldNot = false;
                for (Resource resource : resources.getResources()) {
                    if (resource instanceof JdbcResource) {
                        JdbcResource jdbc = (JdbcResource) resource;
                        if (jdbc.getJndiName().equals("foo")) {
                            shouldNot = true;
                            break;
                        }
                    }
                }
                assertFalse(shouldNot);

                // now I am throwing a transaction failure since I don't care about saving it
                throw new TransactionFailure("Test passed", null);
            }        
        },resources);
    }
}
