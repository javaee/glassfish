/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.admin.rest.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.jvnet.hk2.config.Dom;

import org.glassfish.admin.rest.provider.MethodMetaData;
import org.glassfish.admin.rest.ResourceUtil;
import org.glassfish.admin.rest.RestService;
import org.glassfish.admin.rest.Util;
import org.glassfish.external.statistics.Statistic;

import javax.ws.rs.core.UriInfo;


/**
 *
 * @author Pajeshwar Patil
 * @author Ludovic Champenois ludo@dev.java.net
 */
public class ProviderUtil extends Util {

    /**
     * Produce a string in double quotes with backslash sequences in all the
     * right places.
     */
    static protected final String quote(String string) {
        if (string == null || string.length() == 0) {
            return "\"\"";
        }

        char b;
        char c = 0;
        int i;
        int len = string.length();
        StringBuffer sb = new StringBuffer(len + 4);
        String t;

        sb.append('"');
        for (i = 0; i < len; i += 1) {
            b = c;
            c = string.charAt(i);
            switch (c) {
                case '\\':
                case '"':
                    sb.append('\\');
                    sb.append(c);
                    break;
                case '/':
                    if (b == '<') {
                        sb.append('\\');
                    }
                    sb.append(c);
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                default:
                    if (c < ' ') {
                        t = "000" + Integer.toHexString(c);
                        sb.append("\\u" + t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
        return sb.toString();
    }


    static protected final String readAsString(InputStream in) throws IOException {
        Reader reader = new InputStreamReader(in);
        StringBuilder sb = new StringBuilder();
        char[] c = new char[1024];
        int l;
        while ((l = reader.read(c)) != -1) {
            sb.append(c, 0, l);
        }
        return sb.toString();
    }


    static protected final String getElementLink(UriInfo uriInfo,
        String elementName) {
        String link = uriInfo.getAbsolutePath().toString();
        return link.endsWith("/")?
            (link + elementName):(link + "/" + elementName);
    }


    static protected String getStartXmlElement(String name) {
        assert((name != null) && name.length() > 0);
        String result ="<";
        result = result + name;
        result = result + ">";
        return result;
    }


    static protected String getEndXmlElement(String name) {
        assert((name != null) && name.length() > 0);
        String result ="<";
        result = result + "/";
        result = result + name;
        result = result + ">";
        return result;
    }


    static protected Map getStatistics(Statistic statistic) throws 
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        HashMap results = new HashMap();
        Class classObject = statistic.getClass();
        Method[] methods = 	classObject.getMethods();
        for (Method method: methods) {
             int modifier = method.getModifiers();
             //consider only the public methods
             if (Modifier.isPublic(modifier)) {
                 String name = method.getName();
                 //considier only the get* methods
                 if (name.startsWith("get")) {
                     name = name.substring("get".length());
                     Class<?> returnType = method.getReturnType();
                     //consider only the methods that return primitives or String objects)
                     if (returnType.isPrimitive() || returnType.getName().equals("java.lang.String")) {
                         results.put(name, method.invoke(statistic, null));
                     } else {
                         //control should never reach here
                         //we do not expect statistic object to return object
                         //as value for any of it's stats.
                     }
                 }
             }
        }
        return results;
    }


    static protected String getHtmlRespresentationForAttributes(Dom proxy,
            UriInfo uriInfo) {
        String result = "";
        Set<String> attributes = proxy.model.getAttributeNames();
        for (String attribute : attributes) { //for each attribute
            result = result + "<dt><label for=\"" + attribute + "\">" + attribute + ":&nbsp;" + "</label></dt>";
            result = result + "<dd><input name=\"" + attribute + "\" value =\"" + proxy.attribute(attribute) + "\" type=\"text\"></dd>";
        }

        if (result != "") {
            result = "<div><form action=\"" + uriInfo.getAbsolutePath().toString() + "\" method=\"post\">" +
                "<dl>" + result +
                    "<dt class=\"button\"></dt><dd class=\"button\"><input value=\"Update\" type=\"submit\"></dd>" +
                        "</dl></form></div>";
        }

        return result;
    }


    static protected String getHtmlRespresentationsForCommand(String command,
            String commandMethod, String commandDisplayName, UriInfo uriInfo) {
        return getHtmlRespresentationsForCommand(command, commandMethod,
            commandDisplayName, uriInfo, true);
    }


    static protected String getHtmlRespresentationsForCommand(String command,
            String commandMethod, String commandDisplayName, UriInfo uriInfo,
                boolean displayId) {
        String result ="";
        if ((command != null) && (command != "")) {
            ResourceUtil resourceUtil = new ResourceUtil();
            MethodMetaData methodMetaData = resourceUtil.getMethodMetaData(
            command, RestService.getHabitat(), RestService.logger);
            Set<String> parameters = methodMetaData.parameters();
            Iterator<String> iterator = parameters.iterator();
            String parameter;
            ParameterMetaData parameterMetaData;
            while (iterator.hasNext()) {
                parameter = iterator.next();
                if (!(parameter.equals("id") && (!displayId))) { //do not display id in case of command resources. displayId = false for command resources.
                     if ((!commandMethod.equals("delete")) ||                                //in case of delete operation(command),
                        ((commandMethod.equals("delete")) && (!parameter.equals("id")))) {  //do not  display/provide id attribute.
                        parameterMetaData = methodMetaData.getParameterMetaData(parameter);
                        result = result +
                            getHtmlRespresentationForParameter(parameter, parameterMetaData);
                    }
                }
            }

            //Fix to diplay component for commands with 0 arguments.
            //For example, rotate-log or restart.
            if (result.equals("")) {
                result = " ";
            }

        }

        if (result != "") {
            result = "<div><form action=\"" + uriInfo.getAbsolutePath().toString() +
                "\" method=\"" + /*commandMethod*/"post" + "\">" +  //hack-1 : support delete method for html
                "<dl>" + result;                       //hardcode "post" instead of commandMethod which chould be post or delete.

            //hack-1 : support delete method for html
            //add hidden field
            if(commandMethod.equals("delete")) {
                result = result +
                    "<input name=\"operation\" value=\"__deleteoperation\" type=\"hidden\">";
            }

            result = result + "<dt class=\"button\"></dt><dd class=\"button\"><input value=\"" + commandDisplayName + "\" type=\"submit\"></dd>";
            result = result + "</dl></form></div>";
        }

        return result;
    }


    static protected String getHtmlForComponent(String component, String heading,
            String result) {
        if ((component != null) && (component.length() > 0)) {
            result = result + "<h2>" + heading + "</h2>";
            result = result + component;
            result = result + "<hr class=\"separator\"/>";
        }
        return result;
    }


    static public String getHtmlHeader() {
        String result = "<html><head>";
        result = result + "<title>GlassFish REST Interface</title>";
        result = result + getInternalStyleSheet();
        //FIXME - uncomment with the correct link for css file. This external file will override the internal style sheet.
        ///result = result + "<link rel=\"stylesheet\" type=\"text/css\" href=\"http://localhost:8080/glassfish_rest_interface.css\" />";
        result = result + "</head><body>";
        result = result + "<h1 class=\"mainheader\">GlassFish REST Interface</h1>";
        result = result + "<hr>";
        return result;
    }


    static protected String getJsonForMethodMetaData(OptionsResult metaData, String indent) {
        OptionsResultJsonProvider provider = new OptionsResultJsonProvider();
        return provider.getRespresenationForMethodMetaData(metaData, indent);
    }


    static protected String getXmlForMethodMetaData(OptionsResult metaData, String indent) {
        OptionsResultXmlProvider provider = new OptionsResultXmlProvider();
        return provider.getRespresenationForMethodMetaData(metaData, indent);
    }


    static protected String getResourcesKey() {
        return "Child Resources";
    }


    static protected String getResourceKey() {
        return "Child Resource";
    }


    static protected String getMethodsKey() {
        return "Methods";
    }


    static private String getHtmlRespresentationForParameter(String parameter,
            ParameterMetaData parameterMetaData) {
        String result = parameter;
        
        //indicate mandatory field with * super-script
        if (parameterMetaData.getAttributeValue("Optional").equalsIgnoreCase("false")) {
            result = result + "<sup>*</sup>";
        }

        result = "<dt><label for=\"" + parameter + "\">" + result + ":&nbsp;" + "</label></dt>";

        boolean isBoolean = false;
        if(parameterMetaData.getAttributeValue("Type").endsWith("java.lang.Boolean")) {
            isBoolean = true;
        }

        boolean hasAcceptableValues = false;
        String acceptableValues = parameterMetaData.getAttributeValue("Acceptable Values");
        if ((acceptableValues != null) && (acceptableValues.length() > 0)) {
            hasAcceptableValues = true;
        }

        String defaultValue = parameterMetaData.getAttributeValue("Default Value");
        boolean hasDefaultValue = false;
        if ((defaultValue != null) && (defaultValue.length() > 0)) {
            hasDefaultValue = true;
        }

        if (isBoolean || hasAcceptableValues) {
            //use combo box
            result = result + "<dd><select name=" + parameter + ">";
            String[] values;
            if (isBoolean) {
                values = new String[] {"true", "false"};
            } else {
                values = stringToArray(acceptableValues, ",");
            }

            for (String value : values) {
                if ((hasDefaultValue) && (value.equalsIgnoreCase(defaultValue))){
                    if (isBoolean) { defaultValue = defaultValue.toLowerCase();} //boolean options are all displayed as lowercase
                    result = result + "<option selected>" + defaultValue + "<br>";
                } else {
                    result = result + "<option>" + value + "<br>";
                }
            }
            result = result + "</select></dd>";
        } else {
            //use text box
            if (hasDefaultValue) {
                result = result + "<dd><input name=\"" + parameter + "\" value =\"" +
                    defaultValue + "\" type=\"text\"></dd>";
            } else {
                result = result + "<dd><input name=\"" + parameter + "\" type=\"text\"></dd>";
            }
        }

        return result;
    }


    /**
     *  This method converts a string into stringarray, uses the delimeter as the
     *  separator character. If the delimiter is null, uses space as default.
     */
    private static String[] stringToArray(String str, String delimiter) {
        String[] retString = new String[0];

        if (str != null) {
            if(delimiter == null) {
                delimiter = " ";
            }
            StringTokenizer tokens = new StringTokenizer(str, delimiter);
            retString = new String[tokens.countTokens()];
            int i = 0;
            while(tokens.hasMoreTokens()) {
                retString[i++] = tokens.nextToken();
            }
        }
        return retString;
    }


    private static String getInternalStyleSheet() {
        String result = "<style type=\"text/css\">";
        result = result + "body {";
        result = result + "font-size:75%;font-family:verdana,arial,'sans serif';";
        result = result + "background-repeat:repeat-x;background-color:#F0F0F0;";
        result = result + "color:#303030;";
        result = result + "margin:60px;margin-top:20px;margin-bottom:20px;margin-right:60px;margin-left:60px;";
        result = result + "}";
        result = result + "h1 {font-size:200%;background-color:#E0E0E0}";
        result = result + "h2 {font-size:140%;background-color:#E8E8E8}";
        result = result + "h3 {font-size:110%;background-color:#E8E8E8}";
        result = result + "h1.mainheader {color:#101010;font-size:200%;background-color:#D8D8D8;text-align:center}";
        result = result + "a:link {color:#000080;}";
        result = result + "a:hover {color:red;}";
        result = result + "input[type=\"text\"] {background-color:#F8F8F8;border-style:inset;width:350px}";
        result = result + "dl {position: relative;width:500px}";
        result = result + "dt {clear: both;float:left;width: 210px;padding: 4px 0 2px 0;text-align:left}";
        result = result + "dd {float: left;width: 200px;margin: 0 0 8px 0;padding-left: 6px;}";
        result = result + ".separator{clear:both}";
        result = result + "</style>";
        return result;
    }
}
