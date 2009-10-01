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

package org.glassfish.api.embedded.web.config;



import java.util.Set;

/**
 * Class through which the security related parameters for a context
 * may be configured.
 *
 * @author Rajiv Mordani
 */
public class SecurityConfig {

    private Set<SecurityConstraint> securityConstraints;
    private LoginConfig lc;

    /**
     * Create an instance of SecurityConfig
     */
    public SecurityConfig() {
        
    }

    /**
     * Set the security constraints for a context.
     *
     * @see org.glassfish.web.embed.config.SecurityConstraint
     *
     * @param securityConstraints a set of constraints for the
     * context on which this security configuration applies.
     */
    public void setSecurityConstraints(Set<SecurityConstraint> securityConstraints) {
        this.securityConstraints = securityConstraints;
    }

    /**
     * Configures the login related configuration for the context
     *
     * @see org.glassfish.web.embed.config.LoginConfig
     *
     * @param lc the login config for the context
     */
    public void setLoginConfig(LoginConfig lc) {
        this.lc = lc;
    }

    /**
     * Gets the security constraints for the context
     *
     * @see org.glassfish.web.embed.config.SecurityConstraint
     *
     * @return the security constraints for the context
     */
    public Set<SecurityConstraint> getSecurityConstraints() {
        return this.securityConstraints;
    }

    /**
     * Gets the login config for the context
     *
     * @see org.glassfish.web.embed.config.LoginConfig
     *
     * @return the login configuration for the context
     */
    public LoginConfig getLoginConfig() {
        return this.lc;
    }
}
