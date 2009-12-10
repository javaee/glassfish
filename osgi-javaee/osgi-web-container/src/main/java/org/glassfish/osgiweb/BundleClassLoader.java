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


package org.glassfish.osgiweb;

import org.osgi.framework.Bundle;

import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * This is a delegating class loader.
 * It always delegates to OSGi bundle's class loader.
 * ClassLoader.defineClass() is never called in the context of this class.
 * There will never be a class for which getClassLoader()
 * would return this class loader.
 * It overrides loadClass(), getResource() and getResources() as opposed to
 * their findXYZ() equivalents so that the OSGi export control mechanism
 * is enforced even for classes and resources available in the system/boot
 * class loader.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class BundleClassLoader extends ClassLoader
{
    private Bundle bundle;

    public BundleClassLoader(Bundle b)
    {
        super(Bundle.class.getClassLoader());
        this.bundle = b;
    }

    @Override
    protected synchronized Class<?> loadClass(final String name, boolean resolve) throws ClassNotFoundException
    {
        return bundle.loadClass(name);
    }

    @Override
    public URL getResource(String name)
    {
        return bundle.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException
    {
        Enumeration<URL> resources = bundle.getResources(name);
        if (resources == null)
        {
            // This check is needed, because ClassLoader.getResources()
            // expects us to return an empty enumeration.
            resources = new Enumeration<URL>()
            {

                public boolean hasMoreElements()
                {
                    return false;
                }

                public URL nextElement()
                {
                    throw new NoSuchElementException();
                }
            };
        }
        return resources;
    }
}
