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

import org.glassfish.internal.api.DelegatingClassLoader;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;

/**
 * An implementation of {@link org.glassfish.internal.api.DelegatingClassLoader.ClassFinder}
 * that uses reflection to call the methods of the delegate.
 * It is currently NOT used because it requires special permission
 * granted to this codebase to access protected members like findClass.
 *
 * This is pretty much an ugly hack.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
class ReflectiveClassFinder implements DelegatingClassLoader.ClassFinder
{
    ClassLoader delegate;
    Method findClass, findLoadedClass, findResource, findResources;

    ReflectiveClassFinder(ClassLoader delegate)
    {
        this.delegate = delegate;
        Class<ClassLoader> clazz = ClassLoader.class;
        try {
            findClass = clazz.getDeclaredMethod("findClass", String.class);
            findLoadedClass = clazz.getDeclaredMethod("findLoadedClass", String.class);
            findResource = clazz.getDeclaredMethod("findResource", String.class);
            findResources = clazz.getDeclaredMethod("findResources", String.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public ClassLoader getParent()
    {
        return delegate.getParent();
    }

    public Class<?> findClass(String name) throws ClassNotFoundException
    {
        try
        {
            Object result = findClass.invoke(delegate, name);
            return (Class) result;
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e)
        {
            Throwable cause = e.getCause();
            if (cause instanceof ClassNotFoundException)
            {
                throw (ClassNotFoundException) cause;
            }
            else if (cause instanceof RuntimeException)
            {
                throw (RuntimeException) cause;
            }
            else
            {
                throw new RuntimeException(e);
            }
        }
    }

    public Class<?> findExistingClass(String name)
    {
        try
        {
            Object result = findLoadedClass.invoke(delegate, name);
            return (Class) result;
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e)
        {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException)
            {
                throw (RuntimeException) cause;
            }
            else
            {
                throw new RuntimeException(e);
            }
        }
    }

    public URL findResource(String name)
    {
        try
        {
            Object result = findResource.invoke(delegate, name);
            return (URL) result;
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e)
        {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException)
            {
                throw (RuntimeException) cause;
            }
            else
            {
                throw new RuntimeException(e);
            }
        }
    }

    public Enumeration<URL> findResources(String name) throws IOException
    {
        try
        {
            Object result = findResources.invoke(delegate, name);
            return (Enumeration<URL>) result;
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e)
        {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException)
            {
                throw (RuntimeException) cause;
            }
            else
            {
                throw new RuntimeException(e);
            }
        }
    }
}
