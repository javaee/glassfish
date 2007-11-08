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

import com.sun.org.apache.jdo.model.java.JavaField;
import com.sun.org.apache.jdo.model.java.JavaModel;
import com.sun.org.apache.jdo.model.java.JavaType;
import com.sun.org.apache.jdo.model.jdo.JDOClass;
import com.sun.org.apache.jdo.model.jdo.JDOCollection;
import com.sun.org.apache.jdo.model.jdo.JDOField;
import com.sun.org.apache.jdo.model.jdo.JDORelationship;
import com.sun.persistence.runtime.model.mapping.RuntimeMappingModel;

/**
 * 
 * @author Dave Bristor
 */
public class PersistenceQueryContext extends QueryContextImpl {
    /** Provides way to get information about classes and fields. */
    private final JavaModel javaModel;
    
    public PersistenceQueryContext(RuntimeMappingModel mappingModel) {
        this.javaModel = mappingModel.getJDOModel().getJavaModel();
    }

    /**
     * @see com.sun.persistence.runtime.query.QueryContext#isCompatibleWith(java.lang.Object, java.lang.Object)
     */
    public boolean isCompatibleWith(Object left, Object right) {
        boolean rc = false;
        try {
            JavaType lhs = (JavaType) left;
            JavaType rhs = (JavaType) right;
            rc = lhs.isCompatibleWith(rhs);
        } catch (ClassCastException ex) {
            // empty
        }
        return rc;
    }

    
    /**
     * @see com.sun.persistence.runtime.query.QueryContext#getTypeInfoForAbstractSchema(java.lang.String)
     */
    public Object getTypeInfoForAbstractSchema(String abstractSchema) {
        // XXX PersistenceUnit: For now, assume given name is Entity name and also a class name
        // XXX TBD Remove use of classname as abstract schema name.
        JavaType rc = getTypeForShortName(abstractSchema);
        if (rc == null) {
            rc = javaModel.getJavaType(abstractSchema);
        }
        if (rc == null) {
            rc = errorType;
        }
        return rc;
    }

    /*
     * @param shortName short name of a type, as used in an EJBQL query.  For
     * example, in <code>select d from Department d where d.id = ?1</code>,
     * "Department" is a short name for the fully qualified class name.
     * @return JavaType corresponding to the short name given
     */
    private JavaType getTypeForShortName(String shortName) {
        JavaType rc = null;
        JDOClass c = javaModel.getJDOModel().getJDOClassForShortName(shortName);
        if (c != null) {
            rc = c.getJavaType();
        }
        return rc;
    }

    /**
     * @see com.sun.persistence.runtime.query.QueryContext#getAbstractSchemaForTypeInfo(java.lang.Object)
     */
    public String getAbstractSchemaForTypeInfo(Object typeInfo) {
        String rc = null;
        JavaType t = (JavaType) typeInfo;
        if (t != null) {
            JDOClass jdoClass = t.getJDOClass();
            if (jdoClass != null) {
                rc = jdoClass.getShortName();
            }
        }
        return rc;
    }

    /**
     * @see com.sun.persistence.runtime.query.QueryContext#getFieldType(java.lang.Object, java.lang.String)
     */
    public Object getFieldType(Object typeInfo, String fieldName) {
        JavaType rc = null;
        if (typeInfo instanceof JavaType) {
            JavaType t = (JavaType) typeInfo;
            JavaField f = t.getJavaField(fieldName);
            rc = f == null ? null : f.getType();         
        }
        return rc;
    }

    /* getFieldInfo, isRelationship, and getElementType work together:
     * getFieldInfo returns a JDOField, and that is the expected parameter
     * type for isRelationship and getElementType.
     */

    /**
     * @see com.sun.persistence.runtime.query.QueryContext#getFieldInfo(java.lang.Object, java.lang.String)
     */
    public Object getFieldInfo(Object typeInfo, String fieldName) {
        JDOField rc = null;
        if (typeInfo instanceof JavaType) {
            JavaType t = (JavaType) typeInfo;
            JDOClass jdoClass = t.getJDOClass();
            rc = jdoClass.getField(fieldName);
        }
        return rc;
    }
    
    /**
     * @see com.sun.persistence.runtime.query.QueryContext#isRelationship(java.lang.Object)
     */
    public boolean isRelationship(Object fieldInfo) {
        boolean rc = false;
        if (fieldInfo instanceof JDOField) {
            JDOField f = (JDOField) fieldInfo;
            rc = f.getRelationship() != null;
        }
        return rc;
    }

    /**
     * @see com.sun.persistence.runtime.query.QueryContext#getElementType(java.lang.Object)
     */
    public Object getElementType(Object fieldInfo) {
        Object rc = null;
        if (fieldInfo instanceof JDOField) {
            JDOField f = (JDOField) fieldInfo;
            JDORelationship r = f.getRelationship();
            if (r != null && r.isJDOCollection()) {
                JDOCollection c = (JDOCollection) r;
                rc = c.getElementType();
            }
        }
        return rc;
    }

    /**
     * @see com.sun.persistence.runtime.query.QueryContext#getPCForTypeInfo(java.lang.Object)
     */
    public String getPCForTypeInfo(Object typeInfo) {
        return getTypeName(typeInfo);
    }

    /**
     * @see com.sun.persistence.runtime.query.QueryContext#isEjbName(java.lang.Object)
     */
    public boolean isEjbName(Object typeInfo) {
        boolean rc = false;
        JavaType t = (JavaType) typeInfo;
        if (t != null) {
            JDOClass jdoClass = t.getJDOClass();
            rc = (jdoClass != null);
        }
        return rc; 
    }

    /**
     * @see com.sun.persistence.runtime.query.QueryContext#isEjbOrInterfaceName(java.lang.Object)
     */
    public boolean isEjbOrInterfaceName(Object typeInfo) {
           return isEjbName(typeInfo);
    }

    /**
     * @see com.sun.persistence.runtime.query.QueryContext#isRemoteInterfaceOfEjb(java.lang.Object, java.lang.String)
     */
    public boolean isRemoteInterfaceOfEjb(Object typeInfo, String ejbName) {
        return false;
    }

    /**
     * @see com.sun.persistence.runtime.query.QueryContext#isLocalInterfaceOfEjb(java.lang.Object, java.lang.String)
     */
    public boolean isLocalInterfaceOfEjb(Object typeInfo, String ejbName) {
        return false;
    }

    /**
     * @see com.sun.persistence.runtime.query.QueryContext#isRemoteInterface(java.lang.Object)
     */
    public boolean isRemoteInterface(Object typeInfo) {
        return false;
    }

    /**
     * @see com.sun.persistence.runtime.query.QueryContext#isLocalInterface(java.lang.Object)
     */
    public boolean isLocalInterface(Object typeInfo) {
        return false;
    }

    /**
     * @see com.sun.persistence.runtime.query.QueryContext#hasRemoteInterface(java.lang.Object)
     */
    public boolean hasRemoteInterface(Object typeInfo) {
        return false;
    }

    /**
     * @see com.sun.persistence.runtime.query.QueryContext#hasLocalInterface(java.lang.Object)
     */
    public boolean hasLocalInterface(Object typeInfo) {
        return false;
    }

}
