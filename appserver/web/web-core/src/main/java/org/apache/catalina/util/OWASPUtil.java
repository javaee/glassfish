/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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


package org.apache.catalina.util;

import java.util.regex.Pattern;

/*
Util class for static methods for handling encoding of invalid string characters. Use recommendations from Open Web Application Security Project (see here
http://www.owasp.org/index.php/Log_Forging)
 */
public final class OWASPUtil {
    public static final String HEADER_VALUE_VALIDATION_PATERN = "^[a-zA-Z0-9()\\-=\\*\\.\\?;,+\\/:&_ ]*$";

    private OWASPUtil() {
    }

    public static String neutralizeForLog(String message){
        message = message.replace( '\n' ,  '_' ).replace( '\r' , '_' )
                .replace( '\t' , '_' );
        message = encodeForHtml(message);
        return message;
    }

    public static String encodeForHtml(String message){
        StringBuilder escaped = new StringBuilder();
        for(char ch:message.toCharArray()){
            switch (ch) {
                case '<':escaped.append("&lt;");break;
                case '>':escaped.append("&gt;");break;
                case '&':escaped.append("&amp;");break;
                case '"':escaped.append("&quot;");break;
                case '\'':escaped.append("&#x27;");break;
                case '/':escaped.append("&#x2F;");break;
                default:escaped.append(ch);
            }
        }
        return escaped.toString();
    }
    public static String removeLinearWhiteSpaces(String input) {
        if (input != null) {
                input = Pattern.compile("//s").matcher(input).replaceAll(" ");
            }
        return input;
    }
    public static String getSafeHeaderValue(String headerValue) throws Exception {
        headerValue = removeLinearWhiteSpaces(headerValue);
        if (headerValue != null) {
                if (!Pattern.compile(HEADER_VALUE_VALIDATION_PATERN).matcher(headerValue).matches()) {
                        throw new Exception("Header Value invalid characters");
                    }
            }
        return headerValue;
    }

}
