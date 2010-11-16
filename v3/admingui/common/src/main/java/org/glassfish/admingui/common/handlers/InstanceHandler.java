/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
 * InstanceHandler.java
 *
 * Created on August 10, 2006, 2:32 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
/**
 *
 * @author anilam
 */
package org.glassfish.admingui.common.handlers;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.admingui.common.util.RestResponse;
import org.glassfish.admingui.common.util.RestUtil;

public class InstanceHandler {

    /** Creates a new instance of InstanceHandler */
    public InstanceHandler() {
    }

    @Handler(id="getJvmOptionsValues",
        input={
            @HandlerInput(name="endpoint",   type=String.class, required=true),
            @HandlerInput(name="attrs", type=Map.class, required=false)
        },
        output={
            @HandlerOutput(name="result", type=java.util.List.class)})
    public static void getJvmOptionsValues(HandlerContext handlerCtx) {
        try{
            ArrayList<String> list = getJvmOptions(handlerCtx);
            handlerCtx.setOutputValue("result", GuiUtil.convertArrayToListOfMap(list.toArray(), "value"));
        }catch (Exception ex){
            ex.printStackTrace();
            handlerCtx.setOutputValue("result", new HashMap());
        }
    }
    
     public static ArrayList getJvmOptions(HandlerContext handlerCtx) {
        ArrayList<String> list;
        String endpoint = (String) handlerCtx.getInputValue("endpoint");
        if (!endpoint.endsWith(".json"))
            endpoint = endpoint + ".json";
        Map<String, Object> attrs = (Map<String, Object>) handlerCtx.getInputValue("attrs");
        Map result = (HashMap) RestUtil.restRequest(endpoint, attrs, "get", handlerCtx, false).get("data");
        list = (ArrayList<String>) ((Map<String, Object>) result.get("extraProperties")).get("leafList");
        if (list == null)
            list = new ArrayList<String>();
        return list;
    }
 
   // FIXME: There's no reason to call each endpoint once for each option.  They can
   // be passed several JVM Options, and they'll do the right thing.
   @Handler(id="saveJvmOptionValues",
        input={
            @HandlerInput(name="endpoint",   type=String.class, required=true),
            @HandlerInput(name="target",   type=String.class, required=true),
            @HandlerInput(name="attrs", type=Map.class, required=false),
            @HandlerInput(name="options",   type=List.class)} )
   public static void saveJvmOptionValues(HandlerContext handlerCtx) {
        try {
            String endpoint = (String) handlerCtx.getInputValue("endpoint");
            List<Map<String, String>> options = (List) handlerCtx.getInputValue("options");
            Map<String, Object> payload = new HashMap<String, Object>();
            payload.put("target", (String) handlerCtx.getInputValue("target"));
            deleteJvmOptions(handlerCtx);
            for (Map<String, String> oneRow : options) {
                String str = oneRow.get(PROPERTY_VALUE);
                ArrayList kv = getKeyValuePair(str);
                payload.put((String)kv.get(0), kv.get(1));
                addJvmOption(endpoint,payload);
            }
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    public static void addJvmOption(String endpoint, Map payload) throws Exception{
        if (endpoint.contains("profiler")) {
            payload.put("profiler", "true");
        }
        RestResponse response = RestUtil.post(endpoint, payload);
        if (!response.isSuccess()) {
            throw new Exception (response.getResponseBody());
        }
    }

    public static void deleteJvmOptions(HandlerContext handlerCtx) throws Exception{
        Map<String, Object> payload = new HashMap<String, Object>();
        String endpoint = (String) handlerCtx.getInputValue("endpoint");
        String target = (String) handlerCtx.getInputValue("target");
        payload.put("target", target);
        ArrayList list = getJvmOptions(handlerCtx);
        for (Object s: list) {
            String str = (String)s;
            ArrayList kv = getKeyValuePair(str);
            payload.put((String)kv.get(0), kv.get(1));
            if (endpoint.contains("/profiler")) {
                endpoint = endpoint.substring(0, endpoint.indexOf("/profiler")) + "/jvm-options";
                payload.put("profiler", "true");
            }
            RestResponse response = RestUtil.delete(endpoint, payload);
            if (!response.isSuccess()) {
                throw new Exception (response.getResponseBody());
            }
        }
    }

    public static ArrayList getKeyValuePair(String str) {
        ArrayList list = new ArrayList(2);
        int index = str.indexOf("=");
        String key = "";
        String value = "";
        if (index != -1) {
            key = str.substring(0,str.indexOf("="));
            value = str.substring(str.indexOf("=")+1,str.length());
        } else {
            key = str;
        }
        if (key.startsWith("-XX:"))
            key = "\"" + key + "\"";
        list.add(0, key);
        list.add(1, value);
        return list;
    }

    private static final String PROPERTY_VALUE = "value";
}
        
 
