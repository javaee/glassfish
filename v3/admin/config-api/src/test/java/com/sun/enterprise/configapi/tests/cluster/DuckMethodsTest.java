package com.sun.enterprise.configapi.tests.cluster;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.configapi.tests.ConfigApiTest;
import org.glassfish.tests.utils.Utils;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hk2.component.Habitat;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test a number of cluster related {@link org.jvnet.hk2.config.DuckTyped}
 * methods implementation
 *
 * @author Jerome Dochez 
 */
public class DuckMethodsTest extends ConfigApiTest {
    Habitat habitat;

    public String getFileName() {
        return "ClusterDomain";
    }

    @Before
    public void setup() {
        habitat = Utils.getNewHabitat(this);        
    }


    @Test
    public void getClusterFromServerTest() {
        Domain d = habitat.getComponent(Domain.class);
        Server server = d.getServerNamed("server");
        assertTrue(server!=null);
        Cluster cluster = server.getCluster();
        System.out.println("Cluster name is " + cluster.getName());
    }
}
