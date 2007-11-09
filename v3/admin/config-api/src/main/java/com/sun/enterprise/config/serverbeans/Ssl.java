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
import org.glassfish.api.admin.ConfigBean;

import java.beans.PropertyVetoException;
import java.io.Serializable;


/**
 *
 */

/* @XmlType(name = "") */
@Configured
public class Ssl
 extends ConfigBean implements Serializable {

    private final static long serialVersionUID = 1L;
    @Attribute(required = true)

    protected String certNickname;
    @Attribute

    protected String ssl2Enabled;
    @Attribute

    protected String ssl2Ciphers;
    @Attribute

    protected String ssl3Enabled;
    @Attribute

    protected String ssl3TlsCiphers;
    @Attribute

    protected String tlsEnabled;
    @Attribute

    protected String tlsRollbackEnabled;
    @Attribute

    protected String clientAuthEnabled;



    /**
     * Gets the value of the certNickname property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getCertNickname() {
        return certNickname;
    }

    /**
     * Sets the value of the certNickname property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCertNickname(String value) throws PropertyVetoException {
        support.fireVetoableChange("certNickname", this.certNickname, value);

        this.certNickname = value;
    }

    /**
     * Gets the value of the ssl2Enabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getSsl2Enabled() {
        if (ssl2Enabled == null) {
            return "false";
        } else {
            return ssl2Enabled;
        }
    }

    /**
     * Sets the value of the ssl2Enabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSsl2Enabled(String value) throws PropertyVetoException {
        support.fireVetoableChange("ssl2Enabled", this.ssl2Enabled, value);

        this.ssl2Enabled = value;
    }

    /**
     * Gets the value of the ssl2Ciphers property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getSsl2Ciphers() {
        return ssl2Ciphers;
    }

    /**
     * Sets the value of the ssl2Ciphers property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSsl2Ciphers(String value) throws PropertyVetoException {
        support.fireVetoableChange("ssl2Ciphers", this.ssl2Ciphers, value);

        this.ssl2Ciphers = value;
    }

    /**
     * Gets the value of the ssl3Enabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getSsl3Enabled() {
        if (ssl3Enabled == null) {
            return "true";
        } else {
            return ssl3Enabled;
        }
    }

    /**
     * Sets the value of the ssl3Enabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSsl3Enabled(String value) throws PropertyVetoException {
        support.fireVetoableChange("ssl3Enabled", this.ssl3Enabled, value);

        this.ssl3Enabled = value;
    }

    /**
     * Gets the value of the ssl3TlsCiphers property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getSsl3TlsCiphers() {
        return ssl3TlsCiphers;
    }

    /**
     * Sets the value of the ssl3TlsCiphers property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSsl3TlsCiphers(String value) throws PropertyVetoException {
        support.fireVetoableChange("ssl3TlsCiphers", this.ssl3TlsCiphers, value);

        this.ssl3TlsCiphers = value;
    }

    /**
     * Gets the value of the tlsEnabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getTlsEnabled() {
        if (tlsEnabled == null) {
            return "true";
        } else {
            return tlsEnabled;
        }
    }

    /**
     * Sets the value of the tlsEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTlsEnabled(String value) throws PropertyVetoException {
        support.fireVetoableChange("tlsEnabled", this.tlsEnabled, value);

        this.tlsEnabled = value;
    }

    /**
     * Gets the value of the tlsRollbackEnabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getTlsRollbackEnabled() {
        if (tlsRollbackEnabled == null) {
            return "true";
        } else {
            return tlsRollbackEnabled;
        }
    }

    /**
     * Sets the value of the tlsRollbackEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTlsRollbackEnabled(String value) throws PropertyVetoException {
        support.fireVetoableChange("tlsRollbackEnabled", this.tlsRollbackEnabled, value);

        this.tlsRollbackEnabled = value;
    }

    /**
     * Gets the value of the clientAuthEnabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getClientAuthEnabled() {
        if (clientAuthEnabled == null) {
            return "false";
        } else {
            return clientAuthEnabled;
        }
    }

    /**
     * Sets the value of the clientAuthEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setClientAuthEnabled(String value) throws PropertyVetoException {
        support.fireVetoableChange("clientAuthEnabled", this.clientAuthEnabled, value);

        this.clientAuthEnabled = value;
    }



}
