
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
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

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
import java.util.Vector;
import java.util.logging.Logger;
import java.text.MessageFormat;
import java.net.URLEncoder;

import java.io.UnsupportedEncodingException;
import com.sun.appserv.management.util.misc.ExceptionUtil;
import javax.servlet.ServletContext;
import org.glassfish.deployment.client.DeploymentFacility;
import org.glassfish.deployment.client.ServerConnectionIdentifier;
import org.jvnet.hk2.component.Habitat;

/**
 *
 * @author anilam
 */
public class GuiUtil {

    /** Creates a new instance of GuiUtil */
    public GuiUtil() {
    }

    public Logger getLogger() {
        return Logger.getLogger("org.glassfish.admingui");
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

    public static void setSessionValue(String key, Object value) {
        Map sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        sessionMap.put(key, value);
    }

    public static Object getSessionValue(String key) {
        Map sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        return sessionMap.get(key);
    }

    public static DeploymentFacility getDeploymentFacility() {
        DeploymentFacility df = null; //(DeploymentFacility) getSessionValue("_DEPLOYMENT_FACILITY");
        boolean enable = false;
        if (df == null) {
            //df= DeploymentFacilityFactory.getDeploymentFacility();
            df = new LocalDeploymentFacility();
            ServerConnectionIdentifier sci = new ServerConnectionIdentifier(
                    (String) getSessionValue("serverName"),
                    ((Integer) getSessionValue("serverPort")).intValue(),
                    (String) getSessionValue("userName"), //user name
                    "", //password    FIXME: how to get password ?
                    (Boolean) getSessionValue("requestIsSecured") //security enabled
                    );
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
        Locale locale = com.sun.jsftemplating.util.Util.getLocale(FacesContext.getCurrentInstance());
        ResourceBundle bundle = ResourceBundleManager.getInstance().getBundle(resourceName, locale);
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
        if (line == null) {
            return null;
        }

        StringTokenizer st;
        if (sep == null) {
            st = new StringTokenizer(line);
        } else {
            st = new StringTokenizer(line, sep);
        }

        String token;

        List tokens = new Vector();
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

                if (element == null || element.length() == 0) {
                    throw new IllegalArgumentException();
                }
                retStr.append(element);

                if (i < str.length - 1) {
                    retStr.append(delimiter);
                }
            }
        }

        return retStr.toString();
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
    public static final String I18N_RESOURCE_BUNDLE = "__i18n_resource_bundle";
    public static final String RESOURCE_NAME = "org.glassfish.admingui.core.Strings";
}
