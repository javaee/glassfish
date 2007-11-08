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

//NOTE: Tabs are used instead of spaces for indentation. 
//  Make sure that your editor does not replace tabs with spaces. 
//  Set the tab length using your favourite editor to your 
//  visual preference.

/*
 * Filename: Pool.java	
 *
 * Copyright 2000-2001 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of iPlanet/Sun Microsystems, Inc. ("Confidential Information").
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license 
 * agreement you entered into with iPlanet/Sun Microsystems.
 */
 
/**
 * <BR> <I>$Source: /cvs/glassfish/appserv-commons/src/java/com/sun/enterprise/util/pool/Pool.java,v $</I>
 * @author     $Author: tcfujii $
 * @version    $Revision: 1.3 $ $Date: 2005/12/25 04:12:27 $
 */
 
package com.sun.enterprise.util.pool;

/**
 * Pool defines the methods that can be used by the application to access pooled objects. The basic
 *	assumption is that all objects in the pool are identical (homogeneous). This interface defines
 *	methods for a) getting an object from the pool, b) returning an object back to the pool 
 *	and, c) destroying (instead of reusing) an object. In addition to these methods, the Pool has 
 *	methods for adding and removing PoolEventListeners. There are six overloaded methods for 
 *	getting objects from a pool.
 */
public interface Pool {
    
    /**
     * Get an object.
     * @param toWait - true indicates that the calling thread agrees to wait indefinitely false if not.
     * @param param - some value that might be used while creating the object
     * @return an Object or null if an object could not be returned in 'waitForMillis' millisecond.
     * @exception Throws InterruptedException if the calling thread was interrupted during the call.
     * @exception Throws PoolException if the underlying pool implementation throws specific exception
     *		(for example, the pool is closed etc.)
     */
	public Object getObject(boolean toWait, Object param)
		throws PoolException, InterruptedException;
    
    /**
     * Get an object from the pool within the specified time.
     * @param The amount of time the calling thread agrees to wait.
     * @param Some value that might be used while creating the object
     * @return an Object or null if an object could not be returned in 'waitForMillis' millisecond.
     * @exception Throws InterruptedException if the calling thread was interrupted during the call.
     * @exception Throws PoolException if the underlying pool implementation throws specific exception
     *		(for example, the pool is closed etc.)
     */
    public Object getObject(long waitForMillis, Object param)
		throws PoolException, InterruptedException;

   
    /**
     * Return an object back to the pool. An object that is obtained through
     *	getObject() must always be returned back to the pool using either 
     *	returnObject(obj) or through destroyObject(obj).
     */
    public void returnObject(Object obj);
    			
    /**
     * Destroys an Object. Note that applications should not ignore the reference
     *	to the object that they got from getObject(). An object that is obtained through
     *	getObject() must always be returned back to the pool using either 
     *	returnObject(obj) or through destroyObject(obj). This method tells that the
     *	object should be destroyed and cannot be reused.
     */
    public void destroyObject(Object obj);
    	
    /**
    * Add a PoolListener
    * @param listener The pool listener
    */
    public boolean addPoolListener(PoolListener listener);
    
    /**
    * Add a PoolListener
    * @param listener The pool listener
    */
    public boolean removePoolListener(PoolListener listener);
    
    	
    
}
