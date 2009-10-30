package org.glassfish.tests.embedded;

import org.glassfish.api.embedded.*;

import java.io.File;


/** A 'wrapper' class that is used by QA to run tests on embedded. This class is used to start the domain
 * in embedded mode from asadmin cli
 *
 */

public class EmbeddedMain {
    public static void main(String[] args) {

       String installRoot = System.getenv("S1AS_HOME");
       if (installRoot == null) {
           System.out.println("Environment variable S1AS_HOME not defined - it must point to the glassfish install root");
           return;
       }
       String instanceRoot = installRoot + "/domains/domain1";
       String domainXml = instanceRoot + "/config/domain.xml";

        System.setProperty("com.sun.aas.instanceRootURI", "file:" + instanceRoot);
        //System.setProperty("com.sun.aas.installRoot", installRoot );
//        System.setProperty("com.sun.aas.instanceRoot", instanceRoot );

       EmbeddedFileSystem efs =
          new EmbeddedFileSystem.Builder().
          installRoot(new File(installRoot), true).
          instanceRoot(new File(instanceRoot)).
          configurationFile(new File(domainXml), false).autoDelete(false).build();

        Server.Builder builder = new Server.Builder("asadmin");

        Server server = builder.embeddedFileSystem(efs).build();
        server.addContainer(ContainerBuilder.Type.all);
    }
}


