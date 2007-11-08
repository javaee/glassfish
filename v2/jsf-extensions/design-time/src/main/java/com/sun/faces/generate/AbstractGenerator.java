/*
 * $Id: AbstractGenerator.java,v 1.1 2005/09/20 21:11:37 edburns Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at
 * https://javaserverfaces.dev.java.net/CDDL.html or
 * legal/CDDLv1.0.txt. 
 * See the License for the specific language governing
 * permission and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at legal/CDDLv1.0.txt.    
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * [Name of File] [ver.__] [Date]
 * 
 * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.faces.generate;


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;


/**
 * <p>Abstract base class for Java and TLD generators.</p>
 *
 * <p>The methods in this class presume the following command line option
 * names and corresponding values:</p>
 * <ul>
 * </ul>
 */

public abstract class AbstractGenerator implements Generator {


    // -------------------------------------------------------- Static Variables


    // The set of default values for primitives, keyed by the primitive type
    protected static final Map<String,String> TYPE_DEFAULTS = new HashMap<String, String>();
    static {
        TYPE_DEFAULTS.put("boolean", "false");
        TYPE_DEFAULTS.put("byte", "Byte.MIN_VALUE");
        TYPE_DEFAULTS.put("char", "Character.MIN_VALUE");
        TYPE_DEFAULTS.put("double", "Double.MIN_VALUE");
        TYPE_DEFAULTS.put("float", "Float.MIN_VALUE");
        TYPE_DEFAULTS.put("int", "Integer.MIN_VALUE");
        TYPE_DEFAULTS.put("long", "Long.MIN_VALUE");
        TYPE_DEFAULTS.put("short", "Short.MIN_VALUE");
    }


    // The set of reserved keywords in the Java language
    protected static final Set<String> JAVA_KEYWORDS = new HashSet<String>();
    static {
        JAVA_KEYWORDS.add("abstract");
        JAVA_KEYWORDS.add("boolean");
        JAVA_KEYWORDS.add("break");
        JAVA_KEYWORDS.add("byte");
        JAVA_KEYWORDS.add("case");
        JAVA_KEYWORDS.add("cast");
        JAVA_KEYWORDS.add("catch");
        JAVA_KEYWORDS.add("char");
        JAVA_KEYWORDS.add("class");
        JAVA_KEYWORDS.add("const");
        JAVA_KEYWORDS.add("continue");
        JAVA_KEYWORDS.add("default");
        JAVA_KEYWORDS.add("do");
        JAVA_KEYWORDS.add("double");
        JAVA_KEYWORDS.add("else");
        JAVA_KEYWORDS.add("enum");
        JAVA_KEYWORDS.add("extends");
        JAVA_KEYWORDS.add("final");
        JAVA_KEYWORDS.add("finally");
        JAVA_KEYWORDS.add("float");
        JAVA_KEYWORDS.add("for");
        JAVA_KEYWORDS.add("future");
        JAVA_KEYWORDS.add("generic");
        JAVA_KEYWORDS.add("goto");
        JAVA_KEYWORDS.add("if");
        JAVA_KEYWORDS.add("implements");
        JAVA_KEYWORDS.add("import");
        JAVA_KEYWORDS.add("inner");
        JAVA_KEYWORDS.add("instanceof");
        JAVA_KEYWORDS.add("int");
        JAVA_KEYWORDS.add("interface");
        JAVA_KEYWORDS.add("long");
        JAVA_KEYWORDS.add("native");
        JAVA_KEYWORDS.add("new");
        JAVA_KEYWORDS.add("null");
        JAVA_KEYWORDS.add("operator");
        JAVA_KEYWORDS.add("outer");
        JAVA_KEYWORDS.add("package");
        JAVA_KEYWORDS.add("private");
        JAVA_KEYWORDS.add("protected");
        JAVA_KEYWORDS.add("public");
        JAVA_KEYWORDS.add("rest");
        JAVA_KEYWORDS.add("return");
        JAVA_KEYWORDS.add("short");
        JAVA_KEYWORDS.add("static");
        JAVA_KEYWORDS.add("strictfp");
        JAVA_KEYWORDS.add("super");
        JAVA_KEYWORDS.add("switch");
        JAVA_KEYWORDS.add("synchronized");
        JAVA_KEYWORDS.add("this");
        JAVA_KEYWORDS.add("throw");
        JAVA_KEYWORDS.add("throws");
        JAVA_KEYWORDS.add("transient");
        JAVA_KEYWORDS.add("try");
        JAVA_KEYWORDS.add("var");
        JAVA_KEYWORDS.add("void");
        JAVA_KEYWORDS.add("volatile");
        JAVA_KEYWORDS.add("while");
    }


    // ------------------------------------------------------- Protected Methods


    /**
     * <p>Return the capitalized version of the specified property name.</p>
     *
     * @param name Uncapitalized property name
     */
    protected static String capitalize(String name) {

        return (Character.toUpperCase(name.charAt(0)) + name.substring(1));

    }    


    /**
     * <p>Return a mangled version of the specified name if it conflicts with
     * a Java keyword; otherwise, return the specified name unchanged.</p>
     *
     * @param name Name to be potentially mangled
     */
    protected static String mangle(String name) {

        if (JAVA_KEYWORDS.contains(name)) {
            return ('_' + name);
        } else {
            return (name);
        }

    }


    /**
     * <p>Parse the command line options into a <code>Map</code>.</p>
     *
     * @param args Command line arguments passed to this program
     *
     * @exception IllegalArgumentException if an option flag does not start
     *  with a '-' or is missing a corresponding value
     */
    protected static Map<String,String> options(String[] args) {

        Map<String,String> options = new HashMap<String, String>();
        int i = 0;
        while (i < args.length) {
            if (!args[i].startsWith("-")) {
                throw new IllegalArgumentException
                    ("Invalid option name '" + args[i] + '\'');
            } else if ((i + 1) >= args.length) {
                throw new IllegalArgumentException
                    ("Missing value for option '" + args[i] + '\'');
            }
            options.put(args[i], args[i+1]);
            i += 2;
        }
        return (options);

    }


    /**
     * <p>Return <code>true</code> if the specified type is a primitive.</p>
     *
     * @param type Type to be tested
     */
    protected static boolean primitive(String type) {

        return ((GeneratorUtil.convertToPrimitive(type) != null));

    }


    /**
     * <p>Return the short class name from the specified (potentially fully
     * qualified) class name.  If the specified name has no periods, the
     * input value is returned unchanged.</p>
     *
     * @param className Class name that is optionally fully qualified
     */
    protected static String shortName(String className) {

        int index = className.lastIndexOf('.');
        if (index >= 0) {
            return (className.substring(index + 1));
        } else {
            return (className);
        }

    }


    // ----------------------------------------------------------- Inner Classes


    protected static class CodeWriter extends BufferedWriter {

        private final String TAB = "    ";
        private final int TAB_LENGTH = TAB.length();

        private Stack<String> depth;
        private String formatString = "";


        // -------------------------------------------------------- Constructors

        public CodeWriter(Writer writer) {

            super(writer);
            depth = new Stack<String>();

        } // END CodeWriter


        public void indent() {

            depth.push(TAB);
            updateFormatString(depth.size());


        } // END indent

        public void outdent() {

            depth.pop();
            updateFormatString(depth.size());

        } // END outdent


        public void fwrite(String str) throws IOException {

            super.write(formatString + str);

        } // END write


        public void writePackage(String packageName) throws IOException {

            fwrite(new StringBuffer("package ").append(packageName).
                append(";\n").toString());

        } // END writePackage


        public void writeImport(String fullyQualifiedClassName)
        throws IOException {

            fwrite(new StringBuffer("import ").
                append(fullyQualifiedClassName).append(";\n").
                toString());

        } // END writeImport
        
       
        public void writePublicClassDeclaration(String className,
                                                String extendsClass,
                                                String[] implementsClasses,
                                                boolean isAbstract,
                                                boolean isFinal)
        throws IOException {

            if (isAbstract && isFinal) {
                throw new IllegalArgumentException("Cannot have a class" +
                    " declaration be both abstract and final.");
            }

            StringBuffer sb = new StringBuffer("public");
            if (isAbstract) {
                sb.append(" abstract");
            }

            if (isFinal) {
                sb.append(" final");
            }

            sb.append(" class ").append(className);

            if (extendsClass != null && extendsClass.length() > 0) {
                sb.append(" extends ").append(extendsClass);
            }

            if (implementsClasses != null && implementsClasses.length > 0) {
                sb.append(" implements ");
                for (int i = 0; i < implementsClasses.length; i++) {
                    sb.append(implementsClasses[i]);
                    if (i < implementsClasses.length) {
                        sb.append(", ");
                    }
                }
            }

            sb.append(" {\n\n");
            fwrite(sb.toString());

        } // END writePublicClassDeclaration


        public void writeJavadocComment(String str) throws IOException {

            fwrite("/**\n");
            String[] tokens = str.split("\r|\n|\t");
            for (int i = 0; i < tokens.length; i++) {
                fwrite(" * ");
                write(tokens[i].trim());
                write('\n');
            }
            fwrite(" */\n");

        } // END writeJavadocComment


        public void writeLineComment(String str) throws IOException {

            String[] tokens = str.split("\r|\n|\t");
            for (int i = 0; i < tokens.length; i++) {
                fwrite("// ");
                write(tokens[i].trim());
                write('\n');
            }           

        } // END writeLineComment


        public void writeBlockComment(String str) throws IOException {

            fwrite("/*\n");
            String[] tokens = str.split("\r|\n|\t");
            for (int i = 0; i < tokens.length; i++) {
                fwrite(" * ");
                write(tokens[i].trim());
                write('\n');
            }
            fwrite(" */\n");

        } // END writeBlockComment


        public void writeReadWriteProperty(String propertyName, String type,
                                           String defaultValue)
        throws IOException {

            String iVarName = mangle(propertyName);
            String methodName = capitalize(propertyName);
            writeLineComment("PROPERTY: " + propertyName);
            fwrite("private " + type + ' ' + iVarName +
                (defaultValue == null ? ";" : " = " + defaultValue) + '\n');
            fwrite("public void set" + methodName +
                '(' + type + ' ' + iVarName + ") {\n");
            indent();
            fwrite("this." + iVarName + " = " + iVarName +
                ";\n");
            outdent();
            fwrite("}\n\n");
            fwrite("public " + type + "get" + methodName + "() {\n");
            indent();
            fwrite("return this." + iVarName + ";\n");
            outdent();
            fwrite("}\n\n");

        } // END writeReadWriteProperty


        public void writeReadWriteProperty(String propertyName, String type)
        throws IOException {

            writeReadWriteProperty(propertyName, type, null);

        } // END writeReadWriteProperty


        public void writeReadOnlyProperty(String propertyName, String type,
                                          String defaultValue)
        throws IOException {

            writeLineComment("PROPERTY: " + propertyName);
            String iVarName = mangle(propertyName);
            fwrite("private " + type + ' ' + iVarName +
                (defaultValue == null ? ";" : " = " + defaultValue) + '\n');
            fwrite("public " + type + "get" + capitalize(propertyName) +
                "() {\n");
            indent();
            fwrite("return this." + iVarName + ";\n");
            outdent();
            fwrite("}\n\n");

        } // END writeReadOnlyProperty


        public void writeReadOnlyProperty(String propertyName, String type)
        throws IOException {

            writeReadOnlyProperty(propertyName, type, null);

        } // END writeReadOnlyProperty


        public void writeWriteOnlyProperty(String propertyName, String type,
                                           String defaultValue)
        throws IOException {

            writeLineComment("PROPERTY: " + propertyName);
            String iVarName = mangle(propertyName);
            fwrite("private " + type + ' ' + iVarName +
		   (defaultValue == null ? ";" : " = " + defaultValue + ";") 
		   + '\n');
            fwrite("public void set" + capitalize(propertyName) +
                '(' + type + ' ' + iVarName + ") {\n");
            indent();
            fwrite("this." + iVarName + " = " + iVarName + ";\n");
            outdent();
            fwrite("}\n\n");

        } // END writeWriteOnlyProperty


        public void writeWriteOnlyProperty(String propertyName, String type)
        throws IOException {

            writeWriteOnlyProperty(propertyName, type, null);

        } // END writeWriteOnlyProperty


        // ----------------------------------------------------- Private Methods


        private void updateFormatString(int numTabs) {

            if (numTabs == 0) {
                formatString = "";
            } else {
                StringBuffer sb = new StringBuffer(numTabs * TAB_LENGTH);
                for (int i = 0; i < numTabs; i++) {
                    sb.append(TAB);
                }
                formatString = sb.toString();
            }

        } // END updateFormatString

    }
}
