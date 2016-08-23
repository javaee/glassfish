/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.runlevel;


import org.jvnet.hk2.annotations.Contract;


/**
 * Instances of classes implementing this contract can be registered with HK2
 * to be informed of events of RunLevelControllers.
 * <p>
 * Lengthy operations should not be performed in the listener since
 * that may impact the performance of the RunLevelController calling the
 * listener.
 *
 * @author jtrent, tbeerbower
 */
@Contract
public interface RunLevelListener {
    /**
     * Called when the RunLevelController advances to the next level
     * <p>
     * Neither {@link RunLevelController#proceedTo(int)} nor
     * {@link RunLevelController#proceedToAsync(int)} may be called from this method.  However,
     * {@link ChangeableRunLevelFuture#changeProposedLevel(int)} may be called
     * <p>
     * Any exception thrown from this method is ignored
     * <p>
     * onProgress is also called once at the start of any change with the
     * current level of the RunLevelService.  When called at the start
     * of a level the {@link OnProgressCallbackType} returned by the
     * {@link ChangeableRunLevelFuture#getCallbackType()} method will be
     * INITIAL.  If onProgress is being called back to to an actual
     * progression of the system (up or down) then the 
     * {@link ChangeableRunLevelFuture#getCallbackType()} method will
     * return PROGRESSION.
     *
     * @param currentJob the job currently running
     * @param levelAchieved the level just achieved by the currentJob.  Note
     * that if the currentJob is going up then the levelAchieved will
     * be the level for which all the services in that level were just started.
     * When going down the levelAchieved will be the level for which
     * all the services ABOVE that level have been shutdown.  In both cases
     * the levelAchieved represents the current level of the system
     */
    void onProgress(ChangeableRunLevelFuture currentJob, int levelAchieved);
    
    /**
     * Called when an RunLevelController implementation's proceedTo() operation
     * has been canceled for some reason.
     * <p>
     * Neither {@link RunLevelController#proceedTo(int)} nor
     * {@link RunLevelController#proceedToAsync(int)} may be called from this method
     * <p>
     * Any exception thrown from this method is ignored
     *
     * @param currentJob the job currently running
     * @param levelAchieved the level just achieved by the currentJob.  Note
     * that if the currentJob is currently going up then the levelAchieved will
     * be the level for which all the services in that level were just started
     * while when going down the levelAchieved will be the level for which
     * all the services ABOVE that level have been shutdown.  In both cases
     * the levelAchieved represents the current level of the system
     */
    void onCancelled(RunLevelFuture currentJob, int levelAchieved);

    /**
     * Called when a service throws an exception during a proceedTo
     * operation
     * <p>
     * Neither {@link RunLevelController#proceedTo(int)} nor
     * {@link RunLevelController#proceedToAsync(int)} may be called from this method
     * <p>
     * Any exception thrown from this method is ignored
     * 
     * @param currentJob    the run level controller
     * @param errorInformation information about the error that had been caught
     */
    void onError(RunLevelFuture currentJob, ErrorInformation errorInformation);

    
}
