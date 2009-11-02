/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
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

package org.glassfish.appclient.client.acc;

import com.sun.enterprise.glassfish.bootstrap.ASMainStatic;
import com.sun.enterprise.glassfish.bootstrap.MaskingClassLoader;
import com.sun.logging.LogDomains;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tjquinn
 */
public class ACCClassLoader extends URLClassLoader {

    private static ACCClassLoader instance = null;

    private ACCClassLoader shadow = null;

    private static Logger logger = LogDomains.getLogger(ACCClassLoader.class, LogDomains.ACC_LOGGER);

    private boolean shouldTransform = false;
    
    private final List<ClassFileTransformer> transformers =
            Collections.synchronizedList(
                new ArrayList<ClassFileTransformer>());

    public static ACCClassLoader newInstance(ClassLoader parent,
            final boolean shouldTransform) {
        if (instance != null) {
            throw new IllegalStateException("already set");
        }
        instance = new ACCClassLoader(parent, shouldTransform);
        return instance;
    }

    public static ACCClassLoader instance() {
        return instance;
    }

    /**
     * Constructor invoked by the VM (because of the -Djava.system.class.loader
     * setting).
     *
     * @param parent
     */
    public ACCClassLoader(ClassLoader parent) {
        /*
         * This constructor is used by the VM to create an ACCClassLoader as
         * the system class loader (as specified by -Djava.system.class.loader
         * on the java command created from the appclient script).  
         * <p>
         * Actually create two new loaders.  One will handle the GlassFish system JARs
         * and the second will handle the application classes and anything
         * from APPCPATH.  The first new ACCClassLoader will be set as the
         * parent of the second and the second will be the one built by
         * this constructor.
         */
        super(userClassPath(),
                maskedGFSystemClassLoader(parent, logger));
        instance = this;
    }

    public static ClassLoader maskedGFSystemClassLoader(
            final ClassLoader originalParent,
            final Logger logger) {
        final File installRoot = new File(installRoot());
        final ClassLoader maskingCL = ASMainStatic.getMaskingClassLoader(
                originalParent.getParent(), installRoot, logger,
                false);
        final ClassLoader GFSystemClassLoader = new ACCClassLoader(
                GFSystemClassPath(), maskingCL);
        return GFSystemClassLoader;
    }

    private static URL[] userClassPath() {
        final URI GFSystemURI = GFSystemURI();
        final List<URL> result = classPathToURLs(System.getProperty("java.class.path"));
        for (ListIterator<URL> it = result.listIterator(); it.hasNext();) {
            final URL url = it.next();
            try {
                if (url.toURI().equals(GFSystemURI)) {
                    it.remove();
                }
            } catch (URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
        }

        result.addAll(classPathToURLs(System.getenv("APPCPATH")));

        return result.toArray(new URL[result.size()]);
    }

    private static URL[] GFSystemClassPath() {
        try {
            return new URL[] {GFSystemURI().normalize().toURL()};
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static URI GFSystemURI() {
        try {
            Class agentClass = Class.forName("org.glassfish.appclient.client.acc.agent.AppClientContainerAgent");
            return agentClass.getProtectionDomain().getCodeSource().getLocation().toURI().normalize();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static URI installRoot() {
        final URI gfModulesURI = GFSystemURI();
        return gfModulesURI.resolve("../").normalize();
    }

    private static List<URL> classPathToURLs(final String classPath) {
        if (classPath == null) {
            return Collections.EMPTY_LIST;
        }
        final List<URL> result = new ArrayList<URL>();
        try {
            for (String classPathElement : classPath.split(File.pathSeparator)) {
                result.add(new File(classPathElement).toURI().normalize().toURL());
            }
            return result;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public ACCClassLoader(ClassLoader parent, final boolean shouldTransform) {
        super(new URL[0], parent);
        this.shouldTransform = shouldTransform;
    }
//
//    public ACCClassLoader(URL[] urls) {
//        super(urls);
//    }

    public ACCClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

//    public ACCClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
//        super(urls, parent, factory);
//    }

    public synchronized void appendURL(final URL url) {
        addURL(url);
        if (shadow != null) {
            shadow.addURL(url);
        }
    }

    public void addTransformer(final ClassFileTransformer xf) {
        transformers.add(xf);
    }

    public void setShouldTransform(final boolean shouldTransform) {
        this.shouldTransform = shouldTransform;
    }

    synchronized ACCClassLoader shadow() {
        if (shadow == null) {
            shadow = new ACCClassLoader( getURLs(), getParent());
            }
        return shadow;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if ( ! shouldTransform) {
            try {
                return super.findClass(name);
            } catch (ClassNotFoundException cnfe) {
                throw cnfe;
            }
        }
        final ACCClassLoader s = shadow();
        final Class<?> c = s.findClassUnshadowed(name);
        return copyClass(c);
    }

    private Class<?> copyClass(final Class c) throws ClassNotFoundException {
        final String name = c.getName();
        final ProtectionDomain pd = c.getProtectionDomain();
        byte[] bytecode = readByteCode(name);

        for (ClassFileTransformer xf : transformers) {
            try {
                bytecode = xf.transform(this, name, null, pd, bytecode);
            } catch (IllegalClassFormatException ex) {
                throw new ClassNotFoundException(name, ex);
            }
        }
        return defineClass(name, bytecode, 0, bytecode.length, pd);
    }

    private Class<?> findClassUnshadowed(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }

    private byte[] readByteCode(final String className) throws ClassNotFoundException {
        final String resourceName = className.replace('.', '/') + ".class";
        InputStream is = getResourceAsStream(resourceName);
        if (is == null) {
            throw new ClassNotFoundException(className);
        }
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final byte[] buffer = new byte[8196];
            int bytesRead;
            while ( (bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new ClassNotFoundException(className, e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new ClassNotFoundException(className, e);
            }
        }
    }
}
