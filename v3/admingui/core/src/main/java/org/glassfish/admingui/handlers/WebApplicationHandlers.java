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

package org.glassfish.admingui.handlers;

import com.sun.appserv.management.config.ApplicationConfigConfig;
import com.sun.appserv.management.config.DeployedItemRefConfig;
import com.sun.appserv.management.ext.runtime.RuntimeMgr;
import com.sun.appserv.management.util.misc.FileUtils;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import java.io.File;
import java.io.StringReader;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.admingui.util.TargetUtil;
import org.glassfish.web.plugin.common.WebAppConfig;
import org.jvnet.hk2.config.ConfigParser;
import org.jvnet.hk2.config.DomDocument;
import org.glassfish.web.plugin.common.ContextParam;
import org.glassfish.web.plugin.common.EnvEntry;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;

import org.glassfish.admingui.common.util.AMXRoot;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 *
 * @author anilam
 */
public class WebApplicationHandlers {

    /**
     *	<p> This handler returns the <env-entry> and <context-param> list for display
     */
    @Handler(id="getWebDDInfo",
        input={
            @HandlerInput(name="appName", type=String.class, required=true),
            @HandlerInput(name="serverName", type=String.class, defaultValue="server"),
            @HandlerInput(name="appType", type=String.class, required=true)},
        output={
            @HandlerOutput(name="envList", type=List.class),
            @HandlerOutput(name="ctxParamList", type=List.class)})
    public static void getWebDDInfo(HandlerContext handlerCtx) {
        
        String appName = (String) handlerCtx.getInputValue("appName");
        String appType = (String) handlerCtx.getInputValue("appType");
        String serverName = (String) handlerCtx.getInputValue("serverName");
        
        List envList = new ArrayList();
        List ctxParamList = new ArrayList();
        
        // Get the <env-entry> list from the DD
        RuntimeMgr runtimeMgr = AMXRoot.getInstance().getRuntimeMgr();
        String webXML = runtimeMgr.getDeploymentDescriptors(appName).get(WEB_APP_TYPE);  //get the content of web.xml;
        
        if (GuiUtil.isEmpty(webXML)){
            handlerCtx.setOutputValue("envList", envList);
            handlerCtx.setOutputValue("ctxParamList", ctxParamList);

            //TODO  the following is just for development until we really able to read in web.xml
            System.out.println("!!! ERROR:  Cannot read in web.xml,  just try reading from /tmp/web.xml ");
            try{
                webXML = FileUtils.fileToString(new File("/tmp/web.xml"));
            //System.out.println(webXML);
            }catch(Exception ex1){
                System.out.println("!!!!!! cannot open /tmp/web.xml");
            }
        }
        
        NodeList envNodeList = null;
        NodeList ctxParamNodeList = null;
        try{
            DOMParser parser = new DOMParser();
            parser.parse(new InputSource( new StringReader(webXML)));
            Document document = parser.getDocument();
            envNodeList = document.getElementsByTagName(ENV_ENTRY);
            ctxParamNodeList = document.getElementsByTagName(CONTEXT_PARAM);
        }catch(Exception ex){
            ex.printStackTrace();
            handlerCtx.setOutputValue("envList", envList);
            handlerCtx.setOutputValue("ctxParamList", ctxParamList);
            return;
        }

        //If there is no <env-entry> and no <context-param> in DD, there is nothing to customized, so return an empty table list.
        if ( (envNodeList == null || envNodeList.getLength() <=0) &&
            (ctxParamNodeList == null || ctxParamNodeList.getLength() <=0) ){
            handlerCtx.setOutputValue("envList", envList);
            handlerCtx.setOutputValue("ctxParamList", ctxParamList);
            return;
        }
        
        // Get the customized list in domain.xml
        DeployedItemRefConfig refConfig = TargetUtil.getDeployedItemRefObject(appName, serverName);
        ApplicationConfigConfig acc = refConfig.getApplicationConfigConfigMap().get(appType);
        List<EnvEntry> envListFromDomain = new ArrayList();
        List<ContextParam> ctxParamListFromDomain = new ArrayList();
        if (acc != null){
            String rawData = acc.getConfig();
            WebAppConfig wac = getConfigData(rawData, true);
            if (wac != null){
                envListFromDomain = wac.getEnvEntry();
                ctxParamListFromDomain = wac.getContextParam();
            }
        }
        
        for(int i=0; i< envNodeList.getLength(); i++){
            HashMap oneRow = new HashMap();
            Node aNode = envNodeList.item(i);
            String envName = getChildNodeValue(ENV_ENTRY_NAME, aNode);
            oneRow.put("name",envName);
            oneRow.put("orig", getChildNodeValue(ENV_ENTRY_VALUE, aNode));
            oneRow.put("type", getChildNodeValue(ENV_ENTRY_TYPE, aNode));
            oneRow.put("desc", getChildNodeValue(DESC, aNode));
            oneRow.put("value", getEnvValueFromDomain(envName, envListFromDomain ));
            envList.add(oneRow);
        }
        
        for(int i=0; i< ctxParamNodeList.getLength(); i++){
            HashMap oneRow = new HashMap();
            Node aNode = ctxParamNodeList.item(i);
            String paramName = getChildNodeValue(PARAM_NAME, aNode);
            oneRow.put("paramName",paramName);
            oneRow.put("paramOrigValue", getChildNodeValue(PARAM_VALUE, aNode));
            oneRow.put("paramValue", getCtxParamValueFromDomain(paramName, ctxParamListFromDomain));
            ctxParamList.add(oneRow);
        }
        handlerCtx.setOutputValue("envList", envList);
        handlerCtx.setOutputValue("ctxParamList", ctxParamList);
    }
    
    
    
    @Handler(id="saveWebDDInfo",
        input={
            @HandlerInput(name="appName", type=String.class, required=true),
            @HandlerInput(name="serverName", type=String.class, defaultValue="server"),
            @HandlerInput(name="appType", type=String.class, required=true),
            @HandlerInput(name="envList", type=List.class, required=true),
            @HandlerInput(name="ctxParamList", type=List.class, required=true)})
    public static void saveWebDDInfo(HandlerContext handlerCtx) {
        
        String appName = (String) handlerCtx.getInputValue("appName");
        String appType = (String) handlerCtx.getInputValue("appType");
        String serverName = (String) handlerCtx.getInputValue("serverName");
        List<Map> envList = (List<Map>)handlerCtx.getInputValue("envList");
        List<Map> ctxParamList = (List<Map>)handlerCtx.getInputValue("ctxParamList");
        
        String configValue = "";
        
        StringBuffer newEntValues = new StringBuffer();
        for(Map oneRow: envList){
            String envName = (String) oneRow.get("name");
            String orig = (String) oneRow.get("orig");
            String newValue = (String) oneRow.get("value");
            if (!GuiUtil.isEmpty(newValue) && !(newValue.equals(orig))){
                String oneEntry=ENV_ENTRY_B + 
                        ENV_ENTRY_NAME_B + envName + ENV_ENTRY_NAME_E  + 
                        ENV_ENTRY_VALUE_B + newValue + ENV_ENTRY_VALUE_E + 
                        ENV_ENTRY_E;
                newEntValues.append(oneEntry);
            }
        }
        StringBuffer newParamsValues = new StringBuffer();
        for(Map oneRow : ctxParamList){
            String paramName = (String) oneRow.get("paramName");
            String orig = (String) oneRow.get("paramOrigValue");
            String newValue = (String) oneRow.get("paramValue");
            if (! GuiUtil.isEmpty(newValue) && !(newValue.equals(orig))){
                String oneParam = CONTEXT_PARAM_B + 
                        PARAM_NAME_B + paramName + PARAM_NAME_E +
                        PARAM_VALUE_B + newValue + PARAM_VALUE_E +
                        CONTEXT_PARAM_E;
                newParamsValues.append(oneParam);
            } 
        }
        if (newParamsValues.length() > 0 || newEntValues.length() > 0){
            configValue = WEB_APP_CONFIG_B + newParamsValues + newEntValues +  WEB_APP_CONFIG_E;
        }
        
        //If no customized value found for env-entry nor context-param, we may want to remove the <application-config> entirely.
        try{
            DeployedItemRefConfig refConfig = TargetUtil.getDeployedItemRefObject(appName, serverName);
            ApplicationConfigConfig acc = refConfig.getApplicationConfigConfigMap().get(appType);
            if (acc == null){
                //no <application-config> exists yet, need to create one.
                if (! GuiUtil.isEmpty(configValue)){
                    refConfig.createApplicationConfigConfig(WEB_APP_TYPE, URLEncoder.encode (configValue, ENCODING));
                }
                //no config, no customization,  just return.
                return;
            }

            
            if ( GuiUtil.isEmpty(configValue)){
                //no customization, remove the entry.
                refConfig.removeApplicationConfigConfig(WEB_APP_TYPE);
                return;
            }
            
            //update the config in domain.xml
            acc.setConfig(URLEncoder.encode (configValue, ENCODING));
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    private static String getChildNodeValue(String childNodeName, Node current){
        NodeList childNodes = current.getChildNodes();
        if (childNodes == null) return "";
        for(int i=0; i < childNodes.getLength(); i++){
            Node childNode = childNodes.item(i);
            String name = childNode.getLocalName();
            String text = childNode.getTextContent();
            if (childNodeName.equals(childNode.getLocalName())){
                return (text == null) ? "" : text;
            }
        }
        return "";
    }
        
    private static WebAppConfig getConfigData(String data,  boolean doDecoding){
        if ( GuiUtil.isEmpty(data))
            return null;
        try{
            if (doDecoding){
                data = URLDecoder.decode(data, ENCODING);
            }
            if (GuiUtil.isEmpty(data))
                return null;
            ConfigParser parser = new ConfigParser(GuiUtil.getHabitat());
            XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(new StringReader(data));
            DomDocument dom = parser.parse(reader);
            WebAppConfig topLevelElement = (WebAppConfig)dom.getRoot().get();
            return topLevelElement;
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
    
    private static String getEnvValueFromDomain(String name, List<EnvEntry> envListFromDomain ){
        if (envListFromDomain == null || envListFromDomain.size() <=0)
            return "";
        for(EnvEntry oneEnv : envListFromDomain){
            if (oneEnv.getEnvEntryName().equals(name)){
                String value = oneEnv.getEnvEntryValue();
                return (value == null) ? "" : value;
            }
        }
        return "";
    }
    
    private static String getCtxParamValueFromDomain(String name, List<ContextParam> ctxParamListFromDomain ){
        if (ctxParamListFromDomain == null || ctxParamListFromDomain.size() <=0)
            return "";
        for(ContextParam oneParam : ctxParamListFromDomain){
            if (oneParam.getParamName().equals(name)){
                String value = oneParam.getParamValue() ;
                return (value == null) ? "" : value;
            }
        }
        return "";
    }
    
    /** for encoding and decoding the config attribute contents */
    private static final String WEB_APP_TYPE = "web";
    private static final String ENCODING = "UTF-8";
    private static final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

    private static final String ENV_ENTRY = "env-entry";
    private static final String ENV_ENTRY_NAME = "env-entry-name";
    private static final String ENV_ENTRY_VALUE = "env-entry-value";
    private static final String ENV_ENTRY_TYPE = "env-entry-type";
    private static final String DESC = "description";

    private static final String WEB_APP_CONFIG_B = "<web-app-config>";
    private static final String WEB_APP_CONFIG_E = "</web-app-config>";
    private static final String ENV_ENTRY_B = "<" + ENV_ENTRY + ">";
    private static final String ENV_ENTRY_E = "</"+ ENV_ENTRY + ">";
    private static final String ENV_ENTRY_NAME_B = "<" + ENV_ENTRY_NAME + ">";
    private static final String ENV_ENTRY_NAME_E = "</" + ENV_ENTRY_NAME + ">";
    private static final String ENV_ENTRY_VALUE_B = "<" + ENV_ENTRY_VALUE + ">";
    private static final String ENV_ENTRY_VALUE_E = "</" + ENV_ENTRY_VALUE + ">";

    private static final String CONTEXT_PARAM = "context-param";
    private static final String PARAM_NAME = "param-name";
    private static final String PARAM_VALUE ="param-value" ;

    private static final String CONTEXT_PARAM_B = "<" + CONTEXT_PARAM + ">"; ;
    private static final String CONTEXT_PARAM_E = "<" + CONTEXT_PARAM + ">"; ;
    private static final String PARAM_NAME_B = "<" + PARAM_NAME + ">";
    private static final String PARAM_NAME_E = "</" + PARAM_NAME + ">";

    private static final String PARAM_VALUE_B ="<" + PARAM_VALUE + ">";
    private static final String PARAM_VALUE_E ="<" + PARAM_VALUE + ">";
   
}
