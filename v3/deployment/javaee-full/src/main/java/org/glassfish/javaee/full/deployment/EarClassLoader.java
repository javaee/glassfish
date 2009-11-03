/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.javaee.full.deployment;

import java.util.*;

import com.sun.enterprise.loader.ASURLClassLoader;
import org.jvnet.hk2.component.PreDestroy;

/**
 * Simplistic class loader which will delegate to each module class loader in the order
 * they were added to the instance
 *
 * @author Jerome Dochez
 */
public class EarClassLoader extends ASURLClassLoader
{

    private List<ClassLoaderHolder> moduleClassLoaders = new LinkedList<ClassLoaderHolder>();
    boolean isPreDestroyCalled = false;

    public EarClassLoader(ClassLoader classLoader) {
        super(classLoader); 
    }

    public void addModuleClassLoader(String moduleName, ClassLoader cl) {
        moduleClassLoaders.add(new ClassLoaderHolder(moduleName, cl));
    }

    public ClassLoader getModuleClassLoader(String moduleName) {
        for (ClassLoaderHolder clh : moduleClassLoaders) {
            if (moduleName.equals(clh.moduleName)) {
                return clh.loader;
            }
        }
        return null;
    }

    @Override
    public void preDestroy() {
        if (isPreDestroyCalled) {
            return;
        }

        try {
            for (ClassLoaderHolder clh : moduleClassLoaders) {
                // destroy all the module classloaders
                if ( !(clh.loader instanceof EarLibClassLoader) &&  
                     !(clh.loader instanceof EarClassLoader)) {
                    try {
                        PreDestroy.class.cast(clh.loader).preDestroy();
                    } catch (Exception e) {
                        // ignore, the class loader does not need to be 
                        // explicitely stopped.
                    }
                }
            }

            // destroy itself
            super.preDestroy();
 
            // now destroy the EarLibClassLoader
            PreDestroy.class.cast(this.getParent().getParent()).preDestroy();

            moduleClassLoaders = null;
        } catch (Exception e) {
            // ignore, the class loader does not need to be explicitely stopped.
        }

        isPreDestroyCalled = true;
    }

    private class ClassLoaderHolder {
        final ClassLoader loader;
        final String moduleName;

        private ClassLoaderHolder(String moduleName, ClassLoader loader) {
            this.loader = loader;
            this.moduleName = moduleName;
        }
    }

    @Override
    protected String getClassLoaderName() {
        return "EarClassLoader";
    }
}
