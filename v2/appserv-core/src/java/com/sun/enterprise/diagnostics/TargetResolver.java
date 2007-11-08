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
import com.sun.logging.LogDomains;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.util.Iterator;
/**
 *
 * @author mu125243
 */
public abstract class TargetResolver {
    
    protected String target;
    protected String repositoryName;
    protected String repositoryDir;
    protected boolean local;
    protected TargetType type;
    protected List<String> instances = new ArrayList(1);
    protected ReportTarget reportTarget;
    protected List<ServiceConfig> serviceConfigs = new ArrayList(1);
    protected ExecutionContext context;
    
    protected static final String DEFAULT_FEATURES_PROPERTY_CLASS =
            "com.sun.enterprise.admin.pluggable.PEClientPluggableFeatureImpl";
    
    protected Logger logger =
            LogDomains.getLogger(LogDomains.ADMIN_LOGGER);
    /** Creates a new instance of TargetResolver */
    public TargetResolver(String target, String repositoryDir, boolean local) {
        this.target = target;
        this.repositoryDir = repositoryDir;
        this.local = local;
    }
    
    public ReportTarget resolve() throws DiagnosticException {
        if(validateTarget()) {
            createTarget();
            return reportTarget;
        }
        throw new InvalidTargetException(" Unable to resolve target : " + target);
    }
    
    
    public abstract boolean validateTarget() throws DiagnosticException;

    
    public ExecutionContext getExecutionContext() {
        return context;
    }
    
    
     public List<ServiceConfig> getServiceConfigs() {
        return serviceConfigs;
     }
     
 
   
    protected void createTarget() throws DiagnosticException {
        setExecutionContext();
        initLogger();
        determineTargetDetails();
        createTargetObject();
    }
    
    protected abstract void setExecutionContext();
    
    protected void determineTargetDetails() {
        //determineTargetDir();
        determineTargetType();
        determineRepositoryDetails();
        determineInstances();
        determineServiceConfigs();
        logger.log(Level.FINE,"diagnostic-service.target_details",
                new Object[] {target, repositoryDir, type, instances});
    }
    
    protected  abstract void determineRepositoryDetails() ;
    protected abstract void determineTargetType() ;
    protected abstract void determineInstances();
    
    protected void setTargetType(TargetType type) {
        this.type = type;
    }

    protected void addInstance(String instanceName) {
        instances.add(instanceName);
    }
    
    protected void determineServiceConfigs() {
        if(instances != null) {
            Iterator<String> iterator = instances.iterator();
            while(iterator.hasNext()) {
                addServiceConfig(iterator.next());
            }
        }
    }
    
    protected void  addServiceConfig(String instanceName) {
        String instanceRepDir = repositoryDir + File.separator +
                            repositoryName ;

        if ((!(instanceName.equals(Constants.SERVER))) &&
                (!(type.equals(TargetType.INSTANCE)))){
            // for example in case of node agent this value would be
            //nodeagents/na1/instance1
            instanceRepDir = instanceRepDir + File.separator + instanceName;
        }
                    
                
        ServiceConfig instanceConfig = ServiceConfigFactory.getInstance().
                        getServiceConfig(context.isLocal(), instanceRepDir, 
                        instanceName);
        if(instanceConfig != null)
            serviceConfigs.add(instanceConfig);
    }
    
    
    protected void createTargetObject() {
        reportTarget = new ReportTarget(repositoryDir, repositoryName, target,
        type, instances, local);
    }
    
    private void initLogger() {
        logger = context.getLogger();
    }

    private Logger getLogger() {
        return logger;
    }
 
}
