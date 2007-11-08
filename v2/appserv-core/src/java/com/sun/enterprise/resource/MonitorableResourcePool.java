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

package com.sun.enterprise.resource;

/**
 * An interface that adds 8.1 monitoring capabilities to the ResourcePool
 * interface 
 * @author Sivakumar Thyagarajan
 * @since 8.1
 */
public interface MonitorableResourcePool extends ResourcePool {
    
    /**
     * Return the number of threads that are waiting
     * to obtain a connection from the pool
     */
    public int getNumThreadWaiting();
    
    /**
     * Return the number of connections that have failed validation
     */
    public long getNumConnFailedValidation();
    
    /**
     * Return the number of threads that have time out after
     * waiting to obtain a connection from the pool.
     */
    public long getNumConnTimedOut();
    
    /**
     * Return the number of free connections in the pool
     */
    public long getNumConnFree();
    public long getMaxNumConnFree();
    public long getMinNumConnFree();
    
    /**
     * Return the number of connections in use 
     */
    public long getNumConnInUse();
    public long getMinNumConnUsed();
    
    /**
     * Return the maximum number of connections ever used in
     * this pool
     */
    public long getMaxNumConnUsed();
    
    //8.1 pool monitoring statistics
    public long getCurrentConnRequestWaitTime();
    public long getMaxConnRequestWaitTime();
    public long getMinConnRequestWaitTime();
    public long getTotalConnectionRequestWaitTime();
    
    
    public long getNumConnCreated();
    public long getNumConnDestroyed();
    
    public long getNumConnAcquired();
    public long getNumConnReleased();
    
    public long getNumConnSuccessfullyMatched();
    public long getNumConnNotSuccessfullyMatched();
    
    //9.1 pool monitoring statistics
    public long getNumPotentialConnLeak();
}
