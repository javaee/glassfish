/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008-2011 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.config.support;

import com.sun.enterprise.module.bootstrap.BootException;
import com.sun.enterprise.universal.Duration;
import com.sun.enterprise.universal.NanoDuration;
import com.sun.enterprise.util.EarlyLogger;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.api.admin.config.ConfigurationCleanup;
import org.glassfish.server.ServerEnvironmentImpl;
import com.sun.enterprise.module.bootstrap.Populator;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
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
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.URL;
import org.glassfish.api.admin.RuntimeType;

/**
 * Locates and parses the portion of <tt>domain.xml</tt> that we care.
 *
 * @author Jerome Dochez
 * @author Kohsuke Kawaguchi
 * @author Byron Nevins
 */
public abstract class DomainXml implements Populator {

    @Inject
    StartupContext context;
    @Inject
    protected Habitat habitat;
    @Inject
    ModulesRegistry registry;
    @Inject
    XMLInputFactory xif;
    @Inject
    ServerEnvironmentImpl env;
    @Inject
    ConfigurationAccess configAccess;

    final static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(DomainXml.class);    

    @Override
    public void run(ConfigParser parser) throws BootException {
        EarlyLogger.add(Level.FINE, "Startup class : " + this.getClass().getName());

        habitat.addComponent("parent-class-loader",
                new ExistingSingletonInhabitant<ClassLoader>(ClassLoader.class, registry.getParentClassLoader()));

        try {
            parseDomainXml(parser, getDomainXml(env), env.getInstanceName());
        }
        catch (IOException e) {
            throw new BootException(localStrings.getLocalString("ConfigParsingFailed","Failed to parse domain.xml"), e);
        }

        // run the upgrades...
        if ("upgrade".equals(context.getPlatformMainServiceName())) {
            upgrade();
        }

        // run the cleanup.
        for (Inhabitant<? extends ConfigurationCleanup> cc : habitat.getInhabitants(ConfigurationCleanup.class)) {
            try {
                cc.get(); // run the upgrade
                EarlyLogger.add(Level.FINE, "Successful cleaned domain.xml with " + cc.getClass());
            }
            catch (Exception e) {
                EarlyLogger.add(Level.FINE, e.toString() + e);
                EarlyLogger.add(Level.SEVERE, cc.getClass() + " cleaning domain.xml failed " + e);
            }
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
                EarlyLogger.add(Level.FINE, "Successful Upgrade domain.xml with " + cu.getClass());
            }
            catch (Exception e) {
                EarlyLogger.add(Level.FINE, e.toString() + e);
                EarlyLogger.add(Level.SEVERE, cu.getClass() + " upgrading domain.xml failed " + e);
            }
        }
    }

    /**
     * Determines the location of <tt>domain.xml</tt> to be parsed.
     */
    protected URL getDomainXml(ServerEnvironmentImpl env) throws IOException {
        File domainXml = new File(env.getConfigDirPath(), ServerEnvironmentImpl.kConfigXMLFileName);
        if (domainXml.exists() && domainXml.length()>0) {
            return domainXml.toURI().toURL();
        } else {

            EarlyLogger.add(Level.SEVERE,
                    localStrings.getLocalString("NoConfigFile",
                            "{0} does not exist or is empty, will use backup",
                            domainXml.getAbsolutePath()));
            domainXml = new File(env.getConfigDirPath(), ServerEnvironmentImpl.kConfigXMLFileNameBackup);
            if (domainXml.exists() && domainXml.length()>0) {
                return domainXml.toURI().toURL();
            }
            EarlyLogger.add(Level.SEVERE,
                    localStrings.getLocalString("NoBackupFile",
                            "{0} does not exist or is empty, cannot use backup",
                            domainXml.getAbsolutePath()));
        }
        throw new IOException(localStrings.getLocalString("NoUsableConfigFile",
                            "No usable configuration file at {0}",
                            env.getConfigDirPath()));
    }

    /**
     * Parses <tt>domain.xml</tt>
     */
    protected void parseDomainXml(ConfigParser parser, final URL domainXml, final String serverName) {
        long startNano = System.nanoTime();

        try {
            ServerReaderFilter xsr = null;

            if (env.getRuntimeType() == RuntimeType.DAS || env.getRuntimeType() == RuntimeType.EMBEDDED)
                xsr = new DasReaderFilter(habitat, domainXml, xif);
            else if (env.getRuntimeType() == RuntimeType.INSTANCE)
                xsr = new InstanceReaderFilter(env.getInstanceName(), habitat, domainXml, xif);
            else
                throw new RuntimeException("Internal Error: Unknown server type: "
                        + env.getRuntimeType());

            Lock lock = null;
            try {
                // lock the domain.xml for reading if not embedded
                try {
                    lock = configAccess.accessRead();
                } catch(Exception e) {
                    // ignore
                }
                parser.parse(xsr, getDomDocument());
                xsr.close();
            } finally {
                if (lock!=null) {
                    lock.unlock();
                }
            }
            String errorMessage = xsr.configWasFound();

            if (errorMessage != null)
                EarlyLogger.add(Level.WARNING, errorMessage);
        }
        catch (Exception e) {
            if (e instanceof RuntimeException)
                throw (RuntimeException) e;
            else
                throw new RuntimeException("Fatal Error.  Unable to parse " + domainXml, e);
        }
        EarlyLogger.add(Level.FINE, strings.get("time", new NanoDuration(System.nanoTime() - startNano).toString()));
    }

    protected abstract DomDocument getDomDocument();
    private final static LocalStringsImpl strings = new LocalStringsImpl(DomainXml.class);
}
