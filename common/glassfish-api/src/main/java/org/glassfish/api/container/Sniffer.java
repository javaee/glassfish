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
 * Header Notice in eokach file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.glassfish.api.container;

import org.glassfish.api.deployment.archive.ReadableArchive;
import org.jvnet.hk2.annotations.Contract;

import java.io.IOException;
import java.util.logging.Logger;
import java.lang.annotation.Annotation;

import com.sun.enterprise.module.Module;
import java.util.Map;

/**
 * A sniffer implementation is responsible for identifying a particular
 * application type and/or a particular file type.
 *
 * <p>
 * For clients who want to work with Sniffers, see <tt>SnifferManager</tt> in the kernel.
 *
 * @author Jerome Dochez
 */
@Contract
public interface Sniffer {

    /**
     * Returns true if the passed file or directory is recognized by this
     * sniffer.
     * @param source the file or directory abstracted as an archive
     * @param loader if the class loader capable of loading classes and
     * resources from the source archive.
     * @return true if the location is recognized by this sniffer
     */
    public boolean handles(ReadableArchive source, ClassLoader loader);

    /**
     * Returns the array of patterns to apply against the request URL
     * If the pattern matches the URL, the service method of the associated
     * container will be invoked
     * @return array of patterns
     */
    public String[] getURLPatterns();

    /**
     * Returns the list of annotations types that this sniffer is interested in.
     * If an application bundle contains at least one class annotated with
     * one of the returned annotations, the deployment process will not
     * call the handles method but will invoke the containers deployers as if
     * the handles method had been called and returned true.
     *
     * @return list of annotations this sniffer is interested in or an empty array
     */
    public Class<? extends Annotation>[] getAnnotationTypes();
    
    /**
     * Returns the module type associated with this sniffer
     * @return the container name
     */
    public String getModuleType();

   /**                                          
     * Sets up the container libraries so that any imported bundle from the
     * connector jar file will now be known to the module subsystem
     *
     * This method returns a {@link Module}s for the module containing
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
    public Module[] setup(String containerHome, Logger logger) throws IOException;

   /**
     * Tears down a container, remove all imported libraries from the module
     * subsystem.
     *
     */
    public void tearDown();

    /**
     * Returns the list of Containers that this Sniffer enables.
     *
     * The runtime will look up each container implementing
     * using the names provided in the habitat.
     *
     * @return list of container names known to the habitat for this sniffer
     */
    public String[] getContainersNames();

    /** 
     * @return whether this sniffer should be visible to user
     * 
     */
    public boolean isUserVisible();
    
    /**
     * Returns a map of deployment configurations for this Sniffer from the
     * specific archive source.  
     * <p>
     * Many sniffers (esp. Java EE sniffers) will choose to set the key of each 
     * map entry to the relative path within the ReadableArchive of the 
     * deployment descriptor and the value of that map entry to the
     * descriptor's contents.
     * 
     * @param source the contents of the application's archive
     * @return map of configuration names to configurations for the application
     * @throws java.io.IOException in case of errors searching or reading the
     * archive for the deployment configuration(s)
     */
    public Map<String,String> getDeploymentConfigurations(final ReadableArchive source) throws IOException;

    /** 
     * @return the set of the sniffers that should not co-exist for the 
     * same module. For example, ejb and appclient sniffers should not 
     * be returned in the sniffer list for a certain module.
     * This method will be used to validate and filter the retrieved sniffer
     * lists for a certain module
     * 
     */
    public String[] getIncompatibleSnifferTypes();
}
