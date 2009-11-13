/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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


package com.sun.enterprise.naming.util;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectStreamClass;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiObjectInputOutputStreamFactoryImpl
        implements ObjectInputOutputStreamFactory
{
    private BundleContext ctx;
    PackageAdmin pkgAdm;

    // Since bundle id starts with 0, we use -1 to indicate a non-bundle
    private static final long NOT_A_BUNDLE_ID = -1;

    public OSGiObjectInputOutputStreamFactoryImpl(BundleContext ctx)
    {
        this.ctx = ctx;
        ServiceReference ref = ctx.getServiceReference(PackageAdmin.class.getName());
        pkgAdm = PackageAdmin.class.cast(ctx.getService(ref));
    }

    public ObjectInputStream createObjectInputStream(InputStream in)
            throws IOException
    {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        return new OSGiObjectInputStream(in, loader);
    }

    public ObjectOutputStream createObjectOutputStream(OutputStream out)
            throws IOException
    {
        return new OSGiObjectOutputStream(out);
    }

    private class OSGiObjectInputStream extends ObjectInputStreamWithLoader
    {

        public OSGiObjectInputStream(InputStream in, ClassLoader loader) throws IOException
        {
            super(in, loader);
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc)
                throws IOException, ClassNotFoundException
        {
            Class clazz =
                OSGiObjectInputOutputStreamFactoryImpl.this.resolveClass(this, desc);

            if (clazz == null) {
                clazz = super.resolveClass(desc);
            }

            return clazz;
        }

    }

    private class OSGiObjectOutputStream extends ObjectOutputStream {


        private OSGiObjectOutputStream(OutputStream out) throws IOException
        {
            super(out);
        }

        @Override
        protected void annotateClass(Class<?> cl) throws IOException
        {
            OSGiObjectInputOutputStreamFactoryImpl.this.annotateClass(this, cl);
        }
    }

    public Class<?> resolveClass(ObjectInputStream in, final ObjectStreamClass desc)
            throws IOException, ClassNotFoundException
    {
        long bundleId = in.readLong();
        if (bundleId != NOT_A_BUNDLE_ID) {
            final Bundle b = ctx.getBundle(bundleId);

            if (System.getSecurityManager() == null) {
                return b.loadClass(desc.getName());
            } else {
                try {
                    return (Class) java.security.AccessController.doPrivileged(
                        new java.security.PrivilegedExceptionAction()  {
                            public java.lang.Object run() throws ClassNotFoundException {
                               return b.loadClass(desc.getName());
                            }
                        });
                } catch(java.security.PrivilegedActionException pae) {
                    throw (ClassNotFoundException) pae.getException();    
                }
            }
        } else {
            return null;
        }
    }

    public void annotateClass(ObjectOutputStream out, Class<?> cl) throws IOException
    {
        long id = NOT_A_BUNDLE_ID;
        Bundle b = pkgAdm.getBundle(cl);
        if (b != null) {
            id = b.getBundleId();
        }
        out.writeLong(id);
    }



}
