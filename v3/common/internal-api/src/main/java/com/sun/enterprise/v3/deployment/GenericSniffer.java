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
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.Module;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.jvnet.hk2.annotations.Inject;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.lang.annotation.Annotation;

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
            try {
                if (location.exists(appStigma)) {
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
     *
     * This method returns a {@link ModuleDefinition} for the module containing
     * the core implementation of the container. That means that this module
     * will be locked as long as there is at least one module loaded in the
     * associated container.
     *
     * @param containerHome is where the container implementation resides
     * @param logger the logger to use
     * @return the module definition of the core container implementation.
     *
     * @throws java.io.IOException exception if something goes sour
     */
    public Module[] setup(String containerHome, Logger logger) throws IOException {
       return null;
    }

    /**
     * Tears down a container, remove all imported libraries from the module
     * subsystem.
     * 
     */
    public void tearDown() {
    }

    /**
     * Returns the list of annotations types that this sniffer is interested in.
     * If an application bundle contains at least one class annotated with
     * one of the returned annotations, the deployment process will not
     * call the handles method but will invoke the containers deployers as if
     * the handles method had been called and returned true.
     *
     * @return list of annotations this sniffer is interested in.
     */
    public Class<? extends Annotation>[] getAnnotationTypes() {
        return new Class[0];
    }

    /**
     * @return whether this sniffer should be visible to user
     *
     */
    public boolean isUserVisible() {
        return false;
    }
}
