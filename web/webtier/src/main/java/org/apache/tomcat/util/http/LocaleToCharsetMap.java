

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

/*
 *
 * This class was originally written by Jason Hunter <jhunter@acm.org>
 * as part of the book "Java Servlet Programming" (O'Reilly).  
 * See http://www.servlets.com/book for more information.
 * Used by Sun Microsystems with permission.
 *
 */

package org.apache.tomcat.util.http;

import java.util.*;

/** 
 * A mapping to determine the (somewhat arbitrarily) preferred charset for 
 * a given locale.  Supports all locales recognized in JDK 1.1.
 * This class was originally written by Jason Hunter [jhunter@acm.org]
 * as part of the book "Java Servlet Programming" (O'Reilly).
 * See <a href="http://www.servlets.com/book">
 * http://www.servlets.com/book</a> for more information.
 * Used by Sun Microsystems with permission.
 */
public class LocaleToCharsetMap {

  private static Hashtable map;

  static {
    map = new Hashtable();

    map.put("ar", "ISO-8859-6");
    map.put("be", "ISO-8859-5");
    map.put("bg", "ISO-8859-5");
    map.put("ca", "ISO-8859-1");
    map.put("cs", "ISO-8859-2");
    map.put("da", "ISO-8859-1");
    map.put("de", "ISO-8859-1");
    map.put("el", "ISO-8859-7");
    map.put("en", "ISO-8859-1");
    map.put("es", "ISO-8859-1");
    map.put("et", "ISO-8859-1");
    map.put("fi", "ISO-8859-1");
    map.put("fr", "ISO-8859-1");
    map.put("hr", "ISO-8859-2");
    map.put("hu", "ISO-8859-2");
    map.put("is", "ISO-8859-1");
    map.put("it", "ISO-8859-1");
    map.put("iw", "ISO-8859-8");
    map.put("ja", "Shift_JIS");
    map.put("ko", "EUC-KR");     // Requires JDK 1.1.6
    map.put("lt", "ISO-8859-2");
    map.put("lv", "ISO-8859-2");
    map.put("mk", "ISO-8859-5");
    map.put("nl", "ISO-8859-1");
    map.put("no", "ISO-8859-1");
    map.put("pl", "ISO-8859-2");
    map.put("pt", "ISO-8859-1");
    map.put("ro", "ISO-8859-2");
    map.put("ru", "ISO-8859-5");
    map.put("sh", "ISO-8859-5");
    map.put("sk", "ISO-8859-2");
    map.put("sl", "ISO-8859-2");
    map.put("sq", "ISO-8859-2");
    map.put("sr", "ISO-8859-5");
    map.put("sv", "ISO-8859-1");
    map.put("tr", "ISO-8859-9");
    map.put("uk", "ISO-8859-5");
    map.put("zh", "GB2312");
    map.put("zh_TW", "Big5");

  }

  /**
   * Gets the preferred charset for the given locale, or null if the locale
   * is not recognized.
   *
   * @param loc the locale
   * @return the preferred charset
   */
  public static String getCharset(Locale loc) {
    String charset;

    // Try for an full name match (may include country)
    charset = (String) map.get(loc.toString());
    if (charset != null) return charset;

    // If a full name didn't match, try just the language
    charset = (String) map.get(loc.getLanguage());
    return charset;  // may be null
  }
}
