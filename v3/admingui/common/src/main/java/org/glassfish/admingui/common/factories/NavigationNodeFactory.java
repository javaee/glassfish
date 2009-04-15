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
 *  <li>__id__ - The ID of the component.  While IDs are optional, it is a good idea to provide a specific ID,
 *      especially if one expects to want to add nodes under this node in the future.</li>
 *  <li>__label__ - The text label for the navigation node</li>
 *  <li>__url__ - An optional URL</li>
 *  <li>__icon__ - The URL to an image for the tree's root icon</li>
 *  <li>__target__ - An optional target to specify on the link created for this node (e.g., '_blank')</li>
 *  <li>__expanded__ - A boolean indicating whether or not this node should be expanded by default.</li>
 *  <li>__template__ - An optional parameter indicate what template should be used to decorate the page to
 *      which this node links.  The value will be a relative path to a template file provided by the Admin
 *      Console or one of its plugins (e.g., '/pluginId/templates/customLayout.xhmtl').  The default value
 *      is <code>/layout.xhtml</code>.  If the <code>url</code> parameter points to an external resource,
 *      the URL rendered will point a page in the admin console.  This page will then read the contents of the
 *      users-specified URL and display those contents in the appropriate spot in the specified template.</li>
 *  <li>__processPage__ - This option is intended to be used in conjunction with the <code>template</code>
 *      parameter.  By default, the page read and displayed will be rendered as is.  If, however, the plugin
 *      author wishes the page to processed by the Admin Console runtime, the parameter should be set to "true."
 *      The URL referenced must then return valid markup.</li>
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
            final boolean externalResource = ((String) url).contains("://");
            if (externalResource) {
                comp.getAttributes().put("realUrl", url);
                comp.getAttributes().put("template", (template != null) ? template : "/templates/default.layout");
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
