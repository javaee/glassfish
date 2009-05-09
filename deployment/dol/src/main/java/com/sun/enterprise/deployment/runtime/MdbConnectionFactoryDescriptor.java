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

/*
 * @(#) MdbConnectionFactory.java
 *
 * Copyright 2000-2001 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of iPlanet/Sun Microsystems, Inc. ("Confidential Information").
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license
 * agreement you entered into with iPlanet/Sun Microsystems.
 */
package com.sun.enterprise.deployment.runtime;

import com.sun.enterprise.deployment.Descriptor;
import com.sun.enterprise.deployment.ResourcePrincipal;


/** iAS specific DD Element (see the ias-ejb-jar_2_0.dtd for this element)
 * @author Ludo
 * @since JDK 1.4
 */
public class MdbConnectionFactoryDescriptor extends Descriptor {
    
    
    
    private String jndiName;
    private ResourcePrincipal defaultResourcePrincipal;
    
    public MdbConnectionFactoryDescriptor() {
        
    }
    
    /** Getter for property defaultResourcePrincipal.
     * @return Value of property defaultResourcePrincipal.
     */
    public ResourcePrincipal getDefaultResourcePrincipal() {
        return defaultResourcePrincipal;
    }
    
    /** Setter for property defaultResourcePrincipal.
     * @param defaultResourcePrincipal New value of property defaultResourcePrincipal.
     */
    public void setDefaultResourcePrincipal(ResourcePrincipal defaultResourcePrincipal) {
        this.defaultResourcePrincipal = defaultResourcePrincipal;
    }
    
    /** Getter for property jndiName.
     * @return Value of property jndiName.
     */
    public java.lang.String getJndiName() {
        return jndiName;
    }
    
    /** Setter for property jndiName.
     * @param jndiName New value of property jndiName.
     */
    public void setJndiName(java.lang.String jndiName) {
        this.jndiName = jndiName;
    }
    
}

