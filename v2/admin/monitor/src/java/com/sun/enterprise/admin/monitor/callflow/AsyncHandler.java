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
 * AsyncHandlerImpl.java
 * $Id: AsyncHandler.java,v 1.12 2006/10/09 19:20:20 harpreet Exp $
 * $Date: 2006/10/09 19:20:20 $
 * $Revision: 1.12 $
 */

package com.sun.enterprise.admin.monitor.callflow;

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.sun.enterprise.admin.common.constant.AdminConstants;

/**
 * This class asynchronously writes supplied input into a data base. That is,
 * a separate asynchronous thread is used to write data to the data base.
 *
 * The implementation uses a typical producer - consumer model: The data
 * produced by application threads is collected in unbounded queue objects;
 * an asynchronous thread consumes the data from the queues and writes it out
 * to the data base.
 *
 * @author Ram Jeyaraman, Harpreet Singh
 * @date March 21, 2005
 */
class AsyncHandler implements AsyncHandlerIntf {
    
    /** Static fields */
    
    private static final Logger logger =
            Logger.getLogger(AdminConstants.kLoggerName);
    private static final int WAIT_INTERVALS = 100;
    private static final int MAX_BULK_SIZE = 10000;
    private static final int BUFFER_COUNT = 6;
    private static final String THREAD_NAME = "Callflow AsyncThread";
        
    /** Private fields */
    
    private LinkedBlockingQueue<RequestStartTO> requestStartQ;
    private LinkedBlockingQueue<RequestEndTO> requestEndQ;
    private LinkedBlockingQueue<MethodStartTO> methodStartQ;
    private LinkedBlockingQueue<MethodEndTO> methodEndQ;
    private LinkedBlockingQueue<StartTimeTO> startTimeQ;
    private LinkedBlockingQueue<EndTimeTO> endTimeQ;
    
    private ConcurrentLinkedQueue<RequestStartTO> requestStartFreeQ;
    private ConcurrentLinkedQueue<RequestEndTO> requestEndFreeQ;
    private ConcurrentLinkedQueue<MethodStartTO> methodStartFreeQ;
    private ConcurrentLinkedQueue<MethodEndTO> methodEndFreeQ;
    private ConcurrentLinkedQueue<StartTimeTO> startTimeFreeQ;
    private ConcurrentLinkedQueue<EndTimeTO> endTimeFreeQ;
    
    private AsyncThread asyncThread = null;
    private boolean enabled = false;
    
    private class AsyncThread extends Thread {
        
        private boolean shutdown;
        private int emptyBufferCount;
        private DbAccessObject dbAccessObject;
        
        AsyncThread() {
            setDaemon(true);
            setName(THREAD_NAME);
            dbAccessObject = DbAccessObjectImpl.getInstance();
        }
        
        void shutdown() {
            shutdown = true;
            while (emptyBufferCount < BUFFER_COUNT) {
                try {
                    Thread.sleep(WAIT_INTERVALS);
                } catch (InterruptedException e) {}
            }
        }
        
        public void run() {
            
            List<RequestStartTO> rsTransferObjects =
                    new ArrayList<RequestStartTO>();
            List<RequestEndTO> reTransferObjects =
                    new ArrayList<RequestEndTO>();
            List<MethodStartTO> msTransferObjects =
                    new ArrayList<MethodStartTO>();
            List<MethodEndTO> meTransferObjects =
                    new ArrayList<MethodEndTO>();
            List<StartTimeTO> stTransferObjects =
                    new ArrayList<StartTimeTO>();
            List<EndTimeTO> etTransferObjects =
                    new ArrayList<EndTimeTO>();
            
            while (emptyBufferCount < BUFFER_COUNT) {
                
                // Handle RequestStart
                
                for (int i=0; i<MAX_BULK_SIZE; i++) {
                    try {
                        RequestStartTO rsto =
                                requestStartQ.poll(
                                    WAIT_INTERVALS, TimeUnit.MILLISECONDS);
                        if (rsto == null) {
                            break;
                        }
                        rsTransferObjects.add(rsto);
                    } catch (InterruptedException e) {
                        logger.log(
                                Level.FINE,
                                "callflow.async_thread_interrupted",
                                e);
                        break;
                    }
                }
                try {
                    if (rsTransferObjects.isEmpty()) {
                        if (shutdown) {
                            emptyBufferCount++;
                        }
                    } else {
                        dbAccessObject.insert(
                            rsTransferObjects.toArray(new TransferObject[0]));
                    }
                } catch (Exception e) {
                    logger.log(
                            Level.WARNING,
                            "callflow.async_db_write_failed",
                            e); 
                }
                requestStartFreeQ.addAll(rsTransferObjects);
                rsTransferObjects.clear();
                
                // Handle RequestEnd
                
                for (int i=0; i<MAX_BULK_SIZE; i++) {
                    try {
                        RequestEndTO reto =
                                requestEndQ.poll(
                                    WAIT_INTERVALS, TimeUnit.MILLISECONDS);
                        if (reto == null) {
                            break;
                        }
                        reTransferObjects.add(reto);
                    } catch (InterruptedException e) {
                        logger.log(
                                Level.FINE,
                                "callflow.async_thread_interrupted",
                                e);
                        break;
                    }
                }
                try {
                    if (reTransferObjects.isEmpty()) {
                        if (shutdown) {
                            emptyBufferCount++;
                        }
                    } else {
                        dbAccessObject.insert(
                            reTransferObjects.toArray(new TransferObject[0]));
                    }
                } catch (Exception e) {
                    logger.log(
                            Level.WARNING,
                            "callflow.async_db_write_failed",
                            e); 
                }
                requestEndFreeQ.addAll(reTransferObjects);
                reTransferObjects.clear();
                
                // Handle MethodStart
                
                for (int i=0; i<MAX_BULK_SIZE; i++) {
                    try {
                        MethodStartTO msto =
                                methodStartQ.poll(
                                    WAIT_INTERVALS, TimeUnit.MILLISECONDS);
                        if (msto == null) {
                            break;
                        }                      
                        msTransferObjects.add(msto);
                    } catch (InterruptedException e) {
                        logger.log(
                                Level.FINE,
                                "callflow.async_thread_interrupted",
                                e);
                        break;
                    }
                }
                try {
                    if (msTransferObjects.isEmpty()) {
                        if (shutdown) {
                            emptyBufferCount++;
                        }
                    } else {
                        dbAccessObject.insert(
                            msTransferObjects.toArray(new TransferObject[0]));
                    }
                } catch (Exception e) {
                    logger.log(
                            Level.WARNING,
                            "callflow.async_db_write_failed",
                            e); 
                }
                methodStartFreeQ.addAll(msTransferObjects);
                msTransferObjects.clear();
                
                // Handle MethodEnd
                
                for (int i=0; i<MAX_BULK_SIZE; i++) {
                    try {
                        MethodEndTO meto =
                                methodEndQ.poll(
                                    WAIT_INTERVALS, TimeUnit.MILLISECONDS);
                        if (meto == null) {
                            break;
                        }                        
                        meTransferObjects.add(meto);
                    } catch (InterruptedException e) {
                        logger.log(
                                Level.FINE,
                                "callflow.async_thread_interrupted",
                                e);
                        break;
                    }
                }
                try {
                    if (meTransferObjects.isEmpty()) {
                        if (shutdown) {
                            emptyBufferCount++;
                        }
                    } else {
                        dbAccessObject.insert(
                            meTransferObjects.toArray(new TransferObject[0]));
                    }
                } catch (Exception e) {
                    logger.log(
                            Level.WARNING,
                            "callflow.async_db_write_failed",
                            e); 
                }
                methodEndFreeQ.addAll(meTransferObjects);
                meTransferObjects.clear();
                
                // Handle StartTime
                
                for (int i=0; i<MAX_BULK_SIZE; i++) {
                    try {
                        StartTimeTO stto =
                                startTimeQ.poll(
                                    WAIT_INTERVALS, TimeUnit.MILLISECONDS);
                        if (stto == null) {
                            break;
                        }
                        stTransferObjects.add(stto);
                    } catch (InterruptedException e) {
                        logger.log(
                                Level.FINE,
                                "callflow.async_thread_interrupted",
                                e);
                        break;
                    }
                }
                try {
                    if (stTransferObjects.isEmpty()) {
                        if (shutdown) {
                            emptyBufferCount++;
                        }
                    } else {
                        dbAccessObject.insert(
                            stTransferObjects.toArray(new TransferObject[0]));
                    }
                } catch (Exception e) {
                    logger.log(
                            Level.WARNING,
                            "callflow.async_db_write_failed",
                            e); 
                }
                startTimeFreeQ.addAll(stTransferObjects);
                stTransferObjects.clear();
                
                // Handle EndTime
                
                for (int i=0; i<MAX_BULK_SIZE; i++) {
                    try {
                        EndTimeTO etto =
                                endTimeQ.poll(
                                    WAIT_INTERVALS, TimeUnit.MILLISECONDS);
                        if (etto == null) {
                            break;
                        }
                        etTransferObjects.add(etto);
                    } catch (InterruptedException e) {
                        logger.log(
                                Level.FINE,
                                "callflow.async_thread_interrupted",
                                e);
                        break;
                    }
                }
                try {
                    if (etTransferObjects.isEmpty()) {
                        if (shutdown) {
                            emptyBufferCount++;
                        }
                    } else {
                        dbAccessObject.insert(
                            etTransferObjects.toArray(new TransferObject[0]));
                    }
                } catch (Exception e) {
                    logger.log(
                            Level.WARNING,
                            "callflow.async_db_write_failed",
                            e); 
                }
                endTimeFreeQ.addAll(etTransferObjects);
                etTransferObjects.clear();                
            }
        }
    }
    
    AsyncHandler() {
        
        requestStartQ = new LinkedBlockingQueue<RequestStartTO>();
        requestEndQ = new LinkedBlockingQueue<RequestEndTO>();
        methodStartQ = new LinkedBlockingQueue<MethodStartTO>();
        methodEndQ = new LinkedBlockingQueue<MethodEndTO>();
        startTimeQ = new LinkedBlockingQueue<StartTimeTO>();
        endTimeQ = new LinkedBlockingQueue<EndTimeTO>();
        
        requestStartFreeQ = new ConcurrentLinkedQueue<RequestStartTO>();
        requestEndFreeQ = new ConcurrentLinkedQueue<RequestEndTO>();
        methodStartFreeQ = new ConcurrentLinkedQueue<MethodStartTO>();
        methodEndFreeQ = new ConcurrentLinkedQueue<MethodEndTO>();
        startTimeFreeQ = new ConcurrentLinkedQueue<StartTimeTO>();
        endTimeFreeQ = new ConcurrentLinkedQueue<EndTimeTO>();
        
        asyncThread = new AsyncThread ();
    }
    
  
    
    public synchronized void enable() {
        if (!enabled){
            enabled = true;
            if (asyncThread == null)
                asyncThread = new AsyncThread ();
            asyncThread.start();
        }
    }
    
    public synchronized void disable() {
        asyncThread.shutdown();
        enabled = false;
        asyncThread = null;
    }
    
    public void handleRequestStart(
            String requestId, long timeStamp, long timeStampMillis, 
            RequestType requestType, String callerIPAddress,
            String remoteUser) {
        RequestStartTO rsto = requestStartFreeQ.poll();
        if (rsto == null) {
            rsto = new RequestStartTO();
        }
        rsto.setRequestId(requestId);
        rsto.setTimeStamp(timeStamp);
        rsto.setTimeStampMillis(timeStampMillis);
        rsto.setRequestType(requestType);
        rsto.setIpAddress(callerIPAddress);
        //rsto.setRemoteUser(remoteUser); // not currently in db schema.
        boolean success = false;
        while (!success) {
            try {
                requestStartQ.put(rsto);
                success = true;
            } catch (InterruptedException e) {
                logger.log(
                        Level.FINE,
                        "callflow.transfer_to_async_thread_interrupted", e);
            }
        }
    }
    
    public void handleRequestEnd(String requestId, long timeStamp) {
        RequestEndTO reto = requestEndFreeQ.poll();
        if (reto == null) {
            reto = new RequestEndTO();
        }
        reto.setRequestId(requestId);
        reto.setTimeStamp(timeStamp);

        boolean success = false;
        while (!success) {
            try {
                requestEndQ.put(reto);
                success = true;
            } catch (InterruptedException e) {
                logger.log(
                        Level.FINE,
                        "callflow.transfer_to_async_thread_interrupted", e);
            }
        }
    }
    
    public void handleMethodStart(
            String requestId, long timeStamp, String methodName,
            ComponentType componentType, String applicationName,
            String moduleName, String componentName, String threadId,
            String transactionId, String securityId) {
        MethodStartTO msto = methodStartFreeQ.poll();
        if (msto == null) {
            msto = new MethodStartTO();
        }
        msto.setRequestId(requestId);
        msto.setTimeStamp(timeStamp);
        msto.setMethodName(methodName);
        msto.setComponentType(componentType);
        msto.setAppName(applicationName);
        msto.setModuleName(moduleName);
        msto.setComponentName(componentName);
        msto.setThreadId(threadId);
        msto.setTransactionId(transactionId);
        msto.setSecurityId(securityId);
        
        boolean success = false;
        while (!success) {
            try {
                methodStartQ.put(msto);
                success = true;
            } catch (InterruptedException e) {
                logger.log(
                        Level.FINE,
                        "callflow.transfer_to_async_thread_interrupted", e);
            }
        }
    }
    
    public void handleMethodEnd(
            String requestId, long timeStamp, Throwable exception) {
        MethodEndTO meto = methodEndFreeQ.poll();
        if (meto == null) {
            meto = new MethodEndTO();
        }
        meto.setRequestId(requestId);
        meto.setTimeStamp(timeStamp);
        meto.setException(((exception == null) ? null : exception.toString()));
        
        boolean success = false;
        while (!success) {
            try {
                methodEndQ.put(meto);
                success = true;
            } catch (InterruptedException e) {
                logger.log(
                        Level.FINE,
                        "callflow.transfer_to_async_thread_interrupted", e);
            }
        }
    }
    
    public void handleStartTime(
            String requestId, long timeStamp,
            ContainerTypeOrApplicationType type) {
        StartTimeTO stto = startTimeFreeQ.poll();
        if (stto == null) {
            stto = new StartTimeTO();
        }
        stto.setRequestId(requestId);
        stto.setTimeStamp(timeStamp);
        stto.setContainerTypeOrApplicationType(type);
        boolean success = false;
        while (!success) {
            try {
                startTimeQ.put(stto);
                success = true;
            } catch (InterruptedException e) {
                logger.log(
                        Level.FINE,
                        "callflow.transfer_to_async_thread_interrupted", e);
            }
        }
    }
    
    public void handleEndTime(
            String requestId, long timeStamp,
            ContainerTypeOrApplicationType type) {
        EndTimeTO etto = endTimeFreeQ.poll();
        if (etto == null) {
            etto = new EndTimeTO();
        }
        etto.setRequestId(requestId);
        etto.setTimeStamp(timeStamp);
        etto.setContainerTypeOrApplicationType(type);
        boolean success = false;
        while (!success) {
            try {
                endTimeQ.put(etto);
                success = true;
            } catch (InterruptedException e) {
                logger.log(
                        Level.FINE,
                        "callflow.transfer_to_async_thread_interrupted", e);
            }
        }
    }
    public void flush (){}
}
