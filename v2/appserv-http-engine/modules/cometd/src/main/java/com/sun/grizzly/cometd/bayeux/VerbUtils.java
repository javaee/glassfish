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

package com.sun.grizzly.cometd.bayeux;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

/**
 * This class unmarshal a JSON message into a Java Object.
 * @author Jeanfrancois Arcand
 */
public class VerbUtils {
    
    private final static String META = "/meta";
    private final static String HANDSHAKE = "/handshake";
    private final static String CONNECT = "/connect";
    private final static String DISCONNECT = "/disconnect";    
    private final static String RECONNECT = "/reconnect";    
    private final static String SUBSCRIBE = "/subscribe";
    private final static String UNSUBSCRIBE = "/unsubscribe";    
    private final static String STATUS = "/status";
    private final static String PING = "/ping";
    private final static String DATA = "/data";    
    
    
    public VerbUtils() {
    }
    
    
    public static Verb parse(Object verb){   
        
        if (verb.getClass().isArray()){
            int length = Array.getLength(verb);
            for (int i=0; i < length; i++){
                return parseMap((Map)Array.get(verb,i));
            }
        }
        throw new RuntimeException("Wrong type");  
    }
    
    
    protected static Verb parseMap(Map map){
        
        String channel = (String)map.get("channel");
        
        if (!channel.startsWith(META)){
            return newData(map);
        }
        
        VerbBase vb = null;
        if (channel.indexOf(HANDSHAKE) != -1){
            vb = newHandshake(map);
        } else if (channel.indexOf(CONNECT) != -1){
            vb = newConnect(map);
        } else if (channel.indexOf(DISCONNECT) != -1){
            vb = newDisconnect(map);            
        } else if (channel.indexOf(RECONNECT) != -1){
            vb = newReconnect(map);
        } else if (channel.indexOf(SUBSCRIBE) != -1){
            vb = newSubscribe(map);
        } else if (channel.indexOf(UNSUBSCRIBE) != -1){
            vb = newUnsubscribe(map);
        } else if (channel.indexOf(PING) != -1){
            vb = newPing(map);
        } else if (channel.indexOf(STATUS) != -1){
            vb = newStatus(map);
        }
        configureExt(vb,map);        
        return vb;
    }
    
    
    private final static Handshake newHandshake(Map map){
        Handshake handshake = new Handshake();
        
        handshake.setAuthScheme((String)map.get("authScheme"));
        handshake.setAuthUser((String)map.get("authUser"));
        handshake.setAuthToken((String)map.get("authToken"));
        handshake.setChannel((String)map.get("channel"));
        handshake.setVersion(String.valueOf((Double)map.get("version")));
        handshake.setMinimumVersion(String.valueOf((Double)map.get("minimumVersion")));
        handshake.setAdvice(new Advice());

        return handshake; 
    }
    
    
    private final static Connect newConnect(Map map){
        Connect connect = new Connect();
        
        connect.setAuthToken((String)map.get("authToken"));
        connect.setChannel((String)map.get("channel"));
        connect.setClientId((String)map.get("clientId"));
        connect.setConnectionType((String)map.get("connectionType"));
        
        return connect;
    }
    
    
    private final static Disconnect newDisconnect(Map map){
        Disconnect disconnect = new Disconnect();
        
        disconnect.setAuthToken((String)map.get("authToken"));
        disconnect.setChannel((String)map.get("channel"));
        disconnect.setClientId((String)map.get("clientId"));
        disconnect.setConnectionType((String)map.get("connectionType"));
        
        return disconnect;
    }    
    
    
    private final static Reconnect newReconnect(Map map){
        Reconnect reconnect = new Reconnect();
        
        reconnect.setAuthToken((String)map.get("authToken"));
        reconnect.setChannel((String)map.get("channel"));
        reconnect.setClientId((String)map.get("clientId"));
        reconnect.setConnectionType((String)map.get("connectionType"));
        
        return reconnect;
    } 
    
    
    private final static Data newData(Map map){
        Data data = new Data();
        
        data.setChannel((String)map.get("channel"));
        data.setClientId((String)map.get("clientId"));
        data.setData((HashMap)map.get("data"));
        
        return data;
    }
    
    
    private final static Subscribe newSubscribe(Map map){
        Subscribe subscribe = new Subscribe();
        
        subscribe.setChannel((String)map.get("channel"));
        subscribe.setAuthToken((String)map.get("authToken"));
        subscribe.setSubscription((String)map.get("subscription"));
        subscribe.setClientId((String)map.get("clientId"));
        return subscribe;
    }
    
        
    private final static Unsubscribe newUnsubscribe(Map map){
        Unsubscribe unsubscribe = new Unsubscribe();
        
        unsubscribe.setChannel((String)map.get("channel"));
        unsubscribe.setAuthToken((String)map.get("authToken"));
        unsubscribe.setSubscription((String)map.get("subscription"));
        unsubscribe.setClientId((String)map.get("clientId"));
        return unsubscribe;
    }
    
    
    private final static Ping newPing(Map map){
        Ping ping = new Ping();
        
        ping.setChannel((String)map.get("channel"));
        return ping;
    }
    
    
    private final static Status newStatus(Map map){
        Status status = new Status();
        
        status.setChannel((String)map.get("channel"));
        return status;        
    }
    
    
    private static void configureExt(VerbBase vb, Map map){
        Map extMap = (Map)map.get("ext");
        if (extMap == null) return;
        
        Ext ext = new Ext();
        ext.setExtensionMap(extMap);
        vb.setExt(ext);
    }
    
}
