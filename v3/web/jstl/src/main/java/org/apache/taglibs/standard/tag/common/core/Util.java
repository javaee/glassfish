/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.taglibs.standard.tag.common.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.taglibs.standard.resources.Resources;

/**
 * <p>Utilities in support of tag-handler classes.</p>
 *
 * @author Jan Luehe
 */
public class Util {

    private static final String REQUEST = "request";   
    private static final String SESSION = "session";   
    private static final String APPLICATION = "application"; 
    private static final String DEFAULT = "default";
    private static final String SHORT = "short";
    private static final String MEDIUM = "medium";
    private static final String LONG = "long";
    private static final String FULL = "full";

    public static final int HIGHEST_SPECIAL = '>';
    public static char[][] specialCharactersRepresentation = new char[HIGHEST_SPECIAL + 1][];
    static {
        specialCharactersRepresentation['&'] = "&amp;".toCharArray();
        specialCharactersRepresentation['<'] = "&lt;".toCharArray();
        specialCharactersRepresentation['>'] = "&gt;".toCharArray();
        specialCharactersRepresentation['"'] = "&#034;".toCharArray();
        specialCharactersRepresentation['\''] = "&#039;".toCharArray();
    }

    /*
     * Converts the given string description of a scope to the corresponding
     * PageContext constant.
     *
     * The validity of the given scope has already been checked by the
     * appropriate TLV.
     *
     * @param scope String description of scope
     *
     * @return PageContext constant corresponding to given scope description
     */
    public static int getScope(String scope) {
	int ret = PageContext.PAGE_SCOPE; // default

	if (REQUEST.equalsIgnoreCase(scope))
	    ret = PageContext.REQUEST_SCOPE;
	else if (SESSION.equalsIgnoreCase(scope))
	    ret = PageContext.SESSION_SCOPE;
	else if (APPLICATION.equalsIgnoreCase(scope))
	    ret = PageContext.APPLICATION_SCOPE;

	return ret;
    }

    /*
     * Converts the given string description of a formatting style for
     * dates and times to the corresponding java.util.DateFormat constant.
     *
     * @param style String description of formatting style for dates and times
     * @param errCode Error code to throw if given style is invalid
     *
     * @return java.util.DateFormat constant corresponding to given style
     *
     * @throws JspException if the given style is invalid
     */
    public static int getStyle(String style, String errCode)
	                throws JspException {
	int ret = DateFormat.DEFAULT;

	if (style != null) {
	    if (DEFAULT.equalsIgnoreCase(style)) {
		ret = DateFormat.DEFAULT;
	    } else if (SHORT.equalsIgnoreCase(style)) {
		ret = DateFormat.SHORT;
	    } else if (MEDIUM.equalsIgnoreCase(style)) {
		ret = DateFormat.MEDIUM;
	    } else if (LONG.equalsIgnoreCase(style)) {
		ret = DateFormat.LONG;
	    } else if (FULL.equalsIgnoreCase(style)) {
		ret = DateFormat.FULL;
	    } else {
		throw new JspException(Resources.getMessage(errCode, style));
	    }
	}

	return ret;
    }
    


    /**
     * Performs the following substring replacements
     * (to facilitate output to XML/HTML pages):
     *
     *    & -> &amp;
     *    < -> &lt;
     *    > -> &gt;
     *    " -> &#034;
     *    ' -> &#039;
     *
     * See also OutSupport.writeEscapedXml().
     */
    public static String escapeXml(String buffer) {
        int start = 0;
        int length = buffer.length();
        char[] arrayBuffer = buffer.toCharArray();
        StringBuffer escapedBuffer = null;

        for (int i = 0; i < length; i++) {
            char c = arrayBuffer[i];
            if (c <= HIGHEST_SPECIAL) {
                char[] escaped = specialCharactersRepresentation[c];
                if (escaped != null) {
                    // create StringBuffer to hold escaped xml string
                    if (start == 0) {
                        escapedBuffer = new StringBuffer(length + 5);
                    }
                    // add unescaped portion
                    if (start < i) {
                        escapedBuffer.append(arrayBuffer,start,i-start);
                    }
                    start = i + 1;
                    // add escaped xml
                    escapedBuffer.append(escaped);
                }
            }
        }
        // no xml escaping was necessary
        if (start == 0) {
            return buffer;
        }
        // add rest of unescaped portion
        if (start < length) {
            escapedBuffer.append(arrayBuffer,start,length-start);
        }
        return escapedBuffer.toString();
    }

    /**
     * Get the value associated with a content-type attribute.
     * Syntax defined in RFC 2045, section 5.1.
     */
    public static String getContentTypeAttribute(String input, String name) {
	int begin;
	int end;
        int index = input.toUpperCase().indexOf(name.toUpperCase());
        if (index == -1) return null;
        index = index + name.length(); // positioned after the attribute name
        index = input.indexOf('=', index); // positioned at the '='
        if (index == -1) return null;
        index += 1; // positioned after the '='
        input = input.substring(index).trim();
        
        if (input.charAt(0) == '"') {
            // attribute value is a quoted string
            begin = 1;
            end = input.indexOf('"', begin);
            if (end == -1) return null;
        } else {
            begin = 0;
            end = input.indexOf(';');
            if (end == -1) end = input.indexOf(' ');
            if (end == -1) end = input.length();
        }
        return input.substring(begin, end).trim();
    }
    
    /**
     * URL encodes a string, based on the supplied character encoding.
     * This performs the same function as java.next.URLEncode.encode
     * in J2SDK1.4, and should be removed if the only platform supported
     * is 1.4 or higher.
     * @param s The String to be URL encoded.
     * @param enc The character encoding 
     * @return The URL encoded String
     * [taken from jakarta-tomcat-jasper/jasper2
     *  org.apache.jasper.runtime.JspRuntimeLibrary.java]
     */
    public static String URLEncode(String s, String enc) {

	if (s == null) {
	    return "null";
	}

	if (enc == null) {
	    enc = "UTF-8";	// Is this right?
	}

	StringBuffer out = new StringBuffer(s.length());
	ByteArrayOutputStream buf = new ByteArrayOutputStream();
	OutputStreamWriter writer = null;
	try {
	    writer = new OutputStreamWriter(buf, enc);
	} catch (java.io.UnsupportedEncodingException ex) {
	    // Use the default encoding?
	    writer = new OutputStreamWriter(buf);
	}
	
	for (int i = 0; i < s.length(); i++) {
	    int c = s.charAt(i);
	    if (c == ' ') {
		out.append('+');
	    } else if (isSafeChar(c)) {
		out.append((char)c);
	    } else {
		// convert to external encoding before hex conversion
		try {
		    writer.write(c);
		    writer.flush();
		} catch(IOException e) {
		    buf.reset();
		    continue;
		}
		byte[] ba = buf.toByteArray();
		for (int j = 0; j < ba.length; j++) {
		    out.append('%');
		    // Converting each byte in the buffer
		    out.append(Character.forDigit((ba[j]>>4) & 0xf, 16));
		    out.append(Character.forDigit(ba[j] & 0xf, 16));
		}
		buf.reset();
	    }
	}
	return out.toString();
    }

    private static boolean isSafeChar(int c) {
	if (c >= 'a' && c <= 'z') {
	    return true;
	}
	if (c >= 'A' && c <= 'Z') {
	    return true;
	}
	if (c >= '0' && c <= '9') {
	    return true;
	}
	if (c == '-' || c == '_' || c == '.' || c == '!' ||
	    c == '~' || c == '*' || c == '\'' || c == '(' || c == ')') {
	    return true;
	}
	return false;
    }    
    
    /**
     * HttpServletRequest.getLocales() returns the server's default locale 
     * if the request did not specify a preferred language.
     * We do not want this behavior, because it prevents us from using
     * the fallback locale. 
     * We therefore need to return an empty Enumeration if no preferred 
     * locale has been specified. This way, the logic for the fallback 
     * locale will be able to kick in.
     */
    public static Enumeration getRequestLocales(HttpServletRequest request) {        
        Enumeration values = request.getHeaders("accept-language");
        if (values.hasMoreElements()) {
            // At least one "accept-language". Simply return
            // the enumeration returned by request.getLocales().
            // System.out.println("At least one accept-language");
            return request.getLocales();
        } else {
            // No header for "accept-language". Simply return
            // the empty enumeration.
            // System.out.println("No accept-language");
            return values;
        }
    }
}
