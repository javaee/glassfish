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


package org.glassfish.common.util;

import java.util.regex.Pattern;

/*
Util class for static methods for handling encoding of invalid string characters.
Use recommendations from Open Web Application Security Project (see here
http://www.owasp.org/index.php/)
 */
public class InputValidationUtil {

    public static final String CRLF_ENCODED_STRING_LOWER = "%0d%0a";
    public static final String CRLF_ENCODED_STRING_UPPER = "%0D%0A";
    public static final String CR_ENCODED_STRING_LOWER = "%0d";
    public static final String CR_ENCODED_STRING_UPPER = "%0D";
    public static final String CRLF_STRING = "\"\\r\\n\"";

    /**
     Validate the String for Header Injection Attack.

     @param input	String to be validate
     @return		boolean
     */
    public static boolean validateStringforCRLF (String input) {
        if (input != null && (input.contains(CRLF_ENCODED_STRING_LOWER)
                || input.contains(CRLF_ENCODED_STRING_UPPER)
                || input.contains(CR_ENCODED_STRING_UPPER)
                || input.contains(CR_ENCODED_STRING_LOWER)
                || input.contains(CRLF_STRING))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Remove unwanted white spaces in the URL.
     *
     * @param input	String to be stripped with whitespaces
     * @return		String
     */
    public static String removeLinearWhiteSpaces(String input) {
        if (input != null) {
            input = Pattern.compile("\\s").matcher(input).replaceAll(" ");
        }
        return input;
    }

    /**
     * Return Http Header Name after suitable validation
     *
     * @param headerName Header Name which should be validated before being set
     * @return String Header Name sanitized for CRLF attack
     */
    public static String getSafeHeaderName(String headerName) throws Exception {
        headerName = removeLinearWhiteSpaces(headerName);
        if (validateStringforCRLF(headerName)) {
            throw new Exception("Header Name invalid characters");
        }
        return headerName;
    }

    /**
     * Return Http Header Value after suitable validation
     *
     * @param headerValue Header Value which should be validated before being set
     * @return String Header Value sanitized for CRLF attack
     */
    public static String getSafeHeaderValue(String headerValue) throws Exception {
        headerValue = removeLinearWhiteSpaces(headerValue);
        if (validateStringforCRLF(headerValue)) {
            throw new Exception("Header Value invalid characters");
        }
        return headerValue;
    }

    /**
     * Return Cookie Http Header Value after suitable validation
     *
     * @param headerValue Header Value which should be validated before being set
     * @return String Header Value sanitized for CRLF attack
     */
    public static String getSafeCookieHeaderValue(String headerValue) throws Exception {
        headerValue = removeLinearWhiteSpaces(headerValue);
        if (validateStringforCRLF(headerValue)) {
            throw new Exception (" Cookie Header Value has invalid characters");
        }
        return headerValue;
    }
}
