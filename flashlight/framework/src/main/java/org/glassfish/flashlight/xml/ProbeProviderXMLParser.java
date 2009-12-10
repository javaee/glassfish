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

package org.glassfish.flashlight.xml;

import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory;  
import javax.xml.parsers.ParserConfigurationException;
 
import org.xml.sax.SAXException;  
import org.xml.sax.InputSource;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import com.sun.logging.LogDomains;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import static org.glassfish.flashlight.xml.XmlConstants.*;

/**
 * This Class reads the Probe Provider from the xml file supplied
 * to constructor
 */
public class ProbeProviderXMLParser
{
    private List<Provider> providers = new ArrayList();
    private Document document;
    
    private static final Logger logger =
        LogDomains.getLogger(ProbeProviderXMLParser.class, LogDomains.MONITORING_LOGGER);
    public final static LocalStringManagerImpl localStrings =
                            new LocalStringManagerImpl(ProbeProviderXMLParser.class);
 
    /** Creates new ProbeProviderXMLParser */
    public ProbeProviderXMLParser(InputStream is) throws Exception {
        initProperties(is);
        generateProviders();
        if (providers.size() == 0) {
            String errStr = localStrings.getLocalString("noProviderFromXML", "No providers identified from the xml");
                logger.log(Level.SEVERE, errStr);
        }
    }

    /**
     *Parse the XML Properties file and populate it into document object
     */
    private void initProperties(InputStream is) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            //AddResourcesErrorHandler  errorHandler = new AddResourcesErrorHandler();
            //factory.setValidating(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            //builder.setEntityResolver(this);
            //builder.setErrorHandler(errorHandler);
            DOMParser parser = new DOMParser();
            InputSource isource = new InputSource(is);
            parser.parse(isource);
            document = parser.getDocument();
            //document = builder.parse(is);
        } catch (SAXException sxe) {
            Exception  x = sxe;
            if (sxe.getException() != null)
               x = sxe.getException();
            throw new Exception(x.getLocalizedMessage());
        }
        catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            throw new Exception(pce.getLocalizedMessage());
        }
        catch (IOException ioe) {
            throw new Exception(ioe.getLocalizedMessage());
        }
    }

    public List<Provider> getProviders() {
        return providers;
    }
    
    /**
     * Get All the providers from the document object.
     *
     */
    private void generateProviders() throws Exception
    {
        if (document != null) {

            Element probeProvidersElement = document.getDocumentElement();
            //providers.add(getProvider(probeProvidersElement));

            //get a nodelist of  probe-provider's
            NodeList probeProviderList = probeProvidersElement.getElementsByTagName(PROBE_PROVIDER);

            if (probeProviderList != null && probeProviderList.getLength() > 0) {
                for (int i=0; i < probeProviderList.getLength(); i++) {
                    //get the probe-provider element
                    Element probeProvider = (Element) probeProviderList.item(i);

                    //get the Provider Object
                    Provider p = getProvider(probeProvider);

                    //add it to the list
                    providers.add(p);
                }

            }
        }
    }
    

    /*
     * Generate the provider
     */
    private Provider getProvider(Element providerEl) throws Exception
    {
        List<Probe> probes = new ArrayList();
        //get probe-provider attributes
        String moduleProviderName = providerEl.getAttribute(MODULE_PROVIDER_NAME);
        String moduleName = providerEl.getAttribute(MODULE_NAME);
        String probeProviderName = providerEl.getAttribute(PROBE_PROVIDER_NAME);
        String providerClass = providerEl.getAttribute(PROBE_PROVIDER_CLASS);
        //get a nodelist of  probe's
        NodeList probeList = providerEl.getElementsByTagName(PROBE);

        if (probeList != null && probeList.getLength() > 0) {
            for (int i=0; i < probeList.getLength(); i++) {
                //get the probe-provider element
                Element probeEl = (Element) probeList.item(i);

                //get the Provider Object
                Probe p = getProbe(probeEl);

                //add it to the list
                probes.add(p);
            }

        }


        return (new Provider(moduleProviderName, moduleName, probeProviderName, providerClass, probes));
    }

    /*
     * Generate the provider
     */
    private Probe getProbe(Element probeEl) throws Exception
    {
        //get probe name
        String name = probeEl.getAttribute(PROBE_NAME);

        //get self attr of Probe
        String self = probeEl.getAttribute(PROBE_SELF);
        if (self == null)
                self = "false";
        //get hidden attr of Probe
        String hidden = probeEl.getAttribute(PROBE_HIDDEN);
        if (hidden == null)
            hidden = "false";

        String method =
             probeEl.getElementsByTagName(METHOD).item(0).getFirstChild().getNodeValue();

        // Collect the ProbeParams
        List<ProbeParam> params = new ArrayList();
        NodeList probeParamsList = probeEl.getElementsByTagName(PROBE_PARAM);
        if (probeParamsList != null && probeParamsList.getLength() > 0) {
            for (int i=0; i < probeParamsList.getLength(); i++) {
                Element probeParamEl = (Element) probeParamsList.item(i);

                // get the ProbeParam object
                ProbeParam probeParam = getProbeParam(probeParamEl);
                params.add(probeParam);
            }
        }



        Probe probe = new Probe(name, method, params, Boolean.parseBoolean(self), Boolean.parseBoolean(hidden));
        return probe;
    }


    /*
     * Generate the provider
     */
    private ProbeParam getProbeParam(Element paramEl) throws Exception
    {
        //get param name
        String name = paramEl.getAttribute(PROBE_PARAM_NAME);
        String type = paramEl.getAttribute(PROBE_PARAM_TYPE);
        return (new ProbeParam(name, type));
    }
}