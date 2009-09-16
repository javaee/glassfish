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

import org.glassfish.admingui.common.util.V3AMX;
import org.glassfish.admingui.common.util.GuiUtil;

import javax.management.ObjectName;
import javax.management.Attribute;
import org.glassfish.admin.amx.core.AMXProxy;


public class InstanceHandler {

    /** Creates a new instance of InstanceHandler */
    public InstanceHandler() {
    }


     @Handler(id = "getDebugInfo",
    input = {
        @HandlerInput(name = "debugOptions", type = String.class, required = true),
        @HandlerInput(name = "debugEnabled", type = Boolean.class, required = true)
    },
    output = {
        @HandlerOutput(name = "debugInfo", type = String.class)
    })
    public static void getDebugInfo(HandlerContext handlerCtx) {

        String debugOptions = (String) handlerCtx.getInputValue("debugOptions");
        String debugPort = "";
        StringTokenizer tokens = new StringTokenizer(debugOptions, ",");
        String doption = "";
        while (tokens.hasMoreTokens()) {
            doption = tokens.nextToken().trim();
            if (doption.startsWith("address")) {
                int pos = doption.indexOf("=");
                if (pos >= 0) {
                    debugPort = doption.substring(pos + 1).trim();
                    break;
                }
            }
        }

        Boolean debugEnabled = (Boolean) handlerCtx.getInputValue("debugEnabled");
        String msg = ("true".equals(""+debugEnabled)) ?
            GuiUtil.getMessage("inst.debugEnabled") + debugPort :
            GuiUtil.getMessage("inst.notEnabled");
        handlerCtx.setOutputValue("debugInfo", msg);

     }


@Handler(id="getProfilerAttrs",
    output={
        @HandlerOutput(name="objectName",        type=String.class),
        @HandlerOutput(name="edit",        type=Boolean.class)})

        public static void getProfilerAttrs(HandlerContext handlerCtx) {
        ObjectName objName = null;
        Boolean edit = false; 
        try{
            AMXProxy amx = V3AMX.getInstance().getConfig("server-config").getJava();
            objName = (ObjectName) amx.attributesMap().get("Profiler");
            if (objName != null) {
                edit = true;
            }
            handlerCtx.setOutputValue("edit", edit);
            handlerCtx.setOutputValue("objectName", objName);
        }catch (Exception ex){
            ex.printStackTrace();
            handlerCtx.setOutputValue("edit", false); 
        }
    }    

 @Handler(id="getJvmOptionsValues",
    input={
        @HandlerInput(name="objectNameStr",   type=String.class, required=true)},
    output={
        @HandlerOutput(name="result", type=java.util.List.class)})

        public static void getJvmOptionsValues(HandlerContext handlerCtx) {
        try{
            String objectNameStr = (String) handlerCtx.getInputValue("objectNameStr");
            AMXProxy  amx = (AMXProxy) V3AMX.getInstance().getProxyFactory().getProxy(new ObjectName(objectNameStr));
            final String[] options = (String[])amx.attributesMap().get("JvmOptions");
            handlerCtx.setOutputValue("result", GuiUtil.convertArrayToListOfMap(options, "Value"));
        }catch (Exception ex){
            ex.printStackTrace();
            handlerCtx.setOutputValue("result", new HashMap());
        }
    }   
 
     @Handler(id="saveJvmOptionValues",
    input={
        @HandlerInput(name="objectNameStr",   type=String.class, required=true),
        @HandlerInput(name="options",   type=List.class)} )
       public static void saveJvmOptionValues(HandlerContext handlerCtx) {
        List newList = new ArrayList();
        try {
            String objectNameStr = (String) handlerCtx.getInputValue("objectNameStr");
            ObjectName objectName = new ObjectName(objectNameStr);
            List<Map<String, String>> options = (List) handlerCtx.getInputValue("options");
            for (Map<String, String> oneRow : options) {
                String value = oneRow.get(PROPERTY_VALUE);
                if (!GuiUtil.isEmpty(value)) {
                    newList.add(value);
                }
            }
            V3AMX.setAttribute(objectName, new Attribute("JvmOptions", (String[]) newList.toArray(new String[0])));
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }
    
    /**
     *	<p> This handler restart DAS immediately.</p>
     */
    @Handler(id = "restartDomain")
    public static void restartDomain(HandlerContext handlerCtx) {
        V3AMX.getInstance().getRuntime().restartDomain();
    }


    /**
     *	<p> This handler stops DAS immediately.</p>
     */
    @Handler(id = "stopDomain")
    public static void stopDomain(HandlerContext handlerCtx) {
        V3AMX.getInstance().getDomainRoot().stopDomain();
    }

    /**
     *	<p> This method returns values for the JVM Report </p>
     *  <p> Output value: "ViewsList" -- Type: <code>java.util.Array</code>/</p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getJvmReport",
    input={
        @HandlerInput(name="type",            type=String.class) },
    output={
        @HandlerOutput(name="report",        type=String.class) })
    public static void getJvmReport(HandlerContext handlerCtx) {
        String type = (String)handlerCtx.getInputValue("type");
        if(type == null || type.equals("")){
            type = "summary";
        }
        try{
            String report = V3AMX.getInstance().getRuntime().getJVMReport(type);
            handlerCtx.setOutputValue("report", report);
        }catch(Exception ex){
            ex.printStackTrace();
            handlerCtx.setOutputValue("report", "");
        }
    }

    private static final String PROPERTY_VALUE = "Value";
}
        
 
