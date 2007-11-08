package dol.validation;

import java.io.*;
import com.sun.enterprise.deployment.archivist.*;
import com.sun.enterprise.deployment.Descriptor;


public class Validator {
    
    public static void main(String args[]) {
        String fileName = args[0];
        String ext = getExtension(fileName);

        String outputFileName = fileName + "1" + ext;
        String outputFileName2 = fileName + "2" + ext;

        
        // first read/parse and write out the original valid archive
        try {
            Archivist archivist = 
                ArchivistFactory.getArchivistForArchive(fileName);
            archivist.setHandleRuntimeInfo(true);
            archivist.setArchiveUri(fileName);
	    archivist.setXMLValidation(true);
	    archivist.setXMLValidationLevel("full");
            log("Reading/parsing the orginal archive: " + 
                fileName);
            Descriptor descriptor = 
                ApplicationArchivist.openArchive(archivist, 
                    new File(fileName), true);     
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

            Archivist archivist = 
                ArchivistFactory.getArchivistForArchive(outputFileName);
            archivist.setHandleRuntimeInfo(true);
            archivist.setArchiveUri(outputFileName);
            archivist.setXMLValidation(true);
            archivist.setXMLValidationLevel("full");
            log("Reading/parsing the output archive" + 
                outputFileName);
            Descriptor descriptor = 
                ApplicationArchivist.openArchive(archivist, 
                    new File(outputFileName), true);
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

            Archivist archivist =
                ArchivistFactory.getArchivistForArchive(outputFileName2);
            archivist.setHandleRuntimeInfo(true);
            archivist.setArchiveUri(outputFileName2);
            archivist.setXMLValidation(true);
            archivist.setXMLValidationLevel("full");
            log("Reading/parsing the output archive" +
                outputFileName2);
            Descriptor descriptor =
                ApplicationArchivist.openArchive(archivist,
                    new File(outputFileName2), true);
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
