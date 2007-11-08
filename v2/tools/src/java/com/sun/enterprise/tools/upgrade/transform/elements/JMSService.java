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
 * Element.java
 *
 * Created on August 4, 2003, 2:04 PM
 */

package com.sun.enterprise.tools.upgrade.transform.elements;

/**
 *
 * @author  prakash
 */
import java.util.Vector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import com.sun.enterprise.tools.upgrade.transform.ElementToObjectMapper;
import java.util.logging.Level;

public class JMSService extends BaseElement {

    /** Creates a new instance of Element */
    public JMSService() {
    }
    /**
     * element - jms-service
     * parentSource - parent server of element
     * parentResult - domain
     */
    public void transform(Element element, Element parentSource, Element parentResult){
         logger.log(Level.FINE, stringManager.getString("upgrade.transform.transformingMSG", 
                 this.getClass().getName(), element.getTagName()));               
         Vector doNotTransferList = new Vector();
        // There must be only one or zero jms-service.
        // the destination has jms-host* as child elements which inherits most of the attributes from source jms-sesrvice
        NodeList resultJMSServices = parentResult.getElementsByTagName("jms-service");
        Element resultJMSService = null;
        logger.log(Level.FINE, this.getClass().getName() +  ":: resultJMSServices.getLength() ", resultJMSServices.getLength());
        if(resultJMSServices.getLength() == 0){
            NodeList config = parentResult.getElementsByTagName("config");
            resultJMSService = parentResult.getOwnerDocument().createElement("jms-service");
            this.transferAttributes(element, resultJMSService, doNotTransferList);
            this.appendElementToParent((Element)config.item(0),resultJMSService);
        }else {
            resultJMSService = (Element)resultJMSServices.item(0);
            this.transferAttributes(element, resultJMSService, doNotTransferList);
        }
        super.transform(element,  parentSource, resultJMSService);
    }
    // Over ride the method.
    protected void transferAttributes(Element source, Element result, java.util.List nonTransferList){
        if(commonInfoModel.getSourceVersion().equals(com.sun.enterprise.tools.upgrade.common.UpgradeConstants.VERSION_7X)){
            // Transfer init-timeout-in-seconds from source to result.
            int sourceSec = Integer.parseInt(source.getAttribute("init-timeout-in-seconds"));
            if(sourceSec < 60) sourceSec = 60;
            // init-timeout-in-seconds is 60 by default in domain.xml
            result.setAttribute("init-timeout-in-seconds", String.valueOf(sourceSec));
            // start-args is an implied attribute, should it be really transferred?
            result.setAttribute("start-args", source.getAttribute("start-args"));
            // Now other attributes from source should be transferred to jms-host element in the result. There can be zero or more jms-hosts
            NodeList resultJMSHosts = result.getElementsByTagName("jms-host");
            Element resultJMSHost = null;
            if(resultJMSHosts.getLength() == 0){
                resultJMSHost = result.getOwnerDocument().createElement("jms-host");
                resultJMSHost.setAttribute("name", "default_JMS_host");
                result.appendChild(resultJMSHost);
            }else {
                for(int index=0; index < resultJMSHosts.getLength(); index++){
                    if(((Element)resultJMSHosts.item(index)).getAttribute("name").equals("default_JMS_host")){
                        resultJMSHost = (Element)resultJMSHosts.item(index);
                    }
                }
            }
            resultJMSHost.setAttribute("port",source.getAttribute("port"));
            // Are you sure you want to transfer these?????
            resultJMSHost.setAttribute("admin-user-name",source.getAttribute("admin-user-name"));
            resultJMSHost.setAttribute("admin-password",source.getAttribute("admin-password"));
        }else{
            super.transferAttributes(source, result, nonTransferList);
        }
    }
}
