package org.glassfish.tests.embedded;

import org.glassfish.api.embedded.*;

import org.glassfish.api.container.Sniffer;
import java.util.*;
import java.io.File;
import java.util.logging.Logger;

public class EmbeddedMain {
    public static void main(String[] args) {


       String installRoot = System.getenv("S1AS_HOME");
       if (installRoot == null) {
           System.out.println("Environment variable S1AS_HOME not defined - it must point to the glassfish install root");
           return;
       }
       String instanceRoot = installRoot + "/domains/domain1";
       String domainXml = instanceRoot + "/config/domain.xml";
       EmbeddedFileSystem efs =
          new EmbeddedFileSystem.Builder().
          setInstallRoot(new File(installRoot), true).
          setInstanceRoot(new File(instanceRoot)).
          setConfigurationFile(new File(domainXml)).
          build();

        Server.Builder builder = new Server.Builder("asadmin");
        //.logFile(new File("/export/home/log.log")).logger(true);
        Server server = builder.setEmbeddedFileSystem(efs).build();

        System.setProperty("com.sun.aas.instanceRootURI", instanceRoot + "/applications");

        server.addContainer(ContainerBuilder.Type.all);

        ArrayList<Sniffer> sniffers = new ArrayList<Sniffer>();
        for (EmbeddedContainer c : server.getContainers()) {
            sniffers.addAll(c.getSniffers());
        }
    }
}
