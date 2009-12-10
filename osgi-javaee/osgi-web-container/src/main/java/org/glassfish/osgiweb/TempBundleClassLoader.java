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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 * This class loader only provides a new class loading namespace
 * It is useful during annotation scanning classes get loaded in that separate
 * namespace. This class loader delegates all stream handling (i.e. reading
 * actual class/resource data) operations to a delegate Bundle.
 * It only defines the Class using the byte codes.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class TempBundleClassLoader extends ClassLoader
{
    private BundleClassLoader delegate;

    public TempBundleClassLoader(BundleClassLoader delegate)
    {
        // Set our parent same as delegate's
        super(delegate.getParent());
        this.delegate = delegate;
    }

    /**
     * This method uses the delegate to use class bytes and then defines
     * the class using this class loader
     */
    protected Class findClass(String name) throws ClassNotFoundException
    {
        String entryName = name.replace('.', '/') + ".class";
        URL url = delegate.getResource(entryName);
        if (url == null)
        {
            throw new ClassNotFoundException(name);
        }
        InputStream inputStream = null;
        byte[] bytes = null;
        try
        {
            inputStream = url.openStream();
            bytes = getClassData(inputStream);
        }
        catch (IOException e)
        {
            throw new ClassNotFoundException(name, e);
        }
        finally
        {
            try
            {
                if (inputStream != null)
                {
                    inputStream.close();
                }
            }
            catch (IOException e)
            {
                // ignore
            }
        }
        // Define package information if necessary
        int lastPackageSep = name.lastIndexOf('.');
        if (lastPackageSep != -1)
        {
            String packageName = name.substring(0, lastPackageSep);
            if (getPackage(packageName) == null)
            {
                try
                {
                    // There's a small chance that one of our parents
                    // could define the same package after getPackage
                    // returns null but before we call definePackage,
                    // since the parent classloader instances
                    // are not locked.  So, just catch the exception
                    // that is thrown in that case and ignore it.
                    //
                    // It's unclear where we would get the info to
                    // set all spec and impl data for the package,
                    // so just use null.  This is consistent will the
                    // JDK code that does the same.
                    definePackage(packageName, null, null, null,
                            null, null, null, null);
                }
                catch (IllegalArgumentException iae)
                {
                    // duplicate attempt to define same package.
                    // safe to ignore.
                }
            }
        }
        Class clazz = null;
        try
        {
            clazz = defineClass(name, bytes, 0, bytes.length, null); // TODO(Sahoo): Set appropriate protection domain
            return clazz;
        }
        catch (UnsupportedClassVersionError ucve)
        {
            throw new UnsupportedClassVersionError(name + " can't be defined as we are running in Java version" +
                    System.getProperty("java.version"));
        }
    }

    public URL getResource(String name)
    {
        return delegate.getResource(name);
    }

    public Enumeration<URL> findResources(String name) throws IOException
    {
        return delegate.getResources(name);
    }

    /**
     * Returns the byte array from the given input stream.
     *
     * @param istream input stream to the class or resource
     * @throws IOException if an i/o error
     */
    private byte[] getClassData(InputStream istream) throws IOException
    {
        BufferedInputStream bstream = new BufferedInputStream(istream);
        byte[] buf = new byte[4096];
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        int num = 0;
        try
        {
            while ((num = bstream.read(buf)) != -1)
            {
                bout.write(buf, 0, num);
            }
        }
        finally
        {
            if (bstream != null)
            {
                bstream.close();
            }
        }

        return bout.toByteArray();
    }


}
