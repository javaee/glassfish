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

package com.sun.org.apache.jdo.impl.enhancer.meta;


/**
 * Provides the JDO meta information neccessary for byte-code enhancement.
 * <p>
 * <b>Please note: This interface deals with fully qualified names in the
 * JVM notation, that is, with '/' as package separator character&nbsp;
 * (instead of '.').</b>
 */
public interface EnhancerMetaData
{
    /**
     * The JDO field flags.
     */
    int CHECK_READ    = 0x01;
    int MEDIATE_READ  = 0x02;
    int CHECK_WRITE   = 0x04;
    int MEDIATE_WRITE = 0x08;
    int SERIALIZABLE  = 0x10;

    // ----------------------------------------------------------------------
    // Class Metadata
    // ----------------------------------------------------------------------

    /**
     * Tests if a class is not to be modified by the enhancer.
     * <P>
     * It is an error if an unenhanceable class is persistence-capable
     * (or persistence-aware).  The following holds:
     *   isKnownUnenhancableClass(classPath)
     *       ==> !isPersistenceCapableClass(classPath)
     * @param classPath the non-null JVM-qualified name of the class
     * @return <code>true</code> if this class is known to be unmodifiable
     * @see #isPersistenceCapableClass(String)
     */
    boolean isKnownUnenhancableClass(String classPath)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError;

    /**
     * Tests if a class is persistence-capable.
     * <P>
     * If a persistence-capable class is also known to be unenhancable,
     * an exception is thrown.
     * The following holds:
     *   isPersistenceCapableClass(classPath)
     *       ==> !isKnownUnenhancableClass(classPath)
     * @param classPath the non-null JVM-qualified name of the class
     * @return <code>true</code> if this class is persistence-capable
     * @see #isKnownUnenhancableClass(String)
     */
    boolean isPersistenceCapableClass(String classPath)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError;

    /**
     * Tests if a class implements java.io.Serializable.
     * <P>
     * @param classPath the non-null JVM-qualified name of the class
     * @return <code>true</code> if this class is serializable
     */
    boolean isSerializableClass(String classPath)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError;

    /**
     * Returns the name of the persistence-capable superclass of a class.
     * <P>
     * The following holds:
     *   (String s = getPersistenceCapableSuperClass(classPath)) != null
     *       ==> isPersistenceCapableClass(classPath)
     *           && !isPersistenceCapableRootClass(classPath)
     * @param classPath the non-null JVM-qualified name of the class
     * @return the name of the PC superclass or null if there is none
     * @see #isPersistenceCapableClass(String)
     * @see #getPersistenceCapableRootClass(String)
     */
    String getPersistenceCapableSuperClass(String classPath)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError;

    /**
     * Returns the name of the key class of a class.
     * <P>
     * The following holds:
     *   (String s = getKeyClass(classPath)) != null
     *       ==> !isPersistenceCapableClass(s)
     *           && isPersistenceCapableClass(classPath)
     * @param classPath the non-null JVM-qualified name of the class
     * @return the name of the key class or null if there is none
     * @see #isPersistenceCapableClass(String)
     */
    String getKeyClass(String classPath)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError;

    /**
     * Returns the names of all declared, jdo-managed fields of a class.
     * <P>
     * The position of the field names in the result array corresponds
     * to their unique field index as returned by getFieldNumber such that
     * these equations hold:
     * <P> getFieldNumber(getManagedFields(classPath)[i]) == i
     * <P> getManagedFields(classPath)[getFieldNumber(fieldName)] == fieldName
     * <P>
     * This method requires all fields having been declared by
     * declareField().
     * @param classPath the non-null JVM-qualified name of the class
     * @return an array of names of all declared, jdo-managed fields
     * @see #getFieldNumber(String, String)
     * @see #declareField(String, String, String)
     */
    String[] getManagedFields(String classPath)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError;

    // ----------------------------------------------------------------------
    // Field Metadata
    // ----------------------------------------------------------------------

    /** 
     * Returns the JVM-qualified name of a field's declaring class.
     * <P>
     * The method first checks whether the class of the specified
     * classPath (the JVM-qualified name) declares such a field. If yes,
     * classPath is returned. Otherwise, it checks its superclasses. The
     * method returns <code>null</code> for an unkown field.
     * @param classPath the non-null JVM-qualified name of the class
     * @param fieldName the non-null name of the field
     * @return the JVM-qualified name of the declararing class of the
     * field, or <code>null</code> if there is no such field.
     */
    String getDeclaringClass(String classPath, String fieldName)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError; 

    /**
     * Declares a field with the JDO model.
     * <P>
     * By the new JDO model, it's a requirement to declare fields to
     * the model for their type information before any field information
     * based on persistence-modifiers can be retrieved.  This method
     * passes a field's type information to the underlying JDO model.
     * <P>
     * There's one important exception: The method isKnownNonManagedField()
     * may be called at any time.
     * <P>
     * The class must be persistence-capable, otherwise an exception
     * is thrown.
     * @param classPath the non-null JVM-qualified name of the class
     * @param fieldName the non-null name of the field
     * @param fieldSig the non-null JVM signature of the field
     * @see #isPersistenceCapableClass(String)
     */
    void declareField(String classPath, String fieldName, String fieldSig)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError;

    /**
     * Tests if a field is known to be non-managed.
     * <P>
     * This method differs from isManagedField() in that a field may or
     * may not be managed if its not known as non-managed.
     * The following holds (not vice versa!):
     *   isKnownNonManagedField(classPath, fieldName, fieldSig)
     *       ==> !isManagedField(classPath, fieldName)
     * <P>
     * This method doesn't require the field having been declared by
     * declareField().
     * @param classPath the non-null JVM-qualified name of the class
     * @param fieldName the non-null name of the field
     * @param fieldSig the non-null type signature of the field
     * @return <code>true</code> if this field is known to be non-managed
     * @see #isManagedField(String, String)
     * @see #declareField(String, String, String)
     */
    boolean isKnownNonManagedField(String classPath,
                                   String fieldName,
                                   String fieldSig)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError;

    /**
     * Tests if a field is managed by a JDO implementation.
     * <P>
     * A managed field must not be known as non-managed.
     * The following holds:
     *   isManagedField(classPath, fieldName)
     *       ==> !isKnownNonManagedField(classPath, fieldName, fieldSig)
     *           && (isPersistentField(classPath, fieldName)
     *               ^ isTransactionalField(classPath, fieldName))
     * <P>
     * This method requires the field having been declared by declareField().
     * @param classPath the non-null JVM-qualified name of the class
     * @param fieldName the non-null name of the field
     * @return <code>true</code> if this field is managed
     * @see #isKnownNonManagedField(String, String, String)
     * @see #isPersistentField(String, String)
     * @see #isTransactionalField(String, String)
     * @see #isPersistenceCapableClass(String)
     */
    boolean isManagedField(String classPath, String fieldName)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError;

    /**
     * Tests if a field is transient-transactional.
     * <P>
     * The following holds:
     *   isTransactionalField(classPath, fieldName)
     *       ==> isManagedField(classPath, fieldName)
     *           && !isPersistentField(classPath, fieldName)
     * <P>
     * This method requires the field having been declared by declareField().
     * @param classPath the non-null JVM-qualified name of the class
     * @param fieldName the non-null name of the field
     * @return <code>true</code> if this field is transactional
     * @see #isManagedField(String, String)
     * @see #declareField(String, String, String)
     */
    boolean isTransactionalField(String classPath, String fieldName)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError;

    /**
     * Tests if a field is persistent.
     * <P>
     * The following holds:
     *   isPersistentField(classPath, fieldName)
     *       ==> isManagedField(classPath, fieldName)
     *           && !isTransactionalField(classPath, fieldName)
     * <P>
     * This method requires the field having been declared by declareField().
     * @param classPath the non-null JVM-qualified name of the class
     * @param fieldName the non-null name of the field
     * @return <code>true</code> if this field is persistent
     * @see #isManagedField(String, String)
     * @see #declareField(String, String, String)
     */
    boolean isPersistentField(String classPath, String fieldName)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError;

    /**
     * Tests if a field is a property.
     * <P>
     * The following holds:
     *   isProperty(classPath, fieldName)
     *       ==> isManagedField(classPath, fieldName)
     * <P>
     * This method requires the field having been declared by declareField().
     * @param classPath the non-null JVM-qualified name of the class
     * @param fieldName the non-null name of the field
     * @return <code>true</code> if this field is a property
     * @see #isManagedField(String, String)
     * @see #declareField(String, String, String)
     */
    boolean isProperty(String classPath, String fieldName)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError;

    /**
     * Tests if a field has key field access annotation.
     * <P>
     * The following holds:
     *   isKeyField(classPath, fieldName)
     *       ==> isPersistentField(classPath, fieldName)
     *           && !isDefaultFetchGroupField(classPath, fieldName)
     * <P>
     * This method requires the field having been declared by declareField().
     * @param classPath the non-null JVM-qualified name of the class
     * @param fieldName the non-null name of the field
     * @return <code>true</code> if this field has key field access annotation
     * @see #isPersistentField(String, String)
     * @see #declareField(String, String, String)
     */
    boolean isKeyField(String classPath, String fieldName)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError;

    /**
     * Tests if a field has default-fetch-group access annotation.
     * <P>
     * The following holds:
     *   isDefaultFetchGroupField(classPath, fieldName)
     *       ==> isPersistentField(classPath, fieldName)
     *           && !isKeyField(classPath, fieldName)
     * <P>
     * This method requires the field having been declared by declareField().
     * @param classPath the non-null JVM-qualified name of the class
     * @param fieldName the non-null name of the field
     * @return <code>true</code> if this field has default-fetch-group access annotation
     * @see #isPersistentField(String, String)
     * @see #declareField(String, String, String)
     */
    boolean isDefaultFetchGroupField(String classPath, String fieldName)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError;

    /**
     * Returns the index of a declared, managed field.
     * <P>
     * The following holds:
     *   int i = getFieldFlags(classPath, fieldName);
     *   i > 0  ==>  getManagedFields(classPath)[i] == fieldName
     * <P>
     * This method requires the field having been declared by declareField().
     * @param classPath the non-null JVM-qualified name of the class
     * @param fieldName the non-null name of the field
     * @return the non-negative, unique field index or -1 if the field
     *         is non-managed
     * @see #getManagedFields(String)
     * @see #declareField(String, String, String)
     */
    int getFieldNumber(String classPath, String fieldName)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError;

    /**
     * Returns the access annotation flags for a declared, managed field.
     * <P>
     * The following holds for the field flags:
     *   int f = getFieldFlags(classPath, fieldName);
     *
     *   !isManagedField(classPath, fieldName)
     *       ==> (f & CHECK_READ == 0) && (f & MEDIATE_READ == 0) &&
     *           (f & CHECK_WRITE == 0) && (f & MEDIATE_WRITE == 0)
     *
     *   isTransactionalField(classPath, fieldName)
     *       ==> (f & CHECK_READ == 0) && (f & MEDIATE_READ == 0) &&
     *           (f & CHECK_WRITE != 0) && (f & MEDIATE_WRITE == 0)
     *
     *   isKeyField(classPath, fieldName)
     *       ==> (f & CHECK_READ == 0) && (f & MEDIATE_READ == 0) &&
     *           (f & CHECK_WRITE == 0) && (f & MEDIATE_WRITE != 0)
     *
     *   isDefaultFetchGroupField(classPath, fieldName)
     *       ==> (f & CHECK_READ != 0) && (f & MEDIATE_READ != 0) &&
     *           (f & CHECK_WRITE == 0) && (f & MEDIATE_WRITE == 0)
     *
     *   isPersistentField(classPath, fieldName)
     *   && isKeyField(classPath, fieldName)
     *   && isDefaultFetchGroupField(classPath, fieldName)
     *       ==> (f & CHECK_READ == 0) && (f & MEDIATE_READ == 0) &&
     *           (f & CHECK_WRITE != 0) && (f & MEDIATE_WRITE != 0)
     * <P>
     * This method requires the field having been declared by declareField().
     * @param classPath the non-null JVM-qualified name of the class
     * @param fieldName the non-null name of the field
     * @return the field flags for this field
     * @see #declareField(String, String, String)
     */
    int getFieldFlags(String classPath, String fieldName)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError;

    // ----------------------------------------------------------------------
    // Convenience Methods
    // ----------------------------------------------------------------------

    /**
     * Tests if a class is the least-derived, persistence-capable class.
     * <P>
     * The following holds:
     *   isPersistenceCapableRootClass(classPath)
     *     <==> isPersistenceCapableClass(classPath)
     *          && getPersistenceCapableSuperClass(classPath) == null
     * @param classPath the non-null JVM-qualified name of the class
     * @return <code>true</code> if this class is least-derived and
     *         persistence-capable
     */
    boolean isPersistenceCapableRootClass(String classPath)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError;

    /**
     * Returns the name of the least-derived, persistence-capable class of
     * a class.
     * <P>
     * The following holds:
     *   (String s = getPersistenceCapableRootClass(classPath)) != null
     *       ==> isPersistenceCapableClass(classPath)
     *           && getPersistenceCapableSuperClass(classPath) == null
     * @param classPath the non-null JVM-qualified name of the class
     * @return the name of the least-derived persistence-capable class that
     *         is equal to or a super class of the argument class; if the
     *         argument class is not persistence-capable, null is returned.
     */
    String getPersistenceCapableRootClass(String classPath)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError;

    /**
     * Returns the name of the key class of the next persistence-capable
     * superclass that defines one.
     * <P>
     * The following holds:
     *   (String s = getSuperKeyClass(classPath)) != null
     *       ==> !isPersistenceCapableClass(s)
     *           && isPersistenceCapableClass(classPath)
     *           && !isPersistenceCapableRootClass(classPath)
     * @param classPath the non-null JVM-qualified name of the class
     * @return the name of the key class or null if there is none
     * @see #getKeyClass(String)
     * @see #getPersistenceCapableSuperClass(String)
     */
    String getSuperKeyClass(String classPath)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError;

    /**
     * Returns an array of field names of all key fields of a class.
     * <P>
     * This method requires all fields having been declared by declareField().
     * @param classPath the non-null JVM-qualified name of the class
     * @return an array of all declared key fields of a class
     * @see #declareField(String, String, String)
     */
    String[] getKeyFields(String classPath)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError;

    /**
     * Returns the indexes for some declared, managed fields.
     * <P>
     * This method requires all fields having been declared by declareField().
     * @param classPath the non-null JVM-qualified name of the class
     * @param fieldNames the non-null array of names of the declared fields
     * @return the non-negative, unique field indices
     * @see #declareField(String, String, String)
     */
    int[] getFieldNumber(String classPath, String[] fieldNames)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError;

    /**
     * Returns the access flags for some declared, managed fields.
     * <P>
     * This method requires all fields having been declared by declareField().
     * @param classPath the non-null JVM-qualified name of the class
     * @param fieldNames the non-null array of names of the declared fields
     * @return the access flags for the fields
     * @see #declareField(String, String, String)
     */
    int[] getFieldFlags(String classPath, String[] fieldNames)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError;
}
