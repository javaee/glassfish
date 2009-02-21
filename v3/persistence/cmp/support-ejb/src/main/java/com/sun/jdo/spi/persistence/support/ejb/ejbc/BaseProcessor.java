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

package com.sun.jdo.spi.persistence.support.ejb.ejbc;

import com.sun.enterprise.deployment.Application;
//import com.sun.enterprise.deployment.backend.DeploymentEventInfo;
//import com.sun.enterprise.deployment.backend.DeploymentRequest;
//import com.sun.enterprise.deployment.backend.DeploymentStatus;
//import com.sun.enterprise.deployment.backend.IASDeploymentException;

import com.sun.jdo.spi.persistence.utility.I18NHelper;
import com.sun.jdo.spi.persistence.utility.database.DatabaseConstants;
import com.sun.jdo.spi.persistence.utility.logging.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 *
 * @author pramodg
 */
abstract public class BaseProcessor  
        implements DatabaseConstants {

    /** The logger */
    protected  static final Logger logger = LogHelperEJBCompiler.getLogger();
    
    /** I18N message handler */
    protected  final static ResourceBundle messages = I18NHelper.loadBundle(
        "com.sun.jdo.spi.persistence.support.ejb.ejbc.Bundle", // NOI18N
        BaseProcessor.class.getClassLoader());

    protected Application application;
//    protected DeploymentEventInfo info;
//    protected DeploymentStatus status;
    
    /**
     * True if this event results in creating new tables.
     */
    protected  boolean create;
    protected  String cliCreateTables;
    protected  String cliDropAndCreateTables;
    protected  String cliDropTables;
    /**
     * Name with which the application is registered.
     */
    protected String appRegisteredName;
    
    protected String appDeployedLocation;
    protected String appGeneratedLocation;
    /**
     * The string name of the create jdbc ddl file.
     */
    protected String createJdbcFileName;
    /**
     * The string name of the drop jdbc ddl file.
     */
    protected String dropJdbcFileName;

    protected  boolean debug = logger.isLoggable(logger.FINE);     
    
    /**
     * Creates a new instance of BaseProcessor.
     * @param info the deployment info object.
     * @param create true if this event results in creating new tables.
     * @param cliCreateTables the cli string related to creating tables
     * at deployment time.
     * @param cliDropAndCreateTables the cli string that indicates that
     * old tables have to be dropped and new tables created.
     * @param cliDropTables the cli string to indicate that the tables
     * have to dropped at undeploy time.
     */
/*
    public BaseProcessor(DeploymentEventInfo info,
            boolean create, String cliCreateTables,
            String cliDropAndCreateTables, String cliDropTables) {
        initializeVariables(info, create, cliCreateTables,
            cliDropAndCreateTables, cliDropTables);
    }

    private void initializeVariables(
            DeploymentEventInfo info, boolean create, String cliCreateTables,
            String cliDropAndCreateTables, String cliDropTables) {
        this.info = info;
        this.application = this.info.getApplicationDescriptor();
        this.appRegisteredName = this.application.getRegistrationName();
        this.status = 
                this.info.getDeploymentRequest().getCurrentDeploymentStatus();
        
        this.create = create;
        this.cliCreateTables = cliCreateTables;
        this.cliDropAndCreateTables = cliDropAndCreateTables;
        this.cliDropTables = cliDropTables;  
    }
  */
    abstract protected void processApplication();

    
       /**
        * Read the ddl file from the disk location.
        * @param fileName the string name of the file.
        * @param create true if this event results in creating tables.
        * @return the jdbc ddl file.
        */
    protected  File getDDLFile(String fileName, boolean create) {
        File file = null;        
        try {
            file = new File(fileName);   
            if (debug) {
                logger.fine(
                    ((create)? "ejb.BaseProcessor.createfilename" //NOI18N
                    : "ejb.BaseProcessor.dropfilename"), //NOI18N
                    file.getName());
            }
        } catch (Exception e) {
            logI18NWarnMessage(
                 "Exception caught in BaseProcessor.getDDLFile()", 
                appRegisteredName, null, e);
        }
        return file;        
    }
    
    /**
     * Open a DDL file and execute each line as a SQL statement.
     * @throw IOException if there is a problem with reading the file.
     * @param f the File object to use.
     * @param sql the Statement to use for execution.
     * @throws java.io.IOException if there is a problem with reading the file.
     */
    protected  void executeDDLs(File f, Statement sql)
            throws IOException {

        BufferedReader reader = null;
        StringBuffer warningBuf = new StringBuffer();

        try {
            reader = new BufferedReader(new FileReader(f));
            String s;
            while ((s = reader.readLine()) != null) {
                try {
                    if (debug) {
                        logger.fine("ejb.BaseProcessor.executestatement", s); //NOI18N
                    }
                    sql.execute(s);

                } catch(SQLException ex) {
                    String msg = getI18NMessage("ejb.BaseProcessor.sqlexception", 
                            s, null, ex);
                    logger.warning(msg);
                    warningBuf.append("\n\t").append(msg); // NOI18N
                }
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch(IOException ex) {
                    // Ignore.
                }
            }
            if (warningBuf.length() > 0) {
                String warning = 
                        getI18NMessage("ejb.BaseProcessor.tablewarning");
                warnUser(warning + warningBuf.toString());
            }
        }
    }

    /**
     * Provide a warning message to the user.  The message is appended to any
     * already-existing warning message text.
     * @param status DeploymentStatus via which user is warned
     * @param msg Message for user.
     */

    protected  void warnUser(String msg) {
/*
        status.setStageStatus(DeploymentStatus.WARNING);
        status.setStageStatusMessage(
                status.getStageStatusMessage() + "\n" + msg); // NOI18N
  */
    }

    /**
     * Provide a warning message to the user about inability to connect to the
     * database.  The message is created from the cmpResource's JNDI name and
     * the exception.
     * @param status DeploymentStatus via which user is warned
     * @param cmpResource For obtaining JNDI name
     * @param ex Exception which is cause for inability to connect.
     */
    protected void cannotConnect(String connName, 
            Throwable ex) {
        logI18NWarnMessage( "ejb.BaseProcessor.cannotConnect",  
                connName,  null, ex);
    }
    
    protected void fileIOError(String regName, 
            Throwable ex) {
        logI18NWarnMessage("ejb.BaseProcessor.ioexception",  
                regName,  null, ex);
    }
    
    /**
     * Close the connection that was opened
     * to the database
     * @param conn the database connection.
     */
    protected static void closeConn(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch(SQLException ex) {
                // Ignore.
            }
        }
    }
    
    protected void logI18NInfoMessage(
            String errorCode, String regName, 
            String fileName, Throwable ex) {
        String msg = getI18NMessage(errorCode, 
                regName, fileName, ex);
        logger.info(msg);
    }
    
    protected void logI18NWarnMessage(
            String errorCode, String regName, 
            String fileName, Throwable ex) {
        String msg = getI18NMessage(errorCode, 
                regName, fileName, ex);
        logger.warning(msg);
        warnUser(msg);        
    }
    
    /**
     * 
     * @param errorCode 
     * @return 
     */
    protected String getI18NMessage(String errorCode) {
        return getI18NMessage(
             errorCode, null, null, null);
    }    

    protected String getI18NMessage(
            String errorCode, String regName, 
            String fileName, Throwable ex) {
        String msg = null;
        if(null != ex)
               msg = I18NHelper.getMessage(
                    messages, errorCode,  regName,  ex.toString());
        else if(null != fileName )
            msg = I18NHelper.getMessage(
                    messages, errorCode,  regName,  fileName); 
        else            
             msg = I18NHelper.getMessage(messages, errorCode);
        
        return msg;
    }


    /**
     * The location where the application has been deployed. This would ideally 
     * be the domains/domain1/applications directory. This information is obtained
     * from the DeploymentEventListener object that is passed in.
     */
    protected void setApplicationLocation() {
        if(null != this.appDeployedLocation)
            return;
        
//        this.appDeployedLocation =
//            info.getDeploymentRequest().getDeployedDirectory().getAbsolutePath()
//            + File.separator;
    }
    
    /**
     * The location where files have been generated as part of the application 
     * deployment cycle. This is where we write out the sql/jdbc files used to
     * create or drop objects from the database. This information is obtained
     * from the DeploymentEventListener object that is passed in.
     */
    protected void setGeneratedLocation() {
        if(null != this.appGeneratedLocation)
            return;
//        this.appGeneratedLocation =
//                info.getScratchDir("ejb").getAbsolutePath() + File.separator;
    }
} 
