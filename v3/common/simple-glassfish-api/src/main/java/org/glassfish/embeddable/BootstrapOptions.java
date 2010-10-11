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
 * The set of options that are passed to a GlassFish Runtime, when bootstrapping
 * Typically a user could also specify a set of properties as a Properties object
 * as an argument to the constructor
 * @author Prasad.Subramanian@Sun.COM
 */
public class BootstrapOptions {

    private Properties bsProperties;

    /**
     * Constructor that creates an empty BootstrapOptions
     */
    public BootstrapOptions() {
        bsProperties = new Properties();
    }

    /**
     * Creates a BootstrapOptions wired with a set of propeties passed
     * @param props
     */
    public BootstrapOptions(Properties props) {
        this.bsProperties = props;
    }

    /**
     * Returns a Properties object with all the options that were added to it
     * @return Properties
     */
    public Properties getAllOptions() {
        return bsProperties;
    }

    /**
     * Setter for the Platform Property
     * @param platformProperty
     * @return BootStrapOptions
     */
    public BootstrapOptions setPlatformProperty(String platformProperty) {
        bsProperties.setProperty(BootstrapConstants.PLATFORM_PROPERTY_KEY, platformProperty);
        return this;

    }

    /**
     * Getter for the Platform Property
     * @return String
     */
    public String getPlatformProperty() {
        return bsProperties.getProperty(BootstrapConstants.PLATFORM_PROPERTY_KEY);
    }

    /**
     * Setter for the Install Root of the GlassFish installation
     * @param installRoot
     * @return BootStrapOptions
     */
    public BootstrapOptions setInstallRoot(String installRoot) {
        bsProperties.setProperty(BootstrapConstants.INSTALL_ROOT_PROP_NAME, installRoot);
        return this;
    }

    /**
     * Getter for install root
     * @return String representing the install root
     */
    public String getInstallRoot() {
        return bsProperties.getProperty(BootstrapConstants.INSTALL_ROOT_PROP_NAME);
    }

    /**
     * Setter for the Install Root of the GlassFish installation as a URI
     * @param installRootUri
     * @return BootStrapOptions 
     */
    public BootstrapOptions setInstallRootUri(String installRootUri) {
        bsProperties.setProperty(BootstrapConstants.INSTALL_ROOT_URI_PROP_NAME, installRootUri);
        return this;
    }

    /**
     * Getter for the install root uri
     * @return String representing the install root uri
     */
    public String getInstallRootUri() {
        return bsProperties.getProperty(BootstrapConstants.INSTALL_ROOT_URI_PROP_NAME);
    }

}
