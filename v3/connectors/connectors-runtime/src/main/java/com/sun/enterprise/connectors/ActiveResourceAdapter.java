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

package com.sun.enterprise.connectors;

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.enterprise.deployment.ConnectorDescriptor;

import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;

import org.jvnet.hk2.annotations.Contract;


/**
 * Interface class for different types (1.0 and 1.5 complient) resource
 * adapter abstraction classes.
 * Containes methods for setup(initialization), destroy and creation of MCF.
 *
 * @author Srikanth P and Binod PG
 */

@Contract
public interface ActiveResourceAdapter {

    public void init(ResourceAdapter ra, ConnectorDescriptor cd, String moduleName, ClassLoader loader)
            throws ConnectorRuntimeException;

    /**
     * It initializes the resource adapter.
     *
     * @throws ConnectorRuntimeException This exception is thrown if the
     *                                   setup/initialization fails.
     */

    public void setup() throws ConnectorRuntimeException;

    /**
     * uninitializes the resource adapter.
     */

    public void destroy();

    /**
     * Returns the Connector descriptor which represents/holds ra.xml
     *
     * @return ConnectorDescriptor Representation of ra.xml.
     */

    public ConnectorDescriptor getDescriptor();

    public boolean handles(ConnectorDescriptor desc);

    public ManagedConnectionFactory[] createManagedConnectionFactories
            (ConnectorConnectionPool cpr, ClassLoader loader);

    /**
     * Creates managed Connection factory instance.
     *
     * @param ccp    Connector connection pool which contains the pool properties
     *               and ra.xml values pertaining to managed connection factory
     *               class. These values are used in MCF creation.
     * @param loader Classloader used to managed connection factory class.
     * @return ManagedConnectionFactory created managed connection factory
     *         instance
     */

    public ManagedConnectionFactory createManagedConnectionFactory
            (ConnectorConnectionPool ccp, ClassLoader loader);

    /**
     * Returns the class loader that is used to load the RAR.
     *
     * @return <code>ClassLoader</code> object.
     */
    public ClassLoader getClassLoader();

    /**
     * Returns the module Name of the RAR
     *
     * @return A <code>String</code> representing the name of the
     *         connector module
     */
    public String getModuleName();
}
