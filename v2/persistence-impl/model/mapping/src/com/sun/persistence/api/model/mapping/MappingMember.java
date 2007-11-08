/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * MappingMember.java
 *
 */


package com.sun.persistence.api.model.mapping;

import com.sun.org.apache.jdo.model.ModelException;

/**
 *
 */
public interface MappingMember extends MappingElement, Comparable {
    // <editor-fold desc="//======================= declaring class ===========================">

    /**
     * Get the declaring mapping class.
     * @return the class that owns this member, or <code>null</code> if the
     *         member is not attached to any class
     */
    public MappingClass getDeclaringMappingClass();

    // </editor-fold>

    // <editor-fold desc="//======================== name handling ============================">

    /**
     * Get the name of this mapping element.
     * @return the name
     */
    public String getName();

    /**
     * Set the name of this mapping element.
     * @param name the name
     * @throws ModelException if impossible
     */
    public void setName(String name) throws ModelException;

    // </editor-fold>
}

