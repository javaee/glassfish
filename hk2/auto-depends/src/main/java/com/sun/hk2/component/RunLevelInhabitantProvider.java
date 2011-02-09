/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2011 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.hk2.component;

import java.util.Iterator;
import java.util.Set;

import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.InhabitantListener;
import org.jvnet.hk2.component.InhabitantProviderInterceptor;
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
      // this is a RunLevel service, we need to load type in order to get
      // more type information about it, namely it's environment and actual RunLevel id
      RunLevel rl = i.getAnnotation(RunLevel.class);
      assert(null != rl) : typeName + " is a problem; " + i + " has no RunLevel annotation";
      assert(!i.isInstantiated()) : "inhabitant should not be active: " + i;

      // get the appropriate RLS for this RunLevel
      RunLevelService<?> rls = runLevelServices.get(habitat, rl);
      InhabitantListener listener = InhabitantListener.class.isInstance(rls) ?
          InhabitantListener.class.cast(rls) : null;

      // wrap the inhabitant with a RunLevelInhabitant
      int runLevel = rl.value();
      
      // construct the runLevel inhabitant
      i = new RunLevelInhabitant(i, runLevel, rls.getState(), listener);
    }
    
    InhabitantProviderInterceptor next = 
        remainingInterceptors.hasNext() ? remainingInterceptors.next() : null;
    return (null == next) ? 
        i : next.visit(i, typeName, indicies, remainingInterceptors, store);
  }

}
