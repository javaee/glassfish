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

package org.glassfish.hk2.runlevel;


import org.glassfish.hk2.api.ActiveDescriptor;
import org.jvnet.hk2.annotations.Contract;


/**
 * Implementations of this contract are responsible for orchestration
 * lifecycle events (i.e., start levels) in Hk2.
 * <p>
 * Each run level service is responsible for a particular class of
 * {@link RunLevel} that is identified by a {@link RunLevelControllerIndicator}
 * value.
 * <p>
 * Implementations of this service are responsible for orchestrating
 * lifecycle events for services annotated with {@link RunLevel}.
 * <p>
 *
 * @author jtrent, tbeerbower
 */
@Contract
public interface RunLevelController {

    // ----- Methods --------------------------------------------------------
    
    /**
     * Causes this RunLevelController to move to the specified run level for
     * all {@link RunLevel} instances (identified by
     * {@link RunLevelControllerIndicator}), orchestrating the appropriate
     * lifecycle events.
     * <p>
     * If the run level specified is the same as the current run level then
     * the RunLevelController may return immediately.
     * <p>
     * Note that the underlying implementation may perform this operation
     * asynchronously. Implementors who choose the asynchronous approach
     * are expected to treat a subsequent proceedTo(newRunLevel) call as
     * an implicit cancellation of any currently running proceedTo() that
     * is running on one or more managed threads.
     * <p>
     * This method will use the default activator which either starts or
     * stops the services depending on whether or not the runLevel is
     * coming up or going down
     *
     * @param runLevel  the run level to move to
     */
    void proceedTo(int runLevel);

    /**
     * Causes this RunLevelController to attempt to stop any in-flight
     * proceedTo() operation.  This call will not have any effect if
     * there is no current proceedTo() operation in progress.
     */
    void interrupt();

    /**
     * The current run level state.  This represents the last run level
     * successfully achieved by the underlying RunLevelController responsible
     * for this scope.
     *
     * @return the current run level, or null if no run level has been
     *         been achieved
     */
    Integer getCurrentRunLevel();

    /**
     * The planned run level state.
     *
     * @return the planned run level, or null if there is no planned level
     */
    Integer getPlannedRunLevel();

    /**
     * Record the activation the run level service associated with the given
     * descriptor.
     *
     * @param descriptor  the descriptor
     */
    public void recordActivation(ActiveDescriptor<?> descriptor);
}
