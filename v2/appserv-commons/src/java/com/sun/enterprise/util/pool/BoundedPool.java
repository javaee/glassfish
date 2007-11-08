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
 * Filename: BoundedPool.java	
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
 * <BR> <I>$Source: /cvs/glassfish/appserv-commons/src/java/com/sun/enterprise/util/pool/BoundedPool.java,v $</I>
 * @author     $Author: tcfujii $
 * @version    $Revision: 1.3 $ $Date: 2005/12/25 04:12:26 $
 */
 
package com.sun.enterprise.util.pool;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

import com.sun.enterprise.util.collection.DList;
import com.sun.enterprise.util.collection.DListNode;

import com.sun.enterprise.util.scheduler.PeriodicallyServicable;
import com.sun.enterprise.util.scheduler.PeriodicEventScheduler;

import com.sun.enterprise.util.ApproximateClock;
import com.sun.enterprise.util.collection.DListNode;
import com.sun.enterprise.util.collection.DListNodeFactory;


public class BoundedPool
    extends ObjectPool
{

	/**
	 * Create an Unbounded pool.
	 * @param The ObjectFactory to create objects
	 * @param The minimum number of objects to be held in the pool (initial size)
	 * @param The pool limit (maximum number of objects in the pool).
	 * @param The maximum idle time after which the object may be removed from the pool.
	 */
    public BoundedPool(ObjectFactory factory, int minSize, int maxLimit, long maxIdleTime) {
    	super(factory, minSize, minSize, maxLimit, maxIdleTime);
    }
    
	/**
	 * Create an Unbounded pool.
	 * @param The ObjectFactory to create objects
	 * @param The minimum number of objects to be held in the pool (initial size)
	 * @param The pool limit (maximum number of objects in the pool).
	 * @param The maximum idle time after which the object may be removed from the pool.
	 * @param The initial size of the pool. If this is less than the minSize parameter then this is ignored.
	 * @param The maximum number of strong references that are to held. The difference between
	 *	maxLimit and maxStrongRefs gives the number of objects held in the pool through SoftReferences.
	 */
    public BoundedPool(ObjectFactory factory, int minSize, int maxLimit, long maxIdleTime,
    		int initialSize)
    {
    	super(factory, minSize, initialSize, maxLimit, maxIdleTime);
    }
    
    
}