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
 * RuntimeMappingModelImpl.java
 *
 */

package com.sun.persistence.runtime.model.mapping.impl;

import com.sun.org.apache.jdo.model.ModelException;
import com.sun.org.apache.jdo.model.jdo.JDOModel;
import com.sun.persistence.api.model.mapping.MappingClass;
import com.sun.persistence.api.model.mapping.MappingModel;
import com.sun.persistence.impl.model.mapping.MappingModelImplDynamic;
import com.sun.persistence.runtime.model.mapping.RuntimeMappingClass;
import com.sun.persistence.runtime.model.mapping.RuntimeMappingModel;

/**
 *
 * @author Rochelle Raccah
 * @author Michael Bouschen
 */
public class RuntimeMappingModelImpl extends MappingModelImplDynamic
        implements RuntimeMappingModel {

    // <editor-fold desc="//========================= constructors ============================">

    /**
     * Creates a new instance of RuntimeMappingModelImpl
     */
    protected RuntimeMappingModelImpl(JDOModel jdoModel) {
        super(jdoModel);
    }

    // </editor-fold>

    // <editor-fold desc="//======= RuntimeMappingModel & related convenience methods =========">

    // <editor-fold desc="//======================= class handling ============================">

    /**
     * Returns a new instance of the MappingClass implementation class.
     */
    protected RuntimeMappingClass newMappingClassInstance(String className,
            MappingModel declaringMappingModel) {
        return new RuntimeMappingClassImpl(className, declaringMappingModel);
    }

    /** */
    public RuntimeMappingClass createMappingClass(String className)
            throws ModelException {
        return (RuntimeMappingClass) super.createMappingClass(className);
    }
    
    /** */
    public RuntimeMappingClass getMappingClass(String className) {
        return (RuntimeMappingClass) super.getMappingClass(className);
    }
    
    /** */
    public RuntimeMappingClass[] getMappingClasses() {
        MappingClass[] mappingClasses = super.getMappingClasses();
        int length = mappingClasses.length;
        RuntimeMappingClass[] result = new RuntimeMappingClass[length];
        System.arraycopy(mappingClasses, 0, result, 0, length); 
        return result;
    }

    // </editor-fold>

    // </editor-fold>
}
