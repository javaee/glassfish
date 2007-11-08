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

package com.sun.enterprise.web.connector.grizzly.comet;

import com.sun.enterprise.web.connector.grizzly.SelectorThread;
import com.sun.enterprise.web.connector.grizzly.NioProvider;
import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * NIO <code>Selector</code> allowing <code>CometHandler</code> to receive
 * non-blocking requests bytes during request polling.
 *
 * @author Jeanfrancois Arcand
 */
public class CometSelector {

    /**
     * The <code>CometEngine</code> singleton
     */
    protected CometEngine cometEngine;
     
    
    /**
     * The <code>java.nio.channels.Selector</code>
     */
    private Selector selector;
       
    
    /**
     * Logger.
     */
    private Logger logger = SelectorThread.logger();
    
    
    /**
     * The list of <code>SelectionKey</code> to register with the 
     * <code>Selector</code>
     */
    private ConcurrentHashMap<SelectionKey,CometTask> keysToRegister 
            = new ConcurrentHashMap<SelectionKey,CometTask>();

    
    /**
     * New <code>CometSelector</code>
     * @param cometEngine The <code>CometEngine</code> singleton 
     */
    public CometSelector(CometEngine cometEngine) {
        this.cometEngine = cometEngine;
    }

    
    /**
     * Start the <code>java.nio.channels.Selector</code> running on its 
     * Thread.
     */
    public void start(){
        new Thread("CometSelector"){
            {
               setDaemon(true);                
            }
            
            public void run(){       
                try{
                    selector = Selector.open();
                } catch(IOException ex){
                    // Most probably a fd leak.
                    logger.log(Level.SEVERE,"CometSelector.open()",ex);
                    return;
                }
                while (true){
                    SelectionKey key = null;
                    Set readyKeys;
                    Iterator<SelectionKey> iterator;
                    int selectorState = 0; 

                    try{
                        selectorState = 0;
  
                        try{
                            selectorState = selector.select(1000);
                        } catch (CancelledKeyException ex){
                            ;
                        }

                        readyKeys = selector.selectedKeys();
                        iterator = readyKeys.iterator();                      
                        CometTask cometTask;
                        while (iterator.hasNext()) {
                            key = iterator.next();
                            iterator.remove();
                            if (key.isValid()) {
                                cometTask = (CometTask)key.attachment();
                                if (key.isReadable()){
                                    key.interestOps(key.interestOps() 
                                        & (~SelectionKey.OP_READ));
                                    cometTask.upcoming_op = 
                                            CometTask.OP_EVENT.READ;
                                } 
                                
                                if (key.isWritable()){
                                    key.interestOps(key.interestOps() 
                                        & (~SelectionKey.OP_WRITE));   
                                    cometTask.upcoming_op = 
                                            CometTask.OP_EVENT.WRITE;                                    
                                }
                                
                                if (cometTask.getSelectionKey().attachment() == null){
                                    cometTask.execute();
                                } else {
                                    key.cancel();
                                }
                            } else {
                                cancelKey(key);
                            }
                        }
                        
                        Iterator<SelectionKey> keys = 
                                keysToRegister.keySet().iterator();
                        /**
                         * The mainKey is the SelectionKey returned by the
                         * Selector used in the SelectorThread class.
                         */
                        SelectionKey mainKey;
                        SocketChannel channel;
                        CometTask cometTask;
                        while (keys.hasNext()){
                            mainKey = keys.next();
                            channel =  (SocketChannel)mainKey.channel();
                            if (mainKey.isValid() && channel.isOpen()) {
                                key = channel
                                    .register(selector,SelectionKey.OP_READ);  
                                cometTask = keysToRegister.remove(mainKey);
                                cometTask.setCometKey(key);
                                key.attach(cometTask); 
                                keys.remove();
                            } 
                        }                             
                        expireIdleKeys();
                        
                        if (selectorState <= 0){
                            selector.selectedKeys().clear();
                        }
                    } catch (Throwable t){
                        if (key != null){                           
                            try{
                                cancelKey(key);
                            } catch (Throwable t2){
                                logger.log(Level.SEVERE,"CometSelector",t2);
                            }
                        }
                                                
                        if (selectorState <= 0){
                            selector.selectedKeys().clear();
                        }
                        
                        try{
                            cancelKey(key);
                        } catch (Throwable t2){
                            logger.log(Level.SEVERE,"CometSelector",t2);
                        }
                                                
                        if (selectorState <= 0){
                            selector.selectedKeys().clear();
                        }
                        logger.log(Level.SEVERE,"CometSelector",t);
                    }      
                }   
            }
        }.start();
    }   
    
    
    /**
     * Expires registered <code>SelectionKey</code>. If a 
     * <code>SelectionKey</code> is expired, the request will be resumed and the 
     * HTTP request will complete,
     */
    protected void expireIdleKeys(){       
        Set<SelectionKey> readyKeys = selector.keys();
        if (readyKeys.isEmpty()){
            return;
        }
        long current = System.currentTimeMillis();
        Iterator<SelectionKey> iterator = readyKeys.iterator();
        SelectionKey key;
        while (iterator.hasNext()) {
            key = iterator.next();    
            CometTask cometTask = (CometTask)key.attachment();
            
            if (cometTask == null) return;
            
            long expire = cometTask.getExpireTime();
            if (current - expire >= cometTask.getExpirationDelay()) {
                cancelKey(key);
            } 
            
            /**
             * The connection has been resumed since the timeout is 
             * re-attached to the SelectionKey so cancel the Comet key.
             */
            if (cometTask.getSelectionKey() != null 
                    && cometTask.getSelectionKey().attachment() instanceof Long){
                key.cancel();
                cometEngine.interrupt(key);
            }
        }                    
    }  
 
    
    /**
     * Cancel a <code>SelectionKey</code>, and delegate the request
     * polling interruption to the <code>CometEngine</code>
     * @param key the expired <code>SelectionKey</code>
     */
    protected synchronized void cancelKey(SelectionKey key){
        if (!key.isValid()) return;
        
        try{
            CometTask cometTask = (CometTask)key.attachment();
            if (cometTask != null){
                SelectorThread st = cometTask.getSelectorThread();
                SelectionKey mainKey = cometTask.getSelectionKey();
                cometTask.getCometContext().interrupt(mainKey);        
                cometEngine.interrupt(key);                    
                st.cancelKey(mainKey);
            } else {
                cometEngine.interrupt(key);                    
            }
        } catch (Throwable t) {
            logger.log(Level.SEVERE,"CometSelector",t);
        } finally {
            key.attach(null);
        }
        key.cancel();
    }
    
    
    /**
     * Register the <code>SelectionKey/code> to the <code>Selector</code>. We
     * cannot register the <code>SelectionKey/code> directy on the
     * <code>Selector</code> because there is a deadlock in the VM (see bug XXX).
     */
    public void registerKey(SelectionKey key, CometTask cometTask){
        cometTask.setExpireTime(System.currentTimeMillis());
        keysToRegister.put(key, cometTask);
        selector.wakeup();
    }
    
    
    /**
     * Wakes up the <code>java.nio.channels.Selector</code> 
     */
    public void wakeup(){
        selector.wakeup();
    }
    
    /**
     * Return the SelectionKey associated with this channel.
     */
    public SelectionKey cometKeyFor(SelectableChannel channel){
        NioProvider nioP = NioProvider.getProvider();
        if (nioP == null) {
            return channel.keyFor(selector);
        } else {
            return nioP.keyFor(channel, selector);
        }
    }
}
