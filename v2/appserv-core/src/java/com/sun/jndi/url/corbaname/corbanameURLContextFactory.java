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

import javax.naming.*;
import javax.naming.spi.*;

import java.util.Hashtable;
import java.net.MalformedURLException;
import com.sun.jndi.cosnaming.CNCtx;
import org.omg.CORBA.ORB;

//START OF IASRI 4660742
import java.util.logging.*;
import com.sun.logging.*;
//END OF IASRI 4660742

/**
 * A corbaname URL context factory. (INS 12-03-99)
 *
 * <corbaname>		= "corbaname:" <corbaloc_obj> ["#" <string_name>]
 * <string_name> 	= stringified Name | empty_string
 * <corbaloc_obj> 	= <obj_addr_list> ["/" <key_string>]
 * <obj_addr_list> 	= [<obj_addr> ","]* <obj_addr>
 * <obj_addr> 		= <prot_addr> | <future_prot_addr>
 * <prot_addr> 		= <rir_prot_addr> | <iiop_prot_addr>
 * <rir_prot_addr> 	= "rir:"
 * <iiop_prot_addr> 	= <iiop_id> <iiop_addr>
 * <iiop_id> 		= ":" | "iiop:"
 * <iiop_addr> 		= <version> <host> [":" <port>]
 * <host> 		= DNS-style host name | IP address
 * <version> 		= <major> "." <minor> "@" | empty_string
 * <port> 		= number
 * <major> 		= number
 * <minor> 		= number
 * <key_string> 	= <string> | empty_string
 * 
 * NOTES:
 * 1. Does NOT support having a <key_string> in its <corbaloc_obj>.
 * 2. Does NOT support multiple "rir" in single URL
 *
 * @author Rosanna Lee
 */

public class corbanameURLContextFactory implements ObjectFactory {
    // START OF IASRI 4660742
    static Logger _logger=LogDomains.getLogger(LogDomains.JNDI_LOGGER);
    // END OF IASRI 4660742

    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "2089";

    public Object getObjectInstance(Object urlInfo, Name name, Context nameCtx,
				    Hashtable env) throws Exception {

	if (urlInfo == null) {
	    return new corbanameURLContext(env);
	}
	if (urlInfo instanceof String) {
	    return getUsingURL((String)urlInfo, env);
	} else if (urlInfo instanceof String[]) {
	    return getUsingURLs((String[])urlInfo, env);
	} else {
	    throw (new IllegalArgumentException(
		    "iiopURLContextFactory.getObjectInstance: " +
		    "argument must be an iiop URL String or array of iiop URLs"));
	}
    }

    /**
      * Resolves 'name' into a target context with remaining name.
      * It only resolves the hostname/port number. The remaining name
      * contains the rest of the name found in the URL.
      *
      * For example, with a corbaname URL:
      *     corbaname:iiop://localhost:900#rest/of/name
      * this method resolves 
      *     corbaname:iiop://localhost:900/ to the "NameService"
      * context on for the ORB at 'localhost' on port 900, 
      * and returns as the remaining name "rest/of/name".
      */
    static ResolveResult getUsingURLIgnoreRest(String url, Hashtable env)
	    throws NamingException {

	ORB inOrb = (env != null) 
	    ? (ORB) env.get("java.naming.corba.orb") 
	    : null;
		
	if (inOrb != null) {
	    try {
		CorbanameUrl parsedUrl = new CorbanameUrl(url);

		// See if ORB can handle corbaname URL directly
		org.omg.CORBA.Object ncRef = null;

		try {
		    // Get object ref for NameService specified in corbaname URL
		    ncRef = inOrb.string_to_object(parsedUrl.getLocation());
		} catch (Exception e) {
		}

		if (ncRef != null) {
		    // Convert to JNDI Context
		    Context ctx = CNCtxHelper.getInstance(inOrb, ncRef, env);

		    return new ResolveResult(ctx, parsedUrl.getCosName());
		}
	    } catch (MalformedURLException e) {
		throw new ConfigurationException(e.getMessage());
	    }
	}

	// Rewrite to iiop URL for handling by JNDI/COS provider
	url = rewriteUrl(url);	
	    
	return CNCtx.createUsingURL(url, env);
    }

    private static Object getUsingURL(String url, Hashtable env)
	    throws NamingException {
	ResolveResult res = getUsingURLIgnoreRest(url, env);

	Context ctx = (Context)res.getResolvedObj();
	try {
	    return ctx.lookup(res.getRemainingName());
	} finally {
	    ctx.close();
	}
    }

    private static Object getUsingURLs(String[] urls, Hashtable env) {
	for (int i = 0; i < urls.length; i++) {
	    String url = urls[i];
	    try {
		Object obj = getUsingURL(url, env);
		if (obj != null) {
		    return obj;
		}
	    } catch (NamingException e) {
	    }
	}
	return null;	// %%% exception??
    }


    /**
     * corbaname:rir: -> iiopname://localhost:2089/
     * corbaname:rir:/NameService -> iiopname://localhost:2089/
     * corbaname:rir:/dev/NameService 
     * 		-> InvalidNameException (key_string not supported)
     *
     * corbaname:: -> iiopname://localhost:2089/
     * corbaname::orbhost:999#this/is/a/name 
     * 		-> iiopname://orbhost:999/this/is/a/name
     * corbaname::orbhost:999,:webhost#this/is/a/name 
     * 		-> iiopname://orbhost:999,webhost:2089/this/is/a/name
     * corbaname::orbhost:999,:webhost/key/String#this/is/a/name 
     * 		-> InvalidNameException (key_string not supported)
    
     * corbaname:iiop: -> iiopname://localhost:2089/
     * corbaname:iiop:orbhost:999#this/is/a/name
     * 		-> iiopname://orbhost:999/this/is/a/name
     * corbaname:iiop:orbhost:999,iiop:webhost#this/is/a/name 
     * 		-> iiopname://orbhost:999,webhost:2089/this/is/a/name
     * corbaname:iiop:orbhost:999,iiop:webhost/key/String#this/is/a/name 
     * 		-> InvalidNameException (key_string not supported)
     *
     * corbaname:iiop:orbhost:999,:webhost#this/is/a/name
     * 		-> iiopname://orbhost:999,webhost:2089/this/is/a/name
     * corbaname::orbhost:999,iiop:webhost#this/is/a/name
     * 		-> iiopname://orbhost:999,webhost:2089/this/is/a/name
     */
    static String rewriteUrl(String url) throws NamingException {

	// Find string_name
	String stringName = null, corbaloc;
	int hash = url.indexOf('#');
	if (hash >= 0) {
	    stringName = url.substring(hash+1);

	    // get rid of 'corbaname:' and string_name
	    corbaloc = url.substring(10, hash);  
	} else {
	    corbaloc = url.substring(10);   // get rid of 'corbaname:'
	}

	// Make sure key_string is one that we can support
	String objAddrList;
	int firstSlash = corbaloc.indexOf('/');
	if (firstSlash >= 0) {
	    String keyString = corbaloc.substring(firstSlash+1); // skip slash

	    // An empty key_string is interpreted as "NameService"
	    if ("".equals(keyString)) {
		keyString = "NameService";
	    } else if (!"NameService".equals(keyString)) {
		throw new InvalidNameException(
		    "Support is available only for the NameService key string");
	    }

	    objAddrList = corbaloc.substring(0, firstSlash);
	} else {
	    objAddrList = corbaloc;
	}
	    
	// Rewrite objAddrList into iiopname format

	int len = objAddrList.length();
	int colon, start = 0, comma;
	String prot, addr;
	StringBuffer newUrl = new StringBuffer("iiopname://");
	while (start < len) {
	    colon = objAddrList.indexOf(':', start);
	    prot = objAddrList.substring(start, colon);
	    if (prot.equals("") || prot.equals("iiop")) {
		// Find end of this address
		comma = objAddrList.indexOf(',', colon+1);
		if (comma < 0) {
		    // last address in list
		    addr = objAddrList.substring(colon+1, len);
		    start = len;
		} else {
		    addr = objAddrList.substring(colon+1, comma);
		    start = comma + 1;
		}

		newUrl.append(addr);

		// Add default port if none has been specified
		if (addr.indexOf(':') < 0) {
		    newUrl.append(':');
		    newUrl.append(DEFAULT_PORT);
		}

		if (comma >= 0) {
		    // add comma
		    newUrl.append(',');
		}
	    } else if (prot.equals("rir")) {
		newUrl.append(DEFAULT_HOST);
		newUrl.append(':');
		newUrl.append(DEFAULT_PORT);

		start = colon + 1; // skip colon

		if (start != len) {
		    throw new InvalidNameException("Only one rir is supported");
		}
	    } else {
		throw new InvalidNameException("Unknown subscheme: " + url);
	    }
	}

	if (stringName != null) {
	    newUrl.append('/');
	    newUrl.append(stringName);
	}

	return newUrl.toString();
    }

    public static final String[] tests = {
	"corbaname:rir:",
	"corbaname:rir:/NameService",
	"corbaname:rir:/dev/NameService",
	"corbaname:rir:,rir:/NameService",
	"corbaname::",
	"corbaname::orbhost:999#this/is/a/name",
	"corbaname::orbhost:999,:webhost#this/is/a/name",
	"corbaname::orbhost:999,:webhost/key/String#this/is/a/name",
	"corbaname:iiop:",
	"corbaname:iiop:orbhost:999#this/is/a/name",
	"corbaname:iiop:orbhost:999,iiop:webhost#this/is/a/name",
	"corbaname:iiop:orbhost:999,iiop:webhost/key/String#this/is/a/name",
	"corbaname:iiop:orbhost:999,:webhost#this/is/a/name",
	"corbaname::orbhost:999,iiop:webhost#this/is/a/name",
	"corbaname:foo:bar#this/is/a/name"
    };

    public static void main(String[] args) {

	for (int i = 0; i < tests.length; i++) {
      /** IASRI 4660742
	    System.out.println(tests[i]);
      **/
      //START OF IASRI 4660742
      if (_logger.isLoggable(Level.FINE))
          _logger.log(Level.FINE,tests[i]);
      //END OF IASRI 4660742

	    try {
    /** IASRI 4660742
		System.out.println("    " + rewriteUrl(tests[i]));
    **/
    //START OF IASRI 4660742
		if (_logger.isLoggable(Level.FINE))
   	 _logger.log(Level.FINE,"    " + rewriteUrl(tests[i]));
    //END OF IASRI 4660742
	    } catch (NamingException e) {
    /** IASRI 4660742
		System.out.println("    " + e);
    **/
    //START OF IASRI 4660742
    _logger.log(Level.SEVERE,"java_jndi.excep_in_main", e);
    //END OF IASRI 4660742
	    }
	}
    }
}
