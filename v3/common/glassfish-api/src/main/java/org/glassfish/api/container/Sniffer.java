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

import org.glassfish.api.deployment.Deployer;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.jvnet.hk2.annotations.Contract;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * A sniffer implementation is responsible for identifying a particular
 * application type and/or a particular file type.
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
     * Returns the pattern to apply against the request URL
     * If the pattern matches the URL, the service method of the associated
     * container will be invoked
     * @return pattern instance
     */
    public Pattern getURLPattern();

    /**
     * Returns the module type associated with this sniffer
     * @return the container name
     */
    public String getModuleType();

   /**
     * Sets up the container libraries so that any imported bundle from the
     * connector jar file will now be known to the module subsystem
     * @param containerHome is where the container implementation resides
     * @param logger the logger to use
     * @throws java.io.IOException exception if something goes sour
     */
    public void setup(String containerHome, Logger logger) throws IOException;

   /**
     * Tears down a container, remove all imported libraries from the module
     * subsystem.
     *
     */
    public void tearDown();

    /**
     * Returns the list of Containers that this Sniffer enables
     *
     * @return list of container names known to the habitat for this sniffer
     */
    public String[] getContainersNames();

}
