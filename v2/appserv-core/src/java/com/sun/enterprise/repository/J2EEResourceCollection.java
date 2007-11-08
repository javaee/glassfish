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
package com.sun.enterprise.repository;

import java.util.Set;

/**
 * Interface representing a collection of J2EEResources,
 * ordered by type and keyed by name.
 *
 * @author Kenneth Saks
 */
public interface J2EEResourceCollection {

    /**
     * Get all the resources of a given type.
     * @return Shallow copy of resource set. If there
     *         are no resources of the given type, an
     *         empty set is returned.
     */
    Set getResourcesByType(int type);

    /**
     * Get all the resources.
     */
    Set getAllResources();

    /**
     * Add a resource. This resource will replace any
     * existing one with the same type and name.
     */
    void addResource(J2EEResource resource);

    /**
     * Remove a resource. 
     * @return true if resource was removed, false if
     *         resource was not found
     */
    boolean removeResource(J2EEResource resource);

    /**
     * Remove all resources of the given type.  After
     * this call getResourcesByType(type) should return
     * an empty set.
     */
    void removeAllResourcesByType(int type);

    /**
     * Direct-access resource lookup.
     * @return J2EEResource if resource was found and
     *         null otherwise
     */
    J2EEResource getResourceByName(int type, String name);

}
