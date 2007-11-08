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
import com.sun.enterprise.diagnostics.DiagnosticException;
import com.sun.enterprise.diagnostics.ServiceConfig;
import com.sun.enterprise.diagnostics.CLIOptions;
import com.sun.enterprise.diagnostics.Constants;
import com.sun.enterprise.diagnostics.Data;
import com.sun.enterprise.diagnostics.util.FileUtils;
import com.sun.enterprise.diagnostics.util.DDFilter;

import java.io.IOException;
import java.io.File;
import java.io.FilenameFilter;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * Responsible for collecting applications deployment descriptors and generated
 * files. 
 * @author Manisha Umbarje
 */

public class AppInfoCollector implements Collector {
    
    private String destFolder;
    private String repositoryFolder;
    private ServiceConfig config;
    private static Logger logger = 
    LogDomains.getLogger(LogDomains.ADMIN_LOGGER);
    /**
     * Creates new instance of AppInfoCollector
     * @param repositoryFolder central/cache repository root
     * @param destFolder destination folder in which files are collected
     */
    public AppInfoCollector(String repositoryFolder, String destFolder) {
        this.destFolder = destFolder;
        this.repositoryFolder = repositoryFolder;
        this.config = config;
    }
   
    /**
     * Captures applications deployment descriptors and generated files
     * @throws DiagnosticException
     */
    public Data capture() throws DiagnosticException {
        WritableDataImpl dataImpl = new WritableDataImpl(DataType.APPL_INFO);
        dataImpl.addChild(captureAppRelatedInfo(Constants.GENERATED_DIR));
        dataImpl.addChild(
                captureAppRelatedInfo(Constants.APPLICATIONS_DIR,
                new DDFilter()));
        return dataImpl;
    }//capture

    /**
     * Captures files
     * @param relativePath directory to be copied relative to central/cache
     * repository
     * @throw DiagnosticException
     */
    private Data captureAppRelatedInfo(String relativePath) 
    throws DiagnosticException {
        try {
            String sourceFolder = repositoryFolder + File.separator + relativePath;
            String destFileObj = destFolder + File.separator + relativePath;       
            FileUtils.copyDir(sourceFolder, destFileObj, true);
            return new FileData(destFileObj, DataType.APPL_INFO);
        } catch(IOException ioe) {
            logger.log(Level.WARNING, "diagnostic-service.copy_failed" ,
                    new Object[]{relativePath, ioe.getMessage()});
        }
        return null;
    }
    
    /**
     * Capture filtered files
     * @param relativePath  directory to be copied relative to central/cache
     * repository
     * @param filter file name filter
     */
    private Data captureAppRelatedInfo(String relativePath, 
            FilenameFilter filter) throws DiagnosticException {
        WritableDataImpl dataImpl = new WritableDataImpl(relativePath);
        String sourceFolderName = repositoryFolder + File.separator + relativePath;
   
        File sourceFolder = new File(sourceFolderName);
        String[] filteredChildren = null;
        String[] children = null;

        // Assumes that there is no further sub directory , if children
        // satisfying the filter are found
        if (filter != null) {
            filteredChildren = sourceFolder.list(filter);
         }
         // Visit subfolders to search files with matching the filter
        
        if (filteredChildren != null) {
            if(filteredChildren.length ==  0) {
                children = sourceFolder.list();
                for (int i = 0 ; i < children.length; i++) {
                    String childName =  relativePath + File.separator + children[i];
                    String absoluteChildName = repositoryFolder  + File.separator + childName;
                    File child = new File(absoluteChildName);
                    if (child.isDirectory())
                        dataImpl.addChild(captureAppRelatedInfo(childName, filter));
                }
            } else {
                for (int i = 0 ; i < filteredChildren.length; i++) {
                    String childName =  relativePath + File.separator +
                            filteredChildren[i];
                    String absoluteChildName = repositoryFolder  +
                            File.separator + childName;
                    File child = new File(absoluteChildName);
                    try {
                        String dest = destFolder + File.separator + childName;
                        FileUtils.copyFile(absoluteChildName, dest);
                        dataImpl.addChild(new FileData(dest, DataType.APPL_INFO));
                    }catch(IOException ioe) {
                        logger.log(Level.WARNING, "diagnostic-service.copy_failed" ,
                            new Object[]{absoluteChildName, ioe.getMessage()});
                    }
                }// for
            }//else
        }//if
        return dataImpl;
    }//createFileDataObjects
}
