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
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.sun.org.apache.jdo.impl.enhancer.generator;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;


/**
 *
 */
final class ImplHelper
    extends NameHelper
{
    // string constants. 
    //XXX Cleanup: Use constants from JDO_Conatsnts
    static final String[] COMMENT_ENHANCER_ADDED
    = null; //{ "added by enhancer" };
    static final String[] COMMENT_NOT_ENHANCER_ADDED
    = null; //{ "not added by enhancer" };

    static final String CLASSNAME_JDO_PERSISTENCE_CAPABLE
    = "com.sun.persistence.support.spi.PersistenceCapable";
    static final String CLASSNAME_JDO_PERSISTENCE_MANAGER
    = "com.sun.persistence.support.PersistenceManager";
    static final String CLASSNAME_JDO_IMPL_HELPER
    = "com.sun.persistence.support.spi.JDOImplHelper";
    static final String CLASSNAME_JDO_STATE_MANAGER
    = "com.sun.persistence.support.spi.StateManager";
    static final String CLASSNAME_JDO_PERMISSION
    = "com.sun.persistence.support.spi.JDOPermission";
    static final String CLASSNAME_JDO_USER_EXCEPTION
    = "com.sun.persistence.support.JDOUserException";
    //static final String CLASSNAME_JDO_FATAL_INTERNAL_EXCEPTION
    //= "com.sun.persistence.support.JDOFatalInternalException";
    static final String CLASSNAME_JDO_OBJECT_ID_FIELD_SUPPLIER
    = CLASSNAME_JDO_PERSISTENCE_CAPABLE + "." + "ObjectIdFieldSupplier";
    static final String CLASSNAME_JDO_OBJECT_ID_FIELD_CONSUMER
    = CLASSNAME_JDO_PERSISTENCE_CAPABLE + "." + "ObjectIdFieldConsumer";

    static final String FIELDNAME_JDO_FLAGS
    = "jdoFlags";
    static final String FIELDNAME_JDO_STATE_MANAGER
    = "jdoStateManager";
    static final String FIELDNAME_JDO_INHERITED_FIELD_COUNT
    = "jdoInheritedFieldCount";
    static final String FIELDNAME_JDO_FIELD_NAMES
    = "jdoFieldNames";
    static final String FIELDNAME_JDO_FIELD_TYPES
    = "jdoFieldTypes";
    static final String FIELDNAME_JDO_FIELD_FLAGS
    = "jdoFieldFlags";
    static final String FIELDNAME_JDO_PC_SUPERCLASS
    = "jdoPersistenceCapableSuperclass";
    static final String FIELDNAME_SERIAL_VERSION_UID
    = "serialVersionUID";

    static final String METHODNAME_WRITE_OBJECT
    = "writeObject";

    static final String METHODNAME_JDO_GET_MANAGED_FIELD_COUNT
    = "jdoGetManagedFieldCount";
    static final String METHODNAME_JDO_NEW_INSTANCE
    = "jdoNewInstance";
    static final String METHODNAME_JDO_NEW_OID_INSTANCE
    = "jdoNewObjectIdInstance";
    static final String METHODNAME_JDO_REPLACE_STATE_MANAGER
    = "jdoReplaceStateManager";
    static final String METHODNAME_JDO_REPLACE_FLAGS
    = "jdoReplaceFlags";
    static final String METHODNAME_JDO_REPLACE_FIELD
    = "jdoReplaceField";
    static final String METHODNAME_JDO_REPLACE_FIELDS
    = "jdoReplaceFields";
    static final String METHODNAME_JDO_PROVIDE_FIELD
    = "jdoProvideField";
    static final String METHODNAME_JDO_PROVIDE_FIELDS
    = "jdoProvideFields";
    static final String METHODNAME_JDO_COPY_FIELDS
    = "jdoCopyFields";
    static final String METHODNAME_JDO_COPY_FIELD
    = "jdoCopyField";
    static final String METHODNAME_JDO_PRE_SERIALIZE
    = "jdoPreSerialize";
    static final String METHODNAME_JDO_GET_PERSISTENCE_MANAGER
    = "jdoGetPersistenceManager";
    static final String METHODNAME_JDO_MAKE_DIRTY
    = "jdoMakeDirty";
    static final String METHODNAME_JDO_GET_OBJECT_ID
    = "jdoGetObjectId";
    static final String METHODNAME_JDO_GET_TRANSACTIONAL_OBJECT_ID
    = "jdoGetTransactionalObjectId";
    static final String METHODNAME_JDO_IS_PERSISTENT
    = "jdoIsPersistent";
    static final String METHODNAME_JDO_IS_TRANSACTIONAL
    = "jdoIsTransactional";
    static final String METHODNAME_JDO_IS_NEW
    = "jdoIsNew";
    static final String METHODNAME_JDO_IS_DIRTY
    = "jdoIsDirty";
    static final String METHODNAME_JDO_IS_DELETED
    = "jdoIsDeleted";
    static final String METHODNAME_JDO_COPY_KEY_FIELDS_TO_OID
    = "jdoCopyKeyFieldsToObjectId";
    static final String METHODNAME_JDO_COPY_KEY_FIELDS_FROM_OID
    = "jdoCopyKeyFieldsFromObjectId";

    static final String SINGLE_FIELD_IDENTITY_PREFIX
    = "com/sun/persistence/support/identity/";
    
    static final String BYTE_IDENTITY
    = SINGLE_FIELD_IDENTITY_PREFIX + "ByteIdentity";
    static final String CHAR_IDENTITY
    = SINGLE_FIELD_IDENTITY_PREFIX + "CharIdentity";
    static final String SHORT_IDENTITY
    = SINGLE_FIELD_IDENTITY_PREFIX + "ShortIdentity";
    static final String INT_IDENTITY
    = SINGLE_FIELD_IDENTITY_PREFIX + "IntIdentity";
    static final String LONG_IDENTITY
    = SINGLE_FIELD_IDENTITY_PREFIX + "LongIdentity";
    static final String String_IDENTITY
    = SINGLE_FIELD_IDENTITY_PREFIX + "StringIdentity";
    
    static private final HashMap typeNameConversion = new HashMap();

    /**
     * SingleFieldIdentity class names
     */
    private static HashMap singleFieldIdentityClassNames = new HashMap(11);

    static {
        typeNameConversion.put(int.class.getName(), "Int");
        typeNameConversion.put(long.class.getName(), "Long");
        typeNameConversion.put(byte.class.getName(), "Byte");
        typeNameConversion.put(char.class.getName(), "Char");
        typeNameConversion.put(boolean.class.getName(), "Boolean");
        typeNameConversion.put(short.class.getName(), "Short");
        typeNameConversion.put(float.class.getName(), "Float");
        typeNameConversion.put(double.class.getName(), "Double");
        typeNameConversion.put(String.class.getName(), "String");
        

        singleFieldIdentityClassNames.put(BYTE_IDENTITY, "Byte");
        singleFieldIdentityClassNames.put(CHAR_IDENTITY, "Char");
        singleFieldIdentityClassNames.put(SHORT_IDENTITY, "Short");
        singleFieldIdentityClassNames.put(INT_IDENTITY, "Integer");
        singleFieldIdentityClassNames.put(LONG_IDENTITY, "Long");
        singleFieldIdentityClassNames.put(String_IDENTITY, "String");
    }
   
    static private String getConvertedTypeName(String fieldtype)
    {
        final String name = (String)typeNameConversion.get(fieldtype);
        return (name != null ? name : "Object");
    }

    static private String getMethodNameGetField(String fieldtype)
    {
        return "get" + getConvertedTypeName(fieldtype) + "Field";
    }

    static private String getMethodNameSetField(String fieldtype)
    {
        return "set" + getConvertedTypeName(fieldtype) + "Field";
    }

    static private String getMethodNameReplacingField(String fieldtype)
    {
        return "replacing" + getConvertedTypeName(fieldtype) + "Field";
    }

    static private String getMethodNameProvidedField(String fieldtype)
    {
        return "provided" + getConvertedTypeName(fieldtype) + "Field";
    }

    static private String getMethodNameFetchField(String fieldtype)
    {
        return "fetch" + getConvertedTypeName(fieldtype) + "Field";
    }

    static private String getMethodNameStoreField(String fieldtype)
    {
        return "store" + getConvertedTypeName(fieldtype) + "Field";
    }

    // ----------------------------------------------------------------------

    static String createJDOFieldAccessorName(String classname,
                                             String fieldname)
    {
        return "jdoGet" + fieldname;
    }
    
    static String createJDOFieldMutatorName(String classname,
                                            String fieldname)
    {
        return "jdoSet" + fieldname;
    }

    // Create initial values of fields.

    static String getJDOInheritedFieldCountInitValue(String superclassname)
    {
        return(superclassname == null  ?
               "0"  :
               (normalizeClassName(superclassname)
                + '.' + METHODNAME_JDO_GET_MANAGED_FIELD_COUNT + "()"));
    }

    static String getJDOFieldNamesInitValue(String[] fieldnames)
    {
        String value = "new String[]{ ";
        final int n = fieldnames.length;
        for (int i = 0; i < n; i++) {
            value += "\"" + fieldnames[i] + "\"";
            if (i < n - 1) {
                value += ", ";
            }
        }
        return value + " }";
    }

    static String getJDOFieldTypesInitValue(String[] fieldtypes)
    {
        String value = "new Class[]{ ";
        final int n = fieldtypes.length;
        for (int i = 0; i < n; i++) {
            value += normalizeClassName(fieldtypes[i]) + ".class";
            if (i < n - 1) {
                value += ", ";
            }
        }
        return value + " }";
    }

    static String getJDOFieldFlagsInitValue(int[] fieldflags)
    {
        String value = "new byte[]{ ";
        final int n = fieldflags.length;
        for (int i = 0; i < n; i++) {
            value += "0x" + Integer.toHexString(fieldflags[i]);
            if (i < n - 1) {
                value += ", ";
            }
        }
        return value + " }";
    }

    static String getJDOPCSuperclassInitValue(String superclass)
    {
        return (superclass == null
                ? "null"
                : normalizeClassName(superclass) + ".class");
    }

    static String getSerialVersionUIDInitValue(long uid)
    {
        return uid + "L";
    }

    // Create bodies of methods.

    static List getJDOManagedFieldCountImpl(int fieldcount)
    {
        final List impl = new ArrayList(3);
        impl.add(FIELDNAME_JDO_INHERITED_FIELD_COUNT
                 + " + " + fieldcount + ';');
        return impl;
    }

    static List getStaticInitializerImpl(String classname,
                                         String superPC,
                                         String[] managedFieldNames,
                                         String[] managedFieldTypes,
                                         int[] managedFieldFlags)
    {
        classname = normalizeClassName(classname);
        final List impl = new ArrayList(20);

        impl.add(ImplHelper.FIELDNAME_JDO_INHERITED_FIELD_COUNT
                 + " = "+ getJDOInheritedFieldCountInitValue(superPC) + ";");
        impl.add(ImplHelper.FIELDNAME_JDO_FIELD_NAMES
                 + " = " + getJDOFieldNamesInitValue(managedFieldNames) + ";");
        impl.add(ImplHelper.FIELDNAME_JDO_FIELD_TYPES
                 + " = " + getJDOFieldTypesInitValue(managedFieldTypes) + ";");
        impl.add(ImplHelper.FIELDNAME_JDO_FIELD_FLAGS
                 + " = " + getJDOFieldFlagsInitValue(managedFieldFlags) + ";");
        impl.add(ImplHelper.FIELDNAME_JDO_PC_SUPERCLASS
                 + " = " + getJDOPCSuperclassInitValue(superPC) + ";");

        impl.add(CLASSNAME_JDO_IMPL_HELPER
                 + ".registerClass(");
        impl.add("    " + classname + ".class" + ", ");
        impl.add("    " + FIELDNAME_JDO_FIELD_NAMES + ", ");
        impl.add("    " + FIELDNAME_JDO_FIELD_TYPES + ", ");
        impl.add("    " + FIELDNAME_JDO_FIELD_FLAGS + ", ");
        impl.add("    " + FIELDNAME_JDO_PC_SUPERCLASS + ", ");
        impl.add("    " + "new " + classname + "()");
        impl.add(");");
        return impl;
    }

    static List getJDOGetManagedFieldCountImpl(boolean isRoot,
                                               String superPC,
                                               int fieldcount)
    {
        superPC = normalizeClassName(superPC);
        final List impl = new ArrayList(5);
        if (isRoot) {
            impl.add("return " + fieldcount + ';');
        }
        else {
            impl.add("return " + superPC + "." + 
                     METHODNAME_JDO_GET_MANAGED_FIELD_COUNT +
                     "() + " + fieldcount + ';');
        }
        return impl;
    }
    
    static List getDefaultConstructorImpl()
    {
        final List impl = new ArrayList(5);
        impl.add("super();");
        return impl;
    }

    static List getDummyConstructorImpl()
    {
        final List impl = new ArrayList(5);
        impl.add("super();");
        return impl;
    }

    static List getOidStringArgConstructorImpl(String superoidclassname,
                                               String str)
    {
        final List impl = new ArrayList(5);
        if (superoidclassname != null) {
            impl.add("super(" + str + ");");
        }
        //^olsen: todo
        impl.add("// not implemented yet");
        impl.add("throw new UnsupportedOperationException();");
        return impl;
    }

    static List getCloneImpl(String classname)
    {
        classname = normalizeClassName(classname);
        final List impl = new ArrayList(5);
        impl.add("final " + classname
                 + " pc = ("  + classname + ")super.clone();");
        impl.add("pc." + FIELDNAME_JDO_FLAGS + " = 0; // == READ_OK");
        impl.add("pc." + FIELDNAME_JDO_STATE_MANAGER + " = null;");
        impl.add("return pc;");
        return impl;
    }

    static List getJDONewInstanceImpl(String classname,
                                      String statemanager) 
    {
        final List impl = new ArrayList(5);
        classname = getClassName(classname);
        impl.add("final " + classname
                 + " pc = new " + classname + "();");        
        impl.add("pc." + FIELDNAME_JDO_FLAGS + " = 1; // == LOAD_REQUIRED");
        impl.add("pc." + FIELDNAME_JDO_STATE_MANAGER
                 + " = " + statemanager + ';');
        impl.add("return pc;");
        return impl;
    }

    static List getJDONewInstanceKeyImpl(String classname,
                                         String statemanager,
                                         String oid)
    {
        final List impl = new ArrayList(5);
        classname = getClassName(classname);
        impl.add("final " + classname
                 + " pc = new " + classname + "();");        
        impl.add("pc." + METHODNAME_JDO_COPY_KEY_FIELDS_FROM_OID
                 + "(" + oid + ");");
        impl.add("pc." + FIELDNAME_JDO_FLAGS + " = 1; // == LOAD_REQUIRED");
        impl.add("pc." + FIELDNAME_JDO_STATE_MANAGER
                 + " = " + statemanager + ';');
        impl.add("return pc;");
        return impl;
    }

    static List getJDONewOidInstanceImpl(String oidclassname)
    {
        final List impl = new ArrayList(5);
        if (oidclassname == null) {
            impl.add("return null;");
        } else {
            impl.add("return new " + oidclassname + "();");
        }
        return impl;
    }

    static List getJDONewOidInstanceImpl(String oidclassname,
                                         String str)
    {
        final List impl = new ArrayList(5);
        if (oidclassname == null) {
            impl.add("return null;");
        } else {
            impl.add("return new " + oidclassname + "(" + str + ");");
        }
        return impl;
    }

    static boolean isSingleFieldIdentity(String oidClassName) {
        return singleFieldIdentityClassNames.get(oidClassName) != null;
    }
    
    static List getJDONewOidInstanceImpl(String ownerClassName,
            String oidClassName, String obj, String[] fieldNames,
            String[] fieldtypes, int[] fieldNumbers)
    {
        final List impl = new ArrayList(5);
        if (oidClassName == null) {
            impl.add("return null;");
        } else {
            String wrapperType = (String) singleFieldIdentityClassNames.get(
                    oidClassName.replace('.', '/'));
            String primitiveType = lowerCaseFirstChar(wrapperType);
            if (wrapperType == null) {
                impl.add("throw new IllegalArgumentException(\"arg1\");");
            } else {
                //SingleFieldIdentity
                
                final boolean isNumberType
                    = !(wrapperType.equals("Char") || wrapperType.equals("String"));
                System.out.println("Generator:    classname: " + ownerClassName
                        + " oidClassName: " + oidClassName + " isNumberType: " + isNumberType);
                
                impl.add(primitiveType + " ret_val_" + obj + " = " 
                        + (isNumberType ? "0" : "null") + ";");
                impl.add("if (" + obj + " instanceof "
                        + ImplHelper.CLASSNAME_JDO_OBJECT_ID_FIELD_SUPPLIER + ") {");
                impl.add("    " + CLASSNAME_JDO_OBJECT_ID_FIELD_SUPPLIER
                        + " ofs_" + obj + " = ("
                        + CLASSNAME_JDO_OBJECT_ID_FIELD_SUPPLIER + ") " + obj + ";");
                impl.add("    ret_val_" + obj + " = ofs_"+obj+".fetch" + wrapperType
                        + " Field(jdoInheritedFieldCount + " + fieldNumbers[0] + ");");
                if (isNumberType) {
                    impl.add("} else if (" + obj + " instanceof java.lang.Number) {");
                    impl.add("    ret_val_" + obj + " = ((java.lang.Number) " + obj + ")."
                            + primitiveType + "Value();");
                    impl.add("} else if (" + obj + " instanceof java.lang.String) {");
                    impl.add("    ret_val_" + obj + " = ((java.lang.String) " + obj + ").parse"
                            + wrapperType + "();");
                    impl.add("} else {");
                    impl.add("    throw new IllegalArgumentException(\"arg1\");");
                    impl.add("}");
                } else if (wrapperType.equals("Char")) {
                    impl.add("} else if (" + obj + " instanceof java.lang.Character) {");
                    impl.add("    ret_val_" + obj + " = ((java.lang.Character) "
                            + obj + ").charValue();");
                    impl.add("} else {");
                    impl.add("    throw new IllegalArgumentException(\"arg1\");");
                    impl.add("}");
                } else { // (wrapperType.equals("String") ) {
                    impl.add("} else if (" + obj + " instanceof java.lang.String) {");
                    impl.add("    ret_val_" + obj + " = ((java.lang.String) "
                            + obj + ").toString();");
                    impl.add("} else {");
                    impl.add("    throw new IllegalArgumentException(\"arg1\");");
                    impl.add("}");
                }
                
                impl.add("");
                impl.add("return new " + oidClassName +
                        "(" + ownerClassName.replace('/', '.') + ".class, ret_val_" + obj + ");");
            }
        }
        return impl;
    }

    static List getJDOCopyKeyFieldsToOid(String oidclassname,
                                         String superoidclassname,
                                         String oid,
                                         String[] fieldnames)
    {
        final List impl = new ArrayList(5);
        if (oidclassname == null) {
            impl.add("return;");
        } else {
            impl.add("if (!(" + oid + " instanceof " + oidclassname + ")) {");
            impl.add("    throw new IllegalArgumentException(\"arg1\");");
            impl.add("}");
            final String _oid = "_" + oid;
            impl.add("final " + oidclassname
                     + " " + _oid + " = (" + oidclassname + ")" + oid + ";");
            if (superoidclassname != null) {
                impl.add("super." + METHODNAME_JDO_COPY_KEY_FIELDS_TO_OID
                         + "(" + _oid + ");");
            }
            for (int i = 0; i < fieldnames.length; i++) {
                final String fname = fieldnames[i];
                impl.add(_oid + "." + fname + " = " + "this." + fname + ";");
            }
        }
        return impl;
    }

    static List getJDOCopyKeyFieldsFromOid(String oidclassname,
                                           String superoidclassname,
                                           String oid,
                                           String[] fieldnames)
    {
        final List impl = new ArrayList(5);
        if (oidclassname == null) {
            impl.add("return;");
        } else {
            impl.add("if (!(" + oid + " instanceof " + oidclassname + ")) {");
            impl.add("    throw new IllegalArgumentException(\"arg1\");");
            impl.add("}");
            final String _oid = "_" + oid;
            impl.add("final " + oidclassname
                     + " " + _oid + " = (" + oidclassname + ")" + oid + ";");
            if (superoidclassname != null) {
                impl.add("super." + METHODNAME_JDO_COPY_KEY_FIELDS_FROM_OID
                         + "(" + _oid + ");");
            }
            for (int i = 0; i < fieldnames.length; i++) {
                final String fname = fieldnames[i];
                impl.add("this." + fname + " = " + _oid + "." + fname + ";");
            }
        }
        return impl;
    }

    static List getJDOCopyKeyFieldsToOid(String oidclassname,
                                         String superoidclassname,
                                         String fm,
                                         String oid,
                                         String[] fieldnames,
                                         String[] fieldtypes,
                                         int[] fieldnumbers)
    {
        final List impl = new ArrayList(5);
        if (oidclassname == null) {
            impl.add("return;");
        } else {
            impl.add("if (" + fm + " == null) {");
            impl.add("    throw new IllegalArgumentException(\"arg1\");");
            impl.add("}");
            impl.add("if (!(" + oid + " instanceof " + oidclassname + ")) {");
            impl.add("    throw new IllegalArgumentException(\"arg2\");");
            impl.add("}");
            final String _oid = "_" + oid;
            impl.add("final " + oidclassname
                     + " " + _oid + " = (" + oidclassname + ")" + oid + ";");
            if (superoidclassname != null) {
                impl.add("super." + METHODNAME_JDO_COPY_KEY_FIELDS_TO_OID
                         + "(" + fm + ", " + _oid + ");");
            }
            for (int i = 0; i < fieldnames.length; i++) {
                impl.add(_oid + "." + fieldnames[i] + " = " + fm + "."
                         + getMethodNameFetchField(fieldtypes[i])
                         + "(" + FIELDNAME_JDO_INHERITED_FIELD_COUNT
                         + " + " + fieldnumbers[i] + ");");
            }
        }
        return impl;
    }

    static List getJDOCopyKeyFieldsFromOid(String oidclassname,
                                           String superoidclassname,
                                           String fm,
                                           String oid,
                                           String[] fieldnames,
                                           String[] fieldtypes,
                                           int[] fieldnumbers)
    {
        final List impl = new ArrayList(5);
        if (oidclassname == null) {
            impl.add("return;");
        } else {
            impl.add("if (" + fm + " == null) {");
            impl.add("    throw new IllegalArgumentException(\"arg1\");");
            impl.add("}");
            impl.add("if (!(" + oid + " instanceof " + oidclassname + ")) {");
            impl.add("    throw new IllegalArgumentException(\"arg2\");");
            impl.add("}");
            final String _oid = "_" + oid;
            impl.add("final " + oidclassname
                     + " " + _oid + " = (" + oidclassname + ")" + oid + ";");
            if (superoidclassname != null) {
                impl.add("super." + METHODNAME_JDO_COPY_KEY_FIELDS_FROM_OID
                         + "(" + fm + ", " + _oid + ");");
            }
            for (int i = 0; i < fieldnames.length; i++) {
                impl.add(fm + "." + getMethodNameStoreField(fieldtypes[i])
                         + "(" + FIELDNAME_JDO_INHERITED_FIELD_COUNT
                         + " + " + fieldnumbers[i] + ", "
                         + _oid + "." + fieldnames[i] + ");");
            }
        }
        return impl;
    }

    static List getJDOReplaceStateManagerImpl(String statemanager)
    {
        final List impl = new ArrayList(8);
        impl.add("final " + CLASSNAME_JDO_STATE_MANAGER
                 + " s = this." + FIELDNAME_JDO_STATE_MANAGER + ";");
        impl.add("if (s != null) {");
        impl.add("    this." + FIELDNAME_JDO_STATE_MANAGER
                 + " = s.replacingStateManager(this, " + statemanager + ");");
        impl.add("    return;");
        impl.add("}");
        impl.add(CLASSNAME_JDO_IMPL_HELPER 
                 + ".checkAuthorizedStateManager(" + statemanager + ");");
        impl.add("this." + FIELDNAME_JDO_STATE_MANAGER
                 + " = " + statemanager + ';');
        impl.add("this." + FIELDNAME_JDO_FLAGS
                 + " = LOAD_REQUIRED;");
        return impl;
    }
    
    static List getJDOReplaceFlagsImpl()
    {
        final List impl = new ArrayList(5);
        impl.add("final " + CLASSNAME_JDO_STATE_MANAGER
                 + " sm = this." + FIELDNAME_JDO_STATE_MANAGER + ";");
        impl.add("if (sm != null) {");
        impl.add("    this." + FIELDNAME_JDO_FLAGS
                 + " = sm.replacingFlags(this);");
        impl.add("}");
        return impl;
    }

    static List getJDOFieldDirectReadImpl(String fieldname,
                                          String fieldtype,
                                          int    fieldnumber,
                                          String instancename)
    {
        fieldtype = normalizeClassName(fieldtype);
        final List impl = new ArrayList(20);
        impl.add("// augmentation: grant direct read access");
        impl.add("return " + instancename + '.' + fieldname + ';');
        return impl;
    }
    
    static private void addFieldMediateReadImpl(List impl,
                                                String fieldname,
                                                String fieldtype,
                                                int    fieldnumber,
                                                String instancename)
    {
        impl.add("final " + CLASSNAME_JDO_STATE_MANAGER + " sm = "
                 + instancename + '.' + FIELDNAME_JDO_STATE_MANAGER + ";");
        impl.add("if (sm == null) {");
        impl.add("    return " + instancename + '.' + fieldname + ';');
        impl.add("}");
        impl.add("if (sm.isLoaded(" + instancename + ", "
                 + FIELDNAME_JDO_INHERITED_FIELD_COUNT
                 + " + " + fieldnumber + ")) {");
        impl.add("    return " + instancename + '.' + fieldname + ';');
        impl.add("}");
        impl.add("return (" + fieldtype + ")"
                 + "sm." + getMethodNameGetField(fieldtype)
                 + "(" + instancename + ", "
                 + FIELDNAME_JDO_INHERITED_FIELD_COUNT
                 + " + " + fieldnumber + ", "
                 + instancename + '.' + fieldname + ");");
    }

    static List getJDOFieldMediateReadImpl(String fieldname,
                                           String fieldtype,
                                           int    fieldnumber,
                                           String instancename)
    {
        fieldtype = normalizeClassName(fieldtype);
        final List impl = new ArrayList(20);
        impl.add("// augmentation: mediate read access");
        addFieldMediateReadImpl(impl, fieldname, fieldtype,
                                fieldnumber, instancename);
        return impl;
    }

    static List getJDOFieldCheckReadImpl(String fieldname,
                                         String fieldtype,
                                         int    fieldnumber,
                                         String instancename)
    {
        fieldtype = normalizeClassName(fieldtype);
        final List impl = new ArrayList(20);
        impl.add("// augmentation: check read access");
        impl.add("if (" + instancename + '.' + FIELDNAME_JDO_FLAGS
                 + " <= 0) {");
        impl.add("    return " + instancename + '.' + fieldname + ';');
        impl.add("}");
        addFieldMediateReadImpl(impl, fieldname, fieldtype,
                                fieldnumber, instancename);
        return impl;
    }

    static List getJDOFieldDirectWriteImpl(String fieldname,
                                           String fieldtype,
                                           int    fieldnumber,
                                           String instancename,
                                           String newvalue)
    {
        fieldtype = normalizeClassName(fieldtype);
        final List impl = new ArrayList(20);
        impl.add("// augmentation: grant direct write access");
        impl.add(instancename + '.' + fieldname
                 + " = " + newvalue + ';');
        return impl;
    }

    static private void addFieldMediateWriteImpl(List impl,
                                                 String fieldname,
                                                 String fieldtype,
                                                 int    fieldnumber,
                                                 String instancename,
                                                 String newvalue)
    {
        impl.add("final " + CLASSNAME_JDO_STATE_MANAGER + " sm = "
                 + instancename + '.' + FIELDNAME_JDO_STATE_MANAGER + ";");
        impl.add("if (sm == null) {");
        impl.add("    " + instancename + '.' + fieldname
                 + " = " + newvalue + ';');
        impl.add("    return;");
        impl.add("}");
        impl.add("sm."
                 + getMethodNameSetField(fieldtype)
                 + "(" + instancename + ", "
                 + FIELDNAME_JDO_INHERITED_FIELD_COUNT
                 + " + " + fieldnumber + ", "
                 + instancename
                 + '.' + fieldname + ", "
                 + newvalue + ");");
    }

    static List getJDOFieldMediateWriteImpl(String fieldname,
                                            String fieldtype,
                                            int    fieldnumber,
                                            String instancename,
                                            String newvalue)
    {
        fieldtype = normalizeClassName(fieldtype);
        final List impl = new ArrayList(20);
        impl.add("// augmentation: mediate write access");
        addFieldMediateWriteImpl(impl, fieldname, fieldtype,
                                 fieldnumber, instancename, newvalue);
        return impl;
    }

    static List getJDOFieldCheckWriteImpl(String fieldname,
                                          String fieldtype,
                                          int    fieldnumber,
                                          String instancename,
                                          String newvalue)
    {
        fieldtype = normalizeClassName(fieldtype);
        final List impl = new ArrayList(20);
        impl.add("// augmentation: check write access");
        impl.add("if (" + instancename
                 + '.' + FIELDNAME_JDO_FLAGS + " == 0) {");
        impl.add("    " + instancename + '.' + fieldname
                 + " = " + newvalue + ';');
        impl.add("    return;");
        impl.add("}");
        addFieldMediateWriteImpl(impl, fieldname, fieldtype,
                                 fieldnumber, instancename, newvalue);
        return impl;
    }

    static List getJDOReplaceFieldImpl(String   fieldnumber,
                                       boolean  isRoot,
                                       String[] fieldnames,
                                       String[] fieldtypes)
    {
        final List impl = new ArrayList(20);
        impl.add("final " + CLASSNAME_JDO_STATE_MANAGER + " sm = this."
                 + FIELDNAME_JDO_STATE_MANAGER + ";");
        impl.add("switch (" + fieldnumber
                 + " - " + FIELDNAME_JDO_INHERITED_FIELD_COUNT + ") {");
        for (int i = 0; i < fieldnames.length; i++) {
            String fieldtype = normalizeClassName(fieldtypes[i]);
            impl.add("case " + i + ':');
            impl.add("    if (sm == null) {");
            impl.add("        throw new IllegalStateException(\"arg0."
                     + FIELDNAME_JDO_STATE_MANAGER + "\");");
            impl.add("    }");
            impl.add("    this." + fieldnames[i]
                     + " = (" + fieldtype + ")sm."
                     + getMethodNameReplacingField(fieldtype)
                     + "(this, " + fieldnumber + ");");
            impl.add("    return;");
        }
        impl.add("default:");
        if (isRoot) {
            impl.add("    throw new IllegalArgumentException(\"arg1\");");
        } else {
            impl.add("    super." + METHODNAME_JDO_REPLACE_FIELD
                     + "(" + fieldnumber + ");");
        }
        impl.add("}");
        return impl;
    }

    static List getJDOProvideFieldImpl(String   fieldnumber,
                                       boolean  isRoot,
                                       String[] fieldnames,
                                       String[] fieldtypes)
    {
        final List impl = new ArrayList(20);
        impl.add("final " + CLASSNAME_JDO_STATE_MANAGER + " sm = this."
                 + FIELDNAME_JDO_STATE_MANAGER + ";");
        impl.add("switch (" + fieldnumber
                 + " - " + FIELDNAME_JDO_INHERITED_FIELD_COUNT + ") {");
        for (int i = 0; i < fieldnames.length; i++) {
            String fieldtype = normalizeClassName(fieldtypes[i]);
            impl.add("case " + i + ':');
            impl.add("    if (sm == null) {");
            impl.add("        throw new IllegalStateException(\"arg0."
                     + FIELDNAME_JDO_STATE_MANAGER + "\");");
            impl.add("    }");
            impl.add("    sm." + getMethodNameProvidedField(fieldtype)
                     + "(this, " + fieldnumber + ", "
                     + "this." + fieldnames[i] + ");");
            impl.add("    return;");
        }
        impl.add("default:");
        if (isRoot) {
            impl.add("    throw new IllegalArgumentException(\"arg1\");");
        } else {
            impl.add("    super." + METHODNAME_JDO_PROVIDE_FIELD
                     + "(" + fieldnumber + ");");
        }
        impl.add("}");
        return impl;
    }

    static List getJDOCopyFieldsImpl(String   classname,
                                     String   copy,
                                     String   fieldnumbers)
    {
        classname = normalizeClassName(classname);
        final List impl = new ArrayList(50);
        impl.add("if (this." + FIELDNAME_JDO_STATE_MANAGER + " == null) {");
        impl.add("    throw new IllegalStateException(\"arg0."
                 + FIELDNAME_JDO_STATE_MANAGER + "\");");
        impl.add("}");
        impl.add("if (!(" + copy + " instanceof " + classname + ")) {");
        impl.add("    throw new IllegalArgumentException(\"arg1\");");
        impl.add("}");
        impl.add("if (" + fieldnumbers + " == null) {");
        impl.add("    throw new IllegalArgumentException(\"arg2\");");
        impl.add("}");
        impl.add("final " + classname
                 + " other = (" + classname + ")" + copy + ';');
        impl.add("if (other." + FIELDNAME_JDO_STATE_MANAGER
                 + " != this." + FIELDNAME_JDO_STATE_MANAGER + ") {");
        impl.add("    throw new IllegalArgumentException(\""
                 + "arg1." + FIELDNAME_JDO_STATE_MANAGER + "\");");
        impl.add("}");
        impl.add("final int n = " + fieldnumbers + ".length;");
        impl.add("for (int i = 0; i < n; i++) {");
        impl.add("    this." + METHODNAME_JDO_COPY_FIELD
                 + "(other, " + fieldnumbers + "[i]);");
        impl.add("}");
        return impl;
    }

    static List getJDOCopyFieldImpl(String   classname,
                                    String   copy,
                                    String   fieldnumber,
                                    String[] fieldnames,
                                    boolean  isRoot)
    {
        classname = normalizeClassName(classname);
        final List impl = new ArrayList(50);
        impl.add("switch (" + fieldnumber
                 + " - " + FIELDNAME_JDO_INHERITED_FIELD_COUNT + ") {");
        for (int i = 0; i < fieldnames.length; i++) {
            impl.add("case " + i + ':');
            impl.add("    if (" + copy + " == null) {");
            impl.add("        throw new IllegalArgumentException(\"arg1\");");
            impl.add("    }");
            impl.add("    this." + fieldnames[i]
                     + " = " + copy + "." + fieldnames[i] + ';');
            impl.add("    return;");
        }
        impl.add("default:");
        if (isRoot) {
            impl.add("    throw new IllegalArgumentException(\"arg2\");");
        } else {
            impl.add("    super." + METHODNAME_JDO_COPY_FIELD
                     + "(" + copy + ", " + fieldnumber + ");");
        }
        impl.add("}");
        return impl;
    }

    static List getWriteObjectImpl(String out)
    {
        final List impl = new ArrayList(5);
        impl.add(METHODNAME_JDO_PRE_SERIALIZE + "();");
        impl.add(out + ".defaultWriteObject();");

        return impl;
    }

    static List getJDOStateManagerVoidDelegationImpl(String delegation)
    {
        final List impl = new ArrayList(5);
        impl.add("final " + CLASSNAME_JDO_STATE_MANAGER
                 + " sm = this." + FIELDNAME_JDO_STATE_MANAGER + ";");
        impl.add("if (sm != null) {");
        impl.add("    sm." + delegation + ';');
        impl.add("}");
        return impl;

    }

    static List getJDOStateManagerObjectDelegationImpl(String delegation)
    {
        final List impl = new ArrayList(5);
        impl.add("final " + CLASSNAME_JDO_STATE_MANAGER
                 + " sm = this." + FIELDNAME_JDO_STATE_MANAGER + ";");
        impl.add("if (sm != null) {");
        impl.add("    return sm." + delegation + ';');
        impl.add("}");
        impl.add("return null;");
        return impl;
    }

    static List getJDOStateManagerBooleanDelegationImpl(String delegation)
    {
        final List impl = new ArrayList(5);
        impl.add("final " + CLASSNAME_JDO_STATE_MANAGER
                 + " sm = this." + FIELDNAME_JDO_STATE_MANAGER + ";");
        impl.add("if (sm != null) {");
        impl.add("    return sm." + delegation + ';');
        impl.add("}");
        impl.add("return false;");
        return impl;
    }

    static List getJDOFieldIterationImpl(String fieldnumbers,
                                         String method)
    {
        final List impl = new ArrayList(10);
        impl.add("if (" + fieldnumbers + " == null) {");
        impl.add("    throw new IllegalArgumentException(\"arg1\");");
        impl.add("}");
        impl.add("final int n = " + fieldnumbers + ".length;");
        impl.add("for (int i = 0; i < n; i++) {");
        impl.add("    this." + method + "(" + fieldnumbers + "[i]);");
        impl.add("}");
        return impl;
    }

    static List getOidHashCodeImpl(String[] pknames,
                                   String[] pktypes,
                                   boolean  isRoot)
    {
        final List impl = new ArrayList(3);
        if (isRoot) {
            impl.add("int hash = 0;");
        } else {
            impl.add("int hash = super.hashCode();");
        }
        for (int i = 0; i < pknames.length; i++) {
            if (isPrimitiveClass(pktypes[i])) {
                if (pktypes[i].equals("boolean")) {
                    impl.add("hash += (" + pknames[i] + " ? 1 : 0);");
                } else {
                    impl.add("hash += (int)" + pknames[i] + ';');
                }
            } else {
                impl.add("hash += (this." + pknames[i]
                         + " != null ? this." + pknames[i]
                         + ".hashCode() : 0);");
            }
        }
        impl.add("return hash;");
        return impl;
    }

    static List getOidEqualsImpl(String   oidclassname,
                                 String[] pknames,
                                 String[] pktypes,
                                 String   pk,
                                 boolean  isRoot)
    {
        final List impl = new ArrayList(3);
        if (isRoot) {
            impl.add("if (" + pk + " == null || !this.getClass().equals("
                     + pk + ".getClass())) {");
        } else {
            impl.add("if (!super.equals(" + pk + ")) {");
        }
        impl.add("    return false;");
        impl.add("}");
        oidclassname = getClassName(oidclassname);
        impl.add(oidclassname + " oid = (" + oidclassname + ")" + pk + ';');
        for (int i = 0; i < pknames.length; i++) {
            if (isPrimitiveClass(pktypes[i])) {
                impl.add("if (this." + pknames[i] + " != oid."
                         + pknames[i] + ") return false;");
            } else {
                impl.add("if (this." + pknames[i] + " != oid."
                         + pknames[i] + " && (this." + pknames[i]
                         + " == null || " + "!this." + pknames[i]
                         + ".equals(oid." + pknames[i]
                         + "))) return false;");
            }
        }
        impl.add("return true;");
        return impl;
    }

    static private boolean isPrimitiveClass(String classname)
    {
        return (classname.equals("int")
                || classname.equals("long")
                || classname.equals("short")
                || classname.equals("byte")
                || classname.equals("boolean")
                || classname.equals("char")
                || classname.equals("double")
                || classname.equals("float"));
    }
    
    private static String lowerCaseFirstChar(String str) {
        if (str == null) {
            return null;
        }
        if (str.length() == 1) {
            return str.toLowerCase();
        } else {
            char ch = str.charAt(0);
            return Character.toLowerCase(ch) + str.substring(1);
        }
    }
}
