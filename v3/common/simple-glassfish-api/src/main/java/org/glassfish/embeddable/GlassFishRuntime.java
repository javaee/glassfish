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

package org.glassfish.embeddable;

import org.glassfish.embeddable.spi.RuntimeBuilder;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
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
     * Calling this method twice will throw a GlassFishException
     *
     * @param options  BootstrapOptions used to setup the runtime
     * @param cl         ClassLoader used as parent loader by GlassFish modules. If null is passed, the class loader
     *                   of this class is used.
     * @return a GlassFishRuntime
     * @throws GlassFishException
     */
    public synchronized static GlassFishRuntime bootstrap(BootstrapOptions options, ClassLoader cl) throws GlassFishException {
        if (me != null)  {
         // TODO throw an exception
            return me;
        }
        runtimeBuilder = getRuntimeBuilder(options, cl != null ? cl : GlassFishRuntime.class.getClassLoader());
        me = runtimeBuilder.build(options);
        return me;
    }

    /**
     * Shuts down the Runtime and dispose off all the GlassFish objects
     * created via this Runtime
     * @throws GlassFishException
     */
    public synchronized  void shutdown() throws GlassFishException {
        disposeGlassFishInstances();
        try {
            runtimeBuilder.destroy();
            me = null;
        } catch (Exception e) {
            throw new GlassFishException(e);
        }
    }


    public synchronized static GlassFishRuntime get() {
        if (me == null) {
            throw new RuntimeException("Not yet bootstrapped");
        }
        return me;
    }
     

    /**
     *  Creates a new instance of GlassFish
     * @param option GlassFishOption used to setup the GlassFish instance
     * @return GlassFish
     * @throws GlassFishException
     */
    public abstract GlassFish newGlassFish(GlassFishOptions options) throws GlassFishException;

    /**
     * Dispose all the GlassFish instances created.
     * @return List
     */
    protected abstract void disposeGlassFishInstances();



    private static RuntimeBuilder getRuntimeBuilder(BootstrapOptions  options, ClassLoader cl) {
//        StringBuilder sb = new StringBuilder("Launcher Class Loader = " + cl);
//        if (cl instanceof URLClassLoader) {
//            sb.append("has following Class Path: ");
//            for (URL url : URLClassLoader.class.cast(cl).getURLs()) {
//                sb.append(url).append(", ");
//            }
//        }
//        System.out.println(sb);
        Iterator<RuntimeBuilder> runtimeBuilders = ServiceLoader.load(RuntimeBuilder.class, cl).iterator();
        while (runtimeBuilders.hasNext()) {
            try {
                RuntimeBuilder builder = runtimeBuilders.next();
                if (builder.handles(options)) {
                    return builder;
                }
            } catch (ServiceConfigurationError sce) {
                // Ignore the exception and move ahead to the next builder.
            }
        }
        throw new RuntimeException("No runtime builder for this configuration: " + options.getAllOptions());
    }

}
