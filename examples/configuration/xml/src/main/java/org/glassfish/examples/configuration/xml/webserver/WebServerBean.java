/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.examples.configuration.xml.webserver;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.glassfish.hk2.xml.api.annotations.Hk2XmlPreGenerate;
import org.glassfish.hk2.xml.api.annotations.XmlIdentifier;
import org.jvnet.hk2.annotations.Contract;

/**
 * This bean defines a WebServer
 * 
 * @author jwells
 *
 */
@Contract
@Hk2XmlPreGenerate
public interface WebServerBean {
    /**
     * @return the name
     */
    @XmlAttribute(required=true)
    @XmlIdentifier
    public String getName();
    
    /**
     * @param name the name to set
     */
    public void setName(String name);
    
    /**
     * @return the address
     */
    @XmlElement
    public String getAddress();
    
    /**
     * @param address the address to set
     */
    public void setAddress(String address);
    
    /**
     * @return the adminPort
     */
    @XmlElement(defaultValue="1007")
    public int getAdminPort();
    
    /**
     * @param adminPort the adminPort to set
     */
    public void setAdminPort(int adminPort);
    
    /**
     * @return the sslPort
     */
    @XmlElement(name="SSLPort", defaultValue="81")
    public int getSSLPort();
    
    /**
     * @param sslPort the sslPort to set
     */
    public void setSSLPort(int sslPort);
    
    /**
     * @return the port
     */
    @XmlElement(defaultValue="80")
    public int getPort();
    
    /**
     * @param sshPort the port to set
     */
    public void setPort(int port);
}
