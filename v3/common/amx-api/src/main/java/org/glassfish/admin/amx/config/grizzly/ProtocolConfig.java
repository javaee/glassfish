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
package org.glassfish.admin.amx.config.grizzly;

import com.sun.appserv.management.config.DefaultValues;
import com.sun.appserv.management.config.PropertiesAccess;
import com.sun.appserv.management.config.NamedConfigElement;
import com.sun.appserv.management.config.SSLConfig;
import com.sun.appserv.management.config.ConfigCreator;
import com.sun.appserv.management.config.ConfigRemover;
import com.sun.appserv.management.config.ResolveTo;


/**
 * Defines one single high-level protocol like:
 * http, https, iiop, etc.
 */
public interface ProtocolConfig extends PropertiesAccess, NamedConfigElement, DefaultValues, ConfigCreator, ConfigRemover {
    /**
     * Gets the {@link ProtocolConfig} security status. True means the protocol is
     * secured and the {@link SSLConfig} member will be used to initialize security
     * settings. False means that the {@link ProtocolConfig} is not secured and
     * the {@link SSLConfig} member, if present, will be ignored.
     *
     * @return the {@link Protocol} security status
     */
    @ResolveTo(Boolean.class)
    public String getSecurityEnabled();
    public void setSecurityEnabled(String securityEnabled);

    /**
     * Get the max temporary {@link SelectorConfig} number, which could be used by
     * this {@link ProtocolConfig}
     *
     * @return the max temporary {@link SelectorConfig} number, which could be used by
     * this {@link ProtocolConfig}
     */
    @ResolveTo(Integer.class)
    public String getMaxSelectors();
    public void setMaxSelectors(String maxSelectors);

    /**
     * Get the type of ByteBuffer, which will be used with the protocol.
     * Possible values are: HEAP and DIRECT
     *
     * @return the type of ByteBuffer, which will be used with the protocol.
     */
    public String getByteBufferType();
    public void setByteBufferType(String byteBufferType);

    /**
     * Get the read operation timeout in seconds, which will be used for this
     * {@link ProtocolConfig}
     *
     * @return the read operation timeout in seconds, which will be used for 
     * this {@link ProtocolConfig}
     */
    @ResolveTo(Integer.class)
    public String getReadTimeoutSeconds();
    public void setReadTimeoutSeconds(String readTimeoutSeconds);

    /**
     * Get the write operation timeout in seconds, which will be used for this
     * {@link ProtocolConfig}
     *
     * @return the write operation timeout in seconds, which will be used for
     * this {@link ProtocolConfig}
     */
    @ResolveTo(Integer.class)
    public String getWriteTimeoutSeconds();
    public void setWriteTimeoutSeconds(String writeTimeoutSeconds);

    /**
     * When the oob-inline option is set, any TCP urgent
     * data received on the socket will be received through
     * the socket input stream. Boolean attribute, possible
     * values are true or false
     *
     * Gets the protocol oob inline parameter
     *
     * @return the protocol oob inline parameter
    @Attribute
    public String getOobInline();
    public void setOobInline(String oobInline);

    /**
     * Get the {@link Protocol> SSL configuration
     *
     * @return the {@link Protocol> SSL configuration
     */
    public SSLConfig getSSLConfig();

    /**
     * Gets the {@link PortUnificationConfig} logic, if it is required to handle
     * more than one high level protocol on a single listener
     *
     * @return the {@link PortUnificationConfig} logic, if it is required to handle
     * more than one high level protocol on a single listener
     */
    public PortUnificationConfig getPortUnificationConfig();

    /**
     * Gets the {@link ProtocolChainInstanceHandlerConfig} configuration
     *
     * @return the {@link ProtocolChainInstanceHandlerConfig} configuration
     */
    public ProtocolChainInstanceHandlerConfig getProtocolChainInstanceHandlerConfig();
}









