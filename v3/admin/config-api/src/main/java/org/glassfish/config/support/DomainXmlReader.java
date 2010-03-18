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
import java.util.*;
import java.util.logging.*;
import javax.xml.stream.*;
import org.jvnet.hk2.annotations.Inject;

/**
 * {@link XMLStreamReader} that skips irrelvant &lt;config> elements that we shouldn't see.
 * @author Jerome Dochez
 * @author Kohsuke Kawaguchi
 * @author Byron Nevins
 * Byron Nevins == wbn:  This class was initially an inner class of DomainXml
 * in this package.  It was pulled into its own stand-alone class on
 * March 16,2010 in preparation for new clustering requirements.
 */
class DomainXmlReader extends XMLStreamReaderFilter {

    /**
     * put ALL servers and configs into the Habitat
     * @param domainXml
     * @throws XMLStreamException
     */
    DomainXmlReader(URL theDomainXml, XMLInputFactory theXif, Logger theLogger) throws XMLStreamException {
        domainXml = theDomainXml;
        serverName = null;
        onlyOneConfig = false;
        logger = theLogger;
        xif = theXif;
        try {
            stream = domainXml.openStream();
            setParent(xif.createXMLStreamReader(domainXml.toExternalForm(), stream));
        }
        catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    /**
     * Put just the one server and config into the habitat.
     * @param domainXml
     * @param serverName
     * @throws XMLStreamException
     */
    DomainXmlReader(URL theDomainXml, String theServerName,
            XMLInputFactory theXif, Logger theLogger) throws XMLStreamException {
        domainXml = theDomainXml;
        serverName = theServerName;
        onlyOneConfig = true;
        logger = theLogger;
        xif = theXif;
        try {
            stream = domainXml.openStream();
            setParent(xif.createXMLStreamReader(domainXml.toExternalForm(), stream));
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

        // The only element we ever filter out is <config>
        if (!getLocalName().equals("config"))
            return false;

        // note that all <server> are children of <servers>.  Similarly all
        // <config> are children of <configs>.  Therefore if the one-and-only config
        // name is missing then <configs> must be before <servers> in domain.xml and we
        // need a second parse RIGHT NOW.
        // On the other hand if we want ALL configs -- then we already have them and
        // we never need a reparse!
        if (onlyOneConfig && configName == null) {
            parse2ndTime();

            // should be impossible!  parse2ndTime already threw a RuntimeException
            if (configName == null)
                throw new XMLStreamException("Failed to parse " + domainXml);
        }

        String theConfigName = getAttributeValue(null, "name");

        if (onlyOneConfig) {
            if (configName.equals(theConfigName)) {
                foundConfig = true;
                return false;
            }
            else
                return true;    // filter it out!!
        }

        // unknown config -- don't filter it because onlyOneConfig is false...
        // we need to add it to the list in case a server element appears later 
        // and needs to access it.
        setConfigName(theConfigName);
        return false;
    }

    String getConfigName() {
        return configName;
    }

    /**
     * Report on whether parsing was a success or not
     * @return a String error message if there was an error else return null for all-ok
     */
    String configWasFound() {
        // very inefficient string handling but it is of no concern because if
        // we create the strings we are in the midst of a FATAL error...
        final String m1 = "Could not locate the config element: ";
        final String m2 = " for the server: ";
        final String m3 = "\n";

        if (onlyOneConfig) {
            if (foundConfig)
                return null;
            else
                return m1 + configName + m2 + serverName + m3;
        }

        // we are interested in ALL configs and ALL servers
        // Let's be thorough and document ALL missing elements, not just the
        // first one we find...
        String msg = "";

        for (ServerAndConfig sac : serverCheckList) {
            // it is legal to have config with no associated server
            // vice-versa is an error
            if (sac.serverName == null)
                continue;

            if (!sac.foundConfig)
                msg += m1 + sac.configName + m2 + sac.serverName;
        }
        if (msg.length() > 0)
            return msg;

        return null;
    }

    ///////////////////////////////////////////////////////////////////////////
    /////////   Everything below here is private  /////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    /**
     * Note that this method will only be called when starting an instance, never for DAS
     * @throws XMLStreamException if domain.xml is miissing the needed config element
     */
    private void parse2ndTime() throws XMLStreamException {
        if (!onlyOneConfig)
            throw new RuntimeException("Programmer Error.  Called " + getClass().getName()
                    + ".parse2ndTime() for DAS.  This is illegal.");

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
                throw new RuntimeException(domainXml + " contains no <config> element that matches the config-ref in the server: " + serverName);
        }
        catch (IOException e) {
            throw new XMLStreamException("Failed to parse " + domainXml, e);
        }
    }

    private void checkConfigRef(XMLStreamReader xsr) {
        // fairly complicated looking logic below.  The reason is to save time and return
        // ASAP instead of wasting time parsing for no reason.  Also more readable!

        String elementName = xsr.getLocalName();

        // we only care about <server>
        if (!elementName.equals("server"))
            return;

        // if we are only accepting one config and we already have the configName
        // set then that means this <server> element is not interesting and we
        // don't care about its config.
        if (onlyOneConfig && configName != null)
            return;

        // we still have a bunch of possibilities!  For each possibility we need
        // the server name so let's fetch it...
        String theServerName = xsr.getAttributeValue(null, "name");

        if (theServerName == null)
            throw new RuntimeException("<server> element is missing @name at " + formatLocation(xsr));

        // if it is the wrong server, go away...
        if (onlyOneConfig && !serverName.equals(theServerName))
            return;

        // OK -- we now need to get the config-ref name...
        String theConfigName = xsr.getAttributeValue(null, "config-ref");

        if (theConfigName == null)
            throw new RuntimeException("<server> element is missing @config-ref at " + formatLocation(xsr));

        // Whew!

        if (onlyOneConfig)
            configName = theConfigName;
        else
            setServerName(theServerName, theConfigName);
    }

    /**
     * We never need a second parse if we are collecting ALL configs and servers.
     * So if we find config before server we need to create a ServerAndConfig and
     * leave server null and set the config name.
     * Note that if we have more than one server pointing at the same config -- 
     * everything will still be ok - we simply overwrite the first server with the 
     * second server, etc.  
     * This method will either create a new ServerAndConfig or set the server
     * name inside an existing one.
     * @param theConfigName
     * @return
     */
    private void setServerName(String theServerName, String theConfigName) {
        if (theServerName == null || theConfigName == null)
            throw new IllegalArgumentException("null args");

        if (onlyOneConfig)
            throw new RuntimeException("Internal Error: Can not call "
                    + "setServerName if onlyOneConfig is set.");

        // is the config name already saved?
        for (ServerAndConfig sac : serverCheckList) {
            if (theConfigName.equals(sac.configName)) {
                sac.serverName = theServerName;
                sac.foundConfig = true;
                return;
            }
        }

        ServerAndConfig sac = new ServerAndConfig();
        sac.serverName = theServerName;
        sac.configName = theConfigName;
        sac.foundConfig = false; // unnecessary but why not?
        serverCheckList.add(sac);
    }

    private void setConfigName(String theConfigName) {
        if (theConfigName == null)
            throw new IllegalArgumentException("null arg");

        for (ServerAndConfig sac : serverCheckList) {
            if (theConfigName.equals(sac.configName)) {
                sac.foundConfig = true;
                return;
            }
        }
        // it is not already in there -- either it is a config with no associated
        // server or the server has not appeared yet.
        ServerAndConfig sac = new ServerAndConfig();
        sac.configName = theConfigName;
        serverCheckList.add(sac);

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
    /**
     * I don't want to see if serverName is not null over and over so we use this
     * convenience boolean
     */
    private final boolean onlyOneConfig;
    private Logger logger;
    private XMLInputFactory xif;
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
    /**
     * Note that &lt;server&gt; refers to a &lt;config&g; but not vice-versa
     */
    private final List<ServerAndConfig> serverCheckList = new LinkedList<ServerAndConfig>();

    private static class ServerAndConfig {

        String serverName;
        String configName;
        boolean foundConfig;
    }
}

