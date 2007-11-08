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
 * RuntimeMappingClassImpl.java
 *
 * Created on 15. April 2005, 16:39
 */


package com.sun.persistence.runtime.model.mapping.impl;

import com.sun.org.apache.jdo.impl.model.java.runtime.RuntimeJavaType;
import com.sun.org.apache.jdo.model.ModelException;
import com.sun.org.apache.jdo.model.jdo.JDOClass;
import com.sun.org.apache.jdo.model.jdo.JDOField;

import com.sun.persistence.api.model.mapping.MappingClass;
import com.sun.persistence.api.model.mapping.MappingField;
import com.sun.persistence.api.model.mapping.MappingModel;

import com.sun.persistence.impl.model.mapping.MappingClassImplDynamic;
import com.sun.persistence.runtime.model.mapping.RuntimeMappingClass;
import com.sun.persistence.runtime.model.mapping.RuntimeMappingField;
import com.sun.persistence.runtime.model.mapping.RuntimeMappingModel;

/**
 *
 * @author Rochelle Raccah
 * @author Michael Bouschen
 */
public class RuntimeMappingClassImpl extends MappingClassImplDynamic
        implements RuntimeMappingClass {

    // <editor-fold desc="//===================== constants & variables =======================">

    /** The Class instance of the mapped class. */
    private transient Class javaClass = null;
    
    /** The primary key mapping fields. */
    private RuntimeMappingField[] pkMappingFields = null;

    /** The default fetch group mapping fields. */
    private RuntimeMappingField[] dfgMappingFields = null;

    // </editor-fold>

    // <editor-fold desc="//========================= constructors ============================">

    /** Creates a new instance of RuntimeMappingClassImpl. */
    public RuntimeMappingClassImpl() { }

    /**
     * Creates new RuntimeMappingClassImpl with the corresponding name.
     * @param name the name of the mapping class
     */
    protected RuntimeMappingClassImpl(String name,
            MappingModel declaringMappingModel) {
        super(name, declaringMappingModel);
    }

    // </editor-fold>

    // <editor-fold desc="//======= RuntimeMappingClass & related convenience methods =========">

    // <editor-fold desc="//======================= declaring model ===========================">

    /** {@inheritDoc} */
    public RuntimeMappingModel getDeclaringMappingModel() {
        return (RuntimeMappingModel) super.getDeclaringMappingModel();
    }

    // </editor-fold>

    // <editor-fold desc="//======================== class handling  ==========================">

     /** {@inheritDoc} */
    public RuntimeMappingClass getSuperclassMappingClass() {
        throw new UnsupportedOperationException();
    }
    
    /** {@inheritDoc} */
    public Class getJavaClass() {
        if (javaClass == null) {
            RuntimeJavaType javaType =
                (RuntimeJavaType) getJDOClass().getJavaType();
            javaClass = javaType.getJavaClass(); 
        }
        return javaClass;
    }
    
    // </editor-fold>

    // <editor-fold desc="//======================= field handling ============================">

    /** {@inheritDoc} */
    public RuntimeMappingField[] getMappingFields() {
        MappingField[] mappingFields = super.getMappingFields();
        int length = mappingFields.length;
        RuntimeMappingField[] result = new RuntimeMappingField[length];
        System.arraycopy(mappingFields, 0, result, 0, length); 
        return result;
    }
    
    /** {@inheritDoc} */
    public RuntimeMappingField getMappingField(String name) {
        return (RuntimeMappingField) super.getMappingField(name);
    }

    /** {@inheritDoc} */
    public RuntimeMappingField getMappingField(int fieldNumber) {
        RuntimeMappingField mappingField = null;
        JDOField jdoField = getJDOClass().getField(fieldNumber);
        if (jdoField != null) {
            mappingField = getMappingField(jdoField.getName());
        }
        return mappingField;
    }

    /**
     * Returns a new instance of the MappingField implementation class.
     */
    protected RuntimeMappingField newMappingFieldInstance(String fieldName,
            MappingClass declaringMappingClass) {
        return new RuntimeMappingFieldImpl(fieldName, declaringMappingClass);
    }

    /** {@inheritDoc} */
    public RuntimeMappingField createMappingField(String name) 
        throws ModelException {
        return (RuntimeMappingField) super.createMappingField(name);
    }

    /** {@inheritDoc} */
    public RuntimeMappingField[] getPrimaryKeyMappingFields() {
        if (pkMappingFields == null) {
            JDOClass jdoClass = getJDOClass();
            JDOField[] pkFields = jdoClass.getPrimaryKeyFields();
            pkMappingFields = new RuntimeMappingField[pkFields.length];
            for (int i = 0; i < pkFields.length; i++) {
                pkMappingFields[i] = getMappingField(pkFields[i].getName());
            }
        }
        return pkMappingFields;
    }

    /** {@inheritDoc} */
    public RuntimeMappingField[] getDefaultFetchGroupMappingFields() {
        if (dfgMappingFields == null) {
            JDOClass jdoClass = getJDOClass();
            JDOField[] dfgFields = jdoClass.getDefaultFetchGroupFields();
            dfgMappingFields = new RuntimeMappingField[dfgFields.length];
            for (int i = 0; i < dfgFields.length; i++) {
                dfgMappingFields[i] = getMappingField(dfgFields[i].getName());
            }
        }
        return dfgMappingFields;
    }

    /** {@inheritDoc} */
    public RuntimeMappingField[] getVersionMappingFields() {
        MappingField[] mappingFields = super.getVersionMappingFields();
        int length = mappingFields.length;
        RuntimeMappingField[] result = new RuntimeMappingField[length];
        System.arraycopy(mappingFields, 0, result, 0, length); 
        return result;
    }

    // </editor-fold>

    // </editor-fold>
}
