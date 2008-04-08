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

package com.sun.ejb.base.io;

import com.sun.ejb.spi.io.SerializableObjectFactory;

import com.sun.enterprise.naming.util.ObjectInputStreamWithLoader;

import com.sun.logging.LogDomains;

import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.rmi.Remote;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class that is used to restore SFSB conversational state
 *
 * @author Mahesh Kannan
 */
class EJBObjectInputStream
    extends ObjectInputStreamWithLoader
{

    private static final Logger _ejbLogger =
       LogDomains.getLogger(LogDomains.EJB_LOGGER);

    EJBObjectInputStream(InputStream in, ClassLoader cl, boolean resolve)
        throws IOException, StreamCorruptedException
    {
        super(in, cl);
        if (resolve == true) {
            enableResolveObject(resolve);
        }
    }

    protected Object resolveObject(Object obj)
        throws IOException
    {
        try {
            /*TODO if ( StubAdapter.isStub(obj) ) {
                // connect the Remote object to the Protocol Manager
                //TODO Switch.getSwitch().getProtocolManager.connectObject((Remote)obj);
                return obj;
            } else */ if (obj instanceof SerializableObjectFactory) {
                return ((SerializableObjectFactory) obj).createObject();
            } else {
                return obj;
            }
        } catch (IOException ioEx ) {
            _ejbLogger.log(Level.SEVERE, "ejb.resolve_object_exception", ioEx);
            throw ioEx;
        } catch (Exception ex) {
            _ejbLogger.log(Level.SEVERE, "ejb.resolve_object_exception", ex);
            IOException ioe = new IOException();
            ioe.initCause(ex);
            throw ioe;
        }
    }

    protected Class resolveProxyClass(String[] interfaces)
        throws IOException, ClassNotFoundException
    {
        Class[] classObjs = new Class[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            Class cl = Class.forName(interfaces[i], false, loader);
            // If any non-public interfaces, delegate to JDK's
            // implementation of resolveProxyClass.
            if ((cl.getModifiers() & Modifier.PUBLIC) == 0) {
                return super.resolveProxyClass(interfaces);
            } else {
                classObjs[i] = cl;
            }
        }
        try {
            return Proxy.getProxyClass(loader, classObjs);
        } catch (IllegalArgumentException e) {
            throw new ClassNotFoundException(null, e);
        }
    }
}
