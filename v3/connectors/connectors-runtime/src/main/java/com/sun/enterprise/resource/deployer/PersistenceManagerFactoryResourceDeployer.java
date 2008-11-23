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

/*
 * Copyright 2000-2001 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of iPlanet/Sun Microsystems, Inc. ("Confidential Information").
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license
 * agreement you entered into with iPlanet/Sun Microsystems.
 *
 * $Id: PersistenceManagerFactoryResourceDeployer.java,v 1.5 2007/05/05 05:35:14 tcfujii Exp $
 */
package com.sun.enterprise.resource.deployer;

import com.sun.appserv.connectors.internal.spi.ResourceDeployer;
import com.sun.enterprise.config.serverbeans.PersistenceManagerFactoryResource;

import com.sun.enterprise.resource.beans.PMFResource;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Set;
import java.util.Iterator;
import java.util.List;
import java.lang.reflect.Method;

import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.repository.ResourceProperty;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.appserv.connectors.internal.api.JavaEEResource;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.api.admin.config.Property;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.Singleton;

/**
 * Handles PersistenceManagerFactory resource evnets in the server instance.
 * When user adds a pmf resource, the admin instance fires a resource event.
 * And the event is propagated to this object.
 * The methods may be invoked concurrently, therefore synchronized is used.
 *
 * @author Shing Wai Chan
 */
 @Service
 @Scoped(Singleton.class)
public class PersistenceManagerFactoryResourceDeployer implements ResourceDeployer {

    @Inject
    private GlassfishNamingManager namingMgr;

    //TODO V3 log strings for the entire class
    
    /** StringManager for this deployer */
    private static final StringManager localStrings =
        StringManager.getManager(PersistenceManagerFactoryResourceDeployer.class);

    /** logger for this deployer */
    private static Logger _logger=LogDomains.getLogger(
            PersistenceManagerFactoryResourceDeployer.class, LogDomains.CORE_LOGGER);

    private static final String SET_ = "set";

    //GJCINT
    private static final String SET_CONNECTION_FACTORY_NAME = "setConnectionFactoryName";

    
    //---- begin implements ResourceDeployer ----

    public synchronized void deployResource(Object resource) throws Exception {
        PersistenceManagerFactoryResource configPMFRes =
                (PersistenceManagerFactoryResource)resource;

        //TODO V3 isEnabled() not available ?
        //if (configPMFRes.isEnabled()) {
            // load associated jdbc resource with PMF
            loadJdbcResource(configPMFRes);

            PMFResource j2eeResource = (PMFResource) toPMFJavaEEResource(configPMFRes);

            //ResourceInstaller installer = ConnectorRuntime.getRuntime().getResourceInstaller();

            installPersistenceManagerResource(j2eeResource);

            //TODO V3 not needed ?
            //installer.addResource(j2eeResource);
/*
        } else {
            _logger.log(Level.INFO, "core.resource_disabled",
                new Object[] {configPMFRes.getJndiName(),
                              IASJ2EEResourceFactoryImpl.PMF_RES_TYPE});
        }
*/
    }

    public synchronized void undeployResource(Object resource)
            throws Exception {

        PersistenceManagerFactoryResource configPMFRes =
                (PersistenceManagerFactoryResource)resource;
        namingMgr.unpublishObject(configPMFRes.getJndiName());

/* TODO V3 not needed ?
        ResourceInstaller installer = Switch.getSwitch().getResourceInstaller();
        installer.removeResource(
                toPMFJavaEEResource(configPMFRes));
*/
    }

    public synchronized void redeployResource(Object resource)
            throws Exception {
        undeployResource(resource);
        deployResource(resource);
    }

    public boolean handles(Object resource){
        return resource instanceof PersistenceManagerFactoryResource;
    }


    public synchronized void enableResource(Object resource) throws Exception {
        deployResource(resource);
    }

    public synchronized void disableResource(Object resource) throws Exception {
        undeployResource(resource);
    }

/* TODO V3 not needed ?
    */
/**
     * Utility method to find a resource from a Resource bean and convert
     * it to a resource object to be used by ResourceDeployer implementation
     *
     * @param     name      resource name (normally the jndi-name)
     * @param     rbeans    Resources config-beans
     * @exception Exception thrown if fail
     */
/*
    public Object getResource(String name, Resources rbeans) throws Exception {
        Object res = rbeans.getPersistenceManagerFactoryResourceByJndiName(name);
        if (res == null) {
            String msg = localStrings.getString("resource.no_resource",name);
            throw new Exception(msg);
        }
        return res;
    }
*/

    private void loadJdbcResource(PersistenceManagerFactoryResource cr) throws Exception {

        String resName = cr.getJdbcResourceJndiName();
            try {
                InitialContext ic = new InitialContext();
                ic.lookup(resName);
            } catch (Exception e) {
                // resource is not loaded
                //TODO V3 logstrings
                _logger.log(Level.WARNING,"Unable to load jdbc-resource [ "+resName+" ] " +
                        "used by PMF [ "+cr.getJndiName()+" ]", e);
                //TODO V3 throw Exception ?
            }
    }

    public void installPersistenceManagerResource(com.sun.enterprise.resource.beans.PMFResource pmfRes)
            throws Exception {
        String jndiName = null;
        try {
            jndiName = pmfRes.getName();
            logFine("***** installPersistenceManagerResources jndiName *****" + jndiName);

            String factory = pmfRes.getFactoryClass();
            logFine("**** PersistenceManagerSettings - factory " + factory);
            Class pmfImplClass = Class.forName(factory);
            Object pmfImpl = pmfImplClass.newInstance();

            String ds_jndi = pmfRes.getJdbcResourceJndiName();
            if (ds_jndi != null && ds_jndi.length() > 0) {
                String ds_jndi_pm = ConnectorsUtil.getPMJndiName(ds_jndi);
                logFine("**** PersistenceManagerSettings - ds_jndi " + ds_jndi_pm);
                //@TODO : Check whether this call is needed
                DataSource pmDataSource = null;
                try {
                    javax.naming.Context ctx = new javax.naming.InitialContext();
                    //@TODO : Check whether this call is needed
                    pmDataSource = (DataSource) ctx.lookup(ds_jndi_pm);

                    //ASSUMPTION
                    //factory must have the following method specified in JDO
                    //    public void setConnectionFactory(Object);
                    //GJCINT - changing to setConnectionFactoryName
                    Method connFacMethod = pmfImplClass.getMethod(SET_CONNECTION_FACTORY_NAME,
                            new Class[]{String.class});
                    connFacMethod.invoke(pmfImpl, new Object[]{ds_jndi_pm});

                } catch (Exception ex) {
                    _logger.log(Level.SEVERE, "jndi.persistence_manager_config", ds_jndi_pm);
                    _logger.log(Level.FINE, "jndi.persistence_manager_config_excp", ex);
                    throw ex;
                }
            }

            Set propSet = pmfRes.getProperties();
            Iterator propIter = propSet.iterator();
            while (propIter.hasNext()) {
                ResourceProperty prop = (ResourceProperty) propIter.next();
                String name = prop.getName();
                String value = (String) prop.getValue();
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.fine("**** PersistenceManager propSettings - " + name + " " + value);
                }
                String methodName = SET_ + name.substring(0, 1).toUpperCase() +
                        name.substring(1);
                //ASSUMPTION
                //set property in pmf have a mutator with String as arg
                Method method = pmfImplClass.getMethod(methodName,
                        new Class[]{String.class});
                //ASSUMPTION
                method.invoke(pmfImpl, new Object[]{value});
            }


            namingMgr.publishObject(jndiName, pmfImpl, true);

            logFine("***** After publishing PersistenceManagerResources *****");
        } catch (Exception ex) {
            _logger.log(Level.SEVERE, "poolmgr.datasource_error", jndiName);
            _logger.log(Level.FINE, "poolmgr.datasource_error_excp", ex);
            throw ex;
        }
    } // end installPersistenceManagerResource


    private void logFine(String msg) {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine(msg);
        }
    }

    /**
     * Returns a new instance of j2ee pmf resource from the given config bean.
     *
     * This method gets called from the Persistence Manager Factory Resource
     * deployer to convert persistence-manager-resource-factory config bean into
     * pmf j2ee resource.
     *
     * @param rbean persistence-manager-resource-factory config bean
     *
     * @return a new instance of j2ee pmf resource
     *
     */
    public static JavaEEResource toPMFJavaEEResource(
            com.sun.enterprise.config.serverbeans.PersistenceManagerFactoryResource rbean) {
        com.sun.enterprise.resource.beans.PMFResource jr = new PMFResource(rbean.getJndiName());
        //TODO V3 setEnabled() not available ?
        //jr.setEnabled(rbean.isEnabled());
        jr.setFactoryClass(rbean.getFactoryClass());
        jr.setJdbcResourceJndiName(rbean.getJdbcResourceJndiName());

        List<Property> properties = rbean.getProperty();
        if (properties!= null) {
            for (Property property : properties) {
                ResourceProperty rp = new com.sun.appserv.connectors.internal.api.ResourcePropertyImpl(property.getName(), property.getValue());
                jr.addProperty(rp);
            }
        }
        //jr.setDescription(next.getDescription()); // FIXME add this

        return jr;
    }




    //---- end implements ResourceDeployer ----
}
