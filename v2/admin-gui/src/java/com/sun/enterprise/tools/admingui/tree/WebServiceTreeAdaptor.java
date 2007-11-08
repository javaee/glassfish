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

package com.sun.enterprise.tools.admingui.tree;

import com.sun.enterprise.tools.admingui.tree.MBeanTreeAdaptor;
import com.sun.enterprise.tools.admingui.util.AMXUtil;

import com.sun.jsftemplating.component.factory.tree.TreeAdaptor;
import com.sun.jsftemplating.layout.descriptors.LayoutComponent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.component.ActionSource;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import javax.management.ObjectName;


/**
 *  <p> The <code>WebServiceTreeAdaptor</code> implementation must have a
 *	<code>public static WebServiceTreeAdaptor getInstance(FacesContext,
 *	LayoutComponent, UIComponent)</code> method in order to get access to
 *	an instance of the <code>WebServiceTreeAdaptor</code> instance.</p>
 *
 *  <p>	This class is used by <code>DynamicTreeNodeFactory</code>.</p>
 *
 *  @author Jennifer Chou (jennifer.chou@sun.com)
 */
public class WebServiceTreeAdaptor extends MBeanTreeAdaptor {

    /**
     *	<p> This constructor is not used.</p>
     */
  //  private WebServiceTreeAdaptor() {
  //  }

    /**
     *	<p> This constructor saves the <code>LayoutComponent</code> descriptor
     *	    and the <code>UIComponent</code> associated with this
     *	    <code>TreeAdaptor</code>.  This constructor is used by the
     *	    getInstance() method.</p>
     */
    protected WebServiceTreeAdaptor(LayoutComponent desc, UIComponent parent) {
	super(desc, parent);
    }

    /**
     *	<p> This method provides access to an <code>WebServiceTreeAdaptor</code>
     *	    instance.  Each time it is invoked, it returns a new instance.</p>
     */
    public static TreeAdaptor getInstance(FacesContext ctx, LayoutComponent desc, UIComponent parent) {
	return new WebServiceTreeAdaptor(desc, parent);
    }

    /**
     *	<p> This method is called shortly after
     *	    {@link #getInstance(FacesContext, LayoutComponent, UIComponent)}.
     *	    It provides a place for post-creation initialization to take
     *	    occur.</p>
     */
    public void init() {
	// Get the FacesContext
	FacesContext ctx = FacesContext.getCurrentInstance();

	// This is the descriptor for this dynamic TreeNode, it contains all
	// information (options) necessary for this Adaptor
	LayoutComponent desc = getLayoutComponent();

	// The parent UIComponent
	UIComponent parent = getParentUIComponent();

     	// Get the Object Name
	Object val = desc.getEvaluatedOption(ctx, "objectName", parent);
	if (val == null) {
	    throw new IllegalArgumentException(
		    "'objectName' must be specified!");
	}
        _objectName = (String) val;

	// The following method should set the "key" to the node containing all
	// the children... the children will also have keys which must be
	// retrievable by the next method (getChildTreeNodeObjects)... these
	// "keys" will be used by the rest of the methods in this file for
	// getting information about the TreeNode that should be built.
	setTreeNodeObject(_objectName);
    }

    /**
     *	<p> Returns child <code>TreeNode</code>s for the given
     *	    <code>TreeNode</code> model Object.</p>
     */
    public List getChildTreeNodeObjects(Object nodeObject) {
	if (nodeObject == null) {
	    return null;
	}
	if (nodeObject.toString().equals(_objectName)) {
	    // In this implementation _objectName represents the top-level,
	    // we need to find its children here
	    if (_children != null) {
		return Arrays.asList((Object[])_children);
	    }          
            
            Map<Object,String> m = AMXUtil.getWebServiceMgr().getWebServiceEndpointKeys();
            Map newMap = new HashMap();
            HashSet names = new HashSet();
            // If a web service has the same name, append the application name
            // to make unique. ie, "HelloIF (jaxrpc-simple)"
            HashSet duplicateNames = new HashSet();
            for (Iterator iter = m.entrySet().iterator();iter.hasNext();) {
                Map.Entry entry = (Map.Entry)iter.next();
                String name = (String)entry.getValue();
                String fullName = (String)entry.getKey();
                if (!names.add(name)) {
                    duplicateNames.add(name);
                    String appName = fullName.substring(0, fullName.indexOf('#'));
                    names.add(name + " ("+ appName +")");
                    newMap.put(fullName, name + " ("+ appName +")");
                } else {
                    newMap.put(fullName, name);
                }
            }
            for (Iterator iter = m.entrySet().iterator();iter.hasNext();) {
                Map.Entry entry = (Map.Entry)iter.next();
                String name = (String)entry.getValue();
                if (duplicateNames.contains(name)) {
                    names.remove(name);
                    String fullName = (String)entry.getKey();
                    String appName = fullName.substring(0, fullName.indexOf('#'));
                    names.add(name + " ("+ appName +")");
                    newMap.put(fullName, name + " ("+ appName +")");
                }
            }
            _children = newMap.entrySet().toArray();

	    // Ok, we got the result, provide an event in case we want to
	    // do some filtering
	    FacesContext ctx = FacesContext.getCurrentInstance();
	    Object retVal = getLayoutComponent().dispatchHandlers(
		    ctx, FilterTreeEvent.EVENT_TYPE,
		    new FilterTreeEvent(getParentUIComponent(), _children));
	    if ((retVal != null) && (retVal instanceof Object [])) {
		// We have a return value, use it instead of the original list
		_children = (Object []) retVal;
	    }
	} else {
	    // Currently multiple levels are not implemented
	    return null;
	}

	return _children != null ? Arrays.asList((Object[])_children):null; 
    }

    /**
     *	<p> This method returns the "options" that should be supplied to the
     *	    factory that creates the <code>TreeNode</code> for the given tree
     *	    node model object.</p>
     *
     *	<p> Some useful options for the standard <code>TreeNode</code>
     *	    component include:<p>
     *
     *	<ul><li>text</li>
     *	    <li>url</li>
     *	    <li>imageURL</li>
     *	    <li>target</li>
     *	    <li>action<li>
     *	    <li>actionListener</li>
     *	    <li>expanded</li></ul>
     *
     *	<p> See Tree / TreeNode component documentation for more details.</p>
     */
    public Map<String, Object> getFactoryOptions(Object nodeObject) {
	if (nodeObject == null) {
	    return null;
	}

	LayoutComponent desc = getLayoutComponent();
	Map<String, Object> props = new HashMap<String, Object>();
	if (nodeObject.toString().equals(_objectName)) {
	    // This case deals with the top node.

	    // NOTE: All supported options must be handled here,
	    //		otherwise they'll be ignored.
	    // NOTE: Options will be evaluated later, do not eval here.
	    setProperty(props, "text", desc.getOption("text"));
	    setProperty(props, "url", desc.getOption("url"));
	    setProperty(props, "imageURL", desc.getOption("imageURL"));
	    setProperty(props, "target", desc.getOption("target"));
	    setProperty(props, "action", desc.getOption("action"));

	    // NOTE: Although actionListener is supported, LH currently
	    //	     implements this to be the ActionListener of the "turner"
	    //	     which is inconsistent with "action".  We should make use
	    //	     of the "Handler" feature which provides a "toggle"
	    //	     CommandEvent.
	    setProperty(props, "actionListener", desc.getOption("actionListener"));
	    setProperty(props, "expanded", desc.getOption("expanded"));
	} else {
	    // This case deals with the children

	    // NOTE: All supported options must be handled here,
	    // otherwise they'll be ignored

	    if (nodeObject instanceof Map.Entry) {
		setProperty(props, "text",
			(String)((Map.Entry) nodeObject).getValue());
                setProperty(props, "webServiceKey", 
                    (Object)((Map.Entry) nodeObject).getKey());
                setProperty(props, "webServiceName",
                    (String)((Map.Entry) nodeObject).getValue());
                
	    } else {
		throw new RuntimeException("'" + nodeObject
			+ "' Illegal type ("
			+ nodeObject.getClass().getName()
			+ ") for tree processing");
	    }

	    // Finish setting the child properties
	    setProperty(props, "url", desc.getOption("childURL"));
	    setProperty(props, "imageURL", desc.getOption("childImageURL"));
	    setProperty(props, "target", desc.getOption("childTarget"));
	    setProperty(props, "action", desc.getOption("childAction"));
// We are using "childActionListener" for the hyperlink, not the TreeNode
//	    setProperty(props, "actionListener", desc.getOption("childActionListener"));
	    setProperty(props, "expanded", desc.getOption("childExpanded"));
	}

	// Return the options
	return props;
    }
    
    /**
     *	The MBean object name.
     */
 //  private String  objectName	=   null;
    
    /**
     *	This sub-nodes of the top-level Node.
     */
//   private Object[]	children	=   null;

}
