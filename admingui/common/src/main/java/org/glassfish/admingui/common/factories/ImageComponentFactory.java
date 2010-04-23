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
package org.glassfish.admingui.common.factories;

import com.sun.jsftemplating.annotation.UIComponentFactory;
import com.sun.jsftemplating.component.factory.ComponentFactoryBase;
import com.sun.jsftemplating.layout.descriptors.LayoutComponent;

import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;


/**
 *  <p>	This factory is responsible for creating a <code>ImageComponent</code>
 *	UIComponent.  It has a special feature which turns an image into a
 *	"fake" hyperlink when an "href" attribute is supplied.  This "fake"
 *	hyperlink cannot be "tabbed to".  This is useful if you have an image
 *	and a hyperlink next to eachother and do not want a screen reader to
 *	read both links.  If an href is supplied, a "target" attribute may
 *	optionally be supplied as well.</p>
 *
 *  <p>	The {@link com.sun.jsftemplating.layout.descriptors.ComponentType}
 *	id for this factory is: "gf:image".</p>
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
@UIComponentFactory("gf:image")
public class ImageComponentFactory extends ComponentFactoryBase {

    /**
     *	<p> This is the factory method responsible for creating the
     *	    <code>ImageComponent</code> UIComponent.</p>
     *
     *	@param	context	    The FacesContext
     *
     *	@param	descriptor  The {@link LayoutComponent} descriptor that is
     *			    associated with the requested
     *			    <code>ImageComponent</code>.
     *
     *	@param	parent	    The parent UIComponent
     *
     *	@return	The newly created <code>ImageComponent</code>.
     */
    public UIComponent create(FacesContext context, LayoutComponent descriptor, UIComponent parent) {
	// Create the UIComponent
	UIComponent comp = createComponent(context, COMPONENT_TYPE, descriptor, parent);

	// Set all the attributes / properties (allow these to override theme)
	setOptions(context, descriptor, comp);
	String href = (String) descriptor.getOption("href");
	if (href != null) {
	    // This is meant to be a link as well, but we will do this via
	    // onClick to prevent 508 screen readers from tabbing to it.  This
	    // special feature is added to help accomodate cases where an
	    // image an link sit next to each other.  In this case, the screen
	    // reader should only read 1 of these, by making the image not
	    // tabbable, we solve this.
	    Map<String, Object> atts = comp.getAttributes();
	    String onclick = (String) atts.get("onClick");
	    if (onclick == null) {
		onclick = "";
	    }
	    onclick += "; admingui.ajax.loadPage({url: '" + href + "'});";
// selectTreeNode??

	    atts.put("onClick", onclick);
	    atts.put("onMouseOver", "this.oldStatus=window.status; window.status='" + href + "';");
	    atts.put("onMouseOut", "window.status=this.oldStatus;");
	    String style = (String) atts.get("style");
	    atts.put("style", "cursor:pointer;" + ((style == null) ? "" : style));
	}

	// Return the value
	return comp;
    }

    /**
     *	<p> The <code>UIComponent</code> type that must be registered in the
     *	    <code>faces-config.xml</code> file mapping to the UIComponent class
     *	    to use for this <code>UIComponent</code>.</p>
     */
    public static final String COMPONENT_TYPE	= "com.sun.webui.jsf.Image";
}
