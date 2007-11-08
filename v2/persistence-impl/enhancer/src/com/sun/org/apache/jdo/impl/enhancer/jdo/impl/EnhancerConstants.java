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

package com.sun.org.apache.jdo.impl.enhancer.jdo.impl;

import com.sun.org.apache.jdo.impl.enhancer.meta.EnhancerMetaData;

interface VMConstants {

	public static final int ACCPrivate = 2;

	public static final int ACCProtected = 4;

	public static final int ACCPublic = 1;

	public static final int ACCStatic = 8;

	public static final int ACCFinal = 16;
	
	public static final int ACCTransient = 128;
	
	public static final int ACCSynchronized = 32;
	
	public static final int ACCSynthetic = 4096;
	
}

/**
 * Constant definitions by the Java2 platform specification.
 */
interface JAVA_ClassConstants
	extends VMConstants
{
    String JAVA_Object_Name
        = "Object";
    String JAVA_Object_Path
        = NameHelper.javaLangPathForType(JAVA_Object_Name);
    String JAVA_Object_Sig
        = NameHelper.sigForPath(JAVA_Object_Path);
    String JAVA_Object_Type
        = NameHelper.typeForPath(JAVA_Object_Path);

    String JAVA_Boolean_Name
        = "Boolean";
    String JAVA_Boolean_Path
        = NameHelper.javaLangPathForType(JAVA_Boolean_Name);
    String JAVA_Boolean_Sig
        = NameHelper.sigForPath(JAVA_Boolean_Path);
    String JAVA_Boolean_Type
        = NameHelper.typeForPath(JAVA_Boolean_Path);

    String JAVA_Character_Name
        = "Character";
    String JAVA_Character_Path
        = NameHelper.javaLangPathForType(JAVA_Character_Name);
    String JAVA_Character_Sig
        = NameHelper.sigForPath(JAVA_Character_Path);
    String JAVA_Character_Type
        = NameHelper.typeForPath(JAVA_Character_Path);

    String JAVA_Byte_Name
        = "Byte";
    String JAVA_Byte_Path
        = NameHelper.javaLangPathForType(JAVA_Byte_Name);
    String JAVA_Byte_Sig
        = NameHelper.sigForPath(JAVA_Byte_Path);
    String JAVA_Byte_Type
        = NameHelper.typeForPath(JAVA_Byte_Path);

    String JAVA_Short_Name
        = "Short";
    String JAVA_Short_Path
        = NameHelper.javaLangPathForType(JAVA_Short_Name);
    String JAVA_Short_Sig
        = NameHelper.sigForPath(JAVA_Short_Path);
    String JAVA_Short_Type
        = NameHelper.typeForPath(JAVA_Short_Path);

    String JAVA_Integer_Name
        = "Integer";
    String JAVA_Integer_Path
        = NameHelper.javaLangPathForType(JAVA_Integer_Name);
    String JAVA_Integer_Sig
        = NameHelper.sigForPath(JAVA_Integer_Path);
    String JAVA_Integer_Type
        = NameHelper.typeForPath(JAVA_Integer_Path);

    String JAVA_Long_Name
        = "Long";
    String JAVA_Long_Path
        = NameHelper.javaLangPathForType(JAVA_Long_Name);
    String JAVA_Long_Sig
        = NameHelper.sigForPath(JAVA_Long_Path);
    String JAVA_Long_Type
        = NameHelper.typeForPath(JAVA_Long_Path);

    String JAVA_Float_Name
        = "Float";
    String JAVA_Float_Path
        = NameHelper.javaLangPathForType(JAVA_Float_Name);
    String JAVA_Float_Sig
        = NameHelper.sigForPath(JAVA_Float_Path);
    String JAVA_Float_Type
        = NameHelper.typeForPath(JAVA_Float_Path);

    String JAVA_Double_Name
        = "Double";
    String JAVA_Double_Path
        = NameHelper.javaLangPathForType(JAVA_Double_Name);
    String JAVA_Double_Sig
        = NameHelper.sigForPath(JAVA_Double_Path);
    String JAVA_Double_Type
        = NameHelper.typeForPath(JAVA_Double_Path);

    String JAVA_Class_Name
        = "Class";
    String JAVA_Class_Path
        = NameHelper.javaLangPathForType(JAVA_Class_Name);
    String JAVA_Class_Sig
        = NameHelper.sigForPath(JAVA_Class_Path);
    String JAVA_Class_Type
        = NameHelper.typeForPath(JAVA_Class_Path);

    String JAVA_String_Name
        = "String";
    String JAVA_String_Path
        = NameHelper.javaLangPathForType(JAVA_String_Name);
    String JAVA_String_Sig
        = NameHelper.sigForPath(JAVA_String_Path);
    String JAVA_String_Type
        = NameHelper.typeForPath(JAVA_String_Path);

    String JAVA_Throwable_Name
        = "Throwable";
    String JAVA_Throwable_Path
        = NameHelper.javaLangPathForType(JAVA_Throwable_Name);
    String JAVA_Throwable_Sig
        = NameHelper.sigForPath(JAVA_Throwable_Path);
    String JAVA_Throwable_Type
        = NameHelper.typeForPath(JAVA_Throwable_Path);

    String JAVA_ClassNotFoundException_Name
        = "ClassNotFoundException";
    String JAVA_ClassNotFoundException_Path
        = NameHelper.javaLangPathForType(JAVA_ClassNotFoundException_Name);
    String JAVA_ClassNotFoundException_Sig
        = NameHelper.sigForPath(JAVA_ClassNotFoundException_Path);
    String JAVA_ClassNotFoundException_Type
        = NameHelper.typeForPath(JAVA_ClassNotFoundException_Path);

    String JAVA_NoClassDefFoundError_Name
        = "NoClassDefFoundError";
    String JAVA_NoClassDefFoundError_Path
        = NameHelper.javaLangPathForType(JAVA_NoClassDefFoundError_Name);
    String JAVA_NoClassDefFoundError_Sig
        = NameHelper.sigForPath(JAVA_NoClassDefFoundError_Path);
    String JAVA_NoClassDefFoundError_Type
        = NameHelper.typeForPath(JAVA_NoClassDefFoundError_Path);

    String JAVA_System_Name
        = "System";
    String JAVA_System_Path
        = NameHelper.javaLangPathForType(JAVA_System_Name);
    String JAVA_System_Sig
        = NameHelper.sigForPath(JAVA_System_Path);
    String JAVA_System_Type
        = NameHelper.typeForPath(JAVA_System_Path);

    String JAVA_SecurityManager_Name
        = "SecurityManager";
    String JAVA_SecurityManager_Path
        = NameHelper.javaLangPathForType(JAVA_SecurityManager_Name);
    String JAVA_SecurityManager_Sig
        = NameHelper.sigForPath(JAVA_SecurityManager_Path);
    String JAVA_SecurityManager_Type
        = NameHelper.typeForPath(JAVA_SecurityManager_Path);

    String JAVA_Permission_Name
        = "Permission";
    String JAVA_Permission_Path
        = "java/security/" + JAVA_Permission_Name;
    String JAVA_Permission_Sig
        = NameHelper.sigForPath(JAVA_Permission_Path);
    String JAVA_Permission_Type
        = NameHelper.typeForPath(JAVA_Permission_Path);

    String JAVA_ObjectOutputStream_Name
        = "ObjectOutputStream";
    String JAVA_ObjectOutputStream_Path
        = "java/io/" + JAVA_ObjectOutputStream_Name;
    String JAVA_ObjectOutputStream_Sig
        = NameHelper.sigForPath(JAVA_ObjectOutputStream_Path);
    String JAVA_ObjectOutputStream_Type
        = NameHelper.typeForPath(JAVA_ObjectOutputStream_Path);

    String JAVA_ObjectInputStream_Name
        = "ObjectInputStream";
    String JAVA_ObjectInputStream_Path
        = "java/io/" + JAVA_ObjectInputStream_Name;
    String JAVA_ObjectInputStream_Sig
        = NameHelper.sigForPath(JAVA_ObjectInputStream_Path);
    String JAVA_ObjectInputStream_Type
        = NameHelper.typeForPath(JAVA_ObjectInputStream_Path);

    String JAVA_IllegalArgumentException_Name
        = "IllegalArgumentException";
    String JAVA_IllegalArgumentException_Path
        = NameHelper.javaLangPathForType(JAVA_IllegalArgumentException_Name);
    String JAVA_IllegalArgumentException_Sig
        = NameHelper.sigForPath(JAVA_IllegalArgumentException_Path);
    String JAVA_IllegalArgumentException_Type
        = NameHelper.typeForPath(JAVA_IllegalArgumentException_Path);

    String JAVA_IllegalStateException_Name
        = "IllegalStateException";
    String JAVA_IllegalStateException_Path
        = NameHelper.javaLangPathForType(JAVA_IllegalStateException_Name);
    String JAVA_IllegalStateException_Sig
        = NameHelper.sigForPath(JAVA_IllegalStateException_Path);
    String JAVA_IllegalStateException_Type
        = NameHelper.typeForPath(JAVA_IllegalStateException_Path);

    // void XXX.<clinit>()
    String JAVA_clinit_Name
        = "<clinit>";
    String JAVA_clinit_Sig
        = "()V";
    int JAVA_clinit_Mods
        = ACCStatic;

    // Object Object.clone()
    String JAVA_Object_clone_Name
        = "clone";
    String JAVA_Object_clone_Sig
        = "()" + JAVA_Object_Sig;

    // void [Object].writeObject(java.io.ObjectOutputStream)
    //    throws IOException
    String JAVA_Object_writeObject_Name
        = "writeObject";
    String JAVA_Object_writeObject_Sig
        = "(" + JAVA_ObjectOutputStream_Sig + ")V";
    int JAVA_Object_writeObject_Mods
        = ACCPrivate;

    // void [ObjectOutputStream].defaultWriteObject()
    //    throws IOException
    String JAVA_ObjectOutputStream_defaultWriteObject_Name
        = "defaultWriteObject";
    String JAVA_ObjectOutputStream_defaultWriteObject_Sig
        = "()V";

    // Object writeReplace() 
    //    throws java.io.ObjectStreamException
    String JAVA_Object_writeReplace_Name
        = "writeReplace";
    String JAVA_Object_writeReplace_Sig
        = "()" + JAVA_Object_Sig;

    // void [Object].readObject(java.io.ObjectInputStream)
    //    throws IOException, ClassNotFoundException
    String JAVA_Object_readObject_Name
        = "readObject";
    String JAVA_Object_readObject_Sig
        = "(" + JAVA_ObjectInputStream_Sig + ")V";

    // Class Boolean.TYPE
    String JAVA_Boolean_TYPE_Name
        = "TYPE";
    String JAVA_Boolean_TYPE_Sig
        = JAVA_Class_Sig;

    // Class Character.TYPE
    String JAVA_Character_TYPE_Name
        = "TYPE";
    String JAVA_Character_TYPE_Sig
        = JAVA_Class_Sig;

    // Class Byte.TYPE
    String JAVA_Byte_TYPE_Name
        = "TYPE";
    String JAVA_Byte_TYPE_Sig
        = JAVA_Class_Sig;

    // Class Short.TYPE
    String JAVA_Short_TYPE_Name
        = "TYPE";
    String JAVA_Short_TYPE_Sig
        = JAVA_Class_Sig;

    // Class Integer.TYPE
    String JAVA_Integer_TYPE_Name
        = "TYPE";
    String JAVA_Integer_TYPE_Sig
        = JAVA_Class_Sig;

    // Class Long.TYPE
    String JAVA_Long_TYPE_Name
        = "TYPE";
    String JAVA_Long_TYPE_Sig
        = JAVA_Class_Sig;

    // Class Float.TYPE
    String JAVA_Float_TYPE_Name
        = "TYPE";
    String JAVA_Float_TYPE_Sig
        = JAVA_Class_Sig;

    // Class Double.TYPE
    String JAVA_Double_TYPE_Name
        = "TYPE";
    String JAVA_Double_TYPE_Sig
        = JAVA_Class_Sig;

    // Class Class.forName(String)
    String JAVA_Class_forName_Name
        = "forName";
    String JAVA_Class_forName_Sig
        = "(" + JAVA_String_Sig + ")" + JAVA_Class_Sig;

    // String Throwable.getMessage()
    String JAVA_Throwable_getMessage_Name
        = "getMessage";
    String JAVA_Throwable_getMessage_Sig
        = "()" + JAVA_String_Sig;

    // NoClassDefFoundError.NoClassDefFoundError(String)
    String JAVA_NoClassDefFoundError_NoClassDefFoundError_Name
        = NameHelper.constructorName();
    String JAVA_NoClassDefFoundError_NoClassDefFoundError_Sig
        = NameHelper.constructorSig(JAVA_String_Sig);

    // SecurityManager System.getSecurityManager()
    String JAVA_System_getSecurityManager_Name
        = "getSecurityManager";
    String JAVA_System_getSecurityManager_Sig
        = "()" + JAVA_SecurityManager_Sig;

    // void SecurityManager.checkPermission(Permission)
    String JAVA_SecurityManager_checkPermission_Name
        = "checkPermission";
    String JAVA_SecurityManager_checkPermission_Sig
        = "(" + JAVA_Permission_Sig + ")V";
}

/**
 * Constant definitions for JDO classes.
 */
interface JDO_ClassConstants
    extends JAVA_ClassConstants
{
    String JDO_PersistenceCapable_Name
        = "PersistenceCapable";
    String JDO_PersistenceCapable_Path
        = JDONameHelper.jdoSPIPathForType(JDO_PersistenceCapable_Name);
    String JDO_PersistenceCapable_Sig
        = NameHelper.sigForPath(JDO_PersistenceCapable_Path);
    String JDO_PersistenceCapable_Type
        = NameHelper.typeForPath(JDO_PersistenceCapable_Path);

    String JDO_InstanceCallbacks_Name
        = "InstanceCallbacks";
    String JDO_InstanceCallbacks_Path
        = JDONameHelper.jdoPathForType(JDO_InstanceCallbacks_Name);
    String JDO_InstanceCallbacks_Sig
        = NameHelper.sigForPath(JDO_InstanceCallbacks_Path);
    String JDO_InstanceCallbacks_Type
        = NameHelper.typeForPath(JDO_InstanceCallbacks_Path);

/*
    String JDO_SecondClassObjectBase_Name
        = "SCO";
    String JDO_SecondClassObjectBase_Path
        = JDONameHelper.jdoPathForType(JDO_SecondClassObjectBase_Name);
    String JDO_SecondClassObjectBase_Sig
        = NameHelper.sigForPath(JDO_SecondClassObjectBase_Path);
    String JDO_SecondClassObjectBase_Type
        = NameHelper.typeForPath(JDO_SecondClassObjectBase_Path)
*/

    String JDO_JDOPermission_Name
        = "JDOPermission";
    String JDO_JDOPermission_Path
        = JDONameHelper.jdoSPIPathForType(JDO_JDOPermission_Name);
    String JDO_JDOPermission_Sig
        = NameHelper.sigForPath(JDO_JDOPermission_Path);
    String JDO_JDOPermission_Type
        = NameHelper.typeForPath(JDO_JDOPermission_Path);

    String JDO_PersistenceManager_Name
        = "PersistenceManager";
    String JDO_PersistenceManager_Path
        = JDONameHelper.jdoPathForType(JDO_PersistenceManager_Name);
    String JDO_PersistenceManager_Sig
        = NameHelper.sigForPath(JDO_PersistenceManager_Path);
    String JDO_PersistenceManager_Type
        = NameHelper.typeForPath(JDO_PersistenceManager_Path);

    String JDO_StateManager_Name
        = "StateManager";
    String JDO_StateManager_Path
        = JDONameHelper.jdoSPIPathForType(JDO_StateManager_Name);
    String JDO_StateManager_Sig
        = NameHelper.sigForPath(JDO_StateManager_Path);
    String JDO_StateManager_Type
        = NameHelper.typeForPath(JDO_StateManager_Path);

    String JDO_ObjectIdFieldSupplier_Name
        = "ObjectIdFieldSupplier";
    String JDO_ObjectIdFieldSupplier_Path
        = JDONameHelper.jdoSPIPathForType(JDO_PersistenceCapable_Name
                                          + "$"
                                          + JDO_ObjectIdFieldSupplier_Name);
    String JDO_ObjectIdFieldSupplier_Sig
        = NameHelper.sigForPath(JDO_ObjectIdFieldSupplier_Path);
    String JDO_ObjectIdFieldSupplier_Type
        = NameHelper.typeForPath(JDO_ObjectIdFieldSupplier_Path);

    String JDO_ObjectIdFieldConsumer_Name
        = "ObjectIdFieldConsumer";
    String JDO_ObjectIdFieldConsumer_Path
        = JDONameHelper.jdoSPIPathForType(JDO_PersistenceCapable_Name
                                          + "$"
                                          + JDO_ObjectIdFieldConsumer_Name);
    String JDO_ObjectIdFieldConsumer_Sig
        = NameHelper.sigForPath(JDO_ObjectIdFieldConsumer_Path);
    String JDO_ObjectIdFieldConsumer_Type
        = NameHelper.typeForPath(JDO_ObjectIdFieldConsumer_Path);

    String JDO_JDOImplHelper_Name
        = "JDOImplHelper";
    String JDO_JDOImplHelper_Path
        = JDONameHelper.jdoSPIPathForType(JDO_JDOImplHelper_Name);
    String JDO_JDOImplHelper_Sig
        = NameHelper.sigForPath(JDO_JDOImplHelper_Path);
    String JDO_JDOImplHelper_Type
        = NameHelper.typeForPath(JDO_JDOImplHelper_Path);

    String JDO_JDOFatalInternalException_Name
        = "JDOFatalInternalException";
    String JDO_JDOFatalInternalException_Path
        = JDONameHelper.jdoPathForType(JDO_JDOFatalInternalException_Name);
    String JDO_JDOFatalInternalException_Sig
        = NameHelper.sigForPath(JDO_JDOFatalInternalException_Path);
    String JDO_JDOFatalInternalException_Type
        = NameHelper.typeForPath(JDO_JDOFatalInternalException_Path);

    // string constant for JDOPermission
    String JDO_JDOPermission_setStateManager_Name
        = "setStateManager";

    // JDOPermission.JDOPermission(String)
    String JDO_JDOPermission_JDOPermission_Name
        = NameHelper.constructorName();
    String JDO_JDOPermission_JDOPermission_Sig
        = NameHelper.constructorSig(JAVA_String_Sig);
}

/**
 * Constant definitions for members of the PersistenceCapable interface.
 */
interface JDO_PC_MemberConstants
    extends JAVA_ClassConstants, JDO_ClassConstants, VMConstants
{
    // JDO flags values
    //byte READ_WRITE_OK = 0;
    //byte LOAD_REQUIRED = 1;
    //byte READ_OK = -1;

    // JDO field access flags
    int CHECK_READ     = EnhancerMetaData.CHECK_READ;
    int MEDIATE_READ   = EnhancerMetaData.MEDIATE_READ;
    int CHECK_WRITE    = EnhancerMetaData.CHECK_WRITE;
    int MEDIATE_WRITE  = EnhancerMetaData.MEDIATE_WRITE;
    int SERIALIZABLE   = EnhancerMetaData.SERIALIZABLE;

    // Generic Augmentation

    // StateManager jdoStateManager
    String JDO_PC_jdoStateManager_Name
        = "jdoStateManager";
    String JDO_PC_jdoStateManager_Sig
        = JDO_StateManager_Sig;
    int JDO_PC_jdoStateManager_Mods
        = (ACCProtected | ACCTransient);

    // byte jdoFlags
    String JDO_PC_jdoFlags_Name
        = "jdoFlags";
    String JDO_PC_jdoFlags_Sig
        = "B";
    int JDO_PC_jdoFlags_Mods
        = (ACCProtected | ACCTransient);

    // void jdoReplaceStateManager(StateManager)
    String JDO_PC_jdoReplaceStateManager_Name
        = "jdoReplaceStateManager";
    String JDO_PC_jdoReplaceStateManager_Sig
        = "(" + JDO_StateManager_Sig + ")V";
    int JDO_PC_jdoReplaceStateManager_Mods
        = (ACCPublic | ACCFinal | ACCSynchronized);

    // void jdoReplaceFlags()
    String JDO_PC_jdoReplaceFlags_Name
        = "jdoReplaceFlags";
    String JDO_PC_jdoReplaceFlags_Sig
        = "()V";
    int JDO_PC_jdoReplaceFlags_Mods
        = (ACCPublic | ACCFinal);

    // PersistenceManager jdoGetPersistenceManager()
    String JDO_PC_jdoGetPersistenceManager_Name
        = "jdoGetPersistenceManager";
    String JDO_PC_jdoGetPersistenceManager_Sig
        = "()" + JDO_PersistenceManager_Sig;
    int JDO_PC_jdoGetPersistenceManager_Mods
        = (ACCPublic | ACCFinal);

    // Object jdoGetObjectId()
    String JDO_PC_jdoGetObjectId_Name
        = "jdoGetObjectId";
    String JDO_PC_jdoGetObjectId_Sig
        = "()" + JAVA_Object_Sig;
    int JDO_PC_jdoGetObjectId_Mods
        = (ACCPublic | ACCFinal);

    // Object jdoGetTransactionalObjectId()
    String JDO_PC_jdoGetTransactionalObjectId_Name
        = "jdoGetTransactionalObjectId";
    String JDO_PC_jdoGetTransactionalObjectId_Sig
        = "()" + JAVA_Object_Sig;
    int JDO_PC_jdoGetTransactionalObjectId_Mods
        = (ACCPublic | ACCFinal);

    // boolean jdoIsPersistent()
    String JDO_PC_jdoIsPersistent_Name
        = "jdoIsPersistent";
    String JDO_PC_jdoIsPersistent_Sig
        = "()Z";
    int JDO_PC_jdoIsPersistent_Mods
        = (ACCPublic | ACCFinal);

    // boolean jdoIsTransactional()
    String JDO_PC_jdoIsTransactional_Name
        = "jdoIsTransactional";
    String JDO_PC_jdoIsTransactional_Sig
        = "()Z";
    int JDO_PC_jdoIsTransactional_Mods
        = (ACCPublic | ACCFinal);

    // boolean jdoIsNew()
    String JDO_PC_jdoIsNew_Name
        = "jdoIsNew";
    String JDO_PC_jdoIsNew_Sig
        = "()Z";
    int JDO_PC_jdoIsNew_Mods
        = (ACCPublic | ACCFinal);

    // boolean jdoIsDeleted()
    String JDO_PC_jdoIsDeleted_Name
        = "jdoIsDeleted";
    String JDO_PC_jdoIsDeleted_Sig
        = "()Z";
    int JDO_PC_jdoIsDeleted_Mods
        = (ACCPublic | ACCFinal);

    // boolean jdoIsDirty()
    String JDO_PC_jdoIsDirty_Name
        = "jdoIsDirty";
    String JDO_PC_jdoIsDirty_Sig
        = "()Z";
    int JDO_PC_jdoIsDirty_Mods
        = (ACCPublic | ACCFinal);

    // void jdoMakeDirty(String)
    String JDO_PC_jdoMakeDirty_Name
        = "jdoMakeDirty";
    String JDO_PC_jdoMakeDirty_Sig
        = "(" + JAVA_String_Sig + ")V";
    int JDO_PC_jdoMakeDirty_Mods
        = (ACCPublic | ACCFinal);

    // void jdoProvideFields(int[])
    String JDO_PC_jdoProvideFields_Name
        = "jdoProvideFields";
    String JDO_PC_jdoProvideFields_Sig
        = "([I)V";
    int JDO_PC_jdoProvideFields_Mods
        = (ACCPublic | ACCFinal);

    // void jdoReplaceFields(int[])
    String JDO_PC_jdoReplaceFields_Name
        = "jdoReplaceFields";
    String JDO_PC_jdoReplaceFields_Sig
        = "([I)V";
    int JDO_PC_jdoReplaceFields_Mods
        = (ACCPublic | ACCFinal);

    // void jdoPreSerialize()
    // augmented, but not a member of the PC interface
    String JDO_PC_jdoPreSerialize_Name
        = "jdoPreSerialize";
    String JDO_PC_jdoPreSerialize_Sig
        = "()V";
    int JDO_PC_jdoPreSerialize_Mods
        = (ACCProtected | ACCFinal);


    // Specific Augmentation

    // private static final int jdoInheritedFieldCount
    String JDO_PC_jdoInheritedFieldCount_Name
        = "jdoInheritedFieldCount";
    String JDO_PC_jdoInheritedFieldCount_Sig
        = "I";
    int JDO_PC_jdoInheritedFieldCount_Mods
        = (ACCStatic | ACCPrivate | ACCFinal);

    // private static final String[] jdoFieldNames
    String JDO_PC_jdoFieldNames_Name
        = "jdoFieldNames";
    String JDO_PC_jdoFieldNames_Sig
        = "[" + JAVA_String_Sig;
    int JDO_PC_jdoFieldNames_Mods
        = (ACCStatic | ACCPrivate | ACCFinal);

    // private static final Class[] jdoFieldTypes
    String JDO_PC_jdoFieldTypes_Name
        = "jdoFieldTypes";
    String JDO_PC_jdoFieldTypes_Sig
        = "[" + JAVA_Class_Sig;
    int JDO_PC_jdoFieldTypes_Mods
        = (ACCStatic | ACCPrivate | ACCFinal);

    // private static final byte[] jdoFieldFlags
    String JDO_PC_jdoFieldFlags_Name
        = "jdoFieldFlags";
    String JDO_PC_jdoFieldFlags_Sig
        = "[B";
    int JDO_PC_jdoFieldFlags_Mods
        = (ACCStatic | ACCPrivate | ACCFinal);

    // private static final Class jdoPersistenceCapableSuperclass
    String JDO_PC_jdoPersistenceCapableSuperclass_Name
        = "jdoPersistenceCapableSuperclass";
    String JDO_PC_jdoPersistenceCapableSuperclass_Sig
        = JAVA_Class_Sig;
    int JDO_PC_jdoPersistenceCapableSuperclass_Mods
        = (ACCStatic | ACCPrivate | ACCFinal);

    // protected static int jdoGetManagedFieldCount()
    String JDO_PC_jdoGetManagedFieldCount_Name
        = "jdoGetManagedFieldCount";
    String JDO_PC_jdoGetManagedFieldCount_Sig
        = "()I";
    int JDO_PC_jdoGetManagedFieldCount_Mods
        = (ACCStatic | ACCProtected);

    // void jdoCopyFields(Object,int[])
    String JDO_PC_jdoCopyFields_Name
        = "jdoCopyFields";
    String JDO_PC_jdoCopyFields_Sig
        = "(" + JAVA_Object_Sig + "[I)V";
    int JDO_PC_jdoCopyFields_Mods
        = (ACCPublic);

    // protected final void jdoCopyField(XXX pc, int fieldnumber)
    String JDO_PC_jdoCopyField_Name
        = "jdoCopyField";
    //String JDO_PC_jdoCopyField_Sig
    //    = "(XXXI)V";
    int JDO_PC_jdoCopyField_Mods
        = (ACCProtected | ACCFinal);

    // void jdoProvideField(int)
    String JDO_PC_jdoProvideField_Name
        = "jdoProvideField";
    String JDO_PC_jdoProvideField_Sig
        = "(I)V";
    int JDO_PC_jdoProvideField_Mods
        = (ACCPublic);

    // void jdoReplaceField(int)
    String JDO_PC_jdoReplaceField_Name
        = "jdoReplaceField";
    String JDO_PC_jdoReplaceField_Sig
        = "(I)V";
    int JDO_PC_jdoReplaceField_Mods
        = (ACCPublic);

    // PersistenceCapable jdoNewInstance(StateManager)
    String JDO_PC_jdoNewInstance_Name
        = "jdoNewInstance";
    String JDO_PC_jdoNewInstance_Sig
        = "(" + JDO_StateManager_Sig + ")" + JDO_PersistenceCapable_Sig;
    int JDO_PC_jdoNewInstance_Mods
        = (ACCPublic);

    // PersistenceCapable jdoNewInstance(StateManager,Object)
    String JDO_PC_jdoNewInstance_Object_Name
        = "jdoNewInstance";
    String JDO_PC_jdoNewInstance_Object_Sig
        = "(" + JDO_StateManager_Sig + JAVA_Object_Sig + ")" + JDO_PersistenceCapable_Sig;
    int JDO_PC_jdoNewInstance_Object_Mods
        = (ACCPublic);

    // Object jdoNewObjectIdInstance()
    String JDO_PC_jdoNewObjectIdInstance_Name
        = "jdoNewObjectIdInstance";
    String JDO_PC_jdoNewObjectIdInstance_Sig
        = "()" + JAVA_Object_Sig;
    int JDO_PC_jdoNewObjectIdInstance_Mods
        = (ACCPublic);

    // Object jdoNewObjectIdInstance(String)
    String JDO_PC_jdoNewObjectIdInstance_String_Name
        = "jdoNewObjectIdInstance";
    String JDO_PC_jdoNewObjectIdInstance_String_Sig
        = "(" + JAVA_String_Sig + ")" + JAVA_Object_Sig;
    int JDO_PC_jdoNewObjectIdInstance_String_Mods
        = (ACCPublic);

    // Object jdoNewObjectIdInstance(String)
    String JDO_PC_jdoNewObjectIdInstance_Object_Name
        = "jdoNewObjectIdInstance";
    String JDO_PC_jdoNewObjectIdInstance_Object_Sig
        = "(" + JAVA_Object_Sig + ")" + JAVA_Object_Sig;
    int JDO_PC_jdoNewObjectIdInstance_Object_Mods
        = (ACCPublic);

    // void jdoCopyKeyFieldsToObjectId(Object)
    String JDO_PC_jdoCopyKeyFieldsToObjectId_Name
        = "jdoCopyKeyFieldsToObjectId";
    String JDO_PC_jdoCopyKeyFieldsToObjectId_Sig
        = "(" + JAVA_Object_Sig + ")V";
    int JDO_PC_jdoCopyKeyFieldsToObjectId_Mods
        = (ACCPublic);

    // void jdoCopyKeyFieldsFromObjectId(Object)
    String JDO_PC_jdoCopyKeyFieldsFromObjectId_Name
        = "jdoCopyKeyFieldsFromObjectId";
    String JDO_PC_jdoCopyKeyFieldsFromObjectId_Sig
        = "(" + JAVA_Object_Sig + ")V";
    int JDO_PC_jdoCopyKeyFieldsFromObjectId_Mods
        = (ACCPublic);

    // void jdoCopyKeyFieldsToObjectId(ObjectIdFieldSupplier,Object)
    String JDO_PC_jdoCopyKeyFieldsToObjectId_OIFS_Name
        = "jdoCopyKeyFieldsToObjectId";
    String JDO_PC_jdoCopyKeyFieldsToObjectId_OIFS_Sig
        = "(" + JDO_ObjectIdFieldSupplier_Sig + JAVA_Object_Sig + ")V";
    int JDO_PC_jdoCopyKeyFieldsToObjectId_OIFS_Mods
        = (ACCPublic);

    // void jdoCopyKeyFieldsFromObjectId(ObjectIdFieldConsumer,Object)
    String JDO_PC_jdoCopyKeyFieldsFromObjectId_OIFC_Name
        = "jdoCopyKeyFieldsFromObjectId";
    String JDO_PC_jdoCopyKeyFieldsFromObjectId_OIFC_Sig
        = "(" + JDO_ObjectIdFieldConsumer_Sig + JAVA_Object_Sig + ")V";
    int JDO_PC_jdoCopyKeyFieldsFromObjectId_OIFC_Mods
        = (ACCPublic);
}

/**
 * Constant definitions for members of the ObjectIdFieldSuppplier interface.
 */
interface JDO_IC_MemberConstants
    extends JAVA_ClassConstants
{
    // void jdoPostLoad()
    String JDO_IC_jdoPostLoad_Name
        = "jdoPostLoad";
    String JDO_IC_jdoPostLoad_Sig
        = "()V";
    int JDO_IC_jdoPostLoad_Mods
        = (ACCPublic);

    // void jdoPreStore()
    String JDO_IC_jdoPreStore_Name
        = "jdoPreStore";
    String JDO_IC_jdoPreStore_Sig
        = "()V";
    int JDO_IC_jdoPreStore_Mods
        = (ACCPublic);

    // void jdoPreClear()
    String JDO_IC_jdoPreClear_Name
        = "jdoPreClear";
    String JDO_IC_jdoPreClear_Sig
        = "()V";
    int JDO_IC_jdoPreClear_Mods
        = (ACCPublic);

    // void jdoPreDelete()
    String JDO_IC_jdoPreDelete_Name
        = "jdoPreDelete";
    String JDO_IC_jdoPreDelete_Sig
        = "()V";
    int JDO_IC_jdoPreDelete_Mods
        = (ACCPublic);
}

/**
 * Constant definitions for members of the ObjectIdFieldSuppplier interface.
 */
interface JDO_OIFS_MemberConstants
    extends JAVA_ClassConstants
{
    // boolean fetchBooleanField(int)
    String JDO_OIFS_fetchBooleanField_Name
        = "fetchBooleanField";
    String JDO_OIFS_fetchBooleanField_Sig
        = "(I)Z";

    // char fetchCharField(int)
    String JDO_OIFS_fetchCharField_Name
        = "fetchCharField";
    String JDO_OIFS_fetchCharField_Sig
        = "(I)C";

    // byte fetchByteField(int)
    String JDO_OIFS_fetchByteField_Name
        = "fetchByteField";
    String JDO_OIFS_fetchByteField_Sig
        = "(I)B";

    // short fetchShortField(int)
    String JDO_OIFS_fetchShortField_Name
        = "fetchShortField";
    String JDO_OIFS_fetchShortField_Sig
        = "(I)S";

    // int fetchIntField(int)
    String JDO_OIFS_fetchIntField_Name
        = "fetchIntField";
    String JDO_OIFS_fetchIntField_Sig
        = "(I)I";

    // long fetchLongField(int)
    String JDO_OIFS_fetchLongField_Name
        = "fetchLongField";
    String JDO_OIFS_fetchLongField_Sig
        = "(I)J";

    // float fetchFloatField(int)
    String JDO_OIFS_fetchFloatField_Name
        = "fetchFloatField";
    String JDO_OIFS_fetchFloatField_Sig
        = "(I)F";

    // double fetchDoubleField(int)
    String JDO_OIFS_fetchDoubleField_Name
        = "fetchDoubleField";
    String JDO_OIFS_fetchDoubleField_Sig
        = "(I)D";

    // String fetchStringField(int)
    String JDO_OIFS_fetchStringField_Name
        = "fetchStringField";
    String JDO_OIFS_fetchStringField_Sig
        = "(I)" + JAVA_String_Sig;

    // Object fetchObjectField(int)
    String JDO_OIFS_fetchObjectField_Name
        = "fetchObjectField";
    String JDO_OIFS_fetchObjectField_Sig
        = "(I)" + JAVA_Object_Sig;
}

/**
 * Constant definitions for members of the ObjectIdFieldConsumer interface.
 */
interface JDO_OIFC_MemberConstants
    extends JAVA_ClassConstants
{
    // void storeBooleanField(int,boolean)
    String JDO_OIFC_storeBooleanField_Name
        = "storeBooleanField";
    String JDO_OIFC_storeBooleanField_Sig
        = "(IZ)V";

    // void storeCharField(int,char)
    String JDO_OIFC_storeCharField_Name
        = "storeCharField";
    String JDO_OIFC_storeCharField_Sig
        = "(IC)V";

    // void storeByteField(int,byte)
    String JDO_OIFC_storeByteField_Name
        = "storeByteField";
    String JDO_OIFC_storeByteField_Sig
        = "(IB)V";

    // void storeShortField(int,short)
    String JDO_OIFC_storeShortField_Name
        = "storeShortField";
    String JDO_OIFC_storeShortField_Sig
        = "(IS)V";

    // void storeIntField(int,int)
    String JDO_OIFC_storeIntField_Name
        = "storeIntField";
    String JDO_OIFC_storeIntField_Sig
        = "(II)V";

    // void storeLongField(int,long)
    String JDO_OIFC_storeLongField_Name
        = "storeLongField";
    String JDO_OIFC_storeLongField_Sig
        = "(IJ)V";

    // void storeFloatField(int,float)
    String JDO_OIFC_storeFloatField_Name
        = "storeFloatField";
    String JDO_OIFC_storeFloatField_Sig
        = "(IF)V";

    // void storeDoubleField(int,double)
    String JDO_OIFC_storeDoubleField_Name
        = "storeDoubleField";
    String JDO_OIFC_storeDoubleField_Sig
        = "(ID)V";

    // void storeStringField(int,String)
    String JDO_OIFC_storeStringField_Name
        = "storeStringField";
    String JDO_OIFC_storeStringField_Sig
        = "(I" + JAVA_String_Sig + ")V";

    // void storeObjectField(int,Object)
    String JDO_OIFC_storeObjectField_Name
        = "storeObjectField";
    String JDO_OIFC_storeObjectField_Sig
        = "(I" + JAVA_Object_Sig + ")V";
}

/**
 * Constant definitions for members of the StateManager interface.
 */
interface JDO_SM_MemberConstants
    extends JAVA_ClassConstants, JDO_ClassConstants
{
    // byte replacingFlags(PersistenceCapable);
    String JDO_SM_replacingFlags_Name
        = "replacingFlags";
    String JDO_SM_replacingFlags_Sig
        = "(" + JDO_PersistenceCapable_Sig + ")B";

    // StateManager replacingStateManager(PersistenceCapable,StateManager);
    String JDO_SM_replacingStateManager_Name
        = "replacingStateManager";
    String JDO_SM_replacingStateManager_Sig
        = "(" + JDO_PersistenceCapable_Sig + JDO_StateManager_Sig + ")" + JDO_StateManager_Sig;

    // boolean isDirty(PersistenceCapable);
    String JDO_SM_isDirty_Name
        = "isDirty";
    String JDO_SM_isDirty_Sig
        = "(" + JDO_PersistenceCapable_Sig + ")Z";

    // boolean isTransactional(PersistenceCapable);
    String JDO_SM_isTransactional_Name
        = "isTransactional";
    String JDO_SM_isTransactional_Sig
        = "(" + JDO_PersistenceCapable_Sig + ")Z";

    // boolean isPersistent(PersistenceCapable);
    String JDO_SM_isPersistent_Name
        = "isPersistent";
    String JDO_SM_isPersistent_Sig
        = "(" + JDO_PersistenceCapable_Sig + ")Z";

    // boolean isNew(PersistenceCapable);
    String JDO_SM_isNew_Name
        = "isNew";
    String JDO_SM_isNew_Sig
        = "(" + JDO_PersistenceCapable_Sig + ")Z";

    // boolean isDeleted(PersistenceCapable);
    String JDO_SM_isDeleted_Name
        = "isDeleted";
    String JDO_SM_isDeleted_Sig
        = "(" + JDO_PersistenceCapable_Sig + ")Z";

    // PersistenceManager getPersistenceManager(PersistenceCapable);
    String JDO_SM_getPersistenceManager_Name
        = "getPersistenceManager";
    String JDO_SM_getPersistenceManager_Sig
        = "(" + JDO_PersistenceCapable_Sig + ")" + JDO_PersistenceManager_Sig;

    // void makeDirty(PersistenceCapable,String);
    String JDO_SM_makeDirty_Name
        = "makeDirty";
    String JDO_SM_makeDirty_Sig
        = "(" + JDO_PersistenceCapable_Sig + JAVA_String_Sig + ")V";

    // Object getObjectId(PersistenceCapable);
    String JDO_SM_getObjectId_Name
        = "getObjectId";
    String JDO_SM_getObjectId_Sig
        = "(" + JDO_PersistenceCapable_Sig + ")" + JAVA_Object_Sig;

    // Object getTransactionalObjectId(PersistenceCapable);
    String JDO_SM_getTransactionalObjectId_Name
        = "getTransactionalObjectId";
    String JDO_SM_getTransactionalObjectId_Sig
        = "(" + JDO_PersistenceCapable_Sig + ")" + JAVA_Object_Sig;

    // boolean isLoaded(PersistenceCapable,int);
    String JDO_SM_isLoaded_Name
        = "isLoaded";
    String JDO_SM_isLoaded_Sig
        = "(" + JDO_PersistenceCapable_Sig + "I)Z";

    // void preSerialize(PersistenceCapable);
    String JDO_SM_preSerialize_Name
        = "preSerialize";
    String JDO_SM_preSerialize_Sig
        = "(" + JDO_PersistenceCapable_Sig + ")V";

    // boolean getBooleanField(PersistenceCapable,int,boolean);
    String JDO_SM_getBooleanField_Name
        = "getBooleanField";
    String JDO_SM_getBooleanField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "IZ)Z";

    // char getCharField(PersistenceCapable,int,char);
    String JDO_SM_getCharField_Name
        = "getCharField";
    String JDO_SM_getCharField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "IC)C";

    // byte getByteField(PersistenceCapable,int,byte);
    String JDO_SM_getByteField_Name
        = "getByteField";
    String JDO_SM_getByteField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "IB)B";

    // short getShortField(PersistenceCapable,int,short);
    String JDO_SM_getShortField_Name
        = "getShortField";
    String JDO_SM_getShortField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "IS)S";

    // int getIntField(PersistenceCapable,int,int);
    String JDO_SM_getIntField_Name
        = "getIntField";
    String JDO_SM_getIntField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "II)I";

    // long getLongField(PersistenceCapable,int,long);
    String JDO_SM_getLongField_Name
        = "getLongField";
    String JDO_SM_getLongField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "IJ)J";

    // float getFloatField(PersistenceCapable,int,float);
    String JDO_SM_getFloatField_Name
        = "getFloatField";
    String JDO_SM_getFloatField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "IF)F";

    // double getDoubleField(PersistenceCapable,int,double);
    String JDO_SM_getDoubleField_Name
        = "getDoubleField";
    String JDO_SM_getDoubleField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "ID)D";

    // String getStringField(PersistenceCapable,int,String);
    String JDO_SM_getStringField_Name
        = "getStringField";
    String JDO_SM_getStringField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "I" + JAVA_String_Sig + ")" + JAVA_String_Sig;

    // Object getObjectField(PersistenceCapable,int,Object);
    String JDO_SM_getObjectField_Name
        = "getObjectField";
    String JDO_SM_getObjectField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "I" + JAVA_Object_Sig + ")" + JAVA_Object_Sig;

    // void setBooleanField(PersistenceCapable,int,boolean,boolean);
    String JDO_SM_setBooleanField_Name
        = "setBooleanField";
    String JDO_SM_setBooleanField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "IZZ)V";

    // void setCharField(PersistenceCapable,int,char,char);
    String JDO_SM_setCharField_Name
        = "setCharField";
    String JDO_SM_setCharField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "ICC)V";

    // void setByteField(PersistenceCapable,int,byte,byte);
    String JDO_SM_setByteField_Name
        = "setByteField";
    String JDO_SM_setByteField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "IBB)V";

    // void setShortField(PersistenceCapable,int,short,short);
    String JDO_SM_setShortField_Name
        = "setShortField";
    String JDO_SM_setShortField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "ISS)V";

    // void setIntField(PersistenceCapable,int,int,int);
    String JDO_SM_setIntField_Name
        = "setIntField";
    String JDO_SM_setIntField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "III)V";

    // void setLongField(PersistenceCapable,int,long,long);
    String JDO_SM_setLongField_Name
        = "setLongField";
    String JDO_SM_setLongField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "IJJ)V";

    // void setFloatField(PersistenceCapable,int,float,float);
    String JDO_SM_setFloatField_Name
        = "setFloatField";
    String JDO_SM_setFloatField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "IFF)V";

    // void setDoubleField(PersistenceCapable,int,double,double);
    String JDO_SM_setDoubleField_Name
        = "setDoubleField";
    String JDO_SM_setDoubleField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "IDD)V";

    // void setStringField(PersistenceCapable,int,String,String);
    String JDO_SM_setStringField_Name
        = "setStringField";
    String JDO_SM_setStringField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "I" + JAVA_String_Sig + JAVA_String_Sig + ")V";

    // void setObjectField(PersistenceCapable,int,Object,Object);
    String JDO_SM_setObjectField_Name
        = "setObjectField";
    String JDO_SM_setObjectField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "I" + JAVA_Object_Sig + JAVA_Object_Sig + ")V";

    // void providedBooleanField(PersistenceCapable,int,boolean);
    String JDO_SM_providedBooleanField_Name
        = "providedBooleanField";
    String JDO_SM_providedBooleanField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "IZ)V";

    // void providedCharField(PersistenceCapable,int,char);
    String JDO_SM_providedCharField_Name
        = "providedCharField";
    String JDO_SM_providedCharField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "IC)V";

    // void providedByteField(PersistenceCapable,int,byte);
    String JDO_SM_providedByteField_Name
        = "providedByteField";
    String JDO_SM_providedByteField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "IB)V";

    // void providedShortField(PersistenceCapable,int,short);
    String JDO_SM_providedShortField_Name
        = "providedShortField";
    String JDO_SM_providedShortField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "IS)V";

    // void providedIntField(PersistenceCapable,int,int);
    String JDO_SM_providedIntField_Name
        = "providedIntField";
    String JDO_SM_providedIntField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "II)V";

    // void providedLongField(PersistenceCapable,int,long);
    String JDO_SM_providedLongField_Name
        = "providedLongField";
    String JDO_SM_providedLongField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "IJ)V";

    // void providedFloatField(PersistenceCapable,int,float);
    String JDO_SM_providedFloatField_Name
        = "providedFloatField";
    String JDO_SM_providedFloatField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "IF)V";

    // void providedDoubleField(PersistenceCapable,int,double);
    String JDO_SM_providedDoubleField_Name
        = "providedDoubleField";
    String JDO_SM_providedDoubleField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "ID)V";

    // void providedStringField(PersistenceCapable,int,String);
    String JDO_SM_providedStringField_Name
        = "providedStringField";
    String JDO_SM_providedStringField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "I" + JAVA_String_Sig + ")V";

    // void providedObjectField(PersistenceCapable,int,Object);
    String JDO_SM_providedObjectField_Name
        = "providedObjectField";
    String JDO_SM_providedObjectField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "I" + JAVA_Object_Sig + ")V";

    // boolean replacingBooleanField(PersistenceCapable,int);
    String JDO_SM_replacingBooleanField_Name
        = "replacingBooleanField";
    String JDO_SM_replacingBooleanField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "I)Z";

    // char replacingCharField(PersistenceCapable,int);
    String JDO_SM_replacingCharField_Name
        = "replacingCharField";
    String JDO_SM_replacingCharField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "I)C";

    // byte replacingByteField(PersistenceCapable,int);
    String JDO_SM_replacingByteField_Name
        = "replacingByteField";
    String JDO_SM_replacingByteField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "I)B";

    // short replacingShortField(PersistenceCapable,int);
    String JDO_SM_replacingShortField_Name
        = "replacingShortField";
    String JDO_SM_replacingShortField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "I)S";

    // int replacingIntField(PersistenceCapable,int);
    String JDO_SM_replacingIntField_Name
        = "replacingIntField";
    String JDO_SM_replacingIntField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "I)I";

    // long replacingLongField(PersistenceCapable,int);
    String JDO_SM_replacingLongField_Name
        = "replacingLongField";
    String JDO_SM_replacingLongField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "I)J";

    // float replacingFloatField(PersistenceCapable,int);
    String JDO_SM_replacingFloatField_Name
        = "replacingFloatField";
    String JDO_SM_replacingFloatField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "I)F";

    // double replacingDoubleField(PersistenceCapable,int);
    String JDO_SM_replacingDoubleField_Name
        = "replacingDoubleField";
    String JDO_SM_replacingDoubleField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "I)D";

    // String replacingStringField(PersistenceCapable,int);
    String JDO_SM_replacingStringField_Name
        = "replacingStringField";
    String JDO_SM_replacingStringField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "I)" + JAVA_String_Sig;

    // Object replacingObjectField(PersistenceCapable,int);
    String JDO_SM_replacingObjectField_Name
        = "replacingObjectField";
    String JDO_SM_replacingObjectField_Sig
        = "(" + JDO_PersistenceCapable_Sig + "I)" + JAVA_Object_Sig;
}

/**
 * Constant definitions for members of the ImplementationHelper class.
 */
interface JDO_IH_MemberConstants
    extends JAVA_ClassConstants, JDO_ClassConstants
{
    // void registerClass(Class,String[],Class[],byte[],Class,PersistenceCapable)
    String JDO_JDOImplHelper_registerClass_Name
        = "registerClass";
    String JDO_JDOImplHelper_registerClass_Sig
        = "(" + JAVA_Class_Sig + "[" + JAVA_String_Sig + "[" + JAVA_Class_Sig + "[B" + JAVA_Class_Sig + JDO_PersistenceCapable_Sig + ")V";

    // void checkAuthorizedStateManager(StateManager)
    String JDO_JDOImplHelper_checkAuthorizedStateManager_Name
        = "checkAuthorizedStateManager";
    String JDO_JDOImplHelper_checkAuthorizedStateManager_Sig
        = "(" + JDO_StateManager_Sig + ")V";
}

/**
 * Constant definitions for members of the JDOFatalInternalException class.
 */
interface JDO_FIE_MemberConstants
    extends JAVA_ClassConstants, JDO_ClassConstants
{
    // JDOFatalInternalException(String)
    String JDO_JDOFatalInternalException_JDOFatalInternalException_Name
        = NameHelper.constructorName();
    String JDO_JDOFatalInternalException_JDOFatalInternalException_Sig
        = NameHelper.constructorSig();
}

/**
 * Constant definitions specific to this enhancer implementation.
 */
public interface EnhancerConstants
    extends JAVA_ClassConstants
{
    // constants for the class level enhancement attribute
    String SUNJDO_PC_EnhancedAttribute
        = "com.sun.jdori.enhancer.enhanced";
    short SUNJDO_PC_EnhancedVersion
        = 1;

    // Class sunjdoClassForName(String)
    String SUNJDO_PC_sunjdoClassForName_Name
        = "sunjdo$classForName$";
    String SUNJDO_PC_sunjdoClassForName_Sig
        = "(" + JAVA_String_Sig + ")" + JAVA_Class_Sig;
    int SUNJDO_PC_sunjdoClassForName_Mods
        = (ACCStatic | ACCProtected | ACCFinal);
}
