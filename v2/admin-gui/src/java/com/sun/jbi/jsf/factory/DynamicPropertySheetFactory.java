/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.jbi.jsf.factory;

import com.sun.jsftemplating.annotation.UIComponentFactory;
import com.sun.jsftemplating.component.ComponentUtil;
import com.sun.jsftemplating.component.factory.ComponentFactoryBase;
import com.sun.jsftemplating.layout.descriptors.LayoutComponent;
import com.sun.jsftemplating.layout.descriptors.handler.Handler;
import com.sun.jsftemplating.util.Util;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.faces.context.FacesContext;
import javax.faces.component.UIComponent;

import com.sun.webui.jsf.component.StaticText;


/**
 *  <p>	Some property sheet are non-static, or Dynamic.  By this it is meant
 *	that some of the property elements can only be determined at Runtime.  
 *      The goal of this factory is to provide a means for dynamic property
 *      components to be defined. </p>
 *
 *  <p>	The {@link com.sun.jsftemplating.layout.descriptors.ComponentType}
 *	id for this factory is: "dynamicPropertySheet".</p>
 *
 */
@UIComponentFactory("dynamicPropertySheet")
public class DynamicPropertySheetFactory extends ComponentFactoryBase
{

    /**
     *	Constructor
     */
    public DynamicPropertySheetFactory() {
    }


    /**
     *	<p> This is the factory method responsible for creating the
     *	    <code>UIComponent</code>.</p>
     *
     *	@param	context	    The <code>FacesContext</code>
     *	@param	descriptor  The {@link LayoutComponent} descriptor associated
     *			    with the requested <code>UIComponent</code>.
     *	@param	parent	    The parent <code>UIComponent</code>
     *
     *	@return	The newly created component.
     */
    public UIComponent create(FacesContext context, LayoutComponent descriptor, UIComponent parent) {

        // Get the PropertySheetAdaptor which should be used
        PropertySheetAdaptor propertySheetAdaptor = getPropertySheetAdaptor(context, 
                                                                            descriptor, 
                                                                            parent);

        // Initialize the PropertySheetAdaptor instance
        propertySheetAdaptor.init();

        // Create the Property Sheet
        //String propertySheetId = propertySheetAdaptor.getPropertySheetId();
        String propertySheetId = propertySheetAdaptor.getId();
        return createPropertySheet(context,
                                   propertySheetAdaptor,
                                   propertySheetId,
                                   parent);
    }

    /**
     *	<p> This method gets the <code>PropertySheetAdaptor</code> by looking at the
     *	{@link #PROPERTY_SHEET_ADAPTOR_CLASS} option and invoking
     *	<code>getInstance</code> on the specified <code>PropertySheetAdaptor</code>
     *	implementation.</p>
     */
    protected PropertySheetAdaptor getPropertySheetAdaptor(FacesContext ctx, LayoutComponent desc, UIComponent parent) {
        PropertySheetAdaptor adaptor = null;
        Object cls = desc.getEvaluatedOption(ctx, PROPERTY_SHEET_ADAPTOR_CLASS, parent);
        if (cls == null)
        {
            throw new IllegalArgumentException("'" + PROPERTY_SHEET_ADAPTOR_CLASS
                                               + "' must be specified!");
        }
        try
        {
            Class adaptorClass = Util.getClass(cls);
            adaptor = (PropertySheetAdaptor) adaptorClass.getMethod("getInstance",
                    (Class []) new Class[] {FacesContext.class,
                     LayoutComponent.class, UIComponent.class}).invoke((Object) null,
                     (Object []) new Object[] {ctx,desc, parent});
        } catch (ClassNotFoundException ex)
        {
            throw new RuntimeException(ex);
        } catch (NoSuchMethodException ex)
        {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex)
        {
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex)
        {
            throw new RuntimeException(ex);
        }

        // Return the PropertySheetAdaptor
        return adaptor;
    }


    /**
     *	<p> This method creates the </code>PropertySheet</code>
     */
    protected UIComponent createPropertySheet (FacesContext ctx,
                                               PropertySheetAdaptor adaptor,
                                               String propertySheetId,
                                               UIComponent parent)
    {
       // String id = adaptor.getPropertySheetId();
        String id = adaptor.getId();
        String factoryClass = adaptor.getFactoryClass();
        Map<String, Object> props = adaptor.getFactoryOptions();
        Properties properties = Util.mapToProperties(props);

        // Create "this" Property Sheet component that will have
        // the property sheet section and property(s) added to it
        UIComponent sheet = ComponentUtil.getChild((UIComponent)parent, 
                                                   id, 
                                                   factoryClass, 
                                                   properties);

        // Use the adaptor class "getPropertySheetComponent" to generate
        // the dynamic property sheet.  Note, the parent PropertySheet "sheet" 
        // is passed into the adaptor method and the property sheet section 
        // and property(s) are generated and added to this property sheet.
        sheet = adaptor.getPropertySheet(sheet);

	// Add the Facets and Handlers
	configurePropertySheet(ctx, adaptor, sheet, propertySheetId);

        // Add the newly created PropertySheet to the parent class.
        parent.getChildren().add(sheet);
        return sheet;
    }


    /**
     *	<p> Adds on facets and handlers.</p>
     */
    protected void configurePropertySheet(FacesContext ctx, 
                                          PropertySheetAdaptor adaptor, 
                                          UIComponent propertySheet, 
                                          Object currentObj) {
        // Add facets (such as "content" and "image")
        Map<String, UIComponent> facets = adaptor.getFacets(propertySheet, currentObj);
        if (facets != null)
        {
            Map<String, UIComponent> propertySheetFacets = propertySheet.getFacets();
            Iterator<String> it = facets.keySet().iterator();
            String facetName;
            UIComponent facetValue;
            while (it.hasNext())
            {
                facetName = it.next();
                facetValue = facets.get(facetName);
                if (facetValue != null)
                {
                    propertySheetFacets.put(facetName, facetValue);
                }
            }
        }

        // Add instance handlers
        Map<String, List<Handler>> handlersByType =
        adaptor.getHandlersByType(propertySheet, currentObj);
        if (handlersByType != null)
        {
            Iterator<String> it = handlersByType.keySet().iterator();
            if (it.hasNext())
            {
                String eventType = null;
                Map<String, Object> compAttrs = propertySheet.getAttributes();
                while (it.hasNext())
                {
                    // Assign instance handlers to attribute for retrieval later
                    //   (Retrieval must be explicit, see LayoutElementBase)
                    eventType = it.next();
                    compAttrs.put(eventType, handlersByType.get(eventType));
                }
            }
        }
    }

    /**
     *	<p> This is the option that must be supplied when using this factory
     *	    in order to specify which PropertySheetAdaptor instance should be used.
     *	    The value should be a fully qualified class name of a valid
     *	    PropertySheetAdaptor instance.  The PropertySheetAdaptor instance must have a
     *	    <code>public static PropertySheetAdaptor getInstance(FacesContext,
     *	    LayoutComponent, UIComponent)</code> method in order to get access
     *	    to an instance of the PropertySheetAdaptor instance.</p>
     */
    public static final String PROPERTY_SHEET_ADAPTOR_CLASS = "propertySheetAdaptorClass";
}
