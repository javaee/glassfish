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

/*
 * Copyright 2005-2006 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.mfwk.agent.appserv.delegate;

import java.util.Map;
import java.util.StringTokenizer;
import org.w3c.dom.Element;
import javax.management.ObjectName;
import javax.management.MBeanServerConnection;
import com.sun.mfwk.agent.appserv.mapping.MappingQueryService;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.mfwk.agent.appserv.logging.LogDomains;

import java.io.IOException;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.InstanceNotFoundException;

/**
 * Converts comma separated thread id to an array of long.
 */
public class StringToLongArrayHandler extends BaseHandler {

    /**
     * Constructor.
     */
    public StringToLongArrayHandler() {
        super();
    }
    
    public Object handleAttribute(ObjectName peer, String attribute, 
            MBeanServerConnection mbs) throws HandlerException, AttributeNotFoundException, 
             MBeanException, ReflectionException, InstanceNotFoundException,
             IOException {

        Object val = mbs.getAttribute(peer, attribute);
        String eASType = (String) _handlerProperties.get(AS_TYPE);

        long[] longValues = null;
        if (val instanceof java.lang.String) {

            LogDomains.getLogger().log(Level.FINE,
                "[StringToLongArrayHandler] AS Type: java.lang.String");
            LogDomains.getLogger().log(Level.FINE,
                "[StringToLongArrayHandler] Expected AS Type: " + eASType);

            StringTokenizer st = new StringTokenizer((String) val, ",");
            longValues= new long[st.countTokens()];
            int idx = 0;
            while (st.hasMoreTokens()) {
                String id = st.nextToken();
                longValues[idx] = Long.parseLong(id);
                idx++;
            }
        }

        return longValues;
    }

    // ---- INSTANCE VARIABLES -------------------------------
    private static final String AS_TYPE ="as-type"; 
}
