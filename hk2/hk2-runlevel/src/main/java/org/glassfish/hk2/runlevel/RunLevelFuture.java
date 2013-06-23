/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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

import java.util.concurrent.Future;

/**
 * This is the Future object that will be returned by the
 * RunLevelController and it contains extra information about
 * the job being done
 * 
 * @author jwells
 *
 */
public interface RunLevelFuture extends Future<Object> {
    /**
     * This gets the level that this future job is attempting
     * to get to
     * 
     * @return The level that this future job is attempting
     * to go to
     */
    public int getProposedLevel();
    
    /**
     * Returns true if this job represents the system going from
     * a lower level to a higher level.  This method and isDown
     * can both be false (for the case that proceedTo asked to go
     * to the level it is already at) but they cannot both be true
     * 
     * @return true if this job was proceeding from a lower level
     * to a higher level
     */
    public boolean isUp();
    
    /**
     * Returns true if this job represents the system going from
     * a higher level to a lower level.  This method and isUp
     * can both be false (for the case that proceedTo asked to go
     * to the level it is already at) but they cannot both be true
     * 
     * @return true if this job was proceeding from a higher level
     * to a lower level
     */
    public boolean isDown();
    
    /**
     * The cancel method attempts to cancel the current running
     * job (if the job is not already completed or already cancelled).
     * The meaning of cancel is different depending on whether or
     * not the system was going up to a level or coming down to
     * a level.
     * <p>
     * If the system was going up to a level then calling cancel
     * will cause the system to stop going up, and instead proceed
     * back down to the last completed level.  For example, suppose
     * there were three services at level ten and the system was
     * going up to level ten.  As the system was proceeding up to
     * level ten the first of the three services had already been
     * started and the second service was in progress and the third
     * service had not been started.  The system will wait for the second
     * service to complete coming up and then will shut it down along
     * with the first service.  Since the last completed level was nine,
     * the system will remain at level nine and this job will be complete.
     * <p>
     * If the system was going down to a level then calling cancel
     * will cause the system to continue going down, but it will stop
     * going down at the next level.  For example, suppose there were
     * three services at level ten and the current proposed level is
     * five.  Suppose one of those three services had already been shutdown
     * and one was in the process of being shutdown and the other had
     * not yet been shutdown when the cancel arrives.  The system will
     * continue to shutdown the one in progress and then will shutdown
     * the remaining service at level ten to reach level nine.  However,
     * the job will no longer attempt to go down to level five, but will
     * instead be finished at level nine.
     * <p>
     * @param mayInterruptIfRunning is currently ignored
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning);
    
    
}
