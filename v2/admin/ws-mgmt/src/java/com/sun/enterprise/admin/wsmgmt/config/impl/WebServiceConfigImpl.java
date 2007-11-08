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
package com.sun.enterprise.admin.wsmgmt.config.impl;

import com.sun.enterprise.admin.wsmgmt.config.spi.WebServiceConfig;
import com.sun.enterprise.config.serverbeans.WebServiceEndpoint;
import com.sun.enterprise.admin.wsmgmt.config.spi.TransformationRule;
import com.sun.enterprise.admin.wsmgmt.config.spi.RegistryLocation;
import com.sun.enterprise.admin.wsmgmt.config.spi.Constants;

import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;

/**
 * This represents management configuration for a web service end point. 
 */
public class WebServiceConfigImpl implements WebServiceConfig {

    public WebServiceConfigImpl( String n, String level, int size, boolean
        jbi ) {
        name = n;
        monitoringLevel = level;
        maxHistorySize = size;
        jbiEnabled = jbi;
    }

    public WebServiceConfigImpl(WebServiceEndpoint wsEp ) {

        if (wsEp == null) {
            return;
        }

        name = wsEp.getName();
        monitoringLevel = wsEp.getMonitoring();
        maxHistorySize = Integer.parseInt(wsEp.getMaxHistorySize());
        jbiEnabled = wsEp.isJbiEnabled();
        tRules = wsEp.getTransformationRule();
    }

    /**
     * Returns the name of the web service endpoint
     *
     * @return the name of the web service endpoint
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the relative name of the web service endpoint
     *
     * @return the relative name of the web service endpoint
     */
    public String getEndpointName() {
        StringTokenizer strTok = new StringTokenizer(name, "#");
        String relId = null;
        while (strTok.hasMoreElements()) {
            relId = (String) strTok.nextElement();
        }
        return relId;
    }

    /**
     * Returns the monitoring level OFF, LOW or HIGH 
     *
     * @return the monitoring level OFF, LOW or HIGH 
     */
    public String getMonitoringLevel() {
        return monitoringLevel;
    }

    /**
     * Returns the max history size (size of stored monitoring stats)
     *
     * @return the max history size (size of stored monitoring stats)
     */
    public int getMaxHistorySize() {
        return maxHistorySize;
    }

    /**
     * Returns true, if this web service endpoint JBI enabled
     *
     * @return true, if this web service endpoint JBI enabled
     */
    public boolean getJbiEnabled() {
        return jbiEnabled;
    }
    
    /**
     * Returns the transformation rules defined for this endpoint
     *
     * @return the transformation rules defined for this endpoint
     */
    public TransformationRule[] getTransformationRule() {
        if ( tRules != null) {
            TransformationRule[] transformRules = new
                        TransformationRuleImpl[tRules.length];
            for (int index =0; index < tRules.length; index++) {
               transformRules[index]= new TransformationRuleImpl(tRules[index]);
            }
            return transformRules;
        } else {
            return null;
        }
    }

    /**
     * Returns the transformation rules defined for this endpoint
     * during request phase
     *
     * @return the transformation rules defined for this endpoint
     */
    public TransformationRule[] getRequestTransformationRule() {
        if ( tRules != null) {
            List tList = new ArrayList();
            for (int index =0; index < tRules.length; index++) {
                String phase = tRules[index].getApplyTo();
                if ( (phase.equals(Constants.REQUEST) ) ||
                        (phase.equals(Constants.BOTH)) ) {
                   if ( tRules[index].isEnabled() == true) {
                       tList.add(new TransformationRuleImpl(tRules[index]));
                   }
               }
            }
            if ( tList.size() > 0 ) {
                TransformationRule[] transformRules = new
                        TransformationRuleImpl[tList.size()];
                return (TransformationRule[]) tList.toArray(transformRules);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Returns the transformation rules defined for this endpoint
     * during response phase
     *
     * @return the transformation rules defined for this endpoint
     */
    public TransformationRule[] getResponseTransformationRule() {
        if ( tRules != null) {
            List tList = new ArrayList();
            for (int index =0; index < tRules.length; index++) {
                String phase = tRules[index].getApplyTo();
                if ( (phase.equals(Constants.RESPONSE) ) ||
                        (phase.equals(Constants.BOTH)) ) {
                   if ( tRules[index].isEnabled() == true) {
                       tList.add(new TransformationRuleImpl(tRules[index]));
                   }
               }
            }
            if ( tList.size() > 0 ) {
                TransformationRule[] transformRules = new
                        TransformationRuleImpl[tList.size()];
                return (TransformationRule[]) tList.toArray(transformRules);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Returns the registries where the web service end point artifacts are
     * published.
     *
     * @return the registries where the web service end point artifacts are
     * published.
     */
    public RegistryLocation[] getRegistryLocation() {
        return null;
    }

    // PRIVATE VARS
    String name = null;
    String monitoringLevel = null;
    boolean jbiEnabled = false;
    int maxHistorySize = 0;
    com.sun.enterprise.config.serverbeans.TransformationRule[] tRules = null;
}
