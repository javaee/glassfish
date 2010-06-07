/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.deployment.node.runtime.web;

import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.runtime.RuntimeDescriptorNode;
import com.sun.enterprise.deployment.runtime.web.ClassLoader;
import com.sun.enterprise.deployment.runtime.web.SessionConfig;
import com.sun.enterprise.deployment.runtime.web.SessionProperties;
import com.sun.enterprise.deployment.runtime.web.WebProperty;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;

import org.glassfish.deployment.common.DeploymentProperties;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This node is responsible for handling weblogic.xml container-descriptor.
 *
 * @author  Shing Wai Chan
 */
public class WLContainerDescriptorNode extends RuntimeDescriptorNode {
    /**
     * receives notification of the value for a particular tag
     * 
     * @param element the xml element
     * @param value it's associated value
     */
    public void setElementValue(XMLElement element, String value) {
        String name = element.getQName();
        if (name.equals(RuntimeTagNames.SAVE_SESSIONS_ENABLED)) {
            WebBundleDescriptor descriptor = (WebBundleDescriptor)getParentNode().getDescriptor();
            SessionProperties sessionProperties = getSessionProperties(descriptor, true);
            WebProperty webProperty = new WebProperty();
            webProperty.setAttributeValue(WebProperty.NAME, DeploymentProperties.KEEP_SESSIONS);
            webProperty.setAttributeValue(WebProperty.VALUE, value);
            sessionProperties.addWebProperty(webProperty);           
        } else if (name.equals(RuntimeTagNames.PREFER_WEB_INF_CLASSES)) {
            WebBundleDescriptor descriptor = (WebBundleDescriptor)getParentNode().getDescriptor();
            ClassLoader clBean = descriptor.getSunDescriptor().getClassLoader();
            if (clBean == null) {
                clBean = new ClassLoader();
                descriptor.getSunDescriptor().setClassLoader(clBean);
            }
            clBean.setAttributeValue(ClassLoader.DELEGATE,
                    Boolean.toString(!Boolean.parseBoolean(value)));
        } else {
            super.setElementValue(element, value);
        }
    }

    /**
     * @return the descriptor instance to associate with this XMLNode
     */    
    public Object getDescriptor() {
        return null;
    }

    public Node writeDescriptor(Element root, WebBundleDescriptor webBundleDescriptor) {
        Node containerDescriptorNode = null;
        ClassLoader clBean = webBundleDescriptor.getSunDescriptor().getClassLoader();
        SessionProperties sessionProperties = getSessionProperties(webBundleDescriptor, false);

        if  (clBean != null || sessionProperties != null) {
            containerDescriptorNode = appendChild(root, RuntimeTagNames.CONTAINER_DESCRIPTOR);
        }

        if (sessionProperties != null && sessionProperties.sizeWebProperty() > 0) {
            for (WebProperty prop : sessionProperties.getWebProperty()) {
                String name = prop.getAttributeValue(WebProperty.NAME);
                String value = prop.getAttributeValue(WebProperty.VALUE);
                if (DeploymentProperties.KEEP_SESSIONS.equals(name)) {
                    appendTextChild(containerDescriptorNode, RuntimeTagNames.SAVE_SESSIONS_ENABLED, value);
                    break;
                }
            }
        }

        if (clBean != null) {
            appendTextChild(containerDescriptorNode,
                    RuntimeTagNames.PREFER_WEB_INF_CLASSES,
                    clBean.getAttributeValue(ClassLoader.DELEGATE));
        }

        return containerDescriptorNode;
    }

    private SessionProperties getSessionProperties(
            WebBundleDescriptor webBundleDescriptor, boolean create) {

        SessionProperties sessionProperties = null;
        SessionConfig runtimeSessionConfig = webBundleDescriptor.getSunDescriptor().getSessionConfig();
        if (runtimeSessionConfig == null && create) {
            runtimeSessionConfig = new SessionConfig();
            webBundleDescriptor.getSunDescriptor().setSessionConfig(runtimeSessionConfig);
        }
        if (runtimeSessionConfig != null) {
            sessionProperties = runtimeSessionConfig.getSessionProperties();
        }

        if (sessionProperties == null && create) {
            sessionProperties = new SessionProperties();
            runtimeSessionConfig.setSessionProperties(sessionProperties);
        }
        return sessionProperties;
    }
}
