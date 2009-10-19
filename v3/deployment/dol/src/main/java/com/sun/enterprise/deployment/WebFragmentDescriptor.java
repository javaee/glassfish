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
package com.sun.enterprise.deployment;

import java.util.List;
import java.util.Set;

import com.sun.enterprise.deployment.runtime.web.SunWebApp;
import com.sun.enterprise.deployment.web.EnvironmentEntry;
import com.sun.enterprise.deployment.web.LoginConfiguration;
import com.sun.enterprise.deployment.web.SecurityConstraint;
import com.sun.enterprise.deployment.types.EjbReference;

/**
 * I am an object that represents all the deployment information about
 * a web fragment.
 *
 * @author Shing Wai Chan
 */

public class WebFragmentDescriptor extends WebBundleDescriptor
{
    private String jarName = null;
    private OrderingDescriptor ordering = null;

    /**
     * Constrct an empty web app [{0}].
     */
    public WebFragmentDescriptor() {
        super();
    }

    public String getJarName() {
        return jarName;
    }

    public void setJarName(String jarName) {
        this.jarName = jarName;
    }

    public OrderingDescriptor getOrderingDescriptor() {
        return ordering;
    }

    public void setOrderingDescriptor(OrderingDescriptor ordering) {
        this.ordering = ordering;
    }

    @Override
    protected void combineSecurityConstraints(Set<SecurityConstraint> firstScSet,
           Set<SecurityConstraint>secondScSet) {
        firstScSet.addAll(secondScSet);
    }

    @Override
    protected void combineLoginConfiguration(WebBundleDescriptor webBundleDescriptor) {
        if (getLoginConfiguration() == null) {
            setLoginConfiguration(webBundleDescriptor.getLoginConfiguration());
        } else {
            LoginConfiguration lgConf = webBundleDescriptor.getLoginConfiguration();
            if (lgConf != null && (!lgConf.equals(getLoginConfiguration()))) {
                conflictLoginConfig = true;
            }
        }
    }

    @Override
    protected void combineEnvironmentEntries(WebBundleDescriptor webBundleDescriptor) {
        for (EnvironmentEntry env: webBundleDescriptor.getEnvironmentEntrySet()) {
            EnvironmentProperty envProp = _getEnvironmentPropertyByName(env.getName());
            if (envProp != null) {
                webBundleDescriptor.conflictEnvironmentEntry = true;
                unionInjectionTargets(envProp, (EnvironmentProperty)env);
            } else {
                addEnvironmentEntry(env);
            }
        }
    }

    @Override
    protected void combineEjbReferenceDescriptors(WebBundleDescriptor webBundleDescriptor) {
        for (EjbReference ejbRef: webBundleDescriptor.getEjbReferenceDescriptors()) {
            EjbReferenceDescriptor ejbRefDesc =
                    (EjbReferenceDescriptor)_getEjbReference(ejbRef.getName());
            if (ejbRefDesc != null) {
                webBundleDescriptor.conflictEjbReference = true;
                unionInjectionTargets(ejbRefDesc, (EnvironmentProperty)ejbRef);
            } else {
                addEjbReferenceDescriptor(ejbRef);
            }
        }
    }

    @Override
    protected void combineServiceReferenceDescriptors(WebBundleDescriptor webBundleDescriptor) {
        for (ServiceReferenceDescriptor serviceRef: webBundleDescriptor.getServiceReferenceDescriptors()) {
            ServiceReferenceDescriptor sr = _getServiceReferenceByName(serviceRef.getName());
            if (sr != null) {
                webBundleDescriptor.conflictServiceReference = true;
                unionInjectionTargets(sr, serviceRef);
            } else {
                addServiceReferenceDescriptor(serviceRef);
            }
        }
    }

    @Override
    protected void combineResourceReferenceDescriptors(WebBundleDescriptor webBundleDescriptor) {
        for (ResourceReferenceDescriptor resRef : webBundleDescriptor.getResourceReferenceDescriptors()) {
            ResourceReferenceDescriptor rrd = _getResourceReferenceByName(resRef.getName());
            if (rrd != null) {
                webBundleDescriptor.conflictResourceReference = true;
                unionInjectionTargets(rrd, resRef);
            } else {
                addResourceReferenceDescriptor(resRef);
            }
        }
    }

    @Override
    protected void combineJmsDestinationReferenceDescriptors(WebBundleDescriptor webBundleDescriptor) {
        for (JmsDestinationReferenceDescriptor jdRef: getJmsDestinationReferenceDescriptors()) {
            JmsDestinationReferenceDescriptor jdr = _getJmsDestinationReferenceByName(jdRef.getName());
            if (jdr != null) {
                webBundleDescriptor.conflictJmsDestinationReference = true;
                unionInjectionTargets(jdr, jdRef);   
            } else {
                addJmsDestinationReferenceDescriptor(jdRef);
            }
        }
    }

    @Override
    protected void combineMessageDestinationReferenceDescriptors(WebBundleDescriptor webBundleDescriptor) {
        for (MessageDestinationReferenceDescriptor mdRef :
                getMessageDestinationReferenceDescriptors()) {
            MessageDestinationReferenceDescriptor mdr =
                _getMessageDestinationReferenceByName(mdRef.getName());
            if (mdr != null) {
                webBundleDescriptor.conflictMessageDestinationReference = true;
                unionInjectionTargets(mdr, mdRef);
            } else {
                addMessageDestinationReferenceDescriptor(mdRef);
            }
        }
    }

    @Override
    protected void combineEntityManagerReferenceDescriptors(WebBundleDescriptor webBundleDescriptor) {
        for (EntityManagerReferenceDescriptor emRef :
                getEntityManagerReferenceDescriptors()) {
            EntityManagerReferenceDescriptor emr =
                _getEntityManagerReferenceByName(emRef.getName());
            if (emr != null) {
                webBundleDescriptor.conflictEntityManagerReference = true;
                unionInjectionTargets(emr, emRef);
            } else {
                addEntityManagerReferenceDescriptor(emRef);
            }
        }
    }

    @Override
     protected void combineEntityManagerFactoryReferenceDescriptors(WebBundleDescriptor webBundleDescriptor) {
        for (EntityManagerFactoryReferenceDescriptor emfRef :
                getEntityManagerFactoryReferenceDescriptors()) {
            EntityManagerFactoryReferenceDescriptor emfr =
                _getEntityManagerFactoryReferenceByName(emfRef.getName());
            if (emfr != null) {
                webBundleDescriptor.conflictEntityManagerReference = true;
                unionInjectionTargets(emfr, emfRef);
            } else {
                addEntityManagerFactoryReferenceDescriptor(emfRef);
            }
        }
    }

    @Override
    protected void combinePostConstructDescriptors(WebBundleDescriptor webBundleDescriptor) {
        getPostConstructDescriptors().addAll(webBundleDescriptor.getPostConstructDescriptors());
    }

    @Override
    protected void combinePreDestroyDescriptors(WebBundleDescriptor webBundleDescriptor) {
        getPreDestroyDescriptors().addAll(webBundleDescriptor.getPreDestroyDescriptors());
    }

    @Override
    protected void combineDataSourceDefinitionDescriptors(WebBundleDescriptor webBundleDescriptor) {
        for (DataSourceDefinitionDescriptor ddd: webBundleDescriptor.getDataSourceDefinitionDescriptors()) {
            DataSourceDefinitionDescriptor ddDesc = getDataSourceDefinitionDescriptor(ddd.getName());
            if (ddDesc == null) {
                getDataSourceDefinitionDescriptors().add(ddd);
            } else {
                conflictDataSourceDefinition = true;
            }
        }
    }

    /**
     * Copy all injection targets from env2 to env1.
     *
     * @param env1
     * @param env2
     */
    private void unionInjectionTargets(EnvironmentProperty env1, EnvironmentProperty env2) {
        for (InjectionTarget injTarget: env2.getInjectionTargets()) {
            env1.addInjectionTarget(injTarget);
        }
    }

    /**
     * Return a formatted version as a String.
     */
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("\nWeb Fragment descriptor");
        toStringBuffer.append("\n");
        printCommon(toStringBuffer);
        if (jarName != null) {
            toStringBuffer.append("\njar name " + jarName);
        }
        if (ordering != null) {
            toStringBuffer.append("\nordering " + ordering);
        }
    }
}
