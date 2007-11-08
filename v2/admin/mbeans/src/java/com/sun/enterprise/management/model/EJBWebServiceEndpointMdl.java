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

package com.sun.enterprise.management.model;

import javax.management.ObjectName;
import java.util.Set;
import java.util.Map;
import com.sun.enterprise.admin.wsmgmt.msg.MessageTraceMgr;
import com.sun.appserv.management.ext.wsmgmt.MessageTrace;
import com.sun.enterprise.admin.wsmgmt.stats.spi.StatsProviderManager;
import com.sun.enterprise.admin.wsmgmt.stats.spi.WebServiceEndpointStatsProvider;

/**
 * WebServiceEndpoint type rutime MBean for EJB web service endpoints.
 * This MBean provides functionality required for content visualization. 
 */ 
public class EJBWebServiceEndpointMdl extends WebServiceEndpointMdl {
    
    /**
     * Public constructor.
     *
     * @param name          Name of the web service endpoint
     * @param moduleName    Name of the module (which this web service belongs)
     * @param regName       registration name of the application or module
     * @param isVirtual     true, if the module is stand alone, false otherwise
     * @param isEjb         If this Web service endpoint is implemented as EJB
     *                      or Servlet
     */
    public EJBWebServiceEndpointMdl(String name, String mName, String regName,
    boolean isVirtual, boolean isejb) {
        super(name,mName, regName, isVirtual, isejb);
    }

    /**
     * Public constructor.
     *
     * @param name          Name of the web service endpoint
     * @param moduleName    Name of the module (which this web service belongs)
     * @param regName       registration name of the application or module
     * @param serverName    Name of the server instance
     * @param isVirtual     true, if the module is stand alone, false otherwise
     * @param isEjb         If this Web service endpoint is implemented as EJB
     *                      or Servlet
     */
    public EJBWebServiceEndpointMdl(String name, String moduleName, 
        String regName, String serverName, boolean isVirtual, boolean isejb) {
        super(name,moduleName, regName, serverName, isVirtual, isejb);
    }

    /**
     * The MBean name of the J2EEManagedObject as specified in
     * runtime-mbeans-descriptors.xml. This value is used in registering the 
     * MBean.
     * 
     * @return String MBeanName value of this Managed Object
     */
    public String getMBeanName() {
             return EJB_MBEAN;
    }
}
