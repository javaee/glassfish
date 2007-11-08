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

package com.sun.enterprise.security.auth.realm;

import java.util.Properties;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.AuthRealm;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.admin.event.AdminEventListenerException;
import com.sun.enterprise.admin.event.AuthRealmEvent;
import com.sun.enterprise.admin.event.AuthRealmEventListener;
import com.sun.enterprise.security.SecurityUtil;
import com.sun.enterprise.server.pluggable.SecuritySupport;

/**
 * Implements interface AuthRealmEventListener.
 * So that realms can be dynamically created/updated/deleted.
 * @author Shing Wai Chan
 */
public class AuthRealmEventListenerImpl implements AuthRealmEventListener {

    /**
     * New auth realm created.
     * It is called whenever a AuthRealmEvent with action of
     * AuthRealmEvent.ACTION_CREATE is received.
     * @throws AdminEventListenerException when the listener is unable to
     *         process the event.
     */
    public void authRealmCreated(AuthRealmEvent event)
             throws AdminEventListenerException {
        try {
            createRealm(event);
        } catch(Exception ex) {
            throw new AdminEventListenerException(ex);
        }
    } 

    /**
     * Auth realm deleted.
     * It is called whenever a AuthRealmEvent with action of
     * AuthRealmEvent.ACTION_DELETE is received.
     * @throws AdminEventListenerException when the listener is unable to
     *         process the event.
     */
    public void authRealmDeleted(AuthRealmEvent event)
             throws AdminEventListenerException {
        try {
            //only unload the realm, keep any auxiliary file for sanity
            Realm.unloadInstance(event.getAuthRealmName());
        } catch(Exception ex) {
            throw new AdminEventListenerException(ex);
        }
    }

    /**
     * Auth realm updated (attributes change).
     * It is called whenever a AuthRealmEvent with action of
     * AuthRealmEvent.ACTION_UPDATE is received.
     * @throws AdminEventListenerException when the listener is unable to
     *         process the event.
     */
    public void authRealmUpdated(AuthRealmEvent event)
             throws AdminEventListenerException {
        try {
            //XXX replace with a new realm, need to revisit in JSR 196
            createRealm(event);
        } catch(Exception ex) {
            throw new AdminEventListenerException(ex);
        }
    }

    /**
     * This method will create or replace existing realm with a new one
     * in cache.
     * @param event
     * @exception for instance, BadRealmException, ConfigException,
     *            SynchronizationException
     */
    private void createRealm(AuthRealmEvent event) throws Exception {
        ConfigContext configContext = event.getConfigContext();
        String realmName = event.getAuthRealmName();
        SecurityService security = 
            ServerBeansFactory.getSecurityServiceBean(configContext);
        AuthRealm authRealm = security.getAuthRealmByName(realmName);
        //authRealm cannot be null here
        String className = authRealm.getClassname();
        ElementProperty[] elementProps = authRealm.getElementProperty();
        int size = (elementProps != null) ? elementProps.length : 0;
        Properties props = new Properties();
        for (int i = 0; i < size; i++) {
            props.setProperty(elementProps[i].getName(),
                    elementProps[i].getValue());
        }

        if ("com.sun.enterprise.security.auth.realm.file.FileRealm".equals(className)) {
            SecuritySupport secSupp = SecurityUtil.getSecuritySupport();
            secSupp.synchronizeKeyFile(configContext, realmName);
        }
        Realm.instantiate(realmName, className, props);
    }
}
