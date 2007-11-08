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
import com.sun.enterprise.web.connector.grizzly.async.AsyncProcessorTask;
import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The main object used by <code>CometHandler</code>. 
 * The <code>CometContext</code> is always available for <code>CometHandler</code>
 * and can be used to notify other <code>CometHandler</code>.
 *
 * Attributes can be added/removed the same way <code>HttpServletSession</code> 
 * is doing. It is not recommended to use attributes if this 
 * <code>CometContext</code> is not shared amongs multiple
 * context path (uses HttpServletSession instead).
 *
 * @author Jeanfrancois Arcand
 */
public class CometContext<E> {
    
    /**
     * Generic error message
     */
    protected final static String INVALID_COMET_HANDLER = "CometHandler cannot be null. " 
            + "This CometHandler was probably resumed and an invalid " 
            +  "reference was made to it.";
    
    /**
     * Main logger
     */
    private final static Logger logger = SelectorThread.logger();  
 
     
    /**
     * Attributes placeholder.
     */
    private ConcurrentHashMap attributes;
    
    
    /**
     * The context path associated with this instance.
     */
    private String contextPath;
    
    
    /**
     * Is the <code>CometContext</code> instance been cancelled.
     */
    protected boolean cancelled = false;
    
    
    /**
     * The list of registered <code>CometHandler</code>
     */
    private ConcurrentHashMap<CometHandler,SelectionKey> handlers;
    
    
    /**
     * The list of registered <code>AsyncProcessorTask</code>. This object
     * are mainly keeping the state of the Comet request.
     */    
    private ConcurrentLinkedQueue<AsyncProcessorTask> asyncTasks;    
    
    
    /**
     * The <code>CometSelector</code> used to register <code>SelectionKey</code>
     * for upcoming bytes.
     */
    private CometSelector cometSelector;
    
    
    /**
     * The <code>CometContext</code> continuationType. See <code>CometEngine</code>
     */
    protected int continuationType = CometEngine.AFTER_SERVLET_PROCESSING;
    
    
    /**
     * The default delay expiration before a <code>CometContext</code>'s
     * <code>CometHandler</code> are interrupted.
     */
    private long expirationDelay = 30 * 1000;
    
    
    /**
     * <tt>true</tt> if the caller of CometContext.notify should block when 
     * notifying other CometHandler.
     */
    private boolean blockingNotification = false;

    
    /**
     * The default NotificationHandler.
     */
    private NotificationHandler notificationHandler; 
    
    // ---------------------------------------------------------------------- //
    
    
    /**
     * Create a new instance
     * @param contextPath the context path 
     * @param type when the Comet processing will happen (see CometEngine).
     */
    public CometContext(String contextPath, int continuationType) {
        this.contextPath = contextPath;
        this.continuationType = continuationType;
        attributes = new ConcurrentHashMap();
        handlers = new ConcurrentHashMap<CometHandler,SelectionKey>();
        asyncTasks = new ConcurrentLinkedQueue<AsyncProcessorTask>();          
    }

    
    /**
     * Get the context path associated with this instance.
     * @return contextPath the context path associated with this instance
     */
    public String getContextPath(){
        return contextPath;
    }
    
    
    /**
     * Add an attibute.
     * @param key the key
     * @param value the value
     */
    public void addAttribute(Object key, Object value){
        attributes.put(key,value);
    }

    
    /**
     * Retrive an attribute.
     * @param key the key
     * @return Object the value.
     */
    public Object getAttribute(Object key){
        return attributes.get(key);
    }    
    
    
    /**
     * Remove an attribute.
     * @param key the key
     * @return Object the value
     */
    public Object removeAttribute(Object key){
        return attributes.remove(key);
    }  
    
    
    /**
     * Add a <code>CometHandler</code>. Client of this method might
     * make sure the <code>CometHandler</code> is removed when the 
     * <code>CometHandler.onInterrupt</code> is invoked.
     * @param handler a new <code>CometHandler</code>
     * @param completeExecution Add the Comethandler but don't block waiting
     *        for event.
     */
    public int addCometHandler(CometHandler handler, boolean completeExecution){
        Long threadId = Thread.currentThread().getId();
        SelectionKey key = CometEngine.getEngine().
                activateContinuation(threadId, this,completeExecution);

        if (key == null){
            throw new 
               IllegalStateException("Grizzly Comet hasn't been registered");
        }
        
        if (handler == null){
            throw new 
               IllegalStateException(INVALID_COMET_HANDLER);
        }

        if (!completeExecution){
            handlers.putIfAbsent(handler,key);
        } else {
            handlers.putIfAbsent(handler,new SelectionKey() {
                public void cancel() {
                }
                public SelectableChannel channel() {
                    throw new IllegalStateException();
                }
                public int interestOps() {
                    throw new IllegalStateException();
                }
                public SelectionKey interestOps(int ops) {
                    throw new IllegalStateException();
                }
                public boolean isValid() {
                    return true;
                }
                public int readyOps() {
                    throw new IllegalStateException();
                }
                public Selector selector() {
                    throw new IllegalStateException();
                }
            });
        }
        return handler.hashCode();
    }
    
    
    /**
     * Add a <code>CometHandler</code>. Client on this method might
     * make sure the <code>CometHandler</code> is removed when the 
     * <code>CometHandler.onInterrupt</code> is invoked.
     * @param handler a new <code>CometHandler</code>
     */
    public int addCometHandler(CometHandler handler){
        return addCometHandler(handler,false);
    }
    
    
    /**
     * Retrive a <code>CometHandler</code> using its hashKey;
     */
    public CometHandler getCometHandler(int hashCode){
        Iterator<CometHandler> iterator = handlers.keySet().iterator();
        CometHandler cometHandler = null;
        while (iterator.hasNext()){
            cometHandler = iterator.next();
            if ( cometHandler.hashCode() == hashCode ){
               return cometHandler;
            }
        }
        return null;
    }   
    
    
    /**
     * Retrive a <code>CometHandler</code> using its SelectionKey. The 
     * <code>SelectionKey</code> is not exposed to the Comet API, hence this
     * method must be protected.
     */
    protected CometHandler getCometHandler(SelectionKey key){
        Iterator<CometHandler> iterator = handlers.keySet().iterator();
        CometHandler cometHandler = null;
        while (iterator.hasNext()){
            cometHandler = iterator.next();
            if (handlers.get(cometHandler) == key){
               return cometHandler;
            }
        }
        return null;        
    }
    
    
    /**
     * Notify all <code>CometHandler</code>. The attachment can be null.
     * The <code>type</code> will determine which code>CometHandler</code> 
     * method will be invoked:
     * 
     * CometEvent.INTERRUPT -> <code>CometHandler.onInterrupt</code>
     * CometEvent.NOTIFY -> <code>CometHandler.onEvent</code>
     * CometEvent.INITIALIZE -> <code>CometHandler.onInitialize</code>
     * CometEvent.TERMINATE -> <code>CometHandler.onTerminate</code>
     * CometEvent.READ -> <code>CometHandler.onEvent</code>
     * CometEvent.WRITE -> <code>CometHandler.onEvent</code>
     *
     * @param attachment An object shared amongst <code>CometHandler</code>. 
     * @param type The type of notification. 
     * @param key The SelectionKey associated with the CometHandler.
     */
    protected void notify(CometEvent event, int eventType, SelectionKey key) 
            throws IOException{
        CometHandler cometHandler = getCometHandler(key);
        if (cometHandler == null){
            throw new IllegalStateException(INVALID_COMET_HANDLER);
        }
        event.setCometContext(CometContext.this);  
        cometHandler.onEvent(event);
    }    
    
    
    /**
     * Remove a <code>CometHandler</code>. If the continuation (connection)
     * associated with this <code>CometHandler</code> no longer have 
     * <code>CometHandler</code> associated to it, it will be resumed.
     */
    public void removeCometHandler(CometHandler handler){
        removeCometHandler(handler,true);
    }
     
    
    /**
     * Remove a <code>CometHandler</code>. If the continuation (connection)
     * associated with this <code>CometHandler</code> no longer have 
     * <code>CometHandler</code> associated to it, it will be resumed.
     * @param handler The CometHandler to remove.
     * @param resume True is the connection can be resumed if no CometHandler
     *                    are associated with the underlying SelectionKey.
     */
    private void removeCometHandler(CometHandler handler,boolean resume){        
        SelectionKey key = handlers.remove(handler);
        if (resume && !handlers.containsValue(key)){
            CometEngine.getEngine().resume(key,this);
        }
    }
    
    
    /**
     * Remove a <code>CometHandler</code> based on its hashcode.
     */
    public void removeCometHandler(int hashCode){
        Iterator<CometHandler> iterator = handlers.keySet().iterator();
        CometHandler cometHandler = null;
        while (iterator.hasNext()){
            cometHandler = iterator.next();
            if ( cometHandler.hashCode() == hashCode ){
                iterator.remove();
                return;
            }
        }
    }
    
    
    /**
     * Resume the Comet request and remove it from the active CometHandler list.
     */
    public void resumeCometHandler(CometHandler handler){
        resumeCometHandler(handler,true);
    }
    
    
    /**
     * Resume the Comet request.
     * @param handler The CometHandler associated with the current continuation.
     * @param remove true if the CometHandler needs to be removed.
     */
    protected void resumeCometHandler(CometHandler handler, boolean remove){
        SelectionKey key = handlers.get(handler);
        if (key == null){
            throw new 
               IllegalStateException("Invalid CometHandler");
        }        
        CometEngine.getEngine().resume(key,this); 
        if (remove){
            removeCometHandler(handler,false);
        }
    }
    
    
    /**
     * Return true if this CometHandler is still active, e.g. there is 
     * still a continuation associated with it.
     */
    public boolean isActive(CometHandler cometHandler){
        if (cometHandler == null){
            throw new IllegalStateException(INVALID_COMET_HANDLER);
        }
        return (handlers.get(cometHandler) != null);
    }
    
    
    /**
     * Resume the Comet request and remove it from the active CometHandler list.
     */
    public void resumeCometHandler(CometHandler handler){
        SelectionKey key = handlers.get(handler);
        if (key == null){
            throw new 
               IllegalStateException("Invalid CometHandler");
        }        
        CometEngine.getEngine().resume(key,this); 
        removeCometHandler(handler);
    }
    
    
    /**
     * Notify all <code>CometHandler</code>. The attachment can be null. All
     * <code>CometHandler.onEvent()</code> will be invoked.
     * @param attachment An object shared amongst <code>CometHandler</code>. 
     */
    public void notify(final E attachment) throws IOException{
        CometEvent event = new CometEvent<E>();
        event.setType(CometEvent.NOTIFY);
        event.attach(attachment);
        event.setCometContext(CometContext.this);
        Iterator<CometHandler> iterator = handlers.keySet().iterator();
        notificationHandler.setBlockingNotification(blockingNotification);
        notificationHandler.notify(event,iterator);
        registerKeys();           
    }
    
    
    /**
     * Notify a single <code>CometHandler</code>. The attachment can be null.
     * The <code>type</code> will determine which code>CometHandler</code> 
     * method will be invoked:
     * 
     * CometEvent.INTERRUPT -> <code>CometHandler.onInterrupt</code>
     * CometEvent.NOTIFY -> <code>CometHandler.onEvent</code>
     * CometEvent.INITIALIZE -> <code>CometHandler.onInitialize</code>
     * CometEvent.TERMINATE -> <code>CometHandler.onTerminate</code>
     * CometEvent.READ -> <code>CometHandler.onEvent</code>
     *
     * @param attachment An object shared amongst <code>CometHandler</code>. 
     * @param type The type of notification. 
     * @param cometHandlerID Notify a single CometHandler.
     */
    public void notify(final E attachment,final int eventType,final int cometHandlerID) 
            throws IOException{   
        CometHandler cometHandler = getCometHandler(cometHandlerID);
  
        if (cometHandler == null){
            throw new IllegalStateException(INVALID_COMET_HANDLER);
        }
        CometEvent event = new CometEvent<E>();
        event.setType(eventType);
        event.attach(attachment);
        event.setCometContext(CometContext.this);
        
        notificationHandler.setBlockingNotification(blockingNotification);        
        notificationHandler.notify(event,cometHandler);
        if (event.getType() == CometEvent.TERMINATE 
            || event.getType() == CometEvent.INTERRUPT) {
            resumeCometHandler(cometHandler);
        } else {
            registerKeys(); 
        }
    }
    

    
    /**
     * Initialize the newly added <code>CometHandler</code>. 
     *
     * @param attachment An object shared amongst <code>CometHandler</code>. 
     * @param type The type of notification. 
     * @param key The SelectionKey representing the CometHandler.
     */      
    protected void initialize(SelectionKey key) throws IOException {
        CometEvent event = new CometEvent<E>();
        event.setType(event.INITIALIZE);
        event.setCometContext(this);
        
        Iterator<CometHandler> iterator = handlers.keySet().iterator();
        CometHandler cometHandler = null;
        while(iterator.hasNext()){
            cometHandler = iterator.next();
            if(handlers.get(cometHandler).equals(key)){
                cometHandler.onInitialize(event);
                break;
            }
        }
    }
    
    
    /**
     * Notify all <code>CometHandler</code>. The attachment can be null.
     * The <code>type</code> will determine which code>CometHandler</code> 
     * method will be invoked:
     * 
     * CometEvent.INTERRUPT -> <code>CometHandler.onInterrupt</code>
     * CometEvent.NOTIFY -> <code>CometHandler.onEvent</code>
     * CometEvent.INITIALIZE -> <code>CometHandler.onInitialize</code>
     * CometEvent.TERMINATE -> <code>CometHandler.onTerminate</code>
     * CometEvent.READ -> <code>CometHandler.onEvent</code>
     *
     * @param attachment An object shared amongst <code>CometHandler</code>. 
     * @param type The type of notification. 
     */   
    public void notify(final E attachment,final int eventType) throws IOException{
        // XXX Use a pool of CometEvent instance.
        CometEvent event = new CometEvent<E>();
        event.setType(eventType);
        event.attach(attachment);
        event.setCometContext(CometContext.this);

        Iterator<CometHandler> iterator = handlers.keySet().iterator();
        notificationHandler.setBlockingNotification(blockingNotification);
        notificationHandler.notify(event,iterator);
        if (event.getType() == CometEvent.TERMINATE 
            || event.getType() == CometEvent.INTERRUPT) {
            while(iterator.hasNext()){
                resumeCometHandler(iterator.next());
            }
        } else {
            registerKeys();
        } 
    }

    
    /**
     * Register the current <code>AsyncProcessorTask</code> keys to the 
     * <code>CometSelector</code> so new bytes can be processed.
     */
    private synchronized void registerKeys(){
        CometTask cometTask;       
        for (AsyncProcessorTask asyncTask: asyncTasks){
            SelectionKey key = asyncTask.getSelectionKey();
            
            if (key == null) continue;
            
            cometTask = (CometTask)key.attachment();            
            // Will hapens when a single CometHandler is invoked.
            if (cometTask == null){
                cometTask = CometEngine.getEngine().getCometTask(this,key);
                key.attach(cometTask);
            }
            cometTask.setExpirationDelay(expirationDelay);
            cometTask.setExpireTime(System.currentTimeMillis());
        }  
    }    
    
    
    /**
     * Register for asynchronous read. If your client supports http pipelining,
     * invoking this method might result in a state where your CometHandler
     * is invoked with a CometRead that will read the next http request. In that
     * case, it is strongly recommended to not use that method unless your
     * CometHandler can handle the http request.
     * @oaram handler The CometHandler that will be invoked.
     */
    public boolean registerAsyncRead(CometHandler handler){
        SelectionKey key = handlers.get(handler);
        if (handler == null){
            throw new 
               IllegalStateException(INVALID_COMET_HANDLER);            
        }
        // Retrieve the CometSelector key.
        SelectionKey cometKey = cometSelector.cometKeyFor(key.channel());
        if (cometKey != null){
            cometKey.interestOps(cometKey.interestOps() | SelectionKey.OP_READ); 
            if (cometKey.attachment() != null){
                ((CometTask)cometKey.attachment()).setAsyncReadSupported(true);
            }
            return true;
        } else {
            return false;
        }  
    }
    
    
    /**
     * Register for asynchronous write.
     */
    public boolean registerAsyncWrite(CometHandler handler){
        SelectionKey key = handlers.get(handler);
        if (handler == null || key == null){
            throw new 
               IllegalStateException(INVALID_COMET_HANDLER);            
        }
        // Retrieve the CometSelector key.
        SelectionKey cometKey = cometSelector.cometKeyFor(key.channel());
        if (cometKey != null){
            cometKey.interestOps(cometKey.interestOps() | SelectionKey.OP_WRITE); 
            return true;
        } else {
            return false;
        }           
    }
    
    
    /**
     * Recycle this object.
     */
    protected void recycle(){
        handlers.clear();
        attributes.clear();
        cancelled = false;
        asyncTasks.clear();
    }    

    
    /**
     * Is this instance beeing cancelled by the <code>CometSelector</code>
     * @return boolean cancelled or not.
     */
    protected boolean isCancelled() {
        return cancelled;
    }

    
    /**
     * Cancel this object or "uncancel".
     * @param cancelled true or false.
     */
    protected void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }


    /**
     * Add a <code>AsyncProcessorTask</code>.
     * @param asyncTask the <code>AsyncProcessorTask</code>
     */
    protected void addAsyncProcessorTask(AsyncProcessorTask asyncTask){
        asyncTasks.add(asyncTask);
    }
    
    
    /**
     * Return the list of <code>AsyncProcessorTask</code>
     * @return ConcurrentLinkedQueue the list of <code>AsyncProcessorTask</code>
     */
    protected ConcurrentLinkedQueue<AsyncProcessorTask> getAsyncProcessorTask(){
        return asyncTasks;
    }

    
    /**
     * Get the <code>CometSelector</code> associated with this instance.
     * @return CometSelector the <code>CometSelector</code> associated with 
     *         this instance.
     */
    protected CometSelector getCometSelector() {
        return cometSelector;
    }

    
    /**
     * Set the <code>CometSelector</code> associated with this instance.
     * @param CometSelector the <code>CometSelector</code> associated with 
     *         this instance.
     */   
    protected void setCometSelector(CometSelector cometSelector) {
        this.cometSelector = cometSelector;
    }
    
    
    /**
     * Helper.
     */
    public String toString(){
        return contextPath;
    }

    
    /**
     * Return the <code>long</code> delay before a request is resumed.
     * @return long the <code>long</code> delay before a request is resumed.
     */
    public long getExpirationDelay() {
        return expirationDelay;
    }

    
    /**
     * Set the <code>long</code> delay before a request is resumed.
     * @param long the <code>long</code> delay before a request is resumed.
     */    
    public void setExpirationDelay(long expirationDelay) {
        this.expirationDelay = expirationDelay;
    }   

    
    /**
     * Invoke <code>CometHandler.onTerminate</code>
     */
    protected void interrupt(SelectionKey key){
        CometEvent event = new CometEvent<E>();
        event.setType(CometEvent.INTERRUPT);
        event.attach(null);
        event.setCometContext(this);
        
        closeConnection(event,key);
    }
       
    
    /**
     * Advise <code>CometHandler</code> the connection will be closed.
     */
    private void closeConnection(CometEvent event, SelectionKey key){    
        Iterator<CometHandler> iterator = handlers.keySet().iterator();
        CometHandler handler;
        while(iterator.hasNext()){         
            handler = iterator.next();
            if ( handlers.get(handler).equals(key) ){
                try{
                    handler.onInterrupt(event);
                    iterator.remove();
                } catch (IOException ex){
                    logger.log(Level.WARNING,"Exception: ",ex);
                }
                break;
            }
        }        
    }
    
    
    /**
     * Return <tt>true</tt> if the invoker of notify() should block when
     * notifying Comet Handlers.
     */
    public boolean isBlockingNotification() {
        return blockingNotification;
    }

    
    /**
     * Set to <tt>true</tt> if the invoker of notify() should block when
     * notifying Comet Handlers.
     */
    public void setBlockingNotification(boolean blockingNotification) {
        this.blockingNotification = blockingNotification;
    }

    
    public void setNotificationHandler(NotificationHandler notificationHandler){
        this.notificationHandler = notificationHandler;
    }
    

    public NotificationHandler getNotificationHandler(){
        return notificationHandler;
    }


}
