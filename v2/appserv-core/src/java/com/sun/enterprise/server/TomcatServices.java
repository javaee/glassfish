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
package com.sun.enterprise.server;

import com.sun.enterprise.server.pluggable.InternalServicesList;

/**
 * pluggable interface (this class will provide ApplicationServer
 * with the right lifecycles array)
 */
public class TomcatServices implements InternalServicesList {

   public String[][] getServicesByName() {
        // AdminService need to be first one due to RMI timeup issue.
        // We need to set securityManager before any runtime.
        // So, we put the following assumption in the code below:
        // 1) The constructor of AdminServiceLifeCycle will not 
        //    not create any runtime object and cannot access SSL.
        // 2) Any LifeCycle should not access SSL in the constructor of
        //    LifeCycle.  This should be done in onInitialization.
	String[][] servicesByName = {
	        {"AdminService", 
	        "com.sun.enterprise.admin.server.core.AdminServiceLifeCycle"},
                //Remote JMX (JSR 160) Connector
                 {"RemoteJmxConnector",
                  "com.sun.enterprise.admin.server.core.JmxConnectorLifecycle"},
                {"SecurityService",
                "com.sun.enterprise.security.SecurityLifecycle"},
                {"SelfManagement Service",
                "com.sun.enterprise.management.selfmanagement.SelfManagementService"},                        
	        {"PersistenceManagerService", 
		"com.sun.jdo.spi.persistence.support.sqlstore.ejb.PersistenceManagerServiceImpl"},	
		{"JMSProvider", 
		"com.sun.enterprise.jms.JmsProviderLifecycle"},
		{"WSMgmt Service", 
		"com.sun.enterprise.admin.wsmgmt.lifecycle.AppServWSMgmtAdminLifeCycle"},

		{"System Application Service", 
		"com.sun.enterprise.server.SystemAppLifecycle"},
		{"LifecycleModuleService", 
		"com.sun.appserv.server.LifecycleModuleService"},
		{"Application Service", 
		"com.sun.enterprise.server.ApplicationLifecycle"},
	        {"Servlet/JSP Service", 
		"com.sun.enterprise.web.PEWebContainerLifecycle"},
                {"DeclarativeLifecycleEventService",
                "com.sun.enterprise.admin.selfmanagement.event.DeclarativeLifecycleEventService"},
                        
	};
	return servicesByName;

   }

}
