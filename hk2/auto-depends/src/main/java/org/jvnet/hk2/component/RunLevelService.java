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
package org.jvnet.hk2.component;

import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.RunLevel;

/**
 * Implementations of this contract are responsible for orchestration
 * lifecycle events (i.e., start levels) in Hk2.
 * <p>
 * Each resident run level service is responsible for a particular
 * class of <code>RunLevel</code> that is identified by an environment value.
 * <p>
 * Implementations of this service are responsible for orchestrating
 * lifecycle events for services annotated with <code>RunLevel</code>. This
 * consists of inhabitant activations (i.e., get) and deactivations (i.e., release).
 * <p>
 * The default RunLevelService uses T==Void.class, and is registered in
 * the habitat with the name "default" and is the primary service instance
 * responsible for the "platform".
 * <p>
 * Third parties (or otherwise any sub-component) may choose to define other
 * implementation variations of this contract for other specific needs.
 * <p>
 * RunLevelServices are special in that they are constructed early on
 * in habitat creation lifecycle.  As a result, they should not rely
 * on the Habitat being fully initialized in any <code>PostConstruct</code>
 * call.  Alternatively, they should implement HabitatListener, and
 * wait for a habitat initialization event before doing any initialization
 * work.
 * <p>
 * Each implementation of the RunLevelService may vary, but in general it is
 * encouraged that the implementation use habitat resident {@link RunLevelListener}(s)
 * for event notification, habitat resident {@link InhabitantSorter}(s) for arranging
 * the order of inhabitants to be activated at a given {@link RunLevel}, and finally
 * habitat resident {@link InhabitantActivator}(s) for the activation (i.e. get) and
 * deactivation (i.e., release) of the sorted inhabitants. 
 * 
 * @see AbstractRunLevelService
 * @see RunLevelListener
 * @see InhabitantSorter
 * @see InhabitantActivator
 * 
 * @author Jeff Trent
 * 
 * @since 3.1
 */
@Contract
public interface RunLevelService<T> {

  /**
   * Returns the current state of this RunLevelService instance.
   */
  RunLevelState<T> getState();

  /**
   * Causes this RunLevelService to move to the specified run level for
   * all RunLevel instances (identified by environment), orchestrating
   * the appropriate lifecycle events based on the given implementation
   * strategy.  See the javadoc for each implementation for specific
   * details.
   * 
   * <p>
   * If the RunLevel specified is the same as the current RunLevel then
   * the RunLevelService may return immediately.
   * 
   * <p>
   * Note that the underlying implementation may perform this operation
   * asynchronously. Implementors who choose the asynchronous approach
   * are expected to treat a subsequent proceedTo(newRunLevel) call as
   * an implicit cancellation of any currently running proceedTo() that
   * is running on one or more managed threads.  Again, see the javadoc
   * for each implementation for details.
   * 
   * @param runLevel the run level to move to.
   */
  void proceedTo(int runLevel);
  
  /**
   * Causes this RunLevelService to attempt to stop any in-flight
   * proceedTo() operation.  This call will not have any
   * any affect if there is no current proceedTo() operation
   * in progress.
   * 
   * <p>
   * See the javadoc for each implementation for specific details 
   */
  void interrupt();
  
  /**
   * Same as {@link #interrupt()}, with the option to immediately perform
   * a {@link #proceedTo(int)} following the interrupt.
   * 
   * @param runLevel the run level to move to following the interrupt
   */
  void interrupt(int runLevel);
  
}
