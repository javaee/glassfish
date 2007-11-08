/*
 * $Id: JspTLD21Generator.java,v 1.1 2005/09/20 21:11:38 edburns Exp $
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import com.sun.faces.config.beans.AttributeBean;
import com.sun.faces.config.beans.ComponentBean;
import com.sun.faces.config.beans.DescriptionBean;
import com.sun.faces.config.beans.PropertyBean;
import com.sun.faces.config.beans.RendererBean;

/**
 * <p>A <code>JspTldGenerator</code> specific for JSP 2.1 style TLDs.</p>
 */
public class JspTLD21Generator extends JspTLDGenerator {

    private static final String JSP_VERSION = "2.1";
    private static final String JSF_TLIB_VERSION = "1.2";

    /**
     * <p>Schema related attributes.</p>
     */
    private static Map<String,String> TAG_LIB_SCHEMA_ATTRIBUTES = new HashMap<String, String>();
    static {
        TAG_LIB_SCHEMA_ATTRIBUTES.put("xmlns",
            "http://java.sun.com/xml/ns/javaee");
        TAG_LIB_SCHEMA_ATTRIBUTES.put("xmlns:xsi",
            "http://www.w3.org/2001/XMLSchema-instance");
        TAG_LIB_SCHEMA_ATTRIBUTES.put("xsi:schemaLocation",
            "http://java.sun.com/xml/ns/javaee web-jsptaglibrary_2_1.xsd");
        TAG_LIB_SCHEMA_ATTRIBUTES.put("version", JSP_VERSION);
    }

    // ------------------------------------------------------------ Constructors


    public JspTLD21Generator(PropertyManager propManager) {

        super(propManager);

    } // END JspTLD21Generator


    // ------------------------------------------------------- Protected Methods

    protected String getRtexprvalue(String tagName, String attributeName) {

        if ("id".equals(attributeName)) {        
            return "true";
        } else {
            return super.getRtexprvalue(tagName, attributeName);
        }

    } // END getRtexprvalue


        /**
     * The description element for this TLD.
     */
    protected void writeTldDescription() throws IOException {

        writer.startElement("taglib", TAG_LIB_SCHEMA_ATTRIBUTES);
        writer.writeComment(
            "============== Tag Library Description Elements =============");

        writer.startElement("description");
        writer.writeText(
            propManager.getProperty(PropertyManager.TAGLIB_DESCRIPTION));
        writer.closeElement();

        writer.startElement("tlib-version");
        writer.writeText(JSF_TLIB_VERSION);
        writer.closeElement();

        writer.startElement("short-name");
        writer.writeText(
            propManager.getProperty(PropertyManager.TAGLIB_SHORT_NAME));
        writer.closeElement();

        writer.startElement("uri");
        writer.writeText(propManager.getProperty(PropertyManager.TAGLIB_URI));
        writer.closeElement();

    } // end tldDescription


    /**
     * The tags for this TLD.
     */
    protected void writeTags() throws IOException {
        writer.writeComment(
            "===================== HTML 4.0 basic tags ======================");

        Map<String,ComponentBean> componentsByComponentFamily =
            GeneratorUtil.getComponentFamilyComponentMap(configBean);
        Map<String, ArrayList<RendererBean>> renderersByComponentFamily =
            GeneratorUtil.getComponentFamilyRendererMap(configBean,
                propManager.getProperty(PropertyManager.RENDERKIT_ID));
        String targetPackage =
            propManager.getProperty(PropertyManager.TARGET_PACKAGE);

        for (Iterator<String> keyIter = renderersByComponentFamily.keySet().iterator();
             keyIter.hasNext();) {

            String componentFamily = keyIter.next();
            List<RendererBean> renderers =
            (List<RendererBean>) renderersByComponentFamily.get(componentFamily);
            for (Iterator<RendererBean> rendererIter = renderers.iterator();
                 rendererIter.hasNext();) {

                RendererBean renderer = rendererIter.next();
                String rendererType = renderer.getRendererType();
                writer.startElement("tag");

                DescriptionBean description = renderer.getDescription("");
                if (description != null) {
                    String descriptionText = description.getDescription().trim();

                    if (descriptionText != null) {
                        writer.startElement("description");
                        StringBuffer sb = new StringBuffer();
                        sb.append("<![CDATA[");
                        sb.append(descriptionText);
                        sb.append("]]>\n");
                        writer.writeText(sb.toString());
                        writer.closeElement();
                    }
                }

                String tagName = makeTldTagName(
                    GeneratorUtil.stripJavaxFacesPrefix(componentFamily),
                    GeneratorUtil.stripJavaxFacesPrefix(rendererType));

                if (tagName == null) {
                    throw new IllegalStateException(
                        "Could not determine tag name");
                }

                writer.startElement("name");
                writer.writeText(tagName);
                writer.closeElement();


                if (GeneratorUtil.makeTagClassName(
                    GeneratorUtil.stripJavaxFacesPrefix(componentFamily),
                    GeneratorUtil.stripJavaxFacesPrefix(rendererType)) ==
                    null) {
                    throw new IllegalStateException(
                        "Could not determine tag class name");
                }

                writer.startElement("tag-class");
                writer.writeText(targetPackage + '.' +
                    GeneratorUtil.makeTagClassName(GeneratorUtil.stripJavaxFacesPrefix(componentFamily),
                        GeneratorUtil.stripJavaxFacesPrefix(rendererType)));
                writer.closeElement();

                writer.startElement("tei-class");
                writer.writeText(getTeiClass(tagName));
                writer.closeElement();

                writer.startElement("body-content");
                writer.writeText(getBodyContent(tagName));
                writer.closeElement();


                // Generate tag attributes
                //

                // Component Properties first...
                //
                ComponentBean component = componentsByComponentFamily.get(componentFamily);

                PropertyBean[] properties = component.getProperties();
                PropertyBean property;

                for (int i = 0, len = properties.length; i < len; i++) {
                    if (null == (property = properties[i])) {
                        continue;
                    }
                    if (!property.isTagAttribute()) {
                        continue;
                    }

                    writer.startElement("attribute");

                    description = property.getDescription("");
                    if (description != null) {
                        String descriptionText =
                        description.getDescription().trim();

                        if (descriptionText != null) {
                            writer.startElement("description");
                            StringBuffer sb = new StringBuffer();
                            sb.append("<![CDATA[");
                            sb.append(descriptionText);
                            sb.append("]]>\n");
                            writer.writeText(sb.toString());
                            writer.closeElement();
                        }
                    }

                    String propertyName = property.getPropertyName();

                    writer.startElement("name");
                    writer.writeText(propertyName);
                    writer.closeElement();

                    writer.startElement("required");
                    writer.writeText(property.isRequired() ?
                                     Boolean.TRUE.toString() :
                                     Boolean.FALSE.toString());
                    writer.closeElement();

                    if (!"id".equals(propertyName)) {

                        if (property.isMethodExpressionEnabled()) {
                            writer.startElement("deferred-method");
                            writer.startElement("method-signature");
                            writer.writeText(
                                property.getMethodSignature());
                            writer.closeElement(2);
                        } else {
                            // PENDING FIX ME
                            String type = property.getPropertyClass();
//                            String wrapperType = (String)
//                                GeneratorUtil.convertToPrimitive(type);
//                            if (wrapperType != null) {
//                                type = wrapperType;
//                            }
                            writer.startElement("deferred-value");
                            writer.startElement("type");
                            writer.writeText(type);
                            writer.closeElement(2);
                        }

                    } else {
                        writer.startElement("rtexprvalue");
                        writer.writeText(getRtexprvalue(tagName, propertyName));
                        writer.closeElement();
                    }

                    writer.closeElement(); // closes attribute element above

                } // END property FOR loop


                // Renderer Attributes Next...
                //
                AttributeBean[] attributes = renderer.getAttributes();
                AttributeBean attribute;
                for (int i = 0, len = attributes.length; i < len; i++) {
                    if (null == (attribute = attributes[i])) {
                        continue;
                    }
                    if (!attribute.isTagAttribute()) {
                        continue;
                    }
                    if (attributeShouldBeExcluded(renderer,
                        attribute.getAttributeName())) {
                        continue;
                    }

                    writer.startElement("attribute");

                    description = attribute.getDescription("");
                    if (description != null) {
                        String descriptionText =
                        description.getDescription().trim();

                        if (descriptionText != null) {
                            writer.startElement("description");
                            StringBuffer sb = new StringBuffer();
                            sb.append("<![CDATA[");
                            sb.append(descriptionText);
                            sb.append("]]>\n");
                            writer.writeText(sb.toString());
                            writer.closeElement();
                        }
                    }

                    String attributeName = attribute.getAttributeName();

                    writer.startElement("name");
                    writer.writeText(attributeName);
                    writer.closeElement();

                    writer.startElement("required");
                    writer.writeText(attribute.isRequired() ?
                                     Boolean.TRUE.toString() :
                                     Boolean.FALSE.toString());
                    writer.closeElement();

                    if (!"id".equals(attributeName)) {
                        // PENDING FIX ME
                        String type = attribute.getAttributeClass();
                        //String wrapperType = (String)
                        //  GeneratorUtil.convertToPrimitive(type);
                        //if (wrapperType != null) {
                        //    type = wrapperType;
                        //}
                        writer.startElement("deferred-value");
                        writer.startElement("type");
                        writer.writeText(type);
                        writer.closeElement(2);

                    } else {
                        writer.startElement("rtexprvalue");
                        writer.writeText(getRtexprvalue(tagName, attributeName));
                        writer.closeElement();
                    }

                    writer.closeElement(); // closes attribute element above

                } // END attribute FOR loop

                // SPECIAL: "Binding" needs to exist on every tag..
                writer.startElement("attribute");

                writer.startElement("description");
                writer.writeText(
                    "The ValueExpression linking this component to a property in a backing bean");
                writer.closeElement();

                writer.startElement("name");
                writer.writeText("binding");
                writer.closeElement();

                writer.startElement("required");
                writer.writeText("false");
                writer.closeElement();

                writer.startElement("deferred-value");
                writer.startElement("type");
                writer.writeText("javax.faces.component.UIComponent");
                writer.closeElement(2);

                // close the most recent attribute, and tag
                // elements
                writer.closeElement(2);

            }
        }

        //Include any other tags defined in the optional tag definition file.
        //These might be tags that were not picked up because they have no renderer
        //- for example "column".
        String tagDef = loadOptionalTags();
        if (tagDef != null) {
            writer.write(tagDef);
        }

    } // END writeTags   

}
