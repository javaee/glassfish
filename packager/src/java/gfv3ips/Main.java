/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
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
    public static HashMap<String, String> zipBundleEntries = new HashMap();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length < 2) {
               System.out.println(
                "Usage:java Main wsRoot[v3 parent] pkgName[common,nucleus,web]");
	       return;
        }

	boolean done=false;

        String CHANGEME = args[0]; //Where is V3 repository
        
        File web = new File(CHANGEME+"/v3/distributions/web/target/web.zip");
        File nucleus = new File(CHANGEME+"/v3/distributions/nucleus/target/nucleus.zip");
        if (web.isFile() && nucleus.isFile())
        {
          try {
              unzip(web, zipWebEntries);
              unzip(nucleus, zipNucleusEntries);
          } catch (IOException ex) {
              ex.printStackTrace();
          }

          if (args[1].indexOf("nucleus")  != -1) {
             printNucleus();
	     done = true;
          }
            
          if (args[1].indexOf("web")  != -1) {
             printWeb();
	     done = true;
          }

          if (args[1].indexOf("common")  != -1) {
             printCommon();
	     done = true;
          }
        }
       
        if ( done != true) {
           //Any generic bundle can be used to create prototype for SVR4 pkg.
	   File generic = new File (CHANGEME);
           if (generic.isFile()) {
             try {
                 unzip(generic, zipBundleEntries);
             } catch (IOException ex) {
                  ex.printStackTrace();
             }

             printBundle(args[1]);
	     done = true;
          }
	}

    }

    public static void printBundle(String bundle) {
	printInitial();
        for (Iterator it = zipBundleEntries.keySet().iterator(); it.hasNext();) {
            String object = (String) it.next();
            if (object.indexOf(bundle) >= 0) {
                System.out.println(object);
            }
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
