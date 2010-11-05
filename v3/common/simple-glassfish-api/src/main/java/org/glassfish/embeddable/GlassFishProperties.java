/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.embeddable;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Encapsulates the set of properties required to create a new GlassFish instance.
 * <p/>
 * <p/>Eg.., GlassFishRuntime.bootstrap(new BootstrapProperties()).newGlassFish(<b>new GlassFishProperties()</b>);
 *
 * @author bhavanishankar@dev.java.net
 * @author Prasad.Subramanian@Sun.COM
 */
public class GlassFishProperties {

    private Properties gfProperties;

    /**
     * Create GlassFishProperties with default properties.
     */
    public GlassFishProperties() {
        gfProperties = new Properties();
    }

    /**
     * Create GlassFishProperties with custom properties.
     * <p/>
     * <p/>Custom properties can include values for all or some of the keys
     * defined as constants in this class. Eg., a value for com.sun.aas.instanceRoot
     * can be included in the custom properties.
     * <p/>
     * <p/>Custom properties can also include additional properties which are required
     * for the plugged in {@link GlassFishRuntime} (if any)
     *
     * @param props Properties object which will back this GlassFishProperties object.
     */
    public GlassFishProperties(Properties props) {
        gfProperties = props;
    }

    /**
     * Get the underlying Properties object which backs this GlassFishProperties.
     * <p/>
     * <p/> If getProperties().setProperty(key,value) is called, then it will
     * add a property to this GlassFishProperties.
     *
     * @return The Properties object that is backing this GlassFishProperties.
     */
    public Properties getProperties() {
        return gfProperties;
    }

    /**
     * Set any custom glassfish property. May be required for the plugged in
     * {@link GlassFishRuntime} (if any)
     *
     * @param key   the key to be placed into this glassfish properties.
     * @param value the value corresponding to the key.
     */
    public void setProperty(String key, String value) {
        gfProperties.setProperty(key, value);
    }

    /**
     * Optionally set the instance root (aka domain dir) using which the
     * GlassFish should run.
     * <p/>
     * <p/> Make sure to specify a valid GlassFish instance directory
     * (eg., GF_INSTALL_DIR/domains/domain1).
     * <p/>
     * <p/> By default, the config/domain.xml at the specified instance root is operated in
     * read only mode. To writeback changes to it, call
     * {@link #setConfigFileReadOnly(boolean)} by passing 'false'
     * <p/>
     * <p/>If the instance root is not specified, then a small sized temporary
     * instance directory is created in the current directory. The temporary
     * instance directory will get deleted when the glassfish.dispose() is called.
     *
     * @param instanceRoot Location of the instance root.
     * @return This object after setting the instance root.
     */
    public void setInstanceRoot(String instanceRoot) {
        gfProperties.setProperty(INSTANCE_ROOT_PROP_NAME, instanceRoot);
    }

    /**
     * Get the location instance root set using {@link #setInstanceRoot(String)}
     *
     * @return Location of instance root set using {@link #setInstanceRoot(String)}
     */
    public String getInstanceRoot() {
        return gfProperties.getProperty(INSTANCE_ROOT_PROP_NAME);
    }

    /**
     * Optionally set the location of configuration file (i.e., domain.xml) using
     * which the GlassFish should run.
     * <p/>
     * Unless specified, the configuration file is operated on read only mode.
     * To writeback any changes, call {@link #setConfigFileReadOnly(boolean)} with 'false'.
     *
     * @param configFileURI Location of configuration file.
     */
    public void setConfigFileURI(String configFileURI) {
        gfProperties.setProperty(CONFIG_FILE_URI_PROP_NAME, configFileURI);
    }


    /**
     * Get the configurationFileURI set using {@link #setConfigFileURI(String)}
     *
     * @return The configurationFileURI set using {@link #setConfigFileURI(String)}
     */
    public String getConfigFileURI() {
        return gfProperties.getProperty(CONFIG_FILE_URI_PROP_NAME);
    }

    /**
     * Check whether the specified configuration file or config/domain.xml at
     * the specified instance root is operated read only or not.
     *
     * @return true if the specified configurator file or config/domain.xml at the
     *         specified instance root remains unchanged when the glassfish runs, false otherwise.
     */
    public boolean isConfigFileReadOnly() {
        return Boolean.valueOf(gfProperties.getProperty(
                CONFIG_FILE_READ_ONLY, "true"));
    }

    /**
     * Mention whether or not the GlassFish should writeback any changes to specified
     * configuration file or config/domain.xml at the specified instance root.
     * <p/>
     * <p/> By default readOnly is true.
     *
     * @param readOnly false to writeback any changes.
     */
    public void setConfigFileReadOnly(boolean readOnly) {
        gfProperties.setProperty(CONFIG_FILE_READ_ONLY,
                Boolean.toString(readOnly));
    }

    /**
     * Optionally specify the protocol and port GlassFish should listen on.
     * <p/>
     * Currently supported values for protocol are 'http' or 'https'.
     * <p/>
     * Eg., To make GlassFish listen on 8080 http port:
     * <pre>
     *      setPort("http", 8080);
     * </pre>
     * <p/>
     * If the specified port is invalid, then setPort returns with no-op.
     *
     * @param protocol Name of the protocol. http or https
     * @param port Port number
     * 
     * @return This object after setting the port.
     */
    public void setPort(String protocol, int port) {
        if (protocol != null) {
            String key = nameMap.get(protocol);
            if (key != null) {
                gfProperties.setProperty(key, Integer.toString(port));
            }
        }
    }

    /**
     * Get the port number set using {@link #setPort(String, int)}
     *
     * @param protocol Name of the protocol
     * @return Port number which was set using {@link #setPort(String, int)}.
     *         -1 if it was not set previously.
     */
    public int getPort(String protocol) {
        int port = -1;
        if (protocol != null) {
            String key = nameMap.get(protocol);
            if (key != null) {
                String portStr = gfProperties.getProperty(key);
                try {
                    port = Integer.parseInt(portStr);
                } catch (NumberFormatException nfe) {
                    // ignore and return -1;
                }
            }
        }
        return port;
    }

    // PRIVATE constants.
    // Key for specifying which instance root (aka domain dir) GlassFish should run with.
    private final static String INSTANCE_ROOT_PROP_NAME =
            "com.sun.aas.instanceRoot";
    // Key for specifying which configuration file (domain.xml) GlassFish should run with.
    private static final String CONFIG_FILE_URI_PROP_NAME =
            "org.glassfish.embeddable.configFileURI";
    // Key for specifying whether the specified configuration file (domain.xml) or config/domain.xml
    // at the user specified instanceRoot should be operated by GlassFish in read only mode or not.
    private static final String CONFIG_FILE_READ_ONLY =
            "org.glassfish.embeddable.configFileReadOnly";
    // Maps the simple user specified names to the internal fully qualified names.
    private static final Map<String, String> nameMap = new HashMap();
    static {
        nameMap.put("http", "org.glassfish.embeddable.httpPort");
        nameMap.put("https", "org.glassfish.embeddable.httpsPort");
    }


}
