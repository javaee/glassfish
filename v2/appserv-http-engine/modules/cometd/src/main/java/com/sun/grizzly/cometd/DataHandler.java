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

import com.sun.enterprise.web.connector.grizzly.SelectorThread;
import com.sun.enterprise.web.connector.grizzly.comet.CometContext;
import com.sun.enterprise.web.connector.grizzly.comet.CometEvent;
import com.sun.enterprise.web.connector.grizzly.comet.CometHandler;
import com.sun.enterprise.web.connector.grizzly.comet.CometInputStream;
import com.sun.grizzly.cometd.bayeux.Data;
import com.sun.grizzly.cometd.bayeux.Verb;
import com.sun.grizzly.cometd.bayeux.VerbUtils;
import com.sun.grizzly.cometd.util.JSONParser;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CometHandler used to support the meta channel Verb Connect and Reconnect.
 * The DataHandler is holding the state of the long polled (Comet) connection.
 * 
 * @author Jeanfrancois Arcand
 * @author TAKAI, Naoto
 */
public class DataHandler implements CometHandler<Object[]>{
    
    private final static Logger logger = SelectorThread.logger();
    
    private CometdRequest req;    
    
    private CometdResponse res; 
   
    public String clientIP;
   
    private String channel;
    
    public void attach(Object[] reqRes){
        this.req = (CometdRequest) reqRes[0];
        this.res = (CometdResponse) reqRes[1];
    }
            
            
    public void onEvent(CometEvent event) throws IOException{ 
        Object obj = event.attachment();
        try{                   
            if (obj instanceof Data){
                Data data = (Data)obj;
                String sdata = data.getJSONData();
                res.write(sdata);
                res.flush();
                event.getCometContext().resumeCometHandler(this);
            }
        }  catch (Throwable t){
           logger.log(Level.SEVERE,"Data.onEvent",t);
        } 
        
        if (event.getType()  == CometEvent.READ){
            CometInputStream is = (CometInputStream)obj;
            
            // XXX This is dangerous...
            byte[] dataStream = new byte[2 * 8192];
            is.setReadTimeout(2000);
            while (is.read(dataStream) > 0){
            }
            String sdata = new String(dataStream).trim();
            
            if (sdata.length() <=1) return;  
            
            // XXX What abou the header.
            try{
                final Verb verb = 
                        VerbUtils.parse(JSONParser.parse(sdata));                
                // Notify our listener;
                CometContext cometContext = event.getCometContext();
                cometContext.notify(
                        BayeuxCometHandler.newCometdContext(req,res,verb),
                        CometEvent.NOTIFY,
                        (Integer)cometContext.getAttribute(
                            BayeuxCometHandler.BAYEUX_COMET_HANDLER));
                event.getCometContext().removeAttribute(this);
            } catch (Throwable t){
                logger.log(Level.SEVERE,"Data.onEvent",t);
            }
        }            
    }

    
    public void onInitialize(CometEvent event) throws IOException{  
    }


    public void onTerminate(CometEvent event) throws IOException{
    }


    public void onInterrupt(CometEvent event) throws IOException{       
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}