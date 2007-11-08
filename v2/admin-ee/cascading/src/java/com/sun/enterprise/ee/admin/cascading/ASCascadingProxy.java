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

package com.sun.enterprise.ee.admin.cascading;
import java.io.IOException;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.InstanceNotFoundException;
import com.sun.jdmk.remote.cascading.proxy.CascadingProxy;
import com.sun.jdmk.remote.cascading.MBeanServerConnectionFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.admin.common.ObjectNames;
import com.sun.enterprise.admin.common.MBeanServerFactory;



/**
 * Extends CascadingProxy so that at the time of cascading
 * application server specific processing can be done. For ex.
 * registartion of monitoring mBeans in the dotted name registry
 *
 * @author Sreenivas Munnangi
 */

public class ASCascadingProxy extends CascadingProxy {

    // Logger and StringManager
    private static final Logger _logger =
	Logger.getLogger(EELogDomains.EE_ADMIN_LOGGER);
    
    private static final StringManager _strMgr =
	StringManager.getManager(ASCascadingProxy.class);
    
    // local variables
    private ObjectName sourceMBeanName = null;

    // Pass thru constructor
    public ASCascadingProxy(ObjectName sourceMBeanName,
                          MBeanServerConnectionFactory mbscf) {
        super(sourceMBeanName, mbscf);
        this.sourceMBeanName = sourceMBeanName;
    }

    /**
     * Extends the default behavior for application server 
     * specific processing
     */
    public void postRegister(Boolean registrationDone) {
        if(registrationDone.equals(Boolean.TRUE))
        {
            super.postRegister(registrationDone);
            _logger.log(Level.FINE, "cascading.proxy.postregister", sourceMBeanName);
            // first check if it is a monitoring MBean
            if(isMonitoringMBean(sourceMBeanName))
            {
                // get the dotted name of the MBean
                String dottedName = null;
                dottedName = getDottedName(sourceMBeanName);
                _logger.log(Level.FINE, "cascading.proxy.postregister.dottedname", dottedName);
                // register the dotted name and the object name
                if(dottedName != null)
                {
                    registerDottedName(dottedName, sourceMBeanName);
                }
            }
        }
    }
    
    /**
     * Extends the default behavior for application server 
     * specific processing
     */
    public void postDeregister() {
        super.postDeregister();
        _logger.log(Level.FINE, "cascading.proxy.postderegister");
        // check if it is a monitoring Mbean
        if(isMonitoringMBean(sourceMBeanName)) {
            unregisterDottedName();
        }
    }

    /**
     * Ensures that the dottedname is only being queried for the
     * monitoring MBeans.
     * @return true     if the MBean is a monitoring MBean
     */
    private boolean isMonitoringMBean(ObjectName objName) {
        
        boolean monitoringMBean = false;
        try {
            String propertyVal = objName.getKeyProperty(CascadingConstants.MONITOR_PROPERTY_NAME);
            if(propertyVal != null)
            {
                // even the JNDI browsing MBean belongs to the monitoring category
                // and has to be excluded
                if((propertyVal.equals(CascadingConstants.MONITOR_PROPERTY_VAL)) && (! isJndiMBean(objName)))
                    monitoringMBean = true;
            }
        } catch (NullPointerException npe) {
            _logger.log(Level.WARNING, "cascading.proxy.nomonitoringmbean");
        }
        return monitoringMBean;
    }
    
    /**
     * Queries the MBean for its dotted name
     * @return  String  representing the dotted name
     */
   
    private String getDottedName(ObjectName objName) {
   
        String attrName = "dotted-name";
        String dottedName = null;
        
        try {
            dottedName = (String) getAttribute(attrName);
        } catch (Exception e) {
            _logger.log(Level.WARNING,e.getClass().getName()+":"+e.getLocalizedMessage());
        }
        return dottedName;
    }
    
    /**
     * registers the dotted name and the object name of the proxied MBean, in the
     * DottedNameRegistry of DAS.
     */
    private void registerDottedName(String dottedName, ObjectName objName){
        
        // get the dotted name registry
        ObjectName registryName = ObjectNames.getDottedNameMonitoringRegistryObjectName();
        MBeanServer mbs = MBeanServerFactory.getMBeanServer();
        
        try{
			mbs.invoke(registryName,
                   "add",
				   new Object[]{dottedName, objName},
				   new String[]{String.class.getName(), ObjectName.class.getName()});
		} catch(Exception e) {
			_logger.log(Level.FINE, e.getClass().getName()+":"+e.getLocalizedMessage());
		}
    }
    
    /**
     * Unregisters the dotted name of the proxied MBean from the 
     * DottedNameRegistry
     */
    private void unregisterDottedName(){
        
        // get the dotted name registry
        ObjectName registryName = ObjectNames.getDottedNameMonitoringRegistryObjectName();
        MBeanServer mbs = MBeanServerFactory.getMBeanServer();
        
        try{
			mbs.invoke(registryName,
                   "remove",
				   new Object[]{sourceMBeanName},
				   new String[]{ObjectName.class.getName()});
		} catch(Exception e) {
			_logger.log(Level.FINE, e.getClass().getName()+":"+e.getLocalizedMessage());
		}
    }
    
    /**
     * Checks the ObjectName of the MBean, to determine if it is a JNDI MBean
     * @return true if it is a JNDI MBean
     */
    private boolean isJndiMBean(ObjectName objName) {
        
        boolean jndiMBean = false;
        try {
            String propertyVal = objName.getKeyProperty(CascadingConstants.JNDI_PROPERTY_NAME);
            if(propertyVal != null)
            {
                if(propertyVal.equals(CascadingConstants.JNDI_PROPERTY_VAL))
                    jndiMBean = true;
            }
        } catch (NullPointerException npe) {
            _logger.log(Level.WARNING, "cascading.proxy.nojndimbean");
        }
        return jndiMBean;
    }
}
