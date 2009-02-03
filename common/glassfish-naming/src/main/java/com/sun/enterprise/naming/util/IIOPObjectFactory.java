/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.enterprise.naming.util;

import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

/**
 * An object factory to handle URL references.
 * Handles references and looks up in the cosnaming contexts.
 */

public class IIOPObjectFactory implements ObjectFactory {

    static Logger _logger;

    public static final boolean debug = false;
    Hashtable env = new Hashtable();

    public Object getObjectInstance(Object obj,
                                    Name name,
                                    Context nameCtx,
                                    Hashtable env) throws Exception {
        env.put("java.naming.factory.initial",
                "com.sun.jndi.cosnaming.CNCtxFactory");

        InitialContext ic = new InitialContext(env);

        Reference ref = (Reference) obj;
        if (debug) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "IIOPObjectFactory " + ref +
                        " Name:" + name);
            }
        }
        RefAddr refAddr = ref.get("url");
        Object realObject = ic.lookup((String) refAddr.getContent());
        if (debug) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Found Object:" + realObject);
            }
        }
        return realObject;
    }

}
