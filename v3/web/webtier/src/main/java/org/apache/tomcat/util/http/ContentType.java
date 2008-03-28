

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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

package org.apache.tomcat.util.http;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

/**
 * Usefull methods for Content-Type processing
 * 
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author Harish Prabandham
 * @author costin@eng.sun.com
 */
public class ContentType {

    // Basically return everything after ";charset="
    // If no charset specified, use the HTTP default (ASCII) character set.
    public static String getCharsetFromContentType(String type) {
        if (type == null) {
            return null;
        }
        int semi = type.indexOf(";");
        if (semi == -1) {
            return null;
        }
        int charsetLocation = type.indexOf("charset=", semi);
        if (charsetLocation == -1) {
            return null;
        }
	String afterCharset = type.substring(charsetLocation + 8);
        // The charset value in a Content-Type header is allowed to be quoted
        // and charset values can't contain quotes.  Just convert any quote
        // chars into spaces and let trim clean things up.
        afterCharset = afterCharset.replace('"', ' ');
        String encoding = afterCharset.trim();
        return encoding;
    }


    // Bad method: the user may set the charset explicitely
    
//     /** Utility method for parsing the mime type and setting
//      *  the encoding to locale. Also, convert from java Locale to mime
//      *  encodings
//      */
//     public static String constructLocalizedContentType(String type,
// 							Locale loc) {
//         // Cut off everything after the semicolon
//         int semi = type.indexOf(";");
//         if (semi != -1) {
//             type = type.substring(0, semi);
//         }

//         // Append the appropriate charset, based on the locale
//         String charset = LocaleToCharsetMap.getCharset(loc);
//         if (charset != null) {
//             type = type + "; charset=" + charset;
//         }

//         return type;
//     }

}
