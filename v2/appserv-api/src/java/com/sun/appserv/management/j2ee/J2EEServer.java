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
package com.sun.appserv.management.j2ee;

import java.util.Map;
import java.util.Set;

import javax.management.ObjectName;

import com.sun.appserv.management.base.Container;


/**
 */
public interface J2EEServer extends J2EELogicalServer, Container
{
	public final static String		J2EE_TYPE	= J2EETypes.J2EE_SERVER;
	
	/**
	    Restart the server.
	    <b>Enterprise Edition only.</b>
	 */
	public void restart();
	
	/**
		Note that the Attribute name is case-sensitive
		"deployedObjects" as defined by JSR 77.
		
	 	@return the ObjectNames as Strings
	 */
	public String[]	getdeployedObjects();
	
	/**
		Return Set of all deployed objects.  Possible types include:
		<ul>
		<li>{@link com.sun.appserv.management.j2ee.J2EEApplication}</li>
		<li>{@link com.sun.appserv.management.j2ee.WebModule}</li>
		<li>{@link com.sun.appserv.management.j2ee.EJBModule}</li>
		<li>{@link com.sun.appserv.management.j2ee.AppClientModule}</li>
		<li>{@link com.sun.appserv.management.j2ee.ResourceAdapterModule}</li>
		</ul>
		<p>
		To obtain Resources of a particular type, use
		{@link Container#getContaineeMap}(j2eeType).
	 */
	public Set<J2EEDeployedObject>	getDeployedObjectsSet();
	
	/**
		In 8.1, there will only ever be one JVM for a J2EEServer.
		Note that the Attribute name is case-sensitive
		"javaVMs" as defined by JSR 77.
		
	 	@return the ObjectNames as Strings
	 */
	public String[]	getjavaVMs();
	
	/**
		There is always a single JVM for a J2EEServer.
		@return JVM
	 */
	public JVM		getJVM();
	
	/**
		Note that the Attribute name is case-sensitive
		"resources" as defined by JSR 77.
		
	 	@return the ObjectNames as Strings
	 */
	public String[]		getresources();
	
	/**
		Return Set of all resources.  Possible types include:
		<ul>
		<li>{@link com.sun.appserv.management.j2ee.JDBCResource}</li>
		<li>{@link com.sun.appserv.management.j2ee.JavaMailResource}</li>
		<li>{@link com.sun.appserv.management.j2ee.JCAResource}</li>
		<li>{@link com.sun.appserv.management.j2ee.JMSResource}</li>
		<li>{@link com.sun.appserv.management.j2ee.JNDIResource}</li>
		<li>{@link com.sun.appserv.management.j2ee.JTAResource}</li>
		<li>{@link com.sun.appserv.management.j2ee.RMIIIOPResource}</li>
		<li>{@link com.sun.appserv.management.j2ee.URLResource}</li>
		</ul>
		<p>
		To obtain Resources of a particular type, use
		{@link Container#getContaineeMap}(j2eeType).
	 */
	public Set<J2EEResource> getResourcesSet();
	
	/**
		Note that the Attribute name is case-sensitive
		"serverVendor" as defined by JSR 77.
		
	 	@return the server vendor, a free-form String
	 */
	public String		getserverVendor();
	
	/**
		Note that the Attribute name is case-sensitive
		"serverVersion" as defined by JSR 77.
		
	 	@return the server version, a free-form String
	 */
	public String		getserverVersion();
    

	/**
	 	@return true if server configuration has changed such that a restart must
	 	be performed.  A server whose state is STATE_STOPPED or STATE_FAILED
        (as returned by {@link #getstate}) always returns 'true'.
	 */
    public boolean  getRestartRequired();
}



