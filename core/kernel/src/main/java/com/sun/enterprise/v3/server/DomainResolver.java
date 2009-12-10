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

package com.sun.enterprise.v3.server;


import com.sun.enterprise.module.common_impl.LogHelper;

import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * DTD resolver used when parsing the domain.xml and resolve to local DTD copies
 *
 * @author Jerome Dochez
 * @Deprecated
 */

@Deprecated
public class   DomainResolver implements EntityResolver {
    public InputSource resolveEntity(String publicId, String systemId) {
        
        if (systemId.startsWith("http://www.sun.com/software/appserver/")) {
            // return a special input source
            String fileName = systemId.substring("http://www.sun.com/software/appserver/".length());
            File f = new File(System.getProperty("com.sun.aas.installRoot"));
            f = new File(f, "lib");
            f = new File(f, fileName.replace('/', File.separatorChar));
            if (f.exists()) {
                try {
                    return new InputSource(new BufferedInputStream(new FileInputStream(f)));
                } catch(IOException e) {
                    LogHelper.getDefaultLogger().log(Level.SEVERE, "Exception while getting " + fileName + " : ", e);
                    return null;
                }
            } else {
                System.out.println("Cannot find " + f.getAbsolutePath());
                return null;
            }
            //MyReader reader = new MyReader();
            //return new InputSource(reader);
        } else {
            // use the default behaviour
            return null;
        }
    }
}
