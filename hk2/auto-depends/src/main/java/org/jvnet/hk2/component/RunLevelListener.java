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

/**
 * Implementations of RunLevelService are encouraged to
 * use this contract for publishing RunLevel events.
 * <p>
 * Instances of classes implementing this contract can
 * be registered into the habitat to be informed of
 * events of RunLevelServices within the same habitat.
 * <p>
 * Note that RunLevelService implementations may be
 * asynchronous, so RunLevelListeners should be thread
 * safe.  Additionally, you are discouraged from performing
 * lengthy operations in the listener since that may impact
 * the performance of the RunLevelService calling the
 * listener on the same thread.
 * <p>
 * All RunLevelListeners get called for all RunLevel events
 * on the same habitat.  It is therefore the caller's
 * responsibility to distinguish the RunLevelState(s) of
 * their interest.
 * 
 * @author Jeff Trent
 * 
 * @since 3.1
 */
@Contract
public interface RunLevelListener {

  /**
   * Called when an RunLevelService implementation's
   * proceedTo() operation has been canceled for some reason.  This could
   * be as a result of a new proceedTo() call or an interrupt() for example.
   * 
   * @param state the current and new planned state
   * 
   * @param ctx the context of the inhabitant that was is the midst of being processed at the time of the cancel
   *    if this information is available, null otherwise 
   * 
   * @param previousProceedTo the previousProceedTo state that is
   *    being canceled.  (This should always be != state.plannedRunLevel)
   *    
   * @param boolean isInterrupt set to true if the onCancelled even was as
   *    a result of an explicit interrupt() call. 
   */
  void onCancelled(RunLevelState<?> state, ServiceContext ctx, int previousProceedTo, boolean isInterrupt);
  
  /**
   * Called when a service throws an exception during lifecycle orchestration.
   * 
   * @param state the current RunLevelService state successfully achieved
   * @param context the of the failure (may be null)
   * @param error the error that was caught
   * @param willContinue the flag indicating whether or not the RunLevelService
   *    plans to proceed thru to the planned RunLevel state.
   */
  void onError(RunLevelState<?> state, ServiceContext context, Throwable error, boolean willContinue);

  /**
   * Called when the RunLevelService advances in some tangible way.
   * 
   * @param state the RunLevelService's state
   */
  void onProgress(RunLevelState<?> state);
  
}
