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

package org.glassfish.embeddable;

import java.util.Properties;

/**
 * Encapsulates the set of properties required to bootstrap GlassFishRuntime.
 * <p/>
 * <p/>Eg.., GlassFishRuntime.bootstrap(new BootstrapProperties());
 *
 * @author Prasad.Subramanian@Sun.COM
 * @author bhavanishankar@dev.java.net
 */
public class BootstrapProperties {

    private Properties properties;

    /**
     * Create BootstrapProperties with default properties.
     */
    public BootstrapProperties() {
        properties = new Properties();
    }

    /**
     * Create BootstrapProperties with custom properties.
     * <p/>
     * <p/>Custom properties can include GlassFish_Platform,
     * com.sun.aas.installRoot, com.sun.aas.installRootURI
     * <p/>
     * <p/>Custom properties can also include additional properties which are required
     * for the plugged in {@link org.glassfish.embeddable.spi.RuntimeBuilder} (if any)
     *
     * @param props Properties object which will back this BootstrapProperties object.
     */
    public BootstrapProperties(Properties props) {
        this.properties = props;
    }

    /**
     * Get the underlying Properties object which backs this BootstrapProperties.
     * <p/>
     * <p/> If getProperties().setProperty(key,value) is called, then it will
     * add a property to this bootstrap properties.
     *
     * @return The Properties object that is backing this BootstrapProperties.
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Set any custom bootstrap property. May be required for the plugged in
     * {@link org.glassfish.embeddable.spi.RuntimeBuilder} (if any)
     *
     * @param key   the key to be placed into this bootstrap properties.
     * @param value the value corresponding to the key.
     * @return This object after setting the custom property.
     */
    public BootstrapProperties setProperty(String key, String value) {
        properties.setProperty(key, value);
        return this;
    }

    /**
     * Optionally set the platform on which the GlassFish should run.
     * <p/>
     * <p/> Eg., setPlatform({@link BootstrapProperties.Platform#Felix});
     * <p/>
     * <p/> Default is {@link BootstrapProperties.Platform#Static}
     *
     * @param platform Platform
     * @return This object after setting the platform.
     */
    public BootstrapProperties setPlatform(Platform platform) {
        properties.setProperty(PLATFORM_PROPERTY_KEY, platform.toString());
        return this;
    }

    /**
     * Gets the name of the platform with which GlassFish is running or will run.
     *
     * @return Name of the platform.
     */
    public Platform getPlatform() {
        String platform = properties.getProperty(PLATFORM_PROPERTY_KEY);
        return platform == null || platform.trim().length() == 0 ? Platform.Static :
                Platform.valueOf(platform.trim());
    }

    /**
     * Optionally set the installation root using which the GlassFish should run.
     *
     * @param installRoot Location of installation root.
     * @return This object after setting the installation root.
     */
    public BootstrapProperties setInstallRoot(String installRoot) {
        properties.setProperty(INSTALL_ROOT_PROP_NAME, installRoot);
        return this;
    }

    /**
     * Get the location installation root set using {@link #setInstallRoot}
     *
     * @return Location of installation root set using {@link #setInstallRoot}
     */
    public String getInstallRoot() {
        return properties.getProperty(INSTALL_ROOT_PROP_NAME);
    }

    /**
     * Various platforms on which GlassFish could run.
     */
    public enum Platform {
        /**
         * Felix OSGi platform
         */
        Felix,

        /**
         * Equinox OSGi platform
         */
        Equinox,

        /**
         * Knopflerfish OSGi platform
         */
        Knopflerfish,

        /**
         * Generic OSGi R4.2 or higher platform.
         * When this is chosen, we expect the framework to be set up in launcher classloader by user.
         */
        GenericOSGi,

        /**
         * Proprietary non-modular hk2 module system
         */
        Static
    }

    // PRIVATE constants.
    // Key for specifying which platform the GlassFish should run with.
    private final static String PLATFORM_PROPERTY_KEY = "GlassFish_Platform";
    // Key for specifying which installation root the GlassFish should run with.
    private static final String INSTALL_ROOT_PROP_NAME = "com.sun.aas.installRoot";
}
