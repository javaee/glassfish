/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2010 Sun Microsystems, Inc. All rights reserved.
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
package org.jvnet.hk2.component;

import java.lang.ref.WeakReference;

import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;

/**
 * A proxy where the underlying HabitatListener is held as a weak
 * proxy and is automatically removed from the habitat if the proxy
 * is GC'ed.
 *
 * @author Jeff Trent
 * 
 * @since 3.1
 */
public class HabitatListenerWeakProxy implements HabitatListener {

  private final WeakReference<HabitatListener> proxy;
  
  public HabitatListenerWeakProxy(HabitatListener proxy) {
    if (null == proxy) throw new IllegalArgumentException();
    this.proxy = new WeakReference<HabitatListener>(proxy);
  }
  
  @Override
  public synchronized boolean inhabitantChanged(EventType eventType, Habitat habitat,
      Inhabitant<?> inhabitant) {
    HabitatListener listener = proxy.get();
    if (null == listener) return false;
    return listener.inhabitantChanged(eventType, habitat, inhabitant);
  }

  @Override
  public boolean inhabitantIndexChanged(EventType eventType, Habitat habitat,
      Inhabitant<?> inhabitant, String index, String name, Object service) {
    HabitatListener listener = proxy.get();
    if (null == listener) return false;
    return listener.inhabitantIndexChanged(eventType, habitat, inhabitant, index, name, service);
  }

}
