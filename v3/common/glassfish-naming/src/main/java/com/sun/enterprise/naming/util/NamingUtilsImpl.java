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

import com.sun.enterprise.naming.spi.NamingObjectFactory;
import com.sun.enterprise.naming.spi.NamingUtils;
import static com.sun.enterprise.naming.util.ObjectInputOutputStreamFactoryFactory.*;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Singleton;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import javax.naming.Context;

/**
 * This is a utils class for refactoring the following method.
 */

@Service
@Scoped(Singleton.class)
public class NamingUtilsImpl
    implements NamingUtils {

    static Logger _logger = LogFacade.getLogger();

    public NamingObjectFactory createSimpleNamingObjectFactory(String name,
        Object value) {
        return new SimpleNamingObjectFactory(name, value);
    }

    public NamingObjectFactory createLazyNamingObjectFactory(String name,
        String jndiName, boolean cacheResult) {
        return new JndiNamingObjectFactory(name, jndiName, cacheResult);
    }

    public NamingObjectFactory createCloningNamingObjectFactory(String name,
        Object value) {
        return new CloningNamingObjectFactory(name, value);
    }

    public NamingObjectFactory createCloningNamingObjectFactory(String name,
        NamingObjectFactory delegate) {
        return new CloningNamingObjectFactory(name, delegate);
    }

    public NamingObjectFactory createDelegatingNamingObjectFactory(String name,
        NamingObjectFactory delegate, boolean cacheResult) {
        return new DelegatingNamingObjectFactory(name, delegate, cacheResult);
    }
    
    public Object makeCopyOfObject(Object obj) {
        if ( !(obj instanceof Context) && (obj instanceof Serializable) ) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "** makeCopyOfObject:: " + obj);
            }
            
            try {
                // first serialize the object
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = getFactory().createObjectOutputStream(bos);
                oos.writeObject(obj);
                oos.flush();
                byte[] data = bos.toByteArray();
                oos.close();
                bos.close();

                // now deserialize it
                ByteArrayInputStream bis = new ByteArrayInputStream(data);
                final ObjectInputStream ois = getFactory().createObjectInputStream(bis);
                obj = AccessController.doPrivileged(new PrivilegedExceptionAction() {
                    public Object run() throws IOException, ClassNotFoundException {
                        return ois.readObject();
                    }
                });
                return obj;
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

    public OutputStream getMailLogOutputStream() {
        return new MailLogOutputStream();
    }
}
