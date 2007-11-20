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

import org.glassfish.api.naming.NamingUtils;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a utils class for refactoring the following method.
 */

@Service
public class NamingUtilsImpl
    implements NamingUtils {

    static Logger _logger = LogFacade.getLogger();

    /**
     * method to make a copy of the object.
     */

    public Object makeCopyOfObject(Object obj) {


        if (obj instanceof Serializable) {
            _logger.log(Level.FINE, "** makeCopyOfObject:: " + obj);
            try {
                // first serialize the object
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(obj);
                oos.flush();
                byte[] data = bos.toByteArray();
                oos.close();
                bos.close();

                // now deserialize it
                ByteArrayInputStream bis = new ByteArrayInputStream(data);
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                ObjectInputStream ois = new ObjectInputStreamWithLoader(bis, cl);


                return ois.readObject();
            } catch (Exception ex) {

                _logger.log(Level.SEVERE,
                        "enterprise_naming.excep_in_copymutableobj", ex);

                RuntimeException re =
                        new RuntimeException("Cant copy Serializable object:");
                re.initCause(ex);
                throw re;
            }
        } else {
            // XXX no copy ?
            return obj;
        }
    }
}
