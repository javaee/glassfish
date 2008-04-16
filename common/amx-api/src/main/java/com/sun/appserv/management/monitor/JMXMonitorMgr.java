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
 */

package com.sun.appserv.management.monitor;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.Singleton;
import com.sun.appserv.management.base.Utility;
import com.sun.appserv.management.base.XTypes;

import java.util.Map;


/**
	Supports the standard monitoring facilities of javax.management.monitor
	by making available routines to create the Monitors available in 
	javax.management.monitor, and to determine query which such Monitors
	are currently loaded.
	<p>
	Note that the naming is somewhat confusing; the use of the term "Monitor"
	here derives from javax.management.monitor; com.sun.appserv.monitor refers
	to MBeans that provide statistics on appserver runtime entities.
 */
public interface JMXMonitorMgr extends AMX, Singleton, Utility
{
/** The j2eeType as returned by {@link com.sun.appserv.management.base.AMX#getJ2EEType}. */
	public static final String	J2EE_TYPE	= XTypes.JMX_MONITOR_MGR;
    
    /**
    	Create a new Monitor.
    	@param name
     */
    public AMXStringMonitor		createStringMonitor( final String name );
    
    /**
    	Create a new Monitor.
    	@param name
     */
    public AMXCounterMonitor		createCounterMonitor( final String name );
    
    /**
    	Create a new Monitor.
    	@param name
     */
    public AMXGaugeMonitor		createGaugeMonitor( final String name );
    
    
    /**
    	Return a Map of all AMXStringMonitor MBean.  The Map is keyed by name.
     */
    public Map<String,AMXStringMonitor> 		getStringMonitorMap();
    
    /**
    	Return a Map of all AMXCounterMonitor.  The Map is keyed by name.
     */
    public Map<String,AMXCounterMonitor> 		getCounterMonitorMap();
    
    /**
    	Return a Map of all AMXGaugeMonitor.  The Map is keyed by name.
     */
    public Map<String,AMXGaugeMonitor> 		getGaugeMonitorMap();

    
    /**
    	Remove a Monitor by name.
    	@param name
     */
    public void		remove( final String name );
}
