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
 * DottedNameRegistrar.java
 * $Id: DottedNameRegistrar.java,v 1.3 2005/12/25 03:43:33 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:43:33 $
 * Indentation Information:
 * 0. Please (try to) preserve these settings.
 * 1. Tabs are preferred over spaces.
 * 2. In vi/vim -
 *		:set tabstop=4 :set shiftwidth=4 :set softtabstop=4
 * 3. In S1 Studio -
 *		1. Tools->Options->Editor Settings->Java Editor->Tab Size = 4
 *		2. Tools->Options->Indentation Engines->Java Indentation Engine->Expand Tabs to Spaces = False.
 *		3. Tools->Options->Indentation Engines->Java Indentation Engine->Number of Spaces per Tab = 4.
 *
 * Created on September 4, 2003, 5:19 PM
 */

package com.sun.enterprise.admin.monitor.registry.spi;
import javax.management.*;
import com.sun.enterprise.admin.common.ObjectNames;
import java.util.logging.*;
import com.sun.enterprise.admin.common.constant.AdminConstants; // for logger name

/**
 * Mediates calls to the DottedNameRegistry to add or 
 * remove dottedName to Object Name maps.
 * @author  Shreedhar Ganapathy<mailto:shreedhar.ganapathy@sun.com>
 */

class DottedNameRegistrar {
	final MBeanServer server;
	final ObjectName   registryName;
	private static final Logger logger = Logger.getLogger(AdminConstants.kLoggerName);	

	/** Creates a new instance of DottedNameRegistryMediator */
	DottedNameRegistrar(MBeanServer server) {
		this.server=server;
		registryName = ObjectNames.getDottedNameMonitoringRegistryObjectName();
	}
	
	void registerDottedName(java.lang.String dottedName, ObjectName objectName) {
		try{
			server.invoke(registryName, "add", 
				new Object[]{dottedName, objectName}, 
				new String[]{String.class.getName(), ObjectName.class.getName()});
		}
		catch(Exception e){
			logger.fine(e.getClass().getName()+":"+e.getLocalizedMessage());
		}	
	}
	
	void unregisterDottedName(java.lang.String dottedName) {
		try{
			server.invoke(registryName, "remove", 
				new Object[]{dottedName}, 
				new String[]{String.class.getName()});
		}
		catch(Exception e){
			logger.fine(e.getClass().getName()+":"+e.getLocalizedMessage());
		}		
	}	
}
