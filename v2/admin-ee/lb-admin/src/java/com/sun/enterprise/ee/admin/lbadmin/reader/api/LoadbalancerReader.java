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
package com.sun.enterprise.ee.admin.lbadmin.reader.api;

import com.sun.enterprise.config.ConfigContext;
/**
 * Reader class to get information about load balancer configuration.
 *
 * @author Satish Viswanatham
 */
public interface LoadbalancerReader extends BaseReader {

    /**
     * Returns properties of the load balancer.
     * For example response-timeout-in-seconds, reload-poll-interval-in-seconds
     * and https-routing etc.
     *
     * @return PropertyReader[]     array of properties
     */
    public PropertyReader[] getProperties() throws LbReaderException;

    /**
     * Returns the cluster info that are load balanced by this LB.
     *
     * @return ClusterReader        array of cluster readers
     */
    public ClusterReader[] getClusters() throws LbReaderException;

    /**
     * Returns the name of the load balancer
     *
     * @return String               name of the LB
     */
    public String getName() throws LbReaderException; 

    /*** Supported Attribute names for Load balancer **/

    public static final String RESP_TIMEOUT = "response-timeout-in-seconds";

    public static final String RELOAD_INTERVAL =
                                    "reload-poll-interval-in-seconds";
    
    public static final String HTTPS_ROUTING = "https-routing";

    public static final String REQ_MONITOR_DATA = "require-monitor-data";
    
    public static final String ROUTE_COOKIE = "route-cookie-enabled";
 
    public static final String LAST_EXPORTED = "last-exported";

    public static final String ACTIVE_HEALTH_CHECK_VALUE ="false";
    
    public static final String NUM_HEALTH_CHECK_VALUE = "3";
 
    public static final String REWRITE_LOCATION_VALUE = "true";

    public static final String ACTIVE_HEALTH_CHECK="active-healthcheck-enabled";
    
    public static final String NUM_HEALTH_CHECK = "number-healthcheck-retries";
 
    public static final String REWRITE_LOCATION = "rewrite-location";
}
