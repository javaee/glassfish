/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * XMLReader.java
 *
 * Created on March 24, 2005, 12:18 PM
 */


package com.sun.persistence.api.deployment;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class reads Persistence_ORM XML deployment descriptor and builds a
 * descriptor object graph.
 *
 * @author Sanjeeb Sahoo
 * @version 1.0
 */
public class XMLReader {

    public PersistenceJarDescriptor read(InputStream is) throws IOException {
        try {
            JAXBContext jc = JAXBContext.newInstance(
                    PersistenceJarDescriptor.class.getPackage().getName());
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            return (PersistenceJarDescriptor) unmarshaller.unmarshal(is);
        } catch (JAXBException je) {
            IOException ioe = new IOException();
            ioe.initCause(je);
            throw ioe;
        }
    }
}
