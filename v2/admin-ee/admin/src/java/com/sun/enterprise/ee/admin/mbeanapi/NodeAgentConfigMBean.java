
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



import com.sun.enterprise.admin.servermgmt.RuntimeStatus;
import com.sun.enterprise.ee.admin.servermgmt.AgentException;

import javax.management.MBeanException;
import javax.management.ObjectName;


/**
 * Interface NodeAgentConfigMBean
 *
 *
 * @author
 * @version %I%, %G%
 */
public interface NodeAgentConfigMBean {

    /**
     * Method getRuntimeStatus fetches the runtime status of the node agent.
     *
     * @return the runtime status
     *
     * @throws AgentException
     * @throws MBeanException
     *
     */
    RuntimeStatus getRuntimeStatus() throws AgentException, MBeanException;

    /**
     * Method clearRuntimeStatus clears the runtime status of the node agent.
     * This results in the list of recent error messages being deleted.
     *     
     * @throws AgentException
     * @throws MBeanException
     *
     */
    void clearRuntimeStatus() throws AgentException, MBeanException;

    /**
     * Method delete deletes the node agent configuration of the node agent. The 
     * node agent need not be running, but all of its server instances must be 
     * deleted.
     *     
     * @throws AgentException
     * @throws MBeanException
     *
     */
    void delete() throws AgentException, MBeanException;

    /**
     * Method listServerInstancesAsString lists the server instances and optionally
     * their status (e.g. running, stopped) managed by the node agent.     
     *
     * @param andStatus true indicates that the server instances status are to be returned 
     * along with their names.
     *
     * @return the list of server instances managed by the node agent optionally with
     * their status.
     *
     * @throws AgentException
     * @throws MBeanException
     *
     */
    public String[] listServerInstancesAsString(boolean andStatus)
        throws AgentException, MBeanException;

    /**
     * Method listServerInstances returns the JMX object names of the server 
     * instances managed by the node agent. A zero length list is returned
     * if there are no instances.
     *
     * @return JMX object names of the servers managed by this node agent.
     *
     * @throws AgentException
     * @throws MBeanException
     *
     */
    public ObjectName[] listServerInstances()
        throws AgentException, MBeanException;
}


/*--- Formatted in Sun Java Convention Style on Tue, Mar 16, '04 ---*/


/*------ Formatted by Jindent 3.0 --- http://www.jindent.de ------*/
