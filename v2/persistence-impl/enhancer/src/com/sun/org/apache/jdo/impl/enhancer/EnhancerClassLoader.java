/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.sun.org.apache.jdo.impl.enhancer;

import java.lang.ref.WeakReference;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.Properties;

import java.net.URLClassLoader;
import java.net.URL;

//^olsen: eliminate these dependencies
import sun.misc.Resource;
import sun.misc.URLClassPath;

import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;

import java.security.AccessController;
import java.security.AccessControlContext;
import java.security.CodeSource;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;
import java.security.cert.Certificate;

import com.sun.org.apache.jdo.impl.enhancer.jdo.impl.EnhancerFilter;
import com.sun.org.apache.jdo.impl.enhancer.meta.EnhancerMetaData;
import com.sun.org.apache.jdo.impl.enhancer.meta.model.EnhancerMetaDataJDOModelImpl;
import com.sun.org.apache.jdo.impl.enhancer.meta.prop.EnhancerMetaDataPropertyImpl;
import com.sun.org.apache.jdo.impl.enhancer.meta.util.EnhancerMetaDataTimer;
import com.sun.org.apache.jdo.impl.enhancer.util.Support;
import com.sun.org.apache.jdo.model.jdo.JDOModel;






/**
 * Implements a ClassLoader which automatically enchances the .class files
 * according to the EnhancerMetaData information in the jar archive.
 *
 * @author Yury Kamen
 * @author Martin Zaun
 */
public class EnhancerClassLoader extends URLClassLoader {

    static public final String DO_TIMING_STATISTICS
        = EnhancerFilter.DO_TIMING_STATISTICS;
    static public final String VERBOSE_LEVEL
        = EnhancerFilter.VERBOSE_LEVEL;
    static public final String VERBOSE_LEVEL_QUIET
        = EnhancerFilter.VERBOSE_LEVEL_QUIET;
    static public final String VERBOSE_LEVEL_WARN
        = EnhancerFilter.VERBOSE_LEVEL_WARN;
    static public final String VERBOSE_LEVEL_VERBOSE
        = EnhancerFilter.VERBOSE_LEVEL_VERBOSE;
    static public final String VERBOSE_LEVEL_DEBUG
        = EnhancerFilter.VERBOSE_LEVEL_DEBUG;

    static public URL[] pathToURLs(String classpath)
    {
        return URLClassPath.pathToURLs(classpath);
    }

    static final void affirm(boolean cond)
    {
        if (!cond)
            //^olsen: throw AssertionException ?
            throw new RuntimeException("Assertion failed.");
    }

    // misc
    private boolean debug = true;
    private boolean doTiming = false;
    private PrintWriter out = new PrintWriter(System.out, true);

    private ClassFileEnhancer enhancer;
    private EnhancerMetaData metaData;
    private Properties settings;
    private WeakReference outByteCodeRef;

    // The search path for classes and resources
    private final URLClassPath ucp;

    // The context to be used when loading classes and resources
    private final AccessControlContext acc;

    private final void message()
    {
        if (debug) {
            out.println();
        }
    }

    private final void message(String s)
    {
        if (debug) {
            out.println(s);
        }
    }

    private final void message(Exception e)
    {
        if (debug) {
            final String msg = ("Exception caught: " + e);
            out.println(msg);
            e.printStackTrace(out);
        }
    }

    /**
     * Creates a new EnhancerClassLoader for the specified url.
     *
     * @param urls the classpath to search
     */
    protected EnhancerClassLoader(URL[] urls)
    {
        super(urls);
        acc = AccessController.getContext();
        ucp = new URLClassPath(urls);
        checkUCP(urls);
    }

    /**
     * Creates a new EnhancerClassLoader for the specified url.
     *
     * @param urls the classpath to search
     */
    protected EnhancerClassLoader(URL[] urls,
                                  ClassLoader loader)
    {
        super(urls, loader);
        acc = AccessController.getContext();
        ucp = new URLClassPath(urls);
        checkUCP(urls);
    }

    /**
     * Creates a new EnhancerClassLoader for the specified url.
     *
     * @param classpath the classpath to search
     */
    public EnhancerClassLoader(String classpath,
                               Properties settings,
                               PrintWriter out)
    {
        this(pathToURLs(classpath));
        //^olsen: instantiate model
        affirm(false);
        EnhancerMetaData metaData
            = new EnhancerMetaDataJDOModelImpl(out, true, null, null, null);
        init(metaData, settings, out);
    }

    /**
     * Creates a new EnhancerClassLoader for the specified url.
     *
     * @param urls the classpath to search
     */
    public EnhancerClassLoader(URL[] urls,
                               Properties settings,
                               PrintWriter out)
    {
        this(urls);
        //^olsen: instantiate model
        affirm(false);
        EnhancerMetaData metaData
            = new EnhancerMetaDataJDOModelImpl(out, true, null, null, null);
        init(metaData, settings, out);
    }

    /**
     * Creates a new EnhancerClassLoader for the specified url.
     *
     * @param classpath the classpath to search
     */
    public EnhancerClassLoader(String classpath,
                               EnhancerMetaData metaData,
                               Properties settings,
                               PrintWriter out)
    {
        this(pathToURLs(classpath));
        init(metaData, settings, out);
    }

    /**
     * Creates a new EnhancerClassLoader for the specified url.
     *
     * @param urls the classpath to search
     */
    public EnhancerClassLoader(URL[] urls,
                               EnhancerMetaData metaData,
                               Properties settings,
                               PrintWriter out)
    {
        this(urls);
        init(metaData, settings, out);
    }

    /**
     * Appends the specified URL to the list of URLs to search for
     * classes and resources.
     *
     * @param url the URL to be added to the search path of URLs
     */
    protected void addURL(URL url)
    {
        throw new UnsupportedOperationException("Not implemented yet: EnhancerClassLoader.addURL(URL)");
        //super.addURL(url);
        //ucp.addURL(url);
    }

    private void checkUCP(URL[] urls)
    {
        // ensure classpath is not empty
        if (null == urls) {
            throw new IllegalArgumentException("urls == null");
        }
        if (urls.length == 0) {
            throw new IllegalArgumentException("urls.length == 0");
        }

        for (int i = 0; i < urls.length; i++) {
            super.addURL(urls[i]);
        }
    }

    /**
     * Initialize the EnhancingClassLoader
     */
    private void init(EnhancerMetaData metaData,
                      Properties settings,
                      PrintWriter out)
    {
        this.out = out;
        final String verboseLevel
            = (settings == null ? null
               : settings.getProperty(EnhancerFilter.VERBOSE_LEVEL));
        this.debug = EnhancerFilter.VERBOSE_LEVEL_DEBUG.equals(verboseLevel);
        this.settings = settings;
        this.metaData = metaData;
        this.enhancer = null;

        if (settings != null) {
            final String timing
                = settings.getProperty(EnhancerFilter.DO_TIMING_STATISTICS);
            this.doTiming = Boolean.valueOf(timing).booleanValue();
        }
        if (this.doTiming) {
            // wrap with timing meta data object
            this.metaData = new EnhancerMetaDataTimer(metaData);
        }

        message("EnhancerClassLoader: UCP = {");
        final URL[] urls = getURLs();
        for (int i = 0; i < urls.length; i++) {
            message("    " + urls[i]);
        }
        message("}");

        message("EnhancerClassLoader: jdoMetaData = " + metaData);
    }

    public synchronized Class loadClass(String name, boolean resolve)
        throws ClassNotFoundException
    {
        message();
        message("EnhancerClassLoader: loading class: " + name);

        try {
            Class c = null;

            final String classPath = name.replace('.', '/');
            // At least these packages must be delegated to parent class
            // loader:
            //    java/lang,	     (Object, ...)
            //    java/util,         (Collection)
            //    java/io,           (PrintWriter)
            //    javax/sql,         (PMF->javax.sql.DataSource)
            //    javax/transaction  (Tx->javax.transaction.Synchronization)
            //
            //@olsen: delegate loading of "safe" classes to parent
            //if (metaData.isTransientClass(classPath)) {
            //
            //@olsen: only delegate loading of bootstrap classes to parent
            //if (classPath.startsWith("java/lang/")) {
            //
            //@olsen: performance bug 4457471: delegate loading of F4J
            // persistence classes to parent tp prevent passing these and
            // other IDE classes plus database drivers etc. to the enhancer!
            //if (classPath.startsWith("java/lang/")
            //    || classPath.startsWith("com/sun/forte4j/persistence/")) {
            //
            //@olsen: bug 4480618: delegate loading of javax.{sql,transaction}
            // classes to parent class loader to support user-defined
            // DataSource and Synchronization objects to be passed to the
            // TP runtime.  By the same argument, java.{util,io} classes need
            // also be loaded by the parent class loader.  This has been
            // the case since the EnhancerClassLoader will never find these
            // bootstrap classes in the passed Classpath.  However, for
            // efficiency and clarity, this delegation should be expressed
            // by testing for entire "java/" package in the check here.
            if (classPath.startsWith("java/")//NOI18N
                || classPath.startsWith("javax/sql/")//NOI18N
                || classPath.startsWith("javax/transaction/")//NOI18N
                || classPath.startsWith("com/sun/forte4j/persistence/")) {//NOI18N
                message("EnhancerClassLoader: bootstrap class, using parent loader for class: " + name);//NOI18N
                return super.loadClass(name, resolve);

//@olsen: dropped alternative approach
/*
                message("EnhancerClassLoader: transient class, skipping enhancing: " + name);

                // get a byte array output stream to collect byte code
                ByteArrayOutputStream outClassFile
                    = ((null == outByteCodeRef)
                       ? null : (ByteArrayOutputStream)outByteCodeRef.get());
                if (null == outClassFile) {
                    outClassFile = new ByteArrayOutputStream(10000);
                    outByteCodeRef = new WeakReference(outClassFile);
                }
                outClassFile.reset();

                // find byte code of class
                final InputStream is = getSystemResourceAsStream(name);
                //@olsen: (is == null) ?!

                // copy byte code of class into byte array
                final byte[] data;
                try {
                    int b;
                    while ((b = is.read()) >= 0) {
                        outClassFile.write(b);
                    }
                    data = outClassFile.toByteArray();
                } catch (IOException e) {
                    final String msg
                        = ("Exception caught while loading class '"
                           + name + "' : " + e);
                    throw new ClassNotFoundException(msg, e);
                }

                // convert the byte code into class object
                c = defineClass(name, data, 0, data.length);
*/
            }

            //@olsen: check if class has been loaded already
            if (c == null) {
                c = findLoadedClass(name);
                if (c != null) {                
                    message("EnhancerClassLoader: class already loaded: " + name);//NOI18N
                }
            }

            if (c == null) {
                c = findAndEnhanceClass(name);
            }

            // as a last resort, if the class couldn't be found, try
            // loading class by parent class loader
            if (c == null) {
                message("EnhancerClassLoader: class not found, using parent loader for class: " + name);//NOI18N
                return super.loadClass(name, resolve);
            }

            message();
            message("EnhancerClassLoader: loaded class: " + name);
            if (resolve) {
                resolveClass(c);
            }

            message();
            message("EnhancerClassLoader: loaded+resolved class: " + name);
            return c;
        } catch (RuntimeException e) {
            // log exception only
            message();
            message("EnhancerClassLoader: EXCEPTION SEEN: " + e);
            //e.printStackTrace(out);
            throw e;
        } catch (ClassNotFoundException e) {
            // log exception only
            message();
            message("EnhancerClassLoader: EXCEPTION SEEN: " + e);
            //e.printStackTrace(out);
            throw e;
        }
    }

    /**
     * Finds and loads the class with the specified name from the URL search
     * path. Any URLs referring to JAR files are loaded and opened as needed
     * until the class is found.
     *
     * @param name the name of the class
     * @return the resulting class
     * @exception ClassNotFoundException if the class could not be found
     */
    private Class findAndEnhanceClass(final String name)
        throws ClassNotFoundException
    {
        try {
            if (doTiming) {
                Support.timer.push("EnhancerClassLoader.findAndEnhanceClass(String)",
                                   "EnhancerClassLoader.findAndEnhanceClass(" + name + ")");
            }
            return (Class)
            AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws ClassNotFoundException
                {
                    String path = name.replace('.', '/').concat(".class");
                    //message("path=" + path);
                    Resource res = ucp.getResource(path, false);
                    if (res != null) {
                        try {
                            return defineClass(name, res);
                        } catch (IOException e) {
                            final String msg
                                = ("Exception caught while loading class '"
                                   + name + "' : " + e);
                            throw new ClassNotFoundException(msg, e);
                        }
                    } else {
                        // ok if class resource not found (e.g. java.*)
                        //throw new ClassNotFoundException(name);
                        return null;
                    }
                }
            }, acc);
        } catch (PrivilegedActionException pae) {
            throw (ClassNotFoundException) pae.getException();
        } finally {
            if (doTiming) {
                Support.timer.pop();
            }
        }
    }

    /**
     * Defines a Class using the class bytes obtained from the specified
     * Resource. The resulting Class must be resolved before it can be
     * used.
     */
    private Class defineClass(String name, Resource res)
        throws IOException, ClassNotFoundException
    {
        int i = name.lastIndexOf('.');
        URL url = res.getCodeSourceURL();
        if (i != -1) {
            String pkgname = name.substring(0, i);
            // Check if package already loaded.
            Package pkg = getPackage(pkgname);
            Manifest man = res.getManifest();
            if (pkg != null) {
                // Package found, so check package sealing.
                boolean ok;
                if (pkg.isSealed()) {
                    // Verify that code source URL is the same.
                    ok = pkg.isSealed(url);
                } else {
                    // Make sure we are not attempting to seal the package
                    // at this code source URL.
                    ok = (man == null) || !isSealed(pkgname, man);
                }
                if (!ok) {
                    throw new SecurityException("sealing violation");
                }
            } else {
                if (man != null) {
                    definePackage(pkgname, man, url);
                } else {
                    definePackage(pkgname, null, null, null, null, null, null, null);
                }
            }
        }
        // Now read the class bytes and define the class
        byte[] b = res.getBytes();
        Certificate[] certs = res.getCertificates();
        CodeSource cs = new CodeSource(url, certs);

        //@olsen: performance bug 4457471: circumvent enhancer for
        // non-enhancable classes
        final String classPath = name.replace('.', '/');
        if (!metaData.isKnownUnenhancableClass(classPath)) {
            // Add enhancement here
            b = enhance(name, b, 0, b.length);
        }

        return defineClass(name, b, 0, b.length, cs);
    }

    private byte[] enhance(String name, byte[] data, int off, int len)
        throws ClassNotFoundException
    {
        //message("EnhancerClassLoader: enhance class: " + name);

        final byte[] result;
        try {
            // create enhancer if not done yet
            if (null == enhancer) {
                enhancer = new EnhancerFilter(metaData, settings, out, null);
                if (doTiming) {
                    // wrap with timing filter enhancer object
                    enhancer = new ClassFileEnhancerTimer(enhancer);
                }
            }

            // create input and output byte streams
            final ByteArrayInputStream inByteCode
                = new ByteArrayInputStream(data, off, len);
            ByteArrayOutputStream outByteCode
                = ((null == outByteCodeRef)
                   ? null : (ByteArrayOutputStream)outByteCodeRef.get());
            if (null == outByteCode) {
                outByteCode = new ByteArrayOutputStream(10000);
                outByteCodeRef = new WeakReference(outByteCode);
            }
            outByteCode.reset();

            // enhance class
            final boolean changed
                = enhancer.enhanceClassFile(inByteCode, outByteCode);

            // check whether class has been enhanced
            result = (changed ? outByteCode.toByteArray() : data);
        } catch (EnhancerUserException e) {
            message(e);
            final String msg = ("Exception caught while loading class '"
                                + name + "' : " + e);
            throw new ClassNotFoundException(msg, e);
        } catch(EnhancerFatalError e) {
            message(e);
            final String msg = ("Exception caught while loading class '"
                                + name + "' : " + e);
            // discard enhancer because it might have become inconsistent
            enhancer = null;
            throw new ClassNotFoundException(msg, e);
        }
        return result;
    }

    /**
     * Returns true if the specified package name is sealed according to the
     * given manifest.
     */
    private boolean isSealed(String name, Manifest man)
    {
        String path = name.replace('.', '/').concat("/");
        Attributes attr = man.getAttributes(path);
        String sealed = null;
        if (attr != null) {
            sealed = attr.getValue(Name.SEALED);
        }
        if (sealed == null) {
            if ((attr = man.getMainAttributes()) != null) {
                sealed = attr.getValue(Name.SEALED);
            }
        }
        return "true".equalsIgnoreCase(sealed);
    }
}
