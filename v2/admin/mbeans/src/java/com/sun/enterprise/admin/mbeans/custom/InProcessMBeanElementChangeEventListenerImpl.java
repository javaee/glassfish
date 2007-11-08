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

package com.sun.enterprise.admin.mbeans.custom;

import com.sun.enterprise.admin.server.core.CustomMBeanException;
import com.sun.enterprise.admin.common.MBeanServerFactory;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.admin.event.AdminEventListenerException;
import com.sun.enterprise.admin.event.MBeanElementChangeEvent;
import com.sun.enterprise.admin.event.MBeanElementChangeEventListener;
import com.sun.enterprise.admin.mbeans.custom.loading.CustomMBeanRegistrationImpl;
import com.sun.enterprise.admin.mbeans.custom.loading.MBeanAttributeSetter;
import com.sun.enterprise.admin.server.core.CustomMBeanRegistration;
import com.sun.enterprise.config.ConfigAdd;
import com.sun.enterprise.config.ConfigChange;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigDelete;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.config.serverbeans.Mbean;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.ApplicationHelper;
import com.sun.enterprise.config.serverbeans.Server;
import java.util.ArrayList;
import java.util.logging.Logger;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.JMException;
import com.sun.enterprise.config.ConfigException;

public class InProcessMBeanElementChangeEventListenerImpl implements MBeanElementChangeEventListener {
    
    private static final Logger logger  = Logger.getLogger(AdminConstants.kLoggerName);
    private final MBeanServer mbs       = MBeanServerFactory.getMBeanServer();
    public InProcessMBeanElementChangeEventListenerImpl() {
    }    

    public void handleUpdate(final MBeanElementChangeEvent event) throws AdminEventListenerException {
            /* Note that none of the attributes of "mbean" element except "enabled" and "object-name" are modifiable dynamically. Only the properties in this element,
            that are actually the attributes of runtime mbean are modifiable dynamically. Also, note that there need
            not be any validation of the attribute changes in this listener, because that is supposed to happen at
            the configuration time. Hence a ConfigChange has to be only creation/deletion/update of a property element or the two attributes discussed above.
            Every such change should result in setting the corresponding attribute on the MBean that is already registered. */
        try {
            final ConfigContext rcc                 = event.getConfigContext();
            final ArrayList<ConfigChange> changes    = event.getConfigChangeList();
            for (ConfigChange change : changes) {
                handleUpdate(rcc, change);
            }
        } catch (final AdminEventListenerException ae) {
            throw ae;
        } catch (final Exception e) {
            throw new AdminEventListenerException(e);
        }
    }

    public void handleCreate(final MBeanElementChangeEvent event) throws AdminEventListenerException {
        try {
            final ConfigContext rcc                 = event.getConfigContext();
            final ArrayList<ConfigAdd> additions    = event.getConfigChangeList();
            //the list should is supposed to contain ConfigAdd elements only. Hence the above assignment should be safe
            for (ConfigAdd added : additions) {
                final String xp = added.getXPath();
                if (xp != null) {
                    Object co = rcc.exactLookup(xp);
                    if (co instanceof ApplicationRef) {
                        co = ApplicationHelper.findApplication(rcc,
                                ((ApplicationRef)co).getRef());
                        if (co instanceof Mbean) {
                            Mbean mb = (Mbean)co;
                            if(mb.isEnabled())
                                register(mb);
                        }
                    }
                }
            }
        } catch (final Exception e) {
            throw new AdminEventListenerException(e);
        }
    }

    public void handleDelete(final MBeanElementChangeEvent event) throws AdminEventListenerException {
        try {
            final ConfigContext occ                     = event.getOldConfigContext();
            final ArrayList<ConfigDelete> deletions     = event.getConfigChangeList();
            //the list should is supposed to contain Delete elements only. Hence the above assignment should be safe
            for (ConfigDelete deleted : deletions) {
                final String xp = deleted.getXPath();
                if (xp != null) {
                    Object co = occ.exactLookup(xp);
                    if (co instanceof ApplicationRef) {
                        co = ApplicationHelper.findApplication(occ,
                                ((ApplicationRef)co).getRef());
                        if (co instanceof Mbean) {
                            final Mbean mbean = (Mbean)co;
                            unregister(mbean);
                            logger.info(CMBStrings.get("cmb.successfulDelete", mbean.getName()));
                        }
                    }
                }
            }
        } catch (final Exception e) {
            throw new AdminEventListenerException(e);
        }
    }

    protected void register(final Mbean mbean) throws Exception {
        final ObjectName on = CustomMBeanRegistrationImpl.getCascadingAwareObjectName(mbean);
        if (mbs.isRegistered(on)) {
            logger.info(CMBStrings.get("cmb.unsuccessfulRegistration", on));
        }
        else {
            final CustomMBeanRegistration cmr = new CustomMBeanRegistrationImpl(mbs);
            cmr.registerMBean(mbean);
            logger.info(CMBStrings.get("cmb.successfulRegistration",  mbean.getName())); 
        }
    }
    
    protected void unregister(final Mbean mbean) throws Exception {
        // this is the best-case effort
//        final String ons    = mbean.getObjectName();
//        final ObjectName on = new ObjectName(ons);
        final ObjectName on = CustomMBeanRegistrationImpl.getCascadingAwareObjectName(mbean);
        if (mbs.isRegistered(on)) {
            mbs.unregisterMBean(on);
            logger.info(CMBStrings.get("cmb.successfulUnRegistration", on));
        }
        else {
            logger.info(CMBStrings.get("cmb.unsuccessfulUnRegistration", on));
        }
    }
    ///// Private methods /////
    ///// Private methods /////
    private void handleUpdate(ConfigContext rcc, ConfigChange change) 
        throws JMException, ConfigException, AdminEventListenerException, CustomMBeanException {
        final String xp = change.getXPath();

        if (xp == null) 
            return;

        logger.fine(CMBStrings.get("cmb.gotConfigChange", xp));

        final Object changedObject = rcc.exactLookup(xp);

        final String parentPath = xp.substring(0, xp.lastIndexOf("/"));
        Object parent = null;
        try {
            parent = rcc.exactLookup(parentPath);
        } catch(Exception e) {
            // ignore for now
        }
        
        if (changedObject instanceof Mbean) {
            handleUpdate((Mbean)changedObject);
        } else if (changedObject instanceof ElementProperty) {
            handleUpdate(parent, (ElementProperty)changedObject);
        } else if (changedObject instanceof ApplicationRef) {
            handleUpdate(rcc, (ApplicationRef)changedObject);
        }
        else {
            throw new AdminEventListenerException(CMBStrings.get("InternalError", "Can't handle this: ", changedObject.getClass()));
        }
    }
    
    private void handleUpdate(Mbean mbean){
        logger.info(CMBStrings.get("cmb.illegalHandleUpdate", mbean.getName()));
    }

    private void handleUpdate(Object parent, ElementProperty ep) throws JMException, CustomMBeanException{
        final Mbean mbean               = (Mbean) parent;
        if (! mbean.isEnabled()) {
            logger.info(CMBStrings.get("cmb.mbeanIsDisabled", mbean.getName()));
            return;
        }
        final ObjectName on             = new ObjectName(mbean.getObjectName());
        final MBeanAttributeSetter mas  = new MBeanAttributeSetter(mbs, on);
        mas.setIt(ep.getName(), ep.getValue());
    }
    
    private void handleUpdate(ConfigContext rcc, ApplicationRef ref) throws JMException, ConfigException, AdminEventListenerException{
        String mbeanName = ref.getRef();
        Mbean mbean = (Mbean)ApplicationHelper.findApplication(rcc, mbeanName);

        // note: register and unregister declares "throws Exception".  The cleanest
        // way to deal with that is to simply wrap "Exception" and throw it back out.
        // Otherwise I'd have to declare "throws Exception" on each method all the way back up the
        // call stack
        
        try {
            if(ref.isEnabled()) {
                register(mbean);
            }
            else {
                unregister(mbean);
            }
        }catch(Exception e) {
            throw new AdminEventListenerException(e);
        }
    }
}
