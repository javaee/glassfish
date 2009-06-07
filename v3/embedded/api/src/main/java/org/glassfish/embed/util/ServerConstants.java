/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 */

package org.glassfish.embed.util;

import java.net.*;
import org.glassfish.embed.impl.EmbeddedServerEnvironment;
//import static com.sun.enterprise.util.SystemPropertyConstants.*;

/**
 *
 * @author Byron Nevins
 */
public class ServerConstants {
     // Amazingly -- these 2 are not available as constants in V3
     //even though they are used in v3
    public static final String			INSTALL_ROOT_URI_PROPERTY           = "com.sun.aas.installRootURI";
    public static final String			INSTANCE_ROOT_URI_PROPERTY          = "com.sun.aas.instanceRootURI";
    public static final int             DEFAULT_HTTP_PORT                   =  -1;
    public static final String          DEFAULT_HTTP_LISTENER_NAME          = "http-listener-1";
    public static final String          DEFAULT_ADMIN_HTTP_LISTENER_NAME    = "admin-listener";
    public static final int             DEFAULT_ADMIN_HTTP_PORT             =  -1;
    public static final String          DEFAULT_ADMIN_VIRTUAL_SERVER_ID     = "__asadmin";
	public static final String			LOGGING_RESOURCE_BUNDLE             = "org.glassfish.embed.LocalStrings";
	public static final String			EXCEPTION_RESOURCE_BUNDLE           = "/org/glassfish/embed/LocalStrings.properties";
    public static final int             MIN_PORT                            = 1;
    public static final int             MAX_PORT                            = (256 * 256) - 1;
    public static final String          DEFAULT_SERVER_NAME                 = "server";
    public static final String          DEFAULT_PATH_TO_INSTANCE            = "domains/domain1";
    public static final URL             DEFAULT_DOMAIN_XML_URL              = ServerConstants.class.getResource("/org/glassfish/embed/domain.xml");
    public static final String          LOG_FILE_DIR                        = "logs";
    public static final String          LOG_FILE                            = "server.log";
    public static final int             DEFAULT_JMX_CONNECTOR_PORT          = -1;
    public static final String          MODULES_DIR_NAME                    = "modules"; // install-root/modules
    public static final String          APPLICATIONS_DIR_NAME               = EmbeddedServerEnvironment.kRepositoryDirName;
    public static final String          GENERATED_DIR_NAME                  = EmbeddedServerEnvironment.kGeneratedDirName;
    public static final String          CONFIG_DIR_NAME                     = EmbeddedServerEnvironment.kConfigDirName;
    public static final String          DOCROOT_DIR_NAME                    = "docroot";
    public static final String          CONFIG_FILE_NAME                    = EmbeddedServerEnvironment.kConfigXMLFileName;
    public static final String          DEFAULT_PATH_TO_DOMAIN_XML          = CONFIG_DIR_NAME + "/" + CONFIG_FILE_NAME;
    public static final String          WELCOME_FILE                        = "index.html";
    public static final String          EMBEDDED_LOGGER                     = "org.glassfish.embed";
    public static final String          GFV3_ROOT_LOGGER                    = "javax.enterprise";
    public static final String          DTD_RESOURCE_LOCATION               = "/dtds";
    public static final String          DEFAULT_INSTALL_DIR_PREFIX          = "embedded_";
    public static final String          DEFAULT_INSTALL_DIR_SUFFIX          = "_glassfish";
}
