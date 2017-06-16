/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package org.glassfish.resources.admin.cli;

import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import org.glassfish.resourcebase.resources.api.ResourceStatus;
import org.jvnet.hk2.annotations.Contract;

import java.util.HashMap;
import java.util.Properties;

/**
 * Contract for all ResourceManagers
 * 
 * @author Prashanth Abbagani
 */
@Contract
public interface ResourceManager {

    /**
     * creates the resource as a child to the <i>resources</i> provided
     * @param resources parent for the resource to be created
     * @param attributes resource configuration
     * @param properties properties
     * @param target target 
     * @return ResourceStatus indicating the status of resource creation
     * @throws Exception when unable to create the resource
     */
    ResourceStatus create(Resources resources, HashMap attributes, final Properties properties, String target)
            throws Exception ;

    /**
     * creates config-bean equivalent for the resource configuration provided as attributes and properties<br>
     * Does not persist the configuration<br>
     * @param resources parent for the resource to be created
     * @param attributes attributes of the resource
     * @param properties properties of the resource
     * @param validate indicate whether config validation is required or not
     * @return Config-Bean equivalent of the resource
     * @throws Exception when unable to create config-bean-equivalent 
     */
    Resource createConfigBean(Resources resources, HashMap attributes, Properties properties, boolean validate)
            throws Exception;

    /**
     * returns the resource-type
     * @return resource-type
     */
    String getResourceType();
}
