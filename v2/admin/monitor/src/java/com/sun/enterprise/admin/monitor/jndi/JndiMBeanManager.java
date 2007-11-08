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
 * JndiMBeanManager.java
 *
 * Created on March 9, 2004, 1:46 PM
 */

package com.sun.enterprise.admin.monitor.jndi;

import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.util.i18n.StringManager;
import java.util.logging.Logger;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

/**
 * The JndiMBeanManager is responsible for registering and 
 * unregistering the JndiMBean.  
 *
 * @author  Rob Ruyak
 */
public class JndiMBeanManager {
    
    /** MBeanServer for registering/unregistering JndiMBean **/
    private MBeanServer server;
    private static final Logger logger = 
        Logger.getLogger(AdminConstants.kLoggerName);
    private static final StringManager sm = 
        StringManager.getManager(JndiMBeanManager.class);
    
    /** Creates a new instance of JndiMBeanManager */
    public JndiMBeanManager() {
        server = this.getMBeanServer();
    }
    
    /**
     * Registers the JndiMBean.
     *
     * @param objectName The objectName of the JndiMBean.
     */
    public void registerMBean(ObjectName objectName) {
        try {
            if (server.isRegistered(objectName)) {
                logger.fine(sm.getString("monitor.jndi.already_registered", 
                    new Object[] {objectName.toString()}));
                return;
            }            
            server.registerMBean(new JndiMBeanImpl(), objectName);
            logger.finer(sm.getString("monitor.jndi.registered", 
                    new Object[]{objectName.toString()}));
        } catch(Exception e) {
            logger.fine(sm.getString("monitor.jndi.register_exception", 
                    new Object[]{objectName}));
            logger.throwing(JndiMBeanManager.class.getName(),
                    "registerMBean()", e);
        }
    }
    
    /**
     * Unegisters the JndiMBean.
     *
     * @param objectName The objectName of the JndiMBean.
     */
    public void unregisterMBean(ObjectName objectName) {
        try {
            if (server.isRegistered(objectName)) {
                server.unregisterMBean(objectName);
                logger.fine(sm.getString("monitor.jndi.unregistered",
                        new Object[]{objectName.toString()}));
            } else {
                logger.fine(sm.getString("monitor.jndi.never_registered",
                        new Object[]{objectName.toString()}));
            } 
        } catch(Exception e) {
            logger.fine(sm.getString("monitor.jndi.register_failed", 
                    new Object[]{objectName.toString()}));
        }
    }
    
    /**
     * Gets the MBeanServer for use within the JndiMBeanManager.
     *
     * @return The MBeanServer used to register the mbean.
     */
    MBeanServer getMBeanServer() {
        MBeanServer server = null;
        java.util.ArrayList servers = MBeanServerFactory.findMBeanServer(null);
        if(!servers.isEmpty()){
            server = (MBeanServer)servers.get(0);
        }
        return server;
    }
}
