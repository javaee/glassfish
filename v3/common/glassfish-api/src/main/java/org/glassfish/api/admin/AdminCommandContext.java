/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.glassfish.api.admin;


import java.io.File;
import java.util.Collections;
import java.util.List;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ExecutionContext;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Useful services for Deployer service implementation
 *
 * @author Jerome Dochez
 */
public class AdminCommandContext implements ExecutionContext {
    
    public  ActionReport report;
    public final Logger logger;
    private List<File> uploadedFiles;
    
    public AdminCommandContext(Logger logger, ActionReport report) {
        this(logger, report, null);
    }
    
    public AdminCommandContext(Logger logger, ActionReport report,
            List<File> uploadedFiles) {
        this.logger = logger;
        this.report = report;
        this.uploadedFiles = (uploadedFiles == null) ? emptyFileList() : uploadedFiles;
    }
    
    private static List<File> emptyFileList() {
        return Collections.emptyList();
    }
    /**
     * Returns the Reporter for this action
     * @return ActionReport implementation suitable for the client
     */
    public ActionReport getActionReport() {
        return report;
    }
    /**
     * Change the Reporter for this action
     * @param newReport The ActionReport to set.
     */
    public void setActionReport(ActionReport newReport) {
        report = newReport;
    }

    /**
     * Returns the Logger
     * @return the logger
     */
    public Logger getLogger() {
        return logger;
    }
    
    /**
     * Returns the uploaded files
     * @return the uploaded files
     */
    public List<File> getUploadedFiles() {
        return uploadedFiles;
    }
}
