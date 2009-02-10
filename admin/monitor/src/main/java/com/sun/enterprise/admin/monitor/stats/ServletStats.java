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
 * $Id: ServletStats.java,v 1.2 2005/12/25 03:52:24 tcfujii Exp $
 * $Date: 2005/12/25 03:52:24 $
 * $Revision: 1.2 $
 *
 */

package com.sun.enterprise.admin.monitor.stats;
import org.glassfish.j2ee.statistics.Stats;
import org.glassfish.j2ee.statistics.CountStatistic;
import org.glassfish.j2ee.statistics.TimeStatistic;

/** 
 * Defines additional Sun ONE Application Server specific statistics 
 * ServletStats interface.
 * The ServletStats interface that is defined by JSR77, cannot be used
 * here, as it is not possible to encapsulate the data pertaining to 
 * the service method in a TimeStatistic. Therefore it becomes necessary
 * to define our own interface for exposing Servlet Statistics.
 * @since S1AS8.0
 */
public interface ServletStats extends Stats {
    
    /**
     * Number of requests processed by this servlet.
     * @return CountStatistic
     */
    public CountStatistic getRequestCount();
    
    /**
     * Cumulative Value, indicating the time taken to process the
     * requests received so far.
     * @return CountStatistic
     */
    public CountStatistic getProcessingTime();
    
    /**
     * Gets the execution time of the servlet's service method.
     *
     * This method is identical in functionality to getProcessingTime(),
     * except that it exposes the execution time of the servlet's service
     * method under the JSR 77 compliant property name and type.
     *
     * @return Execution time of the servlet's service method
     */
    public TimeStatistic getServiceTime();

    /**
     * The maximum processing time of a servlet request
     * @return CountStatistic
     */
    public CountStatistic getMaxTime();
    
    /**
     * The errorCount represents the number of cases where the response 
     * code was >= 400
     * @return CountStatistic
     */
    public CountStatistic getErrorCount();
    
}
