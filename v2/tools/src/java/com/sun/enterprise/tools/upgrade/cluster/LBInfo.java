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
 * LBInfo.java
 *
 * Created on June 21, 2004, 4:52 PM
 */

package com.sun.enterprise.tools.upgrade.cluster;

/**
 * This class represents Loadbalancer.xml file
 * @author  prakash
 */
import java.util.*;
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory;  
import javax.xml.parsers.FactoryConfigurationError;  
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;  
import org.xml.sax.SAXParseException;  

import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.sun.enterprise.util.i18n.StringManager;

public class LBInfo {
    
    private List clusters;
    private Properties lbProperties;
    private static java.util.logging.Logger log = com.sun.enterprise.tools.upgrade.common.CommonInfoModel.getDefaultLogger();
    private StringManager stringManager = StringManager.getManager("com.sun.enterprise.tools.upgrade.gui");

    /** Creates a new instance of LBInfo */
    public LBInfo() {
        clusters = new ArrayList();
        lbProperties = new Properties();
    }
    public boolean processLoadBalancerFile(String lbFile){
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        //factory.setValidating(false);
        factory.setNamespaceAware(true);
        try {
           DocumentBuilder builder = factory.newDocumentBuilder();
           DocumentBuilder builderDomainXml = factory.newDocumentBuilder();
           builderDomainXml.setEntityResolver((org.xml.sax.helpers.DefaultHandler)Class.forName
                ("com.sun.enterprise.config.serverbeans.ServerValidationHandler").newInstance());
           Element docEle = builderDomainXml.parse(new File(lbFile)).getDocumentElement();
           NodeList clEles = docEle.getElementsByTagName("cluster");
           for(int lh =0; lh < clEles.getLength(); lh++){
               clusters.add(new LBCluster((Element)clEles.item(lh)));
           }
           NodeList propEles = docEle.getElementsByTagName("cluster");
           for(int ph =0; ph < propEles.getLength(); ph++){
               Element propsEle = (Element)propEles.item(ph);
               lbProperties.setProperty(propsEle.getAttribute("name"),propsEle.getAttribute("value"));
           }
        }catch (Exception ex){
            log.severe(stringManager.getString("upgrade.transform.startFailureMessage", ex.getMessage()));
            log.severe(stringManager.getString("upgrade.transform.startFailureCheckAccessMessage"));
            //ex.printStackTrace();  
            return false;
        }
        return true;
    }    
    public java.util.List getClusters() {
        return clusters;
    }
    public java.util.Properties getLbProperties() {
        return lbProperties;
    }  
    
}
