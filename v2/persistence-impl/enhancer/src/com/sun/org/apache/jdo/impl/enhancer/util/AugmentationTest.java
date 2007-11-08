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

package com.sun.org.apache.jdo.impl.enhancer.util;

import java.lang.reflect.Modifier;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Arrays;
import java.util.HashSet;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.IOException;

import com.sun.org.apache.jdo.impl.enhancer.EnhancerFatalError;
import com.sun.org.apache.jdo.impl.enhancer.EnhancerUserException;
import com.sun.org.apache.jdo.impl.enhancer.JdoMetaMain;
import com.sun.org.apache.jdo.impl.enhancer.meta.EnhancerMetaDataFatalError;
import com.sun.org.apache.jdo.impl.enhancer.meta.EnhancerMetaDataUserException;




/**
 * Utility class for testing a class file for correct augmentation.
 *
 * @author Martin Zaun
 */
public class AugmentationTest
    extends JdoMetaMain
{
    // return values of internal test methods
    static public final int AFFIRMATIVE = 1;
    static public final int NEGATIVE = 0;
    static public final int ERROR = -1;

    static private final String[] transientPrefixes
        = {"java.",
           "javax." };

    static String toString(int mods,
                           Class type,
                           String name)
    {
        final StringBuffer s = new StringBuffer();
        s.append(Modifier.toString(mods));
        s.append(" ");
        s.append(type.getName());
        s.append(" ");
        s.append(name);
        return s.toString();
    }

    static String toString(int mods,
                           String name,
                           Class[] params)
    {
        final StringBuffer s = new StringBuffer();
        s.append(Modifier.toString(mods));
        s.append(" ");
        s.append(name);
        s.append("(");
        final int j = params.length - 1;
        for (int i = 0; i <= j; i++) {
            s.append(params[i].getName());
            if (i < j)
                s.append(",");
        }
        s.append(")");
        return s.toString();
    }

    static String toString(int mods,
                           Class result,
                           String name,
                           Class[] params)
    {
        final StringBuffer s = new StringBuffer();
        s.append(Modifier.toString(mods));
        s.append(" ");
        s.append(result.getName());
        s.append(" ");
        s.append(name);
        s.append("(");
        final int j = params.length - 1;
        for (int i = 0; i <= j; i++) {
            s.append(params[i].getName());
            if (i < j)
                s.append(",");
        }
        s.append(")");
        return s.toString();
    }

    static String toString(int mods,
                           Class result,
                           String name,
                           Class[] params,
                           Class[] ex)
    {
        final StringBuffer s = new StringBuffer();
        s.append(toString(mods, result, name, params));
        s.append(" throws ");
        final int j = ex.length - 1;
        for (int i = 0; i <= j; i++) {
            s.append(ex[i].getName());
            if (i < j)
                s.append(",");
        }
        return s.toString();
    }

    // ----------------------------------------------------------------------

    // information on currently processed class
    private boolean verbose;
    private String className;
    private String classPath;
    private Class classObject;
    private HashSet fields;
    private HashSet methods;

    // jdo class objects used by reflective tests
    private ClassLoader classLoader;
    private Class persistenceManagerClass;
    private Class instanceCallbacksClass;
    private Class persistenceCapableClass;
    private Class objectIdFieldSupplierClass;
    private Class objectIdFieldConsumerClass;
    private Class stateManagerClass;

    public AugmentationTest(PrintWriter out,
                            PrintWriter err) 
    {
        super(out, err);
    }

    private int implementsInterface(PrintWriter out,
                                    Class intf)
    {
        final Class[] interfaces = classObject.getInterfaces();
        for (int i = interfaces.length - 1; i >= 0; i--) {
            if (interfaces[i].equals(intf)) {
                out.println("        +++ implements interface: "
                            + intf.getName());
                return AFFIRMATIVE;
            }
        }
        out.println("        --- not implementing interface: "
                    + intf.getName());
        return NEGATIVE;
    }

    private int hasField(PrintWriter out,
                         int mods,
                         Class type,
                         String name)
    {
        try {
            final Field field = classObject.getDeclaredField(name);
            fields.remove(field);
            
            if ((field.getModifiers() & mods) != mods) {
                out.println("        !!! ERROR: field declaration: unmatched modifiers");
                out.println("            expected: "
                            + toString(mods, type, name));
                out.println("            found:    "
                            + field.toString());
                return ERROR;
            }

            if (!field.getType().equals(type)) {
                out.println("        !!! ERROR: field declaration: unexpected type");
                out.println("            expected: "
                            + toString(mods, type, name));
                out.println("            found:    "
                            + field.toString());
                return ERROR;
            }

            out.println("        +++ has field: "
                        + field.toString());
            return AFFIRMATIVE;
        } catch (NoSuchFieldException ex) {
            out.println("        --- no field: "
                        + toString(mods, type, name));
            return NEGATIVE;
        }
    }

    private int hasConstructor(PrintWriter out,
                               int mods,
                               Class[] params)
    {
        try {
            final Constructor ctor = classObject.getDeclaredConstructor(params);

            if ((ctor.getModifiers() & mods) != mods) {
                out.println("        !!! ERROR: constructor declaration: unmatched modifiers");
                out.println("            expected: "
                            + toString(mods, className, params));
                out.println("            found:    "
                            + ctor.toString());
                return ERROR;
            }

            out.println("        +++ has constructor: "
                        + ctor.toString());
            return AFFIRMATIVE;
        } catch (NoSuchMethodException ex) {
            out.println("        --- no constructor: "
                        + toString(mods, className, params));
            return NEGATIVE;
        }
    }

    private int hasMethod(PrintWriter out,
                          int mods,
                          Class result,
                          String name,
                          Class[] params,
                          Class[] exepts)
    {
        try {
            final Method method = classObject.getDeclaredMethod(name, params);
            methods.remove(method);

            if ((method.getModifiers() & mods) != mods) {
                out.println("        !!! ERROR: method declaration: unmatched modifiers");
                out.println("            expected: "
                            + toString(mods, result, name, params));
                out.println("            found:    "
                            + method.toString());
                return ERROR;
            }

            if (!method.getReturnType().equals(result)) {
                out.println("        !!! ERROR: method declaration: unexpected result type");
                out.println("            expected: "
                            + toString(mods, result, name, params));
                out.println("            found:    "
                            + method.toString());
                return ERROR;
            }

            final Collection c0 = Arrays.asList(exepts);
            final Collection c1 = Arrays.asList(method.getExceptionTypes());
            if (!c0.containsAll(c1)) {
                out.println("        !!! ERROR: method declaration: unexpected exceptions");
                out.println("            expected: "
                            + toString(mods, result, name, params, exepts));
                out.println("            found:    "
                            + method.toString());
                return ERROR;
            }
            if (!c1.containsAll(c0)) {
                out.println("        !!! ERROR: method declaration: unmatched exceptions");
                out.println("            expected: "
                            + toString(mods, result, name, params, exepts));
                out.println("            found:    "
                            + method.toString());
                return ERROR;
            }

            out.println("        +++ has method: "
                        + method.toString());
            return AFFIRMATIVE;
        } catch (NoSuchMethodException ex) {
            out.println("        --- no method: "
                        + toString(mods, result, name, params));
            return NEGATIVE;
        }
    }

    private int hasMethod(PrintWriter out,
                          int mods,
                          Class result,
                          String name,
                          Class[] params)
    {
        return hasMethod(out, mods, result, name, params, new Class[]{});
    }

    private int evaluate(int nofFeatures,
                         int[] r)
    {
        affirm(nofFeatures <= r.length);
        
        int res = 0;
        for (int i = 0; i < nofFeatures; i++) {
            final int j = r[i];
            affirm(ERROR <= j && j <= AFFIRMATIVE);

            if (j < ERROR) {
                return ERROR;
            }

            if (j > NEGATIVE) {
                res++;
            }
        }
        affirm(res >= 0);
        
        if (res >= nofFeatures) {
            return AFFIRMATIVE;
        }
        return NEGATIVE;
    }
    
    private int hasGenericAugmentation(PrintWriter out)
    {
        affirm(ERROR < NEGATIVE && NEGATIVE < AFFIRMATIVE);
        affirm(classObject);

        final int nofFeatures = 16;
        final int[] r = new int[nofFeatures];
        {
            int i = 0;

            r[i++] = hasField(
                out,
                Modifier.PROTECTED | Modifier.TRANSIENT,
                stateManagerClass,
                "jdoStateManager");

            r[i++] = hasField(
                out,
                Modifier.PROTECTED | Modifier.TRANSIENT,
                byte.class,
                "jdoFlags");
            
            r[i++] = hasMethod(
                out,
                Modifier.PUBLIC
                | Modifier.FINAL
                | Modifier.SYNCHRONIZED,
                void.class,
                "jdoReplaceStateManager",
                new Class[]{stateManagerClass});

            r[i++] = hasMethod(
                out,
                Modifier.PUBLIC | Modifier.FINAL,
                void.class,
                "jdoReplaceFlags",
                new Class[]{});

            r[i++] = hasMethod(
                out,
                Modifier.PUBLIC | Modifier.FINAL,
                persistenceManagerClass,
                "jdoGetPersistenceManager",
                new Class[]{});

            r[i++] = hasMethod(
                out,
                Modifier.PUBLIC | Modifier.FINAL,
                Object.class,
                "jdoGetObjectId",
                new Class[]{});

            r[i++] = hasMethod(
                out,
                Modifier.PUBLIC | Modifier.FINAL,
                Object.class,
                "jdoGetTransactionalObjectId",
                new Class[]{});

            r[i++] = hasMethod(
                out,
                Modifier.PUBLIC | Modifier.FINAL,
                boolean.class,
                "jdoIsPersistent",
                new Class[]{});
            r[i++] = hasMethod(
                out,
                Modifier.PUBLIC | Modifier.FINAL,
                boolean.class,
                "jdoIsTransactional",
                new Class[]{});
            r[i++] = hasMethod(
                out,
                Modifier.PUBLIC | Modifier.FINAL,
                boolean.class,
                "jdoIsNew",
                new Class[]{});
            r[i++] = hasMethod(
                out,
                Modifier.PUBLIC | Modifier.FINAL,
                boolean.class,
                "jdoIsDeleted",
                new Class[]{});
            r[i++] = hasMethod(
                out,
                Modifier.PUBLIC | Modifier.FINAL,
                boolean.class,
                "jdoIsDirty",
                new Class[]{});

            r[i++] = hasMethod(
                out,
                Modifier.PUBLIC | Modifier.FINAL,
                void.class,
                "jdoMakeDirty",
                new Class[]{String.class});

            r[i++] = hasMethod(
                out,
                Modifier.PUBLIC | Modifier.FINAL,
                void.class,
                "jdoReplaceFields",
                new Class[]{int[].class});

            r[i++] = hasMethod(
                out,
                Modifier.PUBLIC | Modifier.FINAL,
                void.class,
                "jdoProvideFields",
                new Class[]{int[].class});

            r[i++] = hasMethod(
                out,
                Modifier.PROTECTED | Modifier.FINAL,
                void.class,
                "jdoPreSerialize",
                new Class[]{});

            affirm(i == nofFeatures);
        }
        
        return evaluate(nofFeatures, r);
    }

    private int hasSpecificAugmentation(PrintWriter out)
    {
        affirm(ERROR < NEGATIVE && NEGATIVE < AFFIRMATIVE);
        affirm(classObject);

        final int nofFeatures = 15;
        final int[] r = new int[nofFeatures];
        {
            int i = 0;

            r[i++] = implementsInterface(
                out,
                persistenceCapableClass);

            r[i++] = hasField(
                out,
                Modifier.PRIVATE | Modifier.FINAL | Modifier.STATIC,
                int.class,
                "jdoInheritedFieldCount");

            r[i++] = hasField(
                out,
                Modifier.PRIVATE | Modifier.FINAL | Modifier.STATIC,
                String[].class,
                "jdoFieldNames");

            r[i++] = hasField(
                out,
                Modifier.PRIVATE | Modifier.FINAL | Modifier.STATIC,
                Class[].class,
                "jdoFieldTypes");

            r[i++] = hasField(
                out,
                Modifier.PRIVATE | Modifier.FINAL | Modifier.STATIC,
                byte[].class,
                "jdoFieldFlags");

            r[i++] = hasField(
                out,
                Modifier.PRIVATE | Modifier.FINAL | Modifier.STATIC,
                Class.class,
                "jdoPersistenceCapableSuperclass");

            r[i++] = hasMethod(
                out,
                Modifier.PROTECTED | Modifier.STATIC,
                int.class,
                "jdoGetManagedFieldCount",
                new Class[]{});

            r[i++] = hasMethod(
                out,
                Modifier.PUBLIC,
                persistenceCapableClass,
                "jdoNewInstance",
                new Class[]{stateManagerClass});

            r[i++] = hasMethod(
                out,
                Modifier.PUBLIC,
                persistenceCapableClass,
                "jdoNewInstance",
                new Class[]{stateManagerClass, Object.class});

            r[i++] = hasMethod(
                out,
                Modifier.PUBLIC,
                void.class,
                "jdoReplaceField",
                new Class[]{int.class});

            r[i++] = hasMethod(
                out,
                Modifier.PUBLIC,
                void.class,
                "jdoProvideField",
                new Class[]{int.class});

            r[i++] = hasMethod(
                out,
                Modifier.PUBLIC,
                void.class,
                "jdoCopyFields",
                new Class[]{Object.class, int[].class});

            r[i++] = hasMethod(
                out,
                Modifier.PROTECTED | Modifier.FINAL,
                void.class,
                "jdoCopyField",
                new Class[]{classObject, int.class});

//^olsen: hack for debugging
/*
            r[i++] = hasField(
                out,
                Modifier.PRIVATE | Modifier.FINAL | Modifier.STATIC,
                longClass,
                "serialVersionUID");

            r[i++] = hasMethod(
                out,
                Modifier.PRIVATE,
                void.class,
                "writeObject",
                new Class[]{java.io.ObjectOutputStream.class},
                new Class[]{java.io.IOException.class});

            //^olsen: need to check for clone()?
*/

            //^olsen: hack for debugging
            affirm(i == nofFeatures-2);
            //affirm(i == nofFeatures);
        }

        //^olsen: hack for debugging
        return evaluate(nofFeatures - 2, r);
    }

    private int hasKeyHandlingAugmentation(PrintWriter out)
    {
        affirm(ERROR < NEGATIVE && NEGATIVE < AFFIRMATIVE);
        affirm(classObject);

        final int nofFeatures = 6;
        final int[] r = new int[nofFeatures];
        {
            int i = 0;

            r[i++] = hasMethod(
                out,
                Modifier.PUBLIC,
                Object.class,
                "jdoNewObjectIdInstance",
                new Class[]{});

            r[i++] = hasMethod(
                out,
                Modifier.PUBLIC,
                Object.class,
                "jdoNewObjectIdInstance",
                new Class[]{String.class});

            r[i++] = hasMethod(
                out,
                Modifier.PUBLIC,
                void.class,
                "jdoCopyKeyFieldsToObjectId",
                new Class[]{Object.class});

            r[i++] = hasMethod(
                out,
                Modifier.PUBLIC,
                void.class,
                "jdoCopyKeyFieldsToObjectId",
                new Class[]{objectIdFieldSupplierClass, Object.class});

            r[i++] = hasMethod(
                out,
                Modifier.PROTECTED,
                void.class,
                "jdoCopyKeyFieldsFromObjectId",
                new Class[]{Object.class});

            r[i++] = hasMethod(
                out,
                Modifier.PUBLIC,
                void.class,
                "jdoCopyKeyFieldsFromObjectId",
                new Class[]{objectIdFieldConsumerClass, Object.class});

            affirm(i == nofFeatures);
        }

        return evaluate(nofFeatures, r);
    }

    private int hasAccessorMutators(PrintWriter out)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        affirm(classObject);
        int res = NEGATIVE;
        
        // find managed field candidates by scanning for jdo[GS]et methods
        final HashSet managedFields = new HashSet();
        for (Iterator i = new HashSet(methods).iterator(); i.hasNext();) {
            final Method method = (Method)i.next();
            final String name = method.getName();

            if (!name.startsWith("jdoGet") && !name.startsWith("jdoSet")) {
                continue;
            }
            final String fieldName = name.substring(6);

            // find declared field
            final Field field;
            try {
                field = classObject.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ex) {
                out.println("        !!! ERROR: potential jdo accessor/mutator method doesn't match declared field");
                out.println("            found method: " + method);
                methods.remove(method);
                res = ERROR;
                continue;
            }

            // field must not be static
            final int fieldMods = field.getModifiers();
            if ((fieldMods & Modifier.STATIC) != 0) {
                out.println("        !!! ERROR: potential jdo accessor/mutator method matches a static field");
                out.println("            found method: " + method);
                out.println("            found field:  " + field);
                methods.remove(method);
                res = ERROR;
                continue;
            }
            
            // field must be managed by jdo metadata
            if (jdoMeta != null && !jdoMeta.isManagedField(classPath, fieldName)) {
                out.println("        !!! ERROR: potential jdo accessor/mutator method matches a non-managed field");
                out.println("            found method: " + method);
                out.println("            found field:  " + field);
                methods.remove(method);
                res = ERROR;
                continue;
            }

            managedFields.add(field);
        }
        
        // find managed field candidates by jdo meta-data
        final String[] metaFieldNames = (jdoMeta != null
                                         ? jdoMeta.getManagedFields(classPath)
                                         : new String[]{});
        for (int i = 0; i < metaFieldNames.length; i++) {
            final String fieldName = metaFieldNames[i];
            
            // find declared field
            final Field field;
            try {
                field = classObject.getDeclaredField(fieldName);
                fields.remove(field);
            } catch (NoSuchFieldException ex) {
                out.println("        !!! ERROR: field defined by jdo meta-data not declared in class");
                out.println("            no declared field: " + fieldName);
                res = ERROR;
                continue;
            }

            // field must not be static
            final int fieldMods = field.getModifiers();
            if ((fieldMods & Modifier.STATIC) != 0) {

                out.println("        !!! ERROR: field defined by jdo meta-data is declared static in class");
                out.println("            static field:  " + field);
                res = ERROR;
                continue;
            }
            
            managedFields.add(field);
        }

        // check accessor/mutator methods for managed field candidates
        final HashSet methodSet = new HashSet(methods);
        for (Iterator i = managedFields.iterator(); i.hasNext();) {
            final Field field = (Field)i.next();
            final String fieldName = field.getName();
            final Class fieldType = field.getType();
            final int fieldMods = field.getModifiers();

            // accessor's and mutator's signature
            final int mods = (Modifier.STATIC
                              | (fieldMods
                                 & (Modifier.PUBLIC
                                    | Modifier.PROTECTED
                                    | Modifier.PRIVATE)));
            final String accessorName = "jdoGet" + fieldName;
            final Class[] accessorParams = new Class[]{classObject};
            final Class accessorReturnType = fieldType;
            final String mutatorName = "jdoSet" + fieldName;
            final Class[] mutatorParams = new Class[]{classObject, fieldType};
            final Class mutatorReturnType = void.class;
            final Class[] exeptions = new Class[]{};

            // find accessor
            final int r0 = hasMethod(out,
                                     mods,
                                     accessorReturnType,
                                     accessorName,
                                     accessorParams,
                                     exeptions);
            if (r0 < NEGATIVE) {
                res = ERROR;
            } else if (r0 == NEGATIVE) {
                out.println("        !!! ERROR: missing or incorrect jdo accessor for declared field");
                out.println("            field:  " + field);
                out.println("            expected: "
                            + toString(mods,
                                       accessorReturnType,
                                       accessorName,
                                       accessorParams,
                                       exeptions));
                for (Iterator j = methodSet.iterator(); j.hasNext();) {
                    final Method method = (Method)j.next();
                    if (method.getName().equals(accessorName)) {
                        out.println("            found:  " + method);
                        methods.remove(method);
                    }
                }
                res = ERROR;
            }

            // find mutator
            final int r1 = hasMethod(out,
                                     mods,
                                     mutatorReturnType,
                                     mutatorName,
                                     mutatorParams,
                                     exeptions);
            if (r1 < NEGATIVE) {
                res = ERROR;
            } else if (r1 == NEGATIVE) {
                out.println("        !!! ERROR: missing or incorrect jdo mutator for declared field");
                out.println("            field:  " + field);
                out.println("            expected: "
                            + toString(mods,
                                       mutatorReturnType,
                                       mutatorName,
                                       mutatorParams,
                                       exeptions));
                for (Iterator j = methodSet.iterator(); j.hasNext();) {
                    final Method method = (Method)j.next();
                    if (method.getName().equals(accessorName)) {
                        out.println("            found:  " + method);
                        methods.remove(method);
                    }
                }
                res = ERROR;
            }

            // have found legal accessor/mutator pair
            if (res == NEGATIVE) {
                res = AFFIRMATIVE;
            }
        }

        return res;
    }    

    private int hasInstanceCallbacks(PrintWriter out)
    {
        affirm(ERROR < NEGATIVE && NEGATIVE < AFFIRMATIVE);
        affirm(classObject);

        final int nofFeatures = 5;
        final int[] r = new int[nofFeatures];
        {
            int i = 0;

            r[i++] = implementsInterface(
                out,
                instanceCallbacksClass);

            r[i++] = hasMethod(
                out,
                Modifier.PUBLIC,
                void.class,
                "jdoPostLoad",
                new Class[]{});

            r[i++] = hasMethod(
                out,
                Modifier.PUBLIC,
                void.class,
                "jdoPreStore",
                new Class[]{});

            r[i++] = hasMethod(
                out,
                Modifier.PUBLIC,
                void.class,
                "jdoPreClear",
                new Class[]{});

            r[i++] = hasMethod(
                out,
                Modifier.PUBLIC,
                void.class,
                "jdoPreDelete",
                new Class[]{});

            affirm(i == nofFeatures);
        }

        return evaluate(1, r);
    }

    private int testPCFeasibility(PrintWriter out)
    {
        affirm(classObject);

        int status = AFFIRMATIVE;

        final int mods = classObject.getModifiers();

        // PC class must provide default constructor
        StringWriter s = new StringWriter();
        final int hasCtor = hasConstructor(new PrintWriter(s),
                                           0,
                                           new Class[]{});
        if (hasCtor <= NEGATIVE) {
            status = ERROR;
        } else {
            if (verbose) {
                out.print(s.toString());
            }
        }

        // PC class must not be an interface type
        if (classObject.isInterface()) {
            out.println("        !!! ERROR: class is interface");
            status = ERROR;
        } else {
            if (verbose) {
                out.println("        +++ is not an interface");
            }
        }

        // PC class may be abstract if not instantiated at class
        // registration with JDOImplHelper
        //if (Modifier.isAbstract(mods)) {
        //    out.println("        !!! ERROR: class is abstract");
        //    status = ERROR;
        //} else {
        //    if (verbose) {
        //        out.println("        +++ is not abstract");
        //    }
        //}

        // PC class cannot be an inner classes because of instantiation
        // from static context (registration with JDOImplHelper)
        if (classObject.getDeclaringClass() != null
            && !Modifier.isStatic(mods)) {
            out.println("        !!! ERROR: class is inner class");
            status = ERROR;
        } else {
            if (verbose) {
                out.println("        +++ is not an inner class");
            }
        }

        // PC class must not have transient package prefix
        for (int i = 0; i < transientPrefixes.length; i++) {
            final String typePrefix = transientPrefixes[i];
            if (className.startsWith(typePrefix)) {
                out.println("        !!! ERROR: class is in package: "
                            + typePrefix + "..");
                status = ERROR;
            } else {
                if (verbose) {
                    out.println("        +++ is not in package: "
                                + typePrefix + "..");
                }
            }
        }
        
        //^olsen: PC class must not be SCO type?

        // PC class is better not a Throwable
        //if (Throwable.class.isAssignableFrom(classObject)) {
        //    out.println("        !!! ERROR: class extends Throwable");
        //    status = ERROR;
        //} else {
        //    if (verbose) {
        //        out.println("        +++ does not extend Throwable");
        //    }
        //}
        
        // PC class can have any access modifier; JDO runtime accesses it
        // through PersistenceCapable interface only
        //if (!Modifier.isPublic(mods)) {
        //    out.println("        !!! ERROR: class is not public");
        //    status = ERROR;
        //} else {
        //    if (verbose) {
        //        out.println("        +++ is public");
        //    }
        //}

        // pathological: PC class must not be a primitive type
        if (classObject.isPrimitive()) {
            out.println("        !!! ERROR: class is of primitive type");
            status = ERROR;
        }

        // pathological: PC class must not be an array type
        if (classObject.isArray()) {
            out.println("        !!! ERROR: class is of array type");
            status = ERROR;
        }

        // pathological: PC class must have superclass
        if (classObject.getSuperclass() == null) {
            out.println("        !!! ERROR: class doesn't have super class");
            status = ERROR;
        }

        return status;
    }

    private int hasNoIllegalJdoMembers(PrintWriter out)
    {
        affirm(classObject);
        int res = AFFIRMATIVE;
        
        for (Iterator i = new HashSet(methods).iterator(); i.hasNext();) {
            final Method method = (Method)i.next();
            final String name = method.getName();
            if (name.startsWith("jdo")) {
                out.println("        !!! ERROR: illegal jdo method");
                out.println("            found method: " + method);
                methods.remove(method);
                res = ERROR;
            }
        }
        
        for (Iterator i = new HashSet(fields).iterator(); i.hasNext();) {
            final Field field = (Field)i.next();
            final String name = field.getName();
            if (name.startsWith("jdo")) {
                out.println("        !!! ERROR: illegal jdo field");
                out.println("            found field: " + field);
                fields.remove(field);
                res = ERROR;
            }
        }

        return res;
    }
    
    private int testAugmentation(PrintWriter out)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        affirm(ERROR < NEGATIVE && NEGATIVE < AFFIRMATIVE);
        affirm(classObject);
        affirm(className);

        // check class-specific enhancement
        StringWriter s = new StringWriter();
        int r0 = hasSpecificAugmentation(new PrintWriter(s));
        //System.out.println("hasSpecificAugmentation = " + r0);
        if (r0 < NEGATIVE) {
            out.println("    !!! ERROR: inconsistent \"class-specific\" augmentation");
            out.println(s.toString());
            r0 = ERROR;
        } else if (r0 == NEGATIVE) {
            if (jdoMeta != null && jdoMeta.isPersistenceCapableClass(classPath)) {
                out.println("    !!! ERROR: missing \"class-specific\" augmentation");
                out.println(s.toString());
                r0 = ERROR;
            } else {
                if (verbose) {
                    out.println("    --- no \"class-specific\" augmentation");
                    out.println(s.toString());
                }
            }
        } else {
            affirm(r0 > NEGATIVE);
            if (jdoMeta != null && !jdoMeta.isPersistenceCapableClass(classPath)) {
                out.println("    !!! ERROR: unexpected \"class-specific\" augmentation");
                out.println(s.toString());
                r0 = ERROR;
            } else {
                if (verbose) {
                    out.println("    +++ has correct \"class-specific\" augmentation");
                    out.println(s.toString());
                }
            }
        }

        // check key-handling enhancement
        s = new StringWriter();
        int r1 = hasKeyHandlingAugmentation(new PrintWriter(s));
        //System.out.println("hasKeyHandlingAugmentation = " + r1);
        if (r1 < NEGATIVE) {
            out.println("    !!! ERROR: inconsistent \"key-handling\" augmentation");
            out.println(s.toString());
            r1 = ERROR;
        } else if (r1 == NEGATIVE) {
            if (jdoMeta != null
                && (jdoMeta.isPersistenceCapableClass(classPath)
                    && jdoMeta.getKeyClass(classPath) != null)) {
                out.println("    !!! ERROR: missing \"key-handling\" augmentation");
                out.println(s.toString());
                r1 = ERROR;
            } else {
                if (verbose) {
                    out.println("    --- no \"key-handling\" augmentation");
                    out.println(s.toString());
                }
            }
        } else {
            affirm(r1 > NEGATIVE);
            if (r0 == NEGATIVE
                || (jdoMeta != null
                    && (!jdoMeta.isPersistenceCapableRootClass(classPath)
                        && jdoMeta.getKeyClass(classPath) == null))) {
                out.println("    !!! ERROR: unexpected \"key-handling\" augmentation");
                out.println(s.toString());
                r1 = ERROR;
            } else {
                if (verbose) {
                    out.println("    +++ has correct \"key-handling\" augmentation");
                    out.println(s.toString());
                }
            }
        }
        affirm(r0 != NEGATIVE || r1 <= NEGATIVE);
        
        // check generic enhancement
        s = new StringWriter();
        int r2 = hasGenericAugmentation(new PrintWriter(s));
        //System.out.println("hasGenericAugmentation = " + r2);
        if (r2 < NEGATIVE) {
            out.println("    !!! ERROR: inconsistent \"generic\" augmentation");
            out.println(s.toString());
            r2 = ERROR;
        } else if (r2 == NEGATIVE) {
            if (jdoMeta != null
                && jdoMeta.isPersistenceCapableRootClass(classPath)) {
                out.println("    !!! ERROR: missing \"generic\" augmentation");
                out.println(s.toString());
                r2 = ERROR;
            } else {
                if (verbose) {
                    out.println("    --- no \"generic\" augmentation");
                    out.println(s.toString());
                }
            }
        } else {
            affirm(r2 > NEGATIVE);
            if (r0 == NEGATIVE
                || (jdoMeta != null
                    && !jdoMeta.isPersistenceCapableRootClass(classPath))) {
                out.println("    !!! ERROR: unexpected \"generic\" augmentation");
                out.println(s.toString());
                r2 = ERROR;
            } else {
                if (verbose) {
                    out.println("    +++ has correct \"generic\" augmentation");
                    out.println(s.toString());
                }
            }
        }
        affirm(r0 != NEGATIVE || r2 <= NEGATIVE);
        
        // check accessor/mutator enhancement
        s = new StringWriter();
        int r3 = hasAccessorMutators(new PrintWriter(s));
        //System.out.println("hasAccessorMutators = " + r3);
        if (r3 < NEGATIVE) {
            out.println("    !!! ERROR: inconsistent \"accessor/mutator\" augmentation");
            out.println(s.toString());
        } else if (r3 == NEGATIVE) {
            if (verbose) {
                out.println("    --- no \"accessor/mutator\" augmentation");
                out.println(s.toString());
            }
        } else {
            affirm(r3 > NEGATIVE);
            if (r0 == NEGATIVE) {
                out.println("    !!! ERROR: unexpected \"accessor/mutator\" augmentation");
                out.println(s.toString());
                r3 = ERROR;
            } else {
                if (verbose) {
                    out.println("    +++ has correct \"accessor/mutator\" augmentation");
                    out.println(s.toString());
                }
            }
        }        
        affirm(r0 != NEGATIVE || r3 <= NEGATIVE);

        // check user-defined instance callback features
        s = new StringWriter();
        int r4 = hasInstanceCallbacks(new PrintWriter(s));
        //System.out.println("hasInstanceCallbacks = " + r4);
        if (r4 < NEGATIVE) {
            out.println("    !!! ERROR: inconsistent instance callback features");
            out.println(s.toString());
        } else if (r4 == NEGATIVE) {
            if (verbose) {
                out.println("    --- no instance callback features");
                out.println(s.toString());
            }
        } else {
            affirm(r4 > NEGATIVE);
            if (verbose) {
                out.println("    +++ has instance callback features");
                out.println(s.toString());
            }
        }        

        // check for illegal jdo* member enhancement
        s = new StringWriter();
        int r5 = hasNoIllegalJdoMembers(new PrintWriter(s));
        if (r5 <= NEGATIVE) {
            out.println("    !!! ERROR: illegal jdo member");
            out.println(s.toString());
        } else {
            if (verbose) {
                out.println("    +++ no illegal jdo member");
                out.println(s.toString());
            }
        }        

        // return if error so far
        if (r0 < NEGATIVE || r1 < NEGATIVE || r2 < NEGATIVE || r3 < NEGATIVE
            || r4 < NEGATIVE || r5 < NEGATIVE) {
            return ERROR;
        }

        // return if class not PC and no error
        if (r0 == NEGATIVE) {
            affirm(r1 == NEGATIVE);
            affirm(r2 == NEGATIVE);
            affirm(r3 == NEGATIVE);
            affirm(r4 >= NEGATIVE);
            affirm(r5 > NEGATIVE);
            return NEGATIVE;
        }

        // check feasibility if class PC
        s = new StringWriter();
        int r6 = testPCFeasibility(new PrintWriter(s));
        if (r6 <= NEGATIVE) {
            out.println("    !!! not feasible for persistence-capability");
            out.println(s.toString());
            r6 = ERROR;
        } else {
            if (verbose) {
                out.println("    +++ is feasible for persistence-capability");
                out.println(s.toString());
            }
        }
        
        // return with error if class not PC feasible
        if (r6 < NEGATIVE) {
            return ERROR;
        }

        // class PC and no errors with augmentation
        return AFFIRMATIVE;
    }

    private int testLoadingClass(PrintWriter out)
    {
        try {
            classObject = classLoader.loadClass(className);
            out.println("    +++ loaded class");
        } catch (LinkageError err) {
            out.println("    !!! ERROR: linkage error when loading class: "
                        + className);
            out.println("        error: " + err);
            return ERROR;
        } catch (ClassNotFoundException ex) {
            out.println("    !!! ERROR: class not found: " + className);
            out.println("        exception: " + ex);
            return ERROR;
        }

        try {
            fields = new HashSet();
            fields.addAll(Arrays.asList(classObject.getDeclaredFields()));
            methods = new HashSet();
            methods.addAll(Arrays.asList(classObject.getDeclaredMethods()));
        } catch (SecurityException ex) {
            affirm(false);
            return ERROR;
        }   
        return AFFIRMATIVE;
    }

    private int test(PrintWriter out,
                     String className)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        affirm(className);
        this.className = className;
        this.classPath = className.replace('.', '/');


        if (verbose) {
            out.println("-------------------------------------------------------------------------------");
            out.println();
            out.println("Test class for augmentation: "
                        + className + " ...");
        }
        
        // check loading class
        StringWriter s = new StringWriter();
        if (testLoadingClass(new PrintWriter(s)) <= NEGATIVE) {
            out.println();
            out.println("!!! ERROR: failed loading class: " + className);
            out.println(s.toString());
            return ERROR;
        }

        if (verbose) {
            out.println();
            out.println("+++ loaded class: " + className);
            out.println(s.toString());
        }
        
        // check augmentation
        s = new StringWriter();
        final int r = testAugmentation(new PrintWriter(s));
        if (r < NEGATIVE) {
            out.println();
            out.println("!!! ERROR: incorrect augmentation: " + className);
            out.println(s.toString());
            return ERROR;
        }
        
        if (r == NEGATIVE) {
            out.println();
            out.println("--- class not augmented: " + className);
        } else {
            out.println();
            out.println("+++ class augmented: " + className);
        }
        if (verbose) {
            out.println(s.toString());
        }

        return r;
    }

    protected int test(PrintWriter out,
                       boolean verbose,
                       List classNames)
    {
        affirm(classNames);
        this.verbose = verbose;
        final int all = classNames.size();

        out.println();
        out.println("AugmentationTest: Testing Classes for JDO Persistence-Capability Enhancement");

        int nofFailed = 0;
        for (int i = 0; i < all; i++) {
            if (test(out, (String)classNames.get(i)) < NEGATIVE) {
                nofFailed++;
            }
        }
        final int nofPassed = all - nofFailed;

        out.println();
        out.println("AugmentationTest: Summary:  TESTED: " + all
                    + "  PASSED: " + nofPassed
                    + "  FAILED: " + nofFailed);
        return nofFailed;
    }
    
    // ----------------------------------------------------------------------

    /**
     * Initializes all components.
     */
    protected void init()
        throws EnhancerFatalError, EnhancerUserException
    {
        super.init();
        if (!options.classFileNames.isEmpty()
            || !options.archiveFileNames.isEmpty()) {
            throw new EnhancerFatalError("Sorry, this test right now only support class name arguments, not class or archive files.");
        }
        affirm(classes != null);
        try {
            classLoader = classes.getClassLoader();
            persistenceManagerClass
                = classLoader.loadClass("com.sun.persistence.support.PersistenceManager");
            instanceCallbacksClass
                = classLoader.loadClass("com.sun.persistence.support.InstanceCallbacks");
            persistenceCapableClass
                = classLoader.loadClass("com.sun.persistence.support.spi.PersistenceCapable");
            objectIdFieldSupplierClass
                = classLoader.loadClass("com.sun.persistence.support.spi.PersistenceCapable$ObjectIdFieldSupplier");
            objectIdFieldConsumerClass
                = classLoader.loadClass("com.sun.persistence.support.spi.PersistenceCapable$ObjectIdFieldConsumer");
            stateManagerClass
                = classLoader.loadClass("com.sun.persistence.support.spi.StateManager");
        } catch (Exception ex) {
            throw new EnhancerFatalError(ex);
        }
    }
    
    /**
     * Run the augmentation test.
     */
    protected int process()
    {
        //^olsen: Unfortunately, this test cannot reasonably deal with
        // java classfiles passed as command line arguments but only with
        // archive files (.zip/.jar) or a source-path argument.
        // For this restriction, the inherited parsing/checking of options
        // and the usage-help needs to be overriden/corrected.
        // --> BaseOptions.check(), printUsageHeader() ...

        //^olsen: to be extended for zip/jar arguments
        return test(out, options.verbose.value, options.classNames);
    }

    /**
     * Runs this class
     */
    static public void main(String[] args)
    {
        final PrintWriter out = new PrintWriter(System.out, true);
        out.println("--> AugmentationTest.main()");
        final AugmentationTest main = new AugmentationTest(out, out);
        int res = main.run(args);
        out.println("<-- AugmentationTest.main(): exit = " + res);
        System.exit(res);
    }
}
