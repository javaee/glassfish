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

import com.sun.jsftemplating.layout.descriptors.handler.Handler;

import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;


/**
 *  <p>	This interface defines the methods required by
 *	{@link DynamicPropertySheetFactory}.  By providing these methods, you are
 *	able to interface some tree structure with the
 *	{@link DynamicPropertySheetFactory} so that whole or partial trees can be
 *	created without having to do any tree conversion work (the work is done
 *	by the <code>TreeAdaptor</code> implementation in conjunction with the
 *	{@link DynamicPropretySheetFactory}).</p>
 *
 *  <p> The <code>TreeAdaptor</code> implementation must have a <code>public
 *	static TreeAdaptor getInstance(FacesContext, LayoutComponent,
 *	UIComponent)</code> method in order to get access to an instance of the
 *	<code>TreeAdaptor</code> instance.</p>
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
public interface PropertySheetAdaptor {

    /**
     *	<p> This method is called shortly after
     *	    getInstance(FacesContext, LayoutComponent, UIComponent).  It
     *	    provides a place for post-creation initialization to take occur.</p>
     */
    public void init();


    /**
     *	<p> Returns the <code>PropertySheet</code>s for the given
     *	    <code>PropertySheet</code> model Object.</p>
     */
    public UIComponent getPropertySheet(UIComponent parent);


    /**
     *	<p> This method returns the <code>UIComponent</code> factory class
     *	    implementation that should be used to create a
     *	    <code>PropertySheet</code> for the given tree node model object.</p>
     */
    public String getFactoryClass();


    /**
     *	<p> This method returns the "options" that should be supplied to the
     *	    factory that creates the <code>PropertySheet</code>.</p>
     *
     *	<p> Some useful options for the standard <code>PropertySheet</code>
     *	    component include:<p>
     *
     * <ul><li>propertySheetId</li>
     * <li>propertySheetSectionIdTag</li>
     * <li>propertyIdTag</li>
     * <li>staticTextIdTag</li>
     * <li>dropDownIdTag</li>
     * <li>dropDownDefaultLevel</li>
     * <li>hiddenFieldIdTag</li>
     * <li>componentName</li>
     * <li>targetName</li>
     * <li>instanceName</li>
     * <li>propertySheetAdaptorClass</li></ul>
     * 
     *	<p> See PropertySheet component documentation for more details.</p>
     */
    public Map<String, Object> getFactoryOptions();


    /**
     *	<p> This method returns the <code>id</code> for the given tree node
     *	    model object.</p>
     */
    public String getId();


    /**
     *	<p> This method returns any facets that should be applied to the
     *	    <code>PropertySheet (comp)</code>.  Useful facets for the sun
     *	    <code>PropertySheet</code> component are: "content" and "image".</p>
     *
     *	<p> Facets that already exist on <code>comp</code>, or facets that
     *	    are directly added to <code>comp</code> do not need to be returned
     *	    from this method.</p>
     *
     *	@param	comp	    The tree node <code>UIComponent</code>.
     *	@param	nodeObject  The (model) object representing the tree node.
     */
    public Map<String, UIComponent> getFacets(UIComponent comp, Object nodeObject);


    /**
     *	<p> Advanced framework feature which provides better handling for
     *	    things such as expanding PropertySheets, beforeEncode, and other
     *	    events.</p>
     *
     *	<p> This method should return a <code>Map</code> of <code>List</code>
     *	    of <code>Handler</code> objects.  Each <code>List</code> in the
     *	    <code>Map</code> should be registered under a key that cooresponds
     *	    to to the "event" in which the <code>Handler</code>s should be
     *	    invoked.</p>
     */
    public Map<String, List<Handler>> getHandlersByType(UIComponent comp, Object nodeObject);
}
