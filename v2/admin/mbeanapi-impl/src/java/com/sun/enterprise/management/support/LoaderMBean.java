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
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
 
/*
 */

package com.sun.enterprise.management.support;

import javax.management.ObjectName;
import javax.management.MBeanRegistration;
import javax.management.NotificationListener;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;


import com.sun.appserv.management.base.AMXMBeanLogging;

/**
	Loads MBeans. <b>INTERNAL USE ONLY</b>
 */
public interface LoaderMBean
	extends AMXSupport, MBeanRegistration, 
	NotificationListener
{
	public static final String	LOADER_NAME_PROPS	= "name=mbean-loader";
	
	
	/**
		The name of the appserver administrative domain.
		The JMX domain name is derived from this value.
	 */
	public String	getAdministrativeDomainName();
	
	/**
		The JMX domain all AMX MBeans are using.  Derived from mDomainName.
	 */
	public String	getAMXJMXDomainName();
	
	
	/**
		Start loading all MBeans.
		
		@param waitTillDone	if true, waits until started
	public void	start( final boolean waitTillDone );
	 */
	
	/**
		Check if Loader has loaded all MBeans.
		<b>Use
		{@link com.sun.appserv.management.DomainRoot#getAMXReady} instead.</b>.
		
		@return true if started, false otherwise
	 */
	public boolean	isStarted( );
	
	/**
		Synchronize with a specific MBean and return the 
		AMX MBean name for it.
	 */
	public ObjectName	sync( final ObjectName name);
	
	/**
		Wait till all outstanding AMX processing is complate.
	 */
	public void     waitAll();

	
	/**
	    Caution: code may invoke this indirectly by hard-coding the method name.
	    See com.sun.enterprise.admin.server.core.AdminService.callAMXHook()
	 */
	public void         adminServiceReady();
	
	/**
	    Applies only to those MBeans which use a Delegate MBean.
	    Unregister the AMX MBean, and re-process its delegate.
	    Used when an MBean needs to morph into another type, such
	    as a StandaloneServerConfig changing into a ClusteredServerConfig.
	 */
	public ObjectName   resyncAMXMBean( final ObjectName amxObjectName )
	                        throws InstanceNotFoundException, MBeanRegistrationException;
	
	
	/**
	    Return true if in DAS, false otherwise.
	 */
	public boolean  isDAS();
}








