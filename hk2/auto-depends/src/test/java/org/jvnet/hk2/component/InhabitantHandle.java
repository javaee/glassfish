/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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
package org.jvnet.hk2.component;

import java.util.List;

import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.MultiMap;

/**
 * This is the handle returned by the InhabitantHandler
 * when a service is registered.
 * <p>
 * If you need to unregister a service, you will need to
 * use this handle to do so.
 * <p>
 * A test-only, helper construct.
 * 
 * @author Jeff Trent
 */
public interface InhabitantHandle<T> {

  /**
   * Adds an index and name to the given handle.
   * 
   * @param index usually the contract
   * @param name optionally the name
   * @throws ComponentException
   */
  public void addIndex(String index) throws ComponentException;
  
  /**
   * Adds an index and name to the given handle.
   * 
   * @param index usually the contract
   * @param name optionally the name
   * @throws ComponentException
   */
  public void addIndex(String index, String name) throws ComponentException;

  /**
   * The Inhabitant represented by this handle.
   * 
   * @return The inhabitant represented by this handle
   */
  public Inhabitant<T> getInhabitant();
  
  /**
   * Retrieves the list of indices/contracts belonging to this inhabitant.
   * 
   * @return the list of indices/contracts belonging to this inhabitant
   */
  public List<String> getIndices();
  
  /**
   * The current set of properties associated
   * with this handle
   * 
   * @return The current set of properties
   * associated with this handle.
   */
  public MultiMap<String,String> getMetadata();

  /**
   * Checks whether this handle has been committed to the habitat
   * 
   * @return true if this handle has been committed
   */
  public boolean isCommitted();
  
  /**
   * Commits the inhabitant to the habitat (along with all of its related indexes).
   * 
   * Once commit is called, the this handle is no longer modifiable
   */
  public void commit();

  /**
   * Unregisters the inhabitant from the habitat (along with all of its related indexes).
   * 
   * Once release is called, this handle is modifiable
   */
  public void release();
}
