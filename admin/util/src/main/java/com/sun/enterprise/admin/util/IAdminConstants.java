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

/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
/*
 * AdminConstants.java
 *
 * Created on October 9, 2003, 8:51 AM
 */

package com.sun.enterprise.admin.util;

import com.sun.enterprise.util.SystemPropertyConstants;

/**
 *
 * @author  kebbs
 */
public interface IAdminConstants {
    
    public static final String HOST_PROPERTY_NAME = "client-hostname";
    
    public static final String SYSTEM_CONNECTOR_NAME = "system";
    public static final String RENDEZVOUS_PROPERTY_NAME = "rendezvousOccurred"; 
    
    public static final String DOMAIN_TARGET = "domain";
    public static final String STANDALONE_CONFIGURATION_SUFFIX = "-config";
    
    //FIXHTHIS: Change the name when the configuration cloning is in place.
    public static final String DEFAULT_CONFIGURATION_NAME = SystemPropertyConstants.TEMPLATE_CONFIG_NAME;
    
    public static final String DAS_NODECONTROLLER_MBEAN_NAME="com.sun.appserv:type=node-agents,category=config";
    public static final String NODEAGENT_STARTINSTANCES_OVERRIDE =" startInstancesOverride";
    public static final String NODEAGENT_SYNCINSTANCES_OVERRIDE = "syncInstances";
    public static final String NODEAGENT_DOMAIN_XML_LOCATION="/config/domain.xml";

    // resource types
    public static final String SYSTEM_ALL = "system-all";
    public static final String SYSTEM_ADMIN = "system-admin";
    public static final String SYSTEM_INSTANCE = "system-instance";
    public static final String SYSTEM_PREFIX = "system-";
    public static final String USER = "user";

    public static final String DAS_SERVER_NAME    = "server";
    
    public static final String DAS_CONFIG_OBJECT_NAME_PATTERN = 
        "*:type=config,category=config,name=server-config";
}
