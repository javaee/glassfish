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
package org.glassfish.hk2.xml.test.beans;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.glassfish.hk2.xml.api.annotations.PluralOf;
import org.jvnet.hk2.annotations.Contract;

/**
 * @author jwells
 *
 */
@XmlRootElement(name="domain") @Contract
@XmlType(propOrder={
        "name"
        , "securityManager"
        , "machines"
        , "JMSServers"
        , "HTTPFactories"
        , "HTTPSFactories"
        , "subnetwork"
        , "taxonomy"
        , "diagnostics"})
public interface DomainBean extends NamedBean {
    @XmlElement(name="security-manager")
    public SecurityManagerBean getSecurityManager();
    public void setSecurityManager(SecurityManagerBean secBean);
    public boolean removeSecurityManager();
    
    @XmlElement(name="machine")
    public List<MachineBean> getMachines();
    public void setMachines(List<MachineBean> machines);
    public void addMachine(MachineBean machine);
    public MachineBean removeMachine(String machine);
    public MachineBean lookupMachine(String machine);
    
    @XmlElement(name="jms-server")
    public JMSServerBean[] getJMSServers();
    public void setJMSServers(JMSServerBean[] jmsServers);
    public void addJMSServer(JMSServerBean jmsServer);
    public JMSServerBean removeJMSServer(String jmsServer);
    public JMSServerBean lookupJMSServer(String name);
    
    @XmlElement(name="http-factory") @PluralOf("HTTPFactory")
    public List<HttpFactoryBean> getHTTPFactories();
    public void setHTTPFactories(List<HttpFactoryBean> httpFactories);
    public HttpFactoryBean addHTTPFactory(HttpFactoryBean factory);
    public HttpFactoryBean removeHTTPFactory(HttpFactoryBean factory);
    
    @XmlElement(name="https-factory") @PluralOf("HTTPSFactory")
    public HttpsFactoryBean[] getHTTPSFactories();
    public void setHTTPSFactories(HttpsFactoryBean[] httpsFactories);
    public HttpsFactoryBean addHTTPSFactory(HttpFactoryBean factory);
    public void removeHTTPSFactory(HttpsFactoryBean factory);
    public void removeHTTPSFactory(int index);
    
    @XmlElement(name="subnetwork", defaultValue="0.0.0.255")
    public String getSubnetwork();
    public void setSubnetwork(String subnetwork);
    
    @XmlElement(name="taxonomy")
    public String getTaxonomy();
    public void setTaxonomy(String taxonomy);
    
    @XmlElement(name="diagnostics")
    public DiagnosticsBean getDiagnostics();
    public void setDiagnostics(DiagnosticsBean above);
}
