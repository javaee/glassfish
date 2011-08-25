/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2011 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.hk2.component;

import java.util.Iterator;
import java.util.Set;

import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.InhabitantListener;
import org.jvnet.hk2.component.InhabitantProviderInterceptor;
import org.jvnet.hk2.component.MultiMap;
import org.jvnet.hk2.component.RunLevelException;
import org.jvnet.hk2.component.RunLevelService;
import org.jvnet.hk2.component.internal.runlevel.RunLevelServices;

/**
 * {@link InhabitantProviderInterceptor} specializing in the creation
 * of {@link RunLevelInhabitant}s.
 * 
 * @author Jeff Trent
 */
public class RunLevelInhabitantProvider extends AbstractInhabitantProvider {

  private static final RunLevelServices runLevelServices = new RunLevelServices(); 
  
  private final Habitat habitat;

  public RunLevelInhabitantProvider(Habitat h) {
    this.habitat = h;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public AbstractInhabitantImpl<?> visit(AbstractInhabitantImpl<?> i,
        String typeName,
        Set<String> indicies,
        Iterator<InhabitantProviderInterceptor> remainingInterceptors,
        InhabitantStore store) {
    if (contains(indicies, RunLevel.class.getName())) {
      assert(!i.isActive()) : "inhabitant should not be active yet: " + i;
      
      // we need to avoid loading the class to avoid unnecessary classloading!
//      RunLevel rl = i.getAnnotation(RunLevel.class);
//      assert(null != rl) : typeName + " is a problem; " + i + " has no RunLevel annotation";
      MultiMap<String, String> md = i.metadata();

      String runLevelStr = md.getOne(RunLevel.META_VAL_TAG);
      assert(null != runLevelStr) : "expected a " + RunLevel.META_VAL_TAG + " value on " + i;
      
      String scopeStr = i.metadata().getOne(RunLevel.META_SCOPE_TAG);
      assert(null != scopeStr) : "expected a " + RunLevel.META_SCOPE_TAG + " value on " + i;

//    int runLevel = rl.value();
      int runLevel = Integer.valueOf(runLevelStr);
      
      // get the appropriate RLS for this RunLevel
      RunLevelService<?> rls;
      try {
          rls = runLevelServices.get(habitat, runLevel, scopeStr);
      } catch (Exception e) {
          throw new RunLevelException("unable to find the RunLevelService appropriate for: " + scopeStr, e);
      }
      InhabitantListener listener = InhabitantListener.class.isInstance(rls) ?
          InhabitantListener.class.cast(rls) : null;

      // wrap the inhabitant with a RunLevelInhabitant
      i = new RunLevelInhabitant(i, runLevel, rls.getState(), listener);
    }
    
    InhabitantProviderInterceptor next = 
        remainingInterceptors.hasNext() ? remainingInterceptors.next() : null;
    return (null == next) ? 
        i : next.visit(i, typeName, indicies, remainingInterceptors, store);
  }

}
