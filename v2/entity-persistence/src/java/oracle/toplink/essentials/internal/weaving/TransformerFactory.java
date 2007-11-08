/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 2005, 2006, Oracle. All rights reserved.
 * 
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package oracle.toplink.essentials.internal.weaving;

// J2SE imports
import java.lang.reflect.*;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.*;

// ASM imports
import oracle.toplink.libraries.asm.Type;

// TopLink imports
import oracle.toplink.essentials.indirection.ValueHolderInterface;
import oracle.toplink.essentials.logging.SessionLog;
import oracle.toplink.essentials.mappings.*;
import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.sessions.Session;
import oracle.toplink.essentials.sessions.Project;
import oracle.toplink.essentials.internal.security.PrivilegedAccessHelper;
import oracle.toplink.essentials.internal.security.PrivilegedGetDeclaredField;
import oracle.toplink.essentials.internal.security.PrivilegedGetDeclaredMethod;
import oracle.toplink.essentials.internal.security.PrivilegedGetMethod;
import oracle.toplink.essentials.internal.security.PrivilegedGetField;

import javax.persistence.spi.ClassTransformer;

/**
 * INTERNAL:
 * This class creates a ClassFileTransformer that is used for dynamic bytecode
 * weaving. It is called by {@link oracle.toplink.essentials.internal.ejb.cmp3.EntityManagerSetupImpl#predeploy}
 * <p>
 * <i>Note:</i> The Session's Project is is scanned to ensure that weaving is
 * supported and is <b>modified</b> to suit (set the {@link ObjectChangePolicy}
 * for the Descriptor).
 * <p>
 *
 */
public class TransformerFactory {
   
    public static final String WEAVER_NULL_PROJECT =
        "weaver_null_project";
    public static final String WEAVER_DISABLE_BY_SYSPROP =
        "weaver_disable_by_system_property";
    public static final String WEAVER_ADDING_EMBEDDABLE = 
        "weaver_adding_embeddable_class";
    public static final String WEAVER_FOUND_FIELD_LOCK =    
        "weaver_found_field_lock";
    public static final String WEAVER_CLASS_NOT_IN_PROJECT =    
        "weaver_class_not_in_project";
    public static final String WEAVER_PROCESSING_CLASS =    
        "weaver_processing_class";
    
    public static ClassTransformer createTransformerAndModifyProject(Session session,
        Collection entityClasses, ClassLoader classLoader) {
        if (session == null) {
            throw new IllegalArgumentException("Weaver session cannot be null");
        }
        if (session.getProject() == null) {
            ((oracle.toplink.essentials.internal.sessions.AbstractSession)session).log(
                SessionLog.SEVERE, SessionLog.WEAVER, WEAVER_NULL_PROJECT, null);
            throw new IllegalArgumentException("Weaver session's project cannot be null");
        }
        TransformerFactory tf = new TransformerFactory(session, entityClasses, classLoader);
        tf.buildClassDetailsAndModifyProject();
        return tf.buildTopLinkWeaver();
    }
    
    protected Session session;
    protected Collection entityClasses;
    protected List embeddableClasses;
    protected Map classDetailsMap;
    protected ClassLoader classLoader;
    
    public TransformerFactory(Session session, Collection entityClasses, ClassLoader classLoader) {
        this.session = session;
        this.entityClasses = entityClasses;
        this.classLoader = classLoader;
        embeddableClasses = new ArrayList();
        classDetailsMap = new HashMap(); 
    }
    
    /**
     * INTERNAL:
     * Look higher in the hierarchy for the mappings listed in the unMappedAttribute list.
     * 
     * We assume that if a mapping exists, the attribute must either be mapped from the owninig
     * class or from a superclass.
     */
    public void addClassDetailsForMappedSuperClasses(Class clz, ClassDescriptor initialDescriptor, ClassDetails classDetails, Map classDetailsMap, List unMappedAttributes){
        // This class has inheritance to a mapped entity rather than a MappedSuperClass
        if (initialDescriptor.getInheritancePolicyOrNull() != null && initialDescriptor.getInheritancePolicyOrNull().getParentClass() != null){
            return;
        }
        if (unMappedAttributes.isEmpty()){
            return;
        }
        Class superClz = clz.getSuperclass();
        if (superClz == null || superClz == java.lang.Object.class){
            return;
        }
        
        boolean weaveValueHolders = canWeaveValueHolders(superClz, unMappedAttributes);
        List stillUnMappedMappings = null;
        ClassDetails superClassDetails = createClassDetails(superClz, weaveValueHolders);
        superClassDetails.setIsMappedSuperClass(true);
        if (!classDetailsMap.containsKey(superClassDetails.getClassName())){
            stillUnMappedMappings = storeAttributeMappings(superClz, superClassDetails, unMappedAttributes, weaveValueHolders);
            classDetailsMap.put(superClassDetails.getClassName() ,superClassDetails);
        }
       
        if (stillUnMappedMappings != null && !stillUnMappedMappings.isEmpty()){
            addClassDetailsForMappedSuperClasses(superClz, initialDescriptor, classDetails, classDetailsMap, stillUnMappedMappings);
        }
        
        
    }
    
    public ClassTransformer buildTopLinkWeaver() {
        return new TopLinkWeaver(session, classDetailsMap);
    }

    /**
     * Build a list ClassDetails instance that contains a ClassDetails for each class
     * in our entities list.
     */
    public void buildClassDetailsAndModifyProject() {
        if (entityClasses != null && entityClasses.size() > 0) {
            
            // scan thru list building details of persistent classes

            // do @Entity's next
            for (Iterator i = entityClasses.iterator(); i.hasNext();) {
                Class clz = (Class)i.next();
                
                // check to ensure that class is present in project
                ClassDescriptor descriptor = findDescriptor(session.getProject(), clz.getName());
                if (descriptor == null) {

                    log(SessionLog.FINER, WEAVER_CLASS_NOT_IN_PROJECT,
                        new Object[]{clz.getName()});
                } else {
                    log(SessionLog.FINER, WEAVER_PROCESSING_CLASS,
                        new Object[]{clz.getName()});

                    boolean weaveValueHolders = canWeaveValueHolders(clz, descriptor.getMappings());
                    
                    if (weaveValueHolders) {
                        ClassDetails classDetails = createClassDetails(clz, weaveValueHolders);                       
                        List unMappedAttributes = storeAttributeMappings(clz, classDetails, descriptor.getMappings(), weaveValueHolders);
                        classDetailsMap.put(classDetails.getClassName() ,classDetails);

                        if (!unMappedAttributes.isEmpty()){
                            addClassDetailsForMappedSuperClasses(clz, descriptor, classDetails, classDetailsMap, unMappedAttributes);
                        }
                        if (classDetails.getLazyOneToOneMappings() != null){
                            Iterator iterator = classDetails.getLazyOneToOneMappings().iterator();
                            while(iterator.hasNext()){
                                OneToOneMapping mapping = (OneToOneMapping)iterator.next();
                                mapping.setGetMethodName("_toplink_get" + mapping.getAttributeName() + "_vh");
                                mapping.setSetMethodName("_toplink_set" + mapping.getAttributeName() + "_vh");
                            }
                        }
                    }
                }
            }
            // hookup superClassDetails
            for (Iterator i = classDetailsMap.values().iterator(); i.hasNext();) {
                ClassDetails classDetails = (ClassDetails)i.next();
                ClassDetails superClassDetails =
                    (ClassDetails)classDetailsMap.get(
                        classDetails.getSuperClassName());
                if (superClassDetails != null) {
                    classDetails.setSuperClassDetails(superClassDetails);
                }
            }
        // combine lists: add entities to end of embeddables,
        // then clear entities and re-populate from embeddables.
        embeddableClasses.addAll(entityClasses);
        entityClasses.clear();
        entityClasses.addAll(embeddableClasses);
        }
    }
       
    protected boolean canWeaveValueHolders(Class clz, List mappings) {

        // we intend to change to fetch=LAZY 1:1 attributes to ValueHolders
        boolean weaveValueHolders = true; 
        boolean foundOTOM = false;
        for (Iterator j = mappings.iterator(); j.hasNext();) {
            DatabaseMapping dm = (DatabaseMapping)j.next();
            String attributeName = dm.getAttributeName();
            if (dm.isOneToOneMapping()) {
                OneToOneMapping otom = (OneToOneMapping)dm;
                Class typeClz = getAttributeTypeFromClass(clz, attributeName, dm, true);
                if (otom.getIndirectionPolicy().usesIndirection() &&
                    typeClz != null  && !typeClz.isAssignableFrom(
                    ValueHolderInterface.class)) {
                    foundOTOM = true;
                    weaveValueHolders = true;
                 }
             }
        }

        // did we actually <b>find</b> any attributes to change?
        return weaveValueHolders & foundOTOM;
    }

    private ClassDetails createClassDetails(Class clz, boolean weaveValueHolders){
        // compose className in JVM 'slash' format
        // instead of regular Java 'dotted' format
        String className = clz.getName().replace('.','/');
        String superClassName = clz.getSuperclass().getName().replace('.','/');
        ClassDetails classDetails = new ClassDetails();
        classDetails.setClassName(className);
        classDetails.setSuperClassName(superClassName);
        classDetails.weaveValueHolders(weaveValueHolders);
        return classDetails;
    }

    /**
     *  INTERNAL:
     *  Store a set of attribute mappings on the given ClassDetails taht correspont to the given class.
     *  Return the list of mappings that is not specifically found on the given class.  These attributes will 
     *  be found on MappedSuperclasses
     */
    protected List storeAttributeMappings(Class clz, ClassDetails classDetails, List mappings, boolean weaveValueHolders) {      
        List unMappedAttributes = new Vector();
        Map attributesMap = new HashMap();
        Map settersMap = new HashMap();
        Map gettersMap = new HashMap();
        List lazyMappings = new Vector();
        for (Iterator j = mappings.iterator(); j.hasNext();) {
            DatabaseMapping dm = (DatabaseMapping)j.next();
            String attribute = dm.getAttributeName();
            AttributeDetails attributeDetails = new AttributeDetails(attribute);
            Class typeClz = getAttributeTypeFromClass(clz, attribute, dm, false);
            if (typeClz == null){
                attributeDetails.setAttributeOnSuperClass(true);
                if (dm.isOneToOneMapping()){
                    unMappedAttributes.add(dm);
                }
            }
            if (dm.isCollectionMapping()) {
                attributeDetails.setCollectionMapping(true);
            } else if (dm.isOneToOneMapping()) {
                OneToOneMapping otom = (OneToOneMapping)dm;
                attributeDetails.referenceClass = otom.getReferenceClassName();
                attributeDetails.weaveVH(weaveValueHolders, otom);
                if (otom.getGetMethodName() != null){
                    gettersMap.put(otom.getGetMethodName(), attributeDetails);
                    if (otom.getSetMethodName() != null){
                        settersMap.put(otom.getSetMethodName(), attributeDetails);
                    }
                } else {
                    attributeDetails.setIsMappedWithAttributeAccess(true);
                }
                if (typeClz == null){
                    typeClz = getAttributeTypeFromClass(clz, attribute, dm, true);
                }
                if (weaveValueHolders && otom.getIndirectionPolicy().usesIndirection() &&
                    typeClz != null  && !typeClz.isAssignableFrom(ValueHolderInterface.class)) {
                    lazyMappings.add(otom);
                }

            }
            attributesMap.put(attribute, attributeDetails);    
         }
        classDetails.setAttributesMap(attributesMap);
        classDetails.setGetterMethodToAttributeDetails(gettersMap);
        classDetails.setSetterMethodToAttributeDetails(settersMap);
        classDetails.setLazyOneToOneMappings(lazyMappings);
        return unMappedAttributes;
    }
    
    /**
     * Find a descriptor by name in the given project
     * used to avoid referring to descriptors by class.
     * This avoids having to construct a project by class facilitating weaving
     */
    protected ClassDescriptor findDescriptor(Project project, String className){
        Iterator iterator = project.getOrderedDescriptors().iterator();
        while (iterator.hasNext()){
            ClassDescriptor descriptor = (ClassDescriptor)iterator.next();
            if (descriptor.getJavaClassName().equals(className)){
                return descriptor;
            }
        }
        return null;
    }

    /**
     *  Use the database mapping for an attribute to find it's type.  The type returned will either be
     *  the field type of the field in the object or the type returned by the getter method.
     */
    private Class getAttributeTypeFromClass(Class clz, String attributeName, DatabaseMapping mapping, boolean checkSuperclass){       
        String getterMethod = mapping.getGetMethodName();
        if (mapping != null && getterMethod != null){
            try{
                Method method = null;
                if (checkSuperclass){
                    if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                        try {
                            method = (Method)AccessController.doPrivileged(new PrivilegedGetMethod(clz, getterMethod, null, false));
                        } catch (PrivilegedActionException exception) {
                        }
                    } else {
                        method = PrivilegedAccessHelper.getMethod(clz, getterMethod, null, false);
                    }
                } else {
                    method = null;
                    if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                        try {
                            method = (Method)AccessController.doPrivileged(new PrivilegedGetDeclaredMethod(clz, getterMethod, null));
                        } catch (PrivilegedActionException exception) {
                        }
                    } else {
                        method = PrivilegedAccessHelper.getDeclaredMethod(clz, getterMethod, null);
                    }
                }
                if (method != null){
                    return method.getReturnType();
                }
            }  catch (Exception e) {  }
        } else {
            try {
                Class typeClz = null;
                if (checkSuperclass){
                    Field field = null;
                    if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                        try {
                            field = (Field)AccessController.doPrivileged(new PrivilegedGetField(clz, attributeName, false));
                        } catch (PrivilegedActionException exception) {
                        }
                    } else {
                        field = PrivilegedAccessHelper.getField(clz, attributeName, false);
                    }
                    typeClz = field.getType();                     
                } else {
                    Field field = null;
                    if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                        try {
                            field = (Field)AccessController.doPrivileged(new PrivilegedGetDeclaredField(clz, attributeName, false));
                        } catch (PrivilegedActionException exception) {
                        }
                    } else {
                        field = PrivilegedAccessHelper.getDeclaredField(clz, attributeName, false);
                    }
                    typeClz = field.getType();  
                }
                if (typeClz != null){
                    return typeClz;
                }
            }  catch (Exception e) {  }
        }

        return null;
    }

    protected static boolean hasField(Class clz, String fieldName) {
        
        if ("java.lang.Object".equals(clz.getName())) {
            return false;
        }
        else {
            boolean hasField = false;
            // check to see if the mapping's attribute exists as a field on the
            // class; failing that, recurse up super-class(es).
            try {
                Field f = null;
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    try {
                        f = (Field)AccessController.doPrivileged(new PrivilegedGetDeclaredField(clz, fieldName, false));
                    } catch (PrivilegedActionException exception) {
                    }
                } else {
                    f = PrivilegedAccessHelper.getDeclaredField(clz, fieldName, false);
                }
                hasField = true;
            }
            catch (Exception e) { /* ignore */ }
            return hasField ? hasField : hasField(clz.getSuperclass(), fieldName);
        }
    }
    
    protected void log(int level, String msg, Object[] params) {
        ((oracle.toplink.essentials.internal.sessions.AbstractSession)session).log(level,
            SessionLog.WEAVER, msg, params);
    }
}
