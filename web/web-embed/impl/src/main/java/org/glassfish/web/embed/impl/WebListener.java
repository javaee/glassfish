/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 */

package org.glassfish.web.embed.impl;

import org.glassfish.api.embedded.LifecycleException;
import org.glassfish.api.embedded.web.ConfigException;
import org.glassfish.api.embedded.web.config.WebListenerConfig;
import org.apache.catalina.connector.Connector;

/**
 * Representation of a network listener for web requests.
 *
 * <p>Instances of <tt>WebListener</tt> may be in one of two states:
 * <i>stopped</i> or <i>started</i>.
 *
 * @author Amy Roh
 */
public class WebListener extends Connector 
        implements org.glassfish.api.embedded.web.WebListener {

    private WebListenerConfig config;
    
    /**
     * Sets the id for this <tt>WebListener</tt>.
     *
     * @param the id for this <tt>WebListener</tt>
     */
    public void setId(String id) {
        setName(id);
    }
    
    /**
     * Gets the id of this <tt>WebListener</tt>.
     *
     * @return the id of this <tt>WebListener</tt>
     */
    public String getId() {
        return getName();
    }

    /**
     * Reconfigures this <tt>WebListener</tt> with the given
     * configuration.
     *
     * <p>In order for the given configuration to take effect, this
     * <tt>WebListener</tt> may be stopped and restarted.
     *
     * @param config the configuration to be applied
     * 
     * @throws ConfigException if the configuration requires a restart,
     * and this <tt>WebListener</tt> fails to be restarted
     */
    public void setConfig(WebListenerConfig config) throws ConfigException {
        this.config = config;
        setAllowTrace(config.isTraceEnabled());
    }

    /**
     * Gets the current configuration of this <tt>WebListener</tt>.
     *
     * @return the current configuration of this <tt>WebListener</tt>,
     * or <tt>null</tt> if no special configuration was ever applied to this
     * <tt>WebListener</tt>
     */
    public WebListenerConfig getConfig() {
        return config;
    }
        
    /**
     * Enables this component.
     * 
     * @throws org.glassfish.api.embedded.LifecycleException if this component fails to be enabled
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
     * 
     * @throws LifecycleException if this component fails to be disabled
     */
    public void disable() throws LifecycleException {
       try {
            stop();
        } catch (org.apache.catalina.LifecycleException e) {
            throw new LifecycleException(e);
        }        
    }

}
