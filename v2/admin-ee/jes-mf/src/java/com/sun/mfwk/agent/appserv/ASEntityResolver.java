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



package com.sun.mfwk.agent.appserv;

import java.io.InputStream;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import java.io.File;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.mfwk.agent.appserv.logging.LogDomains;

/**
 * The entity resolver looks for dtd in the classpath of this class
 */
public class ASEntityResolver implements EntityResolver {

    public InputSource resolveEntity (String publicId, String systemId) {
        if (systemId != null &&  systemId.endsWith(".dtd")) {
            int lastPart = systemId.lastIndexOf(File.separatorChar);
            if (lastPart >= 0 && lastPart < systemId.length()) {
                systemId = systemId.substring(lastPart + 1); // get filename
            }
            LogDomains.getLogger().finest("systemId = " + systemId + " lastPart = " + lastPart); 
            
            InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(systemId);
            if (resourceStream != null) {
                return new InputSource(resourceStream);
            }
        }
      // use the default behaviour
        return null;
    }
       
}
