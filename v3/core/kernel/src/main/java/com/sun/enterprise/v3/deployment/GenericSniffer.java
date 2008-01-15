/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.v3.deployment;

import com.sun.enterprise.module.ModulesRegistry;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.jvnet.hk2.annotations.Inject;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Generic implementation of the Sniffer service that can be programmatically instantiated
 *
 * @author Jerome Dochez
 */
public abstract class GenericSniffer implements Sniffer {

    @Inject
    protected ModulesRegistry modulesRegistry;

    final private String containerName;
    final private String appStigma;
    final private String urlPattern;

    public GenericSniffer(String containerName, String appStigma, String urlPattern) {
        this.containerName = containerName;
        this.appStigma = appStigma;
        this.urlPattern = urlPattern;
    }

    /**
     * Returns true if the passed file or directory is recognized by this
     * instance.
     *
     * @param location the file or directory to explore
     * @param loader class loader for this application
     * @return true if this sniffer handles this application type
     */
    public boolean handles(ReadableArchive location, ClassLoader loader) {
        if (appStigma != null) {
            InputStream is;
            try {
                is = location.getEntry(appStigma);
                if (is != null) {
                    is.close();
                    return true;
                }
            } catch (IOException e) {
                // ignore
            }
        }
        return false;
    }

    /**
     * Returns the pattern to apply against the request URL
     * If the pattern matches the URL, the service method of the associated
     * container will be invoked
     *
     * @return pattern instance
     */
    public Pattern getURLPattern() {
        if (urlPattern!=null) {
            return Pattern.compile(urlPattern);
        } else {
            return null;
        }
    }

    /**
     * Returns the container name associated with this sniffer
     *
     * @return the container name
     */
    public String getModuleType() {
        return containerName;
    }

    /**
     * Sets up the container libraries so that any imported bundle from the
     * connector jar file will now be known to the module subsystem
     * @param containerHome is where the container implementation resides
     * @param logger the logger to use
     * @throws IOException exception if something goes sour
     */
    public void setup(String containerHome, Logger logger) throws IOException {
    }

    /**
     * Tears down a container, remove all imported libraries from the module
     * subsystem.
     * 
     */
    public void tearDown() {
    }
}
