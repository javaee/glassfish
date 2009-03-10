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

/*
 * SunACCTransfer.java
 *
 * Created on September 8, 2003, 2:08 PM
 */

package com.sun.enterprise.tools.upgrade.miscconfig;

/**
 *
 * @author  prakash
 */
import java.io.*;
import java.util.logging.*;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.tools.upgrade.logging.*;
import com.sun.enterprise.tools.upgrade.common.CommonInfoModel;

public class SunACCTransfer {
    
    private StringManager stringManager = StringManager.getManager(SunACCTransfer.class);
    private Logger logger = CommonInfoModel.getDefaultLogger();
    private String docType = null;
    private String secPolicyProperty = null;
    
    /** 
     * Creates a new instance of SunACCTransfer 
     */
    public SunACCTransfer() {
    }
    
    /**
     * Method to start transformation of sun-acc XML file
     */
    public void transform(String sourceFileName, String targetFileName){
        logger.log(Level.INFO, stringManager.getString(
                "upgrade.configTransfers.sunacc.startMessage"));
        File sourceFile = new File(sourceFileName);
        File targetFile = new File(targetFileName);
        
        //Get the DOCTYPE and security-config of target file
        try {
            docType = this.getDOCTYPEString(targetFile);
            secPolicyProperty = this.getSecurityConfigValue(targetFile);
        } catch(Exception e) {}
        
        //Backup the target file before processing
        boolean renamed = targetFile.renameTo(new File(targetFileName+".bak"));
        if(!renamed){
            // This is possible if user is running the upgrade again 
            //and .bak is already created.
            renamed = targetFile.delete();
        }
        
        //Transfer file contents if backed up
        if(renamed){
            try{
                targetFile = new File(targetFileName);
                targetFile.createNewFile();
                this.transferFileContents(sourceFile, targetFile);
            }catch(Exception ex){
                // Log error message
                logger.log(Level.SEVERE, stringManager.getString(
                        "upgrade.configTransfers.sunacc.startFailureMessage"),ex);
            }  
        }else{
            // Log error message : rename failure
            logger.log(Level.SEVERE, stringManager.getString(
                    "upgrade.configTransfers.sunacc.renameFailureMessage"));
        }
    }
    
    /**
     * Method to get DOCTYPE string from a xml file
     */
    private String getDOCTYPEString(File target) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader
                (new FileInputStream(target)));
        String readLine = null;
        while((readLine = reader.readLine()) != null){
            if(readLine.indexOf("<!DOCTYPE") != -1){
                return readLine;
            }
        }
        return null;
    }

    private String getSecurityConfigValue(File target) throws Exception{
        BufferedReader reader = new BufferedReader(new InputStreamReader
                (new FileInputStream(target)));
        String readLine = null;
        while((readLine = reader.readLine()) != null){
            if(readLine.indexOf("<property") != -1){
                if(readLine.indexOf("security.config") != -1) { 
                    return readLine;
                }
            }
        }
        return null;
    }

    /**
     * Method to transfer contents of sun-acc.xml from source to target
     */
    private void transferFileContents(File source, File target) throws Exception{
        BufferedReader reader = new BufferedReader(new InputStreamReader
                (new FileInputStream(source)));
        PrintWriter writer = new PrintWriter(new FileOutputStream(target));
        String readLine = null;
        while((readLine = reader.readLine()) != null){
            if(readLine.indexOf("<!DOCTYPE") == -1 && 
                    readLine.indexOf("<property") == -1) { 
                writer.println(readLine);
            } else if(readLine.indexOf("<!DOCTYPE") != -1){ 
                if(docType != null)
                    writer.println(docType);
                else
                    writer.println();
            } else if(readLine.indexOf("<property") != -1) {
                if(readLine.indexOf("security.config") != -1) {
                    if(secPolicyProperty != null) 
                        writer.println(secPolicyProperty);
                    else
                        writer.println();
                } else {
                    writer.println(readLine);
                }
            }
        }
        writer.flush();
        writer.close();
        reader.close();
    }
}
