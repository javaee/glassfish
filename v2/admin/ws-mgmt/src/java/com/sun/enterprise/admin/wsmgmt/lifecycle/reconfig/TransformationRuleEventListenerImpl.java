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
package com.sun.enterprise.admin.wsmgmt.lifecycle.reconfig;

import com.sun.enterprise.admin.event.wsmgmt.TransformationRuleEvent;
import com.sun.enterprise.admin.event.wsmgmt.TransformationRuleEventListener;

import com.sun.enterprise.admin.event.AdminEventListenerException;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;

import com.sun.enterprise.config.serverbeans.WebServiceEndpoint;
import com.sun.enterprise.config.serverbeans.TransformationRule;
import com.sun.enterprise.config.serverbeans.J2eeApplication;
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.config.serverbeans.EjbModule;

import com.sun.enterprise.admin.wsmgmt.transform.TransformHandler;
import com.sun.enterprise.admin.wsmgmt.transform.TransformFilter;
import com.sun.enterprise.admin.wsmgmt.filter.spi.Filter;
import com.sun.enterprise.admin.wsmgmt.config.impl.WebServiceConfigImpl;
import com.sun.enterprise.admin.wsmgmt.config.spi.Constants;

/**
 * Listener impl to handle web-service-endpoint/transformation-rule 
 * element events.
 */
public class TransformationRuleEventListenerImpl implements 
        TransformationRuleEventListener {

    /**
     * Handles web-service-endpoint/transformation-rule element removal.
     *
     * @param event    Event to be processed.
     *
     * @throws AdminEventListenerException when the listener is unable to
     *         process the event.
     */
    public void handleDelete(TransformationRuleEvent event)
             throws AdminEventListenerException {
        handleReconfiguration(event,true, true);
    }

    private void handleReconfiguration(TransformationRuleEvent event, boolean 
            takeOld, boolean isRemove)
             throws AdminEventListenerException {
       try { 
           ConfigBean bean = getTRBean(event, takeOld);
           if ( bean instanceof TransformationRule ) {
                TransformationRule tr = (TransformationRule) bean;
                String appId = getApplicationId(tr);
                WebServiceConfigImpl wsc = new WebServiceConfigImpl ( 
                    (WebServiceEndpoint) tr.parent());
                TransformHandler trh = new TransformHandler( wsc , appId);
                Filter f = trh.getFilter(appId, wsc);
                TransformFilter tf = null;
                if ( f != null) {
                    tf = (TransformFilter) f;
                }
                ConfigBean newBean = getTRBean(event, false);
                WebServiceConfigImpl nwsc = null;
                if ( newBean == null) {
                       if ( ! isRemove) {
                            // only remove operation can not have
                            // new element in the new config context
                            throw new AdminEventListenerException();
                       } else {
                            nwsc = wsc;
                       }
                } else {
                    nwsc = new WebServiceConfigImpl(
                        (WebServiceEndpoint) newBean.parent());
                }
                if ( tf == null) {
                    // create new filter to handle transformation
                    tf = (TransformFilter) trh.registerFilter(wsc);
                } else {
                    if ( isRemove) {
                        String applyTo = tr.getApplyTo();
                        if ( applyTo.equals(Constants.BOTH) ||
                            applyTo.equals(Constants.REQUEST)) {
                            com.sun.enterprise.admin.wsmgmt.config.spi.TransformationRule[] rtrs =
                            nwsc.getRequestTransformationRule();
                            tf.resetRequestChain( pruneList( rtrs,
                            tr.getName()));
                        }
                        if ( applyTo.equals(Constants.BOTH) ||
                            applyTo.equals(Constants.RESPONSE)) {
                            com.sun.enterprise.admin.wsmgmt.config.spi.TransformationRule[] rtrs =
                            nwsc.getResponseTransformationRule();
                            tf.resetResponseChain( pruneList( rtrs,
                            tr.getName()));
                        }
                    } else {
                        tf.resetRequestChain(
                            nwsc.getRequestTransformationRule());
                        tf.resetResponseChain(
                            nwsc.getResponseTransformationRule());
                    }
                }
           } 

        } catch (Exception e) {
            throw new AdminEventListenerException(e);
        }
    }


    private com.sun.enterprise.admin.wsmgmt.config.spi.TransformationRule[]
     pruneList (
        com.sun.enterprise.admin.wsmgmt.config.spi.TransformationRule[] tRules, 
    String name) {
        // nothing to prune for a null or empty list
        if (tRules == null || tRules.length < 1) {
            return tRules;
        }

        com.sun.enterprise.admin.wsmgmt.config.spi.TransformationRule[] 
        newRules =
        new com.sun.enterprise.admin.wsmgmt.config.spi.TransformationRule[
        tRules.length-1];

        int newIndex = 0;
        for ( int index =0; index < tRules.length; index++) {
            if ( tRules[index].getName().equals(name)) {
                 // found the match, do not add to the new list
            } else {
                 if ( newIndex >= tRules.length-1) {
                    // removed rule should exist and should not cause array
                    // overflow.
                    throw new RuntimeException();
                 }
                 newRules[newIndex++] = tRules[index];
            }
        }
        return newRules;
    }

    /**
     * Handles web-service-endpoint/transformation-rule element modification 
     * (attributes/properties values changed).
     *
     * @param event    Event to be processed.
     *
     * @throws AdminEventListenerException when the listener is unable to
     *         process the event.
     */
    public void handleUpdate(TransformationRuleEvent event)
             throws AdminEventListenerException {

        handleReconfiguration(event,false,false);
    }

    /**
     * Handles element additions.
     *
     * @param event    Event to be processed.
     *
     * @throws AdminEventListenerException when the listener is unable to
     *         process the event.
     */
    public void handleCreate(TransformationRuleEvent event)
             throws AdminEventListenerException {

        handleReconfiguration(event,false, false);
    }

    private ConfigBean getTRBean(TransformationRuleEvent event, boolean old)
            throws ConfigException {

        if (event == null) {
            throw new IllegalArgumentException();
        }

        ConfigBean bean = null;
        ConfigContext ctx = null;
        String xpath = event.getElementXPath();
        if (old) {
            ctx = event.getOldConfigContext();
        } else {
            ctx = event.getConfigContext();
        }
        if (ctx != null) {
            bean = ctx.exactLookup(xpath);
        }

        return bean;
    }

    private String getApplicationId(TransformationRule trBean) {
        String name = null;

        ConfigBean bean = null;
        if ( trBean != null) {
            bean = (ConfigBean) trBean.parent();
            if (!( bean instanceof WebServiceEndpoint)) {
                throw new RuntimeException();	
            }
        }

        if (bean != null) {
            ConfigBean parent = (ConfigBean) bean.parent();
            if (parent instanceof J2eeApplication) {
                J2eeApplication app = (J2eeApplication) parent;
                name = app.getName();
            } else if (parent instanceof WebModule) {
                WebModule wm = (WebModule) parent;
                name = wm.getName();
            } else if (parent instanceof EjbModule) {
                EjbModule em = (EjbModule) parent;
                name = em.getName();
            }
        }
        return name;
    }
}
