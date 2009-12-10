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
package org.glassfish.admin.rest;

import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.admin.rest.provider.ProviderUtil;
import javax.ws.rs.core.UriInfo;


/**
 * Utilities class. Extended by ResourceUtil and ProviderUtil utilities. Used by
 * resource and providers.
 *
 * @author Rajeshwar Patil
 */
public class Util {

    /**
     * Returns name of the resource from UriInfo.
     */
    public String getResourceName(UriInfo uriInfo) {
        return upperCaseFirstLetter(
            eleminateHypen(getName(uriInfo.getPath(), '/')));
    }


    /**
     * Returns name of the resource parent from UriInfo.
     */
    public String getParentName(UriInfo uriInfo) {
        if (uriInfo == null) return null;
        return getParentName(uriInfo.getPath());
    }


    /**
     * Returns just the name of the given fully qualified name.
     */
    public String getName(String typeName) {
        return getName(typeName, '.');
    }


    /**
     * Returns just the name of the given fully qualified name.
     */
    public String getName(String typeName, char delimiter) {
        if ((typeName == null) || ("".equals(typeName))) return typeName;

        //elimiate last char from typeName if its a delimiter
        if (typeName.length() - 1 == typeName.lastIndexOf(delimiter))
            typeName = typeName.substring(0, typeName.length()-1);

        if ((typeName != null) && (typeName.length() > 0)) {
            int index = typeName.lastIndexOf(delimiter);
            if (index != -1) {
                return typeName.substring(index + 1);
            }
        }
        return typeName;
    }


    /**
     * returns just the parent name of the resource from the resource url.
     */
    public String getParentName(String url) {
        if ((url == null) || ("".equals(url))) return url;
        String name = getName(url, '/');
        int nameIndex = url.indexOf(name);
        return getName(url.substring(0, nameIndex-1), '/');
    }


    /**
     * Removes any hypens ( - ) from the given string.
     * When it removes a hypen, it converts next immidiate
     * character, if any,  to an Uppercase.(schema2beans convention)
     * @param string the input string
     * @return a <code>String</code> resulted after removing the hypens
     */
    public String eleminateHypen(String string){
        if(!(string == null || string.length() <= 0)){
            int index = string.indexOf('-');
            while(index != -1){
                if(index == 0){
                    string = string.substring(1);
                } else {
                    if(index == (string.length() - 1)){
                        string = string.substring(0,string.length()-1);
                    } else {
                        string = string.substring(0,index) +
                            upperCaseFirstLetter(string.substring(index + 1));
                    }
                }
                index = string.indexOf('-');
            }
        }
        return string;
    }


    /**
    * Converts the first letter of the given string to Uppercase.
    *
    * @param string the input string
    * @return the string with the Uppercase first letter
    */
    public String upperCaseFirstLetter(String string)
    {
        if(string == null || string.length() <= 0){
            return string;
        }
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }


    /**
    * Returns the html for the given message.
    *
    * @param uriInfo the uriInfo context of the request
    * @return String the html representation of the given message
    */
    public String getHtml(String message, UriInfo uriInfo) {
        String result = ProviderUtil.getHtmlHeader();
        String uri = uriInfo.getAbsolutePath().toString();
        String name = upperCaseFirstLetter(eleminateHypen(getName(uri, '/')));
        String parentName =
            upperCaseFirstLetter(eleminateHypen(getParentName(uri)));

        result = result + "<h1>" + name + "</h1>";
        result = result + message + "<br><br>";
        result = result + "<a href=\"" + uri + "\">Back</a><br>";

        result = "<div>" + result + "</div>" + "<br>";
        result = result + "</body></html>";
        return result;
    }


    /**
     * Constructs a method name from  element's dtd name
     * name for a given prefix.(schema2beans convention)
     *
     * @param elementName the given element name
     * @param prefix the given prefix
     * @return a method name formed from the given name and the prefix
     */
    public String methodNameFromDtdName(String elementName, String prefix){
        return methodNameFromBeanName(eleminateHypen(elementName), prefix);
    }


    /**
     * Constructs a method name from  element's bean
     * name for a given prefix.(schema2beans convention)
     *
     * @param elementName the given element name
     * @param prefix the given prefix
     * @return a method name formed from the given name and the prefix
     */
    public String methodNameFromBeanName(String elementName, String prefix){
        if((null == elementName) || (null == prefix) ||
                (prefix.length() <= 0 )){
            return elementName;
        }
        String methodName = upperCaseFirstLetter(elementName);
        return methodName = prefix + methodName;
    }

    protected final static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(Util.class);
}