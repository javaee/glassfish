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

import org.jvnet.hk2.component.Inhabitant;

/**
 * Contract abstracting the storage of inhabitants into a backing store (which is
 * usually the habitat in most situations).
 * 
 * @author Jeff Trent
 */
public interface InhabitantStore {

  /**
   * Adds the given inhabitant to the backing store (usually the habitat)
   */
  void add(Inhabitant<?> i);

  /**
   * Adds the given inhabitant index to the backing store (usually the habitat)
   */
  void addIndex(Inhabitant<?> i, String typeName, String name);

  /**
   * Removes an inhabitant
   *
   * @param inhabitant
   *      inhabitant to be removed
   * @return
   *      true if the inhabitant was removed
   */
  boolean remove(Inhabitant<?> inhabitant);

  /**
   * Removes a named Inhabitant index for a specific contract and name
   *
   * @param index 
   *      the contract name
   * @param name 
   *      the instance name, optionally provided
   * @return 
   *      true if the removal was successful
   */
  public boolean removeIndex(String index, String name);

  /**
   * Removes an Inhabitant index for a specific service or inhabitant
   * 
   * @param index
   *      the contract name
   * @param serviceOrInhabitant
   *      the service instance, or inhabitant instance
   * @return
   *      true if the removal was successful
   */
  public boolean removeIndex(String index, Object serviceOrInhabitant);

}
