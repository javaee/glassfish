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
package com.sun.enterprise.deployment;

import org.glassfish.security.common.PrincipalImpl;

/**
 * This class encapsulates the Resource Principal information needed
 * to access the Resource.
 *
 * @author Tony Ng
 */
public class ResourcePrincipal extends  PrincipalImpl {
    private String password;

    static private final int NULL_HASH_CODE = Integer.valueOf(1).hashCode();

    // start IASRI 4676199
    // Mods:
    // - Adding support for default principal cases where a principal
    //          is not needed to acquire a resource. Ex: when username and
    //          password are set on a jdbc datasource, no principal is need
    //          to call getConnection()

    //used for hashCode()
    private static final String DEFAULT_USERNAME = "__default__user__name__";
    //used for hashCode()
    private static final String DEFAULT_PASSWORD = "__default__password__";

    private boolean defaultPrincipal = false;

    /**
     * This constructor is used to construct a default principal. a default
     * principal is used when username and password are not required to 
     * acquire a resource.
     */
    public ResourcePrincipal() {
        super(DEFAULT_USERNAME);
        this.password = DEFAULT_PASSWORD;
        defaultPrincipal = true;
    }

    // end IASRI 4676199

    public ResourcePrincipal(String name, String password) {
        super(name);
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    // start IASRI 4676199
    /**
     * @return true if this principal is a default principal
     * @see ResourcePrincipal()
     */
    public boolean isDefault() {
        return defaultPrincipal;
    }
    // end IASRI 4676199

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (o instanceof ResourcePrincipal) {
            ResourcePrincipal other = (ResourcePrincipal) o;

            // start IASRI 4676199
            // handle the default principal case
            if (isDefault()) {
                return other.isDefault();
            } else if (other.isDefault()) {
                return false;
            }
            // end IASRI 4676199

            return ((isEqual(getName(), other.getName())) &&
                    (isEqual(this.password, other.password)));
        }
        return false;
    }

    public int hashCode() {
        int result = NULL_HASH_CODE;
        String name = getName();
        if (name != null) {
            result += name.hashCode();
        }
        if (password != null) {
            result += password.hashCode();
        }
        return result;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null) {
            return (b == null);
        } else {
            return (a.equals(b));
        }
    }

}
