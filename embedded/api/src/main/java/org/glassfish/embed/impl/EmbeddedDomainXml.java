/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 */

package org.glassfish.embed.impl;

import java.io.*;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import org.glassfish.config.support.ConfigurationPersistence;
import org.glassfish.embed.EmbeddedException;
import org.glassfish.embed.Server;
import org.jvnet.hk2.config.DomDocument;
import org.jvnet.hk2.annotations.Inject;
import org.glassfish.config.support.DomainXml;
import org.glassfish.embed.util.LoggerHelper;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.config.IndentingXMLStreamWriter;


/**
 * Loads the default empty domain.xml.
 * 
 * @author Kohsuke Kawaguchi
 */
public class EmbeddedDomainXml extends DomainXml implements ConfigurationPersistence {
    @Inject
    protected Server server;
 
    public EmbeddedDomainXml() {
    }

    @Override
    protected URL getDomainXml(ServerEnvironmentImpl env) {
        try {
//        return getClass().getResource("/org/glassfish/embed/domain.xml");
            return server.getDomainXmlUrl();
        }
        catch (EmbeddedException ex) {
            return null;
        }
    }

    public void save(DomDocument doc) throws IOException, XMLStreamException {
        LoggerHelper.fine("in EmbeddedDomainXml.save()");
        File dx = null;

        try {
            dx = server.getFileSystem().getTargetDomainXml();
        }
        catch (EmbeddedException ex) {
            LoggerHelper.severe("error in EmbeddedDomainXml.save(): " + ex); // TODO i18n
            throw new IOException("error in EmbeddedDomainXml.save(): " + ex);
        }

        OutputStream out = new FileOutputStream(dx);
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        IndentingXMLStreamWriter writer = new IndentingXMLStreamWriter(
                        factory.createXMLStreamWriter(out));

        doc.writeTo(writer);
        writer.flush();
        writer.close();
        out.close();
    }

    protected DomDocument getDomDocument() {
        return null; // TODO...
    }
    

}
