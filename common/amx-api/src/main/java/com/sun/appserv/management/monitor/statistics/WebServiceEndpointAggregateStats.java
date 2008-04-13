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

import com.sun.appserv.management.j2ee.statistics.NumberStatistic;

import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.Stats;

/**
 * Web Service Endpoint's stats interface. It provides faults, response time,
 * throughput and authentication failure/success information.
 *
 * @since AppServer 9.0
 */
public interface WebServiceEndpointAggregateStats extends Stats
{
    /**
     * Returns the total number of fault as a CountStatistic.
     * Generally if an Endpoint results in a fault, this count will
     * increment by one.
     *
     * @return an instance of {@link CountStatistic}
     */
    public CountStatistic getTotalFaults();

    /**
     * Returns the total number of successful runs, as a CountStatistic.
     * Generally if an operation returns with out a fault it is consider a
     * success. normally, this count will increment by one.
     *
     * @return an instance of {@link CountStatistic}
     */
    public CountStatistic getTotalNumSuccess();

    /**
     * Returns the average time in milli seconds spent during the last 
     * successful/unsuccessful attempt to execute the operation, as a
     * CountStatistic. The time spent is generally an indication of 
     * the system load/processing time.
     *
     * @return an instance of {@link CountStatistic}
     */
    public CountStatistic getAverageResponseTime();

    /**
     * Returns the time in milli seconds spent during the last 
     * successful/unsuccessful attempt to execute the operation, as a
     * CountStatistic.  
     *
     * @return an instance of {@link CountStatistic}
     */
    public CountStatistic getResponseTime();

    /**
     * Returns the minimum time spent in milli seconds for any successful/
     * unsuccessful attempt to execute the operation, as a CountStatistic.
     *
     * @return an instance of {@link CountStatistic}
     */
    public CountStatistic getMinResponseTime();

    /**
     * Returns the maximum time spent in milli seconds for any successful/
     * unsuccessful attempt to execute the operation, as a CountStatistic.
     *
     * @return an instance of {@link CountStatistic}
     */
    public CountStatistic getMaxResponseTime();

    /**
     * Returns the number successful messages/minute since the server is
     * started as a NumberStatistic.
     *
     * @return an instance of {@link NumberStatistic}
     */
    public NumberStatistic getThroughput();

    /**
     * Returns the total number of authentication failures as a
     * CountStatistic.
     *
     * @return an instance of {@link CountStatistic}
     */
    public CountStatistic getTotalAuthFailures();

    /**
     * Returns the total number of authentication successes
     * CountStatistic.
     *
     * @return an instance of {@link CountStatistic}
     */
    public CountStatistic getTotalAuthSuccesses();
}
