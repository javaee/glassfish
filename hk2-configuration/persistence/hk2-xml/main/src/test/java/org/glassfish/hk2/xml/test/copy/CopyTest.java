/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
package org.glassfish.hk2.xml.test.copy;

import java.net.URL;
import java.util.List;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.xml.api.XmlHk2ConfigurationBean;
import org.glassfish.hk2.xml.api.XmlRootCopy;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.test.beans.AuthorizationProviderBean;
import org.glassfish.hk2.xml.test.beans.DomainBean;
import org.glassfish.hk2.xml.test.beans.JMSServerBean;
import org.glassfish.hk2.xml.test.beans.MachineBean;
import org.glassfish.hk2.xml.test.beans.QueueBean;
import org.glassfish.hk2.xml.test.beans.SecurityManagerBean;
import org.glassfish.hk2.xml.test.beans.ServerBean;
import org.glassfish.hk2.xml.test.beans.TopicBean;
import org.glassfish.hk2.xml.test.dynamic.merge.MergeTest;
import org.glassfish.hk2.xml.test.dynamic.rawsets.UpdateListener;
import org.glassfish.hk2.xml.test.utilities.Utilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class CopyTest {
    
    /**
     * Tests a deep tree including all metadata
     * 
     * @throws Exception
     */
    @Test
    // @org.junit.Ignore
    public void testCopyOfDeepTree() throws Exception {
        ServiceLocator locator = Utilities.createLocator(UpdateListener.class);
        XmlService xmlService = locator.getService(XmlService.class);
        Hub hub = locator.getService(Hub.class);
        
        URL url = getClass().getClassLoader().getResource(MergeTest.DOMAIN1_FILE);
        
        XmlRootHandle<DomainBean> rootHandle = xmlService.unmarshal(url.toURI(), DomainBean.class);
        
        MergeTest.verifyDomain1Xml(rootHandle, hub, locator);
        
        // All above just verifying the pre-state
        XmlRootCopy<DomainBean> copy = rootHandle.getXmlRootCopy();
        
        MergeTest.verifyDomain1Xml(rootHandle, copy, null, null);
        
        DomainBean domainCopy = copy.getChildRoot();
        DomainBean domainOriginal = rootHandle.getRoot();
        
        verifyMetadataTheSame((XmlHk2ConfigurationBean) domainOriginal, (XmlHk2ConfigurationBean) domainCopy);
        
        SecurityManagerBean securityManagerCopy = domainCopy.getSecurityManager();
        SecurityManagerBean securityManagerOriginal = domainCopy.getSecurityManager();
        
        verifyMetadataTheSame((XmlHk2ConfigurationBean) securityManagerOriginal, (XmlHk2ConfigurationBean) securityManagerCopy);
        
        AuthorizationProviderBean atzProviderCopy = securityManagerCopy.getAuthorizationProviders().get(0);
        AuthorizationProviderBean atzProviderOriginal = securityManagerOriginal.getAuthorizationProviders().get(0);
        
        verifyMetadataTheSame((XmlHk2ConfigurationBean) atzProviderOriginal, (XmlHk2ConfigurationBean) atzProviderCopy);
        
        MachineBean machineCopy = domainCopy.getMachines().get(0);
        MachineBean machineOriginal = domainOriginal.getMachines().get(0);
        
        verifyMetadataTheSame((XmlHk2ConfigurationBean) machineOriginal, (XmlHk2ConfigurationBean) machineCopy);
        
        ServerBean serverCopy = machineCopy.getServers().get(0);
        ServerBean serverOriginal = machineOriginal.getServers().get(0);
        
        verifyMetadataTheSame((XmlHk2ConfigurationBean) serverOriginal, (XmlHk2ConfigurationBean) serverCopy);
        
        JMSServerBean jmsServersCopy[] = domainCopy.getJMSServers();
        JMSServerBean jmsServersOriginal[] = domainOriginal.getJMSServers();
        
        Assert.assertEquals(jmsServersCopy.length, jmsServersOriginal.length);
        
        for (int lcv = 0; lcv < jmsServersOriginal.length; lcv++) {
            JMSServerBean jmsServerCopy = jmsServersCopy[lcv];
            JMSServerBean jmsServerOriginal = jmsServersOriginal[lcv];
            
            verifyMetadataTheSame((XmlHk2ConfigurationBean) jmsServerOriginal, (XmlHk2ConfigurationBean) jmsServerCopy);
            
            {
                List<TopicBean> topicsCopy = jmsServerCopy.getTopics();
                List<TopicBean> topicsOriginal = jmsServerOriginal.getTopics();
            
                Assert.assertEquals(topicsCopy.size(), topicsOriginal.size());
            
                for (int lcv1 = 0; lcv1 < topicsOriginal.size(); lcv1++) {
                    TopicBean topicCopy = topicsCopy.get(lcv1);
                    TopicBean topicOriginal = topicsOriginal.get(lcv1);
                
                    verifyMetadataTheSame((XmlHk2ConfigurationBean) topicOriginal, (XmlHk2ConfigurationBean) topicCopy);
                }
            }
            
            {
                QueueBean queuesCopy[] = jmsServerCopy.getQueues();
                QueueBean queuesOriginal[] = jmsServerOriginal.getQueues();
            
                Assert.assertEquals(queuesCopy.length, queuesOriginal.length);
            
                for (int lcv1 = 0; lcv1 < queuesOriginal.length; lcv1++) {
                    QueueBean queueCopy = queuesCopy[lcv1];
                    QueueBean queueOriginal = queuesOriginal[lcv1];
                
                    verifyMetadataTheSame((XmlHk2ConfigurationBean) queueOriginal, (XmlHk2ConfigurationBean) queueCopy);
                }
            }
            
        }
    }
    
    private static void verifyMetadataTheSame(XmlHk2ConfigurationBean original, XmlHk2ConfigurationBean copy) {
        Assert.assertEquals("xmlPath does not match", original._getXmlPath(), copy._getXmlPath());
        Assert.assertEquals("instanceName does not match", original._getInstanceName(), copy._getInstanceName());
        Assert.assertEquals("keyPropertyName does not match", original._getKeyPropertyName(), copy._getKeyPropertyName());
        Assert.assertEquals("keyValue does not match", original._getKeyValue(), copy._getKeyValue());
    }

}
