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
 * Copyright 2005-2006 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.mfwk.agent.appserv.discovery;

import java.util.Set;
import javax.management.ObjectName;

public interface CMMMBeanDiscoveryService {
    
    /**
     * Returns a Collection of all CMM mbeans instrumented in 
     * a given mbean server
     *
     * @return available CMM mbeans
     */
    public Set discoverCMMMBeans() throws Exception;
    
    /**
     * Returns a Collection of all CMM mbeans instrumented in 
     * a given mbean server that are child of the given mbean 
     *
     * @param  objectName the given mbean object name 
     * @return the set of <code>ObjectName</code> of all the 
     *         mbeans that are child of the given mbean. Returned
     *         set also includes the given object name. If the
     *         mbean represented by the given object name does not
     *         have any childeren, then the returned set will have
     *         only one object name,that is the given object name.
     */
    public Set discoverCMMMBeans(ObjectName objectName) throws Exception;

    /**
     * Returns a collection of CMM mbeans instrumented for this 
     * given server instance. 
     *
     * @param  serverName  name of a application server instance
     * @param  domainName  name of a application server domain
     *
     * @return available CMM mbeans
     */
    public Set discoverCMMMBeans(String serverName, String domainName) 
            throws Exception;
    
    /**
     * Returns a collection of CMM mbeans corresponding to a cluster
     */
    public Set discoverClusterCMMMBeans() throws Exception;
    
    /**
     * Returns a registered CMM ObjectName,if any with the given name.
     *
     * @param the name string
     * @return CMM ObjectName or null.
     */
     public ObjectName discoverCMMMBean(String name) throws Exception;

    /**
     * Returns a collection of InstalledProduct mbeans 
     */
    public Set discoverInstalledProductCMMMBeans() throws Exception;
}
