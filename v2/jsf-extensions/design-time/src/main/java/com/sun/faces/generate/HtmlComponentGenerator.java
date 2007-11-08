/*
 * $Id: HtmlComponentGenerator.java,v 1.1 2005/09/20 21:11:37 edburns Exp $
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


import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import com.sun.faces.config.beans.ComponentBean;
import com.sun.faces.config.beans.DescriptionBean;
import com.sun.faces.config.beans.FacesConfigBean;
import com.sun.faces.config.beans.PropertyBean;
import com.sun.faces.util.ToolsUtil;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * <p>Generate concrete HTML component classes.  Classes will be generated for
 * each <code>&lt;component&gt;</code> element in the specified configuration
 * file whose absolute class name is in package <code>javax.faces.component.html</code>.</p>
 * <p/>
 * <p>This application requires the following command line options:</p> <ul>
 * <li><strong>--config</strong> Absolute pathname to an input configuration
 * file that will be parsed by the <code>parse()</code> method.</li>
 * <li><strong>--copyright</strong> Absolute pathname to a file containing the
 * copyright material for the top of each Java source file.</li>
 * <li><strong>--dir</strong> Absolute pathname to the directory into which
 * generated Java source code will be created.</li> <li><strong>--dtd</strong>
 * Pipe delimited list of public identifiers and absolute pathnames to files
 * containing the DTDs used to validate the input configuration files.
 * PRECONDITION: The list is the sequence: <public id>|<dtd path>|<public
 * id>|<dtd path>...</li> </ul>
 */

public class HtmlComponentGenerator extends AbstractGenerator {


    // -------------------------------------------------------- Static Variables


    private static final Logger logger = Logger.getLogger(ToolsUtil.FACES_LOGGER +
            ToolsUtil.GENERATE_LOGGER, ToolsUtil.TOOLS_LOG_STRINGS);    

    // The component configuration bean for the component class to be generated
    private ComponentBean cb;


    // Base object of the configuration beans
    private FacesConfigBean configBean;

    // List of relevant properties for the component class to be generated
    private List<PropertyBean> properties;

    // The Writer for each component class to be generated
    private CodeWriter writer;

    private PropertyManager propManager;


    // ------------------------------------------------------------ Constructors


    public HtmlComponentGenerator(PropertyManager propManager) {

        this.propManager = propManager;

    }


    // ---------------------------------------------------------- Public Methods


    /**
     * <p>Generate the concrete HTML component class based on the current
     * component configuration bean.</p>
     */
    public void generate(FacesConfigBean configBean) {

        this.configBean = configBean;

        try {
            generateClasses();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    // ---------------------------------------------------------- Private Method

    private void generateClasses() throws Exception {

        final String compPackage = "javax.faces.component.html";

        // Component generator doesn't use the TARGET_PACKAGE property
        String packagePath = compPackage.replace('.', File.separatorChar);
        File dir = new File(System.getProperty("user.dir") +
            File.separatorChar +
            propManager.getProperty(PropertyManager.BASE_OUTPUT_DIR) +
            File.separatorChar + packagePath);
        ComponentBean[] cbs = configBean.getComponents();
        for (int i = 0; i < cbs.length; i++) {

            String componentClass = cbs[i].getComponentClass();
            if (componentClass.startsWith(compPackage)) {
                cb = cbs[i];

                 if (logger.isLoggable(Level.INFO)) {
                    logger.log(Level.INFO, "Generating concrete HTML component class " +
                        cb.getComponentClass());
                 }

                // Initialize all per-class static variables
                properties = new ArrayList<PropertyBean>();

                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String fileName = cb.getComponentClass();
                fileName = fileName.substring(fileName.lastIndexOf('.') + 1) +
                    ".java";
                File file = new File(dir, fileName);
                writer = new CodeWriter(new FileWriter(file));
                // Generate the various portions of each class
                prefix();
                properties();
                suffix();

                // Flush and close the Writer for this class
                writer.flush();
                writer.close();
            }


        }
    }


    /**
     * <p>Generate the prefix for this component class, down to (and including)
     * the class declaration.</p>
     */
    private void prefix() throws Exception {

        // Acquire the config bean for our base component
        ComponentBean base = configBean.getComponent(cb.getBaseComponentType());
        if (base == null) {
            throw new IllegalArgumentException("No base component type for '" +
                cb.getComponentType() + "'");
        }

        // Generate the copyright information
        writer.writeJavadocComment(
            propManager.getProperty(PropertyManager.COPYRIGHT));

        // Generate the package declaration
        writer.writePackage("javax.faces.component.html");

        writer.write('\n');

        // Generate the imports
        writer.writeImport("java.io.IOException");
        writer.write('\n');
        writer.writeImport("javax.faces.context.FacesContext");
        writer.writeImport("javax.el.MethodExpression");
        writer.writeImport("javax.el.ValueExpression");
        writer.write("\n\n");

        writer.writeBlockComment("******* GENERATED CODE - DO NOT EDIT *******");
        writer.write("\n\n");

        // Generate the class JavaDocs (if any)
        DescriptionBean db = cb.getDescription("");
        String rendererType = cb.getRendererType();

        String description = null;
        if (db != null) {
            description = db.getDescription().trim();
        }

        if (rendererType != null) {
            if (description == null) {
                description = "";
            }
            description +=
            "\n<p>By default, the <code>rendererType</code> property must be set to \"<code>" +
                rendererType +
                "</code>\".\nThis value can be changed by calling the <code>setRendererType()</code> method.</p>\n";
        }

        if (description != null && description.length() > 0) {
            writer.writeJavadocComment(description);
        }

        // Generate the class declaration
        writer.writePublicClassDeclaration(shortName(cb.getComponentClass()),
                                           base.getComponentClass(),
                                           null, false, false);

        writer.write("\n\n");

        // Generate the constructor
        writer.indent();
        writer.fwrite("public ");
        writer.write(shortName(cb.getComponentClass()));
        writer.write("() {\n");
        writer.indent();
        writer.fwrite("super();\n");
        if (rendererType != null) {
            writer.fwrite("setRendererType(\"");
            writer.write(rendererType);
            writer.write("\");\n");
        }
        writer.outdent();
        writer.fwrite("}\n\n\n");

        // Generate the manifest constant for the component type
        writer.writeJavadocComment(
            "<p>The standard component type for this component.</p>\n");
        writer.fwrite("public static final String COMPONENT_TYPE = \"");
        writer.write(cb.getComponentType());
        writer.write("\";\n\n\n");

    }


    /**
     * <p>Generate the property instance variable, and getter and setter
     * methods, for all non-duplicate properties of this component class.</p>
     */
    private void properties() throws Exception {

        ComponentBean base = configBean.getComponent(cb.getBaseComponentType());
        PropertyBean[] pbs = cb.getProperties();
        for (int i = 0; i < pbs.length; i++) {

            // Should we generate this property?
            PropertyBean pb = pbs[i];
            if (base.getProperty(pb.getPropertyName()) != null) {
                if (logger.isLoggable(Level.FINER)) {
                    logger.log(Level.FINER, "Skipping base class property '" +
                        pb.getPropertyName() + "'");
                }
                continue;
            }
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "Generating property variable/getter/setter for '" +
                    pb.getPropertyName() + "'");
            }
            properties.add(pb);
            String type = pb.getPropertyClass();
            String var = mangle(pb.getPropertyName());

            // Generate the instance variable
            writer.fwrite("private ");
            writer.write(type);
            writer.write(' ');
            writer.write(var);
            if (pb.getDefaultValue() != null) {
                writer.write(" = ");
                writer.write(pb.getDefaultValue());
            } else if (primitive(type)) {
                writer.write(" = ");
                writer.write(TYPE_DEFAULTS.get(type));
            }
            writer.write(";\n");
            if (primitive(type)) {
                writer.fwrite("private boolean ");
                writer.write(var);
                writer.write("_set = false;\n");
            }
            writer.write("\n");


            // Document getter method
            String description = "<p>Return the value of the <code>" +
                pb.getPropertyName() + "</code> property.</p>";
            DescriptionBean db = pb.getDescription("");
            if (db != null) {
                String temp = db.getDescription().trim();
                if (temp != null && temp.length() > 0) {
                    description += '\n' + "<p>Contents: " + temp;
                }
            }
            writer.writeJavadocComment(description.trim());

            // Generate the getter method
            writer.fwrite("public ");
            writer.write(type);
            if ("boolean".equals(type)) {
                writer.write(" is");
            } else {
                writer.write(" get");
            }
            writer.write(capitalize(pb.getPropertyName()));
            writer.write("() {\n");
            writer.indent();
            if (primitive(type)) {
                writer.fwrite("if (this.");
                writer.write(var);
                writer.write("_set) {\n");
                writer.indent();
            } else {
                writer.fwrite("if (null != this.");
                writer.write(var);
                writer.write(") {\n");
                writer.indent();
            }
            writer.fwrite("return this.");
            writer.write(var);
            writer.write(";\n");
             writer.outdent();
            writer.fwrite("}\n");
            writer.fwrite("ValueExpression _ve = getValueExpression(\"");
            writer.write(pb.getPropertyName());
            writer.write("\");\n");
            writer.fwrite("if (_ve != null) {\n");
            writer.indent();
            if (primitive(type)) {
                writer.fwrite(
                    "Object _result = _ve.getValue(getFacesContext().getELContext());\n");
                writer.fwrite("if (_result == null) {\n");
                writer.indent();
                writer.fwrite("return ");
                writer.write(TYPE_DEFAULTS.get(type));
                writer.write(";\n");
                writer.outdent();
                writer.fwrite("} else {\n");
                writer.indent();
                writer.fwrite("return ((");
                writer.write(GeneratorUtil.convertToObject(type));
                writer.write(") _result).");
                writer.write(GeneratorUtil.convertToPrimitive(type));
                writer.write("();\n");
                writer.outdent();
                writer.fwrite("}\n");
            } else {
                writer.fwrite("return (");
                writer.write(type);
                writer.write(") _ve.getValue(getFacesContext().getELContext());\n");
            }
            writer.outdent();
            writer.fwrite("} else {\n");
            writer.indent();
            if (primitive(type)) {
                writer.fwrite("return this.");
                writer.write(var);
                writer.write(";\n");
            } else {
                writer.fwrite("return null;\n");
            }
            writer.outdent();
            writer.fwrite("}\n");
            writer.outdent();
            writer.fwrite("}\n\n");

            // Generate the setter method
            writer.writeJavadocComment("<p>Set the value of the <code>" +
                pb.getPropertyName() + "</code> property.</p>\n");

            writer.fwrite("public void set");
            writer.write(capitalize(pb.getPropertyName()));
            writer.write("(");
            writer.write(type);
            writer.write(' ');
            writer.write(var);
            writer.write(") {\n");
            writer.indent();
            writer.fwrite("this.");
            writer.write(var);
            writer.write(" = ");
            writer.write(var);
            writer.write(";\n");
            if (primitive(type)) {
                writer.fwrite("this.");
                writer.write(var);
                writer.write("_set = true;\n");
            }
            writer.outdent();
            writer.fwrite("}\n\n");

            // Generate spacing between properties
            writer.write("\n");

        }

    }


    /**
     * <p>Generate the suffix for this component class.</p>
     */
    private void suffix() throws Exception {

        int p = 0; // Number of primitive properties
        for (int i = 0, size = properties.size(); i < size; i++) {
            PropertyBean pb = properties.get(i);
            if (primitive(pb.getPropertyClass())) {
                p++;
            }
        }

        // Generate the saveState() method
        writer.fwrite("public Object saveState(FacesContext _context) {\n");
        writer.indent();
        writer.fwrite("Object[] _values = new Object[");
        writer.write("" + (properties.size() + p + 1));
        writer.write("];\n");
        writer.fwrite("_values[0] = super.saveState(_context);\n");

        int n = 1; // Index into values array
        for (int i = 0; i < properties.size(); i++) {
            PropertyBean pb = properties.get(i);
            String name = mangle(pb.getPropertyName());
            String type = pb.getPropertyClass();
            writer.fwrite("_values[");
            writer.write("" + n++);
            writer.write("] = ");
            if ("boolean".equals(type)) {
                writer.write("this.");
                writer.write(name);
                writer.write(" ? Boolean.TRUE : Boolean.FALSE");
            } else if (primitive(type)) {
                writer.write("new ");
                writer.write(GeneratorUtil.convertToObject(type));
                writer.write("(this.");
                writer.write(name);
                writer.write(")");
            } else {
                writer.write(name);
            }
            writer.write(";\n");
            if (primitive(type)) {
                writer.fwrite("_values[");
                writer.write("" + n++);
                writer.write("] = this.");
                writer.write(name);
                writer.write("_set ? Boolean.TRUE : Boolean.FALSE;\n");
            }
        }
        writer.fwrite("return _values;\n");
        writer.outdent();
        writer.write("}\n\n\n");

        // Generate the restoreState() method
        writer.fwrite(
            "public void restoreState(FacesContext _context, Object _state) {\n");
        writer.indent();
        writer.fwrite("Object[] _values = (Object[]) _state;\n");
        writer.fwrite("super.restoreState(_context, _values[0]);\n");
        n = 1;
        for (int i = 0, size = properties.size(); i < size; i++) {
            PropertyBean pb = properties.get(i);
            String name = mangle(pb.getPropertyName());
            String type = pb.getPropertyClass();
            writer.fwrite("this.");
            writer.write(name);
            writer.write(" = ");
            if (primitive(type)) {
                writer.write("((");
                writer.write(GeneratorUtil.convertToObject(type));
                writer.write(") _values[");
                writer.write("" + n++);
                writer.write("]).");
                writer.write(GeneratorUtil.convertToPrimitive(type));
                writer.write("()");
            } else {
                writer.write("(");
                writer.write(type);
                writer.write(") _values[");
                writer.write("" + n++);
                writer.write("]");
            }
            writer.write(";\n");
            if (primitive(type)) {
                writer.fwrite("this.");
                writer.write(name);
                writer.write("_set = ((Boolean) _values[");
                writer.write("" + n++);
                writer.write("]).booleanValue();\n");
            }
        }
        writer.outdent();
        writer.fwrite("}\n\n\n");

        // Generate the ending of this class
        writer.outdent();
        writer.write("}\n");

    }



    // ------------------------------------------------------------- Main Method


    public static void main(String[] args) throws Exception {
        PropertyManager propManager = PropertyManager.newInstance(args[0]);
        Generator generator = new HtmlComponentGenerator(propManager);
        generator.generate(GeneratorUtil.getConfigBean(args[1]));
    }

}
