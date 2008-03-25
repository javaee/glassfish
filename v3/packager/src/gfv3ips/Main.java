/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gfv3ips;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author ludo
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String CHANGEME = "/export/tinderbox/dinesh/wsImp"; //Where is V3 repository
                
        
        File web = new File(CHANGEME+"/v3/distributions/web/target/web.zip");
        File nucleus = new File(CHANGEME+"/v3/distributions/nucleus/target/nucleus.zip");
        HashMap<String, String> zipWebEntries = new HashMap();
        HashMap<String, String> zipNucleusEntries = new HashMap();
        try {

            unzip(web, zipWebEntries);
            unzip(nucleus, zipNucleusEntries);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        System.out.println("\n\nNucleus content");

        for (Iterator it = zipNucleusEntries.keySet().iterator(); it.hasNext();) {
            String object = (String) it.next();
            if (object.indexOf("domains/domain1") >= 0) {
                continue;
            }
            System.out.println(object);
        }

        System.out.println("\n\nWeb Content");
        for (Iterator it = zipWebEntries.keySet().iterator(); it.hasNext();) {
            String object = (String) it.next();
            if (object.indexOf("modules/web") >= 0) {
                System.out.println(object);
            }
        }

        System.out.println("\n\nDomain Content");
        for (Iterator it = zipWebEntries.keySet().iterator(); it.hasNext();) {
            String object = (String) it.next();
            if (object.indexOf("domains/domain1") >= 0) {
                System.out.println(object);
            }
        }
        System.out.println("\n\nGlassFish Common Content");
        for (Iterator it = zipWebEntries.keySet().iterator(); it.hasNext();) {
            String object = (String) it.next();
            if (object.indexOf("modules/web") >= 0) {
                continue;
            }
            if (object.indexOf("domains/domain1") >= 0) {
                continue;
            }
            if (object.indexOf("glassfish/javadb") >= 0) {
                continue;
            }
            if (zipNucleusEntries.get(object) != null) {
                continue;
            }
            System.out.println(object);

        }
    }

    private static String getrelLoc(String s) {
        return s;
    }

    /* unzip the zipFile into the TargetFolder
     *
     */
    public static void unzip(File zipFile, HashMap map) throws IOException {
        InputStream source = new FileInputStream(zipFile);

        ZipInputStream zip = new ZipInputStream(source);
        try {
            ZipEntry ent;
            while ((ent = zip.getNextEntry()) != null) {
                String protoString = "";
                if (ent.isDirectory()) {
                    protoString = "d none " + getrelLoc(ent.getName()) + " 755 root bin";
                } else {
                    protoString = "f none " + getrelLoc(ent.getName()) + " 644 root bin";
                }
                map.put(protoString, protoString);
            }
        } finally {
            zip.close();
        }
    }
}
