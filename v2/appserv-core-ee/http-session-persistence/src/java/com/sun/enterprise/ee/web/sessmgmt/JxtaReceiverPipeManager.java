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
 * JxtaReceiverPipeManager.java
 *
 * Created on February 8, 2006, 12:21 PM
 *
 */

package com.sun.enterprise.ee.web.sessmgmt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import com.sun.enterprise.web.ServerConfigLookup;


/**
 *
 * @author Larry White
 */
public class JxtaReceiverPipeManager {
    
    /**
     * The singleton instance of JxtaReceiverPipeManager
     */    
    private static final JxtaReceiverPipeManager _soleInstance
        = new JxtaReceiverPipeManager();    
    
    /** Creates a new instance of JxtaReceiverPipeManager */
    public JxtaReceiverPipeManager() {
    }
    
    /** 
     * Return the singleton instance
     */
    public static JxtaReceiverPipeManager createInstance() {
        return _soleInstance;
    }
    
    public void setReceiverPipe(JxtaBiDiPipeWrapper receiverPipeWrapper) {
        _receiverPipeWrapper = receiverPipeWrapper;
    }

    /**
     * deprecated version - remove after testing
     */     
    public PipePool getPipePool() {
        return _pipePool;
    } 
    
    /**
     * new version
     * @param sourceInstanceName
     * @returns PipePool pipe pool for that source instance
     */     
    public PipePool getPipePool(String sourceInstanceName) {
        PipePool pipePool = (PipePool) _pipePoolMap.get(sourceInstanceName);
        return pipePool;
    }    

    /**
     * deprecated version - remove after testing
     */     
    public void setPipePool(PipePool pool) {
        _pipePool = pool;
    }
    
    /**
     * new version
     * @param sourceInstanceName
     */     
    public void setPipePool(PipePool pool, String sourceInstanceName) {
        _pipePoolMap.put(sourceInstanceName, pool);
    }
    
    /**
     * new version - remove PipePool for sourceInstanceName
     * @param sourceInstanceName
     */      
    public void removePipePool(String sourceInstanceName) {
        _pipePoolMap.remove(sourceInstanceName);
        _pipeArrayMap.remove(sourceInstanceName);
    }     

    /**
     * deprecated version - remove after testing
     */      
    public PipeWrapper getHealthPipeWrapper() {
        return _healthPipeWrapper;
    }
    
    /**
     * new version - get health PipeWrapper for sourceInstanceName
     * @param sourceInstanceName
     */      
    public PipeWrapper getHealthPipeWrapper(String sourceInstanceName) {
        return (PipeWrapper)_healthPipeMap.get(sourceInstanceName);
    }     
    
    /**
     * deprecated version - remove after testing
     */      
    public void setHealthPipeWrapper(PipeWrapper healthPipeWrapper) {
        _healthPipeWrapper = healthPipeWrapper;
    }
    
    /**
     * new version - set health PipeWrapper for sourceInstanceName
     * @param healthPipeWrapper
     * @param sourceInstanceName
     */      
    public void setHealthPipeWrapper(PipeWrapper healthPipeWrapper, String sourceInstanceName) {
        _healthPipeMap.put(sourceInstanceName, healthPipeWrapper);
    } 
    
    /**
     * new version - remove health PipeWrapper for sourceInstanceName
     * @param sourceInstanceName
     */      
    public void removeHealthPipeWrapper(String sourceInstanceName) {
        _healthPipeMap.remove(sourceInstanceName);
    }    
    
    void setPropagatedInputPipeWrapper(InputPipeWrapper inputPipeWrapper) {
        _inputPipeWrapper = inputPipeWrapper;
    }

    /**
     * deprecated version - remove after testing
     */     
    public void initPipePool(ArrayList pipeWrappers) {
        _pipePool = new PipePool(pipeWrappers);
        _pooledWrappers = pipeWrappers;
    } 
    
    /**
     * new version initialize pool for sourceInstanceName
     * using the list pipeWrappers; also put list of pipeWrappers
     * in appropriate list based on sourceInstanceName
     * @param pipeWrappers
     * @param sourceInstanceName
     */     
    public void initPipePool(ArrayList pipeWrappers, String sourceInstanceName) {
        PipePool pipePool = new PipePool(pipeWrappers);
        _pipePoolMap.put(sourceInstanceName, pipePool);
        _pipeArrayMap.put(sourceInstanceName, pipeWrappers);
    }
    
    /**
     * new version initialize pool for sourceInstanceName
     * using the list pipeWrappers; also put list of pipeWrappers
     * in appropriate list based on sourceInstanceName
     * @param pipeWrappers
     * @param sourceInstanceName
     */     
    public void initPresizedPipePool(ArrayList pipeWrappers, String sourceInstanceName) {
        int poolSize = this.getNumberOfPipes() - 1;
        PipePool pipePool = new PipePool(poolSize);
        _pipePoolMap.put(sourceInstanceName, pipePool);
        _pipeArrayMap.put(sourceInstanceName, pipeWrappers);
    }    
 
    /**
     * deprecated version - remove after testing
     */     
    public void addPipeWrapper(PipeWrapper pipeWrapper) {
        if(_pipePool != null) {
            try {
                _pipePool.put((PipePoolElement)pipeWrapper);
            } catch (InterruptedException ex) {
                //FIXME log message
            }
        }
    }
    
    /**
     * new version - add pipeWrapper to pool for sourceInstanceName;
     * if health pipe for sourceInstance name does not exist, add that first
     * @param sourceInstanceName
     */     
    public synchronized void addPipeWrapper(PipeWrapper pipeWrapper, String sourceInstanceName) {
System.out.println("in new addPipeWrapper for " + sourceInstanceName);
        int count = 0;
        boolean needToRetry = true;
        boolean needToClosePreviousPipes = false;
        while(needToRetry) {
            count++;
            needToRetry = false;
            if(needToClosePreviousPipes) {
                this.closePooledPipes(sourceInstanceName);
                needToClosePreviousPipes = false;
            }
            //first check for existence of health pipe and if needed add it
            if(this.getHealthPipeWrapper(sourceInstanceName) == null) {
                System.out.println("setting health pipe for " + sourceInstanceName);
                //add the health pipe for sourceInstanceName
                this.setHealthPipeWrapper(pipeWrapper, sourceInstanceName);
                return;
            }
            PipePool thePool = (PipePool)_pipePoolMap.get(sourceInstanceName);
            if(thePool == null) {
                ArrayList wrappers = new ArrayList();
                wrappers.add(pipeWrapper);
                initPresizedPipePool(wrappers, sourceInstanceName);
                thePool = (PipePool)_pipePoolMap.get(sourceInstanceName);           
            }
            try {
                boolean success = thePool.offer((PipePoolElement)pipeWrapper, 200L, TimeUnit.MILLISECONDS);
                System.out.println("in new addPipeWrapper offer success:" + success + " for:" + sourceInstanceName);
                if(!success) {
                    if(count < 2) {
                        needToRetry = true;
                        needToClosePreviousPipes = true;
                    }
                } else {
                    ArrayList wrapperList = (ArrayList)_pipeArrayMap.get(sourceInstanceName);
                    wrapperList.add(pipeWrapper);
                }
            } catch (InterruptedException ex) {
                //FIXME log message                    
            }
        }
    }    
    
    /**
     * new version - add pipeWrapper to pool for sourceInstanceName;
     * if health pipe for sourceInstance name does not exist, add that first
     * @param sourceInstanceName
     */     
    public synchronized void addPipeWrapperLastGood3(PipeWrapper pipeWrapper, String sourceInstanceName) {
System.out.println("in new addPipeWrapper for " + sourceInstanceName);
        int count = 0;
        boolean needToRetry = true;
        boolean needToClosePreviousPipes = false;
        while(needToRetry) {
            count++;
            needToRetry = false;
            if(needToClosePreviousPipes) {
                this.closePooledPipes(sourceInstanceName);
                needToClosePreviousPipes = false;
            }
            //first check for existence of health pipe and if needed add it
            if(this.getHealthPipeWrapper(sourceInstanceName) == null) {
                System.out.println("setting health pipe for " + sourceInstanceName);
                //add the health pipe for sourceInstanceName
                this.setHealthPipeWrapper(pipeWrapper, sourceInstanceName);
                return;
            }
            PipePool thePool = (PipePool)_pipePoolMap.get(sourceInstanceName);
            if(thePool == null) {
                ArrayList wrappers = new ArrayList();
                wrappers.add(pipeWrapper);
                initPresizedPipePool(wrappers, sourceInstanceName);
                thePool = (PipePool)_pipePoolMap.get(sourceInstanceName);           
            }
            try {
                boolean success = thePool.offer((PipePoolElement)pipeWrapper, 200L, TimeUnit.MILLISECONDS);
                System.out.println("in new addPipeWrapper offer success:" + success + " for:" + sourceInstanceName);
                if(!success && count < 2) {
                    needToRetry = true;
                    needToClosePreviousPipes = true;
                } else {
                    ArrayList wrapperList = (ArrayList)_pipeArrayMap.get(sourceInstanceName);
                    wrapperList.add(pipeWrapper);
                }
            } catch (InterruptedException ex) {
                //FIXME log message                    
            }
        }
    }    
    
    /**
     * new version - add pipeWrapper to pool for sourceInstanceName;
     * if health pipe for sourceInstance name does not exist, add that first
     * @param sourceInstanceName
     */     
    public synchronized void addPipeWrapperLastGood2(PipeWrapper pipeWrapper, String sourceInstanceName) {
System.out.println("in new addPipeWrapper for " + sourceInstanceName);
        int count = 0;
        boolean needToRetry = true;
        boolean needToClosePreviousPipes = false;
        while(needToRetry) {
            count++;
            needToRetry = false;
            if(needToClosePreviousPipes) {
                this.closePooledPipes(sourceInstanceName);
                needToClosePreviousPipes = false;
            }
            //first check for existence of health pipe and if needed add it
            if(this.getHealthPipeWrapper(sourceInstanceName) == null) {
                System.out.println("setting health pipe for " + sourceInstanceName);
                //add the health pipe for sourceInstanceName
                this.setHealthPipeWrapper(pipeWrapper, sourceInstanceName);
                return;
            }
            PipePool thePool = (PipePool)_pipePoolMap.get(sourceInstanceName);
            if(thePool == null) {
                ArrayList wrappers = new ArrayList();
                wrappers.add(pipeWrapper);
                initPresizedPipePool(wrappers, sourceInstanceName);
                thePool = (PipePool)_pipePoolMap.get(sourceInstanceName);           
            } else {
                try {
                    boolean success = thePool.offer((PipePoolElement)pipeWrapper, 200L, TimeUnit.MILLISECONDS);
                    System.out.println("in new addPipeWrapper offer success:" + success + " for:" + sourceInstanceName);
                    if(!success && count < 2) {
                        needToRetry = true;
                        needToClosePreviousPipes = true;
                    } else {
                        ArrayList wrapperList = (ArrayList)_pipeArrayMap.get(sourceInstanceName);
                        wrapperList.add(pipeWrapper);
                    }
                } catch (InterruptedException ex) {
                    //FIXME log message                    
                }
            }
        }
    }
    
    /**
     * new version - add pipeWrapper to pool for sourceInstanceName;
     * if health pipe for sourceInstance name does not exist, add that first
     * @param sourceInstanceName
     */     
    public synchronized void addPipeWrapperLastGood(PipeWrapper pipeWrapper, String sourceInstanceName) {
        //first check for existence of health pipe and if needed add it
        if(this.getHealthPipeWrapper(sourceInstanceName) == null) {
            //System.out.println("setting health pipe for " + sourceInstanceName);
            //add the health pipe for sourceInstanceName
            this.setHealthPipeWrapper(pipeWrapper, sourceInstanceName);
            return;
        }
        PipePool thePool = (PipePool)_pipePoolMap.get(sourceInstanceName);
        if(thePool == null) {
            ArrayList wrappers = new ArrayList();
            wrappers.add(pipeWrapper);
            initPresizedPipePool(wrappers, sourceInstanceName);
            thePool = (PipePool)_pipePoolMap.get(sourceInstanceName);           
        } else {
            try {
                thePool.put((PipePoolElement)pipeWrapper);
                ArrayList wrapperList = (ArrayList)_pipeArrayMap.get(sourceInstanceName);
                wrapperList.add(pipeWrapper);
            } catch (InterruptedException ex) {
                //FIXME log message
            }
        }
    }    
    
    int getNumberOfPipes() {
        ServerConfigLookup lookup = new ServerConfigLookup();
        int result = lookup.getNumberOfReplicationPipesFromConfig();
        /*
        if(result < 2) {
            result = 2;
        }
         */
        if(result < 1) {
            result = 1;
        }
        return result;
    }    
    
    /**
     * deprecated version - remove after testing
     */     
    public void closePooledPipesPrevious() {
        for(int i=0; i<_pooledWrappers.size(); i++) {
            PipeWrapper nextWrapper = (PipeWrapper)_pooledWrappers.get(i);
            nextWrapper.cleanup();
        }
    }
    
    /**
     * new version - close all pooled pipes across
     * all sourceInstances
     */     
    public void closePooledPipes() {
        Set sourceInstanceNames = _pipePoolMap.keySet();
        Iterator sourcesIterator = sourceInstanceNames.iterator();
        while(sourcesIterator.hasNext()) {
            String nextSource = (String)sourcesIterator.next();
            closePooledPipes(nextSource);
        }
    }    
    
    /**
     * new version - close and remove all pipes in pool for sourceInstanceName
     * also close and remove health pipe
     * @param sourceInstanceName
     */     
    public synchronized void closePooledPipes(String sourceInstanceName) {
//System.out.println("JxtaReceiverPipeManager>>closePooledPipes:instance=" + sourceInstanceName);
//System.out.println("beginning closePooledPipes:health=" + this.getHealthPipeWrapper(sourceInstanceName));
//System.out.println("beginning closePooledPipes:pipePool=" + this.getPipePool(sourceInstanceName));
        ArrayList thePooledWrappers = (ArrayList)_pipeArrayMap.get(sourceInstanceName);
//System.out.println("thePooledWrappers=" + thePooledWrappers);
        if(thePooledWrappers != null) {
            for(int i=0; i<thePooledWrappers.size(); i++) {
                PipeWrapper nextWrapper = (PipeWrapper)thePooledWrappers.get(i);
                nextWrapper.cleanup();
            }
        }
//System.out.println("after iterating before removePipePool:thePooledWrappers=" + thePooledWrappers);
        //clear the maps of this sourceInstanceName
        removePipePool(sourceInstanceName);
//System.out.println("after removePipePool");       
        closeHealthPipeWrapper(sourceInstanceName);
//System.out.println("after closePooledPipes:health=" + this.getHealthPipeWrapper(sourceInstanceName));
//System.out.println("after closePooledPipes:pipePool=" + this.getPipePool(sourceInstanceName));
    }    

    /**
     * deprecated version - remove after testing
     */      
    void closeHealthPipeWrapper() {
        PipeWrapper healthPipeWrapper = this.getHealthPipeWrapper();
        if(healthPipeWrapper != null) {
            healthPipeWrapper.cleanup();
        }
        _healthPipeWrapper = null;
    }
    
    /**
     * new version - close & remove health pipe wrapper for sourceInstanceName
     * @param sourceInstanceName
     */      
    void closeHealthPipeWrapper(String sourceInstanceName) {
        PipeWrapper healthPipeWrapper 
            = this.getHealthPipeWrapper(sourceInstanceName);
        if(healthPipeWrapper != null) {
            healthPipeWrapper.cleanup();
        }
        removeHealthPipeWrapper(sourceInstanceName);
    }    
    
    public void closePropagatedInputPipeWrapper() {
        if(_inputPipeWrapper != null) {
            _inputPipeWrapper.cleanup();
        }
        _inputPipeWrapper = null;        
    }    
    
    private JxtaBiDiPipeWrapper _receiverPipeWrapper = null;
    
    /**
     * map of pipe pools - key is instanceName of connection
     * source -- value is the PipePool for those connections
     * from that source
     */    
    private ConcurrentHashMap _pipePoolMap = new ConcurrentHashMap();
    
    /**
     * map of pipe arrays - key is instanceName of connection
     * source -- value is the array of PipeWrappers for those connections
     * from that source - used to cleanup
     */    
    private ConcurrentHashMap _pipeArrayMap = new ConcurrentHashMap(); 
    
    /**
     * map of health PipeWrappers - key is instanceName of connection
     * source -- value is the health PipeWrapper for those connections
     */    
    private ConcurrentHashMap _healthPipeMap = new ConcurrentHashMap();    
    
    private PipePool _pipePool = null;
    private ArrayList _pooledWrappers = new ArrayList();    
    private PipeWrapper _healthPipeWrapper = null;
    private InputPipeWrapper _inputPipeWrapper = null;
}
