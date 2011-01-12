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

package org.glassfish.osgiejb;

import org.glassfish.osgijavaeebase.OSGiDeploymentContext;
import org.glassfish.osgijavaeebase.BundleClassLoader;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.OpsParams;
import org.glassfish.api.deployment.InstrumentableClassLoader;
import org.osgi.framework.Bundle;

import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;

import com.sun.enterprise.module.common_impl.CompositeEnumeration;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiEJBDeploymentContext extends OSGiDeploymentContext {

    public OSGiEJBDeploymentContext(ActionReport actionReport, Logger logger, ReadableArchive source, OpsParams params, ServerEnvironment env, Bundle bundle) throws Exception {
        super(actionReport, logger, source, params, env, bundle);
    }

    protected void setupClassLoader() throws Exception {
        final BundleClassLoader delegate1 = new BundleClassLoader(bundle);
        final ClassLoader delegate2 =
                Globals.get(ClassLoaderHierarchy.class).getAPIClassLoader();

        ClassLoader cl = new DelegatingInstrumentableClassLoader(delegate1, delegate2);
        
        shareableTempClassLoader = cl;
        finalClassLoader = cl;
    }

    private static class DelegatingInstrumentableClassLoader extends ClassLoader implements InstrumentableClassLoader {

        private BundleClassLoader delegate1;
        private ClassLoader delegate2;

        private DelegatingInstrumentableClassLoader(BundleClassLoader delegate1, ClassLoader delegate2) {
            this.delegate1 = delegate1;
            this.delegate2 = delegate2;
        }

        @Override
        protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            Class c = findLoadedClass(name);
            if (c == null) {
                try {
                    return delegate1.loadClass(name, resolve);
                } catch (ClassNotFoundException cnfe) {
                    return delegate2.loadClass(name);
                }
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }

        @Override
        public URL getResource(String name) {
            URL url = delegate1.getResource(name);
            if (url == null) {
                url = delegate2.getResource(name);
            }
            return url;
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException {
            List<Enumeration<URL>> enumerators = new ArrayList<Enumeration<URL>>();
            enumerators.add(delegate1.getResources(name));
            enumerators.add(delegate2.getResources(name));
            return new CompositeEnumeration(enumerators);
        }

        public ClassLoader copy() {
            // do nothing, since we don't expect any transformation to take place because of the way we implement
            // our JPA support. We actually do static enhancement.
            return this;
        }

        public void addTransformer(ClassFileTransformer transformer) {
            System.out.println("addTransformer called " + transformer);
            // do nothing, since we don't expect any transformation to take place because of the way we implement
            // our JPA support. We actually do static enhancement.
        }
    }
}
