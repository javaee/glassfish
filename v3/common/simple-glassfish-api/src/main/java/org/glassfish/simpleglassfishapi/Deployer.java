/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.simpleglassfishapi;

import java.io.File;
import java.net.URI;
import java.util.Map;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public interface Deployer {

    /**
     * Deploys a jar file or an exploded directory to {@link GlassFish}.
     *
     * Example : deployer.deploy("myapp.war");
     * 
     * @param archive jar file or directory of the application
     * @return the deployed application name
     */
    String deploy(File archive);

    /**
     * Deploys a jar file or an exploded directory to the server using the supplied deployment command parameters.
     *
     * @param archive jar file or directory of the application
     * @param params deployment command parameters
     * @return the deployed application name
     */
    String deploy(File archive, Map<String, String> params);

    // TODO(Sahoo): Add more documentation about how to use 
    /**
     * Deploys an application identified by a URI. Please note, there is no separate deployment parameters
     * in this method signature. All the information is encapsulated in the URI as query components.
     * We prefer this approach as opposed to taking a separate properties argument, because one can then
     * easily deploy from an interactive shell by encoding everything as one URI string.
     * GlassFish does not care about what URI scheme is used as long as there is a URL handler installed
     * in the server runtime to handle the URI scheme and a JarInputStream can be obtained from the URI.
     *  
     * @param archive
     * @return
     */
    String deploy(URI archive);

    /**
     * Undeploys an application from {@link GlassFish}
     *
     * Example : deployer.undeploy("myapp");
     *
     * @param appName Identifier of the application to be undeployed.
     *
     */
    void undeploy(String appName);

    /**
     * Undeploys an application from {@link GlassFish} using the supplied
     * undeployment command parameters.
     *
     * Example:
     *
     *      Map&lt;String, String>&gt params = new HashMap();
     *      param.put("externally-managed", "true");
     *      deployer.undeploy("myapp", params);
     *
     * @param appName Identifier of the application to be undeployed.
     * @param params Undeployment parameters.
     */
    void undeploy(String appName, Map<String, String> params);
}
