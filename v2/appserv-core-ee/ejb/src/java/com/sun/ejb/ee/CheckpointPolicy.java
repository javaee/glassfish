/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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


import java.lang.reflect.Method;
import com.sun.enterprise.deployment.EjbDescriptor;

/**
 * CheckpointPolicy is an SPI that will be implemented by
 * S1AS EE and called by the EJB container.   It allows EE
 * to make the policy decision about whether a checkpoint
 * is required after certain events in the stateful session
 * bean lifecycle.   The actual checkpointing is performed
 * by the EJB container.  
 */
public interface CheckpointPolicy {

    /**
     * Called by EJB container to set the EJB descriptor for which
     * the checkpointing decisions will be made.
     * 
     */
    void setDescriptor(EjbDescriptor ejbDescriptor);


    //
    // All possible stateful session bean checkpointing boundaries.
    //

    // An ejbCreate method just finished successfully.
    int EJB_CREATE    = 0;

    // A business method just completed successfully.
    int END_OF_METHOD = 1;

    // A transaction in which this stateful session bean was running
    // completed successfully.  
    int TX_BOUNDARY   = 2;

    
    /**
     * Called by EJB container to determine whether the current
     * stateful session ejb state should be checkpointed.
     *
     * @param which checkpoint boundary has just been reached. 
     * @param method associated with the checkpointing boundary.  This
     *    is not applicable in the TX_BOUNDARY case.
     *
     * @return If true, EJB container will checkpoint the stateful 
     *   session bean state.  
     *
     */
    boolean doCheckpoint(int checkpointBoundary, Method method);

}





