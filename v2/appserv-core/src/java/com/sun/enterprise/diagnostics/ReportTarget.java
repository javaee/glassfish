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
package com.sun.enterprise.diagnostics;

import com.sun.enterprise.server.ApplicationServer;
import com.sun.logging.LogDomains;
import java.util.logging.Logger;
import java.util.List;
import java.io.File;
/**
 *
 * @author mu125243
 */
public class ReportTarget {
    protected String targetDir;
    protected String targetName;
    protected String repositoryName;
    protected String repositoryDir;
    protected TargetType type;
    protected List<String> instances;
    private boolean local ;
    private static Logger logger = 
    LogDomains.getLogger(LogDomains.ADMIN_LOGGER);
    
    /** Creates a new instance of ReportServiceConfig */
    public ReportTarget(String repositoryDir, String repositoryName, String targetName, 
            TargetType type, List<String> instances, boolean local) {
        this.repositoryDir = repositoryDir;
        this.repositoryName = repositoryName;
        this.targetDir = repositoryDir + File.separator + repositoryName;
        this.targetName = targetName;
        this.type = type;
        this.instances = instances;
        this.local = local;
      
    }
    /**
     * Retrieves the type of this target
     * @return target type
     */
    public TargetType getType() {
        return type;
    }
    
    /**
     * Returns target name
     * @return target name
     */
    public String getName() {
        return targetName;
    }
    
      
    /**
     * @return  name of the central/cache repository
     */
    public String getRepositoryDir() {
          return repositoryDir;
    }
    
    public String getTargetDir() {
        return targetDir;
    }

    public String getRepositoryName() {
        return repositoryName;
    }
    /**
     * @return name of the temp folder in which data is collected before 
     * compressing it
     */
    public String getIntermediateReportDir() {
        return  getArchiveDir() + File.separator + targetName; 
    }

    public String getArchiveDir(){
        return repositoryDir + File.separator +
                repositoryName + Defaults.TEMP_REPORT_FOLDER;
    }

    /**
     * @return default directory in which diagnostic report is stored
     */
    public String getDefaultReportDir() {
        return targetDir + Defaults.REPORT_FOLDER;
    }   
    
    public List<String> getInstances() {
        return instances;
    }
    public String toString() {
        return getName() +"," + getType() + "," + getRepositoryDir() + "," +
                getIntermediateReportDir() + "," + getDefaultReportDir() + 
                "," + getInstances();
    }
}
