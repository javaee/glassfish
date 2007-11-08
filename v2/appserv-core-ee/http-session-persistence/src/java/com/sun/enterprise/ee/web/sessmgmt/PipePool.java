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
 * Copyright 2006 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
/*
 * PipePool.java
 *
 * Created on February 24, 2006, 2:53 PM
 */

package com.sun.enterprise.ee.web.sessmgmt;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;
//import com.sun.enterprise.ee.util.concurrent.BoundedLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Larry White
 */
public class PipePool {
    
    public static final int DEFAULT_INITIAL_SIZE = 100; // Bug : 4834004
    public static final int DEFAULT_UPPER_SIZE = -1;   //unlimited upper size
    //FIXME put back to 100 after testing
    public static final int DEFAULT_POLL_TIME = 500;
    
    /** Creates a new instance of PipePool */
    public PipePool(ArrayList pipeWrappers) {       
        _upperSize = DEFAULT_UPPER_SIZE;
        _pollTime = DEFAULT_POLL_TIME;
        if (_logger == null) {
            _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
        }          
        this.initializePool(pipeWrappers); 
    }
    
    /** Creates a new instance of PipePool */
    public PipePool(int poolSize) {       
        _upperSize = DEFAULT_UPPER_SIZE;
        _pollTime = DEFAULT_POLL_TIME;
        if (_logger == null) {
            _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
        }          
        this.initializePool(poolSize); 
    }    
    
    /** Creates a new instance of PipePool */
    public PipePool(int upperSize, int pollTime, ArrayList pipeWrappers) {
        int thePoolSize = pipeWrappers.size();
	_pollTime = pollTime;
	if (_logger == null) 
            _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
	_upperSize = upperSize;
	initializePool(pipeWrappers);
        if (_logger == null) {
            _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
        }          
    }    
    
    /**
     * initialize the pool
     * @param pipeWrappers a list of pipeWrappers
     */       
    private void initializePool(ArrayList pipeWrappers) {
        int poolSize = pipeWrappers.size();
        System.out.println("initializePool - pool size: " + poolSize);
        if(poolSize == 0) {
            //return;
            //_pool = new BoundedLinkedQueue(10);
            _pool = new LinkedBlockingQueue(10);
        } else {
            //_pool = new BoundedLinkedQueue(poolSize);
            _pool = new LinkedBlockingQueue(poolSize);
        }
        for(int i=0; i<poolSize; i++) {
            PipePoolElement nextPipeWrapper = (PipePoolElement)pipeWrappers.get(i);
            try
            {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("initializePool-pipeWrappers  "+nextPipeWrapper.getClass());
                }
                _pool.put(nextPipeWrapper);
            //InterruptedException should not occur during initialization
            } catch (InterruptedException ex) {}
        }
    } 
    
    /**
     * initialize the pool
     * @param poolSize size to initialize the pool
     */       
    private void initializePool(int poolSize) {
        System.out.println("initializePool - pool size: " + poolSize);
        if(poolSize == 0) {
            //return;
            //_pool = new BoundedLinkedQueue(10);
            _pool = new LinkedBlockingQueue(10);
        } else {
            //_pool = new BoundedLinkedQueue(poolSize);
            _pool = new LinkedBlockingQueue(poolSize);
        }
    }         
    
    /**
     * take and return a PipePoolElement from the pool
     */    
    public PipePoolElement take() throws InterruptedException {
        PipePoolElement pipeWrapper = null;
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("IN TAKE -- POLL_TIME=" + _pollTime);
        }
        //pipeWrapper = (PipePoolElement) _pool.poll(_pollTime);
        pipeWrapper = (PipePoolElement) _pool.poll(_pollTime, TimeUnit.MILLISECONDS);
        if(pipeWrapper != null) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("IN TAKE -- GOT FROM MAIN POOL"+pipeWrapper.getClass());
            }
            return pipeWrapper;
        }
        /*
        synchronized(this) {
            //_upperSize negative means unlimited upper size
            if((_currentUpperPermits < _upperSize) | _upperSize < 0) {            
            //if(! (_currentUpperPermits > _upperSize) | _upperSize < 0) {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("IN TAKE -- GOT FROM UPPER POOL");
                }
                pipeWrapper = (PipePoolElement) _pipeFactory.createPipe();
                System.out.println("new pipeWrapper from factory " + pipeWrapper);
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("pipeWrapper CLASS "+pipeWrapper.getClass()+ "   "+_pipeFactory.getClass());
                }
                _currentUpperPermits++;
            } else {
                //FIXME throw exception
            }
        }
         */
        return pipeWrapper;
    }
    
    /**
     * take and return a PipePoolElement from the pool
     */    
    public PipePoolElement takePrevious() throws InterruptedException {
        PipePoolElement pipeWrapper = null;
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("IN TAKE -- POLL_TIME=" + _pollTime);
        }
        //pipeWrapper = (PipePoolElement) _pool.poll(_pollTime);
        pipeWrapper = (PipePoolElement) _pool.poll(_pollTime, TimeUnit.MILLISECONDS);
        if(pipeWrapper != null) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("IN TAKE -- GOT FROM MAIN POOL"+pipeWrapper.getClass());
            }
            return pipeWrapper;
        }
        synchronized(this) {
            //_upperSize negative means unlimited upper size
            if((_currentUpperPermits < _upperSize) | _upperSize < 0) {            
            //if(! (_currentUpperPermits > _upperSize) | _upperSize < 0) {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("IN TAKE -- GOT FROM UPPER POOL");
                }
                pipeWrapper = (PipePoolElement) _pipeFactory.createPipe();
                System.out.println("new pipeWrapper from factory " + pipeWrapper);
                if(pipeWrapper != null) {
                    if(_logger.isLoggable(Level.FINEST)) {
                        _logger.finest("pipeWrapper CLASS "+pipeWrapper.getClass()+ "   "+_pipeFactory.getClass());
                    }
                }
                _currentUpperPermits++;
            } else {
                //FIXME throw exception
            }
        }
        return pipeWrapper;
    }    
    
    /**
     * put a PipePoolElement back into the pool
     * @param pipeWrapper
     */ 
    public void put(PipePoolElement pipeWrapper) throws InterruptedException {    
        //check if there are upper level permits; if so,
        //decrement counter and return -- i.e. toss the pipeWrapper instance
        synchronized(this) {
            if(_currentUpperPermits > 0) {
                pipeWrapper.cleanup();
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
        _pool.put(pipeWrapper);
    } 
    
    /**
     * put a PipePoolElement back into the pool
     * @param pipeWrapper
     */ 
    public boolean offer(PipePoolElement pipeWrapper, long timeToWait, TimeUnit timeunit) throws InterruptedException {    
        //check if there are upper level permits; if so,
        //decrement counter and return -- i.e. toss the pipeWrapper instance
        synchronized(this) {
            if(_currentUpperPermits > 0) {
                pipeWrapper.cleanup();
                _currentUpperPermits--;
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("IN OFFER -- PUTTING BACK INTO UPPER POOL");
                }
                return false;
            }
        }
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("IN OFFER -- PUTTING BACK INTO LOWER POOL");
        }
        return _pool.offer(pipeWrapper, timeToWait, timeunit);
    }    
    
    /**
     * The logger to use for logging ALL web container related messages.
     */
    private static Logger _logger = null;     
    
    //private BoundedLinkedQueue _pool = null;
    private LinkedBlockingQueue _pool = null;
    private int _upperSize = DEFAULT_UPPER_SIZE;
    private int _currentUpperPermits = 0;
    private int _pollTime = DEFAULT_POLL_TIME;
    //default to JxtaBiDiPipe (PipeWrapper) as pool element
    private PipeFactory _pipeFactory = new JxtaBiDiPipeFactory();
    
}
