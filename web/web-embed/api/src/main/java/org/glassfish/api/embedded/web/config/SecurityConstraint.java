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
 * @author Rajiv Mordani
 */

//TODO: Need to think about if we want security to be pluggable. Need to talk to Ron.
public class SecurityConstraint {

    private Set<WebResourceCollection> webResourceCollection;
    private String[] roleNames;
    private TransportGuarantee tg;

    /**
     * Create an instance of SecurityConstraint
     */
    public SecurityConstraint() {
        
    }

    /**
     * Sets the web resource collection associated with this
     * security constrint
     *
     * @see org.glassfish.web.embed.config.WebResourceCollection
     * 
     * @param webResourceCollection the web resource collection
     * for this constraint definition
     */
    public void setWebResourceCollection(Set<WebResourceCollection> webResourceCollection) {
        this.webResourceCollection = webResourceCollection;
    }

    /**
     * Gets the web resource collection for this security constraint
     *
     * @see org.glassfish.web.embed.config.WebResourceCollection
     *
     * @return the web resource collection for this security constraint
     */
    public Set<WebResourceCollection> getWebResourceCollection() {
        return this.webResourceCollection;
    }

    /**
     * Sets the roles authorized to access the URL patterns and HTTP methods
     *
     * @param roleNames the roles authorized to access the url patterns
     * and HTTP methods. 
     */
    public void setAuthConstraint(String... roleNames) {
        this.roleNames = roleNames;
    }

    /**
     * Sets the  requirement that the constrained requests be received
     * over a protected transport layer connection. This guarantees how
     * the data will be transported between client and server. The choices
     * for type of transport guarantee include NONE, INTEGRAL, and
     * CONFIDENTIAL. If no user data constraint applies to a request, the
     * container must accept the request when received over any connection,
     * including an unprotected one.
     *
     * @see org.glassfish.web.embed.config.TransportGuarantee
     *
     * @param tg the transport guarntee
     */
    public void setUserDataConstraint(TransportGuarantee tg) {
        this.tg = tg;
    }

    /**
     * Gets the roles authorized to access the URL patterns and HTTP methods
     *
     * @return an array of roles as a <tt>String</tt> authorized to access
     * the URL patterns and HTTP methods.
     */
    public String[] getAuthConstraint() {
        return this.roleNames;
    }

    /**
     * Gets the transport guarantee requirements for this SecurityConstraint
     *
     * @see org.glassfish.web.embed.config.TransportGuarantee
     *
     * @return the transport guarantee requirement for this SecurityConstraint
     */
    public TransportGuarantee getDataConstraint() {
        return this.tg;
    }
}
