package override;

import java.io.*;
import java.net.*;
import java.util.Set;
import com.sun.enterprise.deployment.archivist.*;
import com.sun.enterprise.deployment.*;
import org.glassfish.deployment.common.Descriptor;

import org.glassfish.api.deployment.archive.ReadableArchive;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.single.StaticModulesRegistry;
import com.sun.enterprise.module.bootstrap.StartupContext;
import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ProcessEnvironment.ProcessType;
import org.glassfish.internal.api.Globals;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

public class OverrideTest {

    private static ServiceLocator serviceLocator = null;

    private static final String EXPECTED_RESOURCE_JNDI_NAME = "jdbc/__default";
    private static final String EXPECTED_RESOURCE_DESCRIPTION = "override";
    private static final String EXPECTED_RESOURCE_SHARING_SCOPE = "Unshareable";
    private static final String EXPECTED_RESOURCE_AUTHORIZATION = "Container";

    public static void main(String args[]) {

        String fileName = args[0];
        String ext = getExtension(fileName);
        String archiveType = ext.substring(1);
        if ("jar".equals(archiveType)) {
          if (fileName.contains("car")) {
            archiveType = "car";
          } else {
            archiveType = "ejb";
          }
        }

        boolean expectException = false;
        if (args.length > 1) {
          if ("true".equals(args[1])) {
            expectException = true;
          }
        }

        String outputFileName = fileName + "1" + ext;
        String outputFileName2 = fileName + "2" + ext;

        prepareServiceLocator();

        ArchivistFactory archivistFactory = serviceLocator.getService(ArchivistFactory.class);
        ArchiveFactory archiveFactory = serviceLocator.getService(ArchiveFactory.class);
        Archivist archivist = null;

        ReadableArchive archive = null;
        
        // first read/parse and write out the original valid archive
        try {
            File archiveFile = new File(fileName);
            archive = archiveFactory.openArchive(
                archiveFile);
            ClassLoader classloader = new URLClassLoader(
                new URL[] { new File(archiveFile, "WEB-INF/classes").toURL() });

            archivist = archivistFactory.getArchivist(archiveType, classloader);
            archivist.setAnnotationProcessingRequested(true);
            JndiNameEnvironment nameEnv = (JndiNameEnvironment)archivist.open(archiveFile);

            Set<ResourceReferenceDescriptor> resRefDescs = nameEnv.getResourceReferenceDescriptors(); 
 
            for (ResourceReferenceDescriptor resRef : resRefDescs) {
                String refName = resRef.getName();
                String jndiName = resRef.getJndiName();
                String mappedName = resRef.getMappedName();
                String lookupName = resRef.getLookupName();
                String description = resRef.getDescription();
                String auth = resRef.getAuthorization();
                String scope = resRef.getSharingScope();
                log ("Resource ref [" + refName + "] with JNDI name: " + jndiName + ", description: " + description + ", authorization: " + auth + ", sharing scope: " + scope + ", mappedName: " + mappedName + ", lookupName: " + lookupName);
                if (refName.equals("myDS7") && 
                    !description.equals(EXPECTED_RESOURCE_DESCRIPTION)) {
                    log("Descriptor did not override the @Resource description attribute as expected");
                    fail();
                } else if (refName.equals("myDS5and6")) { 
                    Set<InjectionTarget> targets = resRef.getInjectionTargets();
                    for (InjectionTarget target : targets) {
                       log("Target class name: " + target.getClassName());
                       log("Target name: " + target.getTargetName());
                    }
                    if (targets.size() != 2) {
                        log("The additional injection target specified in the descriptor is not used as expected");
                        fail();
                    }
                } else if (refName.equals("myDS8") && 
                    !mappedName.equals(EXPECTED_RESOURCE_JNDI_NAME)) {
                    log("Descriptor did not override the @Resource mapped-name attribute as expected");
                    fail();
                } else if (refName.equals("myDS7") && 
                    !scope.equals(EXPECTED_RESOURCE_SHARING_SCOPE)) {
                    log("Descriptor did not override the @Resource sharing scope attribute as expected");
                    fail();
                } else if (refName.equals("myDS7") && 
                    !auth.equals(EXPECTED_RESOURCE_AUTHORIZATION)) {
                    log("Descriptor did not override the @Resource authorization attribute as expected");
                    fail();
                } else if (refName.equals("myDS7") && 
                    !lookupName.equals(EXPECTED_RESOURCE_JNDI_NAME)) {
                    log("Descriptor did not override the @Resource lookup name attribute as expected");
                    fail();
                }
            }
  
        } catch (Exception e) {
            e.printStackTrace();
            log("Input archive: [" + fileName + 
                "] is not valid");
            fail();
        } finally {
            try {
                if (archive != null) {
                    archive.close();
                }
            } catch(IOException ioe) {
            }
        }
    }

    private static void log(String message) {
        System.out.println("[dol.override.OverrideTest]:: " + message);
        System.out.flush();
    }

    private static void pass() {
        log("PASSED: devtests/deployment/dol/override");
        System.exit(0);
    }

    private static void fail() {
        log("FAILED: devtests/deployment/dol/override");
        System.exit(-1);
    }

    private static String getExtension(String file) {
        String ext = file.substring(file.lastIndexOf("."));
        return ext;
    }

    private static void prepareServiceLocator() {
        if ( (serviceLocator == null) ) {
            // Bootstrap a hk2 environment.
            ModulesRegistry registry = new StaticModulesRegistry(Thread.currentThread().getContextClassLoader());
            serviceLocator = registry.createServiceLocator("default");
            StartupContext startupContext = new StartupContext();

            ServiceLocatorUtilities.addOneConstant(serviceLocator, startupContext);
            ServiceLocatorUtilities.addOneConstant(serviceLocator, 
                new ProcessEnvironment(ProcessEnvironment.ProcessType.Other));

            Globals.setDefaultHabitat(serviceLocator);
        }
    }

}
