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
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to collect System Information for Linux OS
 *
 */

public class LinuxSystemInfoCollector implements Collector
{
    private static Logger logger =
            LogDomains.getLogger(LogDomains.ADMIN_LOGGER);

    /* AdminCommand for properties */
    private static final String SWAP_SPACE_CMD = "/proc/meminfo";
    private static final String TCP_SETTINGS_CMD =
            "/proc/sys/net/ipv4/tcp_keepalive_time";
    private static final String MEMORY_INFO_CMD = "/proc/meminfo";
    private static final String PROCESSOR_INFO_CMD = "/proc/cpuinfo";
    private static final String HARD_DISK_INFO_CMD =
            "df -k | grep /dev | grep -v /dev/fd | awk '{print $1, $2, $6}'";
    private static final String NETWORK_SETTINGS_CMD =
            "/sbin/ifconfig | grep MTU";
    private static final String IP_ADDRESS_INFO_CMD =
            "/sbin/ifconfig | grep inet";
    private static final String OS_LEVEL_PATCH_INFO_CMD = "rpm -qai";

    private static final String HOST_NAME_CMD = "hostname";
    private static final String DOMAIN_NAME_CMD = "domainname";
    private static final String SOFT_FILE_DESC_LIMIT_CMD = "ulimit -n";
    private static final String HARD_FILE_DESC_LIMIT_CMD = "ulimit -Hn";
    private String destFolder = null;


    public LinuxSystemInfoCollector(String destFolder){
        this.destFolder = destFolder;
    }


    /**
     * captures the System Information for Linux OS
     * @return Data
     */
    public Data capture(){

        FileData data = null;
        String outputFileName = destFolder + File.separator + Defaults.SYSTEM_INFO_FILE;


        final String ALL_CMDS = "( echo HOSTNAME  ; " + HOST_NAME_CMD + " " +
                ";echo DOMAINNAME   ; "+ DOMAIN_NAME_CMD +
                ";echo 'HARD DISK INFO'  ; " + HARD_DISK_INFO_CMD +
                ";echo 'NETWORK SETTINGS '  ; " + NETWORK_SETTINGS_CMD +
                ";echo 'IP ADDRESS INFO'  ; " + IP_ADDRESS_INFO_CMD +
                ";echo 'OS LEVEL PATCH'  ; " + OS_LEVEL_PATCH_INFO_CMD +
                ";echo 'SOFT FILE DESCRIPTOR LIMIT';"+ SOFT_FILE_DESC_LIMIT_CMD +
                ";echo 'HARD FILE DESCRIPTOR LIMIT';"+ HARD_FILE_DESC_LIMIT_CMD +
                ") >>  " + outputFileName ;

        String[] cmd = {"sh", "-c", ALL_CMDS};

        ProcessExecutor executor = new ProcessExecutor(cmd, 0);
        try{
        executor.execute();

        File outputFile = new File(outputFileName);

        FileWriter writer = new FileWriter(outputFile, true);

            String swapSpaceInfo = getSwapSpaceInfo();

            writer.write("SWAP SPACE\n");
            writer.write(swapSpaceInfo + "\n");

            String processorInfo = getProcessorInfo();

            writer.write("PROCESSOR INFO\n");
            writer.write(processorInfo + "\n");

            String memoryInfo = getMemoryInfo();

            writer.write("MEMORY INFO\n");
            writer.write(memoryInfo + "\n");

            writer.close();

            data = new FileData(outputFile.getName(),DataType.SYSTEM_INFO);

        }
        catch(ProcessExecutorException pee){
            logger.log(Level.WARNING, "Exception while capturing system info" +
                     " : " + pee.getMessage());
        }
        catch(FileNotFoundException fnfe){
            logger.log(Level.WARNING, "Exception while capturing system info" +
                     " : " + fnfe.getMessage());
        }
        catch(IOException ioe){
            logger.log(Level.WARNING, "Exception while capturing system info" +
                     " : " + ioe.getMessage());
        }
        return data;
    }

    /**
     * to get the Swap Space Information
     * @return String representing swap space information
     */
    public String getSwapSpaceInfo() {
        String result = null;

        try {
            File file = new File(SWAP_SPACE_CMD); //file holding swap details
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line = null; //represents one line in the file
            String total = null; //total swap space
            String used = null;  //used space
            String free = null;  //free space

            while (true) {
                line = reader.readLine();
                if (line != null && line.indexOf("Swap:") >= 0) {
                    StringTokenizer tokenizer = new StringTokenizer(line, " ");

                    if (tokenizer.countTokens() >= 4) // Tokenize and get
                                                      // individual strings
                    {
                        tokenizer.nextElement();
                        total = (String) tokenizer.nextElement();
                        used = (String) tokenizer.nextElement();
                        free = (String) tokenizer.nextElement();

                        result = "Total : " + total + " , " + " Used : " + used +
                                " , " + "Free : " + free;
                    }
                    break;
                } else if (line == null) // exit when eof is reached
                {
                    break;
                }
            }
            reader.close();
        }
        catch (IOException ioe) {

           logger.log(Level.WARNING, "Exception while retrieving Swap Space Info" +
                    " : " + ioe.getMessage());
        }
        return result;
    }

    /**
     * To get Processor Information
     * @return String representing processor information
     */
    public String getProcessorInfo() {
        String result = null;
        try {
            File file = new File(PROCESSOR_INFO_CMD); //file holding CPU details
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line = null; //to read a line from the file

            while (true) {
                line = reader.readLine();
                if (line != null && line.indexOf("model name") >= 0) {
                    int index = line.indexOf(":");
                    if (index >= 0) {
                        result = line.substring(index + 1);
                    }
                    break;
                } else if (line == null) // exit when eof is reached
                {
                    break;
                }
            }
            reader.close();
        }
        catch (IOException ioe) {

            logger.log(Level.WARNING,"Exception while retrieving Processor Info" +
                    " : " + ioe.getMessage());
        }
        return result;
    }

    /**
     * To get memory information
     * @return String representing memory information
     */
    public String getMemoryInfo() {
        String result = null;
        try {
            File file = new File(MEMORY_INFO_CMD); //file holding swap details
            BufferedReader reader = new BufferedReader(new FileReader(file));


            String line = null; //to read a line from the file
            String total = null; //total swap space
            String used = null;  //used space
            String free = null;  //free space

            while (true) {
                line = reader.readLine();
                if (line != null && line.indexOf("Mem:") >= 0) {
                    StringTokenizer tokenizer = new StringTokenizer(line, " ");

                    if (tokenizer.countTokens() >= 4)
                    // Tokenize and get individual strings
                    {
                        tokenizer.nextElement();
                        total = (String) tokenizer.nextElement();
                        used = (String) tokenizer.nextElement();
                        free = (String) tokenizer.nextElement();

                        result = "Total : " + total + "\nUsed : " + used +
                                "\nFree : " + free;
                    }
                    break;

                } else if (line == null) // exit when eof is reached
                {
                    break;
                }
            }
            reader.close();
        }
        catch (IOException ioe) {

            logger.log(Level.WARNING, "Exception while retrieving Memory " +
                    "Info : " + ioe.getMessage());
        }
        return result;
    }
}
