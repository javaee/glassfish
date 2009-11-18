/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.admin.rest;

import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.core.ResourceConfig;
import org.glassfish.api.container.EndpointRegistrationException;
import com.sun.grizzly.tcp.Adapter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import java.util.Set;
import org.glassfish.internal.api.ServerContext;

/**
 *
 * @author ludovic champenois ludo@dev.java.net
 */
/**
 * Class that initialize the Jersey container. It is called via introspection from RestAdapter
 * so that RestAdapter does not depend on Jersey classes. This way, we gain 90ms at startup time
 * and load the jersey classes only at the very last time, when needed.
 * @author ludo
 */
public class LazyJerseyInit {

    LazyJerseyInit() {
    }

    /**
     * Called via introspection in the RestAdapter service() method only when the GrizzlyAdapter is not initialized
     * @param classes set of Jersey Resources classes
     * @param sc the current ServerContext, needed to finf the correct classpath
     * @return the correct GrizzlyAdapter
     * @throws EndpointRegistrationException
     */
    public static GrizzlyAdapter exposeContext(Set classes, ServerContext sc)
            throws EndpointRegistrationException {

        Adapter adapter = null;

        ResourceConfig rc = new DefaultResourceConfig(classes);

        //Use common classloader. Jersey artifacts are not visible through
        //module classloader
        ClassLoader originalContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader apiClassLoader = sc.getCommonClassLoader();
            Thread.currentThread().setContextClassLoader(apiClassLoader);
            adapter = ContainerFactory.createContainer(com.sun.grizzly.tcp.Adapter.class, rc);
        } finally {
            Thread.currentThread().setContextClassLoader(originalContextClassLoader);
        }

        return (GrizzlyAdapter) adapter;
    }
}
