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



package com.sun.enterprise.config.serverbeans;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.component.Injectable;

import java.beans.PropertyVetoException;
import java.io.Serializable;


/**
 *
 */

/* @XmlType(name = "") */
@org.glassfish.api.amx.AMXConfigInfo( amxInterfaceName="com.sun.appserv.management.config.SSLConfig", singleton=true)
@Configured
public interface Ssl extends ConfigBeanProxy, Injectable  {

    /**
     * Gets the value of the certNickname property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(required = true)
    public String getCertNickname();

    /**
     * Sets the value of the certNickname property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCertNickname(String value) throws PropertyVetoException;

    /**
     * Gets the value of the ssl2Enabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="false", dataType=Boolean.class)
    public String getSsl2Enabled();

    /**
     * Sets the value of the ssl2Enabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSsl2Enabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the ssl2Ciphers property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getSsl2Ciphers();

    /**
     * Sets the value of the ssl2Ciphers property.
     * Values:  rc4, rc4export, rc2, rc2export, idea, des, desede3.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSsl2Ciphers(String value) throws PropertyVetoException;

    /**
     * Gets the value of the ssl3Enabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="true", dataType=Boolean.class)
    public String getSsl3Enabled();

    /**
     * Sets the value of the ssl3Enabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSsl3Enabled(String value) throws PropertyVetoException;

    /**
     A comma-separated list of the SSL3 ciphers used, with the prefix + to enable or - to 
        disable, for example +SSL_RSA_WITH_RC4_128_MD5 . Allowed values are 
        SSL_RSA_WITH_RC4_128_MD5, SSL_RSA_WITH_3DES_EDE_CBC_SHA, SSL_RSA_WITH_DES_CBC_SHA, 
        SSL_RSA_EXPORT_WITH_RC4_40_MD5, SSL_RSA_WITH_NULL_MD5, SSL_RSA_WITH_RC4_128_SHA, and 
        SSL_RSA_WITH_NULL_SHA. Values available in previous releases are supported for backward 
        compatibility.
     */
    @Attribute
    public String getSsl3TlsCiphers();

    /**
     * Sets the value of the ssl3TlsCiphers property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSsl3TlsCiphers(String value) throws PropertyVetoException;

    /**
     * Gets the value of the tlsEnabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="true", dataType=Boolean.class)
    public String getTlsEnabled();

    /**
     * Sets the value of the tlsEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTlsEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the tlsRollbackEnabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="true", dataType=Boolean.class)
    public String getTlsRollbackEnabled();

    /**
     * Sets the value of the tlsRollbackEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTlsRollbackEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the clientAuthEnabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="false", dataType=Boolean.class)
    public String getClientAuthEnabled();

    /**
     * Sets the value of the clientAuthEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setClientAuthEnabled(String value) throws PropertyVetoException;
}
