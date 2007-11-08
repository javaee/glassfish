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
 * <BR> <I>$Source: /cvs/glassfish/appserv-commons/src/java/com/sun/enterprise/util/pool/SoftObjectPool.java,v $</I>
 * @author     $Author: dpatil $
 * @version    $Revision: 1.4 $ $Date: 2006/11/04 01:39:28 $
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

import com.sun.enterprise.util.collection.FastStack;
//Bug 4677074 begin
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
//Bug 4677074 end


public class SoftObjectPool
    extends com.sun.enterprise.util.pool.AbstractPool
    implements PeriodicallyServicable
{
//Bug 4677074 begin
    static Logger _logger=LogDomains.getLogger(LogDomains.UTIL_LOGGER);
//Bug 4677074 end
	protected DList		list;
	protected int		minSize;
	protected int		initialSize;
	protected int		maxLimit;
	protected long		maxIdleTime;
	protected int		maxStrongRefs;
	
	protected Boolean	isBounded;
	
	/**
	 * Create an Unbounded pool.
	 * @param The ObjectFactory to create objects
	 * @param The minimum number of objects to be held in the pool (initial size)
	 * @param The initial size of the pool. If this is less than the minSize parameter then this is ignored.
	 * @param The maximum idle time after which the object may be removed from the pool.
	 * @param The pool limit (maximum number of objects in the pool).
	 */
    public SoftObjectPool(ObjectFactory factory, int minSize, int initialSize, 
    			long maxIdleTime, int maxStrongRefs) {
        super();
        super.factory = factory;
        this.minSize = minSize;
        this.initialSize = initialSize;
        this.maxIdleTime = maxIdleTime;
        this.maxStrongRefs = maxStrongRefs;
        
        setMaxLimit(-1);
        
        initPool();
    }
    
	/**
	 * Create a Bounded pool.
	 * @param The ObjectFactory to create objects
	 * @param The minimum number of objects to be held in the pool (initial size)
	 * @param The pool limit (maximum number of objects in the pool).
	 * @param The maximum idle time after which the object may be removed from the pool.
	 * @param The initial size of the pool. If this is less than the minSize parameter then this is ignored.
	 */
    public SoftObjectPool(ObjectFactory factory, int minSize, int initialSize, int maxLimit,
    				long maxIdleTime, int maxStrongRefs)
    {
        super();
        super.factory = factory;
        this.minSize = minSize;
        this.maxIdleTime = maxIdleTime;
        this.initialSize = initialSize;
        this.maxStrongRefs = maxStrongRefs;
        
        setMaxLimit(maxLimit);
        
        initPool();
    }
    
    public int getMaxLimit() {
    	return this.maxLimit;
    }
    
    public void setMaxLimit(int limit) {
        if ((limit <= 0) || (limit >= Integer.MAX_VALUE-1)) {
        	this.isBounded = null;
        } else {
        	this.isBounded = Boolean.valueOf(true);
        	this.maxLimit = limit;
        }
    }
    	
    
    private void initPool() {
        list = new DList();
        
        super.collection = list;
        super.preload((minSize < initialSize) ? initialSize : minSize);
        
        scheduler.addTimeRepeatableTask(this, (int) maxIdleTime);
    }
    
    /**
     * Since this method would be called only if the pool is empty
     */
    protected boolean canCreate() {
        return (isBounded == null) ? true : (createdCount < maxLimit);
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
		int size = list.size();
		long now = _clock.getTime();

		if (size < maxStrongRefs) {
			list.addAsLastNode(new TimeStampedSoftDListNode(object, now, object));
		} else {
			list.addAsLastNode(new TimeStampedSoftDListNode(new SoftReference(object), now, null));
		}

    	return this;
    }
    			
    private Object obtainObject(Object param) {
		SoftReference ref;
		Object object = null;
		int notifyCount = 0;
		
		for (int size = list.size(); size > 0; size--) {
			TimeStampedSoftDListNode tsNode = (TimeStampedSoftDListNode) list.getDListNodeAt(0);
			list.delink(tsNode);
			
			if (tsNode.isSoftRef == null) {
				ref = (SoftReference) tsNode.object;
				if ((object = ref.get()) != null) {
					break;
				} else {
					notifyCount++;
				}
			} else {
				object = tsNode.object;
				break;
			}
		}
			
		if (object == null) {
			try {
				object = factory.create(param);
				afterCreate(object);
			} catch (PoolException poolEx) {
				
			}
		}
		
		super.createdCount -= notifyCount;
		
		if (notifyCount == 1) {
			super.collection.notify();
		} else if (notifyCount > 1) {
			super.collection.notifyAll();
		}
		
		return object;
    }
    			
	/**
	 * Notification when an object is given out from the pool (checout).
	 * @return The object that has to be returned to the application. A null
	 *	value must be returned if no object can be returned to the application. Since this 
	 *	class always returns the last node from the list, it returns non-null value. 
	 *	Subclasses can override this behaviour.
	 */
	protected Object checkout(Object param) {
		return obtainObject(param);
    }
    
  
    
    //Methods required for periodically schedulable task
    
    public void prolog() {
    }
    
    public void service() {
   		int killedCount = 0;
   		
    	long now = _clock.getTime();
    	long allowed = now - maxIdleTime;
   		
   		TimeStampedSoftDListNode tsNode = null;
   		FastStack stack = new FastStack();
   		
   		synchronized (super.collection) {

   			Object done = null;
   			while (done == null) {
   				tsNode = (TimeStampedSoftDListNode) list.getFirstDListNode();
	   			
   				if (tsNode == null) {	//Empty list
   					done = new Object();
   				} else if (tsNode.timeStamp <= allowed) {
    				//Need to destroy the contained object
    				list.delink(tsNode);
    				stack.push(tsNode.object);
    				killedCount++;
    			} else {
    				//This node is not old enough
   					done = new Object();
    			}
    		}	//End of for loop
    		
    		super.createdCount -= killedCount;
    		
    		int deficit = list.size() - minSize;
    		super.preload(0 - deficit);
    		
    		if (killedCount == 0) {
    		} else if (killedCount == 1) {
    			collection.notify();
    		} else {
    			collection.notifyAll();
    		}
    		
    	} // end of synchronized
    	
    	
    	//Now destroy all collected objects
    	while (! stack.isEmpty()) {
    		Object object = stack.pop();
			beforeDestroy(object);
   			factory.destroy(object);
    	}
    	
//Bug 4677074    	System.out.println("Leaving service after killing " + killedCount + " (idle) objects. Now size: " + list.size());
//Bug 4677074 begin
		_logger.log(Level.FINE,"Leaving service after killing " + killedCount + " (idle) objects. Now size: " + list.size());
//Bug 4677074 end
    }
    
    public void epilog() {
    }
    
    /**
    * Get the frequency (time interval) at which service() method will be invoked.
    * @return time in milli seconds.
    */
    public long getFrequency() {
    	return this.maxIdleTime;
    }
    
    /**
    * Determine to execute the service method of this object even if it has 
    * missed the right schedule.
    */
    public boolean getExecuteIfMissed() {
    	return true;
    }
    
    /**
    * Determine to execute the service method of this object when the 
    * schedule is delayed by 'missedByMillis' milli seconds.
    */
    public boolean getExecutionTolerance(long missedByMillis) {
    	return true;
    }
    
    /**
    * Print an identification for the object.
    */
    public String toString() {
    	return "";
    }
    
   //Some helper classes
   
	class TimeStampedSoftDListNode
		extends DListNode
	{
		long		timeStamp;
		Object		isSoftRef;
		
		public TimeStampedSoftDListNode(Object object, long ts, Object isSoftRef) {
			super(object);
			this.timeStamp = ts;
			this.isSoftRef = isSoftRef;
				
//Bug 4677074			System.out.println(this + ": created DListNode at: " + ts);
//Bug 4677074 begin
			_logger.log(Level.FINE,this + ": created DListNode at: " + ts);
//Bug 4677074 end
		}
		
    	public String toString() {
    		return "TSDListNode: " + object + "; isSoftRef: " + isSoftRef;
    	}
    		    	
		
	}
    
    
}
