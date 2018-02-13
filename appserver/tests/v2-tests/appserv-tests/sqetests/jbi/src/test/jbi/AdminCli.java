/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
 * @(#)AppserverUtilities.java - ver 1.1 - 01/04/2006
 *
 * Copyright 2004-2006 Sun Microsystems, Inc. All Rights Reserved.
 */

package test.jbi;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;

/**
 * This class is used to represent the admin cli. asadmin
 */
public class AdminCli {
    
    /**
     * The following members are used to store the 
     * various options needed by admincli
     */
    
    private String adminUser;
    private String adminPasswordFile;
    private String adminHost;
    private String adminPort;
    private String isSecure;
    
    private String masterPassword = null;
    private String additionalOptions = null;
    private String operand = null;
    private String command =  null;
    
    private String asadminCommandPath = null;
    
    private File pwdFile = null;
    private String space = " ";
    
    
    AdminCli(
            String adminUser, 
            String adminPasswordFile,
            String adminHost, 
            String adminPort,
            String isSecure) 
    {
        this.adminUser = adminUser;
        this.adminPasswordFile = adminPasswordFile;
        this.adminHost = adminHost;
        this.adminPort = adminPort;
        this.isSecure = isSecure;

        if (System.getProperty("os.name").indexOf("Windows") != -1) {
            asadminCommandPath = 
                    System.getProperty("jbi.appserver.install.dir")
                    + File.separator + "bin" + File.separator + "asadmin.bat";
        } else {
            asadminCommandPath = 
                    System.getProperty("jbi.appserver.install.dir")
                    + File.separator + "bin" + File.separator + "asadmin";
            
        }
        
        
    }
   
    
    /**
     * getter for admin user
     */
    public String getAdminUser(){
        return adminUser;
    }
    
    
    /**
     * setter for user
     */
    public void setAdminUser(String adminUser) {
        this.adminUser = adminUser;
    }
    
    /**
     * getter for admin passwordFile
     */
    public String getAdminPasswordFile(){
        return adminPasswordFile;
    }
    
    
    /**
     * setter for adminPaswordFile
     */
    public void setAdminPassword(String adminPasswordFile) {
        this.adminPasswordFile = adminPasswordFile;
    }
    
    /**
     * getter for master password
     */
    public String getMasterPassword(){
        return masterPassword;
    }
    
    
    /**
     * setter for MasterPasword
     */
    public void setMasterPassword(String masterPassword) {
        this.masterPassword = masterPassword;
    }
        
    /**
     * getter for admin host
     */
    public String getAdminHost(){
        return adminHost;
    }
    
    
    /**
     * setter for admin host
     */
    public void setAdminHost(String adminHost) {
        this.adminHost = adminHost;
    }
    
    /**
     * getter for admin port
     */
    public String getAdminPort(){
        return adminPort;
    }
    
    /**
     * setter for adminPort
     */
    public void setAdminPort(String adminPort) {
        this.adminPort = adminPort;
    }
    
   /**
    * setter for command
    */
    public void setCommand(String command) {
        this.command = command;
    }
    
    /**
     * getter for command
     */
    public String getCommand() {
        return command;
    }
    
    
    /**
     *setter for additional options
     */
     public void setAdditionalOptions(String additionalOptions){
        this.additionalOptions = additionalOptions;
    }
     
    /**
     * getter for additonalOptions
     */
     public String getAdditionalOptions(){
         return additionalOptions;
     }
   
    /**
     * setter for operand
     */ 
    public void setOperand(String operand){
        this.operand = operand;

    }
    
    /**
     * getter for operand
     */
    public String getOperand(){
        return operand;
    }
    
    
    /**
     * this method is used to construct the string
     * that has the standard options
     */
    public String getOptions() {
        String additionalOptions = " ";
        if (getAdditionalOptions() != null) {
            additionalOptions = getAdditionalOptions();
        }
        return 
            "--host" + space + getAdminHost() + space +
            "--port" + space + getAdminPort() + space +
            "--user" + space + getAdminUser() + space +
            "--passwordfile" + space + getAdminPasswordFile()
            + space +  additionalOptions + space;
    }
    
    /**
     * This method is used to execute a command
     */ 
    public String  execute() {
       
        String command = 
            asadminCommandPath + space + 
            getCommand() + space +
            getOptions() + space +
            getOperand();

            
        StringBuffer resultbuf = new StringBuffer();
        StringBuffer error = new StringBuffer();
        int exitValue =  new ProcessExecutor().execute(
                                command, 
                                resultbuf, 
                                error);
        
        String result = resultbuf.toString();
        if(pwdFile != null) {
          pwdFile.delete();
        }
        return result;

    }
}
