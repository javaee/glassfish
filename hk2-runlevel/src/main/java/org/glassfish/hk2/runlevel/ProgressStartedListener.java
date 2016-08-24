/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 * to be informed when the system starts progressing to a new level, either
 * upward or downward
 * <p>
 * Lengthy operations should not be performed in the listener since
 * that may impact the performance of the RunLevelController calling the
 * listener
 *
 * @author jwells
 */
@Contract
public interface ProgressStartedListener {
    /**
     * Called when the RunLevelController starts progressing to a new
     * level but before any work has been done yet
     * <p>
     * Neither {@link RunLevelController#proceedTo(int)} nor
     * {@link RunLevelController#proceedToAsync(int)} may be called from this method.  However,
     * {@link ChangeableRunLevelFuture#changeProposedLevel(int)} may be called
     * <p>
     * Any exception thrown from this method is ignored
     * <p>
     *
     * @param currentJob the job currently running
     * @param currentLevel the level that the system is currently at before
     * any work has been done to move the system up or down
     */
    public void onProgressStarting(ChangeableRunLevelFuture currentJob, int currentLevel);

}
