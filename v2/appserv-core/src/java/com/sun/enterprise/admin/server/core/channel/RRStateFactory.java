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
package com.sun.enterprise.admin.server.core.channel;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.SystemPropertyConstants;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.enterprise.util.i18n.StringManager;

import java.io.IOException;

/**
 * Responsible for persisting restart required state.
 *
 * @author Nazrul Islam
 * @since  JDK1.4
 */
public class RRStateFactory {

    /**
     * Saves the restart required state of the server instance.
     *
     * @param   state  restart required state
     * @throws  IOException  if an i/o error
     */
    public static void saveState(boolean state) throws IOException {

        File stateFile  = getStateFile(null);
        FileWriter fw   = new FileWriter(stateFile);
        try {
            fw.write(Boolean.toString(state));
            fw.flush();
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {}
            }
        }
    }

    /**
     * Reads the persisted server instance's restart required state.
     * If the state file is not present, it returns false, i.e., 
     * server instance does not require a restart.
     * 
     * This uses current server's instance root.
     *
     * @return  restart required state
     */
    public static boolean getState() {
        return getState(null);
    }

    /**
     * Reads the persisted server instance's restart required state.
     * If the state file is not present, it returns false, i.e., 
     * server instance does not require a restart.
     *
     * @param   instanceRoot  server instance root
     * @return  restart required state
     */
    public static boolean getState(String instanceRoot) {

        boolean restartNeeded = false;
        File f = getStateFile(instanceRoot);

        if (f.exists()) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(f));
                String state = br.readLine();
                restartNeeded = new Boolean(state.trim()).booleanValue();
            } catch (IOException ioe) {
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {}
                }
            }
        }

        return restartNeeded;
    }

    /**
     * Removes the restart required state file.
     */
    public static void removeStateFile() {
        File state = getStateFile(null);
        if (state.exists()) {
            FileUtils.liquidate(state);
        }
    }

    /**
     * Returns the state file handle.
     *
     * @param   iRoot  instance root
     * @return  the state file
     */
    private static File getStateFile(String instanceRoot) {

        // instance root
        if (instanceRoot == null) {
            instanceRoot = System.getProperty(
                SystemPropertyConstants.INSTANCE_ROOT_PROPERTY, DEF_LOCATION);
        }

        // state file
        File f = new File(instanceRoot + File.separator + STATE_FILE_NM);

        return f;
    }

    public static void main(String[] args) {
        try {
            System.setProperty(
                SystemPropertyConstants.INSTANCE_ROOT_PROPERTY, "/tmp");
            saveState(true);
            boolean state = getState();
            System.out.println(state);
            removeStateFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---- INSTANCE VARIABLE(S) - PRIVATE ------------------------------
    private static final String STATE_FILE_NM = ".restart-required-state";
    private static final String DEF_LOCATION  = ".";
}
