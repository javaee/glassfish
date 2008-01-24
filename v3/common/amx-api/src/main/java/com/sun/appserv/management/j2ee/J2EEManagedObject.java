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
 * $Header: /cvs/glassfish/appserv-api/src/java/com/sun/appserv/management/j2ee/J2EEManagedObject.java,v 1.2 2007/05/05 05:30:51 tcfujii Exp $
 * $Revision: 1.2 $
 * $Date: 2007/05/05 05:30:51 $
 */
 
package com.sun.appserv.management.j2ee;
 

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.monitor.Monitoring;


 /**
 	The J2EEManagedObject model is the base model of all managed objects
 	in the J2EE Management Model. All managed objects in the J2EE Platform
 	must implement the J2EEManagedObject model.
  */
 public interface J2EEManagedObject extends AMX, ConfigProvider
 {
 	/**
 		The ObjectName of the J2EEManagedObject.
 		All managed objects must have a unique name within the context of
 		the management domain. The name must not be null.
 		<p>
		Note that the Attribute name is case-sensitive
		"getobjectName" as defined by JSR 77.
 		
 		@return the ObjectName of the object, as a String
 	 */
	public String	getobjectName();
	
	/**
		If true, indicates that the managed object provides event
		notification about events that occur on that object.
		
 		NOTE: JSR 77 defines the Attribute name as "eventProvider".
	 */
	public boolean		iseventProvider();
	
	/**
		If true, indicates that this managed object implements the
		StateManageable model and is state manageable.
		<p>
		Note that the Attribute name is case-sensitive
		"stateManageable" as defined by JSR 77.
	 */
	public boolean		isstateManageable();
	
	/**
		If true, indicates that the managed object supports performance
		statistics and therefore implements the StatisticsProvider model.
		<p>
		Note that the Attribute name is case-sensitive
		"statisticProvider" as defined by JSR 77.
	 */
	public boolean		isstatisticProvider();
	public boolean		isstatisticsProvider();
 


	/**
		Get the Monitoring  (if any)
		
		@return the proxy, or null if none
	 */
	public Monitoring		getMonitoringPeer();
	
	/**
		Get the corresponding configuration peer for this MBean (if any).
		The returned proxy will implement the appropriate interface, not
		just AMXConfig.
		
		@return a proxy, or null if not found or inappropriate
	 */
	public AMXConfig		getConfigPeer();
}
