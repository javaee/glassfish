/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.glassfish.bootstrap;

import java.util.Collection;
import java.net.URLClassLoader;
import java.net.URL;

/**
 * {@link ClassLoader} that masks a specified set of classes
 * from its parent class loader.
 *
 * <p>
 * This code is used to create an isolated environment.
 *
 * @author Kohsuke Kawaguchi
 */
public class MaskingClassLoader extends ClassLoader {

    private final String[] masks;
    private final URLClassLoader delegate;
    

/*    public MaskingClassLoader(String... masks) {
        this.masks = masks;
    }

    public MaskingClassLoader(Collection<String> masks) {
        this(masks.toArray(new String[masks.size()]));
    }
*/
    public MaskingClassLoader(ClassLoader parent, URL[] urls, String... masks) {
        super(parent);
        this.delegate = new URLClassLoader(urls, getMaskingClassLoader(masks));
        this.masks = masks;
    }

    public MaskingClassLoader(ClassLoader parent, URL[] urls, Collection<String> masks) {
        this(parent, urls, masks.toArray(new String[masks.size()]));
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {

        try {
            for (String mask : masks) {
                if (name.startsWith(mask)) {
                    Class c = delegate.loadClass(name);
                    return c;
                }
            }
        } catch(ClassNotFoundException e) {

        }
        return super.loadClass(name, resolve);
/*        for (String mask : masks) {
            if(name.startsWith(mask))
                delegate.loadClass(name);
        }

        return super.loadClass(name, resolve);
        */
     }

    public ClassLoader getMaskingClassLoader(final String... masks) {
        return new ClassLoader() {
            @Override
            protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                for (String mask : masks) {
                    if (name.startsWith(mask)) {
                        throw new ClassNotFoundException(name);
                    }
                }
                return super.loadClass(name, resolve);
            };

        };
    }
}
