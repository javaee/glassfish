/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.osgijpa.dd;

import org.glassfish.internal.api.Globals;
import org.glassfish.internal.api.ClassLoaderHierarchy;

import javax.xml.bind.Marshaller;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class PersistenceXMLReaderWriter
{
    private static final Logger logger = Logger.getLogger(
            PersistenceXMLReaderWriter.class.getPackage().getName());

    public Persistence read(URL pxmlURL) throws IOException
    {
        InputStream is = pxmlURL.openStream();
        try {
            return read(is);
        } finally {
            is.close();
        }
    }

    public Persistence read(InputStream is) throws IOException {
        try {
            Unmarshaller unmarshaller = getUnmarshaller();
            return (Persistence) unmarshaller.unmarshal(is);
        } catch (JAXBException je) {
            je.printStackTrace();
            IOException ioe = new IOException();
            ioe.initCause(je);
            throw ioe;
        }
    }

    public void write(Persistence persistence, OutputStream os)
            throws IOException {
        try {
            getMarshaller(persistence.getClass()).marshal(persistence,
                    os);
        } catch (JAXBException je) {
            je.printStackTrace();
            IOException ioe = new IOException();
            ioe.initCause(je);
            throw ioe;
        }
    }

    public void write(Persistence persistence, Writer writer)
            throws IOException {
        try {
            getMarshaller(persistence.getClass()).marshal(persistence,
                    writer);
        } catch (JAXBException je) {
            je.printStackTrace();
            IOException ioe = new IOException();
            ioe.initCause(je);
            throw ioe;
        }
    }

    private Marshaller getMarshaller(Class<?> clazz) throws JAXBException {
         JAXBContext jc = getJAXBContext();
         Marshaller marshaller = jc.createMarshaller();
         marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                 Boolean.TRUE);
         return marshaller;
     }

    private Unmarshaller getUnmarshaller() throws JAXBException {
        JAXBContext jc = getJAXBContext();
        return jc.createUnmarshaller();
    }

    private JAXBContext getJAXBContext() throws JAXBException {
        // We need to set context class loader to be CommonClassLoader, otherwise our stupid JAXB implementation
        // won't be able to locate the default JAXB context factory class.
        final Thread thread = Thread.currentThread();
        ClassLoader oldCL = thread.getContextClassLoader();
        try {
            ClassLoader ccl = Globals.get(ClassLoaderHierarchy.class).getCommonClassLoader();
            thread.setContextClassLoader(ccl);
            JAXBContext jc = JAXBContext.newInstance(ObjectFactory.class);
        return jc;
        } finally {
            thread.setContextClassLoader(oldCL);
        }
    }

}
