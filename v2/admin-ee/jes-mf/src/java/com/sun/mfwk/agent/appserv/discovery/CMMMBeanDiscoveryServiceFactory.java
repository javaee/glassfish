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
 * Copyright 2005-2006 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.mfwk.agent.appserv.discovery;

import java.util.ArrayList;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import com.sun.mfwk.agent.appserv.util.Constants;


public class CMMMBeanDiscoveryServiceFactory {
    
    /** 
     * Creates a new instance of CMMMBeanDiscoveryServiceFactory 
     */
    public CMMMBeanDiscoveryServiceFactory() {
    }


    public static CMMMBeanDiscoveryServiceFactory getInstance() {
        return instance;
    }

    public CMMMBeanDiscoveryService getCMMMBeanDiscoveryService() 
        throws Exception {
        MBeanServer mbeanServer = getMBeanServer();
        if(mbeanServer == null) return null;
        return new CMMMBeanDiscoveryServiceImpl(mbeanServer);
    }


    /**
     * Returns the first MBean Server hosting <code>com.sun.cmm.as</code> domain.
     * 
     * @returns  MBeanServer the mbean server hosting the AS CMM mbeans
     */
     private MBeanServer getMBeanServer(){
        ArrayList mbeanServers = MBeanServerFactory.findMBeanServer(null);
        MBeanServer mbeanServer = null;
        String defaultDomain = null;
        String[] domains = null;
        for(int i=0; i<mbeanServers.size(); i++) {
            mbeanServer = (MBeanServer)mbeanServers.get(i);
            defaultDomain = mbeanServer.getDefaultDomain();
            domains = mbeanServer.getDomains();   //@since.unbundled jmx 1.2
            for(int j=0; j<domains.length; j++) {
                if(Constants.DEF_MODULE_NAME.equals(domains[j])) 
                    return mbeanServer;
            }
        }
        return null;
     }

    private static final CMMMBeanDiscoveryServiceFactory instance = new CMMMBeanDiscoveryServiceFactory();

}
