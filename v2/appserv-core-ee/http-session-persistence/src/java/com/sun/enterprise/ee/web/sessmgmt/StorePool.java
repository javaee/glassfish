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

/*
 * StorePool.java
 *
 * Created on February 19, 2003, 12:47 PM
 */

package com.sun.enterprise.ee.web.sessmgmt;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;
//import com.sun.enterprise.ee.util.concurrent.BoundedLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author  lwhite
 */
public class StorePool {
    
    /**
     * The logger to use for logging ALL web container related messages.
     */
    private static final Logger _logger 
        = LogDomains.getLogger(LogDomains.WEB_LOGGER);   
    
    public static final int DEFAULT_INITIAL_SIZE = 100; // Bug : 4834004
    public static final int DEFAULT_UPPER_SIZE = -1;   //unlimited upper size
    public static final int DEFAULT_POLL_TIME = 100;
    
    /** Creates a new instance of StorePool */
    public StorePool() {       
        int poolSize = DEFAULT_INITIAL_SIZE;
        /*
        if (_logger == null) {
            _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
        }
         */          
        this.initializePool(poolSize); 
    }
    
    /** Creates a new instance of StorePool */
    public StorePool(int poolSize) {
        int thePoolSize = DEFAULT_INITIAL_SIZE;
        if(poolSize > 1) {
            thePoolSize = poolSize;
        }
        /*
        if (_logger == null) {
            _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
        }
         */          
        this.initializePool(thePoolSize);
    } 
    
    /** Creates a new instance of StorePool */
    public StorePool(int poolSize, int upperSize) {
        this(poolSize);
        _upperSize = upperSize;        
    }   
    
    /** Creates a new instance of StorePool */
    public StorePool(int poolSize, int upperSize, int pollTime) {
        this(poolSize, upperSize);
        _pollTime = pollTime;        
    } 
    
    /** Creates a new instance of StorePool */
    public StorePool(int poolSize, int upperSize, int pollTime, StoreFactory storeFactory) {
        int thePoolSize = DEFAULT_INITIAL_SIZE;
        _storeFactory = storeFactory;
	_pollTime = pollTime;
        /*
	if (_logger == null) 
            _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
         */

	if(poolSize > 1) 
            thePoolSize = poolSize;
	_upperSize = upperSize;
	initializePool(thePoolSize);
        /*
        if (_logger == null) {
            _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
        }
         */          
    }
    
    /**
     * initialize the pool
     * @param poolSize
     */       
    private void initializePool(int poolSize) {
        if(_storeFactory == null) {
            return;
        }
        //_pool = new BoundedLinkedQueue(poolSize);
        _pool = new LinkedBlockingQueue(poolSize);
        for(int i=0; i<poolSize; i++) {
            StorePoolElement nextStore = _storeFactory.createHAStore();
            try
            {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("initializePool-int  "+nextStore.getClass());
                }
                _pool.put(nextStore);
            //InterruptedException should not occur during initialization
            } catch (InterruptedException ex) {}
        }
    }    
    
    /**
     * take and return an StorePoolElement from the pool
     */    
    public StorePoolElement take() throws InterruptedException {
        StorePoolElement haStore = null;
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("IN TAKE -- POLL_TIME=" + _pollTime);
        }
        haStore = (StorePoolElement) _pool.poll(_pollTime, TimeUnit.MILLISECONDS);
        //haStore = (StorePoolElement) _pool.poll(_pollTime);
        if(haStore != null) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("IN TAKE -- GOT FROM MAIN POOL"+haStore.getClass());
            }
            return haStore;
        }
        synchronized(this) {
            //_upperSize negative means unlimited upper size
            if((_currentUpperPermits < _upperSize) | _upperSize < 0) {            
            //if(! (_currentUpperPermits > _upperSize) | _upperSize < 0) {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("IN TAKE -- GOT FROM UPPER POOL");
                }
                haStore = (StorePoolElement) _storeFactory.createHAStore();
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("haStore CLASS "+haStore.getClass()+ "   "+_storeFactory.getClass());
                }
                _currentUpperPermits++;
            } else {
                //FIXME throw exception
            }
        }
        return haStore;
    }
    
    /**
     * put an StorePoolElement back into the pool
     * @param haStore
     */     
    public void put(StorePoolElement haStore) throws InterruptedException {
        //check if there are upper level permits; if so,
        //decrement counter and return -- i.e. toss the HAStore instance
        synchronized(this) {
            if(_currentUpperPermits > 0) {
                haStore.cleanup();  //worried about the time for this
                _currentUpperPermits--;
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("IN PUT -- PUTTING BACK INTO UPPER POOL");
                }
                return;
            }
        }
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("IN PUT -- PUTTING BACK INTO LOWER POOL");
        }
        _pool.put(haStore);
    }      
    
    //private BoundedLinkedQueue _pool = null;
    private LinkedBlockingQueue _pool = null;
    private int _upperSize = DEFAULT_UPPER_SIZE;
    private int _currentUpperPermits = 0;
    private int _pollTime = DEFAULT_POLL_TIME;
    //default to full session i.e. ReplicationStore as pool element
    //FIXME restore next line after replication check-ins
    //private StoreFactory _storeFactory = new ReplicationFullSessionStoreFactory();
    private StoreFactory _storeFactory = null;
    
}
