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
 * WebServiceBean.java
 *
 * Created on September 14, 2006, 7:24 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.enterprise.tools.admingui.bean;

import com.sun.appserv.management.ext.wsmgmt.WebServiceEndpointInfo;

/**
 *
 * @author Jennifer
 */
public class WebServiceBean {
    
    /** Creates a new instance of WebServiceBean */
    public WebServiceBean() {
    }
    
    public String getMappingFileLocationKey() {
       return WebServiceEndpointInfo.MAPPING_FILE_LOCATION_KEY;
    }
    
    public String getWSDLFileLocationKey() {
       return WebServiceEndpointInfo.WSDL_FILE_LOCATION_KEY;
    }
    
    public String getEjbJarXmlFileName() {
        return WebServiceBean.EJB_XML;
    }
    
    public String getWebXmlFileName() {
       return WebServiceBean.WEB_XML;
    }
    
    public String getSunEjbJarXmlFileName() {
        return WebServiceBean.SUN_EJB_JAR_XML;
    }
    
    public String getSunWebXmlFileName() {
        return WebServiceBean.SUN_WEB_XML;
    }
    
    public String getWebservicesFileName() {
       return WebServiceBean.WEBSERVICES_XML;
    }
    

    
    public static final String WEBSERVICES_XML  = "Webservices.xml";
    public static final String WEB_XML          = "web.xml";
    public static final String EJB_XML          = "ejb-jar.xml";
    public static final String SUN_WEB_XML      = "sun-web.xml";
    public static final String SUN_EJB_JAR_XML  = "sun-ejb-jar.xml";
}
