package dol.validation;

import java.io.*;
import com.sun.enterprise.deployment.archivist.*;
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

public class Validator {

    private static ServiceLocator serviceLocator = null;

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
            archivist = archivistFactory.getArchivist(archiveType);
            archivist.setHandleRuntimeInfo(true);
            archivist.setArchiveUri(fileName);
	    archivist.setXMLValidation(true);
	    archivist.setXMLValidationLevel("full");
            archivist.setRuntimeXMLValidation(true);
            archivist.setRuntimeXMLValidationLevel("full");
            log("Reading/parsing the orginal archive: " + 
                fileName);
            try {
              Descriptor descriptor = archivist.open(archiveFile);
            } catch (Exception ex) {
              if (expectException) {
                log("Expected exception");
              } else {
                throw ex;
              }
            }
            log("Writing out the archive to: " + 
                outputFileName);
            archivist.write(archive, outputFileName);
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

        // Read/parse the resulted archive, it should be valid too
        // then write to another archive
        try {
            File archiveFile = new File(outputFileName);
            archive = archiveFactory.openArchive(
                archiveFile);
            archivist = archivistFactory.getArchivist(archiveType);
            archivist.setHandleRuntimeInfo(true);
            archivist.setArchiveUri(outputFileName);
            archivist.setXMLValidation(true);
            archivist.setXMLValidationLevel("full");
            archivist.setRuntimeXMLValidation(true);
            archivist.setRuntimeXMLValidationLevel("full");
            log("Reading/parsing the output archive" + 
                outputFileName);
            Descriptor descriptor = archivist.open(archiveFile);
        } catch (Exception e) {
            e.printStackTrace();
            log("The input archive: [" + outputFileName + 
                "] is not valid");
            fail();
        }
        try {
            log("Writing out the archive to: " +
                outputFileName2);
            archivist.write(archive, outputFileName2);
        } catch (Exception e) {
            e.printStackTrace();
            log("The output archive: [" + outputFileName2 + 
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

        // Read/parse the resulted archive, it should be valid too
        try {

            File archiveFile = new File(outputFileName2);
            archive = archiveFactory.openArchive(
                archiveFile);
            archivist = archivistFactory.getArchivist(archiveType);
            archivist.setHandleRuntimeInfo(true);
            archivist.setArchiveUri(outputFileName2);
            archivist.setXMLValidation(true);
            archivist.setXMLValidationLevel("full");
            archivist.setRuntimeXMLValidation(true);
            archivist.setRuntimeXMLValidationLevel("full");
            log("Reading/parsing the output archive" +
                outputFileName2);
            Descriptor descriptor = archivist.open(archiveFile);
        } catch (Exception e) {
            e.printStackTrace();
            log("The output archive: [" + outputFileName2 +
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
        System.out.println("[dol.validation.Validator]:: " + message);
        System.out.flush();
    }

    private static void pass() {
        log("PASSED: devtests/deployment/dol/validation");
        System.exit(0);
    }

    private static void fail() {
        log("FAILED: devtests/deployment/dol/validation");
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
