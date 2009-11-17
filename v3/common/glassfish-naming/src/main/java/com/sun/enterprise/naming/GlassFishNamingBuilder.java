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


package com.sun.enterprise.naming;

import org.glassfish.api.Startup;
import org.glassfish.internal.api.*;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.PreDestroy;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.NamingManager;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedAction;
import java.util.Hashtable;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.lang.reflect.Field;

import com.sun.enterprise.naming.util.LogFacade;

/**
 * This is both a {@link Startup} service as well as our implementation of
 * {@link InitialContextFactoryBuilder}. When GlassFish starts up, this
 * startup service configures NamingManager with appropriate builder by calling
 * {@link javax.naming.spi.NamingManager#setInitialContextFactoryBuilder}.
 * Once the builder is setup, when ever new InitialContext() is called,
 * builder can either instantiate {@link SerialInitContextFactory}, which is our
 * implementation of {@link InitialContextFactory}, or any user specified
 * InitialContextFactory class. While loading user specified class, it first
 * uses Thread's context class loader and then CommonClassLoader.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
@Service
public class GlassFishNamingBuilder implements InitialContextFactoryBuilder, Startup, PostConstruct, PreDestroy
{
    @Inject
    private ServerContext sc;

    private static Logger _logger = LogFacade.getLogger();

    public InitialContextFactory createInitialContextFactory(Hashtable<?, ?> environment) throws NamingException
    {
        if (environment != null)
        {
            // As per the documentation of Context.INITIAL_CONTEXT_FACTORY,
            // it represents a fully qualified class name.
            String className = (String) environment.get(Context.INITIAL_CONTEXT_FACTORY);
            if (className != null)
            {
                try
                {
                    return (InitialContextFactory) (loadClass(className).newInstance());
                }
                catch (Exception e)
                {
                    NoInitialContextException ne =
                            new NoInitialContextException(
                                    "Cannot instantiate class: " + className);
                    ne.setRootCause(e);
                    throw ne;
                }
            }
        }
        // default case
        return new SerialInitContextFactory();
    }

    public Lifecycle getLifecycle()
    {
        return Lifecycle.SERVER;
    }

    private Class loadClass(String className) throws ClassNotFoundException
    {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            return Class.forName(className, true, tccl);
        } catch (ClassNotFoundException e) {
            // Not a significant error.  Try with common class loader instead.
            _logger.logp(Level.FINE, "GlassFishNamingBuilder", "loadClass",
                    "Failed to load {0} using thread context class loader {1}", new Object[]{className, tccl});
            // Try using CommonClassLoader.
            ClassLoader ccl = sc.getCommonClassLoader();
            if (tccl != ccl) {
                try {
                    return Class.forName(className, true, ccl);
                } catch (ClassNotFoundException e2) {
                    _logger.logp(Level.WARNING, "GlassFishNamingBuilder", "loadClass", "Failed to load {0} using CommonClassLoader", new Object[]{className});
                    throw e2;
                }
            }
            throw e;
        }
    }

    public void postConstruct()
    {
        try
        {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null)
            {
                try
                {
                    AccessController.doPrivileged(new PrivilegedExceptionAction<Void>()
                    {
                        public Void run() throws NamingException
                        {
                            NamingManager.setInitialContextFactoryBuilder(GlassFishNamingBuilder.this);
                            return null;  //Nothing to return
                        }
                    });
                }
                catch (PrivilegedActionException e)
                {
                    throw (NamingException) e.getCause();
                }
            }
            else
            {
                NamingManager.setInitialContextFactoryBuilder(this);
            }
        }
        catch (NamingException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void preDestroy()
    {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            AccessController.doPrivileged(new PrivilegedAction<Void>(){
                public Void run() {
                    resetInitialContextFactoryBuilder();
                    return null;
                }
            });
        } else {
            resetInitialContextFactoryBuilder();
        }
    }

    private void resetInitialContextFactoryBuilder()
    {
        try
        {
            Field f = NamingManager.class.getDeclaredField("initctx_factory_builder");
            f.setAccessible(true);
            f.set(null, null);
        }
        catch (NoSuchFieldException e)
        {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e); // TODO(Sahoo): Proper Exception Handling
        }
    }
}
