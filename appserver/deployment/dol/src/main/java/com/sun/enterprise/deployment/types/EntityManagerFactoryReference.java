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

package com.sun.enterprise.deployment.types;

import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.InjectionCapable;

/** 
 * Protocol associated with defining an EntityManagerFactory reference
 *
 */
public interface EntityManagerFactoryReference extends NamedInformation, 
    InjectionCapable {

    /**
     * Set the unit name of the entity manager factory to which this
     * reference is associated.
     */
    public void setUnitName(String unitName);


    /**
     * Get the unit name of the entity manager factory to which this
     * reference is associated.
     */
    public String getUnitName();

    /**
     * Set the referring bundle, i.e. the bundle within which this
     * reference is declared. 
     */
    public void setReferringBundleDescriptor(BundleDescriptor referringBundle);

    /**
     * Get the referring bundle, i.e. the bundle within which this
     * reference is declared.  
     */
    public BundleDescriptor getReferringBundleDescriptor();


}

