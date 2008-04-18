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



package org.apache.tomcat.util.http;

import org.apache.tomcat.util.res.StringManager;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.text.*;

/**
 * Handle (internationalized) HTTP messages.
 * 
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author Harish Prabandham
 * @author costin@eng.sun.com
 */
public class HttpMessages {

    private static final String STATUS_100 = "Continue";
    private static final String STATUS_101 = "Switching Protocols";
    private static final String STATUS_200 = "OK";
    private static final String STATUS_201 = "Created";
    private static final String STATUS_202 = "Accepted";
    private static final String STATUS_203 = "Non-Authoritative Information";
    private static final String STATUS_204 = "No Content";
    private static final String STATUS_205 = "Reset Content";
    private static final String STATUS_206 = "Partial Content";
    private static final String STATUS_207 = "Multi-Status";
    private static final String STATUS_300 = "Multiple Choices";
    private static final String STATUS_301 = "Moved Permanently";
    private static final String STATUS_302 = "Moved Temporarily";
    private static final String STATUS_303 = "See Other";
    private static final String STATUS_304 = "Not Modified";
    private static final String STATUS_305 = "Use Proxy";
    private static final String STATUS_307 = "Temporary Redirect";
    private static final String STATUS_400 = "Bad Request";
    private static final String STATUS_401 = "Unauthorized";
    private static final String STATUS_402 = "Payment Required";
    private static final String STATUS_403 = "Forbidden";
    private static final String STATUS_404 = "Not Found";
    private static final String STATUS_405 = "Method Not Allowed";
    private static final String STATUS_406 = "Not Acceptable";
    private static final String STATUS_407 = "Proxy Authentication Required";
    private static final String STATUS_408 = "Request Timeout";
    private static final String STATUS_409 = "Conflict";
    private static final String STATUS_410 = "Gone";
    private static final String STATUS_411 = "Length Required";
    private static final String STATUS_412 = "Precondition Failed";
    private static final String STATUS_413 = "Request Entity Too Large";
    private static final String STATUS_414 = "Request-URI Too Long";
    private static final String STATUS_415 = "Unsupported Media Type";
    private static final String STATUS_416 = "Requested Range Not Satisfiable";
    private static final String STATUS_417 = "Expectation Failed";
    private static final String STATUS_422 = "Unprocessable Entity";
    private static final String STATUS_423 = "Locked";
    private static final String STATUS_424 = "Failed Dependency";
    private static final String STATUS_500 = "Internal Server Error";
    private static final String STATUS_501 = "Not Implemented";
    private static final String STATUS_502 = "Bad Gateway";
    private static final String STATUS_503 = "Service Unavailable";
    private static final String STATUS_504 = "Gateway Timeout";
    private static final String STATUS_505 = "HTTP Version Not Supported";
    private static final String STATUS_507 = "Insufficient Storage";

    private static final ConcurrentHashMap<String, String> httpStatusCodeMappings =
        new ConcurrentHashMap<String, String>();

    static {
        httpStatusCodeMappings.put("sc.100", STATUS_100);
        httpStatusCodeMappings.put("sc.101", STATUS_101);
        httpStatusCodeMappings.put("sc.200", STATUS_200);
        httpStatusCodeMappings.put("sc.201", STATUS_201);
        httpStatusCodeMappings.put("sc.202", STATUS_202);
        httpStatusCodeMappings.put("sc.203", STATUS_203);
        httpStatusCodeMappings.put("sc.204", STATUS_204);
        httpStatusCodeMappings.put("sc.205", STATUS_205);
        httpStatusCodeMappings.put("sc.206", STATUS_206);
        httpStatusCodeMappings.put("sc.207", STATUS_207);
        httpStatusCodeMappings.put("sc.300", STATUS_300);
        httpStatusCodeMappings.put("sc.301", STATUS_301);
        httpStatusCodeMappings.put("sc.302", STATUS_302);
        httpStatusCodeMappings.put("sc.303", STATUS_303);
        httpStatusCodeMappings.put("sc.304", STATUS_304);
        httpStatusCodeMappings.put("sc.305", STATUS_305);
        httpStatusCodeMappings.put("sc.307", STATUS_307);
        httpStatusCodeMappings.put("sc.400", STATUS_400);
        httpStatusCodeMappings.put("sc.401", STATUS_401);
        httpStatusCodeMappings.put("sc.402", STATUS_402);
        httpStatusCodeMappings.put("sc.403", STATUS_403);
        httpStatusCodeMappings.put("sc.404", STATUS_404);
        httpStatusCodeMappings.put("sc.405", STATUS_405);
        httpStatusCodeMappings.put("sc.406", STATUS_406);
        httpStatusCodeMappings.put("sc.407", STATUS_407);
        httpStatusCodeMappings.put("sc.408", STATUS_408);
        httpStatusCodeMappings.put("sc.409", STATUS_409);
        httpStatusCodeMappings.put("sc.410", STATUS_410);
        httpStatusCodeMappings.put("sc.411", STATUS_411);
        httpStatusCodeMappings.put("sc.412", STATUS_412);
        httpStatusCodeMappings.put("sc.413", STATUS_413);
        httpStatusCodeMappings.put("sc.414", STATUS_414);
        httpStatusCodeMappings.put("sc.415", STATUS_415);
        httpStatusCodeMappings.put("sc.416", STATUS_416);
        httpStatusCodeMappings.put("sc.417", STATUS_417);
        httpStatusCodeMappings.put("sc.422", STATUS_422);
        httpStatusCodeMappings.put("sc.423", STATUS_423);
        httpStatusCodeMappings.put("sc.424", STATUS_424);
        httpStatusCodeMappings.put("sc.500", STATUS_500);
        httpStatusCodeMappings.put("sc.501", STATUS_501);
        httpStatusCodeMappings.put("sc.502", STATUS_502);
        httpStatusCodeMappings.put("sc.503", STATUS_503);
        httpStatusCodeMappings.put("sc.504", STATUS_504);
        httpStatusCodeMappings.put("sc.505", STATUS_505);
        httpStatusCodeMappings.put("sc.507", STATUS_507);
    }
    

    /** Get the status string associated with a status code.
     *  No I18N - return the messages defined in the HTTP spec.
     *  ( the user isn't supposed to see them, this is the last
     *  thing to translate)
     *
     *  Common messages are cached.
     */
    public static String getMessage(int status) {

        // Return the status message for the most frequently used status
        // codes directly, without any lookup
        switch (status) {
            case 200: return STATUS_200;
            case 302: return STATUS_302;
            case 400: return STATUS_400;
            case 404: return STATUS_404;
	}

	return httpStatusCodeMappings.get("sc."+ status);
    }

    /**
     * Filter the specified message string for characters that are sensitive
     * in HTML.  This avoids potential attacks caused by including JavaScript
     * codes in the request URL that is often reported in error messages.
     *
     * @param message The message string to be filtered
     */
    public static String filter(String message) {

	if (message == null)
	    return (null);

	char content[] = new char[message.length()];
	message.getChars(0, message.length(), content, 0);
	StringBuffer result = new StringBuffer(content.length + 50);
	for (int i = 0; i < content.length; i++) {
	    switch (content[i]) {
	    case '<':
		result.append("&lt;");
		break;
	    case '>':
		result.append("&gt;");
		break;
	    case '&':
		result.append("&amp;");
		break;
	    case '"':
		result.append("&quot;");
		break;
	    default:
		result.append(content[i]);
	    }
	}
	return (result.toString());
    }

}
