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

import org.glassfish.hk2.api.Descriptor;

/**
 * @author jwells
 *
 */
public interface ErrorInformation {
    /**
     * The set of actions that the system can perform
     * when an error is detected
     * 
     * @author jwells
     */
    public enum ErrorAction {
        /**
         * Tells the RunLevelController to halt progress
         * in the level and proceed to the next lowest
         * level and stop the proceeding at that level.
         * This is the default action when an error is
         * encountered while the system is proceeding
         * upward.  The error (or errors) will be thrown
         * by the {@link RunLevelFuture}
         */
        GO_TO_NEXT_LOWER_LEVEL_AND_STOP,
        
        /**
         * Tells the RunLevelController to disregard
         * the error and continue its progress as if
         * the error never happened.  This is the default
         * action when an error is encountered while
         * the system is proceeding downward.  The error
         * (or errors) will NOT be thrown by the
         * {@link RunLevelFuture}
         */
        IGNORE
    }
    
    /**
     * Returns the throwable that caused the error
     * @return The non-null throwable that caused
     * the error to occur
     */
    public Throwable getError();
    
    /**
     * Returns the action the system will take
     * 
     * @return The action the system will take
     * once the onError method has returned
     */
    public ErrorAction getAction();
    
    /**
     * Sets the action the system should take
     * 
     * @param action The action the system will take
     * once the onError method has returned
     */
    public void setAction(ErrorAction action);
    
    /**
     * Returns the descriptor associated with this failure,
     * or null if the descriptor could not be determined
     * 
     * @return The failed descriptor, or null if the
     * descriptor could not be determined
     */
    public Descriptor getFailedDescriptor();

}
