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


package com.sun.persistence.runtime.query.impl;

import com.sun.jdo.api.persistence.model.Model;
import com.sun.jdo.api.persistence.model.jdo.PersistenceClassElement;
import com.sun.jdo.api.persistence.model.jdo.RelationshipElement;
import com.sun.jdo.spi.persistence.support.ejb.model.util.NameMapper;

import com.sun.persistence.utility.I18NHelper;

import java.util.ResourceBundle;

/**
 * Helper class to support type info access. The CMPQueryContext uses a
 * NameMapper to map EJB names to state instance names and vice versa.
 * 
 * @author Michael Bouschen
 * @author Shing Wai Chan
 * @author Dave Bristor
 */
public class CMPQueryContext extends QueryContextImpl {
    /**
     * Name mapping EJB <->state instance.
     */
    protected final NameMapper nameMapper;
    
    private final Model model;

    /**
     * I18N support.
     */
    protected final static ResourceBundle msgs =
        I18NHelper.loadBundle(QueryContextImpl.class);

    /**
     * Creates a new QueryContextImpl using the specified model instance to
     * access meta data and the specified nameMapper for EJB <->state instance
     * name mapping.
     */
    public CMPQueryContext(Model model, NameMapper nameMapper) {
        this.model = model;
        this.nameMapper = nameMapper;
    }

    /**
     * Implements type compatibility. The method returns <code>true</code> if
     * left is compatible with right. This is equivalent to
     * rightClass.isAssignableFrom(leftClass). Note, the method does not support
     * inheritance.
     */
    public boolean isCompatibleWith(Object left, Object right) {
        String leftTypeName = getTypeName(left);
        String rightTypeName = getTypeName(right);

        if (nameMapper.isLocalInterface(leftTypeName)
            && nameMapper.isEjbName(rightTypeName)) {
            rightTypeName = nameMapper
                .getLocalInterfaceForEjbName(rightTypeName);
        } else if (nameMapper.isRemoteInterface(leftTypeName)
            && nameMapper.isEjbName(rightTypeName)) {
            rightTypeName = nameMapper
                .getRemoteInterfaceForEjbName(rightTypeName);
        } else if (nameMapper.isLocalInterface(rightTypeName)
            && nameMapper.isEjbName(leftTypeName)) {
            leftTypeName = nameMapper.getLocalInterfaceForEjbName(leftTypeName);
        } else if (nameMapper.isRemoteInterface(rightTypeName)
            && nameMapper.isEjbName(leftTypeName)) {
            leftTypeName = nameMapper
                .getRemoteInterfaceForEjbName(leftTypeName);
        }

        // does not handle inheritance!
        return leftTypeName.equals(rightTypeName);
    }

    /**
     * Returns the typeInfo (the ejb name) for the specified abstract schema.
     */
    public Object getTypeInfoForAbstractSchema(String abstractSchema) {
        return nameMapper.getEjbNameForAbstractSchema(abstractSchema);
    }

    /**
     * Returns the typeInfo (the ejb name) for the specified abstract schema.
     */
    public String getAbstractSchemaForTypeInfo(Object typeInfo) {
        String typeName = getTypeName(typeInfo);
        return nameMapper.isEjbName(typeName) ? nameMapper
            .getAbstractSchemaForEjbName(typeName) : typeName;
    }

    /**
     * Returns the type info for the type of the given field.
     */
    public Object getFieldType(Object typeInfo, String fieldName) {
        String typeName = getTypeName(typeInfo);
        if (!nameMapper.isEjbName(typeName)) {
            ErrorMsg.fatal(I18NHelper.getMessage(msgs, "ERR_EjbNameExpected", // NOI18N
                "QueryContextImpl.getFieldType", typeName)); // NOI18N
        }

        String fieldType = model.getFieldType(typeName, fieldName);
        // check for local or remote interface, map to ejb name
        if (nameMapper.isLocalInterface(fieldType)) {
            fieldType = nameMapper.getEjbNameForLocalInterface(typeName,
                fieldName, fieldType);
        } else if (nameMapper.isRemoteInterface(fieldType)) {

            fieldType = nameMapper.getEjbNameForRemoteInterface(typeName,
                fieldName, fieldType);
        }
        return getTypeInfo(fieldType);
    }

    /**
     * Returns the field info for the specified field of the specified type. The
     * field info is opaque for the caller. Methods {@link #isRelationship}and
     * {@link #getElementType}allow to get details for a given field info.
     */
    public Object getFieldInfo(Object typeInfo, String fieldName) {
        Object fieldInfo = null;
        String typeName = getTypeName(typeInfo);
        if (!nameMapper.isEjbName(typeName)) {
            ErrorMsg.fatal(I18NHelper.getMessage(msgs, "ERR__EjbNameExpected", // NOI18N
                "QueryContextImpl.getFieldInfo", typeName)); // NOI18N
        }
        String pcClassName = nameMapper.getPersistenceClassForEjbName(typeName);
        String pcFieldName = nameMapper.getPersistenceFieldForEjbField(
            typeName, fieldName);
        PersistenceClassElement pce = model.getPersistenceClass(pcClassName);
        if (pce != null) {
            fieldInfo = pce.getField(pcFieldName);
        }
        return fieldInfo;
    }
    
    /**
     * @see com.sun.persistence.runtime.query.QueryContext#isRelationship(java.lang.Object)
     */
    public boolean isRelationship(Object fieldInfo) {
        return (fieldInfo != null) && (fieldInfo instanceof RelationshipElement);
    }


    /**
     * Returns the type info of the element type if the specified field info
     * denotes a collection relationship. Otherwise it returns <code>null</code>.
     */
    public Object getElementType(Object fieldInfo) {
        if ((fieldInfo != null) && (fieldInfo instanceof RelationshipElement)) {
            String elementClass = ((RelationshipElement) fieldInfo)
                .getElementClass();
            return nameMapper.getEjbNameForPersistenceClass(elementClass);
        } else {
            return null;
        }
    }

    /**
     * Gets the name of the persistence-capable class which corresponds to the
     * specified typeInfo (assuming an ejb name). The method returs the type
     * name of the specified typeInfo, it the typeInfo does not denote an
     * ejb-name (e.g. a local or remote interface).
     */
    public String getPCForTypeInfo(Object typeInfo) {
        String typeName = getTypeName(typeInfo);
        String pcClassName = nameMapper.getPersistenceClassForEjbName(typeName);
        return (pcClassName != null) ? pcClassName : typeName;
    }

    /**
     * Returns <code>true</code> if the specified type info denotes an ejb
     * name.
     */
    public boolean isEjbName(Object typeInfo) {
        return nameMapper.isEjbName(getTypeName(typeInfo));
    }

    /**
     * Returns <code>true</code> if the specified type info denotes an ejb
     * name or the name of a local interface or the name of a remote interface.
     */
    public boolean isEjbOrInterfaceName(Object typeInfo) {
        String typeName = getTypeName(typeInfo);
        return nameMapper.isEjbName(typeName)
            || nameMapper.isLocalInterface(typeName)
            || nameMapper.isRemoteInterface(typeName);
    }

    /**
     * Returns <code>true</code> if the specified type info denotes the remote
     * interface of the bean with the specified ejb name.
     */
    public boolean isRemoteInterfaceOfEjb(Object typeInfo, String ejbName) {
        String typeName = getTypeName(typeInfo);
        String remoteInterface = nameMapper
            .getRemoteInterfaceForEjbName(ejbName);
        return (remoteInterface != null) && remoteInterface.equals(typeName);

    }

    /**
     * Returns <code>true</code> if the specified type info denotes the local
     * interface of the bean with the specified ejb name.
     */
    public boolean isLocalInterfaceOfEjb(Object typeInfo, String ejbName) {
        String typeName = getTypeName(typeInfo);
        String localInterface = nameMapper.getLocalInterfaceForEjbName(ejbName);
        return (localInterface != null) && localInterface.equals(typeName);
    }

    /**
     * Returns <code>true</code> if the specified type info denotes a remote
     * interface.
     */
    public boolean isRemoteInterface(Object typeInfo) {
        return nameMapper.isRemoteInterface(getTypeName(typeInfo));
    }

    /**
     * Returns <code>true</code> if the specified type info denotes a local
     * interface.
     */
    public boolean isLocalInterface(Object typeInfo) {
        return nameMapper.isLocalInterface(getTypeName(typeInfo));
    }

    /**
     * Returns <code>true</code> if the bean with the specified ejb name has a
     * remote interface.
     */
    public boolean hasRemoteInterface(Object typeInfo) {
        return nameMapper.getRemoteInterfaceForEjbName(getTypeName(typeInfo)) != null;
    }

    /**
     * Returns <code>true</code> if the bean with the specified ejb name has a
     * local interface.
     */
    public boolean hasLocalInterface(Object typeInfo) {
        return nameMapper.getLocalInterfaceForEjbName(getTypeName(typeInfo)) != null;
    }
}
