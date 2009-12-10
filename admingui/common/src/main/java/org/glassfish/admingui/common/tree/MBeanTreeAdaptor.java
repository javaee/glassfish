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

package org.glassfish.admingui.common.tree;

import com.sun.jsftemplating.component.ComponentUtil;
import com.sun.jsftemplating.component.factory.tree.TreeAdaptor;
import com.sun.jsftemplating.component.factory.tree.TreeAdaptorBase;
import com.sun.jsftemplating.layout.descriptors.LayoutComponent;
import com.sun.jsftemplating.layout.event.CommandActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.faces.component.ActionSource;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.management.ObjectName;
import org.glassfish.admin.amx.config.AMXConfigProxy;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.admingui.common.util.V3AMX;


/**
 *  <p> The <code>MBeanTreeAdaptor</code> implementation must have a
 *	<code>public static MBeanTreeAdaptor getInstance(FacesContext,
 *	LayoutComponent, UIComponent)</code> method in order to get access to
 *	an instance of the <code>MBeanTreeAdaptor</code> instance.</p>
 *
 *  <p>	This class is used by <code>DynamicTreeNodeFactory</code>.</p>
 *
 *  @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class MBeanTreeAdaptor extends TreeAdaptorBase {

    /**
     *	<p> This constructor is not used.</p>
     */
    private MBeanTreeAdaptor() {
    }

    /**
     *	<p> This constructor saves the <code>LayoutComponent</code> descriptor
     *	    and the <code>UIComponent</code> associated with this
     *	    <code>TreeAdaptor</code>.  This constructor is used by the
     *	    getInstance() method.</p>
     */
    protected MBeanTreeAdaptor(LayoutComponent desc, UIComponent parent) {
	super(desc, parent);
    }

    /**
     *	<p> This method provides access to an <code>MBeanTreeAdaptor</code>
     *	    instance.  Each time it is invoked, it returns a new instance.</p>
     */
    public static TreeAdaptor getInstance(FacesContext ctx, LayoutComponent desc, UIComponent parent) {
	return new MBeanTreeAdaptor(desc, parent);
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

	// Get the Method Name
	/*
     val = desc.getEvaluatedOption(ctx, "methodName", parent);
	if (val == null) {
	    throw new IllegalArgumentException(
		    "'methodName' must be specified!");
	}
	_methodName = (String) val;
     */

    _amxChildType = (String) desc.getEvaluatedOption(ctx, "amxChildType", parent);

	// Get Parameters
	_paramsArray = null;
	val = desc.getEvaluatedOption(ctx, "parameters", parent);
	if (val != null) {
	    if (val instanceof List) {
		_paramsArray = ((List) val).toArray();
	    } else {
		_paramsArray = new Object[] {val};
	    }
	}

	// Get Parameter Types
	_paramTypesArray = null;
	val = desc.getEvaluatedOption(ctx, "paramTypes", parent);
	if (val != null) {
	    if (val instanceof String) {
		_paramTypesArray = new String[] {(String) val};
	    } else if (val instanceof List) {
		_paramTypesArray = (String [])
		    ((List<String>) val).toArray(new String[0]);
	    } else {
		throw new IllegalArgumentException(
			"'paramTypes' must be a String or a List of types!");
	    }
	}

	// Get the attribute name for the text (optional)
	_nameAtt = (String) desc.getEvaluatedOption(
		ctx, "attributeName", parent);
	if (_nameAtt != null) {
	    _nameAtt = _nameAtt.trim();
	    if (_nameAtt.length() == 0) {
		_nameAtt = null;
	    }
	}

	// Get the method name for the text (optional)
	_nameMethod = (String) desc.getEvaluatedOption(
		ctx, "attrNameMethod", parent);
	if (_nameMethod != null) {
	    _nameMethod = _nameMethod.trim();
	    if (_nameMethod.length() == 0) {
		_nameMethod = null;
	    }
	}

	/*
FIXME:	 See "DynamicTreeNode.java -- The code in getChildObjectNames should be
FIXME:	 broken up between this method and the next (getChildTreeNodeObjects).
FIXME:	 This file should only deal w/ the normal MBean use case.  WebServices
FIXME:	 should be handled via WebServiceTreeAdaptor (to be written).
	*/

	// The following method should set the "key" to the node containing all
	// the children... the children will also have keys which must be
	// retrievable by the next method (getChildTreeNodeObjects)... these
	// "keys" will be used by the rest of the methods in this file for
	// getting information about the TreeNode that should be built.
        
        String ignored = (String) desc.getEvaluatedOption(ctx, "ignored", parent);
        try{
            if ( ignored == null || "false".equals(ignored)){
                setTreeNodeObject( new ObjectName(_objectName));
            }else{
                setTreeNodeObject(null);
            }
        }catch(Exception ex){
            System.out.println("ERROR: Illegal _objectName: " + _objectName);
            setTreeNodeObject(null);
        }
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
		return _children;	    }
            
            try{
                AMXConfigProxy  amx = (AMXConfigProxy) V3AMX.getInstance().getProxyFactory().getProxy(new ObjectName(_objectName));
                if (_amxChildType == null || _amxChildType.equals("")){
                    ObjectName[] children = amx.getChildren();
                    _children = new ArrayList();
                    for(int i=0; i< children.length; i++){
                        _children.add(children[i]);
                    }
                }else {
                    _children = new ArrayList();
                    Map<String,AMXProxy> childrenMap = amx.childrenMap(_amxChildType);
                    for(AMXProxy oneChild : childrenMap.values()){
                        _children.add( ((AMXConfigProxy) oneChild).objectName());
                    }
                }

            }catch (Exception ex3){
                ex3.printStackTrace();

            }
           

	    // Ok, we got the result, provide an event in case we want to
	    // do some filtering
	    FacesContext ctx = FacesContext.getCurrentInstance();
	    Object retVal = getLayoutComponent().dispatchHandlers(
		    ctx, FilterTreeEvent.EVENT_TYPE,
		    new FilterTreeEvent(getParentUIComponent(), _children));
	    if ((retVal != null) && (retVal instanceof List)) {
		// We have a return value, use it instead of the original list
		_children =  (List) retVal;
	    }
	} else {
	    // Currently multiple levels are not implemented
	    return null;
	}

	return _children; 
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
	    setProperty(props, "rendered", desc.getOption("rendered"));
	} else {
	    // This case deals with the children

	    // NOTE: All supported options must be handled here,
	    // otherwise they'll be ignored

// FIXME: There was a check near here (in DynamicTreeNode.updateKids(...) for
// FIXME: isChildValid... figure out how we want to expose an "exludes" list
// FIXME: or filter to make this more generalized.
	    if (nodeObject instanceof ObjectName) {
		if (_nameAtt != null) {
		    setProperty(props, "text", ((ObjectName) nodeObject).getKeyProperty(_nameAtt));
                    
		} 
                /* TODO  for mbeans
                 else if (_nameMethod != null) {
		    // This is for monitoring MBeans, they don't have a name
		    // attr, but perhaps a getName() method.
		    setProperty(props, "text", (String) JMXUtil.invoke(
			(ObjectName) nodeObject, _nameMethod, null, null));
		}
                 */
                if (!props.containsKey("text")) {
		    // fallback to the object name
		    setProperty(props, "text", nodeObject.toString());
		}
	    } else if (nodeObject instanceof String) {
		setProperty(props, "text", (String) nodeObject);

	    } else if (nodeObject instanceof AMXProxy) {
		setProperty(props, "text", ((AMXProxy) nodeObject).getName());

	    } 
            else {
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
            String tt = (String) desc.getOption("targetConfigName");
            if (!GuiUtil.isEmpty(tt)){
                setProperty(props, "targetConfigName", tt);
            }
            String check = (String) desc.getOption("checkAdminServer");
            if (!GuiUtil.isEmpty(check)){
                String serverName = (String) props.get("text");
                if (serverName.equals("server")){
                    setProperty(props, "text", "server (Admin Server)");
                    setProperty(props, "serverName", "server");
                }else
                    setProperty(props, "serverName", serverName);
            }
            setProperty(props, "encodedText", GuiUtil.encode((String)props.get("text"), null, null));
// We are using "childActionListener" for the hyperlink, not the TreeNode
//	    setProperty(props, "actionListener", desc.getOption("childActionListener"));
	    setProperty(props, "expanded", desc.getOption("childExpanded"));
	}

	// Return the options
	return props;
    }

    /**
     *	<p> Helper method for setting Properties while avoiding NPE's.</p>
     */
    void setProperty(Map props, String key, Object value) {
	if (value != null) {
	    props.put(key, value);
	}
    }

    /**
     *	<p> This method returns the <code>id</code> for the given tree node
     *	    model object.</p>
     */
    public String getId(Object nodeObject) {
	if (nodeObject == null) {
	    return "nullNodeObject";
	}
	if (nodeObject.toString().equals(_objectName)) {
	    // Top level can use the ID of the LayoutComponent
	    return getLayoutComponent().getId(
		FacesContext.getCurrentInstance(), getParentUIComponent());
	}
	return genId(nodeObject.toString());
    }

    /**
     *	<p> This method generates an ID that is safe for JSF for the given
     *	    String.  It does not guarantee that the id is unique, it is the
     *	    responsibility of the caller to pass in a String that will result
     *	    in a UID.  All non-ascii characters will be stripped.</p>
     *
     *	@param	uid	A non-null String.
     */
    private String genId(String uid) {
	char [] chArr = uid.toCharArray();
	int len = chArr.length;
	int newIdx = 0;
	for (int idx=0; idx<len; idx++) {
            char test = chArr[idx];
	    if (Character.isLetterOrDigit(test) || test=='_' || test=='-' ) {
		chArr[newIdx++] = test;
	    }
	}
	return new String(chArr, 0, newIdx);
    }

    /**
     *	<p> This method returns any facets that should be applied to the
     *	    <code>TreeNode (comp)</code>.  Useful facets for the sun
     *	    <code>TreeNode</code> component are: "content" and "image".</p>
     *
     *	<p> Facets that already exist on <code>comp</code>, or facets that
     *	    are directly added to <code>comp</code> do not need to be returned
     *	    from this method.</p>
     *
     *	<p> This implementation directly adds a "content" and "image" facet and
     *	    returns <code>null</code> from this method.</p>
     *
     *	@param	comp	    The tree node <code>UIComponent</code>.
     *	@param	nodeObject  The (model) object representing the tree node.
     */
    public Map<String, UIComponent> getFacets(UIComponent comp, Object nodeObject) {
        if (nodeObject == null){
            return null;
        }
	if (nodeObject.toString().equals(_objectName)) {
	    return null;
	}
	Properties props = new Properties();
	LayoutComponent desc = this.getLayoutComponent();

	// Check to see if a childActionListener was added
	// NOTE: This is not needed when a "command" event is used.  In the
	//	 case of a CommandEvent an ActionListener will be
	//	 automatically registered by the ComponentFactoryBase class
	//	 during "setOptions()".  Also, setting a childActionListener
	//	 here should not stop "command" handlers from being invoked.
	setProperty(props, "actionListener", desc.getOption("childActionListener"));

	// Also se the target and text...
	setProperty(props, "target", desc.getOption("childTarget"));
// FIXME: Add support for other hyperlink properties??

	// Create Hyperlink
	// NOTE: Last attribute "content" will be the facet named used.
	FacesContext ctx = FacesContext.getCurrentInstance();
	ComponentUtil compUtil = ComponentUtil.getInstance(ctx);
	UIComponent imageLink = compUtil.getChild(
	    comp, "imagelink",
	    "com.sun.jsftemplating.component.factory.sun.ImageHyperlinkFactory",
	    props, "image");

	// Force HTML renderer so we can use dynafaces safely.
	imageLink.setRendererType("com.sun.webui.jsf.ImageHyperlink");

	// We don't want the imageHyperlink to have the following property, so
	// set it after creating it
	setProperty(props, "text", comp.getAttributes().get("text"));
	UIComponent link = compUtil.getChild(
	    comp, "link",
	    "com.sun.jsftemplating.component.factory.sun.HyperlinkFactory",
	    props, "content");

	// Force HTML renderer so we can use dynafaces safely.
	link.setRendererType("com.sun.webui.jsf.Hyperlink");

	// Check to see if we have a childURL, evalute it here (after component
	// is created, before rendered) so we can use the link itself to define
	// the URL.  This has proven to be useful...
	Object val = desc.getOption("childURL");
	if (val != null) {
	    val = desc.resolveValue(ctx, link, val);
	    link.getAttributes().put("url", val);
	    imageLink.getAttributes().put("url", val);
	}

	// Set the image URL
	val = desc.getOption("childImageURL");
	if (val != null) {
	    imageLink.getAttributes().put("imageURL", desc.
		resolveValue(ctx, link, val));
	}

	// Set href's handlers...
	// We do it this way rather than earlier b/c the factory will not
	// recognize this as a property, it requires it to be defined in the
	// LayoutComponent as a handler.  So we must do this manually like
	// this.
	List handlers = desc.getHandlers("childCommand");
	if (handlers != null) {
	    link.getAttributes().put("command", handlers);
	    imageLink.getAttributes().put("command", handlers);
	    // This adds the required action listener to proces the commands
	    // This is needed here b/c the factory has already executed -- the
	    // factory is normally the place where this is added (iff there is
	    // at least one command handler).
	    ((ActionSource) link).addActionListener(
		    CommandActionListener.getInstance());
	    ((ActionSource) imageLink).addActionListener(
		    CommandActionListener.getInstance());
	}

	// We already added the facet, return null...
	return null;
    }

    /**
     *	<p> Advanced framework feature which provides better handling for
     *	    things such as expanding TreeNodes, beforeEncode, and other
     *	    events.</p>
     *
     *	<p> This method should return a <code>Map</code> of <code>List</code>
     *	    of <code>Handler</code> objects.  Each <code>List</code> in the
     *	    <code>Map</code> should be registered under a key that cooresponds
     *	    to to the "event" in which the <code>Handler</code>s should be
     *	    invoked.</p>
     */
    public Map getHandlersByType(UIComponent comp, Object nodeObject) {
	/* These handlers apply to the TreeNode not the Hyperlink */
	/*
	LayoutComponent lc = this.getLayoutComponent();
	List list = lc.getHandlers("childCommand");
	if (list != null) {
	    Map m = new HashMap();
	    m.put("command", list);
	    return m;
	}
	*/
	return null;
    }

    /**
     *	<p> This method returns the <code>UIComponent</code> factory class
     *	    implementation that should be used to create a
     *	    <code>TreeNode</code> for the given tree node model object.</p>

	This method provides a means use different TreeNodeFactories,
	currently we are using the default which is defined in the superclass,
	if needed we can change this.

    public String getFactoryClass(Object nodeObject) {
	return "com.sun.enterprise.tools.jsfext.component.factory.basic.TreeNodeFactory";
    }
     */

    /**
     *	The MBean object name.
     */
    private String	_methodName	=   null;

    /**
     *	The MBean object name.
     */
    String	_objectName	=   null;

    /**
     *	The MBean method parameters.
     */
    private Object[]	_paramsArray    =   null;

    /**
     *	The MBean method parameter types.
     */
    private String[]	_paramTypesArray=   null;

    /**
     *	The name of the attribute which describes the TreeNode name.
     */
    private String	_nameAtt	=   null;

    /**
     *	The name of the method which describes the TreeNode name.
     */
    private String	_nameMethod	=   null;

    private String _amxChildType = null;

    /**
     *	This sub-nodes of the top-level Node.
     */
    List	_children	=   null;
}
