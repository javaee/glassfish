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
 * Filename: AbstractPool.java	
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
 * <BR> <I>$Source: /cvs/glassfish/appserv-commons/src/java/com/sun/enterprise/util/pool/ArrayListPool.java,v $</I>
 * @author     $Author: tcfujii $
 * @version    $Revision: 1.3 $ $Date: 2005/12/25 04:12:26 $
 */
 

package com.sun.enterprise.util.pool;

import java.util.Collection;
import java.util.ArrayList;

/**
 * <p>Abstract pool provides the basic implementation of an object pool. The implementation
 *	uses a linked list to maintain a collection of (available) objects. If the pool is
 *	empty it simply creates one using the ObjectFactory instance. Subclasses can change
 *	this behaviour by overriding getObject(...) and returnObject(....) methods. This
 *	class provides basic support for synchronization, event notification, pool shutdown
 *	and pool object recycling. It also does some very basic bookkeeping like the
 *	number of objects created, number of threads waiting for object.
 *	<p> Subclasses can make use of these book-keeping data to provide complex pooling
 *	mechanism like LRU / MRU / Random. Also, note that AbstractPool does not have a
 *	notion of  pool limit. It is upto to the derived classes to implement these features.
 * <p>This class does not define the canCreate() method.
 */
public abstract class ArrayListPool
    extends AbstractPool
{
	protected ArrayList		arrayList;
	
	protected ArrayListPool(ObjectFactory factory) {
		super.factory = factory;
		super.collection = this.arrayList = new ArrayList(6);	
	}
	
	protected ArrayListPool(ObjectFactory factory, int initialCapacity) {
		super.factory = factory;
		super.collection = this.arrayList = new ArrayList(initialCapacity);	
	}
	
	/**
	 * Notification when an object is put back into the pool (checkin).
	 * @param The object to be returned back to the pool.
	 * @return Any non null value can be returned to signal that the object
	 *	was indeed added to the pool. This class always adds the object to the 
	 *	pool (at the end of the collection), it returns non-null value. 
	 *	Subclasses can override this behaviour.
	 */
	protected Object checkin(Object object) {
		collection.add(object);
		return this;
	}
    			
	/**
	 * Notification when an object is given out from the pool (checout).
	 * @return The object that has to be returned to the application. A null
	 *	value must be returned if no object can be returned to the application. Since this 
	 *	class always returns the last node from the collection, it returns non-null value. 
	 *	Subclasses can override this behaviour.
	 */
	protected Object checkout() {
		return arrayList.remove(arrayList.size() - 1);
	}
    			
	/**
	 * Notification when an object is given out from the pool (checout).
	 * @return The object that has to be returned to the application. A null
	 *	value must be returned if no object can be returned to the application. Since this 
	 *	class always returns the last node from the collection, it returns non-null value. 
	 *	Subclasses can override this behaviour.
	 */
	protected Object checkout(long param) {
		return arrayList.remove(arrayList.size() - 1);
	}
    			
	/**
	 * Notification when an object is given out from the pool (checout).
	 * @return The object that has to be returned to the application. A null
	 *	value must be returned if no object can be returned to the application. Since this 
	 *	class always returns the last node from the collection, it returns non-null value. 
	 *	Subclasses can override this behaviour.
	 */
	protected Object checkout(Object param) {
		return arrayList.remove(arrayList.size() - 1);
	}
}