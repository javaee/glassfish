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
 * RuntimeMappingModel.java
 *
 */

package com.sun.persistence.runtime.model.mapping;

import com.sun.org.apache.jdo.model.ModelException;
import com.sun.org.apache.jdo.model.ModelFatalException;
import com.sun.persistence.api.model.mapping.MappingModel;

/**
 *
 * @author Rochelle Raccah
 * @author Michael Bouschen
 */
public interface RuntimeMappingModel extends MappingModel {

    // <editor-fold desc="//======================= class handling ============================">

    /**
     * The method returns a mapping class instance for the specified fully
     * qualified class name. If this mapping model contains the corresponding
     * mapping class instance, the existing instance is returned. Otherwise, it
     * creates a new mapping class instance, sets its declaring mapping model 
     * and returns the new instance. <p>
     * @param className the fully qualified class name of the mapping class
     * instance to be returned
     * @return a mapping class instance for the specified class name
     * @throws ModelException if impossible
     */
    public RuntimeMappingClass createMappingClass(String className)
            throws ModelException;

    /**
     * The method returns the mapping class instance for the specified fully
     * qualified class name if present. The method returns <code>null</code> if
     * it cannot find a mapping class instance for the specified name. <p>
     * @param className the fully qualified class name of the mapping class
     * instance to be returned
     * @return a mapping class instance for the specified class name or
     *         <code>null</code> if not present
     * @throws ModelFatalException if impossible
     */
    public RuntimeMappingClass getMappingClass(String className)
        throws ModelFatalException;

    /**
     * Returns the collection of mapping class instances declared by this
     * mapping model in the form of an array.
     * @return the classes declared by this mapping model
     */
    public RuntimeMappingClass[] getMappingClasses();

    // </editor-fold>
}
