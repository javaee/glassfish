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
package org.glassfish.admingui.handlers;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import com.sun.jsftemplating.layout.LayoutDefinitionManager;
import com.sun.jsftemplating.layout.LayoutViewHandler;
import com.sun.jsftemplating.layout.descriptors.LayoutDefinition;

import org.glassfish.admingui.plugin.ConsolePluginService;
import org.glassfish.admingui.plugin.IntegrationPoint;
import org.glassfish.admingui.plugin.IntegrationPointComparator;

import org.jvnet.hk2.component.Habitat;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;


/**
 *  <p>	This class will provide JSFTemplating <code>Handler</code>s that
 *	provide access to {@link IntegrationPoint}s and possibily other
 *	information / services needed to provide plugin functionality 
 *	i.e. getting resources, etc.).</p>
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
public class PluginHandlers {

    /**
     *	<p> Constructor.</p>
     */
    protected PluginHandlers() {
    }

    /**
     *	<p> Find and return the <code>ConsolePluginService</code>.  This method
     *	    uses the HK2 <code>Habitat</code> to locate the
     *	    <code>ConsolePluginService</code>.</p>
     *
     *	@param	ctx The <code>FacesContext</code>.
     *
     *	@returns The <code>ConsolePluginService</code>.
     */
    private static ConsolePluginService getPluginService(FacesContext ctx) {
	// We need to get the ServletContext to find the Habitat
	ServletContext servletCtx = (ServletContext)
	    (ctx.getExternalContext()).getContext();

	// Get the Habitat from the ServletContext
	Habitat habitat = (Habitat) servletCtx.getAttribute(
	    org.glassfish.admingui.common.plugin.ConsoleClassLoader.HABITAT_ATTRIBUTE);

//	System.out.println("Habitat:" + habitat);

	return habitat.getByType(ConsolePluginService.class);
    }


    /**
     *	<p> This handler provides access to {@link IntegrationPoint}s for the requested key.</p>
     *
     *	@param	context	The <code>HandlerContext</code>.
     */
    @Handler(id="getIntegrationPoints",
    	input={
            @HandlerInput(name="type", type=String.class, required=true)},
        output={
            @HandlerOutput(name="points", type=List.class)})
    public static void getIntegrationPoints(HandlerContext handlerCtx) {
	String type = (String) handlerCtx.getInputValue("type");
	List<IntegrationPoint> value =
	    getIntegrationPoints(handlerCtx.getFacesContext(), type);
	handlerCtx.setOutputValue("points", value);
    }

    /**
     *
     */
    public static List<IntegrationPoint> getIntegrationPoints(FacesContext context, String type) {
	return getPluginService(context).getIntegrationPoints(type);
    }

    /**
     *	<p> This handler adds {@link IntegrationPoint}s of a given type to a
     *	    <code>UIComponent</code> tree.  It looks for
     *	    {@link IntegrationPoint}s using the given <code>type</code>.  It
     *	    then sorts the results (if any) by <code>parentId</code>, and then
     *	    by priority.  It next interates over each one looking for a
     *	    <code>UIComponent</code> with an <code>id</code> which matches the
     *	    its own <code>parentId</code> value.  It then uses the content of
     *	    the {@link IntegrationPoint} to attempt to include the .jsf page
     *	    it refers to under the identified parent component.</p>
     */
    @Handler(id="includeIntegrations",
    	input={
            @HandlerInput(name="type", type=String.class, required=true),
	    @HandlerInput(name="root", type=UIComponent.class, required=false)})
    public static void includeIntegrations(HandlerContext handlerCtx) {
	// Get the input
	String type = (String) handlerCtx.getInputValue("type");
	UIComponent root = (UIComponent) handlerCtx.getInputValue("root");

	// Get the IntegrationPoints
	FacesContext ctx = handlerCtx.getFacesContext();
	List<IntegrationPoint> points = getIntegrationPoints(ctx, type);

	// Include them
	includeIntegrationPoints(ctx, root, points);
    }

    /**
     *
     */
    public static void includeIntegrationPoints(FacesContext ctx, UIComponent root, List<IntegrationPoint> points) {
	if (root == null) {
	    // No root is specified, search whole page
	    root = ctx.getViewRoot();
	}

	// Use a TreeSet to sort automatically
	SortedSet<IntegrationPoint> sortedSet =
	    new TreeSet<IntegrationPoint>(
		IntegrationPointComparator.getInstance());
// FIXME: Check for duplicates! Modify "id" if there is a duplicate?
	sortedSet.addAll(points);

	// Iterate
	IntegrationPoint point;
	Iterator<IntegrationPoint> it = null;
	int lastSize = 0;
	int currSize = sortedSet.size();
	String lastParentId = null;
	while (currSize != lastSize) {
	    // Stop loop by comparing previous size
	    lastSize = currSize;
	    it = sortedSet.iterator();
	    lastParentId = "";
	    UIComponent parent = null;

	    // Iterate through the IntegrationPoints
	    while (it.hasNext()) {
		point = it.next();

		// Optimize for multiple plugins for the same parent
		String parentId = point.getParentId();
		if (parentId == null) {
		    // If not specified, just stick it @ the root
		    parentId = root.getId();
		    parent = root;
		} else if (!parentId.equals(lastParentId)) {
		    parent = findComponentById(root, parentId);
		    if (parent == null) {
			// Didn't find the one specified!
// FIXME: log FINE!  Note this may not be a problem, keep iterating to see if we find it later.
System.out.println("The specified parentId (" + parentId + ") was not found!"); 
			lastParentId = null;
			continue;
		    }
		    lastParentId = parentId;
		}

		// We found the parent, remove from our list of IPs to add
		it.remove();

		// Add the content
		String content = point.getContent();
		while (content.startsWith("/")) {
		    content = content.substring(1);
		}
		LayoutDefinition def =
		    LayoutDefinitionManager.getLayoutDefinition(ctx,
			"/" + point.getConsoleConfigId() + "/" + content);
		LayoutViewHandler.buildUIComponentTree(ctx, parent, def);
	    }

	    // Get the set size to see if we have any left to process
	    currSize = sortedSet.size();
	}
    }

    /**
     *	<p> This method search for the requested simple id in the given
     *	    <code>UIComponent</code>.  If the id matches the UIComponent, it
     *	    is returned, otherwise, it will search the children and facets
     *	    recursively.</p>
     *
     *	@param	base	The <code>UIComponent</code> to search.
     *	@param	id	The <code>id</code> we're looking for.
     *
     *	@return	The UIComponent, or null.
     */
    private static UIComponent findComponentById(UIComponent base, String id) {
	// Check if this is the one we're looking for
	if (id.equals(base.getId())) {
	    return base;
	}

	// Not this one, check its kids
	Iterator<UIComponent> it = base.getFacetsAndChildren();
	UIComponent comp = null;
	while (it.hasNext()) {
	    // Recurse
	    comp = findComponentById(it.next(), id);
	    if (comp != null) {
		// Found!
		return comp;
	    }
	}

	// Not found
	return null;
    }
}
