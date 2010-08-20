/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
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

package org.glassfish.api.embedded.web;

import org.apache.catalina.connector.Connector;
import org.glassfish.api.embedded.LifecycleException;
import org.glassfish.api.embedded.web.config.SslConfig;
import org.glassfish.api.embedded.web.config.SslType;
import org.glassfish.api.embedded.web.config.WebListenerConfig;

import java.io.File;
import java.util.Set;

/**
 * @author Rajiv Mordani
 * @author Amy Roh
 * TODO
 */
public class HttpsListener extends Connector implements WebListener  {


    private WebListenerConfig config;

    private SslConfig sslConfig;
    
    /**
     * Sets the id for this <tt>WebListener</tt>.
     */
    public void setId(String id) {
        setName(id);
    }

    /**
     * Gets the id of this <tt>WebListener</tt>.
     */
    public String getId() {
        return getName();
    }

    /**
     * Reconfigures this <tt>WebListener</tt> with the given
     * configuration.
     */
    public void setConfig(WebListenerConfig config) throws ConfigException {
        this.config = config;
        setAllowTrace(config.isTraceEnabled());
    }

    /**
     * Gets the current configuration of this <tt>WebListener</tt>.
     */
    public WebListenerConfig getConfig() {
        return config;
    }

    /**
     * Enables this component.
     */
    public void enable() throws LifecycleException {
       try {
            start();
        } catch (org.apache.catalina.LifecycleException e) {
            throw new LifecycleException(e);
        }
    }

    /**
     * Disables this component.
     */
    public void disable() throws LifecycleException {
       try {
            stop();
        } catch (org.apache.catalina.LifecycleException e) {
            throw new LifecycleException(e);
        }
    }

    /**
     * Sets the SSL configuration for this web listener
     */
    public void setSslConfig(SslConfig sslConfig) {

        this.sslConfig = sslConfig;
        /* TODO
        setKeystoreFile(sslConfig.getKeyStore());
        setKeystorePass(sslConfig.getKeyPassword());
        sslConfig.getTrustStore();
        sslConfig.getHandshakeTimeout();
        sslConfig.getAlgorithms();
        */
    }

    /**
     * Gets the SslConfig for this web listener
     */
    public SslConfig getSslConfig() {
        return sslConfig;
    }
    
    
}
