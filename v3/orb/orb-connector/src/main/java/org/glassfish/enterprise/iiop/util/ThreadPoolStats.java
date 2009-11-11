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
 * $Id: ThreadPoolStats.java,v 1.4 2007/05/05 05:25:48 tcfujii Exp $
 * $Date: 2007/05/05 05:25:48 $
 * $Revision: 1.4 $
 */

package org.glassfish.enterprise.iiop.util;

import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.BoundedRangeStatistic;
import org.glassfish.external.statistics.RangeStatistic;
import org.glassfish.external.statistics.Stats;
/**
 * Stats interface for the monitorable attributes of the
 * a generic ThreadPool. This combines the statistics that were exposed in 7.0
 * with the new ones. In 8.0, the generic Thread Pool that can be used by any component
 * in the server runtime is introduced.
 * @author Kedar Mhaswade
 * @since S1AS8.0
 * @version $Revision: 1.4 $
 */

public interface ThreadPoolStats extends Stats {
    
    /** Returns the statistical information about the number of Threads in the associated ThreaPool, as an instance of BoundedRangeStatistic.
     * This returned value gives an idea about how the pool is changing.
     * @return		an instance of {@link BoundedRangeStatistic}
     */
    public BoundedRangeStatistic getCurrentNumberOfThreads();
    
    /** Returns the total number of available threads, as an instance of {@link CountStatistic}.
     * @return		an instance of {@link CountStatistic}
     */
    public CountStatistic getNumberOfAvailableThreads();
    
    /** Returns the number of busy threads, as an instance of {@link CountStatistic}.
     * @return		an instance of {@link CountStatistic}
     */
    public CountStatistic getNumberOfBusyThreads();
    
    /**
     * Returns the statistical information about the average completion time of a work item in milliseconds.
     * @return	an instance of {@link RangeStatistic}
     */
    public RangeStatistic getAverageWorkCompletionTime();
    
    /** Returns the the total number of work items added so far to the work queue associated with threadpool.
     * @return		an instance of {@link CountStatistic}
     */
    public CountStatistic getTotalWorkItemsAdded();
    
    /**
     * Returns average time in milliseconds a work item waited in the work queue before getting processed.
     * @return		an instance of {@link RangeStatistic}
     */
    public RangeStatistic getAverageTimeInQueue();
    
    /**
     * Returns the work items in queue
     * @return	an instance of {@link BoundedRangeStatistic}
     */
    public BoundedRangeStatistic getNumberOfWorkItemsInQueue();
    
}
