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

import java.util.Collection;
import java.util.Iterator;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import com.sun.org.apache.jdo.impl.enhancer.meta.EnhancerMetaData;
import com.sun.org.apache.jdo.impl.enhancer.util.Support;

import com.sun.org.apache.jdo.enhancer.classfile.ClassInfo;
import com.sun.org.apache.jdo.enhancer.classfile.FieldInfo;
import com.sun.org.apache.jdo.enhancer.classfile.MethodInfo;

import com.sun.org.apache.jdo.impl.enhancer.jdo.ClassFileAnalyzer;


/**
 * Analyzes a class for enhancement.
 */
final class Analyzer
    extends Support
    implements ClassFileAnalyzer, JDOConstants, EnhancerConstants
{
    /**
     * The class is not to be modified by the enahncer.
     */
    static public final int CC_Unenhancable = -2;

    /**
     * The class is detected to be enhanced already and is not to be modifed.
     */
    static public final int CC_PreviouslyEnhanced = -1;

    /**
     * The enhancement status of the class hasn't been determined yet.
     */
    static public final int CC_PersistenceUnknown = 0;

    /**
     * The class is to be enhanced for persistence-awareness.
     */
    static public final int CC_PersistenceAware = 1;

    /**
     * The class is to be enhanced for specific persistence-capability
     * (class does extend another persistence-capable class).
     */
    static public final int CC_PersistenceCapable = 2;

    /**
     * The class is to be enhanced for generic and specific
     * persistence-capability (class does not extend another
     * persistence-capable class).
     */
    static public final int CC_PersistenceCapableRoot = 3;

    /**
     * The names of the jdo fields of persistene-capable classes.
     */
    static private final Set<String> jdoFieldNames = new HashSet<String>();
    static 
    {
        jdoFieldNames.add(JDO_PC_jdoStateManager_Name);
        jdoFieldNames.add(JDO_PC_jdoFlags_Name);
        jdoFieldNames.add(JDO_PC_jdoInheritedFieldCount_Name);
        jdoFieldNames.add(JDO_PC_jdoFieldNames_Name);
        jdoFieldNames.add(JDO_PC_jdoFieldTypes_Name);
        jdoFieldNames.add(JDO_PC_jdoFieldFlags_Name);
        jdoFieldNames.add(JDO_PC_jdoPersistenceCapableSuperclass_Name);
    }

    /**
     * The classfile's enhancement controller.
     */
    private final Controller control;

    /**
     * The classfile to be enhanced.
     */
    private final ClassInfo classInfo;

    /**
     * The class name in VM form.
     */
    private final String className;

    /**
     * The class name in user ('.' delimited) form.
     */
    private final String userClassName;

    /**
     * Repository for the enhancement options.
     */
    private final Environment env;

    /**
     * Repository for JDO meta-data on classes.
     */
    private final EnhancerMetaData meta;

    /**
     * What type of class is this with respect to persistence.
     */
    private int persistenceType = CC_PersistenceUnknown;

    /**
     * The name of the persistence-capable superclass if defined.
     */
    private String pcSuperClassName;

    /**
     * The name of the persistence-capable rootclass if defined.
     */
    private String pcRootClassName;

    /**
     * The name of this class or the next persistence-capable superclass
     * that owns a key class, or the PC rootclass if none defines a key class.
     */
    private String pcKeyOwnerClassName;

    /**
     * The name next persistence-capable superclass that owns a key class,
     * or the PC rootclass if none defines a key class.
     */
    private String pcSuperKeyOwnerClassName;

    /**
     * The name of the key class if defined.
     */
    private String keyClassName;

    /**
     * The name of the key class of the next persistence-capable superclass
     * that defines one.
     */
    private String superKeyClassName;

    /**
     * The number of key fields.
     */
    private int keyFieldCount;

    /**
     * The indexes of all key fields.
     */
    private int[] keyFieldIndexes;

    /**
     * The number of managed fields.
     */
    private int managedFieldCount;

    /**
     * The number of annotated fields.
     */
    private int annotatedFieldCount;

    /**
     * The names of all annotated fields sorted by relative field index.
     */
    private String[] annotatedFieldNames;

    /**
     * The type names of all annotated fields sorted by relative field index.
     */
    private String[] annotatedFieldSigs;

    /**
     * The java access modifiers of all annotated fields sorted by relative
     * field index.
     */
    private int[] annotatedFieldMods;

    /**
     * The jdo flags of all annotated fields sorted by relative field index.
     */
    private int[] annotatedFieldFlags;

    /**
     * The map of found JDO fields.
     */
    private final Map<String, FieldInfo> jdoLikeFields
    	= new HashMap<String, FieldInfo>(20);

    /**
     * The map of found JDO methods
     */
    private final Map<String, MethodInfo> jdoLikeMethods
    		= new HashMap<String, MethodInfo>(50);

    /**
     * The map of found JDO methods
     */
    private final Map<String, MethodInfo> annotatableMethods
			= new HashMap<String, MethodInfo>(100);

    /**
     * True if a jdo member has been seen in this class.
     */
    private boolean hasImplementsPC = false;
    private boolean hasGenericJDOFields = false;
    private boolean hasGenericJDOMethods = false;
    private boolean hasGenericJDOMembers = false;
    private boolean hasSpecificJDOFields = false;
    private boolean hasSpecificJDOMethods = false;
    private boolean hasSpecificJDOMembers = false;
    private boolean hasCallbackJDOMethods = false;
    private boolean hasJDOMembers = false;

    /**
     * True if the class has a default (no-argument) constructor.
     */
    private boolean hasDefaultConstructor = false;

    //^olsen: performance opt.: make these fields of type ClassMethod
    
    /**
     * True if the class has a static initializer block.
     */
    private boolean hasStaticInitializer = false;

    /**
     * True if the class has a clone() method.
     */
    private boolean hasCloneMethod = false;

    /**
     * True if the class has a writeObject(java.io.ObjectOutputStream) method.
     */
    private boolean hasWriteObjectMethod = false;

    /**
     * True if the class has a writeReplace() method.
     */
    private boolean hasWriteReplaceMethod = false;

    /**
     * True if the class has a readObject(java.io.ObjectInputStream) method.
     */
    private boolean hasReadObjectMethod = false;

    private boolean propertyBasedPersistence = true;
    // ----------------------------------------------------------------------
    
    /**
     * Constructor
     */
    public Analyzer(Controller control,
                    Environment env)
    {
        affirm(control != null);
        affirm(env != null);

        this.control = control;
        this.classInfo = control.getClassInfo();
        this.className = classInfo.getName();
        this.userClassName = classInfo.toJavaName();

        this.env = env;
        this.meta = env.getEnhancerMetaData();

        affirm(classInfo != null);
        affirm(className != null);
        affirm(userClassName != null);
        affirm(meta != null);
    }

    /**
     * Returns the class file which we are operating on.
     */
    public ClassInfo getClassFile()
    {
        return classInfo;
    }

    /**
     * Return the persistence type for this class
     */
    public int getPersistenceType()
    {
        return persistenceType;
    }

    /**
     * Returns true if the class has been analyzed already.
     */
    public boolean isAnalyzed()
    {
        return (persistenceType != CC_PersistenceUnknown);
    }

    /**
     * Returns true if the class is one which should be a candidate for
     * annotation.
     */
    public boolean isAnnotateable()
    {
        return (persistenceType >= CC_PersistenceUnknown);
    }

    /**
     * Returns true if the class is to be enhanced for persistence-capability.
     */
    public boolean isAugmentable()
    {
        return (persistenceType >= CC_PersistenceCapable);
    }

    /**
     * Returns true if the class is to be enhanced as least-derived,
     * persistence-capable class.
     */
    public boolean isAugmentableAsRoot()
    {
        return (persistenceType >= CC_PersistenceCapableRoot);
    }

    /**
     * Returns the methods that are candidates for annotation.
     */
    public Collection getAnnotatableMethods()
    {
        return annotatableMethods.values();
    }

    /**
     * Returns the name of the persistence-capable superclass if defined.
     */
    public String getPCSuperClassName()
    {
        return pcSuperClassName;
    }

    /**
     * Returns the name of the persistence-capable rootclass if defined.
     */
    public String getPCRootClassName()
    {
        return pcRootClassName;
    }

    /**
     * Returns the name of this class or the next persistence-capable
     * superclass that owns a key class.
     */
    public String getPCKeyOwnerClassName()
    {
        return pcKeyOwnerClassName;
    }

    /**
     * Returns the name of this class or the next persistence-capable
     * that owns a key class.
     */
    public String getPCSuperKeyOwnerClassName()
    {
        return pcSuperKeyOwnerClassName;
    }

    /**
     * Returns the name of the key class if defined.
     */
    public String getKeyClassName()
    {
        return keyClassName;
    }

    /**
     * Returns the name of the key class of the next persistence-capable
     * superclass that defines one.
     */
    public String getSuperKeyClassName()
    {
        return superKeyClassName;
    }

    /**
     * Returns the number of key field.
     */
    public int getKeyFieldCount()
    {
        return keyFieldCount;
    }

    /**
     * Returns the names of the key fields.
     */
    public int[] getKeyFieldIndexes()
    {
        return keyFieldIndexes;
    }

    /**
     * Returns the number of managed field.
     */
    public int getManagedFieldCount()
    {
        return managedFieldCount;
    }

    /**
     * Returns the number of annotated field.
     */
    public int getAnnotatedFieldCount()
    {
        return annotatedFieldCount;
    }

    /**
     * Returns the names of the annotated fields.
     */
    public String[] getAnnotatedFieldNames()
    {
        return annotatedFieldNames;
    }

    /**
     * Returns the types names of the annotated fields.
     */
    public String[] getAnnotatedFieldSigs()
    {
        return annotatedFieldSigs;
    }

    /**
     * Returns the Java access modifiers of the annotated fields.
     */
    public int[] getAnnotatedFieldMods()
    {
        return annotatedFieldMods;
    }

    /**
     * Returns the JDO flags of the annotated fields.
     */
    public int[] getAnnotatedFieldFlags()
    {
        return annotatedFieldFlags;
    }

    /**
     * Returns true if the class has a default (no-argument) constructor.
     */
    public boolean hasDefaultConstructor()
    {
        return hasDefaultConstructor;
    }

    /**
     * Returns true if the class has a static initializer block.
     */
    public boolean hasStaticInitializer()
    {
        return hasStaticInitializer;
    }

    /**
     * Returns true if the class has a clone() method.
     */
    public boolean hasCloneMethod()
    {
        return hasCloneMethod;
    }

    /**
     * Returns true if the class has a writeObject() method.
     */
    public boolean hasWriteObjectMethod()
    {
        return hasWriteObjectMethod;
    }

    /**
     * Returns true if the class has a writeReplace() method.
     */
    public boolean hasWriteReplaceMethod()
    {
        return hasWriteReplaceMethod;
    }

    /**
     * Returns true if the class has a readObject() method.
     */
    public boolean hasReadObjectMethod()
    {
        return hasReadObjectMethod;
    }

    /**
     * Returns true if the class already provides the JDO augmentation.
     */
    public boolean hasJDOAugmentation()
    {
        return hasJDOMembers;
    }

    public boolean isPropertyBasedPersistence() {
        return propertyBasedPersistence;
    }
    // ----------------------------------------------------------------------
    
    /**
     * Analyzes the class for existing augmentation.
     */
    public void scan()
    {
        env.message("scanning class " + userClassName);

        // skip previously enhanced files
        checkForEnhancedAttribute();
        if (!isAnnotateable()) {
            return;
        }

        // skip unenhancable files
        initPersistenceType();
        if (!isAnnotateable()) {
            return;
        }
        affirm(persistenceType > CC_Unenhancable);
        
        scanFields();
        scanMethods();

        if (isAugmentable()) {
            checkPCFeasibility();
            checkSpecificAugmentation();
            checkCallbackAugmentation();

            if (isAugmentableAsRoot()) {
                checkGenericAugmentation();
            }
        }
    }

    /*
    public boolean isPropertyBasedAccess(String ownerClassName, String fieldName) {
        return meta.isProperty(ownerClassName, fieldName);
    }
    */
    
    /**
     * Scans the attributes of a ClassFile
     */
    private void checkForEnhancedAttribute()
    {
		/* TBD
        for (Enumeration e = classFile.attributes().elements();
             e.hasMoreElements();) {
            final ClassAttribute attr = (ClassAttribute)e.nextElement();
            final String attrName = attr.attrName().asString();
            if (SUNJDO_PC_EnhancedAttribute.equals(attrName)) {
                persistenceType = CC_PreviouslyEnhanced;

                // At some point we may want to consider stripping old
                // annotations and re-annotating, but not yet
                env.message("ignoring previously enhanced class "
                            + userClassName);
                return;
            }
        }
        */
    }

    // ----------------------------------------------------------------------
    
    /**
     * Sets the persistence type of a class according to JDO metadata.
     */
    private void initPersistenceType()
    {
        affirm(persistenceType == CC_PersistenceUnknown);

        // check if class is known not to be changed
        final EnhancerMetaData meta = env.getEnhancerMetaData();
        if (meta.isKnownUnenhancableClass(className)) {
            persistenceType = CC_Unenhancable;			
            return;
        }

        // check if class is persistence-capable
        if (meta.isPersistenceCapableClass(className)) {
            pcSuperClassName
                = meta.getPersistenceCapableSuperClass(className);
            pcRootClassName
                = meta.getPersistenceCapableRootClass(className);			
            affirm(pcSuperClassName == null || pcRootClassName != null);

            persistenceType
                = (pcSuperClassName == null
                   ? CC_PersistenceCapableRoot
                   : CC_PersistenceCapable);

            //^olsen: assert consistency between Java and JDO metadata
            affirm(!classInfo.isInterface());
            //affirm(!classFile.isInnerClass());

            //^olsen: assert consistency between Java and JDO metadata
            // disallow enhancing classes not derived from java.lang.Object
            final String superClassName = classInfo.getSuperClassName();
            affirm(superClassName != null);

            // non-pc-root classes must not derive from java.lang.Object
            affirm(pcSuperClassName == null
                   || !superClassName.equals("java/lang/Object"));

            // define the PC key owner class
            pcKeyOwnerClassName = className;
            while (meta.getKeyClass(pcKeyOwnerClassName) == null) {
                final String pcSuperClassName
                    = meta.getPersistenceCapableSuperClass(pcKeyOwnerClassName);
                if (pcSuperClassName == null)
                    break;
                pcKeyOwnerClassName = pcSuperClassName;
            }
            affirm(pcKeyOwnerClassName != null);

            // define the PC super key owner class
            pcSuperKeyOwnerClassName = pcSuperClassName;
            if (pcSuperKeyOwnerClassName != null) {
                while (meta.getKeyClass(pcSuperKeyOwnerClassName) == null) {
                    final String pcSuperClassName
                        = meta.getPersistenceCapableSuperClass(
                            pcSuperKeyOwnerClassName);
                    if (pcSuperClassName == null)
                        break;
                    pcSuperKeyOwnerClassName = pcSuperClassName;
                }
                affirm(pcKeyOwnerClassName != null);
            }

            keyClassName
                = meta.getKeyClass(className);
            superKeyClassName
                = meta.getSuperKeyClass(className);
            affirm(superKeyClassName == null || pcSuperClassName != null);
        }
    }

    /**
     * Scans the fields.
     */
    private void scanFields()
    {
        // all fields for which accessor/mutator needs to be generated
        final Map<String, FieldInfo> annotatedFieldMap
        	= new HashMap<String, FieldInfo>();

        propertyBasedPersistence = true;
        if (isAugmentable()) {
            // loop over class fields to declare them to the model
            for (FieldInfo field : classInfo.fields()) {				
                final String name = field.getName();
                final String sig = field.getDescriptor();

                // skip jdo fields
                if (jdoFieldNames.contains(name)) {
                    continue;
                }

                // skip static fields
                if (field.isStatic()) {
                    continue;
                }

                // skip known non-managed fields
                if (meta.isKnownNonManagedField(className, name, sig)) {
                    continue;
                }

                // remember field requiring accessor/mutator
                Object obj = annotatedFieldMap.put(name, field);
				
                affirm(obj == null,
                   ("Error in classfile: repeated declaration of field: "
                    + userClassName + "." + name));

                // skip final, transient fields
                if (field.isFinal() || field.isTransient()) {
                    continue;
                }

                if (false) {
                    System.out.println("Analyzer.scanFields(): declaring FIELD"
                                       + className + "." + name + " : " + sig);
                }
                //meta.declareField(className, name, sig);
                if (! meta.isProperty(className, name)) {
                    propertyBasedPersistence = false;
                }
            }
            
            if (! propertyBasedPersistence) {
                System.out.println("WARNING: Analyzer WILL NOT SCAN METHODS...");
            }
            if (propertyBasedPersistence) {
                for (MethodInfo method : classInfo.methods()) {                
                    final String methodName = method.getName();
                    if (! methodName.startsWith("get")) {
                        continue;
                    }
                    final String name = extractProperty(method.getName());
                    System.out.println("Analyzer.scanFields(): Scanning property: " + name);
                    final String sig = method.getReturnTypeDescriptor();
    
                    com.sun.org.apache.jdo.impl.enhancer.classfile.asm.FieldRef field
                        = new com.sun.org.apache.jdo.impl.enhancer.classfile.asm.FieldRef(
                                method.getAccessCode(), className, name, sig);
                    // skip jdo fields
                    if (jdoFieldNames.contains(name)) {
                        continue;
                    }
    
                    // skip static fields
                    if (field.isStatic()) {
                        continue;
                    }
    
                    // skip known non-managed fields
                    if (meta.isKnownNonManagedField(className, name, sig)) {
                        continue;
                    }
    
                    // remember field requiring accessor/mutator
                    Object obj = annotatedFieldMap.put(name, field);
                    
                    /*
                    affirm(obj == null,
                       ("Error in classfile: repeated declaration of field: "
                        + userClassName + "." + name));
                    */
                    
                    // skip final, transient fields
                    //if (field.isFinal() || field.isTransient()) {
                        //continue;
                    //}
    
                    if (true) {
                        System.out.println("Analyzer.scanFields(): declaring PROPERTY "
                                           + className + "." + name + " : " + sig);
                    }
                    //meta.declareField(className, name, sig);
                }
            }
        }

        
        // nr of fields needing accessor/mutator methods
        annotatedFieldCount = annotatedFieldMap.size();
        
        // get managed field names from meta data
        final String[] managedFieldNames = meta.getManagedFields(className);
        affirm(managedFieldNames != null);
        managedFieldCount = managedFieldNames.length;
        final Set managedFieldNamesSet
            = new HashSet(Arrays.asList(managedFieldNames));

        affirm(managedFieldNamesSet.size() == managedFieldCount,
               "JDO metadata: returned duplicate managed fields.");
				
        affirm(managedFieldCount <= annotatedFieldCount,
               "JDO metadata: managed fields exceed annotated fields.");

        // data structures for key fields
        final String[] keyFieldNames = meta.getKeyFields(className);
        affirm(keyFieldNames != null);
        keyFieldCount = keyFieldNames.length;
        affirm(keyFieldCount == 0 || keyClassName != null,
               "JDO metadata: returned key fields but no key class.");
        final Set keyFieldNamesSet
            = new HashSet(Arrays.asList(keyFieldNames));
        affirm(keyFieldNamesSet.size() == keyFieldCount,
               "JDO metadata: returned duplicate key fields.");
        affirm(keyFieldCount <= managedFieldCount,
               "JDO metadata: key fields exceed managed fields.");

        // loop over class fields to compute 'jdo*' and key/managed fields
        for (FieldInfo field : classInfo.fields()) {
            final String name = field.getName();
            final String sig = field.getDescriptor();
            final String userFieldName = userClassName + "." + name;
            
            if (false) {
                System.out.println("Analyzer.scanFields(): scanning "
                                   + className + "." + name + " : " + sig);
            }

            // map 'jdo*' field names to class fields
            if (name.startsWith("jdo")) {
                final Object f = jdoLikeFields.put(name, field);
                affirm(f == null);
            }

            // skip non-managed fields
            if (!managedFieldNamesSet.contains(name)) {
                affirm(!meta.isManagedField(className, name));

                // check for non-managed key field
                affirm(!keyFieldNamesSet.contains(name),
                       ("JDO metadata: reported the field " + userFieldName
                        + " to be non-managed but key."));
                continue;
            }
            affirm(meta.isManagedField(className, name));

            // check for managed static field
            affirm(!field.isStatic(),
                   ("JDO metadata: reported the field " + userFieldName
                    + " to be managed though it's static."));

            // check for managed final field
            affirm(!field.isFinal(),
                   ("JDO metadata: reported the field " + userFieldName
                    + " to be managed though it's final."));

            // allow for managed transient fields
        }
        
        // get the managed field flags ordered by relative index
        final int[] managedFieldFlags
            = meta.getFieldFlags(className, managedFieldNames);

        // compute the managed field types ordered by relative index
        // and key field indexes
        int j = 0;
        keyFieldIndexes = new int[keyFieldCount];
        final String[] managedFieldSigs = new String[managedFieldCount];
        final int[] managedFieldMods = new int[managedFieldCount];
        for (int i = 0; i < managedFieldCount; i++) {
            final String name = managedFieldNames[i];
            affirm(name != null);
            // assert consistency between Java and JDO metadata
            final FieldInfo field = (FieldInfo)annotatedFieldMap.get(name);
            affirm(field != null,
                   ("The managed field " + userClassName + "." + name +
                    " is not declared by the class."));
            affirm(!field.isStatic(),
                   ("The managed field " + userClassName + "." + name +
                    " is static."));
            affirm(!field.isFinal(),
                   ("The managed field " + userClassName + "." + name +
                    " is final."));

            // mark managed field as taken care of
            annotatedFieldMap.remove(name);

            // assign key field index
            if (keyFieldNamesSet.contains(name)) {
                affirm(meta.isKeyField(className, name));
                keyFieldIndexes[j++] = i;
            }
            
            // add field type and Java access modifers
            managedFieldSigs[i] = field.getDescriptor();
            managedFieldMods[i] = field.getAccessCode();

            // set the serializable bit if field is not (Java) transient
            // This code might be removed as soon as the metadata is able
            // to retrieve the info as part of meta.getFieldFlags.
            if (!field.isTransient()) {
                managedFieldFlags[i] |= EnhancerMetaData.SERIALIZABLE;
            }
            
            if (false) {
                System.out.println("managed field: "
                                   + className + "." + name + " : {");
                System.out.println("    sigs = " + managedFieldSigs[i]);
                System.out.println("    mods = "
                                   + Integer.toHexString(managedFieldMods[i]));
                System.out.println("    flags = "
                                   + Integer.toHexString(managedFieldFlags[i]));
            } 
        }
        
        // post conditions of managed/key field processing
        affirm(keyFieldIndexes.length == keyFieldCount);
        affirm(keyFieldCount <= managedFieldCount);
        affirm(managedFieldNames.length == managedFieldCount);
        affirm(managedFieldSigs.length == managedFieldCount);
        affirm(managedFieldMods.length == managedFieldCount);
        affirm(managedFieldFlags.length == managedFieldCount);
        affirm(managedFieldCount <= annotatedFieldCount);
        
        // assign the annotated field arrays
        if (managedFieldCount == annotatedFieldCount) {
            // return if the annotated fields are equal to the managed ones
            annotatedFieldNames = managedFieldNames;
            annotatedFieldSigs = managedFieldSigs;
            annotatedFieldMods = managedFieldMods;
            annotatedFieldFlags = managedFieldFlags;
        } else {
            // fill the annotated field arrays with the managed ones
            annotatedFieldNames = new String[annotatedFieldCount];
            annotatedFieldSigs = new String[annotatedFieldCount];
            annotatedFieldMods = new int[annotatedFieldCount];
            annotatedFieldFlags = new int[annotatedFieldCount];
            int i = managedFieldCount;
            System.arraycopy(managedFieldNames, 0, annotatedFieldNames, 0, i);
            System.arraycopy(managedFieldSigs, 0, annotatedFieldSigs, 0, i);
            System.arraycopy(managedFieldMods, 0, annotatedFieldMods, 0, i);
            System.arraycopy(managedFieldFlags, 0, annotatedFieldFlags, 0, i);

            // append the annotated, non-managed fields
            for (Iterator k = annotatedFieldMap.entrySet().iterator();
                 k.hasNext();) {
                final Map.Entry entry = (Map.Entry)k.next();
                final String name = (String)entry.getKey();
                final FieldInfo field = (FieldInfo)entry.getValue();
                affirm(name.equals(field.getName()));

                affirm(!field.isStatic(),
                       ("The managed field " + userClassName + "." + name +
                        " is static."));

                // add field type and Java access modifers
                annotatedFieldNames[i] = name;
                annotatedFieldSigs[i] = field.getDescriptor();
                annotatedFieldMods[i] = field.getAccessCode();
                annotatedFieldFlags[i] = 0x0; // direct read/write access
                i++;
            }
            affirm(i == annotatedFieldCount);
        }
        
        // post conditions
        affirm(keyFieldIndexes.length == keyFieldCount);
        affirm(keyFieldCount <= managedFieldCount);
        affirm(annotatedFieldNames.length == annotatedFieldCount);
        affirm(annotatedFieldSigs.length == annotatedFieldCount);
        affirm(annotatedFieldMods.length == annotatedFieldCount);
        affirm(annotatedFieldFlags.length == annotatedFieldCount);
        affirm(managedFieldCount <= annotatedFieldCount);
    }

    /**
     * Scans the methods of a ClassFile.
     */
    private void scanMethods()
    {
        // check methods
        for (MethodInfo method : classInfo.methods()) {
            final String name = method.getName();
            final String sig = method.getDescriptor();
            affirm(name != null);
            affirm(sig != null);

            final String key = methodKey(name, sig);
            affirm(key != null);

            // for non-abstract, non-native methods, map names to class methods
            if (!method.isAbstract() && !method.isNative()) {
                final Object m = annotatableMethods.put(key, method);
                affirm(m == null);
            }

            // for 'jdo*' like methods, map names to class methods
            if (name.startsWith("jdo")) {
                final Object m = jdoLikeMethods.put(key, method);
                affirm(m == null);
                continue;
            }

            // check for a default constructor by name and signature
            if (name.equals(NameHelper.constructorName())
                && sig.equals(NameHelper.constructorSig())) {
                hasDefaultConstructor = true;
                continue;
            }
            
            // check for a static initializer block by name and signature
            if (name.equals(JAVA_clinit_Name)
                && sig.equals(JAVA_clinit_Sig)) {
                hasStaticInitializer = true;
                continue;
            }

            // check for method clone() by name and signature
            if (name.equals(JAVA_Object_clone_Name)
                && sig.equals(JAVA_Object_clone_Sig)) {
                hasCloneMethod = true;
                continue;
            }

            // check for method writeObject() by name and signature
            if (name.equals(JAVA_Object_writeObject_Name)
                && sig.equals(JAVA_Object_writeObject_Sig)) {
                hasWriteObjectMethod = true;
                continue;
            }

            // check for method writeReplace() by name and signature
            if (name.equals(JAVA_Object_writeReplace_Name)
                && sig.equals(JAVA_Object_writeReplace_Sig)) {
                hasWriteReplaceMethod = true;
                continue;
            }

            // check for method readObject() by name and signature
            if (name.equals(JAVA_Object_readObject_Name)
                && sig.equals(JAVA_Object_readObject_Sig)) {
                hasReadObjectMethod = true;

                // remove readObject() method from annotation candidates
                Object m = annotatableMethods.remove(key);
                affirm(m != null);
                continue;
            }
        }
        
        // post conditions
        affirm(keyFieldIndexes.length == keyFieldCount);
        affirm(keyFieldCount <= managedFieldCount);
        affirm(annotatedFieldNames.length == annotatedFieldCount);
        affirm(annotatedFieldSigs.length == annotatedFieldCount);
        affirm(annotatedFieldMods.length == annotatedFieldCount);
        affirm(annotatedFieldFlags.length == annotatedFieldCount);
        affirm(managedFieldCount <= annotatedFieldCount);
		
        // check for a default constructor by name and signature
        if (hasDefaultConstructor) {
            env.message(getI18N("enhancer.class_has_default_constructor"));
        } else {
            env.message(getI18N("enhancer.class_has_not_default_constructor"));
        }
            
        // check for a static initializer block by name and signature
        if (hasStaticInitializer) {
            env.message(getI18N("enhancer.class_has_static_initializer"));
        } else {
            env.message(getI18N("enhancer.class_has_not_static_initializer"));
        }

        // check for method clone() by name and signature
        if (hasCloneMethod) {
            env.message(getI18N("enhancer.class_has_clone_method"));
        } else {
            env.message(getI18N("enhancer.class_has_not_clone_method"));
        }

        // check for method writeObject() by name and signature
        if (hasWriteObjectMethod) {
            env.message(getI18N("enhancer.class_has_writeObject_method"));
        } else {
            env.message(getI18N("enhancer.class_has_not_writeObject_method"));
        }

        // check for method writeReplace() by name and signature
        if (hasWriteReplaceMethod) {
            env.message(getI18N("enhancer.class_has_writeReplace_method"));
        } else {
            env.message(getI18N("enhancer.class_has_not_writeReplace_method"));
        }

        // check for method readObject() by name and signature
        if (hasReadObjectMethod) {
            env.message(getI18N("enhancer.class_has_readObject_method"));
        } else {
            env.message(getI18N("enhancer.class_has_not_readObject_method"));
        }
    }

    private void checkGenericAugmentation()
    {
        scanForImplementsPC();
        scanForGenericJDOFields();
        scanForGenericJDOMethods();

        final boolean all
            = (hasImplementsPC && hasGenericJDOFields && hasGenericJDOMethods);
        //^olsen: check
        final boolean none
            = !(hasImplementsPC
                || hasGenericJDOFields || hasGenericJDOMethods);

        if (all ^ none) {
            hasGenericJDOMembers = hasImplementsPC;
            env.message(
                getI18N("enhancer.class_has_generic_jdo_members",
                        String.valueOf(hasGenericJDOMembers)));

            //^olsen: check for specific enhancement

            return;
        }

        final String key
            = "enhancer.class_has_inconsistently_declared_jdo_members";
        if (hasGenericJDOFields && !hasGenericJDOMethods) {
            env.error(
                getI18N(key,
                        userClassName,
                        "<generic jdo fields>",
                        "<generic jdo methods>"));
        } else if (!hasGenericJDOFields && hasGenericJDOMethods) {
            env.error(
                getI18N(key,
                        userClassName,
                        "<generic jdo methods>",
                        "<generic jdo fields>"));
        } else if (!hasGenericJDOFields && !hasGenericJDOMethods) {
            env.error(
                getI18N(key,
                        userClassName,
                        "<implements " + JDO_PersistenceCapable_Name + ">",
                        "<generic jdo members>"));
        } else {
            env.error(
                getI18N(key,
                        userClassName,
                        "<generic jdo members>",
                        "<implements " + JDO_PersistenceCapable_Name + ">"));
        }
    }
    
    private void checkSpecificAugmentation()
    {
        scanForSpecificJDOFields();
        scanForSpecificJDOMethods();

        final boolean all
            = (hasSpecificJDOFields && hasSpecificJDOMethods);
        //^olsen: check
        final boolean none
            = !(hasSpecificJDOFields || hasSpecificJDOMethods);

        if (all ^ none) {
            hasSpecificJDOMembers = hasSpecificJDOFields;
            env.message(
                getI18N("enhancer.class_has_specific_jdo_members",
                        String.valueOf(hasSpecificJDOMembers)));
            return;
        }

        final String key
            = "enhancer.class_has_inconsistently_declared_jdo_members";
        if (hasSpecificJDOFields && !hasSpecificJDOMethods) {
            env.error(
                getI18N(key,
                        userClassName,
                        "<specific jdo fields>",
                        "<specific jdo methods>"));
        } else {
            env.error(
                getI18N(key,
                        userClassName,
                        "<specific jdo methods>",
                        "<specific jdo fields>"));
        }
    }
    
    private void checkCallbackAugmentation()
    {
        scanForCallbackJDOMethods();
        env.message(
            getI18N("enhancer.class_has_callback_jdo_methods",
                    String.valueOf(hasCallbackJDOMethods)));
    }
    
    private void checkPCFeasibility()
    {
        if (!hasDefaultConstructor) {
            env.error(
                getI18N("enhancer.class_missing_default_constructor",
                        userClassName));
        }
    }
    
    /**
     * Scans the class for implementing the PC interface.
     */
    private void scanForImplementsPC()
    {
        hasImplementsPC = false;
        for (String interfaceName : classInfo.interfaces()) {
            if (interfaceName.equals(JDO_PersistenceCapable_Path)) {
                hasImplementsPC = true;
                break;
            }
        }
        env.message(
            getI18N("enhancer.class_implements_jdo_pc",
                    String.valueOf(hasImplementsPC)));
    }

    /**
     * Scans for JDO fields of generic augmentation.
     */
    private void scanForGenericJDOFields()
    {
        // performance shortcut
        if (jdoLikeFields.isEmpty()) {
            hasGenericJDOFields = false;
            env.message(
                getI18N("enhancer.class_has_generic_jdo_fields",
                        String.valueOf(hasGenericJDOFields)));
            return;
        }

        // sets of found/missing 'jdo*' members
        final Set found = new HashSet(10);
        final Set missing = new HashSet(10);

        scanJDOField(JDO_PC_jdoStateManager_Name,
                     JDO_PC_jdoStateManager_Sig,
                     JDO_PC_jdoStateManager_Mods,
                     found, missing);
        scanJDOField(JDO_PC_jdoFlags_Name,
                     JDO_PC_jdoFlags_Sig,
                     JDO_PC_jdoFlags_Mods,
                     found, missing);

        if (found.isEmpty() ^ missing.isEmpty()) {
            hasGenericJDOFields = missing.isEmpty();
            env.message(
                getI18N("enhancer.class_has_generic_jdo_fields",
                        String.valueOf(hasGenericJDOFields)));
            return;
        }

        reportInconsistentJDOMembers(found, missing);
    }

    /**
     * Scans for JDO methods of generic augmentation.
     */
    private void scanForGenericJDOMethods()
    {
        // performance shortcut
        if (jdoLikeMethods.isEmpty()) {
            hasGenericJDOMethods = false;
            env.message(
                getI18N("enhancer.class_has_generic_jdo_methods",
                        String.valueOf(hasGenericJDOMethods)));
            return;
        }

        // sets of found/missing 'jdo*' members
        final Set found = new HashSet(30);
        final Set missing = new HashSet(30);

        scanJDOMethod(JDO_PC_jdoReplaceStateManager_Name,
                      JDO_PC_jdoReplaceStateManager_Sig,
                      JDO_PC_jdoReplaceStateManager_Mods,
                      found, missing);
        scanJDOMethod(JDO_PC_jdoReplaceFlags_Name,
                      JDO_PC_jdoReplaceFlags_Sig,
                      JDO_PC_jdoReplaceFlags_Mods,
                      found, missing);
        scanJDOMethod(JDO_PC_jdoGetPersistenceManager_Name,
                      JDO_PC_jdoGetPersistenceManager_Sig,
                      JDO_PC_jdoGetPersistenceManager_Mods,
                      found, missing);
        scanJDOMethod(JDO_PC_jdoGetObjectId_Name,
                      JDO_PC_jdoGetObjectId_Sig,
                      JDO_PC_jdoGetObjectId_Mods,
                      found, missing);
        scanJDOMethod(JDO_PC_jdoGetTransactionalObjectId_Name,
                      JDO_PC_jdoGetTransactionalObjectId_Sig,
                      JDO_PC_jdoGetTransactionalObjectId_Mods,
                      found, missing);
        scanJDOMethod(JDO_PC_jdoIsPersistent_Name,
                      JDO_PC_jdoIsPersistent_Sig,
                      JDO_PC_jdoIsPersistent_Mods,
                      found, missing);
        scanJDOMethod(JDO_PC_jdoIsTransactional_Name,
                      JDO_PC_jdoIsTransactional_Sig,
                      JDO_PC_jdoIsTransactional_Mods,
                      found, missing);
        scanJDOMethod(JDO_PC_jdoIsNew_Name,
                      JDO_PC_jdoIsNew_Sig,
                      JDO_PC_jdoIsNew_Mods,
                      found, missing);
        scanJDOMethod(JDO_PC_jdoIsDeleted_Name,
                      JDO_PC_jdoIsDeleted_Sig,
                      JDO_PC_jdoIsDeleted_Mods,
                      found, missing);
        scanJDOMethod(JDO_PC_jdoIsDirty_Name,
                      JDO_PC_jdoIsDirty_Sig,
                      JDO_PC_jdoIsDirty_Mods,
                      found, missing);
        scanJDOMethod(JDO_PC_jdoMakeDirty_Name,
                      JDO_PC_jdoMakeDirty_Sig,
                      JDO_PC_jdoMakeDirty_Mods,
                      found, missing);
        scanJDOMethod(JDO_PC_jdoPreSerialize_Name,
                      JDO_PC_jdoPreSerialize_Sig,
                      JDO_PC_jdoPreSerialize_Mods,
                      found, missing);
        scanJDOMethod(JDO_PC_jdoReplaceFields_Name,
                      JDO_PC_jdoReplaceFields_Sig,
                      JDO_PC_jdoReplaceFields_Mods,
                      found, missing);
        scanJDOMethod(JDO_PC_jdoProvideFields_Name,
                      JDO_PC_jdoProvideFields_Sig,
                      JDO_PC_jdoProvideFields_Mods,
                      found, missing);

        if (found.isEmpty() ^ missing.isEmpty()) {
            hasGenericJDOMethods = missing.isEmpty();
            env.message(
                getI18N("enhancer.class_has_generic_jdo_methods",
                        String.valueOf(hasGenericJDOMethods)));
            return;
        }

        reportInconsistentJDOMembers(found, missing);
    }

    /**
     * Scans for JDO fields of specific augmentation.
     */
    private void scanForSpecificJDOFields()
    {
        // performance shortcut
        if (jdoLikeFields.isEmpty()) {
            hasSpecificJDOFields = false;
            env.message(
                getI18N("enhancer.class_has_specific_jdo_fields",
                        String.valueOf(hasSpecificJDOFields)));
            return;
        }

        // sets of found/missing 'jdo*' members
        final Set found = new HashSet(10);
        final Set missing = new HashSet(10);

        scanJDOField(JDO_PC_jdoInheritedFieldCount_Name,
                     JDO_PC_jdoInheritedFieldCount_Sig,
                     JDO_PC_jdoInheritedFieldCount_Mods,
                     found, missing);
        scanJDOField(JDO_PC_jdoFieldNames_Name,
                     JDO_PC_jdoFieldNames_Sig,
                     JDO_PC_jdoFieldNames_Mods,
                     found, missing);
        scanJDOField(JDO_PC_jdoFieldTypes_Name,
                     JDO_PC_jdoFieldTypes_Sig,
                     JDO_PC_jdoFieldTypes_Mods,
                     found, missing);
        scanJDOField(JDO_PC_jdoFieldFlags_Name,
                     JDO_PC_jdoFieldFlags_Sig,
                     JDO_PC_jdoFieldFlags_Mods,
                     found, missing);
        scanJDOField(JDO_PC_jdoPersistenceCapableSuperclass_Name,
                     JDO_PC_jdoPersistenceCapableSuperclass_Sig,
                     JDO_PC_jdoPersistenceCapableSuperclass_Mods,
                     found, missing);

        if (found.isEmpty() ^ missing.isEmpty()) {
            hasSpecificJDOFields = missing.isEmpty();
            env.message(
                getI18N("enhancer.class_has_specific_jdo_fields",
                        String.valueOf(hasSpecificJDOFields)));
            return;
        }

        reportInconsistentJDOMembers(found, missing);
    }

    /**
     * Scans for JDO methods of specific augmentation.
     */
    private void scanForSpecificJDOMethods()
    {
        // performance shortcut
        if (jdoLikeMethods.isEmpty()) {
            hasSpecificJDOMethods = false;
            env.message(
                getI18N("enhancer.class_has_specific_jdo_methods",
                        String.valueOf(hasSpecificJDOMethods)));
            return;
        }

        // sets of found/missing 'jdo*' members
        final Set found = new HashSet(30);
        final Set missing = new HashSet(30);

        scanJDOMethod(JDO_PC_jdoGetManagedFieldCount_Name,
                      JDO_PC_jdoGetManagedFieldCount_Sig,
                      JDO_PC_jdoGetManagedFieldCount_Mods,
                      found, missing);
        scanJDOMethod(JDO_PC_jdoNewInstance_Name,
                      JDO_PC_jdoNewInstance_Sig,
                      JDO_PC_jdoNewInstance_Mods,
                      found, missing);
        scanJDOMethod(JDO_PC_jdoNewInstance_Name,
                      JDO_PC_jdoNewInstance_Sig,
                      JDO_PC_jdoNewInstance_Mods,
                      found, missing);
        scanJDOMethod(JDO_PC_jdoNewObjectIdInstance_Name,
                      JDO_PC_jdoNewObjectIdInstance_Sig,
                      JDO_PC_jdoNewObjectIdInstance_Mods,
                      found, missing);
        scanJDOMethod(JDO_PC_jdoNewObjectIdInstance_Name,
                      JDO_PC_jdoNewObjectIdInstance_Sig,
                      JDO_PC_jdoNewObjectIdInstance_Mods,
                      found, missing);
        scanJDOMethod(JDO_PC_jdoCopyKeyFieldsToObjectId_Name,
                      JDO_PC_jdoCopyKeyFieldsToObjectId_Sig,
                      JDO_PC_jdoCopyKeyFieldsToObjectId_Mods,
                      found, missing);
        scanJDOMethod(JDO_PC_jdoCopyKeyFieldsToObjectId_OIFS_Name,
                      JDO_PC_jdoCopyKeyFieldsToObjectId_OIFS_Sig,
                      JDO_PC_jdoCopyKeyFieldsToObjectId_OIFS_Mods,
                      found, missing);
        scanJDOMethod(JDO_PC_jdoCopyKeyFieldsFromObjectId_OIFC_Name,
                      JDO_PC_jdoCopyKeyFieldsFromObjectId_OIFC_Sig,
                      JDO_PC_jdoCopyKeyFieldsFromObjectId_OIFC_Mods,
                      found, missing);
        scanJDOMethod(JDO_PC_jdoReplaceField_Name,
                      JDO_PC_jdoReplaceField_Sig,
                      JDO_PC_jdoReplaceField_Mods,
                      found, missing);
        scanJDOMethod(JDO_PC_jdoProvideField_Name,
                      JDO_PC_jdoProvideField_Sig,
                      JDO_PC_jdoProvideField_Mods,
                      found, missing);
        scanJDOMethod(JDO_PC_jdoCopyFields_Name,
                      JDO_PC_jdoCopyFields_Sig,
                      JDO_PC_jdoCopyFields_Mods,
                      found, missing);
        scanJDOMethod(JDO_PC_jdoCopyField_Name,
                      JDONameHelper.getJDO_PC_jdoCopyField_Sig(className),
                      JDO_PC_jdoCopyField_Mods,
                      found, missing);

        if (found.isEmpty() ^ missing.isEmpty()) {
            hasSpecificJDOMethods = missing.isEmpty();
            env.message(
                getI18N("enhancer.class_has_specific_jdo_methods",
                        String.valueOf(hasSpecificJDOMethods)));
            return;
        }

        reportInconsistentJDOMembers(found, missing);
    }

    /**
     * Scans for JDO methods of generic augmentation.
     */
    private void scanForCallbackJDOMethods()
    {
        // performance shortcut
        if (jdoLikeMethods.isEmpty()) {
            hasCallbackJDOMethods = false;
            env.message(
                getI18N("enhancer.class_has_callback_jdo_methods",
                        String.valueOf(hasCallbackJDOMethods)));
            return;
        }

        // sets of found/missing 'jdo*' members
        final Set found = new HashSet(30);
        final Set missing = new HashSet(30);
        final boolean annotatable = true;

        scanJDOMethod(JDO_IC_jdoPostLoad_Name,
                      JDO_IC_jdoPostLoad_Sig,
                      JDO_IC_jdoPostLoad_Mods,
                      found, missing, !annotatable);

        scanJDOMethod(JDO_IC_jdoPreStore_Name,
                      JDO_IC_jdoPreStore_Sig,
                      JDO_IC_jdoPreStore_Mods,
                      found, missing, annotatable);

        scanJDOMethod(JDO_IC_jdoPreClear_Name,
                      JDO_IC_jdoPreClear_Sig,
                      JDO_IC_jdoPreClear_Mods,
                      found, missing, !annotatable);

        scanJDOMethod(JDO_IC_jdoPreDelete_Name,
                      JDO_IC_jdoPreDelete_Sig,
                      JDO_IC_jdoPreDelete_Mods,
                      found, missing, annotatable);

        // no requirement to check for 'missing' methods
        if (!found.isEmpty()) {
            hasCallbackJDOMethods = true;
            env.message(
                getI18N("enhancer.class_has_callback_jdo_methods",
                        String.valueOf(hasCallbackJDOMethods)));
        }
    }

    /**
     * Verifies a JDO field signature.
     */
    private void scanJDOField(String fieldName,
                              String expectedSig,
                              int expectedMods,
                              Set found,
                              Set missing)
    {
        final FieldInfo field = (FieldInfo)jdoLikeFields.get(fieldName);
        if (field == null) {
            missing.add(fieldName);
            return;
        }
        found.add(fieldName);

        final String foundSig = field.getDescriptor();
        final int foundMods = field.getAccessCode();
        if (!expectedSig.equals(foundSig) || expectedMods != foundMods) {
            env.error(
                getI18N("enhancer.class_has_illegally_declared_jdo_member",
                        new Object[]{ userClassName,
                                      fieldName,
                                      expectedSig,
                                      foundSig,
                                      new Integer(expectedMods),
                                      new Integer(foundMods) }));
        }
    }

    /**
     * Verifies a JDO method signature.
     */
    private void scanJDOMethod(String methodName,
                               String expectedSig,
                               int expectedMods,
                               Set found,
                               Set missing)
    {
        scanJDOMethod(methodName, expectedSig, expectedMods,
                      found, missing, true);
    }

    /**
     * Verifies a JDO method signature.
     */
    private void scanJDOMethod(String methodName,
                               String expectedSig,
                               int expectedMods,
                               Set found,
                               Set missing,
                               boolean annotatable)
    {
        final String key = methodKey(methodName, expectedSig);
        final MethodInfo method = (MethodInfo)jdoLikeMethods.get(key);
        if (method == null) {
            missing.add(key);
            return;
        }
        found.add(key);

        final String foundSig = method.getDescriptor();
        final int foundMods = method.getAccessCode();
        if (!expectedSig.equals(foundSig) || expectedMods != foundMods) {
            env.error(
                getI18N("enhancer.class_has_illegally_declared_jdo_member",
                        new Object[]{ userClassName,
                                      methodName,
                                      expectedSig,
                                      foundSig,
                                      new Integer(expectedMods),
                                      new Integer(foundMods) }));
        }

        // remove jdo method from annotation candidates
        if (!annotatable) {
            Object m = annotatableMethods.remove(key);
            affirm(m != null);
        }
    }

    /**
     * Reports an error for some found/missing JDO fields or methods.
     */
    private void reportInconsistentJDOMembers(Set found,
                                              Set missing)
    {
        final Iterator fi = found.iterator();
        final StringBuffer f = new StringBuffer((String)fi.next());
        while (fi.hasNext()) {
            f.append(", " + fi.next());
        }

        final Iterator mi = found.iterator();
        final StringBuffer m = new StringBuffer((String)mi.next());
        while (mi.hasNext()) {
            m.append(", " + mi.next());
        }

        env.error(
            getI18N("enhancer.class_has_inconsistently_declared_jdo_members",
                    userClassName, f.toString(), m.toString()));
    }

    // ----------------------------------------------------------------------
    
    static private String methodKey(String name,
                                    String sig)
    {
        affirm(name != null);
        affirm(sig != null && sig.charAt(0) == '(' && sig.indexOf(')') > 0);
        final String parms = sig.substring(0, sig.indexOf(')') + 1);
        return (name + parms);
    }
    
    private static String extractProperty(String str) {
        String propertyName = str.substring(3);
        return initLowerCase(propertyName);
        
        
    }
    
    private static String initLowerCase(String str) {
        String firstLower = "" + Character.toLowerCase(str.charAt(0));
        if (str.length() == 1) {
            return firstLower;
        }
        
        return firstLower + str.substring(1);
    }
    
}
