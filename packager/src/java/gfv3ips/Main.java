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

    public static HashMap<String, String> zipWebEntries = new HashMap();
    public static HashMap<String, String> zipNucleusEntries = new HashMap();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length < 2) {
               System.out.println(
                "Usage:java Main wsRoot[v3 parent] pkgName[common,nucleus,web]");
	       return;
        }

        String CHANGEME = args[0]; //Where is V3 repository
        
        File web = new File(CHANGEME+"/v3/distributions/web/target/web.zip");
        File nucleus = new File(CHANGEME+"/v3/distributions/nucleus/target/nucleus.zip");
        try {

            unzip(web, zipWebEntries);
            unzip(nucleus, zipNucleusEntries);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (args[1].indexOf("nucleus")  != -1) {
           printNucleus();
        }
            
        if (args[1].indexOf("web")  != -1) {
           printWeb();
        }

        if (args[1].indexOf("common")  != -1) {
           printCommon();
        }

    }

    public static void printInitial() {
            System.out.println("i copyright");
            System.out.println("i pkginfo");
            System.out.println("i depend");
    }

    public static void printNucleus() {
	printInitial();
        for (Iterator it = zipNucleusEntries.keySet().iterator(); it.hasNext();) {
            String object = (String) it.next();
            if (object.indexOf("domains/domain1") >= 0) {
                continue;
            }
            if (object.indexOf("domains") >= 0) {
                continue;
            }
            System.out.println(object);
        }
    }

    public static void printWeb() {
	printInitial();
        for (Iterator it = zipWebEntries.keySet().iterator(); it.hasNext();) {
            String object = (String) it.next();
            if (object.indexOf("modules/web") >= 0) {
                System.out.println(object);
            }
        }
    }

    public static void printCommon() {
	printInitial();
        /* for (Iterator it = zipWebEntries.keySet().iterator(); it.hasNext();) {
            String object = (String) it.next();
            if (object.indexOf("domains/domain1") >= 0) {
                System.out.println(object);
            }
        } */

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
