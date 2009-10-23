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
package org.glassfish.extras.osgicontainer;

import org.glassfish.api.deployment.archive.*;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.internal.deployment.GenericHandler;
import org.glassfish.internal.api.DelegatingClassLoader;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PreDestroy;
import org.jvnet.hk2.component.Singleton;

import java.io.IOException;
import java.io.File;
import java.util.jar.Manifest;
import java.util.*;
import java.net.URL;
import java.net.URI;
import java.lang.ref.WeakReference;

import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.common_impl.DefaultModuleDefinition;
import com.sun.enterprise.util.io.FileUtils;

/**
 * Archive Handler for OSGi modules.
 *
 * @author Jerome Dochez
 */
@Service(name="osgi")
@Scoped(Singleton.class)
public class OSGiArchiveHandler extends GenericHandler implements CompositeHandler {
    
    @Inject
    ModulesRegistry mr;

    public String getArchiveType() {
        return "osgi";
    }

    public boolean accept(ReadableArchive source, String entryName) {
        // we hide everything so far.
        return false;
    }

    public boolean handles(ReadableArchive archive) throws IOException {
        Manifest manifest = getManifest(archive);
        return manifest!=null && ((manifest.getMainAttributes().getValue("Bundle-Name")!=null)
            || manifest.getMainAttributes().getValue("Bundle-SymbolicName")!=null);
    }

    private Map<Module, WeakReference<RefCountingClassLoader>> loaders = new HashMap<Module, WeakReference<RefCountingClassLoader>>();

    // I would love to use PhantomReferences but there is an asynchronicity in its behavior that
    // would clash with deploy/undeploy/redeploy cycles that need a complete clean up of the system.
    public synchronized RefCountingClassLoader getClassLoader(ClassLoader parent, Module m) {

        assert(m!=null);
        WeakReference<RefCountingClassLoader> ref = loaders.get(m);
        if (ref!=null) {
            if (ref.get()!=null) {
                RefCountingClassLoader loader = ref.get();
                loader.increment();
                return loader;
            }
        }
        // time to create one...
        RefCountingClassLoader cl = new RefCountingClassLoader(parent, m);
        ref = new WeakReference<RefCountingClassLoader>(cl);
        loaders.put(m, ref);
        cl.increment();
        return cl;
    }    

    public ClassLoader getClassLoader(ClassLoader parent, DeploymentContext context) {

        //File source = new File(context.getSourceDir(), context.getSourceDir().list()[0]);
        ModuleDefinition moduleDef = null;
        try {
            moduleDef = new DefaultModuleDefinition(context.getSourceDir(), null);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        context.getAppProps().put("module-name", moduleDef.getName());

        // add the new module or retrieve the existing one.
        final Module module = mr.add(moduleDef);

        if (module==null) {
            throw new RuntimeException("Cannot install bundle " + context.getSource() + " in osgi runtime");
        }
            return getClassLoader(parent, module);
        /*
        // we need to protect the class loader so we stop the bundle from the OSGi runtime
        // when the classloader is gced is stopped. Eventually the ApplicationContainer start/stop
        // will maintain the state of the application therefore of the classloader.
        class ProtectedDelegatingCL extends DelegatingClassLoader implements PreDestroy {

            public ProtectedDelegatingCL(ClassLoader parent, List<ClassFinder> delegates) throws IllegalArgumentException {
                super(parent, delegates);
            }

            public void preDestroy() {
                module.stop();
            }
        }

        List<DelegatingClassLoader.ClassFinder> finders = new ArrayList<DelegatingClassLoader.ClassFinder>();
        finders.add(new ProtectedClassLoader(parent, module.getClassLoader()));
        return new ProtectedDelegatingCL(parent, finders);

         */
    }

    public static class ProtectedClassLoader implements DelegatingClassLoader.ClassFinder {

        final ClassLoader parent;
        final ClassLoader delegate;

        public ProtectedClassLoader(ClassLoader parent, ClassLoader delegate) {
            this.parent = parent;
            this.delegate = delegate;
        }

        public ClassLoader getParent() {
            return parent;
        }

        public Class<?> findClass(String name) throws ClassNotFoundException {
            return delegate.loadClass(name);
        }

        public Class<?> findExistingClass(String name) {
            try {
                return delegate.loadClass(name);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        public URL findResource(String name) {
            return delegate.getResource(name);
        }

        public Enumeration<URL> findResources(String name) throws IOException {
            return delegate.getResources(name);
        }
    }

}
