/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.enterprise.connectors.authentication;

import java.io.Serializable;
import java.util.List;

/**
 * @author Kanwar Oberoi
 */
public class ConnectorSecurityMap implements Serializable {
    private String name;

    private List<String> principals;

    private List<String> userGroups;

    private EisBackendPrincipal backendPrincipal;

    public ConnectorSecurityMap(String name, List<String> principals,
                                List<String> userGroups, EisBackendPrincipal backendPrincipal) {
        this.name = name;
        this.principals = principals;
        this.userGroups = userGroups;
        this.backendPrincipal = backendPrincipal;
    }

    /**
     * @return Returns the backendPrincipal.
     */
    public EisBackendPrincipal getBackendPrincipal() {
        return this.backendPrincipal;
    }

    /**
     * @param backendPrincipal The backendPrincipal to set.
     */
    public void setBackendPrincipal(EisBackendPrincipal backendPrincipal) {
        this.backendPrincipal = backendPrincipal;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the principals.
     */
    public List<String> getPrincipals() {
        return this.principals;
    }

    /**
     * @param principals The principals to set.
     */
    public void setPrincipals(List<String> principals) {
        this.principals = principals;
    }

    /**
     * @return Returns the userGroups.
     */
    public List<String> getUserGroups() {
        return this.userGroups;
    }

    /**
     * @param userGroups The userGroups to set.
     */
    public void setUserGroups(List<String> userGroups) {
        this.userGroups = userGroups;
    }
}
