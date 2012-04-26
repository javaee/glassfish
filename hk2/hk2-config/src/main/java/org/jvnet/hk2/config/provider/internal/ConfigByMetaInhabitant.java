/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Sun Microsystems, Inc. All rights reserved.
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
package org.jvnet.hk2.config.provider.internal;

import java.util.HashMap;
import java.util.Set;

import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.config.ConfiguredBy;

import com.sun.hk2.component.EventPublishingInhabitant;
import com.sun.hk2.component.InhabitantStore;
import com.sun.hk2.component.InhabitantsParser;
import com.sun.hk2.component.InjectionResolverQuery;

/**
 * Meta representation for an @{link ConfiguredBy} inhabitant.
 * 
 * @author Jeff Trent
 */
@SuppressWarnings("unchecked")
/*public*/ class ConfigByMetaInhabitant extends EventPublishingInhabitant<Object> {

  private final Habitat h;
  private final Class<?> configuredBeanClass;
  private final Set<String> indicies;

  private HashMap<Object, Inhabitant<?>> configured;
  
  /*public*/ ConfigByMetaInhabitant(Habitat h,
        Inhabitant delegate,
        ConfiguredBy configuredBy,
        Set<String> indicies) {
    super(delegate);

    this.h = h;
    this.configuredBeanClass = configuredBy.value();
    this.indicies = indicies;
  }

  ConfigByCreator creator() {
    return (ConfigByCreator) real;
  }
  
  @Override
  public Object get(Inhabitant onBehalfOf) {
    throw new ComponentException("improper use of " + this);
  }

  @Override
  public boolean isActive() {
    return false;
  }

  Class<?> getConfiguredBy() {
    return configuredBeanClass;
  }

  synchronized void managePrepare(Object configBean, InhabitantStore store, InjectionResolverQuery txnContextResolver) {
    if (null == configured) {
      configured = new HashMap<Object, Inhabitant<?>>();
    }

    // actualize the inhabitant instance from this meta inhabitant
    ConfigByCreator creator = new ConfigByCreator(txnContextResolver, configBean, real.type(), h, real.metadata());
    ConfigByInhabitant managedInhabitantInstance = new ConfigByInhabitant(store, this, creator, null);

    Inhabitant<?> old = configured.put(configBean, managedInhabitantInstance);
    assert(null == old);

    // dynamically add it to whatever the backing store is
    store.add(managedInhabitantInstance);
    if (!indicies.isEmpty()) {
      StringBuilder name = new StringBuilder();
      for (String index : indicies) {
        String contract = InhabitantsParser.parseIndex(index, name);
        store.addIndex(managedInhabitantInstance, contract, (0 == name.length()) ? null : name.toString());
        name.setLength(0);
      }
    }
  }

  synchronized void manageRelease(ConfigByInhabitant configByInhabitant, InhabitantStore store) {
//    // dynamically remove it from the habitat
//    boolean removed = store.remove(configByInhabitant);
//    
//    if (!indicies.isEmpty()) {
//      StringBuilder name = new StringBuilder();
//      for (String index : indicies) {
//        String contract = InhabitantsParser.parseIndex(index, null);
//        removed = store.removeIndex(contract, configByInhabitant);
//        name.setLength(0);
//      }
//    }
  }
  
}
