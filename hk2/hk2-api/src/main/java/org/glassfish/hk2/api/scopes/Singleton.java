/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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
 * file and include the License file at packager/legal/LICENSE.txt.
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
package org.glassfish.hk2.api.scopes;

import java.util.HashMap;

import javax.inject.Provider;

import org.glassfish.hk2.api.ExtendedProvider;
import org.glassfish.hk2.api.InjectionTarget;
import org.glassfish.hk2.api.Scope;

/**
 * An implementation of the Singleton scope
 * 
 * @author jwells
 */
public class Singleton implements Scope {
  private final HashMap<InjectionTarget<?>, Provider<?>> instanceMap = new HashMap<InjectionTarget<?>, Provider<?>>();

  /* (non-Javadoc)
   * @see org.glassfish.hk2.api.Scope#scope(org.glassfish.hk2.api.InjectionTarget, org.glassfish.hk2.api.ExtendedProvider)
   */
  @SuppressWarnings("unchecked")
  @Override
  public <T> Provider<T> scope(InjectionTarget<T> target,
      ExtendedProvider<T> unscopedProvider) {
    synchronized (instanceMap) {
      Provider<T> retVal = (Provider<T>) instanceMap.get(target);
      if (retVal != null) return retVal;
      
      final T instance = unscopedProvider.get();
      retVal = new Provider<T>() {

        @Override
        public T get() {
          return instance;
        }
        
      };
      
      instanceMap.put(target, retVal);
      return retVal;
    }
  }

}
