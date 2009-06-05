package dol.validation;

import java.io.*;
import com.sun.enterprise.deployment.archivist.*;
import com.sun.enterprise.deployment.Descriptor;

import com.sun.enterprise.module.ModulesRegistry;
import org.jvnet.hk2.component.Habitat;
import com.sun.hk2.component.ExistingSingletonInhabitant;
import com.sun.enterprise.module.single.StaticModulesRegistry;
import com.sun.enterprise.module.bootstrap.StartupContext;
import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ProcessEnvironment.ProcessType;

import com.sun.enterprise.deploy.shared.ArchiveFactory;
import org.glassfish.api.deployment.archive.ReadableArchive;



public class Validator {

    public static void main(String args[]) {
        String fileName = args[0];
        String ext = getExtension(fileName);

        String outputFileName = fileName + "1" + ext;
        String outputFileName2 = fileName + "2" + ext;

        // Bootstrap a hk2 environment.
        ModulesRegistry registry = new StaticModulesRegistry(Thread.currentThread().getContextClassLoader());
        Habitat habitat = registry.createHabitat("default");

        StartupContext startupContext = new StartupContext();
        habitat.add(new ExistingSingletonInhabitant(startupContext));

        habitat.addComponent(null, new ProcessEnvironment(ProcessEnvironment.ProcessType.Other));

        ArchivistFactory archivistFactory = habitat.getComponent(ArchivistFactory.class);
        ApplicationFactory applicationFactory = habitat.getComponent(ApplicationFactory.class);
        ArchiveFactory archiveFactory = habitat.getComponent(ArchiveFactory.class);
        
        // first read/parse and write out the original valid archive
        try {
            ReadableArchive archive = archiveFactory.openArchive(
                new File(fileName));
            Archivist archivist = 
                archivistFactory.getArchivist(archive, null);
            archivist.setHandleRuntimeInfo(true);
            archivist.setArchiveUri(fileName);
	    archivist.setXMLValidation(true);
	    archivist.setXMLValidationLevel("full");
            log("Reading/parsing the orginal archive: " + 
                fileName);
            Descriptor descriptor = 
                applicationFactory.openArchive(archivist, 
                    archive, true);     
            log("Writing out the archive to: " + 
                outputFileName);
            archivist.write(outputFileName);
        } catch (Exception e) {
            e.printStackTrace();
            log("Input archive: [" + fileName + 
                "] is not valid");
            fail();
        }

        // Read/parse the resulted archive, it should be valid too
        // then write to another archive
        try {

            ReadableArchive archive = archiveFactory.openArchive(
                new File(outputFileName));
            Archivist archivist = 
                archivistFactory.getArchivist(archive, null);
            archivist.setHandleRuntimeInfo(true);
            archivist.setArchiveUri(outputFileName);
            archivist.setXMLValidation(true);
            archivist.setXMLValidationLevel("full");
            log("Reading/parsing the output archive" + 
                outputFileName);
            Descriptor descriptor = 
                applicationFactory.openArchive(archivist, 
                    archive, true);
            log("Writing out the archive to: " +
                outputFileName2);
            archivist.write(outputFileName2);
        } catch (Exception e) {
            e.printStackTrace();
            log("The output archive: [" + outputFileName + 
                "] is not valid");
            fail();
        }

        // Read/parse the resulted archive, it should be valid too
        try {

            ReadableArchive archive = archiveFactory.openArchive(
                new File(outputFileName2));
            Archivist archivist =
                archivistFactory.getArchivist(archive, null);
            archivist.setHandleRuntimeInfo(true);
            archivist.setArchiveUri(outputFileName2);
            archivist.setXMLValidation(true);
            archivist.setXMLValidationLevel("full");
            log("Reading/parsing the output archive" +
                outputFileName2);
            Descriptor descriptor =
                applicationFactory.openArchive(archivist,
                    archive, true);
        } catch (Exception e) {
            e.printStackTrace();
            log("The output archive: [" + outputFileName2 +
                "] is not valid");
            fail();
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
}
