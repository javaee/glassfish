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
import com.sun.enterprise.deployment.web.LoginConfiguration;
import com.sun.enterprise.deployment.web.SecurityConstraint;

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

    /**
     * This API combines all injection references and set it into
     * webBundleDescriptor and all WebFragmentDescriptor in the list.
     * All resulting WebFragmentDescriptor will have references pointing to
     * given webBundleDescriptor.
     * This is intended to be used for WebArchivist only.
     *
     * @param webBundleDescriptor
     * @param wfList
     */
    public static void repopulateAllInjectionReferences(
            WebBundleDescriptor webBundleDescriptor, List<WebFragmentDescriptor> wfList) {

        for (WebFragmentDescriptor wf : wfList) {
            webBundleDescriptor.combineInjectionReferences(wf);
        }

        for (WebFragmentDescriptor wf : wfList) {
            wf.setInjectionReferences(webBundleDescriptor);
        }
    }
}
