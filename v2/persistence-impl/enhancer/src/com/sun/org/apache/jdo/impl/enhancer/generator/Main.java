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

import java.lang.reflect.Modifier;

import java.util.Iterator;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import java.io.Serializable;
import java.io.File;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import com.sun.org.apache.jdo.impl.enhancer.meta.ExtendedMetaData;
import com.sun.org.apache.jdo.impl.enhancer.meta.prop.EnhancerMetaDataPropertyImpl;
import com.sun.org.apache.jdo.impl.enhancer.util.Support;




/**
 *
 */
public final class Main
    extends Support
{
    /**
     *  The stream to write messages to.
     */
    private final PrintWriter out = new PrintWriter(System.out, true);

    /**
     *  The stream to write error messages to.
     */
    private final PrintWriter err = new PrintWriter(System.err, true);

    /**
     *  The command line options.
     */
    private final CmdLineOptions opts = new CmdLineOptions();

    /**
     *
     */
    private final CodeWriter writer = new CodeWriter();

    /**
     * The MetaData for generating classes.
     */
    private ExtendedMetaData meta = null;
     
    /**
     *
     */
    public Main()
    {}

    /**
     *
     */
    public static final void main(String[] argv)
    {
        final Main gen = new Main();
        try {
            gen.opts.processArgs(argv);
            gen.init();
            gen.generate();
        } catch(Exception ex) {
            gen.printError(null, ex);
        }
    }

    /**
     *  A class for holding the command line options.
     */
    private class CmdLineOptions
    {
        // final Collection inputFileNames = new ArrayList();
        String destinationDirectory = null;
        String jdoXMLModelFileName = null;
        String jdoPropertiesFileName = null;
        boolean verbose = false;
        boolean quiet = false;
        boolean forceWrite = false;
        boolean noWrite = false;

        /**
         * Print a usage message to System.err
         */
        public void usage() {
            err.println("Usage: Main <options> <arguments>...");
            err.println("Options:");
            err.println("  -v, --verbose            print verbose output");
/*
            err.println("  -q, --quiet              supress warnings");
            err.println("  -n, --nowrite            never write classfiles");
            err.println("  -f, --force              ever write classfiles");
*/
            err.println("  -d, --dest <dir>         destination directory for output files");
            err.println("  -p, --properties <file>  use property file for meta data");
/*
            err.println("  -x, --xmlmodel <file>    use JDO XML model file for meta data");
*/
            err.println();
            err.println("Arguments:");
            err.println();
            err.println("Returns a non-zero value in case of errors.");
            System.exit(1);
        }

        /**
         * Process command line options
         */
        protected int processArgs(String[] argv)
        {
            for (int i = 0; i < argv.length; i++) {
                final String arg = argv[i];
                if (arg.equals("-v")
                    || arg.equals("--verbose")) {
                    verbose = true;
                    quiet = false;
                    continue;
                }
/*
                if (arg.equals("-q")
                    || arg.equals("--quiet")) {
                    quiet = true;
                    verbose = false;
                    continue;
                }
                if (arg.equals("-f")
                    || arg.equals("--force")) {
                    forceWrite = true;
                    continue;
                }
                if (arg.equals("-n")
                    || arg.equals("--nowrite")) {
                    noWrite = true;
                    continue;
                }
*/
                if (arg.equals("-d")
                    || arg.equals("--dest")) {
                    if (argv.length - i < 2) {
                        printError("Missing argument to the -d/-dest option", null);
                        usage();
                    }
                    destinationDirectory = argv[++i];
                    continue;
                }
                if (arg.equals("-p") ||
                    arg.equals("--properties")) {
                    if (argv.length - i < 2) {
                        printError("Missing argument to the -p/--properties option", null);
                        usage();
                    }
                    jdoPropertiesFileName = argv[++i];
                    continue;
                }
/*
                if (arg.equals("-x") ||
                    arg.equals("--xmlmodel")) {
                    if (argv.length - i < 2) {
                        printError("Missing argument to the -p/--properties option", null);
                        usage();
                    }
                    jdoXMLModelFileName = argv[++i];
                    continue;
                }
*/
                if (arg.length() > 0 && arg.charAt(0) == '-') {
                    printError("Unrecognized option:" + arg, null);
                    usage();
                }
                if (arg.length() == 0) {
                    printMessage("Ignoring empty command line argument.");
                    continue;
                }

                //inputFileNames.add(arg);
            }

            // The user must specify a destination directory
            if (jdoPropertiesFileName == null) {
                printError("No destination directory specified", null);
                usage();
            }

            // The user must specify a destination directory
            if (destinationDirectory == null) {
                printError("No destination directory specified", null);
                usage();
            }

            return 0;
        }
    }

    private void init()
        throws FileNotFoundException, IOException
    {
        // load the properties
        affirm(opts.jdoPropertiesFileName != null);
        meta = new EnhancerMetaDataPropertyImpl(out, opts.verbose,
                                                opts.jdoPropertiesFileName);

        // create the destination directory
        affirm(opts.destinationDirectory != null);
        final File destinationDir = new File(opts.destinationDirectory);
        boolean res = destinationDir.mkdirs();
        if (!res) {
            if (! destinationDir.exists()) {
                throw new IOException("unable to create destination directory: "
                                  + "'" + destinationDir + "'");
            }
        }
    }

    private void generate()
    {
        final String[] classes = meta.getKnownClasses();
        for (int i = 0; i < classes.length; i++) {
            final String classname = classes[i];
            try {
                if (classname.indexOf('$') != -1) {
                    printMessage("Skipping generation of nested class " + classname + "." +
                                 " Note, a nested ObjectId class is generated with its pc class.");
                    continue;
                }
                final Writer writer = createFileWriter(classname);
                this.writer.setWriter(writer);
                generateClass(classname);
                writer.close();
            } catch(IOException ex) {
                printError("Error generating class '" + classname + "'.", ex);
            }
        }
    }

    private void generateClass(final String classname)
        throws IOException
    {
        affirm(classname);
        
        final String normClassName = NameHelper.normalizeClassName(classname);
        printMessage("generating '" + normClassName + "'...");

        final String packageName = NameHelper.getPackageName(classname);
        writer.writePackage(
            packageName,
            null);

        writer.writeImports(
            null,
            null);
        
        // write the class header and key class
        final String oidClassName = meta.getKeyClass(classname);
        
        if ((oidClassName == null) || (ImplHelper.isSingleFieldIdentity(oidClassName))) {
            writeClassHeader(classname);
        } else {
            final String oidPackageName
                = NameHelper.getPackageName(oidClassName);
            affirm(packageName.equals(oidPackageName),
               "PC class and key class must be in same package.");

            final boolean enclosedOid
                = oidClassName.startsWith(classname + "$");
            if (enclosedOid) {
                writeClassHeader(classname);
                writeOidClass(classname, oidClassName, enclosedOid);
            } else {
                writeOidClass(classname, oidClassName, enclosedOid);
                writeClassHeader(classname);
            }
        }
        
        writeClassMembers(classname);

        // write the augmentation
        final boolean isPC = meta.isPersistenceCapableClass(classname);
        if (isPC) {
            final boolean isPCRoot
                = meta.isPersistenceCapableRootClass(classname);
            if (isPCRoot) {
                writePCRootMembers(classname);
            }
            writePCMembers(classname);
            
            writeClassMemberAccessors(classname);
        }

        writer.writeClassEnd();
    }

    private Writer createFileWriter(String classname)
        throws IOException
    {
        final File file = new File(opts.destinationDirectory,
                                   classname + ".java");
        file.getAbsoluteFile().getParentFile().mkdirs();
        return new BufferedWriter(new FileWriter(file));
    }

    private void writeClassHeader(final String classname)
        throws IOException
    {
        final boolean isPCRoot = meta.isPersistenceCapableRootClass(classname);
        final String superclass = meta.getSuperClass(classname);

        String[] interfaces = null;
        String[] comments = null;
        interfaces
            = new String[]{ ImplHelper.CLASSNAME_JDO_PERSISTENCE_CAPABLE };
        writer.writeClassHeader(meta.getClassModifiers(classname),
                                ImplHelper.getClassName(classname),
                                superclass,
                                interfaces,
                                comments);
    }

    private void writeClassMembers(final String classname)
        throws IOException
    {
        writer.writeComments(1, new String[]{
            "----------------------------------------------------------------------",
            "Class Members:",
            "----------------------------------------------------------------------"
        });
        writer.writeln();
        
        // write default constructor
        writer.writeConstructor(
            ImplHelper.getClassName(classname),
            Modifier.PUBLIC,
            null, null, null,
            ImplHelper.getDefaultConstructorImpl(),
            ImplHelper.COMMENT_NOT_ENHANCER_ADDED);

        // write default constructor
        writer.writeConstructor(
            ImplHelper.getClassName(classname),
            Modifier.PUBLIC,
            new String[]{ "str" },
            new String[]{ "String" },
            null,
            ImplHelper.getDummyConstructorImpl(),
            ImplHelper.COMMENT_NOT_ENHANCER_ADDED);

        final String[] fieldnames = meta.getKnownFields(classname);
        final int n = (fieldnames != null ? fieldnames.length : 0);
        
        // write the fields and with their bean getters/setters
        for (int i = 0; i < n; i++) {
            final String fieldname = (String)fieldnames[i];
            writeFieldMember(classname, fieldname);
        }
    }
    
    private void writeFieldMember(final String classname,
                                  final String fieldname)
        throws IOException
    {
        final String fieldtype = meta.getFieldType(classname, fieldname);
        final int access = meta.getFieldModifiers(classname, fieldname);
        final String normClassName = NameHelper.normalizeClassName(classname);
        final List impl = new ArrayList();

        // the field
        writer.writeField(
            fieldname,
            access,
            fieldtype,
            null, null);

        // do not write bean getters and setters for static fields
        if ((access & Modifier.STATIC) != 0) {
            return;
        }

        // write bean getter (calling accessor)
        impl.clear();
        impl.add("//return this." + fieldname + ';');
        final String accessor
            = ImplHelper.createJDOFieldAccessorName(classname, fieldname);
        impl.add("return " + normClassName + "." + accessor + "(this);");
        writer.writeMethod(
            createMethodName("get", fieldname),
            Modifier.PUBLIC,
            fieldtype,
            null,
            null,
            null,
            impl,
            ImplHelper.COMMENT_NOT_ENHANCER_ADDED);

        // write bean setter (calling mutator)
        impl.clear();
        impl.add("//this." + fieldname + " = " + fieldname + ';');
        final String mutator
            = ImplHelper.createJDOFieldMutatorName(classname, fieldname);
        impl.add(normClassName + "." + mutator + "(this, " + fieldname + ");");
        writer.writeMethod(
            createMethodName("set", fieldname),
            Modifier.PUBLIC,
            "void",
            new String[]{ fieldname },
            new String[]{ fieldtype },
            null,
            impl,
            ImplHelper.COMMENT_NOT_ENHANCER_ADDED);
    }

    private void writeClassMemberAccessors(final String classname)
        throws IOException
    {
        writer.writeComments(1, new String[]{
            "----------------------------------------------------------------------",
            "Augmentation for Field Accessors and Mutators (added by enhancer):",
            "----------------------------------------------------------------------"
        });
        writer.writeln();

        // write the fields and their access methods
        final String[] fields = meta.getManagedFields(classname);
        final int n = (fields != null ? fields.length : 0);
        for (int i = 0; i < n; i++) {
            final String fieldname = (String)fields[i];
            writeFieldAccessors(classname, fieldname);
        }
    }
    
    private void writeFieldAccessors(final String classname,
                                     final String fieldname)
        throws IOException
    {
        final String fieldtype
            = meta.getFieldType(classname, fieldname);
        final int fieldnumber
            = meta.getFieldNumber(classname, fieldname);
        final boolean dfg
            = meta.isDefaultFetchGroupField(classname, fieldname);
        final int access
            = meta.getFieldModifiers(classname, fieldname);
        final int flags
            = meta.getFieldFlags(classname, fieldname);

        final String accessor
            = ImplHelper.createJDOFieldAccessorName(classname, fieldname);
        final String mutator
            = ImplHelper.createJDOFieldMutatorName(classname, fieldname);

        final String instancename
            = "instance";
        
        // jdo accessor
        {
            affirm(((flags & meta.CHECK_READ) == 0)
                   | (flags & meta.MEDIATE_READ) == 0);
            final List impl;
            if ((flags & meta.CHECK_READ) != 0) {
                impl = ImplHelper.getJDOFieldCheckReadImpl(fieldname,
                                                           fieldtype,
                                                           fieldnumber,
                                                           instancename);
            } else if ((flags & meta.MEDIATE_READ) != 0) {
                impl = ImplHelper.getJDOFieldMediateReadImpl(fieldname,
                                                             fieldtype,
                                                             fieldnumber,
                                                             instancename);
            } else {
                impl = ImplHelper.getJDOFieldDirectReadImpl(fieldname,
                                                            fieldtype,
                                                            fieldnumber,
                                                            instancename);
            }
            writer.writeMethod(
                accessor,
                access | Modifier.STATIC | Modifier.FINAL,
                fieldtype,
                new String[]{ instancename },
                new String[]{ classname },
                null,
                impl,
                ImplHelper.COMMENT_ENHANCER_ADDED);
        }
        
        // jdo mutator
        {
            affirm(((flags & meta.CHECK_WRITE) == 0)
                   | (flags & meta.MEDIATE_WRITE) == 0);
            final List impl;
            if ((flags & meta.CHECK_WRITE) != 0) {
                impl = ImplHelper.getJDOFieldCheckWriteImpl(fieldname,
                                                            fieldtype,
                                                            fieldnumber,
                                                            instancename,
                                                            fieldname);
            } else if ((flags & meta.MEDIATE_WRITE) != 0) {
                impl = ImplHelper.getJDOFieldMediateWriteImpl(fieldname,
                                                              fieldtype,
                                                              fieldnumber,
                                                              instancename,
                                                              fieldname);
            } else {
                impl = ImplHelper.getJDOFieldDirectWriteImpl(fieldname,
                                                             fieldtype,
                                                             fieldnumber,
                                                             instancename,
                                                             fieldname);
            }
            writer.writeMethod(
                mutator,
                access | Modifier.STATIC | Modifier.FINAL,
                "void",
                new String[]{ instancename, fieldname },
                new String[]{ classname, fieldtype },
                null,
                impl,
                ImplHelper.COMMENT_ENHANCER_ADDED);
        }
    }
    
    private void writePCRootMembers(final String classname)
        throws IOException
    {
        writer.writeComments(1, new String[]{
            "----------------------------------------------------------------------",
            "Augmentation for Persistence-Capable Root Classes (added by enhancer):",
            "----------------------------------------------------------------------"
        });
        writer.writeln();

        // jdoStateManager
        writer.writeField(
            ImplHelper.FIELDNAME_JDO_STATE_MANAGER,
            Modifier.PROTECTED | Modifier.TRANSIENT,
            ImplHelper.CLASSNAME_JDO_STATE_MANAGER,
            "null",
            ImplHelper.COMMENT_ENHANCER_ADDED);

        // jdoFlags
        writer.writeField(
            ImplHelper.FIELDNAME_JDO_FLAGS,
            Modifier.PROTECTED | Modifier.TRANSIENT,
            "byte",
            "0", // (ImplHelper.CLASSNAME_JDO_PERSISTENCE_CAPABLE
            //       + "." + "READ_WRITE_OK"),
            ImplHelper.COMMENT_ENHANCER_ADDED);

        // jdoReplaceStateManager
        writer.writeMethod(
            ImplHelper.METHODNAME_JDO_REPLACE_STATE_MANAGER,
            Modifier.PUBLIC | Modifier.FINAL | Modifier.SYNCHRONIZED,
            "void",
            new String[]{ "sm" },
            new String[]{ ImplHelper.CLASSNAME_JDO_STATE_MANAGER },
            null,
            ImplHelper.getJDOReplaceStateManagerImpl("sm"),
            ImplHelper.COMMENT_ENHANCER_ADDED);
        
        // jdoReplaceFlags
        writer.writeMethod(
            ImplHelper.METHODNAME_JDO_REPLACE_FLAGS,
            Modifier.PUBLIC | Modifier.FINAL,
            "void", null, null, null,
            ImplHelper.getJDOReplaceFlagsImpl(),
            ImplHelper.COMMENT_ENHANCER_ADDED);

        // getPersistenceManager
        writer.writeMethod(
            ImplHelper.METHODNAME_JDO_GET_PERSISTENCE_MANAGER,
            Modifier.PUBLIC | Modifier.FINAL,
            ImplHelper.CLASSNAME_JDO_PERSISTENCE_MANAGER, null, null, null,
            ImplHelper.getJDOStateManagerObjectDelegationImpl("getPersistenceManager(this)"),
            ImplHelper.COMMENT_ENHANCER_ADDED);
        
        // getObjectId
        writer.writeMethod(
            ImplHelper.METHODNAME_JDO_GET_OBJECT_ID,
            Modifier.PUBLIC | Modifier.FINAL,
            Object.class.getName(), null, null, null,
            ImplHelper.getJDOStateManagerObjectDelegationImpl("getObjectId(this)"),
            ImplHelper.COMMENT_ENHANCER_ADDED);

        writer.writeMethod(
            ImplHelper.METHODNAME_JDO_GET_TRANSACTIONAL_OBJECT_ID,
            Modifier.PUBLIC | Modifier.FINAL,
            Object.class.getName(), null, null, null,
            ImplHelper.getJDOStateManagerObjectDelegationImpl("getTransactionalObjectId(this)"),
            ImplHelper.COMMENT_ENHANCER_ADDED);

        // is-methods
        writer.writeMethod(
            ImplHelper.METHODNAME_JDO_IS_PERSISTENT,
            Modifier.PUBLIC | Modifier.FINAL,
            "boolean", null, null, null,
            ImplHelper.getJDOStateManagerBooleanDelegationImpl("isPersistent(this)"),
            ImplHelper.COMMENT_ENHANCER_ADDED);

        writer.writeMethod(
            ImplHelper.METHODNAME_JDO_IS_TRANSACTIONAL,
            Modifier.PUBLIC | Modifier.FINAL,
            "boolean", null, null, null,
            ImplHelper.getJDOStateManagerBooleanDelegationImpl("isTransactional(this)"),
            ImplHelper.COMMENT_ENHANCER_ADDED);

        writer.writeMethod(
            ImplHelper.METHODNAME_JDO_IS_NEW,
            Modifier.PUBLIC | Modifier.FINAL,
            "boolean", null, null, null,
            ImplHelper.getJDOStateManagerBooleanDelegationImpl("isNew(this)"),
            ImplHelper.COMMENT_ENHANCER_ADDED);

        writer.writeMethod(
            ImplHelper.METHODNAME_JDO_IS_DELETED,
            Modifier.PUBLIC | Modifier.FINAL,
            "boolean", null, null, null,
            ImplHelper.getJDOStateManagerBooleanDelegationImpl("isDeleted(this)"),
            ImplHelper.COMMENT_ENHANCER_ADDED);

        writer.writeMethod(
            ImplHelper.METHODNAME_JDO_IS_DIRTY,
            Modifier.PUBLIC | Modifier.FINAL,
            "boolean", null, null, null,
            ImplHelper.getJDOStateManagerBooleanDelegationImpl("isDirty(this)"),
            ImplHelper.COMMENT_ENHANCER_ADDED);

        // makeDirty
        writer.writeMethod(
            ImplHelper.METHODNAME_JDO_MAKE_DIRTY,
            Modifier.PUBLIC | Modifier.FINAL,
            "void",
            new String[]{ "fieldname" },
            new String[]{ String.class.getName() },
            null,
            ImplHelper.getJDOStateManagerVoidDelegationImpl("makeDirty(this, fieldname)"),
            ImplHelper.COMMENT_ENHANCER_ADDED);

        // replaceFields
        writer.writeMethod(
            ImplHelper.METHODNAME_JDO_REPLACE_FIELDS,
            Modifier.PUBLIC | Modifier.FINAL,
            "void",
            new String[]{ "fieldnumbers" },
            new String[]{ "int[]" },
            null,
            ImplHelper.getJDOFieldIterationImpl("fieldnumbers",
                                                ImplHelper.METHODNAME_JDO_REPLACE_FIELD),
            ImplHelper.COMMENT_ENHANCER_ADDED);

        // provideFields
        writer.writeMethod(
            ImplHelper.METHODNAME_JDO_PROVIDE_FIELDS,
            Modifier.PUBLIC | Modifier.FINAL,
            "void",
            new String[]{ "fieldnumbers" },
            new String[]{ "int[]" },
            null,
            ImplHelper.getJDOFieldIterationImpl("fieldnumbers",
                                                ImplHelper.METHODNAME_JDO_PROVIDE_FIELD),
            ImplHelper.COMMENT_ENHANCER_ADDED);

        // preSerialize
        writer.writeMethod(
            ImplHelper.METHODNAME_JDO_PRE_SERIALIZE,
            Modifier.PROTECTED | Modifier.FINAL,
            "void", null, null, null,
            ImplHelper.getJDOStateManagerVoidDelegationImpl("preSerialize(this)"),
            ImplHelper.COMMENT_ENHANCER_ADDED);

        // write method clone()
        writer.writeMethod(
            "clone",
            Modifier.PUBLIC,
            "Object",
            null,
            null,
            new String[]{ "java.lang.CloneNotSupportedException" },
            ImplHelper.getCloneImpl(classname),
            ImplHelper.COMMENT_NOT_ENHANCER_ADDED);

    }

    private void writePCMembers(final String classname)
        throws IOException
    {
        writer.writeComments(1, new String[]{
            "----------------------------------------------------------------------",
            "Augmentation for Persistence-Capable Classes (added by enhancer):",
            "----------------------------------------------------------------------"
        });
        writer.writeln();
        
        final String[] managedFieldNames
            = meta.getManagedFields(classname);
        final String[] managedFieldTypes
            = meta.getFieldType(classname, managedFieldNames);
        final boolean isPCRoot
            = meta.isPersistenceCapableRootClass(classname);

        writePCStaticMembers(classname);

        // jdoNewInstance
        writer.writeMethod(
            ImplHelper.METHODNAME_JDO_NEW_INSTANCE,
            Modifier.PUBLIC,
            ImplHelper.CLASSNAME_JDO_PERSISTENCE_CAPABLE,
            new String[]{ "sm" },
            new String[]{ ImplHelper.CLASSNAME_JDO_STATE_MANAGER },
            null,
            ImplHelper.getJDONewInstanceImpl(classname,
                                             "sm"),
            ImplHelper.COMMENT_ENHANCER_ADDED);

        // jdoNewInstance
        writer.writeMethod(
            ImplHelper.METHODNAME_JDO_NEW_INSTANCE,
            Modifier.PUBLIC,
            ImplHelper.CLASSNAME_JDO_PERSISTENCE_CAPABLE,
            new String[]{ "sm", "oid" },
            new String[]{ ImplHelper.CLASSNAME_JDO_STATE_MANAGER, "Object" },
            null,
            ImplHelper.getJDONewInstanceKeyImpl(classname,
                                                //oidClassName,
                                                "sm",
                                                "oid"),
                                                //keyFieldNames),
            ImplHelper.COMMENT_ENHANCER_ADDED);

        // jdoReplaceField
        writer.writeMethod(
            ImplHelper.METHODNAME_JDO_REPLACE_FIELD,
            Modifier.PUBLIC,
            "void",
            new String[]{ "fieldnumber" },
            new String[]{ "int" },
            null,
            ImplHelper.getJDOReplaceFieldImpl("fieldnumber",
                                              isPCRoot,
                                              managedFieldNames,
                                              managedFieldTypes),
            ImplHelper.COMMENT_ENHANCER_ADDED);

        // jdoProvideField(s)
        writer.writeMethod(
            ImplHelper.METHODNAME_JDO_PROVIDE_FIELD,
                           Modifier.PUBLIC,
            "void",
            new String[]{ "fieldnumber" },
            new String[]{ "int" },
            null,
            ImplHelper.getJDOProvideFieldImpl("fieldnumber",
                                              isPCRoot,
                                              managedFieldNames,
                                              managedFieldTypes),
            ImplHelper.COMMENT_ENHANCER_ADDED);

        // jdoCopyFields
        writer.writeMethod(
            ImplHelper.METHODNAME_JDO_COPY_FIELDS,
            Modifier.PUBLIC,
            "void",
            new String[]{ "pc", "fieldnumbers" },
            new String[]{ Object.class.getName(), "int[]" },
            null,
            ImplHelper.getJDOCopyFieldsImpl(classname,
                                            "pc",
                                            "fieldnumbers"),
            ImplHelper.COMMENT_ENHANCER_ADDED);

        // jdoCopyField
        writer.writeMethod(
            ImplHelper.METHODNAME_JDO_COPY_FIELD,
            Modifier.PROTECTED | Modifier.FINAL,
            "void",
            new String[]{ "pc", "fieldnumber" },
            new String[]{ classname, "int" },
            null,
            ImplHelper.getJDOCopyFieldImpl(classname,
                                           "pc",
                                           "fieldnumber",
                                           managedFieldNames,
                                           isPCRoot),
            ImplHelper.COMMENT_ENHANCER_ADDED);

        writePCKeyHandlingMembers(classname);

        writePCSerializationMembers(classname);
    }

    private void writePCStaticMembers(final String classname)
        throws IOException
    {
        final String[] managedFieldNames
            = meta.getManagedFields(classname);
        final String superPC
            = meta.getPersistenceCapableSuperClass(classname);
        final String[] managedFieldTypes
            = meta.getFieldType(classname, managedFieldNames);
        final int[] managedFieldFlags
            = meta.getFieldFlags(classname, managedFieldNames);
        final boolean isPCRoot 
            = meta.isPersistenceCapableRootClass(classname);

        // inheritedFieldCount
        writer.writeField(
            ImplHelper.FIELDNAME_JDO_INHERITED_FIELD_COUNT,
            Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL,
            "int",
            null,
            ImplHelper.COMMENT_ENHANCER_ADDED);

        // fieldNames
        writer.writeField(
            ImplHelper.FIELDNAME_JDO_FIELD_NAMES,
            Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL,
            "String[]",
            null,
            ImplHelper.COMMENT_ENHANCER_ADDED);

        // fieldTypes
        writer.writeField(
            ImplHelper.FIELDNAME_JDO_FIELD_TYPES,
            Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL,
            "Class[]",
            null,
            ImplHelper.COMMENT_ENHANCER_ADDED);

        // fieldFlags
        writer.writeField(
            ImplHelper.FIELDNAME_JDO_FIELD_FLAGS,
            Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL,
            "byte[]",
            null,
            ImplHelper.COMMENT_ENHANCER_ADDED);

        // PC superclass
        writer.writeField(
            ImplHelper.FIELDNAME_JDO_PC_SUPERCLASS,
            Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL,
            "Class",
            null,
            ImplHelper.COMMENT_ENHANCER_ADDED);

        // static initializer
        writer.writeStaticInitializer(
            ImplHelper.getStaticInitializerImpl(classname,
                                                superPC,
                                                managedFieldNames,
                                                managedFieldTypes,
                                                managedFieldFlags),
            ImplHelper.COMMENT_ENHANCER_ADDED);

        // jdoGetManagedFieldCount
        writer.writeMethod(
            ImplHelper.METHODNAME_JDO_GET_MANAGED_FIELD_COUNT,
            Modifier.PROTECTED | Modifier.STATIC,
            "int", null, null, null,
            ImplHelper.getJDOGetManagedFieldCountImpl(
                isPCRoot, superPC, managedFieldNames.length),
            ImplHelper.COMMENT_ENHANCER_ADDED);
    }

    private void writePCKeyHandlingMembers(final String classname)
        throws IOException
    {
        final boolean isPCRoot
            = meta.isPersistenceCapableRootClass(classname);
        final String oidClassName
            = NameHelper.normalizeClassName(meta.getKeyClass(classname));


        // generate these methods if this is the PC root class or if
        // there's a key class definition
        if (!isPCRoot && oidClassName == null) {
            return;
        }

        final String superOidClassName
            = NameHelper.normalizeClassName(meta.getSuperKeyClass(classname));
        final String[] keyFieldNames
            = meta.getKeyFields(classname);
        final String[] keyFieldTypes
            = meta.getFieldType(classname, keyFieldNames);
        final int[] keyFieldNumbers
            = meta.getFieldNumber(classname, keyFieldNames);

        // jdoNewOidInstance
        writer.writeMethod(
            ImplHelper.METHODNAME_JDO_NEW_OID_INSTANCE,
            Modifier.PUBLIC,
            Object.class.getName(), null, null, null,
            ImplHelper.getJDONewOidInstanceImpl(oidClassName),
            ImplHelper.COMMENT_ENHANCER_ADDED);

        writer.writeMethod(
            ImplHelper.METHODNAME_JDO_NEW_OID_INSTANCE,
            Modifier.PUBLIC,
            Object.class.getName(),
            new String[]{ "str" },
            new String[]{ "String" },
            null,
            ImplHelper.getJDONewOidInstanceImpl(oidClassName,
                                                "str"),
            ImplHelper.COMMENT_ENHANCER_ADDED);

        writer.writeMethod(
            ImplHelper.METHODNAME_JDO_NEW_OID_INSTANCE,
            Modifier.PUBLIC,
            Object.class.getName(),
            new String[]{ "obj" },
            new String[]{ "Object" },
            null,
            ImplHelper.getJDONewOidInstanceImpl(classname, oidClassName,
                "obj", keyFieldNames, keyFieldTypes, keyFieldNumbers),
            ImplHelper.COMMENT_ENHANCER_ADDED);
        

        // jdoCopyKeyFieldsTo/FromOid
        writer.writeMethod(
            ImplHelper.METHODNAME_JDO_COPY_KEY_FIELDS_TO_OID,
            Modifier.PUBLIC,
            "void",
            new String[]{ "oid" },
            new String[]{ "Object" },
            null,
            ImplHelper.getJDOCopyKeyFieldsToOid(oidClassName,
                                                superOidClassName,
                                                "oid",
                                                keyFieldNames),
            ImplHelper.COMMENT_ENHANCER_ADDED);

        writer.writeMethod(
            ImplHelper.METHODNAME_JDO_COPY_KEY_FIELDS_FROM_OID,
            Modifier.PROTECTED,
            "void",
            new String[]{ "oid" },
            new String[]{ "Object" },
            null,
            ImplHelper.getJDOCopyKeyFieldsFromOid(oidClassName,
                                                  superOidClassName,
                                                  "oid",
                                                  keyFieldNames),
            ImplHelper.COMMENT_ENHANCER_ADDED);

        writer.writeMethod(
            ImplHelper.METHODNAME_JDO_COPY_KEY_FIELDS_TO_OID,
            Modifier.PUBLIC,
            "void",
            new String[]{ "ofs", "oid" },
            new String[]{ ImplHelper.CLASSNAME_JDO_OBJECT_ID_FIELD_SUPPLIER,
                          "Object" },
            null,
            ImplHelper.getJDOCopyKeyFieldsToOid(oidClassName,
                                                superOidClassName,
                                                "ofs",
                                                "oid",
                                                keyFieldNames,
                                                keyFieldTypes,
                                                keyFieldNumbers),
            ImplHelper.COMMENT_ENHANCER_ADDED);

        writer.writeMethod(
            ImplHelper.METHODNAME_JDO_COPY_KEY_FIELDS_FROM_OID,
            Modifier.PUBLIC,
            "void",
            new String[]{ "ofc", "oid" },
            new String[]{ ImplHelper.CLASSNAME_JDO_OBJECT_ID_FIELD_CONSUMER,
                          "Object" },
            null,
            ImplHelper.getJDOCopyKeyFieldsFromOid(oidClassName,
                                                  superOidClassName,
                                                  "ofc",
                                                  "oid",
                                                  keyFieldNames,
                                                  keyFieldTypes,
                                                  keyFieldNumbers),
            ImplHelper.COMMENT_ENHANCER_ADDED);
    }

    private void writePCSerializationMembers(final String classname)
        throws IOException
    {
        final long serialUID
            = createJDOVersionUID(classname);

        //^olsen: to adapt
        writer.writeField(
            ImplHelper.FIELDNAME_SERIAL_VERSION_UID,
            Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL,
            "long",
            ImplHelper.getSerialVersionUIDInitValue(serialUID),
            new String[]{ "only a dummy value yet"});
        //ImplHelper.COMMENT_ENHANCER_ADDED);

        writer.writeMethod(
            ImplHelper.METHODNAME_WRITE_OBJECT,
            Modifier.PRIVATE,
            "void",
            new String[]{ "out" },
            new String[]{ ObjectOutputStream.class.getName() },
            new String[]{ IOException.class.getName() },
            ImplHelper.getWriteObjectImpl("out"),
            ImplHelper.COMMENT_ENHANCER_ADDED);
    }

    private void writeOidClass(final String classname,
                               final String oidClassName,
                               final boolean enclosedOid)
        throws IOException
    {
        final int indent = (enclosedOid ? 1 : 0);
        writer.writeComments(indent, new String[]{
            "----------------------------------------------------------------------",
            "Key Class:",
            "----------------------------------------------------------------------"
        });
        writer.writeln();

        writer.setInitialIndents(indent);

        final String superOidClassName
            = NameHelper.normalizeClassName(meta.getSuperKeyClass(classname));

        writer.writeClassHeader(
            (enclosedOid ? Modifier.PUBLIC | Modifier.STATIC : 0),
            oidClassName,
            superOidClassName,
            new String[]{ Serializable.class.getName() },
            ImplHelper.COMMENT_NOT_ENHANCER_ADDED);

        final boolean isPCRoot
            = meta.isPersistenceCapableRootClass(classname);

        final String[] pknames = meta.getKeyFields(classname);
        final String[] pktypes = meta.getFieldType(classname, pknames);

        // write the PK-fields
        for (int i = 0; i < pknames.length; i++) {
            writer.writeField(
                pknames[i],
                Modifier.PUBLIC,
                pktypes[i],
                null,
                null);
        }

        // write default constructor
        writer.writeConstructor(
            NameHelper.getClassName(oidClassName),
            Modifier.PUBLIC,
            null, null, null,
            ImplHelper.getDefaultConstructorImpl(),
            ImplHelper.COMMENT_NOT_ENHANCER_ADDED);

        // write string argument constructor
        writer.writeConstructor(
            NameHelper.getClassName(oidClassName),
            Modifier.PUBLIC,
            new String[]{ "str" },
            new String[]{ "String" },
            null,
            ImplHelper.getOidStringArgConstructorImpl(superOidClassName,
                                                      "str"),
            ImplHelper.COMMENT_NOT_ENHANCER_ADDED);

        // hashCode
        writer.writeMethod(
            "hashCode",
            Modifier.PUBLIC,
            "int",
            null,
            null,
            null,
            ImplHelper.getOidHashCodeImpl(pknames,
                                          pktypes,
                                          isPCRoot),
            ImplHelper.COMMENT_NOT_ENHANCER_ADDED);

        // equals
        writer.writeMethod(
            "equals", Modifier.PUBLIC, "boolean",
            new String[]{ "pk" },
            new String[]{ Object.class.getName() },
            null,
            ImplHelper.getOidEqualsImpl(oidClassName,
                                        pknames,
                                        pktypes,
                                        "pk",
                                        isPCRoot),
            ImplHelper.COMMENT_NOT_ENHANCER_ADDED);

        writer.writeClassEnd();
        writer.setInitialIndents(0);
    }

    //^olsen to adapt
    static private long createJDOVersionUID(final String classname)
    {
        return classname.hashCode();
    }

    static private String createMethodName(final String prefix,
                                           final String fieldname)
    {
        return (prefix + Character.toUpperCase(fieldname.charAt(0))
                + fieldname.substring(1));
    }

    private void printMessage(String msg)
    {
        out.println(msg);
    }

    private void printError(String    msg,
                                   Throwable ex)
    {
        if (msg != null) {
            err.println(msg + (ex != null ? ": " + ex.getMessage() : ""));
        }
        if (ex != null) {
            ex.printStackTrace(err);
        }
    }
}
