/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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

import org.glassfish.admingui.common.util.V3AMX;

import org.glassfish.admin.amx.intf.config.Config;
import org.glassfish.admin.amx.logging.Logging;
import org.glassfish.admingui.common.util.GuiUtil;


public class LoggingHandlers {

    /** Creates a new instance of InstanceHandler */
    public LoggingHandlers() {
    }


    @Handler(id = "getLoggerLevels",
    output = {
        @HandlerOutput(name = "loggerList", type = List.class)
    })
    public static void getLoggerLevels(HandlerContext handlerCtx) {

        List result = new ArrayList();
        Logging logging = V3AMX.getInstance().getDomainRoot().getLogging();
        Map<String, String> loggerLevels = logging.getLoggingProperties();
        if (loggerLevels != null)    {
            for(String oneLogger:  loggerLevels.keySet()){
                if (oneLogger.endsWith(".level")&& !oneLogger.equals(".level") ){
                    Map oneRow = new HashMap();
                    oneRow.put("loggerName", oneLogger.substring(0,oneLogger.lastIndexOf(".level")));
                    oneRow.put("level", loggerLevels.get(oneLogger));
                    oneRow.put("selected", false);
                    result.add(oneRow);
                }
            }
        }
        handlerCtx.setOutputValue("loggerList",  result);
     }


    @Handler(id = "changeLoggerLevels",
    input = {
        @HandlerInput(name = "newLogLevel", type = String.class, required = true),
        @HandlerInput(name = "allRows", type = List.class, required = true)},
    output = {
        @HandlerOutput(name = "newList", type = List.class)})
    public static void changeLoggerLevels(HandlerContext handlerCtx) {
        String newLogLevel = (String) handlerCtx.getInputValue("newLogLevel");
        List obj = (List) handlerCtx.getInputValue("allRows");
        List<Map> allRows = (List) obj;
        if (GuiUtil.isEmpty(newLogLevel)){
            handlerCtx.setOutputValue("newList",  allRows);
            return;
        }
        for(Map oneRow : allRows){
            boolean selected = (Boolean) oneRow.get("selected");
            if (selected){
                oneRow.put("level", newLogLevel);
                oneRow.put("selected", false);
            }
        }
        handlerCtx.setOutputValue("newList",  allRows);
     }


    @Handler(id = "updateLoggerLevels",
    input = {
        @HandlerInput(name = "allRows", type = List.class, required = true)})
    public static void updateLoggerLevels(HandlerContext handlerCtx) {
        List<Map<String,String>> allRows = (List<Map<String,String>>) handlerCtx.getInputValue("allRows");
        Map<String,String> props = new HashMap();
        for(Map<String,String> oneRow : allRows){
            props.put(oneRow.get("loggerName")+".level", oneRow.get("level"));
        }
        Logging logging = V3AMX.getInstance().getDomainRoot().getLogging();
        logging.updateLoggingProperties(props);
     }


    @Handler(id = "getLoggingAttributes",
    output = {
        @HandlerOutput(name = "attrs", type = Map.class)
    })
    public static void getLoggingAttributes(HandlerContext handlerCtx) {

        Map<String, String>attrs = new HashMap();
        Logging logging = V3AMX.getInstance().getDomainRoot().getLogging();
        Map<String, String>longAttrs = logging.getLoggingAttributes();
        for(String oneAttr:  longAttrs.keySet()){
            attrs.put(oneAttr.substring(oneAttr.lastIndexOf(".")+1), longAttrs.get(oneAttr));
        }
        handlerCtx.setOutputValue("attrs",  attrs);
     }

    @Handler(id = "saveLoggingAttributes",
    input = {
        @HandlerInput(name = "attrs", type = Map.class)
    })
    public static void saveLoggingAttributes(HandlerContext handlerCtx) {

        Map<String, String>attrs = (Map)handlerCtx.getInputValue("attrs");
        if (attrs.get("logtoConsole") == null){
            attrs.put("logtoConsole", "false");
        }
        if (attrs.get("useSystemLogging") == null){
            attrs.put("useSystemLogging", "false");
        }

        Logging logging = V3AMX.getInstance().getDomainRoot().getLogging();
        Map<String, String>longAttrs = logging.getLoggingAttributes();

        for(String oneAttr:  longAttrs.keySet()){
            String shortAttr = oneAttr.substring(oneAttr.lastIndexOf(".")+1);
            String newValue = attrs.get(shortAttr);
            longAttrs.put(oneAttr, (newValue == null)? "" : newValue);
        }
        logging.updateLoggingAttributes(longAttrs);
     }


    @Handler(id = "getValidLogLevels",
    output = {
        @HandlerOutput(name = "loggerList", type = List.class)
    })
    public static void getValidLogLevels(HandlerContext handlerCtx) {
        handlerCtx.setOutputValue("loggerList",  levels);
     }
    
    final private static List<String> levels= new ArrayList();
    static{
        levels.add("OFF");
        levels.add("SEVERE");
        levels.add("WARNING");
        levels.add("INFO");
        levels.add("CONFIG");
        levels.add("FINE");
        levels.add("FINER");
        levels.add("FINEST");
    }
}
        
 
