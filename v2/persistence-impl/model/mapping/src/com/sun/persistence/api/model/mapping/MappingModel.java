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


package com.sun.persistence.api.model.mapping;

import com.sun.org.apache.jdo.model.ModelException;
import com.sun.org.apache.jdo.model.jdo.JDOModel;

/**
 * A mapping model instance bundles a number of mapping class instances used by
 * an application. It provides factory methods to create and retrieve mapping
 * class instances. A fully qualified class name must be unique within a mapping
 * model instance. The model supports multiple classes having the same fully
 * qualified name by different mapping model instances.
 */
public interface MappingModel extends MappingElement {
    // <editor-fold desc="//======================= class handling ============================">

    /**
     * The method returns a mapping class instance for the specified fully
     * qualified class name. If this mapping model contains the corresponding
     * mapping class instance, the existing instance is returned. Otherwise, it
     * creates a new mapping class instance, sets its declaringModel and returns
     * the new instance. <p>
     * @param className the fully qualified class name of the mapping class
     * instance to be returned
     * @return a mapping class instance for the specified class name
     * @throws ModelException if impossible
     */
    public MappingClass createMappingClass(String className)
            throws ModelException;

    /**
     * The method returns the mapping class instance for the specified fully
     * qualified class name if present. The method returns <code>null</code> if
     * it cannot find a mapping class instance for the specified name. <p>
     * Invoking this method is equivalent to <code>createMappingClass(className)</code>.
     * @param className the fully qualified class name of the mapping class
     * instance to be returned
     * @return a mapping class instance for the specified class name or
     *         <code>null</code> if not present
     * @throws ModelFatalException if impossible
     */
    public MappingClass getMappingClass(String className);

    /**
     * Returns the collection of mapping class instances declared by this
     * mapping model in the format of an array.
     * @return the classes declared by this mapping model
     */
    public MappingClass[] getMappingClasses();

    // </editor-fold>

    // <editor-fold desc="//=================== delegation to jdo model  ======================">

    /**
     * Returns the JDOModel bound to this mapping model instance.
     * @return the JDOModel
     */
    public JDOModel getJDOModel();

    /**
     * Sets the JDOModel for this mapping model instance.  <b>Warning: this 
     * method is probably to be removed from the interface once the first
     * MappingModelFactory implementation is available.</b>
     * @param jdoModel the JDOModel
     * @deprecated
     */
    public void setJDOModel(JDOModel jdoModel);

    // </editor-fold>
}
