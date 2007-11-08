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

import com.sun.enterprise.web.connector.grizzly.comet.CometEngine;
import com.sun.enterprise.web.connector.grizzly.comet.CometEvent;
import com.sun.enterprise.web.connector.grizzly.comet.CometHandler;
import com.sun.enterprise.web.connector.grizzly.comet.DefaultNotificationHandler;
import com.sun.grizzly.cometd.bayeux.Data;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;

/**
 * Customized <code>NotificationHandler</code> that isolate notification to 
 * subscribed channel.
 *
 * @author Jeanfrancois Arcand
 */
public class CometdNotificationHandler extends DefaultNotificationHandler{

    /**
     * Notify only client subscribed to the active channel.
     */
    protected void notify0(CometEvent cometEvent,Iterator<CometHandler> iteratorHandlers) 
            throws IOException{
        ArrayList<Throwable> exceptions = null;
        CometHandler handler = null;
        Object o = cometEvent.attachment();
        String activeChannel = "";
        String channel = "";
        
        if (o instanceof Data){
            activeChannel = ((Data)o).getChannel();
        } else if (o instanceof DataHandler){
            activeChannel = ((DataHandler)o).getChannel();
        }
        while(iteratorHandlers.hasNext()){
            try{
                handler = (CometHandler)iteratorHandlers.next();
                
                if (handler instanceof CometdHandler){
                    channel = ((CometdHandler)handler).getChannel();
                } else if (handler instanceof DataHandler){
                    channel = ((DataHandler)handler).getChannel();
                } 
                       
                if (channel != null 
                        && channel.equals(BayeuxCometHandler.BAYEUX_COMET_HANDLER) 
                        || channel.equalsIgnoreCase(activeChannel)){
                    notify0(cometEvent,handler);
                } else if (channel == null){    
                    CometEngine.logger().log(Level.WARNING,"Channel was null");
                }
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
}
