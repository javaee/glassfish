/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
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


package com.sun.enterprise.v3.server;

import org.glassfish.internal.api.DelegatingClassLoader;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;

/**
 * We support two policies:
 * 1. All standalone RARs are available to all other applications. This is the
 * Java EE 5 specific behavior.
 * 2. An application has visbility to only those standalone RARs that it
 * depends on. This is the new behavior defined in Java EE 6 as well as
 * JCA 1.6 spec. See https://glassfish.dev.java.net/issues/show_bug.cgi?id=5380
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
@Service
public class ConnectorClassLoaderServiceImpl implements PostConstruct{

    /*
    * TODO(Sahoo):
    * 1. https://glassfish.dev.java.net/issues/show_bug.cgi?id=5380
    * 3. Decide whether we can retrieve all the desired information from
    * ApplicationsRegistry by using just the name of the application. If not,
    * then revisit the signature of getConnectorClassLoader(String appName)
    *
    * COMPLETED
    * 2. Listen to standalone RAR lifecycle events and add or remove
    * corresponding classloader from this chain.
    */

    @Inject
    CommonClassLoaderServiceImpl ccls;

    /**
     * This class loader is used when we have just a single connector
     * class loader for all applications. In other words, we make every
     * standalone RARs available to all applications.
     */
    DelegatingClassLoader globalConnectorCL;

    public void postConstruct() {
        globalConnectorCL =
                new DelegatingClassLoader(ccls.getCommonClassLoader());
    }

    /**
     * provides connector-class-loader for the specified application
     * If application is null, global connector class loader will be provided
     * @param application application-name
     * @return class-loader
     */
    public DelegatingClassLoader getConnectorClassLoader(String application) {
         DelegatingClassLoader loader = null;

        //if(application == null){
            assert (globalConnectorCL != null);
            loader = globalConnectorCL;
        //}else{
            //TODO V3   need to maintain a rar->class-finder mapping such that
            //TODO V3   applications can refer to only required .rars
        //}
        return loader;
    }
}
