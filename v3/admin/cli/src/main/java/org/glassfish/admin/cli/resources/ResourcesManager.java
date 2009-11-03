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

package org.glassfish.admin.cli.resources;

import org.glassfish.resource.common.Resource;
import org.glassfish.resource.common.ResourceStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Server;
import java.util.HashMap;
import java.util.Properties;
import java.io.File;

/**
 * This class serves as the API to creating new resources when an xml file 
 * is supplied containing the resource definitions
 * 
 * @author PRASHANTH ABBAGANI
 */
public class ResourcesManager {

     /**
     * Creating resources from sun-resources.xml file. This method is used by 
     * the admin framework when the add-resources command is used to create
     * resources
     */
    static ArrayList createResources(Resources resources, File resourceXMLFile,
            Server targetServer, ResourceFactory resourceFactory) throws Exception {
        ArrayList results = new ArrayList();
        ResourcesXMLParser resourcesParser =
            new ResourcesXMLParser(resourceXMLFile);
        List<Resource> vResources = resourcesParser.getResourcesList();
        //First add all non connector resources.
        Iterator<Resource> nonConnectorResources = ResourcesXMLParser.getNonConnectorResourcesList(vResources,false).iterator();
        while (nonConnectorResources.hasNext()) {
            Resource resource = (Resource) nonConnectorResources.next();
            HashMap attrList = resource.getAttributes();
            String desc = resource.getDescription();
            if (desc != null)
                attrList.put("description", desc);

            Properties props = resource.getProperties();

            ResourceStatus rs;
            try {
                ResourceManager rm = resourceFactory.getResourceManager(resource);
                rs = rm.create(resources, attrList, props, targetServer);
            } catch (Exception e) {
                String msg = e.getMessage();
                rs = new ResourceStatus(ResourceStatus.FAILURE, msg);
            }
            results.add(rs);
        }

        //Now add all connector resources
        Iterator connectorResources = ResourcesXMLParser.getConnectorResourcesList(vResources, false).iterator();
        while (connectorResources.hasNext()) {
            Resource resource = (Resource) connectorResources.next();
            HashMap attrList = resource.getAttributes();
            String desc = resource.getDescription();
            if (desc != null)
                attrList.put("description", desc);

            Properties props = resource.getProperties();

            ResourceStatus rs;
            try {
                ResourceManager rm = resourceFactory.getResourceManager(resource);
                rs = rm.create(resources, attrList, props, targetServer);
            } catch (Exception e) {
                String msg = e.getMessage();
                rs = new ResourceStatus(ResourceStatus.FAILURE, msg);
            }
            results.add(rs);
        }

        return results;
    }

}
