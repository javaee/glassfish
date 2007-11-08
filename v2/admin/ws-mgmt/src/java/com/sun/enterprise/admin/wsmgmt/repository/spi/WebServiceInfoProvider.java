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
package com.sun.enterprise.admin.wsmgmt.repository.spi;

import java.util.List;
import java.util.Map;

/**
 * This is the mechanism to provide web service information for a given module.
 * A WebServiceInfoProvider implementation is a class that extends the 
 * WebServiceInfoProvider abstract class. Some WebServiceInfoProvider can deal
 * with ejb and web module. Some only deal with web modules.
 * <br>
 * A WebServiceInfoProvider implemented is identified by its fully qualified 
 * class name. The default RepositoryProvider is 
 * com.sun.enterprise.tools.common.AppServWebServiceInfoProvider
 */
public interface WebServiceInfoProvider {

    /**
     * Returns the unique identifier for this WebServiceInfoProvider object.
     *
     * @return fully qualified class name of this WebServiceInfoProvider
     */
    public String getProviderID();

    /**
     * Returns the List of WebServiceInfos for the provided EJB module.
     *
     * @param  moduleInfo   Descriptor file (sun-ejb-jar.xml or sun-web-app.xml)
     *                      location
     * @param  propMap      Additional properties passed 
     *
     * @return the List of WebServiceInfos
     * @throws when the descriptor can not be read or file is not of
     *          sun-ejb-jar.xml or sun-web-app.xml type.
     */
    public List getWebServiceInfo(String moduleInfo, Map propMap) 
        throws RepositoryException;

    /** 
     * Property name for module type. If this property is not set, the module
     * type is assumed to web module. 
     */
    public final static String MOD_TYPE_PROP_NAME = "module.type";

    /** EJB module type */
    public final static String MOD_TYPE_EJB = "ejb.module.type";

    /** WEB module type */
    public final static String MOD_TYPE_WEB = "web.module.type";

    /** Application id for this web service end point */
    public final static String APP_ID_PROP_NAME = "application.id";

    /** bundle name for this web service end point */
    public final static String BUNDLE_NAME_PROP_NAME = "bundle.name";

    /** WSDL location for this web service end point */
    public final static String WSDL_PROP_NAME = "wsdl.location";

    /** application.xml for this web service end point */
    public final static String APPLICATION_XML_PROP_NAME = "application.xml";

    /** webservices.xml for this web service end point */
    public final static String WS_XML_PROP_NAME = "ws.xml";

    /** webservices.xml location for this web service end point */
    public final static String WS_XML_LOCATION_PROP_NAME = "ws.xml.location";

    /** mapping file location for this web service end point */
    public final static String MAPPING_PROP_NAME = "mapping.location";

    /** ejb-jar.xml location for this web service end point */
    public final static String EJB_JAR_XML_PROP_NAME =
                                                    "ejb.jar.xml.location";

    /** sun-ejb-jar.xml for this web service end point */
    public final static String SUN_EJB_JAR_XML_PROP_NAME =
                                                "sun.ejb.jar.xml";

    /** sun-ejb-jar.xml location for this web service end point */
    public final static String SUN_EJB_JAR_XML_LOCATION_PROP_NAME =
                                                "sun.ejb.jar.xml.location";

    /** web.xml location for this web service end point */
    public final static String WEB_XML_PROP_NAME = "web.xml.location";

    /** sun-web.xml location for this web service end point */
    public final static String SUN_WEB_XML_PROP_NAME = 
                                            "sun.web.xml";

    /** sun-web.xml location for this web service end point */
    public final static String SUN_WEB_XML_LOCATION_PROP_NAME = 
                                            "sun.web.xml.location";


    /** application root location for this web service end point */
    public final static String APP_ROOT_LOCATION_PROP_NAME="app.root.location";

    /** bundle root location for this web service end point */
    public final static String BUNDLE_ROOT_LOCATION_PROP_NAME =
                                                "bundle.root.location";
}
