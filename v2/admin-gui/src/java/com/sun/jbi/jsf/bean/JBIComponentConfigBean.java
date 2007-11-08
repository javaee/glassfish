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
package com.sun.jbi.jsf.bean;

import com.sun.data.provider.TableDataProvider;
import com.sun.data.provider.impl.ObjectListDataProvider;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Properties;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


import com.sun.jbi.jsf.util.BeanUtilities;
import com.sun.jbi.jsf.util.JBILogger;
import com.sun.jbi.jsf.util.ValidationUtilities;
import com.sun.jbi.ui.common.JBIAdminCommands;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import com.sun.org.apache.xpath.internal.XPathAPI;

/**
 * Provides properties used to configure SE or BC just before installation take
   place
 */
public class JBIComponentConfigBean {
    
   

    /** Creates a new instance of JBIComponentConfigBean */
    public JBIComponentConfigBean() {
        isDiffrentComponent = true;
    }
    
    public TableDataProvider getComponentNewConfigurationData()  {
        List<ComponentConfigurationEntry> newConfigData = 
            new ArrayList<ComponentConfigurationEntry>() ;
        componentNewConfigurationDataProvider = new ObjectListDataProvider(newConfigData);
        if(!isValidComponent) {
            return componentNewConfigurationDataProvider;
        }

        
        logger.fine("JBIComponentConfigBean.getNewComponentConfigurationeData(): result=" + componentNewConfigurationDataProvider);
 
        return componentNewConfigurationDataProvider;
    } 
    
    public TableDataProvider getComponentConfigurationData()
    {
        
        if(componentConfigurationDataProvider != null && !isDiffrentComponent) {
            componentConfigurationDataProvider.commitChanges();
        } else {
            logger.fine("JBIComponentConfigBean.getComponentConfigurationeData()"); 

            ArchiveBean archiveBean = BeanUtilities.getArchiveBean ();
            // get the configuration properties from the component jbi.xml
            List<ComponentConfigurationEntry> configProperties  = null;
            if(!isDomainDataSource) {
                configProperties = parseConfigData(archiveBean); 
            } else {
                configProperties = getComponentConfigurationDataFromDomain();
            }
            componentConfigurationDataProvider = new ObjectListDataProvider(configProperties);
    
            logger.fine("JBIComponentConfigBean.getComponentConfigurationeData(): configProperties=" + componentConfigurationDataProvider);
        
            isDiffrentComponent = false;
             
        }
        return componentConfigurationDataProvider;
    } 
 
    
    private List<ComponentConfigurationEntry> parseJbiXMLData(Document jbiXMLDoc) {
        List<ComponentConfigurationEntry> configData = 
                new ArrayList<ComponentConfigurationEntry>() ;
        logger.fine("JBIComponentConfigBean.parseJbiXMLData(): configProperties:" );
	try 
	    {
		NodeList configurationNodes = 
		    XPathAPI.selectNodeList(jbiXMLDoc,
					     XPATH_CONFIGURATION);
                // jbi.xml should have at most 1 configuration node
                if(configurationNodes.getLength() > 0) {
                {
                    NodeList configElementsList = 
                             configurationNodes.item(0).getChildNodes();
                    int count = configElementsList.getLength();
                    for (int i = 0; i < count; ++i) {
                        Node node = configElementsList.item(i);
                        if((node != null) && (node instanceof Element)) {
                            String name = node.getNodeName();
                            Text text = (Text)node.getChildNodes().item(0);
                            String value = null;
                            if(text != null) {
                                value = text.getData();
                            }
                            if(name != null) {
                                if(value == null) {
                                    value = "";
                                }
                                // remove namespace from name 
                                name = name.substring(name.lastIndexOf(":")+1);
                                logger.fine("name=" + name + " value=" +value);
                                ComponentConfigurationEntry entry =
                                   new ComponentConfigurationEntry(name, value,value,false);
                                configData.add(entry);
                            }
                        }
                    }
                         
                    }    
                }
	    }
	catch (Exception ex)
	    {
		// TBD use logging warning
		logger.fine("getType caught ex=" + ex);
		ex.printStackTrace(System.err);
	    }
        return configData;
    }
    
     
    public Properties getConfigurationProperties() {
       Properties configProps = new Properties();
       if(componentConfigurationDataProvider !=null  && componentConfigurationDataProvider.getRowCount() > 0) {
           configProps = getPropertiesFromProvider(componentConfigurationDataProvider);
       }
       Properties additionalProperties = getPropertiesFromProvider(componentNewConfigurationDataProvider);
       // merge jbi properties with the additional properties user
       // added during install
       configProps = mergeProperties(configProps,additionalProperties);
       return configProps;
     }
    
    public void invalidateCommittedDataProvider() {
        componentConfigurationDataProvider =  null;
    }
    
    public String getDomainDataSource() {
        // since this call is made at the beginning of manage target
        // screen we assume we deal we diffrent component
        isDiffrentComponent = true;
        isDomainDataSource = true;
        return "domain";
    }
    
    public String getArchiveDataSource() {
        // since this call is made at the beginning of step 2
        // of new component installation we assume we deal with new component
        isDiffrentComponent = true;
        isDomainDataSource = false;
        return "Archive";
    }
  
    public String getBindorServiceComponent() {
        isValidComponent = true;
        return "component";
    }
    
    public String getAssemblyComponent() {
        isValidComponent = false;
        return "service-assembly";
    }
    public String getShareLibComponent() {
        isValidComponent = false;
        return "shared-false";
    }
   
    public boolean isConfigurationValidForComponent( ) {
        return isValidComponent;
    }
    
    
    private List<ComponentConfigurationEntry> getComponentConfigurationDataFromDomain()
    {
        List<ComponentConfigurationEntry> configData = 
            new ArrayList<ComponentConfigurationEntry>() ;
        if(!isValidComponent) {
            return configData;
        }
        JBIAdminCommands    jbiAdminCommands = BeanUtilities.getClient();
        logger.fine("JBIComponentConfigBean.getComponentConfigurationDataFromDomain(): JBIAdminCommands=" +jbiAdminCommands );
        
        ShowBean showBean = BeanUtilities.getShowBean();
        String componentName = showBean.getName();
        try {
            String jbiXMLData = 
                jbiAdminCommands.getComponentInstallationDescriptor(componentName);
            DocumentBuilderFactory factory =
            DocumentBuilderFactory.newInstance();
            DocumentBuilder db = factory.newDocumentBuilder();
            InputSource inStream = new InputSource();
            inStream.setCharacterStream(new StringReader(jbiXMLData));
            Document jbiXMLDoc = db.parse(inStream);
            logger.fine("JBIComponentConfigBean.getComponentConfigurationDataFromDomain(): jbi.XML Document=" +jbiXMLDoc );
            JBIComponentConfigBean configBean =  new JBIComponentConfigBean();
            configData = configBean.parseJbiXMLData(jbiXMLDoc); 
       } catch (Exception e) {
            e.printStackTrace();
        }
        return configData;
    } 

    
    private Properties getPropertiesFromProvider(ObjectListDataProvider dataProvider) {
        logger.fine("JBIComponentConfigBean.getPropertiesFromProvider():" );
        Properties configProps = new Properties();
        dataProvider.commitChanges();
        List<ComponentConfigurationEntry> origList = dataProvider.getList();
        for (ComponentConfigurationEntry entry : origList) {
            String name = entry.getName();
            String value =  entry.getDefaultValue();
            String newValue =  entry.getNewValue();
            if(newValue != null && !newValue.equals(value)) {
                value =newValue;
            }
            configProps.setProperty(name,value);
            logger.fine("name=" + name + " value=" +value);
        }

       return configProps;
    }
    
    
    private Properties mergeProperties(Properties configProps ,
            Properties newProperties) {
        Properties jbiXMLProps = configProps;
        // add and update(on key match) properties
        Set keys = newProperties.keySet();
        for (Iterator iter = keys.iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            String value = (String)newProperties.get(key);
            jbiXMLProps.setProperty(key, value);
        }
        return jbiXMLProps;
    }
    
    private List<ComponentConfigurationEntry> parseConfigData(ArchiveBean archiveBean ) {
        List<ComponentConfigurationEntry> configData = 
            new ArrayList<ComponentConfigurationEntry>() ;
        if(!archiveBean.getHasJbiXml() || !isValidComponent) {
           return configData;
        }
        Document jbiXMLDoc = ValidationUtilities.getJbiDocument();
        return parseJbiXMLData(jbiXMLDoc);
    }

    
   

    /**
     * Controls printing of diagnostic messages to the log
     */
        //Get Logger to log fine mesages for debugging
   private static Logger logger = JBILogger.getInstance();
    
   private final static String XPATH_CONFIGURATION =
            "/jbi/component/Configuration";
   
   private ObjectListDataProvider componentConfigurationDataProvider;
   private ObjectListDataProvider componentNewConfigurationDataProvider;
   private boolean isDomainDataSource;
   private boolean isDiffrentComponent;
   // workaround  for jsftemplating conditional statement and tables problem.
   private boolean isValidComponent;
}
