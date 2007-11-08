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

import com.sun.enterprise.web.connector.grizzly.Pipeline;
import com.sun.enterprise.web.connector.grizzly.TaskBase;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * Default Notificationhandler that uses the same a Grizzly Pipeline
 * to execute the notification process.
 *
 * @author Jeanfrancois Arcand
 */
public class DefaultNotificationHandler implements NotificationHandler{
    
    /**
     * The <code>Pipeline</code> used to execute threaded notification.
     */
    protected Pipeline pipeline;

    
    /**
     * <tt>true</tt> if the caller of CometContext.notify should block when 
     * notifying other CometHandler.
     */
    protected boolean blockingNotification = false;
    

    /**
     * Set the <code>Pipeline</code> used for notifying the CometHandler.
     */
    protected void setPipeline(Pipeline pipeline){
        this.pipeline = pipeline;
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

        
    /**
     * Notify all <code>CometHandler</code>. 
     * @param cometEvent the CometEvent used to notify CometHandler
     * @param iteratorHandlers An iterator over a list of CometHandler
     */
    public void notify(final CometEvent cometEvent,final Iterator<CometHandler> 
            iteratorHandlers) throws IOException{
        if (blockingNotification || pipeline == null){
            notify0(cometEvent,iteratorHandlers);
        } else {
            pipeline.addTask(new TaskBase(){
                public void doTask() throws IOException{
                    notify0(cometEvent,iteratorHandlers);
                }
            });
        }
    }


    protected void notify0(CometEvent cometEvent,Iterator<CometHandler> iteratorHandlers) 
            throws IOException{
        ArrayList<Throwable> exceptions = null;
        while(iteratorHandlers.hasNext()){
            try{
                notify0(cometEvent,iteratorHandlers.next());
            } catch (Throwable ex){
                if (exceptions == null){
                    exceptions = new ArrayList<Throwable>();
                }
                exceptions.add(ex);
            }
        }
        if (exceptions != null){
            StringBuffer errorMsg = new StringBuffer();
            for(Throwable t: exceptions){
                errorMsg.append(t.getMessage());
            }
            throw new IOException(errorMsg.toString());
        }
    }
    

    /**
     * Notify a single <code>CometHandler</code>. 
     * @param cometEvent the CometEvent used to notify CometHandler
     * @param cometHandler a CometHandler
     */
    public void notify(final CometEvent cometEvent,final CometHandler cometHandler) 
            throws IOException{
        if (blockingNotification || pipeline == null){
            notify0(cometEvent,cometHandler);
        } else {
            final ArrayList<Throwable> exceptions 
                    = new ArrayList<Throwable>();
            pipeline.addTask(new TaskBase(){
                public void doTask() throws IOException{
                    try{
                        notify0(cometEvent,cometHandler);
                    } catch (Throwable ex){
                        exceptions.add(ex);
                    }
                    if (exceptions.size() > 0){
                        StringBuffer errorMsg = new StringBuffer();
                        for(Throwable t: exceptions){
                            errorMsg.append(t.getMessage());
                        }
                        throw new IOException(errorMsg.toString());
                    }
                }
            });
        }
    }

    
    /**
     * Notify a <code>CometHandler</code>.
     * 
     * CometEvent.INTERRUPT -> <code>CometHandler.onInterrupt</code>
     * CometEvent.NOTIFY -> <code>CometHandler.onEvent</code>
     * CometEvent.INITIALIZE -> <code>CometHandler.onInitialize</code>
     * CometEvent.TERMINATE -> <code>CometHandler.onTerminate</code>
     * CometEvent.READ -> <code>CometHandler.onEvent</code>
     * CometEvent.WRITE -> <code>CometHandler.onEvent</code>
     *
     * @param attachment An object shared amongst <code>CometHandler</code>. 
     * @param cometHandler The CometHandler to invoke. 
     */
    protected void notify0(CometEvent cometEvent,CometHandler cometHandler) 
            throws IOException{
        switch (cometEvent.getType()) {
            case CometEvent.INTERRUPT:
                cometHandler.onInterrupt(cometEvent);
                break;
            case CometEvent.NOTIFY:
                cometHandler.onEvent(cometEvent);
                break;
            case CometEvent.READ:
                cometHandler.onEvent(cometEvent);
                break;      
            case CometEvent.WRITE:
                cometHandler.onEvent(cometEvent);
                break;                 
            case CometEvent.INITIALIZE:
                cometHandler.onInitialize(cometEvent);
                break;      
            case CometEvent.TERMINATE:
                cometHandler.onTerminate(cometEvent);
                break;                       
            default:
                throw new IllegalStateException();
        }
    }
}
