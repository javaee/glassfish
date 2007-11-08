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

/**
 * PrintSupport.java
 *
 */

package com.sun.org.apache.jdo.impl.model.jdo.util;

import java.util.*;

import com.sun.org.apache.jdo.model.jdo.JDOArray;
import com.sun.org.apache.jdo.model.jdo.JDOClass;
import com.sun.org.apache.jdo.model.jdo.JDOCollection;
import com.sun.org.apache.jdo.model.jdo.JDOExtension;
import com.sun.org.apache.jdo.model.jdo.JDOField;
import com.sun.org.apache.jdo.model.jdo.JDOIdentityType;
import com.sun.org.apache.jdo.model.jdo.JDOMap;
import com.sun.org.apache.jdo.model.jdo.JDOModel;
import com.sun.org.apache.jdo.model.jdo.JDOReference;
import com.sun.org.apache.jdo.model.jdo.JDORelationship;
import com.sun.org.apache.jdo.model.jdo.NullValueTreatment;
import com.sun.org.apache.jdo.model.jdo.PersistenceModifier;

public class PrintSupport 
{
    public static void printJDOModel(JDOModel jdoModel) 
    {
        JDOClass[] jdoClasses = jdoModel.getDeclaredClasses();
        for (int i = 0; i < jdoClasses.length; i++) {
            printJDOClass(0, jdoClasses[i]);
        }
    }
    
    public static void printJDOClass(JDOClass jdoClass)
    {
        printJDOClass(0, jdoClass);
    }
    
    public static void printJDOFields(JDOField[] jdoFields)
    {
        printJDOFields(0, jdoFields);
    }

    public static void printJDOField(JDOField jdoField) 
    {
        printJDOField(0, jdoField);
    }
    
    public static void printJDORelationship(JDORelationship jdoRelationship) 
    {
        printJDORelationship(0, jdoRelationship);
    }

    public static void printJDOExtensions(JDOExtension[] jdoExtensions)
    {
        printJDOExtensions(0, jdoExtensions);
    }

    public static void printJDOExtension(JDOExtension jdoExtension) 
    {
        printJDOExtension(0, jdoExtension);
    }

    // ----- methods taking indent level -----

    private static void printJDOClass(int indent, JDOClass jdoClass) 
    {
        if (jdoClass == null)
            return;

        println(indent, "--> JDOClass "); //NOI18N
        println(indent+1, "name                       = " + jdoClass.getName()); //NOI18N
        println(indent+1, "shortName                  = " + jdoClass.getShortName()); //NOI18N
        println(indent+1, "packagePrefix              = " + jdoClass.getPackagePrefix()); //NOI18N
        println(indent+1, "identityType               = " + JDOIdentityType.toString(jdoClass.getIdentityType())); //NOI18N
        println(indent+1, "objectIdClass              = " + jdoClass.getObjectIdClass()); //NOI18N
        println(indent+1, "declaredObjectIdClassName  = " + jdoClass.getDeclaredObjectIdClassName()); //NOI18N
        println(indent+1, "requiresExtent             = " + jdoClass.requiresExtent()); //NOI18N
        println(indent+1, "pcSuperclassName           = " + jdoClass.getPersistenceCapableSuperclassName()); //NOI18N
        println(indent+1, "pcSuperclass               = " + jdoClass.getPersistenceCapableSuperclass()); //NOI18N
        println(indent+1, "pcRootClass                = " + jdoClass.getPersistenceCapableRootClass()); //NOI18N
        println(indent+1, "javaType                   = " + jdoClass.getJavaType()); //NOI18N
        println(indent+1, "declaredManagedFieldCount  = " + jdoClass.getDeclaredManagedFieldCount()); //NOI18N
        println(indent+1, "inheritedManagedFieldCount = " + jdoClass.getInheritedManagedFieldCount());  //NOI18N
        println(indent+1, "managedFields              = " + Arrays.asList(jdoClass.getManagedFields())); //NOI18N
        println(indent+1, "managedFieldNumbers        = " + asList(jdoClass.getManagedFieldNumbers()));  //NOI18N
        println(indent+1, "persistentFieldNumbers     = " + asList(jdoClass.getPersistentFieldNumbers())); //NOI18N
        println(indent+1, "primaryKeyFieldNumbers     = " + asList(jdoClass.getPrimaryKeyFieldNumbers())); //NOI18N
        println(indent+1, "persistentNonPKFieldNs     = " + asList(jdoClass.getPersistentNonPrimaryKeyFieldNumbers())); //NOI18N
        println(indent+1, "persistentRelshipFieldNs   = " + asList(jdoClass.getPersistentRelationshipFieldNumbers())); //NOI18N
        println(indent+1, "persistentSerializableFNs  = " + asList(jdoClass.getPersistentSerializableFieldNumbers())); //NOI18N
        println(indent+1, "declaredFields"); //NOI18N
        printJDOFields(indent+1, jdoClass.getDeclaredFields());
        printJDOExtensions(indent+1, jdoClass.getJDOExtensions());
        println(0, "<-- JDOClass\n "); //NOI18N
    }
    
    private static void printJDOFields(int indent, JDOField[] jdoFields) {
        if ((jdoFields != null) && (jdoFields.length > 0)) {
            TreeSet sorted = new TreeSet(new Comparator() {
                    public int compare(Object o1, Object o2) {
                        return (((JDOField)o1).getFieldNumber()
                                - ((JDOField)o2).getFieldNumber());
                    }
                    public boolean equals(Object obj) {
                        return obj.equals(this);
                    }
                });

            for (int i = 0; i < jdoFields.length; i++) {
                sorted.add(jdoFields[i]);
            }

            for (Iterator i = sorted.iterator(); i.hasNext();) {
                printJDOField(indent, (JDOField)i.next());
            }
        }
    }
    
    private static void printJDOField(int indent, JDOField jdoField) 
    {
        if (jdoField == null)
            return;
        boolean isProperty = jdoField.isProperty();

        if (isProperty)
            println(indent, "--> JDOProperty"); //NOI18N
        else
            println(indent, "--> JDOField"); //NOI18N
        println(indent+1, "name                = " + jdoField.getName()); //NOI18N
        println(indent+1, "declaringClass      = " + jdoField.getDeclaringClass().getName()); //NOI18N
        println(indent+1, "persistenceModifier = " + PersistenceModifier.toString(jdoField.getPersistenceModifier())); //NOI18N
        println(indent+1, "primaryKey          = " + jdoField.isPrimaryKey()); //NOI18N
        println(indent+1, "nullValue           = " + NullValueTreatment.toString(jdoField.getNullValueTreatment())); //NOI18N
        println(indent+1, "defaultFetchGroup   = " + jdoField.isDefaultFetchGroup()); //NOI18N
        println(indent+1, "embedded            = " + jdoField.isEmbedded()); //NOI18N
        println(indent+1, "type                = " + jdoField.getType()); //NOI18N
        //println(indent+1, "typeName            = " + jdoField.getTypeName()); //NOI18N
        println(indent+1, "javaField           = " + jdoField.getJavaField()); //NOI18N
        println(indent+1, "serializable        = " + jdoField.isSerializable()); //NOI18N
        println(indent+1, "mappedByName        = " + jdoField.getMappedByName()); //NOI18N
        println(indent+1, "fieldNumber         = " + jdoField.getFieldNumber()); //NOI18N
        println(indent+1, "relativeFieldNumber = " + jdoField.getRelativeFieldNumber()); //NOI18N
        println(indent+1, "isProperty          = " + jdoField.isProperty()); //NOI18N
        printJDORelationship(indent+1, jdoField.getRelationship()); //NOI18N
        printJDOExtensions(indent+1, jdoField.getJDOExtensions()); //NOI18N
        if (isProperty)
            println(indent, "<-- JDOProperty "); //NOI18N
        else
            println(indent, "<-- JDOField "); //NOI18N
    }
    
    private static void printJDORelationship(int indent, 
                                            JDORelationship jdoRelationship) 
    {
        if (jdoRelationship == null) 
            return;

        if (jdoRelationship.isJDOReference())
            printJDOReference(indent, (JDOReference)jdoRelationship);
        else if (jdoRelationship.isJDOCollection())
            printJDOCollection(indent, (JDOCollection)jdoRelationship);
        else if (jdoRelationship.isJDOArray())
            printJDOArray(indent, (JDOArray)jdoRelationship);
        else if (jdoRelationship.isJDOMap())
            printJDOMap(indent, (JDOMap)jdoRelationship);
    }
    
    private static void printJDORelationshipHelper(int indent, JDORelationship jdoRelationship) 
    {
        if (jdoRelationship == null)
            return;

        JDORelationship mappedBy = jdoRelationship.getMappedBy();
        JDORelationship inverse = jdoRelationship.getInverseRelationship();
        println(indent+1, "declaringField  = " + jdoRelationship.getDeclaringField().getName()); //NOI18N
        println(indent+1, "bounds          = " + jdoRelationship.getLowerBound() + " / " +  jdoRelationship.getUpperBound()); //NOI18N
        println(indent+1, "mappedBy        = " + ((mappedBy==null) ? "null" : //NOI18N
            mappedBy.getDeclaringField().getDeclaringClass().getName() + "." + //NOI18N
            mappedBy.getDeclaringField().getName()));
        println(indent+1, "inverseName     = " + jdoRelationship.getInverseRelationshipName());
        println(indent+1, "inverse         = " + ((inverse==null) ? "null" : //NOI18N
            inverse.getDeclaringField().getDeclaringClass().getName() + "." + //NOI18N
            inverse.getDeclaringField().getName()));
    }
    
    private static void printJDOReference(int indent, JDOReference jdoReference) 
    {
        if (jdoReference == null)
            return;

        println(indent, "--> JDOReference"); //NOI18N
        printJDORelationshipHelper(indent, jdoReference);
        printJDOExtensions(indent+1, jdoReference.getJDOExtensions());
        println(indent, "<-- JDOReference"); //NOI18N
    }
    
    private static void printJDOCollection(int indent, JDOCollection jdoCollection) 
    {
        if (jdoCollection == null)
            return;

        println(indent, "--> JDOCollection"); //NOI18N
        printJDORelationshipHelper(indent, jdoCollection);
        println(indent+1, "embeddedElement = " + jdoCollection.isEmbeddedElement()); //NOI18N
        println(indent+1, "elementType     = " + jdoCollection.getElementType()); //NOI18N
        println(indent+1, "elementTypeName = " + jdoCollection.getElementTypeName()); //NOI18N
        printJDOExtensions(indent+1, jdoCollection.getJDOExtensions()); //NOI18N
        println(indent, "<-- JDOCollection"); //NOI18N
    }
    
    private static void printJDOArray(int indent, JDOArray jdoArray) 
    {
        if (jdoArray == null)
            return;

        println(indent, "--> JDOArray"); //NOI18N
        printJDORelationshipHelper(indent, jdoArray);
        println(indent+1, "embeddedElement = " + jdoArray.isEmbeddedElement()); //NOI18N
        printJDOExtensions(indent+1, jdoArray.getJDOExtensions());
        println(indent, "<-- JDOArray"); //NOI18N
    }
    
    private static void printJDOMap(int indent, JDOMap jdoMap) 
    {
        if (jdoMap == null)
            return;

        println(indent, "--> JDOMap"); //NOI18N
        printJDORelationshipHelper(indent, jdoMap);
        println(indent+1, "embeddedKey     = " + jdoMap.isEmbeddedKey()); //NOI18N
        println(indent+1, "keyType         = " + jdoMap.getKeyType()); //NOI18N
        println(indent+1, "keyTypeName     = " + jdoMap.getKeyTypeName()); //NOI18N
        println(indent+1, "embeddedValue   = " + jdoMap.isEmbeddedValue()); //NOI18N
        println(indent+1, "valueType       = " + jdoMap.getValueType()); //NOI18N
        println(indent+1, "valueTypeName   = " + jdoMap.getValueTypeName()); //NOI18N
        printJDOExtensions(indent+1, jdoMap.getJDOExtensions());
        println(indent, "<-- JDOMap"); //NOI18N
    }
    
    private static void printJDOExtensions(int indent, JDOExtension[] jdoExtensions) 
    {
        if ((jdoExtensions != null) && (jdoExtensions.length > 0)) {
            TreeSet sorted = new TreeSet(new Comparator() {
                    public int compare(Object o1, Object o2) {
                        return ((JDOExtension)o1).getKey().compareTo(
                          ((JDOExtension)o2).getKey());
                    }
                    public boolean equals(Object obj) {
                        return obj.equals(this);
                    }
                });

            for (int i = 0; i < jdoExtensions.length; i++) {
                sorted.add(jdoExtensions[i]);
            }

            for (Iterator i = sorted.iterator(); i.hasNext();) {
                printJDOExtension(indent, (JDOExtension)i.next());
            }
        }
    }
    
    private static void printJDOExtension(int indent, JDOExtension jdoExtension) 
    {
        if (jdoExtension == null)
            return;
        
        println(indent, "--> JDOExtension"); //NOI18N
        println(indent+1, "vendorName = " + jdoExtension.getVendorName()); //NOI18N
        println(indent+1, "key        = " + jdoExtension.getKey()); //NOI18N
        println(indent+1, "value      = " + jdoExtension.getValue()); //NOI18N
        println(indent, "<-- JDOExtension"); //NOI18N
    }
    
    // ----- helper methods -----
    
    static void println(int indent, String text) {
        for (int i = 0; i < indent; i++) {
            System.out.print("    "); //NOI18N
        }
        System.out.println(text);
    }

    public static List asList(int[] array) 
    {
        int length = (array == null) ? 0 : array.length;
        List list = new ArrayList(length);
        for (int i = 0; i < length; i++) {
            list.add(new Integer(array[i]));
        }
        return list;
    }
}
