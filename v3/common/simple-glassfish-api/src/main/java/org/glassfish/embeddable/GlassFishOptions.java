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
 * Set of options to be passed while creating a GlassFish instance. This utility class
 * can be passed while creating a GlassFish using GlassFishRuntime.newGlassFish(..)
 * @author Prasad.Subramanian@Sun.COM
 */
public class GlassFishOptions {

    private Properties gfProperties;

    /**
     * Constructor that creates an empty GlassFishOptions
     */
    public GlassFishOptions() {
        gfProperties = new Properties();
    }

    /**
     * Creates a GlassFishOptions wired with a set of propeties passed
     * @param props
     */
    public GlassFishOptions(Properties props) {
        gfProperties = props;
    }

    /**
     * Returns a Properties object with all the options that were added to it
     * @return Properties
     */
    public Properties getAllOptions() {
        return gfProperties;
    }

    /**
     * Getter for instance root
     * @return String representing instance root
     */
    public String getInstanceRoot() {
        return gfProperties.getProperty(GlassFishConstants.INSTANCE_ROOT_PROP_NAME);
    }
    /**
     * Setter for the instance root of the GlassFish instance
     * @param instanceRoot
     * @return
     */
    public GlassFishOptions setInstanceRoot(String instanceRoot) {
        gfProperties.setProperty(GlassFishConstants.INSTANCE_ROOT_PROP_NAME, instanceRoot);
        return this;
    }

    /**
     * Getter for instance root uri
     * @return String representing the instance root uri
     */
    public String getInstanceRootUri() {
        return gfProperties.getProperty(GlassFishConstants.INSTANCE_ROOT_URI_PROP_NAME);
    }

    /**
     * Setter for the instance root uri for the GlassFish instance
     * @param instanceRootUri
     * @return
     */
    public GlassFishOptions setInstanceRootUri(String instanceRootUri) {
        gfProperties.setProperty(GlassFishConstants.INSTANCE_ROOT_URI_PROP_NAME, instanceRootUri);
        return this;
    }

    /**
     * Getter for the config file URI
     * @return String representing the config file uri
     */
    public String getConfigFileUri() {
        return gfProperties.getProperty(GlassFishConstants.CONFIG_FILE_URI_PROP_NAME);
    }

    /**
     * Setter for the uri of the config file for the GlassFish instance
     * This is useful if the user wants to provide a custom domain.xml file
     * @param configFileUri
     * @return
     */
    public GlassFishOptions setConfigFileUri(String configFileUri) {
        gfProperties.setProperty(GlassFishConstants.CONFIG_FILE_URI_PROP_NAME, configFileUri);
        return this;
    }

    public boolean getConfigFileReadOnly() {
        return Boolean.valueOf(gfProperties.getProperty(
                GlassFishConstants.CONFIG_FILE_READ_ONLY, "true"));
    }

    public void setConfigFileReadOnly(boolean readOnly) {
        gfProperties.setProperty(GlassFishConstants.CONFIG_FILE_READ_ONLY,
                Boolean.toString(readOnly));
    }
}
