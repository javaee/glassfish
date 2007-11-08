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
 * Filename: UnboundedPool.java	
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
 * <BR> <I>$Source: /cvs/glassfish/appserv-commons/src/java/com/sun/enterprise/util/pool/UnboundedPool.java,v $</I>
 * @author     $Author: tcfujii $
 * @version    $Revision: 1.3 $ $Date: 2005/12/25 04:12:29 $
 */
 
package com.sun.enterprise.util.pool;

/**
 * An UnboundedPool can be used to create a pool of unlimited size. All getObject(....) methods
 *	are guaranteed to return an object irrespective of the wait flag and wait time. Note that 
 *	if the objects held in the pool consume a siginificant memory, then maintaining a large
 *	UnboundedPool may cause java.lang.OutOfMemory error (which probably would not have occured
 *	if there was no pooling!!). If memory is an issue then use SoftUnboundedPool.
 *
 * <p> The initial size of the pool and the load factor of the pool determine how the
 *	pool size adjusts dynamically. For example, if the initial pool size is 100 and if the load factor
 *	is 90, then as soon as 90% of the pool objects are used (given out), then the pool size grows
 *	by 10.
 */
public class UnboundedPool
    extends ArrayListPool
{
	private int		initialSize;
	
	/**
	 * Create an Unbounded pool.
	 * @param The ObjectFactory to create objects
	 * @param The initial number of objects to be held in the pool
	 * @param The load factor. This value indicates when and how much the pool
	 *	should expand / shrink. Both initialSize and loadFactor are used to
	 *	compute the new size during expansion / shrinking.
	 */
    public UnboundedPool(ObjectFactory factory, int initialSize) {
        super(factory, initialSize);
        this.initialSize = initialSize;
        super.preload(initialSize);
    }
    
    /**
     * Since this method would be called only if the pool is empty,
     *	and since this an unbounded pool, CREATE IT!!
     */
    protected boolean canCreate() {
        return true;
    }
    
	/**
	 * Notification when an object is put back into the pool (checkin).
	 * @param The object to be returned back to the pool.
	 * @return Any non null value can be returned to signal that the object
	 *	was indeed added to the pool. This class always adds the object to the 
	 *	pool (at the end of the list), it returns non-null value. 
	 *	Subclasses can override this behaviour.
	 */
	protected Object checkin(Object object) {
		if (waitCount == 0) {
			int diff = arrayList.size() - initialSize;
			if (diff > initialSize) {
				super.destroyPoolObjects(diff);
			}
		}
		
		arrayList.add(object);
    	return this;
    }
    
}