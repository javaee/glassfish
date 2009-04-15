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
import javax.servlet.ServletContext;

/**
 * The NavigationNodeFactory provides an abstraction layer for the Woodstock treeNode
 * component (currently), giving us the ability to change the treeNode implementation to
 * another component or set, or a different component type altogether.  The supported
 * attributes are:
 * <ul>
 * <li>id</li>
 * <li>label - The text label for the top of the tree</li>
 * <li>url - An optional URL</li>
 * <li>icon - The URL to an image for the tree's root icon</li>
 * <li>target</li>
 * <li>expanded</li>
 * </ul>
 */
@UIComponentFactory("gf:navNode")
public class NavigationNodeFactory extends ComponentFactoryBase {

    /**
     *	<p> This is the factory method responsible for creating the
     *	    <code>UIComponent</code>.</p>
     *
     *	@param	context	    The <code>FacesContext</code>
     *	@param	descriptor  The {@link LayoutComponent} descriptor associated
     *			    with the requested <code>UIComponent</code>.
     *	@param	parent	    The parent <code>UIComponent</code>
     *
     *	@return	The newly created <code>TreeNode</code>.
     */
    public UIComponent create(FacesContext context, LayoutComponent descriptor, UIComponent parent) {
        // Create the UIComponent
        UIComponent comp = createComponent(context, COMPONENT_TYPE, descriptor, parent);
        String compId = (String) descriptor.getId(context, comp.getParent());
        if ((compId != null) && (!compId.equals(""))) {
            comp.setId(compId);
        }

        Object url = descriptor.getOption("url");
        final Object icon = descriptor.getOption("icon");
        final Object label = descriptor.getOption("label");
        final Object target = descriptor.getOption("target");
        final Object expanded = descriptor.getOption("expanded");
        final Object template = descriptor.getOption("template");
        final Object processPage = descriptor.getOption("processPage");

        // Set all the attributes / properties
        if (label != null) {
            setOption(context, comp, descriptor, "text", label);
        }
        if (target != null) {
            setOption(context, comp, descriptor, "target", target);
        }
        if (expanded != null) {
            setOption(context, comp, descriptor, "expanded", expanded);
        }
        if (icon != null) {
            setOption(context, comp, descriptor, "imageURL", icon);
        }
        if (url != null) {
            if (template != null) {
                comp.getAttributes().put("realUrl", url);
                url = ((ServletContext)context.getExternalContext().getContext()).getContextPath() + "/" +
                        "pluginPage.jsf?id=" + comp.getClientId(context);
            }
            setOption(context, comp, descriptor, "url", url);
            if (icon != null) {
                UIComponent imageHyperlink = context.getApplication().createComponent("com.sun.webui.jsf.ImageHyperlink");
                setOption(context, imageHyperlink, descriptor, "imageURL", icon);
                setOption(context, imageHyperlink, descriptor, "url", url);
                setOption(context, imageHyperlink, descriptor, "border", "0");
                setOption(context, imageHyperlink, descriptor, "immediate", Boolean.TRUE);
                comp.getFacets().put("image", imageHyperlink);
            }
        }

        comp.getAttributes().put("processPage", (processPage != null) ? processPage : false);

        // Return the component
        return comp;
    }
    /**
     *	<p> The <code>UIComponent</code> type that must be registered in the
     *	    <code>faces-config.xml</code> file mapping to the UIComponent class
     *	    to use for this <code>UIComponent</code>.</p>
     */
    public static final String COMPONENT_TYPE = "com.sun.webui.jsf.TreeNode";
}
