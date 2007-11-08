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
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.ArrayList;

import com.sun.faces.config.beans.AttributeBean;
import com.sun.faces.config.beans.ComponentBean;
import com.sun.faces.config.beans.DescriptionBean;
import com.sun.faces.config.beans.FacesConfigBean;
import com.sun.faces.config.beans.PropertyBean;
import com.sun.faces.config.beans.RendererBean;
import com.sun.faces.util.ToolsUtil;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class generates tag handler class code that is special to the
 * "html_basic" package.
 */
public class HtmlTaglibGenerator extends AbstractGenerator {

    // -------------------------------------------------------- Static Variables
    
    // Log instance for this class
    private static final Logger logger = Logger.getLogger(ToolsUtil.FACES_LOGGER +
            ToolsUtil.GENERATE_LOGGER, ToolsUtil.TOOLS_LOG_STRINGS);    

    // The Writer for each component class to be generated
    protected CodeWriter writer;


    // Maps used for generatng Tag Classes
    protected ComponentBean component = null;
    protected RendererBean renderer = null;

    // Tag Handler Class Name
    protected String tagClassName = null;
    protected FacesConfigBean configBean;    
    protected PropertyManager propManager;

    private Generator tldGenerator;
    private File outputDir;
    private List<String> imports;



    // ------------------------------------------------------------ Constructors

    public HtmlTaglibGenerator(PropertyManager propManager) {

        this.propManager = propManager;
        
        // initialize structures from the data in propManager

        outputDir = getClassPackageDirectory();

        setTldGenerator(GeneratorUtil.getTldGenerator(propManager));

        addImport("com.sun.faces.util.Util");
        addImport("java.io.IOException");
        addImport("javax.faces.component.*");
        addImport("javax.faces.context.*");
        addImport("javax.faces.convert.*");
        addImport("javax.faces.el.*");
        addImport("javax.faces.webapp.*");
        addImport("javax.servlet.jsp.JspException");

    } // END HtmlTaglibGenerator


    // ---------------------------------------------------------- Public Methods


    public void generate(FacesConfigBean configBean) {

        this.configBean = configBean;
        try {
            generateTagClasses();
            tldGenerator.generate(configBean);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    } // END generate


    public static void main(String[] args) {

        PropertyManager manager = PropertyManager.newInstance(args[0]);
        try {
            Generator generator = new HtmlTaglibGenerator(manager);
            generator.generate(GeneratorUtil.getConfigBean(args[1]));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    } // END main


    // ------------------------------------------------------- Protected Methods


    /**
     * <p>Set the <code>JspTLDGenerator</code> to be used by the taglib
     * generator.</p>
     * @param tldGenerator <code>JspTLDGenerator</code> instance
     */
    protected void setTldGenerator(JspTLDGenerator tldGenerator) {

        this.tldGenerator = tldGenerator;

    } // END setTldGenerator

    protected void addImport(String fullyQualClassName) {

        if (imports == null) {
            imports = new ArrayList<String>();
        }
        imports.add(fullyQualClassName);

    }

    protected void writeImports() throws Exception {

        Collections.sort(imports);

        for (Iterator i = imports.iterator(); i.hasNext(); ) {
            writer.writeImport((String) i.next());
        }

    } // END writeImports


    protected void writeCopyright() throws Exception {

        writer.writeBlockComment(
            propManager.getProperty(PropertyManager.COPYRIGHT));

    } // END writeCopyright


    protected void writePackage() throws Exception {

        // Generate the package declaration
        writer.writePackage(
            propManager.getProperty(PropertyManager.TARGET_PACKAGE));

    } // END writePackage


    protected void writeClassDocumentation() throws Exception {

        // Generate the class JavaDocs (if any)
        DescriptionBean db = component.getDescription("");
        if (db != null) {
            String description = db.getDescription();
            if (description == null) {
                description = "";
            }
            description = description.trim();
            if (description.length() > 0) {
                writer.writeJavadocComment(description);
            }
        }

    } // END writeClassDocumentation


    protected void writeClassDeclaration() throws Exception {

        // Generate the class declaration
        writer.writePublicClassDeclaration(tagClassName,
                                           "UIComponentELTag",
                                           null, false, true);

    } // END writeClassDeclaration

    /**
     * Generate copyright, package declaration, import statements, class
     * declaration.
     */
    protected void tagHandlerPrefix() throws Exception {


        // Generate the copyright information
        writeCopyright();

        writer.write('\n');

        // Generate the package declaration
        writePackage();

        writer.write('\n');

        // Generate the imports
        writeImports();

        writer.write("\n\n");

        writer.writeBlockComment("******* GENERATED CODE - DO NOT EDIT *******");
        writer.write("\n\n");


        // Generate the class JavaDocs (if any)
        writeClassDocumentation();

        // Generate the class declaration
        writeClassDeclaration();

        writer.write('\n');

        writer.indent();

    }


    protected void tagHandlerReleaseMethod() throws Exception {

        writer.writeLineComment("RELEASE");

        writer.fwrite("public void release() {\n");
        writer.indent();
        writer.fwrite("super.release();\n\n");
        writer.writeLineComment("component properties");

        // Generate from component properties
        //
        PropertyBean[] properties = component.getProperties();
        for (int i = 0, len = properties.length; i < len; i++) {
            PropertyBean property = properties[i];
            if (property == null) {
                continue;
            }

            if (!property.isTagAttribute()) {
                continue;
            }

            String propertyName = property.getPropertyName();
            String propertyType = property.getPropertyClass();

            // SPECIAL - Don't generate these properties
            if ("binding".equals(propertyName)
                || "id".equals(propertyName)
                || "rendered".equals(propertyName)) {
                continue;
            }

            String ivar = mangle(propertyName);
            writer.fwrite("this." + ivar + " = ");
            if (primitive(propertyType) && !(property.isValueExpressionEnabled()
                || property.isMethodExpressionEnabled())) {
                writer.write(TYPE_DEFAULTS.get(propertyType));
            } else {
                writer.write("null");
            }
            writer.write(";\n");
        }

        writer.write("\n");
        writer.writeLineComment("rendered attributes");

        // Generate from renderer attributes..
        //
        AttributeBean[] attributes = renderer.getAttributes();

        for (int i = 0, len = attributes.length; i < len; i++) {
            AttributeBean attribute = attributes[i];

            if (attribute == null) {
                continue;
            }
            if (!attribute.isTagAttribute()) {
                continue;
            }

            String attributeName = attribute.getAttributeName();

            writer.fwrite("this." + mangle(attributeName) + " = null;\n");
        }

        writer.outdent();
        writer.fwrite("}\n\n");
    }

    /**
     * Generate Tag Handler setter methods from component properties and
     * renderer attributes.
     */
    protected void tagHandlerSetterMethods() throws Exception {

        writer.writeLineComment("Setter Methods");

        // Generate from component properties
        //
        PropertyBean[] properties = component.getProperties();

        for (int i = 0, len = properties.length; i < len; i++) {
            PropertyBean property = properties[i];

            if (property == null) {
                continue;
            }
            if (!property.isTagAttribute()) {
                continue;
            }

            String propertyName = property.getPropertyName();
            String propertyType = property.getPropertyClass();

            // SPECIAL - Don't generate these properties
            if ("binding".equals(propertyName)
                || "id".equals(propertyName)
                || "rendered".equals(propertyName)) {
                continue;
            }

            if (property.isValueExpressionEnabled() ||
                property.isMethodExpressionEnabled()) {
                writer.writeWriteOnlyProperty(propertyName, "java.lang.String");
            } else {
                writer.writeWriteOnlyProperty(propertyName, propertyType);
            }
        }

        // Generate from renderer attributes..
        //
        AttributeBean[] attributes = renderer.getAttributes();
        for (int i = 0, len = attributes.length; i < len; i++) {
            AttributeBean attribute = attributes[i];

            if (attribute == null) {
                continue;
            }
            if (!attribute.isTagAttribute()) {
                continue;
            }
            String attributeName = attribute.getAttributeName();

            writer.writeWriteOnlyProperty(attributeName,
                "java.lang.String");

        }
        writer.write("\n");
    }

    protected void tagHanderSetPropertiesMethod() throws Exception {

        String componentType = component.getComponentType();
        String componentClass = component.getComponentClass();

        writer.fwrite("protected void setProperties(UIComponent component) {\n");
        writer.indent();
        writer.fwrite("super.setProperties(component);\n");

        String iVar =
            GeneratorUtil.stripJavaxFacesPrefix(componentType).toLowerCase();

        writer.fwrite(componentClass + ' ' + iVar + " = null;\n");

        writer.fwrite("try {\n");
        writer.indent();
        writer.fwrite(iVar + " = (" + componentClass + ") component;\n");
        writer.outdent();
        writer.fwrite("} catch (ClassCastException cce) {\n");
        writer.indent();
        writer.fwrite("throw new IllegalStateException(\"Component \" + " +
            "component.toString() + \" not expected type.  Expected: " +
            componentClass +
            ".  Perhaps you're missing a tag?\");\n");
        writer.outdent();
        writer.fwrite("}\n\n");

        if (isValueHolder(componentClass)) {
            writer.fwrite("if (converter != null) {\n");
            writer.indent();
            writer.fwrite("if (isValueReference(converter)) {\n");
            writer.indent();
            writer.fwrite("ValueBinding vb = Util.getValueBinding(converter);\n");
            writer.fwrite(iVar + ".setValueBinding(\"converter\", vb);\n");
            writer.outdent();
            writer.fwrite("} else {\n");
            writer.indent();
            writer.fwrite("Converter _converter = FacesContext.getCurrent" +
                "Instance().getApplication().createConverter(converter);\n");
            writer.fwrite(iVar + ".setConverter(_converter);\n");
            writer.outdent();
            writer.fwrite("}\n");
            writer.outdent();
            writer.fwrite("}\n\n");
        }

        // Generate "setProperties" method contents from component properties
        //
        PropertyBean[] properties = component.getProperties();
        for (int i = 0, len = properties.length; i < len; i++) {
            PropertyBean property = properties[i];

            if (property == null) {
                continue;
            }
            if (!property.isTagAttribute()) {
                continue;
            }

            String propertyName = property.getPropertyName();
            String propertyType = property.getPropertyClass();

            // SPECIAL - Don't generate these properties
            if ("binding".equals(propertyName) ||
                "id".equals(propertyName) ||
                "rendered".equals(propertyName) ||
                "converter".equals(propertyName)) {
                continue;
            }
            String ivar = mangle(propertyName);
            String vbKey = ivar;
            String comp =
                GeneratorUtil.stripJavaxFacesPrefix(componentType).toLowerCase();
            String capPropName = capitalize(propertyName);

            if (property.isValueExpressionEnabled()) {
                writer.fwrite("if (" + ivar + " != null) {\n");
                writer.indent();
                writer.fwrite("if (isValueReference(" + ivar + ")) {\n");
                writer.indent();
                writer.fwrite("ValueBinding vb = Util.getValueBinding(" +
                    ivar + ");\n");

                writer.fwrite(comp + ".setValueBinding(\"" + vbKey + "\", vb);\n");
                writer.outdent();
                writer.fwrite("} else {\n");
                writer.indent();
                if (primitive(propertyType)) {
                    writer.fwrite(comp + ".set" + capPropName +
                        "(" + GeneratorUtil.convertToPrimitive(propertyType) +
                        ".valueOf(" + ivar + ")." + propertyType +
                        "Value());\n");
                } else {
                    writer.fwrite(comp + ".set" + capPropName + '(' + ivar +
                        ");\n");
                }
                writer.outdent();
                writer.fwrite("}\n");
                writer.outdent();
                writer.fwrite("}\n\n");
            } else if (property.isMethodExpressionEnabled()) {
                if ("action".equals(ivar)) {
                    writer.fwrite("if (" + ivar + " != null) {\n");
                    writer.indent();
                    writer.fwrite("if (isValueReference(" + ivar + ")) {\n");
                    writer.indent();
                    writer.fwrite("MethodBinding vb = FacesContext.getCurrentInstance().");
                    writer.write("getApplication().createMethodBinding(" +
                        ivar + ", null);\n");
                    writer.fwrite(comp + ".setAction(vb);\n");
                    writer.outdent();
                    writer.fwrite("} else {\n");
                    writer.indent();
                    writer.fwrite("final String outcome = " + ivar + ";\n");
                    writer.fwrite("MethodBinding vb = Util.createConstantMethodBinding(" +
                        ivar + ");\n");
                    writer.fwrite(comp + ".setAction(vb);\n");
                    writer.outdent();
                    writer.fwrite("}\n");
                    writer.outdent();
                    writer.fwrite("}\n");
                } else {
                    HashMap<String,String> signatureMap = new HashMap<String, String>(3);
                    signatureMap.put("actionListener",
                        "Class args[] = { ActionEvent.class };");
                    signatureMap.put("validator",
                        "Class args[] = { FacesContext.class, UIComponent.class, Object.class };");
                    signatureMap.put("valueChangeListener",
                        "Class args[] = { ValueChangeEvent.class };");

                    writer.fwrite("if (" + ivar + " != null) {\n");
                    writer.indent();
                    writer.fwrite("if (isValueReference(" + ivar + ")) {\n");
                    writer.indent();
                    writer.fwrite(signatureMap.get(ivar) + "\n");
                    writer.fwrite("MethodBinding vb = FacesContext.getCurrentInstance().");
                    writer.write("getApplication().createMethodBinding(" +
                        ivar + ", args);\n");
                    writer.fwrite(comp + ".set" + capitalize(ivar) + "(vb);\n");
                    writer.outdent();
                    writer.fwrite("} else {\n");
                    writer.indent();
                    writer.fwrite("Object params [] = {" + ivar + "};\n");
                    writer.fwrite("throw new javax.faces.FacesException(Util." +
                        "getExceptionMessageString(Util.INVALID_EXPRESSION_ID, " +
                        "params));\n");
                    writer.outdent();
                    writer.fwrite("}\n");
                    writer.outdent();
                    writer.fwrite("}\n");
                }
            } else {
                writer.fwrite(comp + ".set" + capPropName + "(" + ivar + ");\n");
            }
        }

        // Generate "setProperties" method contents from renderer attributes
        //
        AttributeBean[] attributes = renderer.getAttributes();
        for (int i = 0, len = attributes.length; i < len; i++) {
            AttributeBean attribute = attributes[i];
            if (attribute == null) {
                continue;
            }
            if (!attribute.isTagAttribute()) {
                continue;
            }
            String attributeName = attribute.getAttributeName();
            String attributeType = attribute.getAttributeClass();

            String ivar = mangle(attributeName);
            String vbKey = ivar;
            String comp =
                GeneratorUtil.stripJavaxFacesPrefix(componentType).toLowerCase();

            writer.fwrite("if (" + ivar + " != null) {\n");
            writer.indent();
            writer.fwrite("if (isValueReference(" + ivar + ")) {\n");
            writer.indent();
            writer.fwrite("ValueBinding vb = Util.getValueBinding(" + ivar +
                ");\n");
            writer.fwrite(comp);
            if ("_for".equals(ivar)) {
                writer.write(".setValueBinding(\"" + '_' + vbKey + "\", vb);\n");
            } else {
                writer.write(".setValueBinding(\"" + vbKey + "\", vb);\n");
            }
            writer.outdent();
            writer.fwrite("} else {\n");
            writer.indent();
            if (primitive(attributeType)) {
                writer.fwrite(comp + ".getAttributes().put(\"" + ivar +
                    "\", ");
                writer.write(GeneratorUtil.convertToPrimitive(attributeType) +
                    ".valueOf(" + ivar + "));\n");
            } else {
                if ("bundle".equals(ivar)) {
                    writer.fwrite(comp +
                        ".getAttributes().put(com.sun.faces.RIConstants.BUNDLE_ATTR, ");
                } else if ("_for".equals(ivar)) {
                    writer.fwrite(comp +
                        ".getAttributes().put(\"for\", ");
                } else {
                    writer.fwrite(comp +
                        ".getAttributes().put(\"" + ivar + "\", ");
                }
                writer.write(ivar + ");\n");
            }
            writer.outdent();
            writer.fwrite("}\n");
            writer.outdent();
            writer.fwrite("}\n");
        }
        writer.outdent();
        writer.fwrite("}\n\n");

    }


    /**
     * Generate Tag Handler general methods from component properties and
     * renderer attributes.
     */
    protected void tagHandlerGeneralMethods() throws Exception {

        writer.writeLineComment("General Methods");


        String rendererType = renderer.getRendererType();
        String componentType = component.getComponentType();

        writer.fwrite("public String getRendererType() {\n");
        writer.indent();
        writer.fwrite("return ");
        writer.write('\"' + rendererType + "\";\n");
        writer.outdent();
        writer.fwrite("}\n\n");

        writer.fwrite("public String getComponentType() {\n");
        writer.indent();
        writer.fwrite("return ");
        if (componentType.equals(rendererType)) {
            writer.write(
                "\"javax.faces.Html" +
                GeneratorUtil.stripJavaxFacesPrefix(componentType) +
                "\";\n");
        } else {
            writer.write(
                "\"javax.faces.Html" +
                GeneratorUtil.stripJavaxFacesPrefix(componentType) +
                GeneratorUtil.stripJavaxFacesPrefix(rendererType) +
                "\";\n");
        }
        writer.outdent();
        writer.fwrite("}\n\n");


    }


     protected static boolean isValueHolder(String componentClass) {

        try {
            Class<?> clazz = Class.forName(componentClass);
            Class<?> valueHolderClass =
                Class.forName("javax.faces.component.ValueHolder");
            return valueHolderClass.isAssignableFrom(clazz);
        } catch (ClassNotFoundException cnfe) {
            throw new IllegalStateException("Unable to find component class '" +
                componentClass + "' : " + cnfe.toString());
        }

    } // END isValueHolder


    /**
     * Generate Tag Handler support methods
     */
    protected void tagHandlerClassicSupportMethods() throws Exception {

        writer.writeLineComment("Methods From TagSupport");

        writer.fwrite("public int doStartTag() throws JspException {\n");
        writer.indent();
        writeSuperTagCallBody("doStartTag", true);
        writer.outdent();
        writer.fwrite("}\n\n");

        writer.fwrite("public int doEndTag() throws JspException {\n");
        writer.indent();

        writeSuperTagCallBody("doEndTag", true);
        writer.outdent();
        writer.fwrite("}\n\n");

    }


    /**
     * <p>Convience method to generate code for a super call to
     * a JSP tag lifecycle method.</p>
     * @param method JSP tag lifecycle method name
     * @throws IOException
     */
    protected void writeSuperTagCallBody(String method, boolean hasReturn)
    throws IOException {
        writer.fwrite("try {\n");
        writer.indent();
        writer.fwrite((hasReturn ? "return super." : "super."));
        writer.write(method);
        writer.write("();\n");
        writer.outdent();
        writer.fwrite("} catch (Exception e) {\n");
        writer.indent();
        writer.fwrite("Throwable root = e;\n");
        writer.fwrite("while (root.getCause() != null) {\n");
        writer.indent();
        writer.fwrite("root = root.getCause();\n");
        writer.outdent();
        writer.fwrite("}\n");
        writer.fwrite("throw new JspException(root);\n");
        writer.outdent();
        writer.fwrite("}\n");
    }


    /**
     * Generate remaining Tag Handler methods
     */
    protected void tagHandlerSuffix() throws Exception {

        // generate general purpose method used in logging.
        //
        writer.fwrite("public String getDebugString() {\n");
        writer.indent();
        String res = "\"id: \" + this.getId() + \" class: \" + this.getClass().getName()";
        writer.fwrite("String result = " + res + ";\n");
        writer.fwrite("return result;\n");
        writer.outdent();
        writer.fwrite("}\n\n");
        writer.outdent();
        writer.fwrite("}\n");

    }

    //
    // Helper methods
    //
    //

    // --------------------------------------------------------- Private Methods

    /**
     * Generate the tag handler class files.
     */
    private void generateTagClasses() throws Exception {

        Map<String,ArrayList<RendererBean>> renderersByComponentFamily =
            GeneratorUtil.getComponentFamilyRendererMap(configBean,
                propManager.getProperty(PropertyManager.RENDERKIT_ID));
        Map<String,ComponentBean> componentsByComponentFamily =
            GeneratorUtil.getComponentFamilyComponentMap(configBean);

        for (Iterator<String> keyIter = renderersByComponentFamily.keySet().iterator();
             keyIter.hasNext(); ) {

            String componentFamily = keyIter.next();
            List<RendererBean> renderers = renderersByComponentFamily.get(componentFamily);

            component = componentsByComponentFamily.get(componentFamily);

            for (Iterator<RendererBean> rendererIter = renderers.iterator();
                 rendererIter.hasNext(); ) {

                renderer = rendererIter.next();
                String rendererType = renderer.getRendererType();

                tagClassName = GeneratorUtil.makeTagClassName(
                         GeneratorUtil.stripJavaxFacesPrefix(componentFamily),
                         GeneratorUtil.stripJavaxFacesPrefix(rendererType));

                if (tagClassName == null) {
                    throw new IllegalStateException(
                        "Could not determine tag class name");
                }

                if (logger.isLoggable(Level.INFO)) {
                    logger.log(Level.INFO, "Generating " + tagClassName + "...");
                }


                File file = new File(outputDir, tagClassName + ".java");
                writer = new CodeWriter(new FileWriter(file));

                tagHandlerPrefix();
                tagHandlerSetterMethods();
                tagHandlerGeneralMethods();
                tagHanderSetPropertiesMethod();
                tagHandlerClassicSupportMethods();
                tagHandlerReleaseMethod();
                tagHandlerSuffix();

                // Flush and close the Writer
                writer.flush();
                writer.close();
            }
        }
    }

     private File getClassPackageDirectory() {

        String packagePath =
            propManager.getProperty(PropertyManager.TARGET_PACKAGE).
            replace('.', File.separatorChar);
        File packageDir = new File(getBaseOutputDirectory(),
            packagePath);

        if (!packageDir.exists()) {
            packageDir.mkdirs();
        }

        return packageDir;

    } // END getClassPackageDirectory


    private File getBaseOutputDirectory() {

        File outputDir = new File(System.getProperty("user.dir") +
            File.separatorChar +
            propManager.getProperty(PropertyManager.BASE_OUTPUT_DIR));

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        return outputDir;

    } // END getBaseOutputDirectory

    // ----------------------------------------------------------- Inner Classes


}
