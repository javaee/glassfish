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

package com.sun.enterprise.admin.mbeans;

import javax.management.ObjectName;
import javax.management.MBeanServerConnection;

/**
 * Provides for determining the state of servers within the
 * scope of this domain
 *
 * @author Sreenivas Munnangi
 */

public interface DomainStatusMBean {

    /**
     * attribute name for state
     */
    public static final String DOMAIN_STATUS_STATE = "state";

    /**
     * name of this mbean
     */
    public static final String DOMAIN_STATUS_PROPS = "name=domain-status";

    /**
     * type of the Notification emitted when the status of a server changes
     */
    public final String SERVER_STATUS_NOTIFICATION_TYPE = 
	"com.sun.appserv.management.status.ServerStatusChanged";

    /**
     * key within the Notification's Map of type
     * SERVER_STATUS_NOTIFICATION_TYPE which yields the serverName
     */
    public final String SERVER_NAME_KEY = "ServerName";

    // methods

    /**
     * get the current state of the given server
     * @return Integer the current value of the state
     */
    public int getstate(String serverName) throws Exception;
    
    /**
     * set the current state of the given server
     */
    public void setstate(String serverName, Integer state) throws Exception;
    
    /**
     * get the mBean server connection for the given server
     */
    public MBeanServerConnection getServerMBeanServerConnection(String serverName) throws Exception;
    
}
