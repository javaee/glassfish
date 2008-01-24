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
package com.sun.appserv.management.monitor.statistics;

import javax.management.j2ee.statistics.Stats;
import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.BoundedRangeStatistic;
import javax.management.j2ee.statistics.RangeStatistic;

import com.sun.appserv.management.j2ee.statistics.StringStatistic;

/**
	@see com.sun.appserv.management.monitor.ConnectionQueueMonitor
 */
public interface ConnectionQueueStats extends Stats
{
    /** 
        Gets the total number of connections that have been queued.
        <p>
        A given connection may be queued multiple times, so
        <code>CountTotalQueued</code> may be greater than or equal to
        <code>CountTotalConnections</code>.
     
        @return number of connections queued
     */        
	public CountStatistic	getCountTotalQueued();
	
    /** 
        Gets the number of times the queue has been too full to accommodate
        a connection
     * @return number of overflows
     */    
	public CountStatistic	getCountOverflows();
	 
    /**
        @return total number of connections that have been accepted.
     */   
	public CountStatistic	getCountTotalConnections();
	
    /** 
        Gets the average number of connections queued in the last 5 minutes
     
     @return average
     */    
	public CountStatistic	getCountQueued5MinuteAverage();
	
    /**
        Gets the total number of ticks that connections have spent in the
        queue. A tick is a system-dependent unit of time.
     @return number of ticks
     */    
	public CountStatistic	getTicksTotalQueued();
	
    /** 
        Gets the average number of connections queued in the last 1 minute
     
        @return average number
     */    
	public CountStatistic	getCountQueued1MinuteAverage();
	
    /** 
        Gets the average number of connections queued in the last 15 minutes
        @return average number
     */    
	public CountStatistic	getCountQueued15MinuteAverage();
	
    /**
        Gets the maximum size of the connection queue
        @return the maximum size
     */    
	public CountStatistic	getMaxQueued();
	
    /**
        Gets the largest number of connections that were in the queue
        simultaneously.
     
        @return the count
     */    
	public CountStatistic	getPeakQueued();
	
    /** 
        @return The ID of the connection queue
     */
	public StringStatistic	getID();
	
    /**
     * @return number of connections currently in the queue
     */    
	public CountStatistic	getCountQueued();
}
