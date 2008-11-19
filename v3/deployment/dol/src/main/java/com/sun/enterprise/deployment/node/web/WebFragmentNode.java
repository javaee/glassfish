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

package com.sun.enterprise.deployment.node.web;

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.node.*;
import com.sun.enterprise.deployment.types.EjbReference;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.web.LoginConfiguration;
import com.sun.enterprise.deployment.xml.TagNames;
import com.sun.enterprise.deployment.xml.WebServicesTagNames;
import com.sun.enterprise.deployment.xml.WebTagNames;
import org.w3c.dom.Node;

import java.util.*;

/**
 * This node is responsible for handling the web-fragment xml tree
 *
 * @author  Shing Wai Chan
 * @version 
 */
public class WebFragmentNode extends WebBundleNode {

   public final static XMLElement tag = new XMLElement(WebTagNames.WEB_FRAGMENT);

    /** 
     * The system ID of my documents. 
     */
    public final static String SCHEMA_ID = "web-fragment_3_0.xsd";
    private final static List<String> systemIDs = initSystemIDs();

    private static List<String> initSystemIDs() {
        List<String> systemIDs = new ArrayList<String>();
        systemIDs.add(SCHEMA_ID);
        return Collections.unmodifiableList(systemIDs);
    }
    
   /**
    * register this node as a root node capable of loading entire DD files
    * 
    * @param publicIDToDTD is a mapping between xml Public-ID to DTD 
    * @return the doctype tag name
    */    
    public static String registerBundle(Map publicIDToDTD) {
        return tag.getQName();
    }


    /** Creates new WebBundleNode */
    public WebFragmentNode()  {
        super();
    }
    
   /**
     * @return the XML tag associated with this XMLNode
     */
    protected XMLElement getXMLRootTag() {
        return tag;
    }       

    /**
     * @return the DOCTYPE of the XML file
     */
    public String getDocType() {
        return null;
    }
    
    /**
     * @return the SystemID of the XML file
     */
    public String getSystemID() {
        return SCHEMA_ID;
    }

    /**
     * @return the list of SystemID of the XML schema supported
     */
    public List<String> getSystemIDs() {
        return systemIDs;
    }

   /**
    * @return the descriptor instance to associate with this XMLNode
    */
    public WebBundleDescriptor getDescriptor() {
        // no default bundle for web-fragment
        if (descriptor==null) {
            descriptor = (WebBundleDescriptor) DescriptorFactory.getDescriptor(getXMLPath());
        }
        return descriptor;
    }  

    
}
