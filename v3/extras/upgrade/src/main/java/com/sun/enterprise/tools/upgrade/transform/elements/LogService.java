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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import com.sun.enterprise.tools.upgrade.transform.ElementToObjectMapper;
import java.util.logging.Level;

public class LogService extends BaseElement {

    /** Creates a new instance of Element */
    public LogService() {
    }
    /**
     * element - log-service
     * parentSource - parent server of element
     * parentResult - domain
     */
    public void transform(Element element, Element parentSource, Element parentResult){

        logger.log(Level.FINE, stringManager.getString("upgrade.transform.transformingMSG", this.getClass().getName(), element.getTagName()));
        NodeList logServs = parentResult.getElementsByTagName("log-service");
        Element logService = null;
        logger.log(Level.FINE, this.getClass().getName() +  ":: logServs.getLength() ", logServs.getLength());
        if(logServs.getLength() == 0){
            logService = parentResult.getOwnerDocument().createElement("log-service");
            this.transferAttributes(element, logService, null);
			this.appendElementToParent(parentResult,logService);
        }else {
            logService = (Element)logServs.item(0);
            this.transferAttributes(element, logService, null);
        }

        super.transform(element,  parentSource, logService);
    }
    // Over ride the method.
    protected void transferAttributes(Element source, Element result, java.util.List nonTransferList){        
            nonTransferList = new java.util.Vector();
            super.transferAttributes(source, result, nonTransferList);
    }
    private void setModuleLevelLogs(Element sourceLogService, Element resultLogService){
        // This method is called only for AS 7.x.  log-levels are defined for many source attributes, that need to be set to module level. in target
        NodeList modLogLevels = resultLogService.getElementsByTagName("module-log-levels");
        Element moduleLogLevels = null;
        if(modLogLevels.getLength() == 0){
            moduleLogLevels = resultLogService.getOwnerDocument().createElement("module-log-levels");
            resultLogService.appendChild(moduleLogLevels);
        }else{
            moduleLogLevels = (Element)modLogLevels.item(0);
        }
        // ejb-container from source
        //(http-service , iiop-service , admin-service? , web-container , ejb-container , mdb-container , jms-service , log-service , security-service , transaction-service , java-config , resources , applications , availability-service? , property*)
        //root, server, ejb-container, cmp-container, mdb-container, web-container, classloader, configuration, naming, security, jts, jta, admin, deployment, verifier, jaxr, jaxrpc, saaj, corba, javamail, jms, connector, jdo, cmp, util, resource-adapter, synchronization, node-agent
        setLogLevelAttribute(sourceLogService,"ejb-container", "log-level", moduleLogLevels, "ejb-container");
        setLogLevelAttribute(sourceLogService,"web-container", "log-level", moduleLogLevels, "web-container");
        setLogLevelAttribute(sourceLogService,"mdb-container", "log-level", moduleLogLevels, "mdb-container");
        setLogLevelAttribute(sourceLogService,"orb", "log-level", moduleLogLevels, "corba");
        setLogLevelAttribute(sourceLogService,"admin-service", "log-level", moduleLogLevels, "admin");
        setLogLevelAttribute(sourceLogService,"jms-service", "log-level", moduleLogLevels, "jms");
        setLogLevelAttribute(sourceLogService,"log-service", "level", moduleLogLevels, "server");
        setLogLevelAttribute(sourceLogService,"log-service", "level", moduleLogLevels, "root");
        setLogLevelAttribute(sourceLogService,"security-service", "log-level", moduleLogLevels, "security");
        setLogLevelAttribute(sourceLogService,"transaction-service", "log-level", moduleLogLevels, "jts");
    }
    private void setLogLevelAttribute(Element sourceLogService, String srcEle, String srcAttrName, Element moduleLogLevels, String targetAttrName){
        NodeList srcEles = sourceLogService.getOwnerDocument().getElementsByTagName(srcEle);
        if((srcEles != null) && (srcEles.getLength() > 0)){
            String logLevelAttrValue = ((Element)srcEles.item(0)).getAttribute(srcAttrName);
            if((logLevelAttrValue != null) && (!logLevelAttrValue.trim().equals(""))){
                if(logLevelAttrValue.equals("ALERT") || logLevelAttrValue.equals("FATAL")){
                    logLevelAttrValue = "SEVERE";
                }
                moduleLogLevels.setAttribute(targetAttrName, logLevelAttrValue);
            }
        }
    }
}
