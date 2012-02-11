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

import java.util.Set;

import org.glassfish.hk2.Provider;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.ExtendedProvider;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.DescriptorBuilder;
import org.jvnet.hk2.component.Creator;
import org.jvnet.hk2.component.Inhabitant;

/**
 * This class represents static utilities used in various
 * parts of the implementation
 * 
 * @author jwells
 */
public class Utilities {
  /**
   * Returns the first element in the given set
   * @param set The set to return the first element of
   * @return The first element in the set, or null
   */
  public static <T> T getFirstElement(Set<T> set) {
    if (set == null) return null;
    
    for (T name : set) {
      return name;
    }
    
    return null;
  }
  
  /**
   * Loads the given class name with the CCL, or
   * returns null if the class cannot be loaded
   * 
   * @param className The fully qualified name of the class
   * to load
   * @return The class as loaded by the CCL, or null if
   * the class cannot be loaded for some reason
   */
  public static Class<?> loadMyClass(String className) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    
    try {
      Class<?> retVal = cl.loadClass(className);
      
      return retVal;
    }
    catch (Throwable th) {
      // TODO: Is there a debug logger?
      return null;
    }
  }
  
  /**
   * This returns an ExtendedProvider based on the old provider class (and an optional scope)
   * <p>
   * Note that the descriptor returned here will be partial.  Several fields will NOT be filled
   * in, in particular things like id and the contracts implemented will NOT be there.  This
   * is an interim step until a native new API implementation can be made
   * 
   * @param provider The old style provider to generate
   * @param scopeClass An optional scope class that should be used in the returned provider
   * @return An extended provider
   */
  public static <T> ExtendedProvider<T> getUnscopedProvider(Inhabitant<T> onBehalfOf, Creator<T> creator, Provider<T> provider) {
    // Unfortunately, only *some* of the information is available here.  So this implementation
    // will only be able to return a partial ExtendedProvider.  It'll have to do
    
    DescriptorBuilder db = BuilderHelper.link(provider.type());
    
    //TODO:  Annotations do not work with the old-style providers
    // for (Annotation hardAnno : provider.getAnnotations()) {
    //  db = db.annotatedBy(hardAnno.getClass());
    // }
    
    // Loss of fidelity, all the contracts, metadata, etc are not here
    
    Descriptor desc = db.build();
    UnscopedProviderImpl<T> retVal = new UnscopedProviderImpl<T>(desc, onBehalfOf, creator);
    
    return retVal;
  }

}
