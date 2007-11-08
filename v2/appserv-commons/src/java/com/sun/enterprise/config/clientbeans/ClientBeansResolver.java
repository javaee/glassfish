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

package com.sun.enterprise.config.clientbeans;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXParseException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import java.io.FileInputStream;
import java.io.File;
import java.io.InputStream;

import java.util.logging.Logger;
import java.util.logging.Level;

//The RelativePathResolver is used to translate relative paths containing 
//embedded system properties (e.g. ${com.sun.aas.instanceRoot}/applications) 
//into absolute paths
import com.sun.enterprise.util.RelativePathResolver;

public class ClientBeansResolver extends DefaultHandler {

       
    public static final String SUN_ACC_DTD_PATH = 
        "/sun-application-client-container_1_2.dtd";
    
  
    //===========================================================
    // Resolver methods
    //===========================================================

    public InputSource resolveEntity(String publicID, String systemID) 
      throws SAXException {
        InputSource is = null;
        try {
            InputStream i = this.getClass().getResourceAsStream(SUN_ACC_DTD_PATH );          
            if( i!= null ) {
	        return new InputSource(i);
            }
            is =  new InputSource(
               new FileInputStream(
               new File(new java.net.URI(
                   RelativePathResolver.resolvePath(systemID)))));
        } catch(Exception e) {
            throw new SAXException("Cannot resolve ", e);
        }
        return is;
    }
}



