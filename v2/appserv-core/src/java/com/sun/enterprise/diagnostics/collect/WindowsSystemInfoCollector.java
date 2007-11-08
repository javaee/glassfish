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
package com.sun.enterprise.diagnostics.collect;

import com.sun.logging.LogDomains;
import com.sun.enterprise.diagnostics.Data;
import com.sun.enterprise.diagnostics.Defaults;


import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.File;

/**
 * Class to collect System Information for Windows OS
 */
public class WindowsSystemInfoCollector implements Collector{

    private static Logger logger =
            LogDomains.getLogger(LogDomains.ADMIN_LOGGER);

    /* AdminCommand for properties */
    private static final String MEMORY_INFO_CMD = "mem";
    private static final String IP_ADDRESS_INFO_CMD = "ipconfig";
    private static final String HOST_NAME_CMD = "hostname";
    private static final String OS_INFO_CMD = "ver";

    private String destFolder = null;


    public WindowsSystemInfoCollector(String destFolder){
        this.destFolder = destFolder;
    }

    /**
     * To capture the system information for Windows OS
     * @return  Data representing System Information
     */
    public Data capture(){

        FileData data = null;

        String outputFileName = destFolder + File.separator + Defaults.SYSTEM_INFO_FILE;

        final String ALL_CMDS =

                "(echo HOSTNAME & "+HOST_NAME_CMD+" & " +
                        "echo OS INFO & "+ OS_INFO_CMD +" & " +
                        "echo MEMORY INFO & " + MEMORY_INFO_CMD  + " & " +
        "echo IP ADDRESS & " + IP_ADDRESS_INFO_CMD + ") >> " + outputFileName ;


        String[] cmd = {"cmd.exe", "/C", ALL_CMDS};

        ProcessExecutor executor = new ProcessExecutor(cmd, 0);
        try{
        executor.execute();

        File outputFile = new File(outputFileName);

        data = new FileData(outputFile.getName(),DataType.SYSTEM_INFO);

        }
        catch(ProcessExecutorException pee){
            logger.log(Level.WARNING, "Exception while capturing system info" +
                     " : " + pee.getMessage());
        }
        
        return data;
    }
}
