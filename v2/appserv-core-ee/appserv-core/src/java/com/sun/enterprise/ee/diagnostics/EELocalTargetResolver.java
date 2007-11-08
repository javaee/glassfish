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

package com.sun.enterprise.ee.diagnostics;
import com.sun.enterprise.diagnostics.Defaults;
import com.sun.enterprise.diagnostics.DiagnosticException;
import com.sun.enterprise.diagnostics.PELocalTargetResolver;
import com.sun.enterprise.diagnostics.TargetType;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;


/**
 *
 * @author mu125243
 */
public class EELocalTargetResolver extends PELocalTargetResolver {
    
    /** Creates a new instance of EELocalTargetResolver */
    public EELocalTargetResolver(String target, String repositoryDir, 
            List<String> instances, TargetType type) {
        super(target, repositoryDir, true);
        this.type = type;
        if(instances != null)
            this.instances = instances;
        
    }

    
    public boolean validateTarget() throws DiagnosticException {
        determineTargetType(repositoryDir, target);
        if(type != null)    
            return true;
        return false;
    }

    
   
     protected void setExecutionContext() {
         context =EEExecutionContext.LOCAL_EC;
     }
     
     /**
     * Combination of targetDir and target in local mode is expected 
     * to be appserver's nodeagent's or server instance's directory structure
     * @param targetDir targetDir in local mode
     * @param target target name for which report generation is initiated
     * @return true if directory structure seems inline with nodeagent / instance
     * directory structure
     * @throw DiagnosticException if targetDir and target are null
     */
    protected void determineTargetType(String targetDir, String target) 
    throws DiagnosticException {
        if(type == null) {
            if(targetDir != null && target != null){
                String absoluteDir = targetDir + File.separator + target;
                String agentDir = absoluteDir + EEConstants.AGENT_DIR;
                File agentDirObj = new File(agentDir);
                // Node Agent 
                if(agentDirObj.exists()) {
                    //determineServiceConfigs();
                    setTargetType(EETargetType.NODEAGENT);
                }
                else {
                    //Determine if it is a instance on a node agent
                    String parentDir = targetDir + File.separator + 
                            EEConstants.AGENT_DIR;
                    File parentDirObj = new File(parentDir);
                    if(parentDirObj.exists()) {
                        //instances = Arrays.asList(new String[]{target});
                        setTargetType(EETargetType.INSTANCE);
                    }
                    else {
                        //instances.add(TargetType.DAS.getType());
                        if(super.validateTarget())
                            setTargetType(TargetType.DAS);
                    }
                }
                return;
            } // if
            throw new DiagnosticException("Targetdir and targetname are null");
        }
    }
    
    protected void determineRepositoryDetails() {
        if(type.equals(EETargetType.INSTANCE)) {
            if(repositoryDir.endsWith(File.separator)) {
                repositoryDir = repositoryDir.substring(0,  (repositoryDir.length() -1));
            }
            super.determineRepositoryDetails();
        } else
            super.determineRepositoryDetails();
    }
    protected void determineInstances() {
        if(type.equals(EETargetType.NODEAGENT)) {
            String agentDir = repositoryDir + File.separator + repositoryName;

            File agentDirObj = new File(agentDir);

            instances =  Arrays.asList(agentDirObj.list(new FilenameFilter() {
                public boolean accept (File aDir, String fileName) {
                    if (aDir != null && fileName != null) {
                        if (fileName.matches(EEConstants.AGENT) || 
                                fileName.matches (Defaults.DIAGNOSTIC_REPORT) ||
                                fileName.matches (Defaults.TEMP_REPORT)) {
                            return false;
                        }
                        return true;
                    }
                    return false;
                }
            }));
        } else if(type.equals(EETargetType.INSTANCE)) {
            addInstance(target);
        } else if(type.equals(EETargetType.DAS)) {
            super.determineInstances();
        }
    }
}
