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

package com.sun.enterprise.web.connector.grizzly;

/**
 * A interface used to define the execution of a <code>AsyncTask</code>
 * By default, <code>AsyncTask</code> will invoke an implementation
 * of this interface in this order:
 * 
 * (1) preExecute()
 * (2) interrupt()
 * (3) postExecute()
 *
 * Implementation of this interface must decide when a task must be interrupted.
 *
 * @author Jeanfrancois Arcand
 */
public interface AsyncExecutor {
    
    /**
     * Pre-execute some operations in the <code>AsycnProcesssorTask</code>
     * associated. 
     * @return true if the processing can continue.
     */
    public boolean preExecute() throws Exception;
    
    
    /**
     * Execute some operations on the <code>AsycnProcesssorTask</code> and then
     * interrupt it.
     * @return true if the processing can continue, false if it needs to be 
     *              interrupted.
     */    
    public boolean interrupt() throws Exception;
    
    
   /**
     * Execute the main operation on
     * @return true if the processing can continue, false if it needs to be 
     *              interrupted.
     */    
    public boolean execute() throws Exception;    
    
    /**
     * Post-execute some operations in the <code>AsycnProcesssorTask</code>
     * associated.
     * @return true if the processing can continue.
     */    
    public boolean postExecute() throws Exception;
    
    
    /**
     * Set the <code>AsycnProcesssorTask</code>.
     */
    public void setAsyncTask(AsyncTask task);

    
    /**
     * Get the <code>AsycnProcesssorTask</code>.
     */    
    public AsyncTask getAsyncTask();
    
    
    /**
     * Add a <code>AsyncFilter</code>
     */
    public void addAsyncFilter(AsyncFilter asyncFilter);
    
    
    /**
     * Remove an <code>AsyncFilter</code>
     */
    public boolean removeAsyncFilter(AsyncFilter asyncFilter);
    
        
    /**
     * Get the <code>AsyncHandler</code> who drive the asynchronous process.
     */
    public AsyncHandler getAsyncHandler();
    
    
    /**
     * Set the <code>AsyncHandler</code> who drive the asynchronous process.
     */
    public void setAsyncHandler(AsyncHandler asyncHandler);
}
