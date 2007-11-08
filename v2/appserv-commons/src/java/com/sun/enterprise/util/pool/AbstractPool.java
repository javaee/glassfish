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
 * <BR> <I>$Source: /cvs/glassfish/appserv-commons/src/java/com/sun/enterprise/util/pool/AbstractPool.java,v $</I>
 * @author     $Author: llc $
 * @version    $Revision: 1.4 $ $Date: 2007/04/03 00:35:49 $
 */
 

package com.sun.enterprise.util.pool;

import java.util.Collection;
import java.util.ArrayList;

import com.sun.enterprise.util.scheduler.PeriodicallyServicable;
import com.sun.enterprise.util.scheduler.PeriodicEventScheduler;

import com.sun.enterprise.util.ApproximateClock;
//Bug 4677074 begin
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
//Bug 4677074 end

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
 */
public abstract class AbstractPool
    implements Pool
{
//Bug 4677074 begin
    static Logger _logger=LogDomains.getLogger(LogDomains.UTIL_LOGGER);
//Bug 4677074 end
    protected boolean           bDebug=false;
	protected Collection		collection;
	protected ArrayList			listeners;
    protected ObjectFactory		factory = null;
    protected int				waitCount = 0;
    protected int				createdCount = 0;

    protected Object			onHold = null;
    protected Object			closed = null;
    
    protected static final ApproximateClock _clock = new ApproximateClock(15000);    
    protected PeriodicEventScheduler	scheduler;
    
    
    protected AbstractPool() {
    	scheduler = PeriodicEventScheduler.getInstance();
    }
    
    
    /**
     * Get an object. Application can use pool.getObject() to get an object
     *	instead of using new XXX().
     * @param canWait Must be true if the calling thread is willing to wait for infinite time
     *	to get an object, false if the calling thread does not want to wait at all.
     * @exception Throws InterruptedException if the thread was interrupted while waiting
     */
	public Object getObject(boolean canWait, Object param)
		throws InterruptedException, PoolException
	{
		Object object;

		if (closed != null) {
			throw new PoolException("Pool closed. Cannot obtain object");	
		}
		
		synchronized (collection) {
			while (true) {
				if (collection.size() > 0) {
					if ( (object = checkout(param)) != null) {
						return object;
					}
				} else if (canCreate()) {
					createdCount++;
					break;
				}
					
				if (canWait) {
					try {
						waitCount++;
						beforeWait(param);
						collection.wait();
						afterNotify(param);
						waitCount--;
					} catch (InterruptedException inEx) {
						throw new RequestInterruptedException("InterruptedException", inEx);
					}
				} else {
					return null;
				}
			}
		}
			
		try {
			object = factory.create(param);
		} catch (PoolException poolEx) {
			synchronized (collection) {
				createdCount--;
			}
			throw poolEx;
		}
		afterCreate(object);
					
		return object;
	}
    
    /**
     * Get an object. Application can use pool.getObject() to get an object
     *	instead of using new XXX(). The method throws TimedoutException
     *	if an object could not be returned in 'waitForMillis' millisecond.
     * @param waitFor the amount of time the thread is willing to wait.
     * @exception Throws InterruptedException if the thread was interrupted while waiting
     * @exception Throws TimedoutException if an object could not be obtained from the pool
     *	within the specified time.
     */
    public Object getObject(long waitFor, Object param)
		throws InterruptedException, PoolException
	{

		if (closed != null) {
			throw new PoolException("Pool closed. Cannot obtain object");	
		}
		
	    long now = _clock.getTime();
    	long timeLeft = waitFor;
		long startTime = now;
		Object object;
		synchronized (collection) {
			while (true) {
				if (collection.size() > 0) {
					if ( (object = checkout(param)) != null) {
						return object;
					}
				} else if (canCreate()) {
					createdCount++;
					break;
				}
				
				if (timeLeft > 0) {
					try {
						waitCount++;
						beforeWait(param);
						collection.wait();
						afterNotify(param);
						waitCount--;
					} catch (InterruptedException inEx) {
						throw new RequestInterruptedException("InterruptedException", inEx);
					}
				} else {
					return null;
				}
				now = _clock.getTime();
        		timeLeft =  now - startTime;
        		startTime = now;
			}
		}
		
		try {
			object = factory.create(param);
		} catch (PoolException poolEx) {
			synchronized (collection) {
				createdCount--;
			}
			throw poolEx;
		}
		afterCreate(object);

		return object;
    }

    /**
     * Return an object back to the pool. An object that is obtained through
     *	getObject() must always be returned back to the pool using either 
     *	returnObject(obj) or through destroyObject(obj).
     */
    public void returnObject(Object object) {
    	synchronized (collection) {
    		if (closed != null) {
    			if (waitCount == 0) {
    				destroyObject(object);
    				return;
    			}
    		}
    		
    		checkin(object);
    		if (waitCount > 0) {
    			collection.notify();
	    	}
    	}
    }

    /**
     * Destroys an Object. Note that applications should not ignore the reference
     *	to the object that they got from getObject(). An object that is obtained through
     *	getObject() must always be returned back to the pool using either 
     *	returnObject(obj) or through destroyObject(obj). This method tells that the
     *	object should be destroyed and cannot be reused.
     */
    public void destroyObject(Object object) {
		beforeDestroy(object);
    	factory.destroy(object);
    	synchronized (collection) {
    		createdCount--;
    		if (waitCount > 0) {
    			collection.notify();
    		}
    	}
    }
    
	/**
	 * Notification when an object is put back into the pool (checkin).
	 * @param The object to be returned back to the pool.
	 * @return Any non null value can be returned to signal that the object
	 *	was indeed added to the pool. This class always adds the object to the 
	 *	pool (at the end of the collection), it returns non-null value. 
	 *	Subclasses can override this behaviour.
	 */
	protected abstract Object checkin(Object object);
    			
	/**
	 * Notification when an object is given out from the pool (checout).
	 * @return The object that has to be returned to the application. A null
	 *	value must be returned if no object can be returned to the application. Since this 
	 *	class always returns the last node from the collection, it returns non-null value. 
	 *	Subclasses can override this behaviour.
	 */
	protected abstract Object checkout(Object param);
    			
    
    /**
    * Add a PoolListener
    * @param listener The pool listener
    */
    public boolean addPoolListener(PoolListener listener) {
    	synchronized (this) {
    		if (listeners == null) {
    			listeners = new ArrayList();
    			listeners.add(listener);
    			return true;
    		}
    		
    		if (listeners.indexOf(listener) == -1) {
    			listeners.add(listener);
    			return true;
    		} else {
    			return false;
    		}
    	}
    }

    
    /**
    * Add a PoolListener
    * @param listener The pool listener
    */
    public boolean removePoolListener(PoolListener listener) {
    	synchronized (this) {
    		if (listeners == null) {
    			return false;
    		} else {
    			return listeners.remove(listener);
    		}
    	}
    }
    
    
    protected abstract boolean canCreate();
    
    /**
    * Preload the pool with objects.
    * @param count the number of objects to be added.
    */
    public void preload(int count) {
    	if (count <= 0) {
    		return;
    	}
    	
    	ArrayList tempList = new ArrayList(count);
    	for (int i=0; i<count; i++) {
    		try {
    			tempList.add(factory.create(null));
    		} catch (PoolException poolEx) {
    			
    		}
    	}

		count = tempList.size();
    	synchronized (collection) {
    		for (int i=0; i<count; i++) {
    			checkin(tempList.get(i));
    		}
    		createdCount += count;
    	}
//Bug 4677074    	if(bDebug) System.out.println("After preload(" + count + "): Size: " + collection.size());
//Bug 4677074 begin
	if(com.sun.enterprise.util.logging.Debug.enabled) _logger.log(Level.FINE,"After preload(" + count + "): Size: " + collection.size());
//Bug 4677074 end
    }
    
    public int size() {
    	return collection.size();
    }
    
    /**
    * Destroy the available objects in the pool.
    */
    public int destroyPoolObjects() {
    	return destroyPoolObjects(collection.size());
    }
    
    /**
    * Destroy 'count' available objects in the pool.
    */
    public int destroyPoolObjects(int count) {
    	if (count <= 0) {
    		return 0;
    	}
    	
    	Object[] array = collection.toArray();
    	ArrayList arrayList = null;
    	synchronized (collection) {
    		if (count > collection.size()) {
    			count = collection.size();
    		}
    		arrayList = new ArrayList(count);
    		for (int i=0; i<count; i++) {
   				arrayList.add(checkout(null));
    		}
    		count = arrayList.size();
    		createdCount -= count;
    	}
    	
    	for (int i=0; i<count; i++) {
   			factory.destroy(arrayList.get(i));
    	}
    	return count;
    }
    
    /**
    * Closes the current pool. No further getObject(....)s are allowed, while 
    *	returnObject() and destroyObjects() are allowed.
    */
    public void close() {
    	//first clean up all objects
   		onClose();
    	synchronized (collection) {
    		closed = "__Closed__";
    		int diff = collection.size() - this.waitCount;
    		destroyPoolObjects(diff);
    		//We do not need to change the factory as all fresh getObject()
    		//	requests are blocked well before the synchronized access to the
    		//	collection (or pool).
    		//this.factory = new ClosedObjectFactory(this.factory);
    	}
    }
    
    
    /**
     * Test if the pool is closed or not
     * @return True if the pool is closed, false if not.
     */
    public boolean isClosed() {
    	return (closed != null);
    }
    
    //Event Notification
    
    /**
     * Called after an object is created using factory.create(....)
     * @param The created object.
     */
    public void afterCreate(Object object) {
    	if (listeners != null) {
    		int size = listeners.size();
    		for (int i=0; i<size; i++) {
    			((PoolListener) listeners.get(i)).afterCreate(object);
    		}
    	}
    }
       
    /**
     * Called before an object is destroyed using factory.destroy(object)
     * @param The object to be destroyed.
     */
    public void beforeDestroy(Object object) {
    	if (listeners != null) {
    		int size = listeners.size();
    		for (int i=0; i<size; i++) {
    			((PoolListener) listeners.get(i)).beforeDestroy(object);
    		}
    	}
    }
       
    /**
     * Called by the thread that is about to wait.
     */
    public void beforeWait(Object object) {
    	if (listeners != null) {
    		int size = listeners.size();
    		for (int i=0; i<size; i++) {
    			((PoolListener) listeners.get(i)).beforeWait(object);
    		}
    	}
    }
       
    /**
     * Called by the thread that has been notified.
     */
    public void afterNotify(Object object) {
    	if (listeners != null) {
    		int size = listeners.size();
    		for (int i=0; i<size; i++) {
    			((PoolListener) listeners.get(i)).afterNotify(object);
    		}
    	}
    }
   
    /**
     * Called when the pool is closed.
     */
    public void onClose() {
    	if (listeners != null) {
    		int size = listeners.size();
    		for (int i=0; i<size; i++) {
    			((PoolListener) listeners.get(i)).onClose();
    		}
    	}
    }
   
   
   
}
