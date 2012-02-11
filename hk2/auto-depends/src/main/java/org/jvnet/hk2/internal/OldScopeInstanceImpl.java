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
package org.jvnet.hk2.internal;

import com.sun.hk2.component.AbstractCreatorInhabitantImpl;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import org.glassfish.hk2.Provider;
import org.glassfish.hk2.ScopeInstance;
import org.glassfish.hk2.api.InjectionTarget;
import org.glassfish.hk2.api.Scope;

/**
 * This implementation is going to cheat a bit, knowing the implementation of
 * ScopedInhabitant.  So contains will always return true, and the get
 * operation will consult the underlying new scope.  And so the "put" will
 * do nothing, as will the release.  In this way we can "fake" the system
 * into using the new API rather than the old to control the scoping
 * 
 * @author jwells
 */
public class OldScopeInstanceImpl implements ScopeInstance {
  private final Scope newScope;
  private final HashMap<Provider<?>, InjectionTarget<?>> providerMap = new HashMap<Provider<?>, InjectionTarget<?>>();
  
  /* package */ OldScopeInstanceImpl(Scope newScope) {
    this.newScope = newScope;
  }

  /* (non-Javadoc)
   * @see org.glassfish.hk2.ScopeInstance#get(org.glassfish.hk2.Provider)
   * 
   * We are cheating here, using the underlying scope in order to always get
   * the return object.  Hence, even though a put might have added us, we may
   * return a different value if we have gone over to a different scope
   */
  @SuppressWarnings("unchecked")
  @Override
  public <T> T get(Provider<T> provider) {
    // Collection<Annotation> annotations = provider.getAnnotations();
    Collection<Annotation> annotations = Collections.emptySet();
    
    AbstractCreatorInhabitantImpl<T> acii = (AbstractCreatorInhabitantImpl<T>) provider;
    
    InjectionTarget<T> it;
    synchronized (providerMap) {
      it = (InjectionTarget<T>) providerMap.get(provider);
      if (it == null) {
        Class<? extends T> myClazz = provider.type();
        
        it = (InjectionTarget<T>) InjectionTarget.getInjectionTarget(myClazz,
            annotations.toArray(new Annotation[0]));
        
        providerMap.put(provider, it);
      }
    }
    
    javax.inject.Provider<T> scopedProvider = newScope.scope(it, Utilities.getUnscopedProvider(acii, acii.getCreator(), provider));
    return scopedProvider.get();
  }

  /* (non-Javadoc)
   * @see org.glassfish.hk2.ScopeInstance#contains(org.glassfish.hk2.Provider)
   * 
   * In this cheat contains always returne true (even before put has ever been called!)
   */
  @Override
  public <T> boolean contains(Provider<T> provider) {
    
    return true;
  }

  /* (non-Javadoc)
   * @see org.glassfish.hk2.ScopeInstance#put(org.glassfish.hk2.Provider, java.lang.Object)
   * 
   * In this cheat this is a no-op (and always returns null)
   */
  @Override
  public <T> T put(Provider<T> provider, T value) {
    return null;
  }

  /* (non-Javadoc)
   * @see org.glassfish.hk2.ScopeInstance#release()
   * 
   * In this cheat, this is a no-op, this provider remains
   * active forever
   */
  @Override
  public void release() {
    // TODO Auto-generated method stub

  }

}
