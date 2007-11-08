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

package com.sun.enterprise.ee.admin.mbeanapi;

import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.admin.servermgmt.InstanceException;

/**
 * object name for this mbean: <domainName>:type=ejb-timer-management,category=config
 * EJBTimerManagementMBean provide functionality to list timers and migrate timers.
 *
 * @author sridatta
 *
 */
public interface EJBTimerManagementMBean 
{
	
	/**
         * migrateTimers method is used to migrate ejb timers associated with 
         * a server (that has probably stopped or failed abnormally) to another server
         *
         * @param sourceServerName The name of the server from where ejbTimers
         *                         need to be migrated from. sourceServerName has 
         *                         to be a server which is part of a cluster. 
         * 			   Otherwise an ConfigException is thrown. 
         *
         * @ param destServerName The name of server to which the ejbTimers
         *                        need to be migrated to. destServerName can
         *                        be null. If destServerName is null, one alive 
         *                        server is picked randomly from the cluster 
         *                        that it belongs to. If the migration fails for 
         *                        that server, error is returned to users and 
         *                        migration is NOT initialized on other servers. 
         *
         * @exception ConfigException is thrown when there are no servers to
         *                            migrate to or the sourceServer is not part 
         *                            of a cluster or if sourceServer is alive
         *
         * @exception InstanceException is thrown when there is an error during
         *                              migration.
         */
    void migrateTimers(String sourceServerName, String destServerName)
            throws InstanceException, ConfigException;

    
    /**
     * Lists ALL the ejb timers owned by the target--a standalone 
     * server instance or an instance in a cluster or a cluster.
     *
     * @param target. If target is a server instance then the all timers
     *                owned by that instance will be listed.
     *
     *                If the target is a cluster, then all timers owned by 
     *                each instance in the cluster will be listed.
     *
     * exception ConfigException 
     * exception InstanceException 
     *
     * @return returns the list of timers as a string array.
     * 
     */
    String[] listTimers(String target) 
		throws ConfigException, InstanceException;
}
