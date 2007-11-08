
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



import com.sun.enterprise.admin.servermgmt.InstanceException;
import com.sun.enterprise.admin.servermgmt.RuntimeStatusList;

import javax.management.MBeanException;


/**
 * Interface StatusConfigMBean
 *
 *
 * @author
 * @version %I%, %G%
 */
public interface StatusConfigMBean {

    /**
     * Method clearStatus clears the runtime status of the specified target. This
     * involves clearing the recent error list of the server instances and
     * node agents in the target.  
     *
     * @param targetName The target can be one of the following: "domain" -- 
     * clear the status of all node agents and server instances in the domain, 
     * node-agent-name -- clear the runtime status of the specified node agent,
     * cluster-name -- clear the runtime status of all servers in the specified
     * cluster, server-name -- clear the runtime status of the specified server
     * instance.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    void clearStatus(String targetName)
        throws InstanceException, MBeanException;

    /**
     * Method getStatus get the runtime status of the all instances and node
     * agents in the specified target.
     *
     * @param targetName The target can be one of the following: "domain" -- 
     * get the status of all node agents and server instances in the domain, 
     * node-agent-name -- get the runtime status of the specified node agent,
     * cluster-name -- get the runtime status of all servers in the specified
     * cluster, server-name -- get the runtime status of the specified server
     * instance.
     *
     * @return A list of runtime status for each node-agent and instance in the
     * target.
     *
     * @throws InstanceException
     * @throws MBeanException
     *
     */
    RuntimeStatusList getStatus(String targetName)
        throws InstanceException, MBeanException;
}


/*--- Formatted in Sun Java Convention Style on Mon, Mar 15, '04 ---*/


/*------ Formatted by Jindent 3.0 --- http://www.jindent.de ------*/
