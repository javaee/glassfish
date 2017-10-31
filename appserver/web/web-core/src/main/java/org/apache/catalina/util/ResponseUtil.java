/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package org.apache.catalina.util;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.regex.Pattern;

public final class ResponseUtil {

    public static final String CRLF_ENCODED_STRING = "%0d%0a";
    public static final String CR_ENCODED_STRING = "%0d";

    /**
     * Copies the contents of the specified input stream to the specified
     * output stream.
     *
     * @param istream The input stream to read from
     * @param ostream The output stream to write to
     *
     * @return Exception that occurred during processing, or null
     */
    public static IOException copy(InputStream istream,
                                   ServletOutputStream ostream) {

        IOException exception = null;
        byte buffer[] = new byte[2048];
        int len;
        while (true) {
            try {
                len = istream.read(buffer);
                if (len == -1)
                    break;
                ostream.write(buffer, 0, len);
            } catch (IOException e) {
                exception = e;
                len = -1;
                break;
            }
        }
        return exception;

    }


    /**
     * Copies the contents of the specified input stream to the specified
     * output stream.
     *
     * @param reader The reader to read from
     * @param writer The writer to write to
     *
     * @return Exception that occurred during processing, or null
     */
    public static IOException copy(Reader reader, PrintWriter writer) {

        IOException exception = null;
        char buffer[] = new char[2048];
        int len;
        while (true) {
            try {
                len = reader.read(buffer);
                if (len == -1)
                    break;
                writer.write(buffer, 0, len);
            } catch (IOException e) {
                exception = e;
                len = -1;
                break;
            }
        }
        return exception;

    }

    /**
     * Validate the Redirect URL for Header Injection Attack.
     *
     * @param redirectURL	Redirect URL to be validate
     * @return		boolean
     */
    public static boolean validateRedirectURL(String redirectURL) {
        return (!(redirectURL.contains(CRLF_ENCODED_STRING) || redirectURL.contains(CR_ENCODED_STRING)));
    }

    /**
     * Remove unwanted white spaces in the URL.
     *
     * @param input	String to be stripped with whitespaces
     * @return		String
     */
    public static String removeLinearWhiteSpaces(String input) {
        if (input != null) {
            input = Pattern.compile("//s").matcher(input).replaceAll(" ");
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
            if (headerName != null) {
                if (headerName.contains(CRLF_ENCODED_STRING) || headerName.contains(CR_ENCODED_STRING)) {
                    throw new Exception("Header Name invalid characters");
                }
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
        if (headerValue != null) {
                if (headerValue.contains(CRLF_ENCODED_STRING) || headerValue.contains(CR_ENCODED_STRING)) {
                        throw new Exception("Header Value invalid characters");
                    }
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
        if (headerValue != null) {
            if (headerValue.contains(CRLF_ENCODED_STRING) || headerValue.contains(CR_ENCODED_STRING)) {
                throw new Exception (" Cookie Header Value has invalid characters");
            }
        }
        return headerValue;
    }

}
