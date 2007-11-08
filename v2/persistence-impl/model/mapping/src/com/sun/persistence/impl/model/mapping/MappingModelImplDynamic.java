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
 * MappingModelImplDynamic.java
 *
 */


package com.sun.persistence.impl.model.mapping;

import com.sun.org.apache.jdo.model.ModelException;
import com.sun.org.apache.jdo.model.jdo.JDOModel;
import com.sun.persistence.api.model.mapping.MappingClass;
import com.sun.persistence.api.model.mapping.MappingModel;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Bouschen
 * @author Rochelle Raccah
 */
public class MappingModelImplDynamic extends MappingElementImpl
        implements MappingModel {

    // <editor-fold desc="//===================== constants & variables =======================">

    /**
     * The corresponding JDOModel instance.
     */
    private JDOModel jdoModel;

    /**
     * Map of mapping classes managed by this MappingModel instance, key is the
     * fully qualified class name.
     */
    private Map mappingClasses = new HashMap();

    // </editor-fold>

    // <editor-fold desc="//========================= constructors ============================">

    /**
     * Creates a new instance of MappingModelImplDynamic
     */
    public MappingModelImplDynamic() {
    }

    /**
     * Creates a new instance of MappingModelImplDynamic
     */
    protected MappingModelImplDynamic(JDOModel jdoModel) {
        this.jdoModel = jdoModel;
    }

    // </editor-fold>

    // <editor-fold desc="//========= MappingModel & related convenience methods ==============">

    // <editor-fold desc="//======================= class handling ============================">

    /**
     * Returns a new instance of the MappingClass implementation class.
     */
    protected MappingClass newMappingClassInstance(String className,
            MappingModel declaringMappingModel) {
        return new MappingClassImplDynamic(className, declaringMappingModel);
    }

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
            throws ModelException {
        MappingClass mappingClass = (MappingClass) getMappingClass(className);
        if (mappingClass == null) {
            mappingClass = newMappingClassInstance(className, this);
            mappingClasses.put(className, mappingClass);
        }
        return mappingClass;
    }

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
    public MappingClass getMappingClass(String className) {
        return (MappingClass) mappingClasses.get(className);
    }

    /**
     * Returns the collection of mapping class instances declared by this
     * mapping model in the format of an array.
     * @return the classes declared by this mapping model
     */
    public MappingClass[] getMappingClasses() {
        return (MappingClass[]) mappingClasses.values().toArray(
                new MappingClass[mappingClasses.size()]);
    }

    // </editor-fold>

    // <editor-fold desc="//=================== delegation to jdo model  ======================">

    /**
     * Returns the JDOModel bound to this mapping model instance.
     * @return the JDOModel
     */
    public JDOModel getJDOModel() {
        return jdoModel;
    }

    /**
     * Sets the JDOModel for this mapping model instance.
     * @param jdoModel the JDOModel
     * @deprecated
     */
    public void setJDOModel(JDOModel jdoModel) {
        this.jdoModel = jdoModel;
    }
    
    // </editor-fold>

    // </editor-fold>
}
