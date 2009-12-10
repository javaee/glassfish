/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.internal.deployment;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.internal.api.ClassLoaderHierarchy;

import java.lang.instrument.ClassFileTransformer;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.net.URISyntaxException;
import java.net.MalformedURLException;

/**
 * semi-private interface to the deployment context
 *
 * @author Jerome Dochez
 */
public interface ExtendedDeploymentContext extends DeploymentContext {
    
    public enum Phase { UNKNOWN, PREPARE, LOAD, START, STOP, UNLOAD, CLEAN }

    public static final String IS_TEMP_CLASSLOADER = "isTempClassLoader";
    public static final String TRACKER = "tracker";


    /**
     * Sets the phase of the deployment activity.
     * 
     * @param newPhase
     */
    public void setPhase(Phase newPhase);

    public Phase getPhase();

    /**
     * Returns the list of transformers registered to this context.
     *
     * @return the transformers list
     */
    public List<ClassFileTransformer> getTransformers();

    /**
     * Create the deployment class loader. It will be used for sniffer 
     * retrieval, metadata parsing and deployer prepare. 
     *
     * @param clh the hierarchy of class loader for the parent
     * @param handler the archive handler for the source archive
     */
    public void createDeploymentClassLoader(ClassLoaderHierarchy clh, ArchiveHandler handler)
            throws URISyntaxException, MalformedURLException;

    /**
     * Create the final class loader. It will be used to load and start
     * application.
     *
     * @param clh the hierarchy of class loader for the parent
     * @param handler the archive handler for the source archive
     */
    public void createApplicationClassLoader(ClassLoaderHierarchy clh, ArchiveHandler handler)
            throws URISyntaxException, MalformedURLException;

    public void clean();

    /**
     * Sets the archive handler that's associated with this context
     *
     * @param archiveHandler
     */
    public void setArchiveHandler(ArchiveHandler archiveHandler);

    /**
     * Sets the source archive
     *
     * @param props
     */
    public void setSource(ReadableArchive source);

    /**
     * Sets the module properties for modules
     *
     * @param modulePropsMap
     */
    public void setModulePropsMap(Map<String, Properties> modulePropsMap);

    /**
     * Gets the deployment context for modules
     *
     * @return a map containing module deployment contexts
     */
    public Map<String, ExtendedDeploymentContext> getModuleDeploymentContexts();

   /**
     * Sets the classloader
     *
     * @param cloader
     */
    public void setClassLoader(ClassLoader cloader);



}
