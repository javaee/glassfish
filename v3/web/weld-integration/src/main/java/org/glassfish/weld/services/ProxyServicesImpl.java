/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.weld.services;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;

import org.jboss.weld.bean.proxy.util.SerializableProxy;
import org.jboss.weld.serialization.spi.ProxyServices;
/**
 * A simple implementation of the ProxyServices Service
 * @author Sivakumar Thyagarajan
 */
public class ProxyServicesImpl implements ProxyServices
{

   public ClassLoader getClassLoader(final Class<?> type)
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
      {
         return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>()
         {
            public ClassLoader run()
            {
               return _getClassLoader(type);
            }
         });
      }
      else
      {
         return _getClassLoader(type);
      }      
   }

   private ClassLoader _getClassLoader(Class<?> type)
   {
      return Thread.currentThread().getContextClassLoader();
   }

   public ProtectionDomain getProtectionDomain(Class<?> type)
   {
      if (type.getName().startsWith("java"))
      {
         return this.getClass().getProtectionDomain();
      }
      else
      {
         return type.getProtectionDomain();
      }
   }

   public void cleanup()
   {
      // This implementation requires no cleanup

   }

   public Object wrapForSerialization(Object proxyObject)
   {
      return new SerializableProxy(proxyObject);
   }

   public Class<?> loadBeanClass(final String className)
   {
      try
      {
         return (Class<?>) AccessController.doPrivileged(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               ClassLoader cl = Thread.currentThread().getContextClassLoader();
               return Class.forName(className, true, cl);
            }
         });
      }
      catch (PrivilegedActionException pae)
      {
         pae.printStackTrace();
         throw new RuntimeException(pae);
      }
   }

}
