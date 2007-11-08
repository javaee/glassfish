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

package com.sun.enterprise.security.audit;

import java.util.Properties;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.AuditModule;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.admin.event.AdminEventListenerException;
import com.sun.enterprise.admin.event.AuditModuleEvent;
import com.sun.enterprise.admin.event.AuditModuleEventListener;

/**
 * Listener interface to handle audit module events.
 * So that audit module can be dynamically created/update/deleted.
 * @author Shing Wai Chan
 */
public class AuditModuleEventListenerImpl implements AuditModuleEventListener {

    /**
     * New audit module created.
     * It is called whenever a AuditModuleEvent with action of
     * AuditModuleEvent.ACTION_CREATE is received.
     * @throws AdminEventListenerException when the listener is unable to
     *         process the event.
     */
    public void auditModuleCreated(AuditModuleEvent event)
             throws AdminEventListenerException {
        try {
            String moduleName = event.getModuleName();
            AuditModule am = getAuditModule(moduleName,
                    event.getConfigContext());
            String classname = am.getClassname();
            Properties props = getAuditModuleProperties(am);      

            AuditManager manager =
                    AuditManagerFactory.getInstance().getAuditManagerInstance();
            manager.addAuditModule(moduleName, classname, props);
        } catch(Exception ex) {
            throw new AdminEventListenerException(ex);
        }
    }

    /**
     * Audit module deleted.
     * It is called whenever a AuditModuleEvent with action of
     * AuditModuleEvent.ACTION_DELETE is received.
     * @throws AdminEventListenerException when the listener is unable to
     *         process the event.
     */
    public void auditModuleDeleted(AuditModuleEvent event)
             throws AdminEventListenerException {
        try {
            String moduleName = event.getModuleName();
            AuditManager manager =
                    AuditManagerFactory.getInstance().getAuditManagerInstance();
            manager.removeAuditModule(moduleName);
        } catch(Exception ex) {
            throw new AdminEventListenerException(ex);
        }
    }

    /**
     * Audit module updated (attributes change).
     * It is called whenever a AuditModuleEvent with action of
     * AuditModuleEvent.ACTION_UPDATE is received.
     * @throws AdminEventListenerException when the listener is unable to
     *         process the event.
     */
    public void auditModuleUpdated(AuditModuleEvent event)
             throws AdminEventListenerException {
        try {
            String moduleName = event.getModuleName();

            AuditModule am = getAuditModule(moduleName,
                    event.getConfigContext());
            String classname = am.getClassname();
            Properties props = getAuditModuleProperties(am);      

            AuditModule oldAm = getAuditModule(moduleName,
                    event.getOldConfigContext());
            String oldClassname = oldAm.getClassname();
            Properties oldProps = getAuditModuleProperties(oldAm);      

            AuditManager manager =
                    AuditManagerFactory.getInstance().getAuditManagerInstance();
            if (!classname.equals(oldClassname)) {
                manager.addAuditModule(moduleName, classname, props);
            } else if (!props.equals(oldProps)) {
                com.sun.appserv.security.AuditModule auditModule =
                        manager.getAuditModule(moduleName);
                auditModule.init(props);
            }
        } catch(Exception ex) {
            throw new AdminEventListenerException(ex);
        }
    }

    private AuditModule getAuditModule(String moduleName,
            ConfigContext configContext) throws ConfigException {
        SecurityService security = 
            ServerBeansFactory.getSecurityServiceBean(configContext);
        return security.getAuditModuleByName(moduleName);

    }

    private Properties getAuditModuleProperties(AuditModule am) {
        ElementProperty[] elementProps = am.getElementProperty();
        int size = (elementProps != null) ? elementProps.length : 0;
        Properties props = new Properties();
        //XXX should we set this?
        props.setProperty(AuditManager.NAME, am.getName());
        props.setProperty(AuditManager.CLASSNAME, am.getClassname());
        for (int i = 0; i < size; i++) {
            props.setProperty(elementProps[i].getName(),
                    elementProps[i].getValue());
        }
        return props;
    }
}
