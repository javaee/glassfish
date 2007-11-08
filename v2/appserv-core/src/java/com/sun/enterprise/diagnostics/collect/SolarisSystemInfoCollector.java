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

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Class to collect System Information for Solaris OS
 */
public class SolarisSystemInfoCollector implements Collector {


    private static Logger logger =
            LogDomains.getLogger(LogDomains.ADMIN_LOGGER);

    /* AdminCommand for properties */
    private static final String HARD_DISK_INFO_CMD = "df -k | grep /dev | grep " +
            "-v /dev/fd | awk '{print $1, $2, $6}'";
    private static final String MEMORY_INFO_CMD = "/usr/sbin/prtconf | grep" +
            " 'Memory size'";
    private static final String NETWORK_SETTINGS_CMD = "ifconfig -a | egrep" +
            " '^hme|^qfe' | awk '{print $1, $2, $6}'";
    private static final String TCP_SETTINGS_CMD = "ndd /dev/tcp " +
            "tcp_time_wait_interval";
    private static final String IP_ADDRESS_INFO_CMD = "netstat -in |" +
            " /usr/xpg4/bin/grep -Ev 'Name|lo0' | awk '{print $4}'";
    private static final String OS_LEVEL_PATCH_INFO_CMD = "showrev -p";
    private static final String HOST_NAME_CMD = "hostname";
    private static final String DOMAIN_NAME_CMD = "domainname";
    private static final String SOFT_FILE_DESC_LIMIT_CMD = "ulimit -n";
    private static final String HARD_FILE_DESC_LIMIT_CMD = "ulimit -Hn";
    private static final String PROCESSOR_INFO_CMD = "psrinfo -v | grep " +
            "'processor operates at'";
    private static final String SWAP_INFO_CMD = "swap -s";
    private String destFolder = null;
    

    public SolarisSystemInfoCollector(String destFolder){
        this.destFolder = destFolder;
    }

    /**
     * To capture the system information for solaris OS
     * @return  Data representing System Information
     * @return  Data representing System Information
     */
    public Data capture(){

        FileData data = null;

        String outputFileName = destFolder + File.separator + Defaults.SYSTEM_INFO_FILE;

        final String ALL_CMDS =
                "( " +
                " echo 'HOST NAME' ; "+ HOST_NAME_CMD +
                " ; echo 'DOMAIN NAME' ; "+DOMAIN_NAME_CMD +
                " ; echo 'HARD DISK INFO ' ; "+HARD_DISK_INFO_CMD +
                " ; echo 'NETWORK CONFIGURATION ' ; "+NETWORK_SETTINGS_CMD +
                " ; echo 'IP ADDRESS ' ; "+ IP_ADDRESS_INFO_CMD +
                " ; echo 'OS LEVEL PATCH INFO' ; "+OS_LEVEL_PATCH_INFO_CMD +
                " ; echo 'SOFT FILE DESCRIPTOR LIMIT ' ; "+SOFT_FILE_DESC_LIMIT_CMD +
                " ; echo 'HARD FILE DESCRIPTOR LIMIT ' ; "+HARD_FILE_DESC_LIMIT_CMD +
                " ; echo 'PROCESSOR INFO' ; "+PROCESSOR_INFO_CMD +
                " ; echo 'SWAP SPACE' ; "+SWAP_INFO_CMD +
                " ; echo 'MEMORY INFO ' ; " + MEMORY_INFO_CMD +
                "  ) >> "+ outputFileName ;

        String[] cmd = {"sh", "-c", ALL_CMDS};

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
