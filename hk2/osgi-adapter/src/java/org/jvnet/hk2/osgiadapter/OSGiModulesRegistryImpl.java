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

import org.osgi.framework.BundleContext;
import com.sun.enterprise.module.*;
import com.sun.hk2.component.*;
import com.sun.hk2.component.InhabitantsParser;

import java.io.IOException;
import java.util.Collection;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiModulesRegistryImpl
        extends com.sun.enterprise.module.common_impl.AbstractModulesRegistryImpl {

    /**
     * OSGi BundleContext - used to install/uninstall, start/stop bundles
     */
    BundleContext bctx;

    /*package*/ OSGiModulesRegistryImpl(BundleContext bctx) {
        super(null);
        this.bctx = bctx;
    }

    protected void parseInhabitants(
            Module module, String name, InhabitantsParser inhabitantsParser)
            throws IOException {
        OSGiModuleImpl.class.cast(module).parseInhabitants(name, inhabitantsParser);
    }

    public ModulesRegistry createChild() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void shutdown() {
        for (Module m : modules.values()) {
            OSGiModuleImpl.class.cast(m).uninstall();
        }
        modules.clear();
    }

    /**
     * Sets the classloader parenting the class loaders created by the modules
     * associated with this registry.
     * @param parent parent class loader
     */
    public void setParentClassLoader(ClassLoader parent) {
        throw new UnsupportedOperationException("Not Yet Implemented"); // TODO
    }

    /**
     * Returns the parent class loader parenting the class loaders created
     * by modules associated with this registry.
     * @return the parent classloader
     */
    public ClassLoader getParentClassLoader() {
        throw new UnsupportedOperationException("Not Yet Implemented"); // TODO
    }

    /**
     * Returns a ClassLoader capable of loading classes from a set of modules indentified
     * by their module definition
     *
     * @param parent the parent class loader for the returned class loader instance
     * @param defs module definitions for all modules this classloader should be capable of loading
     * classes from
     * @return class loader instance
     * @throws com.sun.enterprise.module.ResolveError if one of the provided module definition cannot be resolved
     */
    public ClassLoader getModulesClassLoader(ClassLoader parent, Collection<ModuleDefinition> defs)
        throws ResolveError {
        throw new UnsupportedOperationException("Not Yet Implemented"); // TODO
    }
}
