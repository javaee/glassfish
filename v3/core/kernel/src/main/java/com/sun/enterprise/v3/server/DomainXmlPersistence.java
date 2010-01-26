/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.v3.server;

import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.config.DomDocument;
import org.jvnet.hk2.config.IndentingXMLStreamWriter;
import org.jvnet.hk2.component.Singleton;
import org.glassfish.config.support.ConfigurationPersistence;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.logging.Logger;

/**
 * domain.xml persistence.
 *
 * @author Jerome Dochez
 */
@Service
@Scoped(Singleton.class)
public class DomainXmlPersistence implements ConfigurationPersistence {

    @Inject
    ServerEnvironmentImpl env;

    @Inject
    protected Logger logger;
    
    final XMLOutputFactory xmlFactory = XMLOutputFactory.newInstance();

    public synchronized void save(DomDocument doc) throws IOException {


        File destination = getDestination();
        if (destination==null) {
            logger.fine("domain.xml not persisted, null destination");
            return;
        }
        // get a temporary file
        File f = File.createTempFile("domain", ".xml", destination.getParentFile());
        if (f==null) {
            throw new IOException("Cannot create temporary file when saving domain.xml");
        }
        // write to the temporary file
        XMLStreamWriter writer = null;
        OutputStream fos = getOutputStream(f);
        try {
            writer = xmlFactory.createXMLStreamWriter(new BufferedOutputStream(fos));
            IndentingXMLStreamWriter indentingXMLStreamWriter = new IndentingXMLStreamWriter(writer);
            doc.writeTo(indentingXMLStreamWriter);
            indentingXMLStreamWriter.close();
        } catch (XMLStreamException e) {
            logger.severe("Temporary file could not be created, disk full?");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return;
            // return after calling finally clause, because since temp file couldn't be saved,
            // renaming should not be attempted
        } finally {
            if (writer!=null) {
                try {
                    writer.close();
                } catch (XMLStreamException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            try {
                fos.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        
        // backup the current file
        File backup = new File(env.getConfigDirPath(), "domain.xml.bak");
        if (backup.exists() && !backup.delete()) {
            logger.severe("Could not delete previous backup file at " + backup.getAbsolutePath());
            return;
        }
        if (destination!=null) {
            if (!destination.renameTo(backup)) {
                logger.severe("Could not rename " + destination.getAbsolutePath() + " to " + backup.getAbsolutePath());
                return;
            }
            // save the temp file to domain.xml
            if (!f.renameTo(destination)) {
                logger.severe("Could not rename " + f.getAbsolutePath() + " to " + destination.getAbsolutePath());
                if (!backup.renameTo(destination)) {
                    logger.severe("Could not rename backup to" + destination.getAbsolutePath());
                }
            }
        }
        saved(destination);
    }

    protected void saved(File destination) {
        logger.fine("Configuration saved at " + destination);
    }

    protected File getDestination() throws IOException {
        return new File(env.getConfigDirPath(), "domain.xml");
    }

    protected OutputStream getOutputStream(File destination) throws IOException {
        return new FileOutputStream(destination);
    }

}
