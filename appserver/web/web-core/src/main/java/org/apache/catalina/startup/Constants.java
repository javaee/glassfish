/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.catalina.startup;

/**
 * String constants for the startup package.
 *
 * @author Craig R. McClanahan
 * @author Jean-Francois Arcand
 * @version $Revision: 1.6 $ $Date: 2007/02/20 20:16:56 $
 */
public final class Constants {

    public static final String Package = "org.apache.catalina.startup";

    public static final String ApplicationWebXml = "/WEB-INF/web.xml";
    // START GlassFish 2439
    public static final String DEFAULT_CONTEXT_XML = "config/context.xml";
    // END GlassFish 2439
    public static final String DefaultWebXml = "conf/web.xml";

    public static final String TldDtdPublicId_11 =
        "-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.1//EN";
    public static final String TldDtdResourcePath_11 =
        "/javax/servlet/jsp/resources/web-jsptaglibrary_1_1.dtd";

    public static final String TldDtdPublicId_12 =
        "-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.2//EN";
    public static final String TldDtdResourcePath_12 =
        "/javax/servlet/jsp/resources/web-jsptaglibrary_1_2.dtd";

    public static final String TldSchemaPublicId_20 =
        "web-jsptaglibrary_2_0.xsd";
    public static final String TldSchemaResourcePath_20 =
        "/javax/servlet/jsp/resources/web-jsptaglibrary_2_0.xsd";

    public static final String TLD_SCHEMA_PUBLIC_ID_21 =
        "web-jsptaglibrary_2_1.xsd";
    public static final String TLD_SCHEMA_RESOURCE_PATH_21 =
        "/javax/servlet/jsp/resources/web-jsptaglibrary_2_1.xsd";

    public static final String WebDtdPublicId_22 =
        "-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN";
    public static final String WebDtdResourcePath_22 =
        "/javax/servlet/resources/web-app_2_2.dtd";

    public static final String WebDtdPublicId_23 =
        "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN";
    public static final String WebDtdResourcePath_23 =
        "/javax/servlet/resources/web-app_2_3.dtd";

    public static final String WebSchemaPublicId_24 =
        "web-app_2_4.xsd";
    public static final String WebSchemaResourcePath_24 =
        "/javax/servlet/resources/web-app_2_4.xsd";

    public static final String WebSchemaPublicId_25 =
        "web-app_2_5.xsd";
    public static final String WebSchemaResourcePath_25 =
        "/javax/servlet/resources/web-app_2_5.xsd";

    public static final String J2eeSchemaPublicId_14 =
        "j2ee_1_4.xsd";
    public static final String J2eeSchemaResourcePath_14 =
        "/javax/servlet/resources/j2ee_1_4.xsd";

    public static final String JAVA_EE_SCHEMA_PUBLIC_ID_5 =
        "javaee_5.xsd";
    public static final String JAVA_EE_SCHEMA_RESOURCE_PATH_5 =
        "/javax/servlet/resources/javaee_5.xsd";

    public static final String W3cSchemaPublicId_10 =
        "xml.xsd";
    public static final String W3cSchemaResourcePath_10 =
        "/javax/servlet/resources/xml.xsd";

    public static final String JspSchemaPublicId_20 =
        "jsp_2_0.xsd";
    public static final String JspSchemaResourcePath_20 =
        "/javax/servlet/resources/jsp_2_0.xsd";
    
    public static final String JSP_SCHEMA_PUBLIC_ID_21 =
        "jsp_2_1.xsd";
    public static final String JSP_SCHEMA_RESOURCE_PATH_21 =
        "/javax/servlet/resources/jsp_2_1.xsd";

    public static final String J2eeWebServiceSchemaResourcePath_11 =
            "/javax/servlet/resources/j2ee_web_services_1_1.xsd";
    
    public static final String J2eeWebServiceClientSchemaPublicId_11 =
            "j2ee_web_services_client_1_1.xsd";
    public static final String J2eeWebServiceClientSchemaResourcePath_11 =
            "/javax/servlet/resources/j2ee_web_services_client_1_1.xsd";
}
