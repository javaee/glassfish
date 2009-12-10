
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
 * GuiUtil.java
 *
 * Created on August 10, 2006, 9:19 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.glassfish.admingui.common.util;

import com.sun.jsftemplating.resource.ResourceBundleManager;

import javax.faces.context.FacesContext;
// FIXME: 7-31-08 -- FIX by importing woodstock api's:
//import com.sun.webui.jsf.model.Option;

import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.text.MessageFormat;
import java.net.URLEncoder;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import java.io.UnsupportedEncodingException;
import javax.faces.context.ExternalContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.deployment.client.DeploymentFacility;
import org.glassfish.deployment.client.ServerConnectionIdentifier;

import org.jvnet.hk2.component.Habitat;
import org.glassfish.admin.amx.util.ExceptionUtil;

/**
 *
 * @author anilam
 */
public class GuiUtil {
    /** Creates a new instance of GuiUtil */
    public GuiUtil() {
    }

    public static Logger getLogger() {
        return Logger.getLogger(LOGGER_NAME);
    }

    @Handler(id = "guiLog",
        input = {
            @HandlerInput(name = "message", type = String.class, required = true),
            @HandlerInput(name = "level", type = String.class)
        })
    public static void guiLog(HandlerContext handlerCtx) {
        String level = (String)handlerCtx.getInputValue("level");
        if (level == null) {
            level = "INFO";
        } else {
            level = level.toUpperCase();
        }
        getLogger().log(Level.parse(level), (String)handlerCtx.getInputValue("message"));
    }

    //return true if the String is null or is """
    public static boolean isEmpty(String str) {
        return (str == null || "".equals(str)) ? true : false;
    }

    public static String getMessage(String key, Object[] args) {
        if (key == null) {
            return null;
        }
        String value = getMessage(key);
        if (args != null) {
            MessageFormat mf = new MessageFormat(value);
            Object[] mfArgs = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                mfArgs[i] = getMessage(args[i].toString());
            }
            value = mf.format(mfArgs);
        }
        return value;
    }

    
    public static void initSessionAttributes(){

        getLogger().info("admin console: initSessionAttributes()" );
        ExternalContext externalCtx = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, Object> sessionMap = externalCtx.getSessionMap();
        try{
            sessionMap.put("domainName", V3AMX.getInstance().getDomainRoot().getAppserverDomainName());
        }catch(Exception ex){
            ex.printStackTrace();
            sessionMap.put("domainName", "");
        }

        String user = externalCtx.getRemoteUser();
        sessionMap.put("userName", (user == null) ? "" : user);

        Object request = externalCtx.getRequest();
        if (request instanceof javax.servlet.ServletRequest){
            ServletRequest srequest = (ServletRequest) request;
            String serverName = srequest.getServerName();
            sessionMap.put("serverName", serverName);
            sessionMap.put("serverPort", V3AMXUtil.getAdminPort());
            sessionMap.put("requestIsSecured", Boolean.valueOf(srequest.isSecure()));
        }else{
            //should never get here.
            sessionMap.put("serverName", "");
        }
        sessionMap.put("_noNetwork", (System.getProperty("com.sun.enterprise.tools.admingui.NO_NETWORK", "false").equals("true"))? Boolean.TRUE: Boolean.FALSE);
        sessionMap.put("supportCluster", Boolean.FALSE);
        sessionMap.put("appServerVersion", V3AMX.getInstance().getDomainRoot().getApplicationServerFullVersion());
        sessionMap.put("reqMsg", GuiUtil.getMessage("msg.JS.enterValue"));
        sessionMap.put("reqMsgSelect", GuiUtil.getMessage("msg.JS.selectValue"));
        sessionMap.put("reqInt", GuiUtil.getMessage("msg.JS.enterIntegerValue"));
        sessionMap.put("reqNum", GuiUtil.getMessage("msg.JS.enterNumericValue"));
        sessionMap.put("reqPort", GuiUtil.getMessage("msg.JS.enterPortValue"));
        sessionMap.put("RUNTIME", V3AMX.getInstance().getRuntime().objectName());
        sessionMap.put("DOMAIN_ROOT", V3AMX.getInstance().getDomainRoot().objectName());
        sessionMap.put("ADMIN_LISTENER", V3AMX.getInstance().getAdminListener().objectName());
        sessionMap.put("_SESSION_INITIALIZED","TRUE");
        sessionMap.put("restartRequired", Boolean.FALSE);

        /* refer to issue# 5698 and issue# 3691
         * There is a chance that this getAdminSessionTimoutInMinutes() throws an exception in Turkish locale.
         * In such a case, we catch and log the exception and assume it is set to 0.
         * Otherwise GUI's main page can't come up.
         */
        try {
            AMXProxy das = V3AMX.getInstance().getConfig("server-config").getAdminService().child("das-config");
            String timeOut = (String) das.attributesMap().get("AdminSessionTimeoutInMinutes");
            if ((timeOut != null) && (!timeOut.equals(""))) {
                int time = new Integer(timeOut).intValue();
                if (time == 0) {
                    ((HttpServletRequest) request).getSession().setMaxInactiveInterval(-1);
                } else {
                    ((HttpServletRequest) request).getSession().setMaxInactiveInterval(time * 60);
                }
            }
        } catch (Exception nfe) {
            ((HttpServletRequest) request).getSession().setMaxInactiveInterval(-1);
            nfe.printStackTrace();
        }


    }


    public static void setSessionValue(String key, Object value){
        Map sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        sessionMap.put(key, value);
    }

    public static Object getSessionValue(String key){
        Map sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        if (sessionMap.get("_SESSION_INITIALIZED") == null){
            initSessionAttributes();
        }
        return sessionMap.get(key);
    }

    public static DeploymentFacility getDeploymentFacility(){
        DeploymentFacility df = null;
        if (df == null){
            //df= DeploymentFacilityFactory.getDeploymentFacility();
            df = new LocalDeploymentFacility();
            String serverName = (String)getSessionValue("serverName");
            Integer serverPort = (Integer)getSessionValue("serverPort");
            int port = 4848;
            if (serverPort == null){
                getLogger().warning("getDeploymentFacility:  cannot get admin listener port, default to 4848");
            }else{
                port = serverPort.intValue();
            }
            String userName = (String)getSessionValue("userName");
            Boolean requestIsSecured = (Boolean)getSessionValue("requestIsSecured");

            ServerConnectionIdentifier sci = new ServerConnectionIdentifier(
                    serverName, port, userName,
                    "",     //password    FIXME: how to get password ?
                    requestIsSecured);
            df.connect(sci);   //although we pass in sci, it is ignored. refer to issue#6100
            setSessionValue("_DEPLOYMENT_FACILITY", df);
        }
        return df;
    }

    /**
     * <p> This method encodes the given String with the specified type.
     * <p> If type is not specified then it defaults to UTF-8.
     *
     * @param value String to be encoded
     * @param delim Reserved Characters don't want to be encoded
     * @param type Encoding type. Default is UTF-8
     */
    public static String encode(String value, String delim, String type) {
        if (value == null || value.equals("")) {
            return value;
        }
        if (type == null || type.equals("")) {
            type = "UTF-8"; //default encoding type.
        }
        String encdString = "";

        if (delim != null && delim.length() > 0) {
            StringTokenizer st = new StringTokenizer(value, delim, true);
            while (st.hasMoreTokens()) {
                String s = st.nextToken();
                if (delim.indexOf(s) >= 0) {
                    encdString = encdString.concat(s);
                } else {
                    try {
                        encdString += URLEncoder.encode(s, type);
                    } catch (UnsupportedEncodingException uex) {
                        try {
                            encdString += URLEncoder.encode(s, "UTF-8");
                        } catch (UnsupportedEncodingException ex) {
                            //we will never get here.
                            throw new IllegalArgumentException(ex);
                        }
                    }
                }
            }
        } // nothing to escape, encode the whole String
        else {
            try {
                encdString = URLEncoder.encode(value, type);
            } catch (UnsupportedEncodingException uex) {
                try {
                    encdString += URLEncoder.encode(value, "UTF-8");
                } catch (UnsupportedEncodingException ex) {
                    //we'll never get here.
                    throw new IllegalArgumentException(ex);
                }
            }
        }
        return encdString;
    }

    public static ResourceBundle getBundle (String resourceName) {
        Locale locale = com.sun.jsftemplating.util.Util.getLocale(FacesContext.getCurrentInstance());
        return ResourceBundleManager.getInstance().getBundle(resourceName, locale);
    }

    /*
     * returns the strings from org.glassfish.admingui.core.Strings 
     * if no such key exists, return the key itself.
     */

    public static String getMessage(String key) {

        try {
            // Get the Resource Bundle
            ResourceBundle bundle = (ResourceBundle) FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get(I18N_RESOURCE_BUNDLE);

            if (bundle == null) {
                Locale locale = com.sun.jsftemplating.util.Util.getLocale(FacesContext.getCurrentInstance());
                bundle = ResourceBundleManager.getInstance().getBundle(RESOURCE_NAME, locale);
                // Store it in the Request Map
                FacesContext.getCurrentInstance().getExternalContext().getRequestMap().put(I18N_RESOURCE_BUNDLE, bundle);
            }
            String ret = bundle.getString(key);
            return (ret == null) ? key : ret;
        } catch (NullPointerException ex) {
            return "";
        } catch (Exception ex1) {
            return key;
        }
    }

    public static String getMessage(String resourceName, String key) {
        ResourceBundle bundle = getBundle(resourceName);
        String ret = bundle.getString(key);
        return (ret == null) ? key : ret;
    }

    public static String getMessage(ResourceBundle bundle, String key) {
        String ret = bundle.getString(key);
        return (ret == null) ? key : ret;
    }

    public static Locale getLocale() {
        Locale locale = com.sun.jsftemplating.util.Util.getLocale(FacesContext.getCurrentInstance());
        return locale;
    }

    /* This method sets up the attributes of the <sun:alert> message box so that a 
     * saved sucessfully message will be displayed during refresh.
     */
    public static void prepareSuccessful(HandlerContext handlerCtx) {
        prepareAlert(handlerCtx, "success", GuiUtil.getMessage("msg.saveSuccessful"), null);
    }

    /* This method sets up the attributes of the <sun:alert> message box. It is similar 
     * to handleException without calling renderResponse()
     */
    public static void prepareException(HandlerContext handlerCtx, Throwable ex) {
        Throwable rootException = getRootCause(ex);
        prepareAlert(handlerCtx, "error", GuiUtil.getMessage("msg.Error"), rootException.getMessage());
        //TODO use logger
        ex.printStackTrace();
    }

    /* This method sets up the attributes of the <sun:alert> message box so that any
     * alert message of any type will be displayed during refresh.
     * If type is not specified, it will be "information" by default.
     */
    public static void prepareAlert(HandlerContext handlerCtx, String type, String summary, String detail) {
        Map attrMap = handlerCtx.getFacesContext().getExternalContext().getRequestMap();
        if (isEmpty(type)) {
            attrMap.put("alertType", "information");
        } else if (!(type.equals("information") || type.equals("success") ||
                type.equals("warning") || type.equals("error"))) {
            throw new RuntimeException("GuiUtil:prepareMessage():  type specified is not a valid type");
        } else {
            attrMap.put("alertType", type);
        }
        if (detail != null && detail.length() > 500) {
            detail = detail.substring(0, 500) + "...";
        }
        try {
            attrMap.put("alertDetail", isEmpty(detail) ? "" : URLEncoder.encode(detail, "UTF-8"));
            attrMap.put("alertSummary", isEmpty(summary) ? "" : URLEncoder.encode(summary, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            //we'll never get here.
            ex.printStackTrace();
        }

    }

    public static void handleException(HandlerContext handlerCtx, Throwable ex) {
        prepareException(handlerCtx, ex);
        handlerCtx.getFacesContext().renderResponse();
    }

    public static List<Map> getListOfMaps(Map map) {
        List<Map> list = null;

        if (map != null) {
            list = new ArrayList();
            for (Object key : map.keySet()) {
                HashMap row = new HashMap();
                Object value = map.get(key);
                row.put("name", key);
                row.put("value", value != null ? value : "");
                list.add(row);
            }
        }
        return list;
    }

    public static void handleError(HandlerContext handlerCtx, String detail) {
        prepareAlert(handlerCtx, "error", GuiUtil.getMessage("msg.Error"), detail);
        handlerCtx.getFacesContext().renderResponse();
    }

    /* This method ensure that there will not be a NULL String for the passed in object.
     */
    public static String notNull(String test) {
        return (test == null) ? "" : test;
    }

    public static Throwable getRootCause(final Throwable ex) {
        return ExceptionUtil.getRootCause(ex);
    }

    public static List<String> convertListOfStrings(List l) {
        List<String> arrList = new ArrayList<String>();
        for (Object o : l) {
            arrList.add(o.toString());
        }
        return arrList;
    }
    
    /*
    FIXME: 7-31-08 -- FIX by importing woodstock api's.
    public static Option[] getSunOptions(Collection<String> c) {
    if (c == null){
    return new Option[0];
    }
    Option[] sunOptions =  new Option[c.size()];
    int index=0;
    for(String str:c) {
    sunOptions[index++] = new Option(str, str);
    }
    return sunOptions;
    }
     */
    /**
     * Parses a string containing substrings separated from
     * each other by the specified set of separator characters and returns
     * a list of strings.
     *
     * Splits the string <code>line</code> into individual string elements 
     * separated by the field separators specified in <code>sep</code>, 
     * and returns these individual strings as a list of strings. The 
     * individual string elements are trimmed of leading and trailing
     * whitespace. Only non-empty strings are returned in the list.
     *
     * @param line The string to split
     * @param sep  The list of separators to use for determining where the
     *             string should be split. If null, then the standard
     *             separators (see StringTokenizer javadocs) are used.
     * @return     Returns the list containing the individual strings that
     *             the input string was split into.
     */
    public static List parseStringList(String line, String sep) {
         List tokens = new ArrayList();
        if (line == null) {
            return tokens;
        }

        StringTokenizer st;
        if (sep == null) {
            st = new StringTokenizer(line);
        } else {
            st = new StringTokenizer(line, sep);
        }

        String token;
        while (st.hasMoreTokens()) {
            token = st.nextToken().trim();
            if (token.length() > 0) {
                tokens.add(token);
            }
        }

        return tokens;
    }

    public static String removeToken(String line, String sep, String remove) {
        if (line == null) {
            return null;
        }

        StringTokenizer st;
        if (sep == null) {
            st = new StringTokenizer(line);
        } else {
            sep = sep.trim();
            st = new StringTokenizer(line, sep);
        }
        String token;
        String result = "";
        boolean start = true;
        while (st.hasMoreTokens()) {
            token = st.nextToken().trim();
            if (token.length() > 0 && !(token.equals(remove))) {
                if (start) {
                    result = token;
                    start = false;
                } else {
                    result = result + sep + token;
                }
            }
        }
        return result;
    }

    /**
     *  This method converts a string into stringarray, uses the delimeter as the
     *  separator character. If the delimiter is null, uses space as default.
     */
    public static String[] stringToArray(String str, String delimiter) {
        String[] retString = new String[0];

        if (str != null) {
            if (delimiter == null) {
                delimiter = " ";
            }
            StringTokenizer tokens = new StringTokenizer(str, delimiter);
            retString = new String[tokens.countTokens()];
            int i = 0;
            while (tokens.hasMoreTokens()) {
                String token = tokens.nextToken().trim();
                retString[i++] = token;
            }
        }
        return retString;
    }

    /**
     * This method concatenates the delimiter char to the end of each string
     * in the array, and returns a single string with the concatenated string.
     */
    public static String arrayToString(String[] str, String delimiter) {
        StringBuffer retStr = new StringBuffer();

        if (str != null) {
            for (int i = 0; i < str.length; i++) {
                String element = str[i];

//                if (element == null || element.length() == 0) {
//                    throw new IllegalArgumentException();
//                }
                retStr.append(element);

                if (i < str.length - 1) {
                    retStr.append(delimiter);
                }
            }
        }

        return retStr.toString();
    }

    public static String listToString(List<String> list , String delimiter) {
        StringBuffer retStr = new StringBuffer();
        if(list == null || list.size() <=0 ) return "";
        for(String oneItem : list){
            retStr.append(oneItem);
            retStr.append(delimiter);
        }
        String ret = retStr.toString();
        ret = ret.substring(0, ret.length()-1);
        return ret;
    }

    public static <T> T[] asArray(final Object o)
    {
        return (T[]) Object[].class.cast(o);
    }


    public static boolean isSelected(String name, List<Map> selectedList) {
        if (selectedList == null || name == null) {
            return false;
        }
        for (Map oneRow : selectedList) {
            if (name.equals(oneRow.get("name"))) {
                return true;
            }
        }
        return false;
    }

    public static String checkEmpty(String test) {
        if (test == null) {
            return "";
        }
        return test;
    }

    public static Boolean getBooleanValue(Map pMap, String name) {
        if (pMap.get(name) == null) {
            return Boolean.FALSE;
        }
        Object val = pMap.get(name);
        if (val instanceof Boolean) {
            return (Boolean) val;
        }
        return Boolean.valueOf("" + val);
    }

    public static Habitat getHabitat() {
        ServletContext servletCtx = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        // Get the Habitat from the ServletContext
        Habitat habitat = (Habitat) servletCtx.getAttribute(
                org.glassfish.admingui.common.plugin.ConsoleClassLoader.HABITAT_ATTRIBUTE);
        return habitat;
    }

    public static List<Map<String, Object>> convertArrayToListOfMap(Object[] values, String key) {
        List<Map<String, Object>> list = new ArrayList();
        if (values != null) {
            Map<String, Object> map = null;
            for (Object val : values) {
                map = new HashMap<String, Object>();
                map.put(key, val);
                map.put("selected", false);
                list.add(map);
            }
        }

        return list;
    }
    public static List<Map<String, String>> convertMapToListOfMap(Map<String, String> values) {
        List<Map<String, String>> list = new ArrayList();
        if (values != null) {
            Map<String, Object> map = null;
            for (String key : values.keySet()) {
                HashMap oneRow = new HashMap();
                Object value = values.get(key);
                String valString = (value == null) ? "" : value.toString();
                oneRow.put("selected", false);
                oneRow.put("Name", key);
                oneRow.put("Value", valString);
                oneRow.put("Description", "");
                list.add(oneRow);
            }
        }

        return list;
    }
    
    public static final String I18N_RESOURCE_BUNDLE = "__i18n_resource_bundle";
    public static final String RESOURCE_NAME = "org.glassfish.admingui.core.Strings";
    public static final String LOGGER_NAME = "org.glassfish.admingui";
}
