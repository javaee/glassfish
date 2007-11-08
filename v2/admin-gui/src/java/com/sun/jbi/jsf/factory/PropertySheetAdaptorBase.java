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

import com.sun.jsftemplating.layout.descriptors.LayoutComponent;
import com.sun.jsftemplating.layout.descriptors.handler.Handler;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import com.sun.jsftemplating.component.factory.sun.PropertySheetSectionFactory;
import com.sun.jsftemplating.component.factory.sun.PropertyFactory;
import com.sun.jsftemplating.component.factory.sun.StaticTextFactory;
import com.sun.jsftemplating.component.factory.sun.DropDownFactory;
import com.sun.jsftemplating.component.factory.sun.HiddenFieldFactory;

import com.sun.webui.jsf.component.PropertySheetSection;
import com.sun.webui.jsf.component.Property;
import com.sun.webui.jsf.component.StaticText;
import com.sun.webui.jsf.component.DropDown;
import com.sun.webui.jsf.component.HiddenField;


/**
 *  <p>	This class provides some of the implemenation for the methods required
 *	by the PropoertySheetAdaptor interface.  This class may be extended to assist in
 *	implementing a PropertySheetAdaptor implementation.</p>
 *
 *  <p> The <code>PropertySheetAdaptor</code> implementation must have a <code>public
 *	static PropertySheetAdaptor getInstance(FacesContext, LayoutComponent,
 *	UIComponent)</code> method in order to get access to an instance of the
 *	<code>PropertySheetAdaptor</code> instance.</p>
 *
 *  @see    PropertySheetAdaptor
 *
 */
public abstract class PropertySheetAdaptorBase implements PropertySheetAdaptor
{

    /**
     *	<p> This Constructor does nothing.  If you need to store a reference
     *	    to the <code>LayoutComponent</code> or <code>UIComponent</code>
     *	    associated with this <code>PropertySheetAdaptor</code>, it may be more
     *	    convenient to use a different constructor.</p>
     */
    protected PropertySheetAdaptorBase() {
    }


    /**
     *	<p> This Constructor save the <code>LayoutComponent</code> and the
     *	    <code>UIComponent</code> for easy use later.</p>
     */
    protected PropertySheetAdaptorBase(LayoutComponent desc, UIComponent parent) {
        setLayoutComponent(desc);
        setParentUIComponent(parent);
    }


    /**
     *	<p> This method retrieves the <code>LayoutComponent</code> associated
     *	    with this <code>PropertySheetAdaptor</code>.</p>
     */
    public LayoutComponent getLayoutComponent() {
        return mLayoutComponent;
    }


    /**
     *	<p> This method sets the <code>LayoutComponent</code> associated
     *	    with this <code>PropertySheetAdaptor</code>.</p>
     */
    public void setLayoutComponent(LayoutComponent comp) {
        mLayoutComponent = comp;
    }

    /**
     *	<p> This method retrieves the <code>UIComponent</code> associated
     *	    with this <code>PropertySheetAdaptor</code>.</p>
     */
    public UIComponent getParentUIComponent() {
        return mParent;
    }


    /**
     *	<p> This method sets the <code>UIComponent</code> associated with this
     *	    <code>PropertySheetAdaptor</code>.</p>
     */
    public void setParentUIComponent(UIComponent comp) {
        mParent = comp;
    }


    /**
     *	<p> This method is called shortly after
     *	    getInstance(FacesContext, LayoutComponent, UIComponent).  It
     *	    provides a place for post-creation initialization to take occur.</p>
     *
     *	<p> This implemenation does nothing.</p>
     */
    public void init() {
    }


    /**
     *	<p> This method returns the <code>id</code> for the PropertySheet object.</p>
     */
    public String getId()
    {
        return mPropertySheetId;
    }


    /**
     *	<p> Sets the Component Id using the given id tag string.</p>
     *
     *	@param	parent  The component.
     *	@param	tagType  The tag type.
    *	@param	idTagKey  The tag string.
     */
    public void setComponentId (UIComponent component, String tagType, String idTagKey)
    {
        String idNum = getNextIdNum(tagType);
        setIdNum(tagType, idNum);
        String idValue = idTagKey + idNum;
        component.setId(idValue);
    }


    /**
     *	<p> Sets the Component Id using the given id tag string.</p>
     *
     *	@param	parent  The component.
     *	@param	idTagKey  The tag string.
     */
    public void setComponentId (UIComponent component, String idTagKey)
    {
        setComponentId (component,idTagKey,idTagKey);
    }


    /**
     *	<p> This method creates and returns the a PropertySheetSection Component.</p>
     *
     *	@param	parent  The parent component.
     */
    public UIComponent getPropertySheetSectionComponent (UIComponent parent)
    {
        LayoutComponent desc = getLayoutComponent();
        FacesContext ctx = FacesContext.getCurrentInstance();
        PropertySheetSectionFactory factory = new PropertySheetSectionFactory();
        UIComponent component = factory.create(ctx,desc,parent);
        setComponentId(component,mPropertySheetSectionIdTag); 
        return component;
    }


    /**
     *	<p> This method creates and returns the a Property Component Component.</p>
     *
     *	@param	parent  The parent component.
     */
    public UIComponent getPropertyComponent (UIComponent parent)
    {
        LayoutComponent desc = getLayoutComponent();
        FacesContext ctx = FacesContext.getCurrentInstance();
        PropertyFactory factory = new PropertyFactory();
        UIComponent component = factory.create(ctx,desc,parent);
        setComponentId(component,mPropertyIdTag); 
        return component;
    }


    /**
     *	<p> This method creates and returns the a StaticText Component.</p>
     *
     *	@param	parent  The parent component.
     */
    public UIComponent getStaticTextComponent (UIComponent parent)
    {
        LayoutComponent desc = getLayoutComponent();
        FacesContext ctx = FacesContext.getCurrentInstance();
        StaticTextFactory factory = new StaticTextFactory();
        UIComponent component = factory.create(ctx,desc,parent);
        setComponentId(component,mStaticTextIdTag); 
        return component;
    }


    /**
     *	<p> This method creates and returns the a Dropdown Component.</p>
     *
     *	@param	parent  The parent component.
     */
    public UIComponent getDropDownComponent (UIComponent parent)
    {
        LayoutComponent desc = getLayoutComponent();
        FacesContext ctx = FacesContext.getCurrentInstance();
        DropDownFactory factory = new DropDownFactory();
        UIComponent component = factory.create(ctx,desc,parent);
        setComponentId(component,mDropDownIdTag); 
        return component;
    }


    /**
     *	<p> This method creates and returns the a HiddenField Component.</p>
     *
     *	@param	parent  The parent component.
    *	@param	id  The component id.
     */
    public UIComponent getHiddenFieldComponent (UIComponent parent, String id)
    {
        LayoutComponent desc = getLayoutComponent();
        FacesContext ctx = FacesContext.getCurrentInstance();
        HiddenFieldFactory factory = new HiddenFieldFactory();
        UIComponent component = factory.create(ctx,desc,parent);
        setComponentId(component,mHiddenFieldIdTag,id); 
        return component;
    }


    /**
     *	<p> This method creates and returns the a HiddenField Component.</p>
     *
     *	@param	parent  The parent component.
     */
    public UIComponent getHiddenFieldComponent (UIComponent parent)
    {
        return getHiddenFieldComponent(parent,mHiddenFieldIdTag);
    }


    /**
     *	<p> This method returns the <code>UIComponent</code> factory class
     *	    implementation that should be used to create a
     *	    <code>PropertySheet</code> for the given property sheet model object.</p>
     */
    public String getFactoryClass() {
        return "com.sun.jsftemplating.component.factory.sun.PropertySheetFactory";
    }


    /**
     *	<p> This method returns any facets that should be applied to the
     *	    <code>PropertySheet (comp)</code>. 
     *
     *	<p> Facets that already exist on <code>comp</code>, or facets that
     *	    are directly added to <code>comp</code> do not need to be returned
     *	    from this method.</p>
     *
     *	@param	nodeObject  The (model) object representing the property sheet.
     */
    public Map<String, UIComponent> getFacets(Object nodeObject) {
        return null;
    }


    /**
     *	<p> Advanced framework feature which provides better handling for
     *	    things such as expanding PropertySheet, beforeEncode, and other
     *	    events.</p>
     *
     *	<p> This method should return a <code>Map</code> of <code>List</code>
     *	    of <code>Handler</code> objects.  Each <code>List</code> in the
     *	    <code>Map</code> should be registered under a key that cooresponds
     *	    to to the "event" in which the <code>Handler</code>s should be
     *	    invoked.</p>
     *
     *	<p> This implementation returns null.  This method must be overriden
     *	    to take advantage of this feature.</p>
     */
    public Map<String, List<Handler>> getHandlersByType(UIComponent comp, Object nodeObject) {
        return null;
    }


    /**
     *	<p> This method returnes the Optione Value.  If optionName specifies
     *      the session or request scope, then the method will use the session
     *      or request map file to retrieve the value.
     *
     *	@param	optionName  The name of the option.
     *  @param  parent      The parent component.
     */
    protected Object getOptionValue (String optionName, UIComponent parent)
    {
        LayoutComponent desc = getLayoutComponent();
        FacesContext ctx = FacesContext.getCurrentInstance();
        Object value = desc.getEvaluatedOption(ctx, optionName, parent);
        if (value != null)
        {
            String stringValue = value.toString();
            int dotIndex = stringValue.indexOf(".");
            if (dotIndex > 0)
            {
                String scopeString = stringValue.substring(0,dotIndex);
                if (scopeString.equalsIgnoreCase("#{sessionScope"))
                {
                    String sessionKey = stringValue.substring(dotIndex+1);
                    int endBraceIndex = stringValue.indexOf("}");
                    if (endBraceIndex > 0)
                    {
                        sessionKey = stringValue.substring(dotIndex+1,endBraceIndex);
                        Map sessionMap = getSessionScope();
                        value = (Object)sessionMap.get(sessionKey);
                    }
                } else if (scopeString.equalsIgnoreCase("#{requestScope"))
                {
                    String key = stringValue.substring(dotIndex+1);
                    int endBraceIndex = stringValue.indexOf("}");
                    if (endBraceIndex > 0)
                    {
                        key = stringValue.substring(dotIndex+1,endBraceIndex);
                        Map sessionMap = getRequestScope();
                        value = (Object)sessionMap.get(key);
                    }
                }

            }
        }
        if ((value != null) && (((String)value).length() == 0))
        {
            value = null;
        }
        return value;
    }


    /**
     *	<p> This method returnes the Optione Value.  If the requested option does
     *      not exists then an InvalidArgumementExceptin is thrown.
     *
     *	@param	optionName  The name of the option.
     *  @param  parent      The parent component.
     */
    protected String getRequiredOptionValue (String optionName, UIComponent parent)
    throws IllegalArgumentException
    {
        String returnValue = null;
        Object value = getOptionValue(optionName,parent);
        if (value == null)
        {
            String message = optionName + " must be specified!";
            throw new IllegalArgumentException(message);
        }
        returnValue = (String)value;
        return returnValue;
    }


    /**
     *	<p> Returns the session scope Map.</p>
     */
    protected Map getSessionScope() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        return(Map) ctx.getApplication().createValueBinding("#{sessionScope}").getValue(ctx);
    }


    /**
     *	<p> Returns the request scope Map.</p>
     */
    protected Map getRequestScope() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        return(Map) ctx.getApplication().createValueBinding("#{requestScope}").getValue(ctx);
    }


    private String getIdNum(String idTagKey)
    {
        String value = (String)mPropertySheetIds.get(idTagKey);
        if (value == null)
        {
            value = "0";
        }
        return value;
    }


    private void setIdNum(String idTagKey, String value)
    {
        mPropertySheetIds.put(idTagKey, value);
    }


    private String getNextIdNum(String idTagKey)
    {
        String nextIdNum = "";
        String idTag = getIdNum(idTagKey);
        int idTagValue = Integer.parseInt(idTag);
        idTagValue++;
        nextIdNum = idTagValue + "";
        return nextIdNum;
    }
    
    protected String mPropertySheetId             = null;       
    protected String mPropertySheetSectionIdTag   = null; 
    protected String mPropertyIdTag               = null;
    protected String mStaticTextIdTag             = null;
    protected String mDropDownIdTag               = null;
    protected String mHiddenFieldIdTag            = null;

    private HashMap         mPropertySheetIds  = new HashMap();
    private UIComponent     mParent            = null;
    private LayoutComponent mLayoutComponent   = null;
}
