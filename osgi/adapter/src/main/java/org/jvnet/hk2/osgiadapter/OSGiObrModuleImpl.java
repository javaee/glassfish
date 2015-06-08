/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.osgiadapter;

import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.ModuleState;
import com.sun.enterprise.module.ResolveError;
import com.sun.enterprise.module.bootstrap.BootException;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.PopulatorPostProcessor;
import org.glassfish.hk2.api.ServiceLocator;
import org.osgi.framework.Bundle;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiObrModuleImpl extends OSGiModuleImpl {

    public OSGiObrModuleImpl(OSGiObrModulesRegistryImpl registry, File file) throws IOException {
        this(registry, new OSGiModuleDefinition(file));
    }

    public OSGiObrModuleImpl(OSGiObrModulesRegistryImpl registry, ModuleDefinition moduleDef) {
        this(registry, null, moduleDef);
    }

    public OSGiObrModuleImpl(OSGiObrModulesRegistryImpl registry, Bundle bundle, ModuleDefinition moduleDef) {
        super(registry, bundle, moduleDef);
    }

    private synchronized boolean isUninitialized() {
        return getBundle() == null;
    }

    private synchronized void init() {
        if (isUninitialized()) {
            final ModuleDefinition moduleDefinition = getModuleDefinition();
            Bundle bundle = getRegistry().getObrHandler().deploy(moduleDefinition.getName(), moduleDefinition.getVersion());
            if (bundle != null) {
                setBundle(bundle);
            } else {
                throw new RuntimeException("Unable to install module [ "
                        + this
                        + "] due to unsatisfied dependencies. See previous log messages.");
            }
        }
    }

    @Override
    public OSGiObrModulesRegistryImpl getRegistry() {
        return (OSGiObrModulesRegistryImpl) super.getRegistry();
    }

    @Override
    public ModuleState getState() {
        if (isUninitialized()) {
            return ModuleState.NEW;
        }
        return super.getState();
    }

    @Override
    public void resolve() throws ResolveError {
        init();
        super.resolve();
    }

    @Override
    public void start() throws ResolveError {
        init();
        super.start();
    }

    @Override
    public boolean stop() {
        if (isUninitialized()) {
            return false;
        }
        return super.stop();
    }

    @Override
    public void detach() {
        if (isUninitialized()) {
            return;
        }
        super.detach();
    }

    @Override
    public void uninstall() {
        if (isUninitialized()) {
            return;
        }
        super.uninstall();
    }

    @Override
    public void refresh() {
        if (isUninitialized()) {
            return;
        }
        super.refresh();
    }

    @Override
    public void dumpState(PrintStream writer) {
        writer.print(toString());
    }

    @Override
    public ClassLoader getClassLoader() {
        init();
        return super.getClassLoader();
    }

    @Override
    public List<Module> getImports() {
        if (isUninitialized()) {
            return Collections.emptyList();
        }
        return super.getImports();
    }

    @Override
    List<ActiveDescriptor> parseInhabitants(String name, ServiceLocator serviceLocator, List<PopulatorPostProcessor> populatorPostProcessors) throws IOException, BootException {
        init();
        return super.parseInhabitants(name, serviceLocator, populatorPostProcessors);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("OSGiObrModuleImpl::");
        if (isUninitialized()) {
            sb.append("Name: [" + getName() + "], State: [" + getState() + "]");
            return sb.toString();
        }
        return sb.append(super.toString()).toString();
    }
}
