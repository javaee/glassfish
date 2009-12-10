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

package com.sun.enterprise.connectors.service;

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.enterprise.connectors.ConnectorConnectionPool;
import com.sun.enterprise.connectors.ConnectorDescriptorInfo;
import com.sun.enterprise.connectors.naming.ConnectorResourceNamingEventNotifier;
import com.sun.appserv.connectors.internal.spi.ConnectorNamingEvent;
import com.sun.enterprise.connectors.naming.ConnectorNamingEventNotifier;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.appserv.connectors.internal.api.ConnectorConstants;

import javax.naming.*;
import java.util.Hashtable;
import java.util.logging.Level;

/**
 * This is connector resource admin service. It creates and deletes the
 * connector resources.
 *
 * @author Srikanth P
 */
public class ConnectorResourceAdminServiceImpl extends ConnectorService {

    /**
     * Default constructor
     */
    public ConnectorResourceAdminServiceImpl() {
        super();
    }

    /**
     * Creates the connector resource on a given connection pool
     *
     * @param jndiName     JNDI name of the resource to be created
     * @param poolName     PoolName to which the connector resource belongs.
     * @param resourceType Resource type Unused.
     * @throws ConnectorRuntimeException If the resouce creation fails.
     */
    public void createConnectorResource(String jndiName, String poolName,
                                        String resourceType) throws ConnectorRuntimeException {

        String errMsg = "rardeployment.jndi_lookup_failed";
        String name = poolName;
        try {
            ConnectorConnectionPool ccp = null;
            String jndiNameForPool = ConnectorAdminServiceUtils.
                    getReservePrefixedJNDINameForPool(poolName);
            Context ic = _runtime.getNamingManager().getInitialContext();
            try {
                ccp =
                        (ConnectorConnectionPool) ic.lookup(jndiNameForPool);
            } catch (NamingException ne) {
                //Probably the pool is not yet initialized (lazy-loading), try doing a lookup
                try {
                    checkAndLoadPool(poolName);
                    ccp =
                            (ConnectorConnectionPool) ic.lookup(jndiNameForPool);
                } catch (NamingException e) {
                    Object params[] = new Object[]{name, e};
                    _logger.log(Level.SEVERE, "unable.to.lookup.pool", params);
                }
            }

            ccp = (ConnectorConnectionPool) ic.lookup(jndiNameForPool);
            ConnectorDescriptorInfo cdi = ccp.getConnectorDescriptorInfo();

            javax.naming.Reference ref=new  javax.naming.Reference(
                   cdi.getConnectionFactoryClass(), 
                   "com.sun.enterprise.resource.naming.ConnectorObjectFactory",
                   null);
            StringRefAddr addr = new StringRefAddr("poolName",poolName);
            ref.add(addr);
            addr = new StringRefAddr("rarName", cdi.getRarName() );
            ref.add(addr);

            errMsg = "Failed to bind connector resource in JNDI";
            name = jndiName;
            _runtime.getNamingManager().publishObject(
                          jndiName,ref,true);

/*

            ConnectorObjectFactory cof = new ConnectorObjectFactory(jndiName, ccp.getConnectorDescriptorInfo().
                    getConnectionFactoryClass(), cdi.getRarName(), poolName);

            _runtime.getNamingManager().publishObject(jndiName, cof, true);
*/

            //To notify that a connector resource rebind has happened.
            ConnectorResourceNamingEventNotifier.getInstance().
                    notifyListeners(
                            new ConnectorNamingEvent(
                                    jndiName, ConnectorNamingEvent.EVENT_OBJECT_REBIND));

        } catch (NamingException ne) {
            ConnectorRuntimeException cre =
                    new ConnectorRuntimeException(errMsg);
            cre.initCause(ne);
            _logger.log(Level.SEVERE, errMsg, name);
            _logger.log(Level.SEVERE, "", cre);
            throw cre;
        }
    }

    /**
     * Deletes the connector resource.
     *
     * @param jndiName JNDI name of the resource to delete.
     * @throws ConnectorRuntimeException if connector resource deletion fails.
     */
    public void deleteConnectorResource(String jndiName)
            throws ConnectorRuntimeException {

        try {

            _runtime.getNamingManager().unpublishObject(jndiName);
        } catch (NamingException ne) {
            /* TODO V3 handle system RAR later
            ResourcesUtil resUtil = ResourcesUtil.createInstance();
            if (resUtil.resourceBelongsToSystemRar(jndiName)) {
                return;
            }
            */
            if (ne instanceof NameNotFoundException) {
                _logger.log(Level.FINE, "rardeployment.connectorresource_removal_from_jndi_error", jndiName);
                _logger.log(Level.FINE, "", ne);
                return;
            }
            ConnectorRuntimeException cre = new ConnectorRuntimeException
                    ("Failed to delete connector resource from jndi");
            cre.initCause(ne);
            _logger.log(Level.SEVERE, "rardeployment.connectorresource_removal_from_jndi_error", jndiName);
            _logger.log(Level.SEVERE, "", cre);
            throw cre;
        }
    }

    /**
     * Gets Connector Resource Rebind Event notifier.
     *
     * @return ConnectorNamingEventNotifier
     */
    public ConnectorNamingEventNotifier getResourceRebindEventNotifier() {
        return ConnectorResourceNamingEventNotifier.getInstance();
    }


    /**
     * Look up the JNDI name with appropriate suffix.
     * Suffix can be either __pm or __nontx.
     *
     * @param name resource-name
     * @return Object - from jndi
     * @throws NamingException - when unable to get the object form jndi
     */
    public Object lookup(String name) throws NamingException {
        Hashtable ht = null;
        String suffix = ConnectorsUtil.getValidSuffix(name);
        if (suffix != null) {
            ht = new Hashtable();
            ht.put(ConnectorConstants.JNDI_SUFFIX_PROPERTY, suffix);
            name = name.substring(0, name.lastIndexOf(suffix));
        }
        //Context ic = _runtime.getNamingManager().getInitialContext();
        //To pass suffix that will be used by connector runtime during lookup
        Context ic = new InitialContext(ht);
        return ic.lookup(name);
    }
}
