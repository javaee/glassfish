/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.enterprise.admin.monitor.stats.lb;

// 
// This interface has all of the bean info accessor methods.
// 

public interface InstanceStatsInterface {
	public java.lang.String getNumTotalRequests();

	public java.lang.String getId();

	public void setNumActiveRequests(java.lang.String value);

	public void setNumTotalRequests(java.lang.String value);

	public void setApplicationStatsNumErrorRequests(java.lang.String value);

	public int removeApplicationStats(boolean value);

	public void setApplicationStatsNumFailoverRequests(java.lang.String value);

	public java.lang.String getNumActiveRequests();

	public boolean[] getApplicationStats();

	public void setApplicationStats(int index, boolean value);

	public void setApplicationStatsNumActiveRequests(java.lang.String value);

	public void setHealth(java.lang.String value);

	public void setId(java.lang.String value);

	public int sizeApplicationStats();

	public java.lang.String getApplicationStatsId();

	public void setApplicationStatsNumIdempotentUrlRequests(java.lang.String value);

	public void setApplicationStatsNumTotalRequests(java.lang.String value);

	public java.lang.String getApplicationStatsNumErrorRequests();

	public java.lang.String getApplicationStatsMinResponseTime();

	public int addApplicationStats(boolean value);

	public void setApplicationStatsId(java.lang.String value);

	public void setApplicationStatsAverageResponseTime(java.lang.String value);

	public void setApplicationStatsMinResponseTime(java.lang.String value);

	public void setApplicationStats(boolean[] value);

	public java.util.List fetchApplicationStatsList();

	public java.lang.String getApplicationStatsAverageResponseTime();

	public java.lang.String getApplicationStatsNumIdempotentUrlRequests();

	public java.lang.String getApplicationStatsMaxResponseTime();

	public void setApplicationStatsMaxResponseTime(java.lang.String value);

	public java.lang.String getApplicationStatsNumActiveRequests();

	public java.lang.String getHealth();

	public boolean isApplicationStats(int index);

	public java.lang.String getApplicationStatsNumTotalRequests();

	public java.lang.String getApplicationStatsNumFailoverRequests();

}
