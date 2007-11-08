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

import javax.naming.spi.ResolveResult;
import javax.naming.*;
import java.util.Hashtable;
import java.net.MalformedURLException;

import com.sun.jndi.cosnaming.IiopUrl;

/**
 * A corbaname URL context.
 * 
 * @author Rosanna Lee
 */

public class corbanameURLContext
	extends com.sun.jndi.toolkit.url.GenericURLContext {

    corbanameURLContext(Hashtable env) {
	super(env);
    }

    /**
      * Resolves 'name' into a target context with remaining name.
      * It only resolves the hostname/port number. The remaining name
      * contains the rest of the name found in the URL.
      *
      * For example, with a corbaname  URL 
      * "corbaname:iiop://localhost:900#rest/of/name",
      * this method resolves "corbaname:iiop://localhost:900" to the "NameService"
      * context on for the ORB at 'localhost' on port 900, 
      * and returns as the remaining name "rest/of/name".
      */
    protected ResolveResult getRootURLContext(String name, Hashtable env) 
    throws NamingException {
	return corbanameURLContextFactory.getUsingURLIgnoreRest(name, env);
    }

    /**
     * Return the suffix of a corbaname url.
     * prefix parameter is ignored.
     */
    protected Name getURLSuffix(String prefix, String url)
	throws NamingException {
	    // Rewrite to corbaname url
	    url = corbanameURLContextFactory.rewriteUrl(url);
	    try {
		IiopUrl parsedUrl = new IiopUrl(url);
		return parsedUrl.getCosName();
	    } catch (MalformedURLException e) {
		throw new InvalidNameException(e.getMessage());
	    }
    }

    /**
      * Finds the prefix of a corbaname URL.
      * This is all of the non-string name portion of the URL.
      * The string name portion always occurs after the '#'
      * so we just need to look for that.
      */
    protected String getURLPrefix(String url) throws NamingException {
	int start = url.indexOf('#');

	if (start < 0) {
	    return url;	    // No '#', prefix is the entire URL
	}

	return url.substring(0, start);
    }
}
