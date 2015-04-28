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
package org.glassfish.examples.configuration.xml.webserver.internal;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.glassfish.hk2.api.IterableProvider;
import org.glassfish.hk2.configuration.api.Configured;
import org.glassfish.hk2.configuration.api.ConfiguredBy;
import org.glassfish.hk2.configuration.api.Dynamicity;
import org.jvnet.hk2.annotations.Service;

/**
 * @author jwells
 *
 */
@Service
// The name in @ConfiguredBy is the xpath to the XML that configures this service
@ConfiguredBy("/application/web-server")
public class WebServer {
    @Configured
    private String name;
    
    @Configured
    private int adminPort;
    
    @Configured(dynamicity=Dynamicity.FULLY_DYNAMIC)
    private String address;
    
    private int sslPort;
    private int port;
    
    private boolean opened = false;
    
    /**
     * These are configured services that can be used to get other
     * variable information about the WebServer.  In this case
     * it is getting information about the certificates that
     * this server can use for SSL
     *
    @Inject
    private IterableProvider<SSLCertificateService> certificates;
     */
    
    /**
     * This method is called to set the port and sshPort.  It is guaranteed that
     * the server will not have these ports open at the time this method is called.
     * That is guaranteed since the ports are not open until the postConstruct method
     * is called on boot, and it is only called between the startDynamicConfiguration
     * and finishDynamicConfiguration methods when a dynamic configuration change is
     * made
     * 
     * @param sshPort The sshPort to use
     * @param port The port to use
     */
    @SuppressWarnings("unused")
    private void setUserPorts(
            @Configured(value="SSLPort", dynamicity=Dynamicity.FULLY_DYNAMIC) int sslPort,
            @Configured(value="port", dynamicity=Dynamicity.FULLY_DYNAMIC) int port) {
        this.sslPort = sslPort;
        this.port = port;
    }
    
    @PostConstruct
    private void postConstruct() {
        opened = true;
    }
    
    @PreDestroy
    private void preDestroy() {
        opened = false;
    }

    /* (non-Javadoc)
     * @see org.glassfish.examples.configuration.webserver.WebServer#getCertificates()
     *
    @Override
    public List<File> getCertificates() {
        LinkedList<File> retVal = new LinkedList<File>();
        
        for (SSLCertificateService certService : certificates) {
            retVal.add(certService.getCertificate());
        }
        
        return retVal;
    }
    */
    
    @Override
    public String toString() {
        return "WebServer(name=" + name + ",adminPort=" + adminPort + ",port=" + port + ",sslPort=" + sslPort + ",opened=" + opened + ")";
    }

}
