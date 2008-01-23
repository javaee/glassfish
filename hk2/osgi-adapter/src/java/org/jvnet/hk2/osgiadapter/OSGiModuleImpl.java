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


package org.jvnet.hk2.osgiadapter;

import static org.jvnet.hk2.osgiadapter.Logger.logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleEvent;
import com.sun.hk2.component.Holder;
import com.sun.hk2.component.InhabitantsParser;
import com.sun.enterprise.module.ModuleMetadata;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.ModuleState;
import com.sun.enterprise.module.ResolveError;
import com.sun.enterprise.module.ModuleChangeListener;
import com.sun.enterprise.module.ModuleDependency;

import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.List;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiModuleImpl implements Module {
    private Bundle bundle;

    private ModuleDefinition md;

    private ModulesRegistry registry;

    public OSGiModuleImpl(ModulesRegistry registry, Bundle bundle, ModuleDefinition md) {
        this.registry = registry;
        this.bundle = bundle;
        this.md = md;
    }

    public ModuleDefinition getModuleDefinition() {
        return md;
    }

    public String getName() {
        return md.getName();
    }

    public ModulesRegistry getRegistry() {
        return registry;
    }

    public ModuleState getState() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void resolve() throws ResolveError {
        // TODO
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void start() throws ResolveError {
        try {
            bundle.start();
        } catch (BundleException e) {
            throw new ResolveError(e);
        }
    }

    public synchronized boolean stop() {
        if ((bundle.getState() & BundleEvent.STARTED) != 0) {
            return false;
        }
        try {
            bundle.stop();
        } catch (BundleException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public void uninstall() {
        try {
            bundle.uninstall();
        } catch (BundleException e) {
            throw new RuntimeException(e);
        }
    }

    public void detach() {
        // TODO
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void refresh() {
        // TODO
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public ModuleMetadata getMetadata() {
        return md.getMetadata();
    }

    public <T> Iterable<Class<? extends T>> getProvidersClass(
            Class<T> serviceClass) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Iterable<Class> getProvidersClass(String name) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean hasProvider(Class serviceClass) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addListener(ModuleChangeListener listener) {
        // TODO
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void removeListener(ModuleChangeListener listener) {
        // TODO
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void dumpState(PrintStream writer) {
        // TODO
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Parses all the inhabitants descriptors of the given name in this module.
     */
    /* package */ void parseInhabitants(String name, InhabitantsParser parser) throws IOException {
        Holder<ClassLoader> holder = new Holder<ClassLoader>() {
            public ClassLoader get() {
                return new ClassLoader() {
                    @Override public synchronized Class<?> loadClass(
                            String name) throws ClassNotFoundException {
                        final Class aClass = bundle.loadClass(name);
                        logger.logp(Level.INFO, "ModuleImpl", "loadClass",
                                name+".class.getClassLoader() = {0}",
                                aClass.getClassLoader());
                        if ((bundle.getState() & BundleEvent.STARTED) == 0) {
                            try {
                                bundle.start();
                                logger.logp(Level.INFO, "ModuleImpl",
                                        "loadClass", "Started bundle {0}", bundle);
                            } catch (BundleException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        return aClass;
                    }
                };
            }
        };

        for (ModuleMetadata.InhabitantsDescriptor d : md.getMetadata().getHabitats(name))
            parser.parse(d.createScanner(),holder);
    }

    public ClassLoader getClassLoader() {
        throw new UnsupportedOperationException("Not Yet Implemented"); // TODO
    }

    public void addImport(Module module) {
        throw new UnsupportedOperationException("Not Yet Implemented"); // TODO
    }

    public Module addImport(ModuleDependency dependency) {
        throw new UnsupportedOperationException("Not Yet Implemented"); // TODO
    }

    public boolean isSticky() {
        throw new UnsupportedOperationException("Not Yet Implemented"); // TODO
    }

    public void setSticky(boolean sticky) {
        throw new UnsupportedOperationException("Not Yet Implemented"); // TODO
    }

    public List<Module> getImports() {
        throw new UnsupportedOperationException("Not Yet Implemented"); // TODO
    }

    public boolean isShared() {
        throw new UnsupportedOperationException("Not Yet Implemented"); // TODO
    }
}
