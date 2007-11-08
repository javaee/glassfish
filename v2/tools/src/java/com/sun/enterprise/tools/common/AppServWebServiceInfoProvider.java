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
package com.sun.enterprise.tools.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.StringTokenizer;
import com.sun.enterprise.tools.common.dd.webservice.Webservices;
import com.sun.enterprise.tools.common.dd.webservice.PortComponentType;
import com.sun.enterprise.tools.common.dd.webservice.WebserviceDescriptionType;
import com.sun.enterprise.tools.common.dd.webservice.ServiceImplBeanType;
import com.sun.enterprise.admin.wsmgmt.repository.spi.WebServiceInfoProvider;
import com.sun.enterprise.admin.wsmgmt.repository.spi.RepositoryException;
import com.sun.appserv.management.ext.wsmgmt.WebServiceEndpointInfo;
import com.sun.appserv.management.ext.wsmgmt.WebServiceEndpointInfoImpl;

import com.sun.enterprise.tools.common.dd.ejb.SunEjbJar;
import com.sun.enterprise.tools.common.dd.ejb.EnterpriseBeans;
import com.sun.enterprise.tools.common.dd.ejb.Ejb;
import com.sun.enterprise.tools.common.dd.WebserviceEndpoint;

import com.sun.enterprise.tools.common.dd.webapp.SunWebApp;
import com.sun.enterprise.tools.common.dd.webapp.Servlet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.FileNotFoundException;
import com.sun.enterprise.admin.mbeans.J2EEModule;

import org.netbeans.modules.schema2beans.Schema2BeansException;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.ApplicationHelper;
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.admin.server.core.AdminService;

import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilderFactory;
import org.xml.sax.InputSource;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPath;
import javax.xml.namespace.NamespaceContext;
import java.io.BufferedInputStream;
import com.sun.enterprise.webservice.monitoring.NamespaceContextImpl;

/**
 * This is the mechanism to provide web service information for a given module. 
 * A WebServiceInfoProvider implementation is a class that extends the 
 * WebServiceInfoProvider abstract class. Some WebServiceInfoProvider can deal
 * with ejb and web module. Some only deal with web modules.
 * <br>
 * A WebServiceInfoProvider implemented is identified by its fully qualified 
 * class name. The default RepositoryProvider is 
 * com.sun.enterprise.admin.repository.spi.impl.AppServWebServiceInfoProvider
 */
public class AppServWebServiceInfoProvider implements WebServiceInfoProvider {

    /**
     * Returns the unique identifier for this WebServiceInfoProvider object.
     *
     * @return fully qualified class name of this WebServiceInfoProvider
     */
    public String getProviderID() {
        return PROVIDER_ID;
    }

    /**
     * Returns the List of WebServiceInfos for the provided EJB module.
     * @param  moduleInfo   Descriptor file (sun-ejb-jar.xml or sun-web-app.xml)
     *                      location
     * @param  propMap      Additional properties passed 
     *
     * @return the List of WebServiceInfos
     * @throws when the descriptor can not be read or file is not of
     *          sun-ejb-jar.xml or sun-web-app.xml type.
     */
    public List getWebServiceInfo(String moduleInfo, Map propMap) 
                    throws RepositoryException {
        String moduleType = null,appId =null, bundleName = null,
                    webservices = null, bundleRoot = null;
        if (propMap != null) {
            moduleType = (String)
                propMap.get(WebServiceInfoProvider.MOD_TYPE_PROP_NAME);
            appId = (String)
               propMap.get(WebServiceInfoProvider.APP_ID_PROP_NAME);
            bundleName = (String) propMap.get(
               WebServiceInfoProvider.BUNDLE_NAME_PROP_NAME);            
            webservices = (String) propMap.get(
                WebServiceInfoProvider.WS_XML_LOCATION_PROP_NAME);
            bundleRoot = (String) 
             propMap.get(WebServiceInfoProvider.BUNDLE_ROOT_LOCATION_PROP_NAME);
        }

        // validate mandatory arguments
        if ( (moduleType==null) || (appId==null) 
                || (webservices==null) || (bundleRoot==null) ) {
            throw new IllegalArgumentException();
        }

        return getWebServiceInfoInternal(moduleInfo, appId,
                        bundleName, webservices, bundleRoot, propMap);
    }

    /**
     * Returns list of WebServiceEndpointInfo ojbects for 
     * all available web serivce end points in this module.
     *
     * @param  moduleInfo location to sun-web.xml or sun-ejb-jar.xml descriptor
     * @param  appId  name of application or stand alone module
     * @param  bundleName  name of embedded module name. This is null 
     *                     for stand alone module
     * @param  webservices  location of webservices.xml file 
     * @param  bundleRoot  location of module root. For embedded module, this 
     *           would point to generated/xml/j2ee-applications/moduleName. 
     *           For stand alone module, this would point to 
     *           generated/xml/j2ee-modules/moduleName
     *
     * @return  list of WebServiceEndpointInfo 
     *
     * @throws  RepositoryException  if an error during parsing descriptor
     */
    private List getWebServiceInfoInternal(String moduleInfo,
                    String appId, String bundleName, String
                    webservices, String bundleRoot, Map propMap) 
                    throws RepositoryException {

        // load webservices.xml beans
        FileInputStream in = null;
        try {
            in = new FileInputStream(webservices);
        } catch(FileNotFoundException fne) {
            throw new RepositoryException(fne); 
        }
        Webservices webServices = null; 
        webServices = Webservices.createGraph(in);

        // bundle name is null for stand alone module
        boolean isAppStandAloneModule = (bundleName==null) ? true : false;

        // all web service endpoints
        WebserviceDescriptionType[] wsdts =
                webServices.getWebserviceDescription();

        ArrayList aList = new ArrayList();
        String uri = null, implName = null, implType = null, implClass = null;
        Map wsWebMap = null, wsEjbMap = null;

        // get all web services in this module
        for (int wsCnt =0; wsCnt < wsdts.length; wsCnt++) {
                WebserviceDescriptionType wsdt = wsdts[wsCnt];

                String wsdl = null;
                String wsdlFile = null;
                String mappingFileName = null;
                String mapping = null;
                if (wsdt != null) {
                    wsdl = bundleRoot + File.separator + wsdt.getWsdlFile();
                    String mapFile = wsdt.getJaxrpcMappingFile();
                    if (mapFile == null) {
                        mappingFileName = null;
                    } else {
                        mappingFileName = bundleRoot + File.separator + mapFile;
                    }
                }

                J2EEModule j2eeModule = new J2EEModule();
                try {
                    if (mappingFileName != null) {
                        mapping = j2eeModule.getStringForDDxml(mappingFileName);
                    }
                } catch (Exception e ) {
                  //_logger.log(Level.FINE,"Error reading dd file contents", e);
                }

                try {
                    if(wsdl != null) {
                        wsdlFile = j2eeModule.getStringForDDxml(wsdl);
                    }
                } catch (Exception e ) {
                  //_logger.log(Level.FINE,"Error reading dd file contents", e);
                }

                // get all ports inside this web service.
                PortComponentType pts[] = wsdt.getPortComponent();
                for (int portCnt =0; portCnt < pts.length; portCnt++) {
                    PortComponentType pt = pts[portCnt];
                    ServiceImplBeanType beanType = pt.getServiceImplBean();
                    
                    WebServiceDescrInfo wsDescrInfo = null;
                    implName = beanType.getServletLink();
                    if ( implName != null) {
                        implType = "SERVLET";

                        // cache uri and sevlet impl class for all endpoints
                        if (wsWebMap == null) {
                            wsWebMap = getWebServiceInfoForWebModule(moduleInfo,
                                appId, bundleName, webservices);
                        }

                        // uri, servlet impl class from cache
                        if (wsWebMap != null) {
                            wsDescrInfo = (WebServiceDescrInfo) wsWebMap.get(
                                (String)pt.getPortComponentName());
                        }
                    } else  {
                        implName = beanType.getEjbLink();
                        if ( implName != null) {
                            implType = "EJB";

                            // cache uri
                            if (wsEjbMap == null) {
                                wsEjbMap = getWebServiceInfoForEjbModule(
                                    moduleInfo, appId, bundleName, webservices);
                            }

                            // uri 
                            if ( wsEjbMap != null) {
                                wsDescrInfo = (WebServiceDescrInfo)wsEjbMap.get(
                                    (String)pt.getPortComponentName());
                            }
                        } else {
                            // throw warning, unknown type set
                        }
                    }
                    if (wsDescrInfo != null) {
                        uri = wsDescrInfo.getUri();
                        implClass = wsDescrInfo.getImplClass();
                    }

                    final String wsFile = (String) propMap.get(
                        WebServiceInfoProvider.WS_XML_PROP_NAME);

                    // port contains name-space:port-name
                    // get the second part of this string (after the ":")
                    String port = null;
                    StringTokenizer st = new StringTokenizer(
                    pt.getWsdlPort().getLocalPart(),
                    ":");
                    while(st.hasMoreTokens()) {
                        port = st.nextToken();
                    }

                    final String serviceUrl =
                    getServiceURL(wsdl,port) ;

                    // populate web service endpoint info object
                    WebServiceEndpointInfoImpl wsInfo = new 
                        WebServiceEndpointInfoImpl(pt.getPortComponentName(),
                            uri, appId, bundleName, isAppStandAloneModule,
                            wsdlFile, mapping, wsFile,implType,implName,
                            implClass, serviceUrl, wsDescrInfo.isSecure());

                    wsInfo.putField(
                    WebServiceEndpointInfo.SUN_WEB_XML_KEY, (Serializable)propMap.get(
                        WebServiceInfoProvider.SUN_WEB_XML_PROP_NAME));

                    wsInfo.putField(
                    WebServiceEndpointInfo.WEB_XML_KEY, (Serializable)propMap.get(
                        WebServiceInfoProvider.WEB_XML_PROP_NAME));

                    wsInfo.putField(
                    WebServiceEndpointInfo.SUN_EJB_XML_KEY, (Serializable)propMap.get(
                        WebServiceInfoProvider.SUN_EJB_JAR_XML_PROP_NAME));

                    wsInfo.putField(
                    WebServiceEndpointInfo.EJB_XML_KEY, (Serializable)propMap.get(
                        WebServiceInfoProvider.EJB_JAR_XML_PROP_NAME));

                    wsInfo.putField(
                    WebServiceEndpointInfo.APPLICATION_XML_KEY, (Serializable)propMap.get(
                        WebServiceInfoProvider.APPLICATION_XML_PROP_NAME));

                    wsInfo.putField(
                    WebServiceEndpointInfo.MAPPING_FILE_LOCATION_KEY,
			            (Serializable)mappingFileName);

                    wsInfo.putField(
                    WebServiceEndpointInfo.WSDL_FILE_LOCATION_KEY,
			            (Serializable)wsdl);

                    // add to the list
                    aList.add(wsInfo);
                }
        }
        return aList;
    }

    private String getServiceURL(String wsdlFile, String localPart) {
        String endpointURL = null;
        try {
            DocumentBuilderFactory dFactory = 
                DocumentBuilderFactory.newInstance();
            InputSource inputSource = new InputSource(new
                BufferedInputStream(new FileInputStream(wsdlFile)));
            Document wsdlDoc = dFactory.newDocumentBuilder().parse(new
            BufferedInputStream(new FileInputStream(wsdlFile)));
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();
            NamespaceContext context = new NamespaceContextImpl(wsdlDoc);
            xPath.setNamespaceContext(context);
            String xpathExpression = "/:definitions/:service/:port[@name='"+ 
                localPart+"']/soap:address/@location";
            endpointURL = xPath.evaluate(xpathExpression, inputSource);
            return endpointURL;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the List of WebServiceEndpointInfos for the provided WEB module.
     *
     * @param  moduleInfo  path to sun-web.xml location
     * @param  appId  name of the application or module
     * @param  bundleName  name of the bundle
     * @param  webservices path to the webservices.xml location
     *
     * @return the Map Web Service Names and WebServiceDescrInfos
     */
    private Map getWebServiceInfoForWebModule(String moduleInfo,
                    String appId, String bundleName, String webservices) 
                    throws RepositoryException {

        // load sun-web.xml beans
        FileInputStream in = null;
        try {
            in = new FileInputStream(moduleInfo);
        } catch(FileNotFoundException fne) {
            throw new RepositoryException (fne); 
        }
        SunWebApp sunWebApp = null; 
        try {
            sunWebApp = SunWebApp.createGraph(in);
        } catch (Schema2BeansException sce) {
            throw new RepositoryException (sce); 
        }

        // bundle name is null for stand alone module
        boolean isAppStandAloneModule = (bundleName==null) ? true : false;

        // all available servlets
        Servlet[] sLets = sunWebApp.getServlet();

        Map wsMap = new HashMap();
        for (int sCnt =0; sCnt < sLets.length; sCnt++) {
            Servlet sLet = sLets[sCnt];

            // all end points for this servlet
            WebserviceEndpoint[] webSvcEps = sLet.getWebserviceEndpoint();

            for ( int wsCnt = 0; wsCnt < webSvcEps.length; wsCnt++) {
                WebserviceEndpoint webSvc = webSvcEps[wsCnt];

                // context root for web service endpoint
                String ctxRoot = sunWebApp.getContextRoot();
                String uriInConfig = getUriInDomainConfig(appId);
                if (uriInConfig != null) {
                    ctxRoot = uriInConfig;
                }
                String uri;
                String wsUri =  webSvc.getEndpointAddressUri();
                if ((wsUri != null) && (wsUri.length() > 0) 
                                    && (wsUri.charAt(0) != '/')) {
                    wsUri = "/" + wsUri;    
                }

                // FIXME: Do we need to read domain.xml stand alone module?
                if (ctxRoot != null) {
                    uri = ctxRoot + wsUri;
                } else {
                    uri = wsUri;
                }

                boolean isSec = false;
                String trans = webSvc.getTransportGuarantee();
                if (( trans != null) && ("NONE".equals(trans) == false)) {
                    isSec = true;
                } else if ((webSvc.getLoginConfig() != null) ||
                    (webSvc.getMessageSecurityBinding() != null)) {
                    isSec = true;
                }
                // web service uri, endpoint name and servlet impl class
                WebServiceDescrInfo wsdInfo = 
                    new WebServiceDescrInfo(webSvc.getPortComponentName(), 
                                       uri, webSvc.getServletImplClass(),isSec);

                wsMap.put(wsdInfo.getName(),wsdInfo);
            }
       }

        return wsMap;
    }

    /**
     * Returns the webservice-description-type for the given 
     * port-component-name. WSDL and jaxrpc-mapping file 
     * location are available in this object. 
     *
     * @param  webservcesXML path to webservices.xml file
     * @param  pcName port component name 
     */
    private WebserviceDescriptionType getWSDT(String webservicesXML, 
            String pcName) {

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(new File(webservicesXML));
            Webservices ws = Webservices.createGraph(fis);
            WebserviceDescriptionType[] wsdt = ws.getWebserviceDescription();
            for (int i=0; i<wsdt.length; i++) {
                PortComponentType[] pct = wsdt[i].getPortComponent();
                for (int j=0; j<pct.length; j++) {
                    String name = pct[j].getPortComponentName();
                    if (pcName.equals(name)) {
                        return wsdt[i];
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) { }
            }
        }
        return null;
    }


    /**
     * Returns the List of WebServiceEndpointInfos for the provided EJB module.
     *
     * @param  moduleInfo  path to sun-ejb-jar.xml location
     * @param  appId  name of the application or module
     * @param  bundleName  name of the bundle
     * @param  webservices path to the webservices.xml location
     *
     * @return the Map of Web Service Names and WebServiceDescrInfos
     */
    private Map getWebServiceInfoForEjbModule(String moduleInfo,String appId, 
            String bundleName, String webservices) 
            throws RepositoryException {

        // load sun-ejb-jar.xml beans
        FileInputStream in = null;
        try {
            in = new FileInputStream(moduleInfo);
        } catch(FileNotFoundException fne) {
            throw new RepositoryException (fne); 
        }
        SunEjbJar sunEjbJar = null; 
        try {
            sunEjbJar = SunEjbJar.createGraph(in);
        } catch (Schema2BeansException sce) {
            throw new RepositoryException (sce); 
        }

        // bundle name is null for stand alone module
        boolean isAppStandAloneModule = (bundleName==null) ? true : false;

        // all ejbs in this module
        EnterpriseBeans eBeans = sunEjbJar.getEnterpriseBeans();
        Ejb[] ejbs = eBeans.getEjb();
        HashMap wsMap = new HashMap();

        for (int ejbCnt =0; ejbCnt < ejbs.length; ejbCnt++) {
            Ejb ejb = ejbs[ejbCnt];

            // all web service endpoints for this ejb
            WebserviceEndpoint[] webSvcEps = ejb.getWebserviceEndpoint();

            for ( int wsCnt = 0; wsCnt < webSvcEps.length; wsCnt++) {
                WebserviceEndpoint webSvc = webSvcEps[wsCnt];

                boolean isSec = false;
                String trans = webSvc.getTransportGuarantee();
                if (( trans != null) && ("NONE".equals(trans) == false)) {
                    isSec = true;
                } else if ((webSvc.getLoginConfig() != null) ||
                    (webSvc.getMessageSecurityBinding() != null)) {
                    isSec = true;
                }
                // uri
                WebServiceDescrInfo wsdInfo = new
                    WebServiceDescrInfo(webSvc.getPortComponentName(),
                        webSvc.getEndpointAddressUri(), 
                        webSvc.getTieClass(), isSec);

                wsMap.put(wsdInfo.getName(),wsdInfo);
            }
        }

        return wsMap;
    }

    private String getUriInDomainConfig(String appId) {
            ConfigContext configCtx = AdminService.getAdminService().
                getAdminContext().getAdminConfigContext();
        ConfigBean cb = null;
        
        try {
            cb = ApplicationHelper.findApplication(configCtx, appId);
        } catch( Exception e) {
            //String msg = "Could not find a deployed application/module by name "
             //       + appId;
            //_logger.log(Level.FINE, msg);
            return null;
        }
        
        if (cb instanceof WebModule) {
            return ((WebModule)cb).getContextRoot();
        } else {
            return null;
        }

    }

    /**
     * Data structure to hold URI and impl class for end point.
     */
    class WebServiceDescrInfo {
        
        WebServiceDescrInfo (String cName, String URI, String iClass, boolean
        isSec) {

                compName = cName;
                uri = URI;
                implClass = iClass;
                isSecure = isSec;
        }

        String getName () {
            return compName;
        }

        String getUri() {
            return uri;
        }
        
        String getImplClass() {
            return implClass;
        }

        boolean isSecure() {
            return isSecure;
        }

        private String compName;
        private String uri;
        private String implClass;
        private boolean isSecure;
    }

    /** provider id for the default web server info provider */
    public static final String PROVIDER_ID = 
        "com.sun.enterprise.tools.common.AppServWebServiceInfoProvider";
}
