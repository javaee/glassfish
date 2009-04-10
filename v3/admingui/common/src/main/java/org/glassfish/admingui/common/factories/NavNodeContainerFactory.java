/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.common.factories;

import com.sun.jsftemplating.annotation.UIComponentFactory;
import com.sun.jsftemplating.component.factory.ComponentFactoryBase;
import com.sun.jsftemplating.layout.descriptors.LayoutComponent;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 * The NavigationFactory provides an abstraction layer for the Woodstock tree
 * component (currently), giving us the ability to change the tree implementation to
 * another component or set, or a different component type altogether.  The supported
 * attributes are:
 * <ul>
 * <li>id</li>
 * <li>label - The text label for the top of the tree</li>
 * <li>url - An optional URL</li>
 * <li>icon - The URL to an image for the tree's root icon</li>
 * <li>target</li>
 * </ul>
 */
@UIComponentFactory("gf:navNodeContainer")
public class NavNodeContainerFactory extends ComponentFactoryBase {

    /**
     *	<p> This is the factory method responsible for creating the
     *	    <code>UIComponent</code>.</p>
     *
     *	@param	context	    The <code>FacesContext</code>
     *	@param	descriptor  The {@link LayoutComponent} descriptor associated
     *			    with the requested <code>UIComponent</code>.
     *	@param	parent	    The parent <code>UIComponent</code>
     *
     *	@return	The newly created <code>Tree</code>.
     */
    public UIComponent create(FacesContext context, LayoutComponent descriptor, UIComponent parent) {
        // Create the UIComponent
        UIComponent comp = createComponent(context, COMPONENT_TYPE, descriptor, parent);

        final Object url = descriptor.getOption("url");
        final Object icon = descriptor.getOption("icon");
        final Object label = descriptor.getOption("label");
        final Object target = descriptor.getOption("target");

        if (label != null) {
            setOption(context, comp, descriptor, "text",label);
        }
        if (target != null) {
            setOption(context, comp, descriptor, "target",target);
        }
        if (icon != null) {
            setOption(context, comp, descriptor, "imageURL", icon);
        }
        if (url != null) {
            setOption(context, comp, descriptor, "url", url);
        }
        setOption(context, comp, descriptor, "clientSide", Boolean.TRUE);

        // Return the component
        return comp;
    }
    /**
     *	<p> The <code>UIComponent</code> type that must be registered in the
     *	    <code>faces-config.xml</code> file mapping to the UIComponent class
     *	    to use for this <code>UIComponent</code>.</p>
     */
    public static final String COMPONENT_TYPE = "com.sun.webui.jsf.Tree";
}
