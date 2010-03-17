/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.config.support;

import java.io.*;
import java.net.*;
import java.util.logging.*;
import javax.xml.stream.*;

/**
 * {@link XMLStreamReader} that skips irrelvant &lt;config> elements that we shouldn't see.
 * @author Jerome Dochez
 * @author Kohsuke Kawaguchi
 * @author Byron Nevins
 * bnevins:  This class was initially an inner class of DomainXml in this package.
 * It was pulled into its own stand-alone class on March 16,2010 in preparation
 * for new clustering requirements.
 */
class DomainXmlReader extends XMLStreamReaderFilter {

    DomainXmlReader(URL domainXml, String serverName, Logger theLogger,
            XMLInputFactory theXif) throws XMLStreamException {
        try {
            xif = theXif;
            logger = theLogger;
            stream = domainXml.openStream();
            setParent(xif.createXMLStreamReader(domainXml.toExternalForm(), stream));
            this.domainXml = domainXml;
            this.serverName = serverName;
        }
        catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    @Override
    public void close() throws XMLStreamException {
        super.close();
        try {
            stream.close();
        }
        catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    @Override
    boolean filterOut() throws XMLStreamException {
        checkConfigRef(getParent());

        if (getLocalName().equals("config")) {
            if (configName == null) {
                // we've hit <config> element before we've seen <server>,
                // so we still don't know which config element to look for.
                // For us to make this work, we need to parse the file twice
                parse2ndTime();
                assert configName != null;
            }

            // if <config name="..."> didn't match what we are looking for, filter it out
            if (configName.equals(getAttributeValue(null, "name"))) {
                foundConfig = true;
                return false;
            }
            
            
            
            
            return true;
            //return false;   // TODO FIXME




        }

        // we'll read everything else
        return false;
    }

    String getConfigName() {
        return configName;
    }

    boolean foundConfig() {
        return foundConfig;
    }

    ///////////////////////////////////////////////////////////////////////////
    /////////   Everything below here is private  /////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    private void parse2ndTime() throws XMLStreamException {
        logger.info("Forced to parse " + domainXml + " twice because we didn't see <server> before <config>");
        try {
            InputStream stream2 = domainXml.openStream();
            XMLStreamReader xsr = xif.createXMLStreamReader(domainXml.toExternalForm(), stream2);
            while (configName == null) {
                switch (xsr.next()) {
                    case XMLStreamConstants.START_ELEMENT:
                        checkConfigRef(xsr);
                        break;
                    case XMLStreamConstants.END_DOCUMENT:
                        break;
                }
            }
            xsr.close();
            stream2.close();
            if (configName == null)
                // TODO -- fixme FIXME
                throw new RuntimeException(domainXml + " contains no <server> element that matches " + serverName);
        }
        catch (IOException e) {
            throw new XMLStreamException("Failed to parse " + domainXml, e);
        }
    }

    private void checkConfigRef(XMLStreamReader xsr) {
        String ln = xsr.getLocalName();

        if (configName == null && ln.equals("server")) {
            // is this our <server> element?
            if (serverName.equals(xsr.getAttributeValue(null, "name"))) {
                configName = xsr.getAttributeValue(null, "config-ref");
                if (configName == null)
                    throw new RuntimeException("<server> element is missing @config-ref at " + formatLocation(xsr));
            }
        }
    }

    /**
     * Convenience method to return a human-readable location of the parser.
     */
    private String formatLocation(XMLStreamReader xsr) {
        return "line " + xsr.getLocation().getLineNumber() + " at " + xsr.getLocation().getSystemId();
    }
    /**
     * We need to figure out the configuration name from the server name.
     * Once we find that out, it'll be set here.
     */
    private String configName;
    private final URL domainXml;
    private final String serverName;
    private final Logger logger;
    private final XMLInputFactory xif;
    /**
     * If we find a matching config, set to true. Used for error detection in case
     * we don't see any config for us.
     */
    private boolean foundConfig;
    /**
     * Because {@link XMLStreamReader} doesn't close the underlying stream,
     * we need to do it by ourselves. So much for the "easy to use" API.
     */
    private InputStream stream;
    private static class ServerAndConfig {
        String  serverName;
        String  configName;
        boolean found;
    }
}

