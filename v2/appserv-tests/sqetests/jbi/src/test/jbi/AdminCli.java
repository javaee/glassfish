/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://open-esb.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://open-esb.dev.java.net/public/CDDLv1.0.html.
 * If applicable add the following below this CDDL HEADER,
 * with the fields enclosed by brackets "[]" replaced with
 * your own identifying information: Portions Copyright
 * [year] [name of copyright owner]
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
