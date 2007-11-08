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

import java.io.File;
import java.util.logging.Level;

/**
 *
 * @author mu125243
 */
public class PELocalTargetResolver extends TargetResolver {
    
    /** Creates a new instance of PELocalTargetResolver */
    public PELocalTargetResolver(String target, String repositoryDir, boolean local) {
        super(target,repositoryDir,local);
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
    public boolean validateTarget() throws DiagnosticException {
        if(repositoryDir != null && target != null){
            logger.log(Level.FINEST, "validate_local_target" , new String[]{target, repositoryDir});
            return isAInstance(repositoryDir,target);
        } // if
        throw new DiagnosticException("Targetdir and targetname are null");
    }
    
    private boolean isAInstance(String repositoryDir, String target) {
        if(repositoryDir != null && target != null){
            String absoluteDir = repositoryDir + File.separator + target;
            File applicationsDirObj = new File(absoluteDir +
                    Constants.APPLICATIONS_DIR);
            File generatedDirObj = new File(absoluteDir +
                    Constants.GENERATED_DIR);
            File configDirObj = new File(absoluteDir +
                    Constants.CONFIG_DIR);
            if(applicationsDirObj.exists() &&
                    generatedDirObj.exists() &&
                    configDirObj.exists())
                return true;
            return false;
        }//if
        return false;
    }
    
    protected  void determineRepositoryDetails() {
        repositoryName = target;
    }

    protected void setExecutionContext() {
        context = ExecutionContext.LOCAL_EC;
    
    }
    
    protected void determineTargetType() {
        if(type == null)
            setTargetType(TargetType.DAS);
    }
    
    protected void determineTargetDir() {
        //do nothing
    }
    
    protected void determineInstances(){
        addInstance(TargetType.DAS.getType());
    }

}
