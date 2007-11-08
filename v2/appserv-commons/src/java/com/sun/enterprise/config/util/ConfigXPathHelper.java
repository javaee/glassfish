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

package com.sun.enterprise.config.util;

//import org.w3c.dom.*;
//import org.xml.sax.*;

//import java.util.logging.Logger;
//import java.util.logging.Level;
//import com.sun.logging.LogDomains;


/** 
 * Helper methods are provided to generate valid xpaths
 * that are needed by config api.
 */
public final class ConfigXPathHelper {
    public static final String XPATH_SEPARATOR = "/";
    
    /**
     * @param childTagName
     * @param nameId
     * @param valueId
     * 
     */    
    public static String getAbsoluteIdXpathExpression(String childTagName, String nameId, String valueId) {
        if(childTagName.startsWith(XPATH_SEPARATOR))
            return childTagName + "[@" + nameId + "='" + valueId + "']";
        else
            return XPATH_SEPARATOR + childTagName + "[@" + nameId + "='" + valueId + "']";
    }

    private static char SEPARATOR_CHAR  = '/';
    private static char OPENBRACKET_CHAR  = '[';
    private static char CLOSEBRACKET_CHAR  = ']';
    private static char ESCAPE_CHAR  = '\\';
  
    /* 
     *  returns element's tag name extracting from its xpath
     */
    public static String getLastNodeName(String xPath) {
        //FIXME: it is supposed that open bracket should be escaped if its part of name or value
        // FIXME no error handling
        
        int idx = xPath.length()-1;
        char ch;
        int idxEnd = -1;
        if(idx>=0 && (ch=xPath.charAt(idx))==CLOSEBRACKET_CHAR) {
            idxEnd = bypassBrackets(xPath, --idx);
            idx = idxEnd;
        }
     
        while(idx>=0 && ((ch=xPath.charAt(idx))!=SEPARATOR_CHAR || isEscapedChar(xPath,idx))) {
                idx--;
        }
        idx++;
        if(idxEnd<=0 || idxEnd==xPath.length()-1)
            return xPath.substring(idx);
        else
            return xPath.substring(idx, idxEnd+1);
    }
    
    /* 
     *  returns parent XPath for  givend child's xpath
     *  correcly bypasses bracketed values with possible escaping inside of quoted values
     */
    public static String getParentXPath(String xPath) {
        //FIXME: it is supposed that open bracket should be escaped if its part of name or value
        // FIXME no error handling
        
        int idx = xPath.length()-1;
        char ch;
        if(idx>=0 && (ch=xPath.charAt(idx))==CLOSEBRACKET_CHAR) {
            idx = bypassBrackets(xPath, --idx);
        }
     
        while(idx>=0 && ((ch=xPath.charAt(idx))!=SEPARATOR_CHAR || isEscapedChar(xPath,idx))) {
                idx--;
        }
        if(idx<=0)
            return "/";
        return xPath.substring(0, idx);
    }
    
    private static int bypassBrackets(String xPath, int idx) {
        char ch;
        while(idx>=0 && ((ch=xPath.charAt(idx))!=OPENBRACKET_CHAR || isEscapedChar(xPath,idx))) {
            idx--;
        }
        return idx-1;
    }
    private static boolean isEscapedChar(String xPath, int idx) {
        if(idx<=0)
            return false;
        int count = 0;
        while(--idx>=0 && xPath.charAt(idx)==ESCAPE_CHAR)
            count++;
        return ((count%2)==1);
    }
}
