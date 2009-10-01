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

package org.glassfish.api.embedded.web.config;;

import org.glassfish.api.embedded.web.config.FormLoginConfig;
import org.glassfish.api.embedded.web.config.AuthMethod;

/**
 * The class configures the authentication related parameters like,
 * authentication method, form login configuration, if authentication method
 * is form based authentication, the realm name and the realm type.
 *
 * @see org.glassfish.web.embed.config.SecurityConfig
 *
 * @author Rajiv Mordani
 */
public class LoginConfig {

    private AuthMethod authMethod;
    private FormLoginConfig flc;
    String realmName;
    RealmType realmType;

    /**
     * Create an instance of <tt>LoginConfig</tt>.
     */
    public LoginConfig() {

    }

    /**
     * Set the authentication scheme to be used for a given
     * context
     *
     * @param authMethod one of the supported auth methods as
     * defined in <tt>AuthMethod</tt> enumeration
     */
    public void setAuthMethod(AuthMethod authMethod) {
        this.authMethod = authMethod;
    }

    /**
     * Gets the auth method for the context
     * @return the authmethod for the context
     */
    public AuthMethod getAuthMethod() {
        return this.authMethod;        
    }

    /**
     * Sets the realm name to be used for the context
     *
     * @param realmName the realm name for the context
     */
    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }

    /**
     * Gets the realm name set for the context
     *
     * @return the realm name for the context
     */
    public String getRealmName() {
        return this.realmName;
    }

    /**
     * Sets the realm type for the context. The realm type
     * must be one of the supported realm types as defined in the
     * <tt>RealmType</tt> enumeration
     *
     * @see org.glassfish.web.embed.config.RealmType
     *
     * @param realmType the realm type for the context
     */
    public void setRealmType(RealmType realmType) {
        this.realmType = realmType;
    }

    /**
     * Gets the realm type for the context
     *
     * @see org.glassfish.web.embed.config.RealmType
     * 
     * @return the realm type for the context
     */
    public RealmType getRealmType() {
        return this.realmType;
    }

    /**
     * Set the form login configuration, if the authentication
     * method is form based authentication
     *
     * @see org.glassfish.web.embed.config.FormLoginConfig
     * 
     * @param flc form login configuration
     */
    public void setFormLoginConfig(FormLoginConfig flc) {
        this.flc = flc;        
    }

    /**
     * Gets the form login config, or <tt>null</tt> if
     * the authentication scheme is not form based login.
     *
     * @see org.glassfish.web.embed.config.FormLoginConfig
     * 
     * @return form login configuration
     */
    public FormLoginConfig getFormLoginConfig() {
        return this.flc;        
    }
}
