/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.grizzly.cometd.bayeux;

import java.util.Iterator;
import java.util.Map;

/**
 * Bayeux extension element.
 *
 * @author Jeanfrancois Arcand
 */
public class Ext extends VerbBase{
    
    private Map<String,Object> extensionMap;
    
    public Ext() {
    }

    
    public Map getExtensionMap() {
        return extensionMap;
    }

    
    public void setExtensionMap(Map<String,Object> extensionMap) {
        this.extensionMap = extensionMap;
    }
    
    
    public String toJSON() {        
        StringBuffer buf = new StringBuffer();
        Iterator<String> iterator = extensionMap.keySet().iterator();
        String key;
        boolean first = true;
        while (iterator.hasNext()){
            if (!first){
                buf.append(",");
            }
            first = false;
            key = iterator.next();
            buf.append("\"" + key
                + "\":" + extensionMap.get(key));
        }
                
        return "\"ext\":{" 
                + buf.toString()
                + "}";         
    }

}
