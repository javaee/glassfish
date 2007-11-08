/*
 * $Id: GeneratorUtil.java,v 1.1 2005/09/20 21:11:37 edburns Exp $
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

import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.InputStream;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

import com.sun.faces.config.beans.ComponentBean;
import com.sun.faces.config.beans.RenderKitBean;
import com.sun.faces.config.beans.RendererBean;
import com.sun.faces.config.beans.FacesConfigBean;
import com.sun.faces.config.DigesterFactory;
import com.sun.faces.config.rules.FacesConfigRuleSet;

import org.xml.sax.InputSource;
import org.apache.commons.digester.Digester;

/**
 * <p>Utility methods that may be useful to all <code>Generators</code>.</p>
 */
public class GeneratorUtil {

    private static final String PREFIX = "javax.faces.";


     // The set of unwrapper methods for primitives, keyed by the primitive type
    private static Map<String,String> UNWRAPPERS = new HashMap<String, String>();
    static {
        UNWRAPPERS.put("boolean", "booleanValue");
        UNWRAPPERS.put("byte", "byteValue");
        UNWRAPPERS.put("char", "charValue");
        UNWRAPPERS.put("double", "doubleValue");
        UNWRAPPERS.put("float", "floatValue");
        UNWRAPPERS.put("int", "intValue");
        UNWRAPPERS.put("long", "longValue");
        UNWRAPPERS.put("short", "shortValue");
    }


    // The set of wrapper classes for primitives, keyed by the primitive type
    private static Map<String,String> WRAPPERS = new HashMap<String, String>();
    static {
        WRAPPERS.put("boolean", "java.lang.Boolean");
        WRAPPERS.put("byte", "java.lang.Byte");
        WRAPPERS.put("char", "java.lang.Character");
        WRAPPERS.put("double", "java.lang.Double");
        WRAPPERS.put("float", "java.lang.Float");
        WRAPPERS.put("int", "java.lang.Integer");
        WRAPPERS.put("long", "java.lang.Long");
        WRAPPERS.put("short", "java.lang.Short");
    }


    // ---------------------------------------------------------- Public Methods

    public static String convertToPrimitive(String objectType) {

        return UNWRAPPERS.get(objectType);

    }


    public static String convertToObject(String primitiveType) {

        return WRAPPERS.get(primitiveType);

    }

    /**
     * Obtain an instance of JspTldGenerator based on the JSP version
     * provided.
     */
    public static JspTLDGenerator getTldGenerator(PropertyManager propManager) {

        String version =
            propManager.getProperty(PropertyManager.JSP_VERSION_PROPERTY);
        if ("1.2".equals(version)) {
            return new JspTLD12Generator(propManager);
        } else if ("2.1".equals(version)) {
            return new JspTLD21Generator(propManager);
        } else {
            throw new IllegalArgumentException("Unsupported version of JSP '" +
                version + '\'');
        }
    }

    /**
     * <p>Strip any "javax.faces." prefix from the beginning of the specified
     * identifier, and return it.</p>
     *
     * @param identifier Identifier to be stripped
     */
    public static String stripJavaxFacesPrefix(String identifier) {

        if (identifier.startsWith(PREFIX)) {
            return (identifier.substring(PREFIX.length()));
        } else {
            return (identifier);
        }

    } // END stripJavaxFacesPrefix


    /**
     * Build the tag handler class name from componentFamily and rendererType.
     *
     * @param componentFamily the component family
     * @param rendererType the renderer type
     */
    public static String makeTagClassName(String componentFamily,
                                          String rendererType) {

        if (componentFamily == null) {
            return null;
        }
        String tagClassName = componentFamily;
        if (rendererType != null) {
            if (!componentFamily.equals(rendererType)) {
                tagClassName = tagClassName + rendererType;
            }
        }
        return tagClassName + "Tag";

    } // END makeTagClassName


    /**
     * @return a SortedMap, where the keys are component-family String entries,
     *         and the values are {@link com.sun.faces.config.beans.ComponentBean}
     *         instances Only include components that do not have a base
     *         component type.
     */
    public static Map<String,ComponentBean> getComponentFamilyComponentMap(
        FacesConfigBean configBean) {

        TreeMap<String,ComponentBean> result = new TreeMap<String, ComponentBean>();
        ComponentBean component;
        ComponentBean[] components = configBean.getComponents();
        for (int i = 0, len = components.length; i < len; i++) {
            component = components[i];
            if (component == null) {
                throw new IllegalStateException("No Components Found");
            }
            if (component.getBaseComponentType() != null) {
                continue;
            }
            String componentFamily = component.getComponentFamily();

            result.put(componentFamily, component);
        }

        return result;

    } // END getComponentFamilyComponentMap


    public static Map<String,ArrayList<RendererBean>> getComponentFamilyRendererMap(FacesConfigBean configBean,
                                                    String renderKitId) {

        RenderKitBean renderKit = configBean.getRenderKit(renderKitId);
        if (renderKit == null) {
            throw new IllegalArgumentException("No RenderKit for id '" +
                renderKitId + '\'');
        }

        RendererBean[] renderers = renderKit.getRenderers();
        if (renderers == null) {
            throw new IllegalStateException("No Renderers for RenderKit id" +
                '"' + renderKitId + '"');
        }

        TreeMap<String,ArrayList<RendererBean>> result = new TreeMap<String, ArrayList<RendererBean>>();

        for (int i = 0, len = renderers.length; i < len; i++) {
            RendererBean renderer = renderers[i];

            if (renderer == null) {
                throw new IllegalStateException("no Renderer");
            }

            // if this is the first time we've encountered this
            // componentFamily
            String componentFamily = renderer.getComponentFamily();
            ArrayList<RendererBean> list = result.get(componentFamily);
            if (list == null) {
                // create a list for it
                list = new ArrayList<RendererBean>();
                list.add(renderer);
                result.put(componentFamily, list);
            } else {
                list.add(renderer);
            }
        }

        return result;

    } // END getComponentFamilyRendererMap


    public static FacesConfigBean getConfigBean(String facesConfig)
    throws Exception {

        FacesConfigBean fcb = null;
        InputStream stream = null;
        try {
            File file = new File(facesConfig);
            stream = new BufferedInputStream(new FileInputStream(file));
            InputSource source = new InputSource(file.toURL().toString());
            source.setByteStream(stream);
            fcb = (FacesConfigBean)
                createDigester(true, false, true).parse(source);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {
                    ;
                }
                stream = null;
            }
        }
        return (fcb);

    } // END getConfigBean

    
    // --------------------------------------------------------- Private Methods


    /**
     * <p>Configure and return a <code>Digester</code> instance suitable for
     * use in the environment specified by our parameter flags.</p>
     *
     * @param design Include rules suitable for design time use in a tool
     * @param generate Include rules suitable for generating component,
     * renderer, and tag classes
     * @param runtime Include rules suitable for runtime execution
     */
    private static Digester createDigester(boolean design,
                                       boolean generate, boolean runtime) {

        Digester digester = DigesterFactory.newInstance(true).createDigester();

        // Configure parsing rules
        digester.addRuleSet(new FacesConfigRuleSet(design, generate, runtime));

        // Configure preregistered entities

        return (digester);

    } // END createDigester

}
