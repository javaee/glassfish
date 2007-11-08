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

package com.sun.jbi.jsf.util;

import com.sun.enterprise.tools.admingui.tree.FilterTreeEvent;
import com.sun.enterprise.tools.admingui.util.JMXUtil;

import com.sun.jbi.jsf.bean.ListBean;
import com.sun.jbi.jsf.util.BeanUtilities;
import com.sun.jbi.jsf.util.JBILogger;
import com.sun.jbi.ui.common.JBIComponentInfo;
import com.sun.jbi.ui.common.ServiceAssemblyInfo;

import com.sun.jsftemplating.component.ComponentUtil;
import com.sun.jsftemplating.component.factory.tree.TreeAdaptor;
import com.sun.jsftemplating.component.factory.tree.TreeAdaptorBase;
import com.sun.jsftemplating.util.Util;

import com.sun.jsftemplating.layout.descriptors.LayoutComponent;
import com.sun.jsftemplating.layout.event.CommandActionListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Properties;

import javax.faces.component.ActionSource;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;


/**
 *  <p> The <code>JBITreeAdaptor</code> implementation must have a
 *	<code>public static JBITreeAdaptor getInstance(FacesContext,
 *	LayoutComponent, UIComponent)</code> method in order to get access to
 *	an instance of the <code>JBITreeAdaptor</code> instance.</p>
 *
 *  <p>	This class is used by <code>DynamicTreeNodeFactory</code>.</p>
 *
 *
 */
public class JBITreeAdaptor extends TreeAdaptorBase 
{
   
    private static Logger sLog;

    /**
     *	<p> This constructor is not used.</p>
     */
    private JBITreeAdaptor() 
    {
    }

    /**
     *	<p> This constructor saves the <code>LayoutComponent</code> descriptor
     *	    and the <code>UIComponent</code> associated with this
     *	    <code>TreeAdaptor</code>.  This constructor is used by the
     *	    getInstance() method.</p>
     */
    protected JBITreeAdaptor(LayoutComponent desc, UIComponent parent) 
    {
	super(desc, parent);
    }

    /**
     *	<p> This method provides access to an <code>JBITreeAdaptor</code>
     *	    instance.  Each time it is invoked, it returns a new instance.</p>
     */
    public static TreeAdaptor getInstance(FacesContext ctx, LayoutComponent desc, UIComponent parent) 
    {
	return new JBITreeAdaptor(desc, parent);
    }

    /**
     *	<p> This method is called shortly after
     *	    {@link #getInstance(FacesContext, LayoutComponent, UIComponent)}.
     *	    It provides a place for post-creation initialization to take
     *	    occur.</p>
     */
    public void init() 
    {
        //initialise the logger
        sLog = JBILogger.getInstance();
        
	// Get the FacesContext
	FacesContext ctx = FacesContext.getCurrentInstance();

	// This is the descriptor for this dynamic TreeNode, it contains all
	// information (options) necessary for this Adaptor
	LayoutComponent desc = getLayoutComponent();

	// The parent UIComponent
	UIComponent parent = getParentUIComponent();

	// Get the Method Name
	Object val = desc.getEvaluatedOption(ctx, "treeAdaptorListType", parent);
	if (val == null) {
	    throw new IllegalArgumentException(
		    "'treeAdaptorListType' must be specified!");
	}
	mAdaptorListType = (String) val;
	setTreeNodeObject(mAdaptorListType);
    }

    /**						   2/13/2007
     *	<p> Returns child <code>TreeNode</code>s for the given
     *	    <code>TreeNode</code> model Object.</p>
     */
    public List getChildTreeNodeObjects(Object nodeObject) 
    {
	
	sLog.fine("JBITreeAdaptor getChildTreeNodeObjects(" + nodeObject + "), mAdaptorListType=" + mAdaptorListType);
	
	List result = null;

	if (null != nodeObject)
	    {
		if (nodeObject.equals(mAdaptorListType))
		    {
			ListBean  listBean = BeanUtilities.getListBean();
			String xmlQueryResults;
			
			if ("deployments".equals(mAdaptorListType))
			    {
				xmlQueryResults = listBean.getListServiceAssemblies();
				sLog.fine("JBITreeAdaptor getChildTreeNodeObjects(...), xmlQueryResults=" + xmlQueryResults);
				List saInfoList =
				    ServiceAssemblyInfo.readFromXmlTextWithProlog(xmlQueryResults);
				mChildren = saInfoList.toArray();
			    }
			else if ("bindingsEngines".equals(mAdaptorListType))
			    {
				xmlQueryResults = listBean.getListBindingComponents();
				sLog.fine("JBITreeAdaptor getChildTreeNodeObjects(...), xmlQueryResults=" + xmlQueryResults);
				List bcCompInfoList =
				    JBIComponentInfo.readFromXmlText(xmlQueryResults);

				xmlQueryResults = listBean.getListServiceEngines();
				sLog.fine("JBITreeAdaptor getChildTreeNodeObjects(...), xmlQueryResults=" + xmlQueryResults);
				List seCompInfoList =
				    JBIComponentInfo.readFromXmlText(xmlQueryResults);

				List operableComponentsList = new ArrayList();
				operableComponentsList.addAll(bcCompInfoList);
				operableComponentsList.addAll(seCompInfoList);
				mChildren = operableComponentsList.toArray();
			    }
			else if ("libraries".equals(mAdaptorListType))
			    {
				xmlQueryResults = listBean.getListSharedLibraries();
				sLog.fine("JBITreeAdaptor getChildTreeNodeObjects(...), xmlQueryResults=" + xmlQueryResults);
				List slCompInfoList =
				    JBIComponentInfo.readFromXmlText(xmlQueryResults);


				mChildren = slCompInfoList.toArray();
			    }
			
			if (mChildren != null)
			    {
				sLog.fine("JBITreeAdaptor getChildTreeNodeObjects(...), mChildren.length=" + mChildren.length);
				result = Arrays.asList((Object[])mChildren);
			    }
		    }
	    }

		sLog.fine("JBITreeAdaptor getChildTreeNodeObjects, result=" + result);
	    
	return result;
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
    public Map<String, Object> getFactoryOptions(Object nodeObject) 
    {
	Map<String, Object> result = null;

	if (null != nodeObject)
	    {

		LayoutComponent desc = getLayoutComponent();
		result = new HashMap<String, Object>();

			sLog.fine("JBITreeAdaptor getFactoryOptions (" + nodeObject + "), nodeObject.getClass()=" + nodeObject.getClass());
	
		if (nodeObject instanceof String)
		    {
			// This case deals with the top node.
			
			// NOTE: All supported options must be handled here,
			//		otherwise they'll be ignored.
			// NOTE: Options will be evaluated later, do not eval here.
			setProperty(result, "text", desc.getOption("text"));
			setProperty(result, "url", desc.getOption("url"));
			setProperty(result, "imageURL", desc.getOption("imageURL"));
			setProperty(result, "target", desc.getOption("target"));
			setProperty(result, "action", desc.getOption("action"));
			
			
			// NOTE: Although actionListener is supported, LH currently
			//	     implements this to be the ActionListener of the "turner"
			//	     which is inconsistent with "action".  We should make use
			//	     of the "Handler" feature which provides a "toggle"
			//	     CommandEvent.
			setProperty(result, "actionListener", desc.getOption("actionListener"));
			setProperty(result, "expanded", desc.getOption("expanded"));
			sLog.fine("JBITreeAdaptor getFactoryOptions (parent), text=" + desc.getOption("text"));
			
		    } 
		else 
		    {
			// This case deals with the children
			
			// NOTE: All supported options must be handled here,
			// otherwise they'll be ignored
			
			
			// Finish setting the child properties
			String childName = null;
			String imageUrlSuffix = null;
			String type = null;
			String urlSuffix = "type=";

			if (nodeObject instanceof JBIComponentInfo)
			    {
				JBIComponentInfo compInfo = (JBIComponentInfo) nodeObject;
				childName = compInfo.getName();
				type = compInfo.getType();
				sLog.fine("JBITreeAdaptor getFactoryOptions (child), childName=" + childName + ", type=" + type);			
			    }
			else if (nodeObject instanceof ServiceAssemblyInfo)
			    {
				ServiceAssemblyInfo saInfo = (ServiceAssemblyInfo) nodeObject;
				childName = saInfo.getName();
				type = "service-assembly";
			    }
			if (null != childName)
			    {
				setProperty(result, "text", childName);
				urlSuffix += type + "&name=" + childName;
				if ("service-assembly".equals(type))
				{
				    imageUrlSuffix = "JBIServiceAssembly.gif";
				}
				else if ("binding-component".equals(type))
				{
				    imageUrlSuffix = "JBIBindingComponent.gif";
				}
				else if ("service-engine".equals(type))
				{
				    imageUrlSuffix = "JBIServiceEngine.gif";
				}
				else if ("shared-library".equals(type))
				{
				    imageUrlSuffix = "JBISharedLibrary.gif";
				}

			    }
			String url = desc.getOption("childURLbase") + urlSuffix;
			setProperty(result, "url", url);
			setProperty(result, "imageURL", desc.getOption("childImageURLbase") + imageUrlSuffix);
			setProperty(result, "target", desc.getOption("childTarget"));
			setProperty(result, "action", desc.getOption("childAction"));
			setProperty(result, "expanded", desc.getOption("childExpanded"));
			sLog.fine("JBITreeAdaptor getFactoryOptions (child), type=" + type + ", url=" + url);			
		    }

	    }

		sLog.fine("JBITreeAdaptor getFactoryOptions, result=" + result);
	    return result;
    }

    /**
     *	<p> Helper method for setting Properties while avoiding NPE's.</p>
     */
    void setProperty(Map props, String key, Object value) 
    {
	if (value != null) {
	    props.put(key, value);
	}
    }

    /**
     *	<p> This method returns the <code>id</code> for the given tree node
     *	    model object.</p>
     */
    public String getId(Object nodeObject) 
    {
	String result = "nullNodeObject";

	if (null != nodeObject)
	    {
		result = genId(nodeObject.toString());
	    }
	
	return result;
    }

    /**
     *	<p> This method generates an ID that is safe for JSF for the given
     *	    String.  It does not guarantee that the id is unique, it is the
     *	    responsibility of the caller to pass in a String that will result
     *	    in a UID.  All non-ascii characters will be stripped.</p>
     *
     *	@param	uid	A non-null String.
     */
    private String genId(String uid) 
    {
	char [] chArr = uid.toCharArray();
	int len = chArr.length;
	int newIdx = 0;
	for (int idx=0; idx<len; idx++) 
	    {
		char test = chArr[idx];
		if (Character.isLetterOrDigit(test) || test=='_' || test=='-' ) 
		    {
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
     *	<p> This implementation directly adds a "content" facet and returns
     *	    <code>null</code> from this method.</p>
     *
     *	@param	comp	    The tree node <code>UIComponent</code>.
     *	@param	nodeObject  The (model) object representing the tree node.
     */
    public Map<String, UIComponent> getFacets(UIComponent comp, Object nodeObject) 
    {
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
    public Map getHandlersByType(UIComponent comp, Object nodeObject) 
    {
	return null;
    }

    private String	mAdaptorListType;
    Object[]	        mChildren;
    private String	mNameAtt;
}
