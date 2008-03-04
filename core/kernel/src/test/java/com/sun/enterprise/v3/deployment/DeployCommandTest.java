package com.sun.enterprise.v3.deployment;


import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import com.sun.enterprise.v3.deployment.DeployCommand;
import org.glassfish.api.ActionReport;
import com.sun.enterprise.v3.common.PropsFileActionReporter;
import com.sun.enterprise.deploy.shared.FileArchive;
import com.sun.enterprise.v3.data.ApplicationInfo;
import org.glassfish.api.deployment.archive.ReadableArchive;


/**
 * junit test to test DeployCommand class
 */
public class DeployCommandTest {
    private DeployCommand dc = null;
    final ActionReport report = new PropsFileActionReporter();

    @Test
    public void checkIfAppIsRegisteredTest() {
        try {
            dc.name = "hello";
            dc.force = "false";
            ReadableArchive archive = new FileArchive();
            archive.open(new java.net.URL("http://wiki.glassfish.java.net/gfwiki/attach/HelloUser/hello1.war").toURI());
            ApplicationInfo ai = new ApplicationInfo(archive, "hello", null);

                //test for force=true and app!= null
            assertTrue(dc.checkIfAppIsRegistered(ai, report));
                //test for force=true and app=null
            assertFalse(dc.checkIfAppIsRegistered(null, report));

                //test for force=false and app!=null
            dc.force="true";
            assertFalse(dc.checkIfAppIsRegistered(ai, report));
                //test for force=false and app!=null
            assertFalse(dc.checkIfAppIsRegistered(null, report));            
        }
        catch (Exception e) {
                //ignore exception
                //e.printStackTrace();
        }
    }

    @Before
    public void setup() {
        dc = new DeployCommand();
    }
}
