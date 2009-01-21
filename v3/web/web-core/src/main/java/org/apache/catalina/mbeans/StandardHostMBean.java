/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.apache.catalina.mbeans;


import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.RuntimeOperationsException;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Logger;
import org.apache.catalina.Realm;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import org.apache.tomcat.util.modeler.BaseModelMBean;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.tomcat.util.modeler.ManagedBean;
import org.glassfish.web.valve.GlassFishValve;

/**
 * <p>A <strong>ModelMBean</strong> implementation for the
 * <code>org.apache.catalina.core.StandardHost</code> component.</p>
 *
 * @author Amy Roh
 * @version $Revision: 1.3 $ $Date: 2006/03/12 01:27:04 $
 */

public class StandardHostMBean extends BaseModelMBean {

    /**
     * The <code>MBeanServer</code> for this application.
     */
    private static MBeanServer mserver = MBeanUtils.createServer();

    // ----------------------------------------------------------- Constructors


    /**
     * Construct a <code>ModelMBean</code> with default
     * <code>ModelMBeanInfo</code> information.
     *
     * @exception MBeanException if the initializer of an object
     *  throws an exception
     * @exception RuntimeOperationsException if an IllegalArgumentException
     *  occurs
     */
    public StandardHostMBean()
        throws MBeanException, RuntimeOperationsException {

        super();

    }


    // ------------------------------------------------------------- Attributes



    // ------------------------------------------------------------- Operations


   /**
     * Add an alias name that should be mapped to this Host
     *
     * @param alias The alias to be added
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public void addAlias(String alias)
        throws Exception {

        StandardHost host = (StandardHost) this.resource;
        host.addAlias(alias);

    }


   /**
     * Return the set of alias names for this Host
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String [] findAliases()
        throws Exception {

        StandardHost host = (StandardHost) this.resource;
        return host.findAliases();

    }


   /**
     * Return the MBean Names of the Valves assoicated with this Host
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public String [] getValves()
        throws Exception {

        Registry registry = MBeanUtils.createRegistry();
        StandardHost host = (StandardHost) this.resource;
        String mname = MBeanUtils.createManagedName(host);
        ManagedBean managed = registry.findManagedBean(mname);
        String domain = null;
        if (managed != null) {
            domain = managed.getDomain();
        }
        if (domain == null)
            domain = mserver.getDefaultDomain();
        GlassFishValve [] valves = host.getValves();
        String [] mbeanNames = new String[valves.length];
        for (int i = 0; i < valves.length; i++) {
            mbeanNames[i] =
                MBeanUtils.createObjectName(domain, valves[i]).toString();
        }

        return mbeanNames;

    }


   /**
     * Return the specified alias name from the aliases for this Host
     *
     * @param alias Alias name to be removed
     *
     * @exception Exception if an MBean cannot be created or registered
     */
    public void removeAlias(String alias)
        throws Exception {

        StandardHost host = (StandardHost) this.resource;
        host.removeAlias(alias);

    }


}
