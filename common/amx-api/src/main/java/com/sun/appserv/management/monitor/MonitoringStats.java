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
package com.sun.appserv.management.monitor;

import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.Stats;
import javax.management.openmbean.CompositeDataSupport;

/**
	Monitoring MBeans which expose one or more
	{@link Statistic} implement this interface.
	<p>
	Each MonitoringStats MBean exposes Statistics as Attributes formed
	using the following pattern:
<pre>    <i>statistic-name</i>_<i>field-name</i> </pre>
	For example, a CountStatistic names "Hosts" would generate the following
	Attributes:
	<ul>
	<li>Hosts_Name</li>
	<li>Hosts_Description</li>
	<li>Hosts_Unit</li>
	<li>Hosts_StartTime</li>
	<li>Hosts_LastSampleTime</li>
	<li>Hosts_Count</li>
	</ul>
	For most purposes, it is the "<i>name</i>_Count" value which should be of
	primary interest.
	<p>
	The type of the Statistic will govern how many Attributes are generated, 
	based on its actual fields, but every Statistic interface will have
	at least the Name, Description, Unit, StartTime and LastSampleTime fields present.
    <p>
    <b>Note:</b> These derived Attributes are not made available directly
    in the interface for the MBean</b>; please refer to the documentation
    for each MonitoringStats MBean to determine which Statistics are available,
    and thus which derived Attributes available.
    @see com.sun.appserv.management.base.AMX
    @see com.sun.appserv.management.base.StdAttributesAccess
    @see com.sun.appserv.management.base.Util#getExtra
    @see com.sun.appserv.management.base.Extra
    @see javax.management.j2ee.statistics.Statistic
 */
public interface MonitoringStats extends Monitoring
{
	/**
		Get a JSR 77 Stats object for all available statistics. Semantically,
		the returned Stats object MUST be a "snapshot" to the current state
		of the world.
	 */
	public Stats		getStats();
	public String		getStatsInterfaceName();
	
	/**
		Get a specific JSR 77 Statistic.
		
		@param name	the Statistic name
	 */
	public Statistic	getStatistic( String name );
	
	/**
		Get specific JSR 77 Statistics. If a Statistic is not found, then null
		is returned in its array slot.
		
		@param names	the Statistic names
		@return corresponding values for the names
	 */
	public Statistic[]	getStatistics( String[] names );
	
	/**
		Get the names of all available Statistics.
	 */
	public String[]	getStatisticNames( );
	
	
	/**
		Get a JSR 77 Stats object encoded as a standard serializable JMX OpenType.
	 */
	public CompositeDataSupport		getOpenStats();
	
	
	/**
		Get a JSR 77 Statistic encoded as a standard serializable JMX OpenType.
		
		@param name	the Statistic name
	 */
	public CompositeDataSupport		getOpenStatistic( String name );
	
	
	/**
		Get JSR 77 Statistics encoded as a standard serializable JMX OpenTypes.
		If a Statistic is not found, then null is returned in its array slot.
		
		@param names	the Statistic names
		@return corresponding values for the names
	 */
	public CompositeDataSupport[]		getOpenStatistics( String[] names );
	
	
	/**
		Refresh any stale data.  This may or may not be applicable, depending
		on how the implementation performs its data collection.  If the data
		is always "live", then the implementation of this routine does nothing.
		
		@return true if there may be new data present, false otherwise
	 */
	public boolean		refresh();
}




