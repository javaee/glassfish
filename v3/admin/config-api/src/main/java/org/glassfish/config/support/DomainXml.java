/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
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

import org.glassfish.server.ServerEnvironmentImpl;
import com.sun.enterprise.module.bootstrap.Populator;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.hk2.component.ExistingSingletonInhabitant;
import org.glassfish.api.admin.config.ConfigurationUpgrade;
import org.glassfish.api.admin.ServerEnvironment;
import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
import org.jvnet.hk2.config.ConfigParser;
import org.jvnet.hk2.config.DomDocument;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamConstants;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.URL;


/**
 * Locates and parses the portion of <tt>domain.xml</tt> that we care.
 *
 * @author Jerome Dochez
 * @author Kohsuke Kawaguchi
 */
public abstract class DomainXml implements Populator {

    @Inject
    StartupContext context;

    @Inject
    Logger logger;

    @Inject
    protected Habitat habitat;

    @Inject
    ModulesRegistry registry;

    @Inject
    XMLInputFactory xif;

    @Inject
    ServerEnvironmentImpl env;

    public void run(ConfigParser parser) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Startup class : " + this.getClass().getName());
        }

        habitat.addComponent("parent-class-loader",
                new ExistingSingletonInhabitant<ClassLoader>(ClassLoader.class, registry.getParentClassLoader()));

        try {
            parseDomainXml(parser, getDomainXml(env), env.getInstanceName());
        } catch (IOException e) {
            // TODO: better exception handling scheme
            throw new RuntimeException("Failed to parse domain.xml",e);
        }

        // run the upgrades...
        if ("upgrade".equals(context.getPlatformMainServiceName())) {
            upgrade();
        }

        decorate();
    }

    protected void decorate() {

        Server server = habitat.getComponent(Server.class, env.getInstanceName());
        habitat.addIndex(new ExistingSingletonInhabitant<Server>(server),
                         Server.class.getName(), ServerEnvironment.DEFAULT_INSTANCE_NAME);

        habitat.addIndex(new ExistingSingletonInhabitant<Config>(habitat.getComponent(Config.class, server.getConfigRef())),
                         Config.class.getName(), ServerEnvironment.DEFAULT_INSTANCE_NAME);
        
    }

    protected void upgrade() {
        
        // run the upgrades...
        for (Inhabitant<? extends ConfigurationUpgrade> cu : habitat.getInhabitants(ConfigurationUpgrade.class)) {
            try {
                cu.get(); // run the upgrade                
                Logger.getAnonymousLogger().fine("Successful Upgrade domain.xml with " + cu.getClass());
            } catch (Exception e) {
                Logger.getAnonymousLogger().log(Level.FINE,e.toString(),e);
                Logger.getAnonymousLogger().severe(cu.getClass() + " upgrading domain.xml failed " + e);
            }
        }
    }

    /**
     * Determines the location of <tt>domain.xml</tt> to be parsed.
     */
    protected URL getDomainXml(ServerEnvironmentImpl env) throws IOException {
        return new File(env.getConfigDirPath(), ServerEnvironmentImpl.kConfigXMLFileName).toURI().toURL();
    }


    /**
     * Parses <tt>domain.xml</tt>
     */
    protected void parseDomainXml(ConfigParser parser, final URL domainXml, final String serverName) {
        try {
            DomainXmlReader xsr = new DomainXmlReader(domainXml, serverName);
            parser.parse(xsr, getDomDocument());
            xsr.close();
            if(!xsr.foundConfig)
                throw new RuntimeException("No <config> seen for name="+xsr.configName);
        } catch (XMLStreamException e) {
            // TODO: better exception handling scheme
            throw new RuntimeException("Failed to parse "+domainXml,e);
        }
    }

    protected abstract DomDocument getDomDocument();

    /**
     * {@link XMLStreamReader} that skips irrelvant &lt;config> elements that we shouldn't see.
     */
    private class DomainXmlReader extends XMLStreamReaderFilter {
        /**
         * We need to figure out the configuration name from the server name.
         * Once we find that out, it'll be set here.
         */
        private String configName;
        private final URL domainXml;
        private final String serverName;

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

        public DomainXmlReader(URL domainXml, String serverName) throws XMLStreamException {
            try {
                stream = domainXml.openStream();
                setParent(xif.createXMLStreamReader(domainXml.toExternalForm(), stream));
                this.domainXml = domainXml;
                this.serverName = serverName;
            } catch (IOException e) {
                throw new XMLStreamException(e);
            }
        }

        public void close() throws XMLStreamException {
            super.close();
            try {
                stream.close();
            } catch (IOException e) {
                throw new XMLStreamException(e);
            }
        }

        boolean filterOut() throws XMLStreamException {
            checkConfigRef(getParent());

            if(getLocalName().equals("config")) {
                if(configName==null) {
                    // we've hit <config> element before we've seen <server>,
                    // so we still don't know which config element to look for.
                    // For us to make this work, we need to parse the file twice
                    parse2ndTime();
                    assert configName!=null;
                }

                // if <config name="..."> didn't match what we are looking for, filter it out
                if(configName.equals(getAttributeValue(null, "name"))) {
                    foundConfig = true;
                    return false;
                }
                return true;
            }

            // we'll read everything else
            return false;
        }

        private void parse2ndTime() throws XMLStreamException {
            logger.info("Forced to parse "+ domainXml +" twice because we didn't see <server> before <config>");
            try {
                InputStream stream = domainXml.openStream();
                XMLStreamReader xsr = xif.createXMLStreamReader(domainXml.toExternalForm(),stream);
                while(configName==null) {
                    switch(xsr.next()) {
                    case XMLStreamConstants.START_ELEMENT:
                        checkConfigRef(xsr);
                        break;
                    case XMLStreamConstants.END_DOCUMENT:
                        break;
                    }
                }
                xsr.close();
                stream.close();
                if(configName==null)
                    throw new RuntimeException(domainXml +" contains no <server> element that matches "+ serverName);
            } catch (IOException e) {
                throw new XMLStreamException("Failed to parse "+domainXml,e);
            }
        }

        private void checkConfigRef(XMLStreamReader xsr) {
            String ln = xsr.getLocalName();

            if(configName==null && ln.equals("server")) {
                // is this our <server> element?
                if(serverName.equals(xsr.getAttributeValue(null, "name"))) {
                    configName = xsr.getAttributeValue(null,"config-ref");
                    if(configName==null)
                        throw new RuntimeException("<server> element is missing @config-ref at "+formatLocation(xsr));
                }
            }
        }

        /**
         * Convenience method to return a human-readable location of the parser.
         */
        private String formatLocation(XMLStreamReader xsr) {
            return "line "+xsr.getLocation().getLineNumber()+" at "+xsr.getLocation().getSystemId();
        }
        
    }
}
