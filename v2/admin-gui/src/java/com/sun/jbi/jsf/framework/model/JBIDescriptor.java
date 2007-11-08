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
 * JBIDescriptor.java
 * 
 * @author ylee
 */

package com.sun.jbi.jsf.framework.model;

import com.sun.jbi.jsf.framework.common.JbiConstants;
import com.sun.jbi.jsf.framework.common.Util;
import com.sun.jbi.jsf.framework.common.XmlUtils;
import com.sun.jbi.jsf.util.JBILogger;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;


public class JBIDescriptor {
    
    /** xml document */
    protected Document xmlDoc = null;
    /** xml descriptor text */
    protected String xmlText = null;    
    /** jbi element */
    protected Element jbiElement = null;
    /** version */
    protected String version;
    /** schemaLocation */
    protected String schemaLocation;
    /** namespaces */
    protected Map namespaces = new HashMap();
    
    /** get logger instance */
    private Logger logger = JBILogger.getInstance();
    
    public JBIDescriptor() {
    }
    
    /**
     * @param xmlText   xml text of the descriptor
     */
    public JBIDescriptor(String xmlText) {
        this.xmlText = xmlText;
    }
    
    public String getVersion() {
        return version;
    }
    
    public String getSchemaLocation() {
        return schemaLocation;
    }
    
    public String getNamespaceValue(String ns) {
        return (String)namespaces.get(ns);
    }
    
    public void parse() {
        xmlDoc = XmlUtils.buildDomDocument(xmlText);
        
        // parse namespaces
        jbiElement = XmlUtils.getElement(xmlDoc, JbiConstants.JBI_TAG);
        if ( jbiElement!=null ) {
            namespaces.clear();
            NamedNodeMap attrMap = jbiElement.getAttributes();
            if ( attrMap!=null ) {
                for (int i=0; i<attrMap.getLength(); i++) {
                    Attr attr = (Attr)attrMap.item(i);
                    String value = attr.getValue();
                    String name = attr.getName();
                    String prefix = attr.getPrefix();
                    if ( name!=null ) {
                        if ( name.startsWith(JbiConstants.NAMESPACE_PREFIX) ) {
                            // stripoff prefix
                            String n = Util.trimLeft(name,JbiConstants.NAMESPACE_PREFIX);
                            namespaces.put(n, value);
                        } else if ( name.equals(JbiConstants.VERSION) ) {
                            version = value;
                        } else if ( name.startsWith(JbiConstants.SCHEMA_PREFIX+JbiConstants.SCHEMA_LOCATION) ) {
                            schemaLocation = value;
                        }
                    }
                }
            }
        }
        
    }

    
     public static void main(String[] args) {
          String xmlText = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><jbi  xmlns=\"http://java.sun.com/xml/ns/jbi\" xmlns:ns1=\"http://localhost/SynchronousSample/SynchronousSample\"  xmlns:ns2=\"http://enterprise.netbeans.org/bpel/SynchronousSample/SynchronousSample_1\"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"1.0\" xsi:schemaLocation=\"http://java.sun.com/xml/ns/jbi ./jbi.xsd\"> <service-assembly> <identification> <name>SynchronousSampleApplication</name> <description>Represents the Service Assembly of SynchronousSampleApplication</description> </identification> <service-unit> <identification>        <name>SynchronousSampleApplication-SynchronousSample</name> <description>Represents this Service Unit</description>   </identification> <target> <artifacts-zip>SynchronousSample.jar</artifacts-zip>       <component-name>sun-bpel-engine</component-name>  </target> </service-unit> <service-unit> <identification>        <name>SynchronousSampleApplication-sun-http-binding</name> <description>Represents this Service Unit</description>     </identification> <target> <artifacts-zip>sun-http-binding.jar</artifacts-zip>        <component-name>sun-http-binding</component-name> </target> </service-unit> <connections> <connection> <consumer  endpoint-name=\"port1\" service-name=\"ns1:service1\"/> <provider endpoint-name=\"partnerlinktyperole1_myRole\" service-name=\"ns2:SynchronousSample\"/> </connection> </connections> </service-assembly> </jbi>";
          JBIDescriptor desc = new JBIDescriptor(xmlText);
          desc.parse();
     }

}
