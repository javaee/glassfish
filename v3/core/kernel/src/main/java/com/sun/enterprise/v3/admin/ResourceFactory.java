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

package com.sun.enterprise.v3.admin;

import com.sun.enterprise.config.serverbeans.ServerTags;
import static com.sun.enterprise.v3.admin.ResourceConstants.*;

import org.glassfish.api.I18n;

/**
 *
 * @author PRASHANTH ABBAGANI
 * 
 * Factory class which returns the appropriate ResourceManager
 */
@I18n("add.resources")
public class ResourceFactory {

    private static final String CUSTOM_RESOURCE          = ServerTags.CUSTOM_RESOURCE;
    private static final String JDBC_CONNECTION_POOL     = ServerTags.JDBC_CONNECTION_POOL;
    private static final String CONNECTOR_RESOURCE       = ServerTags.CONNECTOR_RESOURCE;
    private static final String ADMIN_OBJECT_RESOURCE    = ServerTags.ADMIN_OBJECT_RESOURCE;
    private static final String JDBC_RESOURCE            = ServerTags.JDBC_RESOURCE;
    private static final String RESOURCE_ADAPTER_CONFIG  = ServerTags.RESOURCE_ADAPTER_CONFIG;
    private static final String MAIL_RESOURCE            = ServerTags.MAIL_RESOURCE;
    private static final String EXTERNAL_JNDI_RESOURCE   = ServerTags.EXTERNAL_JNDI_RESOURCE;
    private static final String CONNECTOR_CONNECTION_POOL = ServerTags.CONNECTOR_CONNECTION_POOL;
    private static final String PERSISTENCE_MANAGER_FACTORY_RESOURCE = ServerTags.PERSISTENCE_MANAGER_FACTORY_RESOURCE;
    private static final String CONNECTOR_SECURITY_MAP   = ServerTags.SECURITY_MAP;
    
    public static ResourceManager getResourceManager(Resource resource) {
        String resourceType = resource.getType();
        if (resourceType.equals(JDBC_RESOURCE))
        {
            return new JDBCResourceManager();
        } else if (resourceType.equals(JDBC_CONNECTION_POOL))
        {
            return new JDBCConnectionPoolManager();
        }
        return null;
    }
    
}
