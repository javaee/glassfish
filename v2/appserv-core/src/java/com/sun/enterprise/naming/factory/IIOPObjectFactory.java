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
package com.sun.enterprise.naming.factory;

import javax.naming.*;
import javax.naming.spi.*;

import java.util.Hashtable;
import com.sun.enterprise.util.ORBManager;

//START OF IASRI 4660742
import java.util.logging.*;
import com.sun.logging.*;
//END OF IASRI 4660742


/**
 * An object factory to handle URL references.
 * Handles references and looks up in the cosnaming contexts.
 */

public class IIOPObjectFactory implements ObjectFactory {

    // START OF IASRI 4660742
    static Logger _logger=LogDomains.getLogger(LogDomains.JNDI_LOGGER);
    // END OF IASRI 4660742

    public static final boolean debug = false;
    Hashtable env = new Hashtable();

    public Object getObjectInstance(Object obj, 
				    Name name, 
				    Context nameCtx,
				    Hashtable env) throws Exception 
    {
	env.put("java.naming.factory.initial",
		"com.sun.jndi.cosnaming.CNCtxFactory");

	InitialContext ic = new InitialContext(env);

	Reference ref = (Reference) obj;
	if(debug) {
	    /** IASRI 4660742
	    System.out.println("IIOPObjectFactory " + ref + 
				" Name:" + name);
	    **/
	    // START OF IASRI 4660742
	    if(_logger.isLoggable(Level.FINE)) {
         _logger.log(Level.FINE,"IIOPObjectFactory " + ref +
                     " Name:" + name);
       }
	    // END OF IASRI 4660742
	}
	RefAddr refAddr = ref.get("url");
	Object realObject = ic.lookup((String) refAddr.getContent());
	if(debug) {
	    /** IASRI 4660742
	    System.out.println("Found Object:" + realObject);
	    **/
	    // START OF IASRI 4660742
	    if(_logger.isLoggable(Level.FINE)) {
         _logger.log(Level.FINE,"Found Object:" + realObject); 
       }
	    // END OF IASRI 4660742
	}
	return realObject;
    }

}
