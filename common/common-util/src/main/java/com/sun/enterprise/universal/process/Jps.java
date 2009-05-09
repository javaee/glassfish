/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.universal.process;

import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.util.OS;
import java.io.*;
import java.util.*;

/**
 * Run a native process with jps
 * -- get the pid for a running JVM
 * note:  dropping in an implementation for jps is not hard.
 * @author bnevins
 */
public class Jps {

    /**
     * return the platform-specific process-id of a JVM
     * @param mainClassName The main class - this is how we identify the right JVM
     * @return the process id if possible otherwise 0
     */
    final static public int getPid(String mainClassName) {
        Jps jps = new Jps();
        Integer integer = jps.pidMap.get(mainClassName);

        if(integer == null)
            return 0;

        return integer;
    }
    /**
     * Is this pid owned by a process?
     * @param apid the pid of interest
     * @return whether there is a process running with that id
     */
    final static public boolean isPid(int apid) {
        return new Jps().pidMap.containsValue(apid);
    }
    
    private Jps(){
        try {
            if(jpsExe == null)
                return;

            ProcessBuilder pb = new ProcessBuilder(jpsExe.getPath());
            Process p = pb.start();
            ProcessStreamDrainer saver = ProcessStreamDrainer.save("jps", p);
            saver.waitFor();
            String jpsOutput = saver.getOutString();
            // get each line
            String[] ss = jpsOutput.split("[\n\r]");

            for(String line : ss) {
                if(line == null || line.length() <= 0)
                    continue;

                String[] sublines = line.split(" ");
                if(sublines == null || sublines.length != 2)
                    continue;

                int aPid = 0;
                try {
                    aPid = Integer.parseInt(sublines[0]);
                }
                catch(Exception e) {
                    continue;
                }
                // todo -- handle duplicate names??
                pidMap.put(sublines[1], aPid);
            }
        }
        catch(Exception e) {
        }
    }

    private Map<String,Integer> pidMap = new  HashMap<String,Integer>();
    private static final File jpsExe;
    private static final String jpsName;

    static{
        if(OS.isWindows())
            jpsName = "jps.exe";
        else
            jpsName = "jps";

        final String    javaroot    = System.getProperty("java.home");
        final String    relpath     = "/bin/" + jpsName;
        final File      fhere       = new File(javaroot + relpath);
        File            fthere      = new File(javaroot + "/.." + relpath);

        if(fhere.isFile())
            jpsExe = SmartFile.sanitize(fhere);
        else if(fthere.isFile())
            jpsExe = SmartFile.sanitize(fthere);
        else
            jpsExe = null;
    }
}

