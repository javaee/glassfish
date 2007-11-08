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

package com.sun.grizzly.cometd;

import com.sun.enterprise.web.connector.grizzly.comet.CometContext;
import com.sun.enterprise.web.connector.grizzly.comet.CometEvent;
import com.sun.grizzly.cometd.bayeux.Connect;
import com.sun.grizzly.cometd.bayeux.Data;
import com.sun.grizzly.cometd.bayeux.Disconnect;
import com.sun.grizzly.cometd.bayeux.Handshake;
import com.sun.grizzly.cometd.bayeux.Reconnect;
import com.sun.grizzly.cometd.bayeux.Subscribe;
import com.sun.grizzly.cometd.bayeux.Unsubscribe;
import com.sun.grizzly.cometd.bayeux.Verb;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This class implement the Bauyeux Server side protocol. 
 *
 * @author Jeanfrancois Arcand
 * @author TAKAI, Naot
 */
public class BayeuxCometHandler extends BayeuxCometHandlerBase{
    
    
    public final static String DEFAULT_CONTENT_TYPE ="application/json";
    
    
    public final static String BAYEUX_COMET_HANDLER = "bayeuxCometHandler";

    
    private ConcurrentLinkedQueue<String> clientIds 
            = new ConcurrentLinkedQueue<String>();
    
    
    private Random random = new Random();
    
    
    private ConcurrentHashMap<String,DataHandler> activeHandler =
            new ConcurrentHashMap<String,DataHandler>();
    
    
    public void onHandshake(CometEvent event) throws IOException {            
        CometdContext cometdContext = (CometdContext)event.attachment();
        
        CometdRequest req = cometdContext.getRequest();
        CometdResponse res = cometdContext.getResponse();
        Handshake handshake = (Handshake)cometdContext.getVerb();
        
        boolean handshakeOK = true;
        String clientId  = "";
        synchronized(random){
            clientId = String.valueOf(Long.toHexString(random.nextLong()));
        }
        clientIds.add(clientId);
        
        // XXX Why do we need to cache the ID. Memory leak right now
        if (handshakeOK){
            handshake.setClientId(clientId);
        }

        res.setContentType(DEFAULT_CONTENT_TYPE);            
        res.write(handshake.toJSON());
        res.flush();
    }
    
    
    public void onConnect(CometEvent event) throws IOException {
        CometdContext cometdContext = (CometdContext)event.attachment();
        
        CometdRequest req = cometdContext.getRequest();
        CometdResponse res = cometdContext.getResponse();
        Connect connect = (Connect)cometdContext.getVerb();            
         
        res.setContentType(DEFAULT_CONTENT_TYPE);            
        res.write(connect.toJSON());
        res.flush();
    }
    
    
    public void onDisconnect(CometEvent event) throws IOException {
        CometdContext cometdContext = (CometdContext)event.attachment();
        
        CometdRequest req = cometdContext.getRequest();
        CometdResponse res = cometdContext.getResponse();
        Disconnect disconnect = (Disconnect)cometdContext.getVerb();      
        activeHandler.remove(disconnect.getClientId());
        
        res.setContentType(DEFAULT_CONTENT_TYPE);            
        res.write(disconnect.toJSON());
        res.flush();
    }
    

    public void onReconnect(CometEvent event) throws IOException {
        CometdContext cometdContext = (CometdContext)event.attachment();
        
        CometdRequest req = cometdContext.getRequest();
        CometdResponse res = cometdContext.getResponse();
        Reconnect reconnect = (Reconnect)cometdContext.getVerb();                    
                       
        CometContext cometContext = event.getCometContext();
        DataHandler requestHandler = new DataHandler();
        requestHandler.attach(new Object[]{req,res});
        DataHandler prevHandler = activeHandler
                .putIfAbsent(reconnect.getClientId(),requestHandler);
        // Set the previous channel value.
        if (prevHandler != null){
            requestHandler.setChannel(prevHandler.getChannel());
        }        
        cometContext.addCometHandler(requestHandler);   
        
        res.setContentType(DEFAULT_CONTENT_TYPE);            
        res.write(reconnect.toJSON());
        res.flush();
    }
    
    
    public void onSubscribe(CometEvent event) throws IOException {
        CometdContext cometdContext = (CometdContext)event.attachment();
        
        CometdRequest req = cometdContext.getRequest();
        CometdResponse res = cometdContext.getResponse();
        Subscribe subscribe = (Subscribe)cometdContext.getVerb();   
        
        DataHandler dataHandler = activeHandler.get(subscribe.getClientId());
        if (dataHandler != null){
            dataHandler.setChannel(subscribe.getSubscription());
        }         
        res.setContentType(DEFAULT_CONTENT_TYPE);            
        res.write(subscribe.toJSON());
        res.flush();
    }
    
    
    public void onUnsubscribe(CometEvent event) throws IOException {
        CometdContext cometdContext = (CometdContext)event.attachment();
        
        CometdRequest req = cometdContext.getRequest();
        CometdResponse res = cometdContext.getResponse();
        Unsubscribe unsubscribe = (Unsubscribe)cometdContext.getVerb();   
        
        activeHandler.remove(unsubscribe.getClientId());
        
        res.setContentType(DEFAULT_CONTENT_TYPE);            
        res.write(unsubscribe.toJSON());
        res.flush();
    }
    
    
    public void onData(CometEvent event) throws IOException {
        CometdContext cometdContext = (CometdContext)event.attachment();
        
        CometdRequest req = cometdContext.getRequest();
        CometdResponse res = cometdContext.getResponse();
        Data data = (Data)cometdContext.getVerb();           
         
        res.setContentType(DEFAULT_CONTENT_TYPE);            
        res.write(data.toJSON());
        res.flush(); 

        event.getCometContext().notify(data);
    }
    
    
    public final static CometdContext newCometdContext(final CometdRequest req, 
            final CometdResponse res,final Verb verb){
        return new CometdContext(){
            
            public Verb getVerb(){
                return verb;
            }
            
            public CometdRequest getRequest(){
                return req;
            }
                      
            public CometdResponse getResponse(){
                return res;
            }            
        };
    }  
    
        
    public void onTerminate(CometEvent event) throws IOException {
        onInterrupt(event);    
    }
    
    
    public void onInterrupt(CometEvent event) throws IOException {
        // TODO: Use the SelectionKey to find which Handler needs to be cancelled.   
    }
    
    // ---------------------------------------------------- Reserved but not used
    
    
    public void onPing(CometEvent event) throws IOException {
    }
    
    
    public void onStatus(CometEvent event) throws IOException {
    }

    
    public String getChannel() {
        return BAYEUX_COMET_HANDLER;
    }

    
    public void setChannel(String channel) {
        // Not supported
    }
 
}