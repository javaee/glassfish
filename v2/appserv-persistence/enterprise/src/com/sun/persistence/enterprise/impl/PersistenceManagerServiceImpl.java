/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */



/*
 * PersistenceManagerServiceImpl.java
 *
 * Created on January 24, 2002
 */


package com.sun.persistence.enterprise.impl;

import com.sun.appserv.server.ServerLifecycleImpl;
import com.sun.persistence.utility.logging.LogHelper;

/**
 * This class extends the default implementation of the ServerLifecycle
 * interface and allows to load Sun specific implementation of the EJBHelper as
 * a part of Sun - Server Lifecycle process. This class does not need to do any
 * processing for the Server Lifecycle events other than load the necessary
 * class.
 */
public class PersistenceManagerServiceImpl extends ServerLifecycleImpl {

    // Initialize the appserver loggers.
    static {
        LogHelper.registerLoggerFactory(new LoggerFactoryAS());

        try {
            // Need to fully initialize the classes
            ClassLoader cl = PersistenceManagerServiceImpl.class.getClassLoader();

            // Sun specific implementation of the EJBHelper and the ContainerHelper.
            //Class.forName(SunContainerHelper.class.getName(), true, cl);
            Class.forName(SunEJBHelperImpl.class.getName(), true, cl);

            // Implementation of the DeploymentEventListener.
            //Class.forName(DeploymentEventListenerImpl.class.getName(), true, cl);

            // XXX 8.x version - remove when not needed
            // need to use fully qualified class name as this class has the same name
            Class.forName(com.sun.jdo.spi.persistence.support.sqlstore.ejb.PersistenceManagerServiceImpl.class.getName(), true, cl);
        } catch (Exception ex) {
            System.err.println("ERROR!!! in initialization of PersistenceManagerService");
            ex.printStackTrace();
        }
    }
}
