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
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
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

package org.glassfish.experimentalgfapi;

import java.util.Properties;
import java.util.ServiceLoader;

/**
 * This is the entry point API to bootstrap GlassFish. We don't call it a ServerRuntime or Server,
 * because, we this can be used to boostrap just an App Client Container environment.
 * A GlassFishRuntime represents just the runtime environment, i.e., no active services yet. e.g.,
 * there won't be any web container started just by creating a GlassFishRuntime object. For that, one
 * has to call {@link GlassFish#start}.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public abstract class GlassFishRuntime {

    /**
     * Singleton
     */
    private static GlassFishRuntime me;
    private static RuntimeBuilder runtimeBuilder;

    /**
     * Bootstrap GlassFish runtime based on runtime configuration passed in the properties object.
     * Calling this method multiple times has no effect.
     *
     * @param properties properties used to setup the runtime
     * @param cl         ClassLoader used as parent loader by GlassFish modules. If null is passed, the class loader
     *                   of this class is used.
     * @return a GlassFishRuntime
     * @throws Exception
     */
    public synchronized static GlassFishRuntime bootstrap(Properties properties, ClassLoader cl) throws Exception {
        if (me != null) return me;
        runtimeBuilder = getRuntimeBuilder(properties, cl != null ? cl : GlassFishRuntime.class.getClassLoader());
        me = runtimeBuilder.build(properties);
        return me;
    }

    public synchronized static void shutdown() throws Exception {
        runtimeBuilder.destroy();
        me = null;
    }

    public synchronized static GlassFishRuntime get() {
        if (me == null) {
            throw new RuntimeException("Not yet bootstrapped");
        }
        return me;
    }

    public abstract GlassFish newGlassFish(Properties properties) throws Exception;

    private static RuntimeBuilder getRuntimeBuilder(Properties properties, ClassLoader cl) {
//        StringBuilder sb = new StringBuilder("Launcher Class Loader = " + cl);
//        if (cl instanceof URLClassLoader) {
//            sb.append("has following Class Path: ");
//            for (URL url : URLClassLoader.class.cast(cl).getURLs()) {
//                sb.append(url).append(", ");
//            }
//        }
//        System.out.println(sb);
        ServiceLoader<RuntimeBuilder> runtimeBuilders = ServiceLoader.load(RuntimeBuilder.class, cl);
        for (RuntimeBuilder builder : runtimeBuilders) {
            if (builder.handles(properties)) {
                return builder;
            }
        }
        throw new RuntimeException("No runtime builder for this configuration: " + properties);
    }

    /**
     * Internal interface. Not for public use.
     * This is an SPI for GlassFishRuntime. Different implementations exist to provide different runtime
     * enviornment such as Felix/Equinox based or non-OSGi based runtime.
     */
    public interface RuntimeBuilder {
        GlassFishRuntime build(Properties properties) throws Exception;

        boolean handles(Properties properties);

        void destroy() throws Exception;
    }

}
