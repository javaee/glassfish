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

package com.sun.appserv.connectors.spi;

import org.jvnet.hk2.annotations.Contract;

import javax.naming.NamingException;
import java.util.Hashtable;
import java.util.Collection;



@Contract
public interface ConnectorRuntime {
        //TODO V3 javadoc 
        public Object createConnectionFactory(String jndiName, String moduleName, String poolName, Hashtable env);
        public void createActiveResourceAdapter(String sourcePath, String moduleName) throws ConnectorRuntimeException;
        public void destroyActiveResourceAdapter(String moduleName, boolean cascade) throws ConnectorRuntimeException;
        public boolean checkAndLoadResource(Object resource, Object pool, String resourceType, String resourceName,
                                                String raName) throws ConnectorRuntimeException;
        public void shutdownAllActiveResourceAdapters(Collection<String> poolNames, Collection<String> resourceNames);
        public void destroyResourcesAndPools(Collection resources, Collection pools);

        /**
         * Does lookup of non-tx-datasource. If found, it will be returned.<br><br>
         *
         * If not found and <b>force</b> is true,  this api will try to get a wrapper datasource specified
         * by the jdbcjndi name. The motivation for having this
         * API is to provide the CMP backend/ JPA-Java2DB a means of acquiring a connection during
         * the codegen phase. If a user is trying to deploy an JPA-Java2DB app on a remote server,
         * without this API, a resource reference has to be present both in the DAS
         * and the server instance. This makes the deployment more complex for the
         * user since a resource needs to be forcibly created in the DAS Too.
         * This API will mitigate this need.
         *
         * @param jndiName  jndi name of the resource
         * @param force provide the resource (in DAS)  even if it is not enabled in DAS
         * @return DataSource representing the resource.
         * @throws javax.naming.NamingException when not able to get the datasource.
         */
        public Object lookupNonTxResource(String jndiName, boolean force) throws NamingException;

        /**
         * Does lookup of "__pm" datasource. If found, it will be returned.<br><br>
         *
         * If not found and <b>force</b> is true, this api will try to get a wrapper datasource specified
         *  by the jdbcjndi name. The motivation for having this
         * API is to provide the CMP backend/ JPA-Java2DB a means of acquiring a connection during
         * the codegen phase. If a user is trying to deploy an JPA-Java2DB app on a remote server,
         * without this API, a resource reference has to be present both in the DAS
         * and the server instance. This makes the deployment more complex for the
         * user since a resource needs to be forcibly created in the DAS Too.
         * This API will mitigate this need.
         * When the resource is not enabled, datasource wrapper provided will not be of
         * type "__pm"
         *
         * @param jndiName  jndi name of the resource
         * @param force provide the resource (in DAS)  even if it is not enabled in DAS
         * @return DataSource representing the resource.
         * @throws javax.naming.NamingException when not able to get the datasource.
         */
        public Object lookupPMResource(String jndiName, boolean force) throws NamingException;

}
