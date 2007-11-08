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
 * IOUtilsCallerImpl.java
 *
 * Created on October 13, 2003, 11:40 AM
 */

package com.sun.ejb.base.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import com.sun.ejb.spi.io.NonSerializableObjectHandler;
import org.apache.catalina.session.IOUtilsCaller;
import org.apache.catalina.util.CustomObjectInputStream;

/**
 * This class is used by the web tier to obtain equivalent
 * object stream service as the ejb tier obtains from directly
 * using classes like IOUtils
 * @author  Administrator
 */
public class IOUtilsCallerImpl implements IOUtilsCaller{
    
    /** Creates a new instance of IOUtilsCallerImpl */
    public IOUtilsCallerImpl() {
    }
    
    public ObjectInputStream createObjectInputStream(
            final InputStream is,
            final boolean resolveObject,
            final ClassLoader loader)
        throws Exception {
        ObjectInputStream strm = null;
        try {
            strm = IOUtils.createObjectInputStream(is, resolveObject, loader);
        } catch (Exception ex) {
            //deliberate no-op - escape findbugs warning
            assert true;
        }
        if (strm == null) {
            strm = new CustomObjectInputStream(is, loader);
        }
        return strm;
    }
    
    public ObjectOutputStream createObjectOutputStream(
        final OutputStream os,
        final boolean replaceObject) throws IOException {
        return IOUtils.createObjectOutputStream(os, replaceObject, 
            new NonSerializableObjectHandler() {
                public Object handleNonSerializableObject(Object obj) {
                    return obj;
                }
            }            
        );
    }     
    
    public ObjectOutputStream createObjectOutputStream(
        final OutputStream os,
        final boolean replaceObject,
        final NonSerializableObjectHandler handler) throws IOException {
        return IOUtils.createObjectOutputStream(os, replaceObject, handler);
    }    
    
}
