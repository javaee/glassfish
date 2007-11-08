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

package com.sun.jndi.url.corbaname;

import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingException;

import java.net.MalformedURLException;
import com.sun.jndi.toolkit.url.UrlUtil;
import com.sun.jndi.cosnaming.CNNameParser;

//START OF IASRI 4660742
import java.util.logging.*;
import com.sun.logging.*;
//END OF IASRI 4660742


/**
 * Extract components of a "corbaname" URL.
 *
 * The format of an corbaname URL is defined in INS 99-12-03 as follows.
 *<p>
 * corbaname url = "corbaname:" <corbaloc_obj> ["#" <string_name>]
 * corbaloc_obj  = <obj_addr_list> ["/" <key_string>]
 * obj_addr_list = as defined in a corbaloc URL
 * key_string    = as defined in a corbaloc URL
 * string_name   = stringified COS name | empty_string
 *<p>
 * Characters in <string_name> are escaped as follows.
 * US-ASCII alphanumeric characters are not escaped. Any characters outside 
 * of this range are escaped except for the following:
 *        ; / : ? @ & = + $ , - _ . ! ~ * ; ( )
 * Escaped characters is escaped by using a % followed by its 2 hexadecimal
 * numbers representing the octet.
 *<p>
 * The corbaname URL is parsed into two parts: a corbaloc URL and a COS name.
 * The corbaloc URL is constructed by concatenation "corbaloc:" with
 * <corbaloc_obj>.
 * The COS name is <string_name> with the escaped characters resolved.
 *<p>
 * A corbaname URL is resolved by:
 *<ol>
 *<li>Construct a corbaloc URL by concatenating "corbaloc:" and <corbaloc_obj>.
 *<li>Resolve the corbaloc URL to a NamingContext by using 
 *     nctx = ORB.string_to_object(corbalocUrl);
 *<li>Resolve <string_name> in the NamingContext.
 *</ol>
 *
 * @author Rosanna Lee
 */

public final class CorbanameUrl {

    // START OF IASRI 4660742
    static Logger _logger=LogDomains.getLogger(LogDomains.JNDI_LOGGER);
    // END OF IASRI 4660742

    private String stringName;
    private String location;

    private static final NameParser parser = new CNNameParser();

    /**
     * Returns a possibly empty but non-null string that is the "string_name"
     * portion of the URL.
     */
    public String getStringName() {
	return stringName;
    }

    public Name getCosName() throws NamingException {
	return parser.parse(stringName);
    }

    public String getLocation() {
	return "corbaloc:" + location;
    }

    public CorbanameUrl(String url) throws MalformedURLException {

	if (!url.startsWith("corbaname:")) {
	    throw new MalformedURLException("Invalid corbaname URL: " + url);
	}

	int addrStart = 10;  // "corbaname:"

	int addrEnd = url.indexOf('#', addrStart);
	if (addrEnd < 0) {
	    addrEnd = url.length();
	    stringName = "";
	} else {
	    stringName = UrlUtil.decode(url.substring(addrEnd+1));
	}
	location = url.substring(addrStart, addrEnd);
	
	int keyStart = location.indexOf("/");
	if (keyStart >= 0) {
	    // Has key string
	    if (keyStart == (location.length() -1)) {
		location += "NameService";
	    }
	} else {
	    location += "/NameService";
	}
    }
/*
    // for testing only
    public static void main(String[] args) {
	try {
	    CorbanameUrl url = new CorbanameUrl(args[0]);

      // IASRI 4660742
	    //System.out.println("location: " + url.getLocation());
	    //System.out.println("string name: " + url.getStringName());
      // IASRI 4660742
      // START OF IASRI 4660742
		if (_logger.isLoggable(Level.FINE)) {
      _logger.log(Level.FINE,"location: " + url.getLocation());
      _logger.log(Level.FINE,"string name: " + url.getStringName());
		}
      // END OF IASRI 4660742
	} catch (MalformedURLException e) {
      // IASRI 4660742
	    e.printStackTrace();
      // IASRI 4660742
      // START OF IASRI 4660742
      _logger.log(Level.SEVERE,"java_jndi.excep_in_corbanameurl_main",e);
      // END OF IASRI 4660742
	}
    }
*/
}
