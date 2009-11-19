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

/*
 * CommonHandlers.java
 *
 * Created on August 30, 2006, 4:21 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.glassfish.admingui.common.handlers;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.el.PageSessionResolver;
import com.sun.jsftemplating.handlers.NavigationHandlers;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import com.sun.jsftemplating.util.Util;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.admingui.common.util.MiscUtil;
import org.glassfish.admingui.common.util.V3AMX;
import org.glassfish.admingui.common.util.V3AMXUtil;


public class CommonHandlers {
    
    /** Creates a new instance of CommonHandlers */
    public CommonHandlers() {
    }

    /**
     * <p> This handler will be called during initialization when Cluster Support is detected.
     */
    @Handler(id="initClusterSessionAttribute")
    public static void initClusterSessionAttribute(HandlerContext handlerCtx){
        Map sessionMap = handlerCtx.getFacesContext().getExternalContext().getSessionMap();
        //The summary or detail view of deploy tables is stored in session to remember user's previous
        //preference.
        sessionMap.put("appSummaryView", true);
        sessionMap.put("webSummaryView", true);
        sessionMap.put("ejbSummaryView", true);
        sessionMap.put("appclientSummaryView", true);
        sessionMap.put("rarSummaryView", true);
        sessionMap.put("lifecycleSummaryView", true);
    
        sessionMap.put("adminObjectSummaryView", true);
        sessionMap.put("connectorResSummaryView", true);
        sessionMap.put("customResSummaryView", true);
        sessionMap.put("externalResSummaryView", true);
        sessionMap.put("javaMailSessionSummaryView", true);
        sessionMap.put("jdbcResSummaryView", true);
        sessionMap.put("jmsConnectionSummaryView", true);
        sessionMap.put("jmsDestinationSummaryView", true);
    }

    /**
     * <p> This handler will be called during initialization for doing any initialization.
     */
    @Handler(id="initSessionAttributes")
    public static void initSessionAttributes(HandlerContext handlerCtx){
        
        //Ensure this method is called once per session
        Object initialized = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("_SESSION_INITIALIZED");
        if (initialized == null){
            GuiUtil.initSessionAttributes();
        }
        return;
    }


    /** This function is called in login.jsf to set the various product specific attributes such as the 
     *  product GIFs and product names. A similar function is called for Sailfin to set Sailfin specific
     *  product GIFs and name.
     *  The function is defined in com.sun.extensions.comms.SipUtilities
     */
    @Handler(id="initProductInfoAttributes")
    public static void initProductInfoAttributes(HandlerContext handlerCtx) {
        Map sessionMap = handlerCtx.getFacesContext().getExternalContext().getSessionMap();
        
        //Ensure this method is called once per session
        Object initialized = sessionMap.get("_INFO_SESSION_INITIALIZED");
        if (initialized != null) 
            return;
        // Initialize Product Specific Attributes
        sessionMap.put("productImageURL", GuiUtil.getMessage("productImage.URL"));
        sessionMap.put("productImageWidth", Integer.parseInt(GuiUtil.getMessage("productImage.width")));
        sessionMap.put("productImageHeight", Integer.parseInt(GuiUtil.getMessage("productImage.height")));
        sessionMap.put("loginProductImageURL", GuiUtil.getMessage("login.productImage.URL"));
        sessionMap.put("loginProductImageWidth", Integer.parseInt(GuiUtil.getMessage("login.productImage.width")));
        sessionMap.put("loginProductImageHeight", Integer.parseInt(GuiUtil.getMessage("login.productImage.height")));        
        sessionMap.put("fullProductName", GuiUtil.getMessage("versionImage.description"));
        sessionMap.put("loginButtonTooltip", GuiUtil.getMessage("loginButtonTooltip"));
        sessionMap.put("mastHeadDescription", GuiUtil.getMessage("mastHeadDescription"));
        
        // showLoadBalancer is a Sailfin specific attribute. Sailfin uses Converged LB instead
        // of HTTP LB. It is true for GF and false for Sailfin. In sailfin this is set in
        // com.sun.extensions.comms.SipUtilities.initProductInfoAttributes() called for Sailfin in login.jsf
        
        //TODO-V3 may need to set this back to true
        //sessionMap.put("showLoadBalancer", true); 
        
        sessionMap.put("_INFO_SESSION_INITIALIZED","TRUE");
    }
    
     /**
     *	<p> This handler returns String[] of the given java.util.List </p>
     *
     *  <p> Output value: "selectedIndex" -- Type: <code>Object</code>/</p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getListElement",
    	input={
        @HandlerInput(name="list", type=java.util.List.class, required=true ),
        @HandlerInput(name="index", type=Integer.class)},
        output={
        @HandlerOutput(name="selectedIndex", type=Object.class)})
    public static void getListElement(HandlerContext handlerCtx) {
		List<String> list = (List)handlerCtx.getInputValue("list");	
		Integer selectedIndex = (Integer)handlerCtx.getInputValue("index");	
		String[] listItem = null;
		if(list != null) {
			if(selectedIndex == null) {
				//default to 0
				selectedIndex = new Integer(INDEX);
			}
			listItem = new String[]{list.get(selectedIndex)};
		}
        handlerCtx.setOutputValue("selectedIndex", listItem);
    }
    
    

    /**
     * <p> This handler returns the encoded String using the type specified.
     * <p> If type is not specified, it defaults to UTF-8.
     * <p> Input value: "value" -- Type: <code>String</code> <p>
     * <p> Input value: "delim" -- Type: <code>String</code> <p>
     * <p> Input Value: "type" -- Type: <code>String</code> <p>
     * <p> Output Value: "value" -- Type: <code>String</code> <p>
     *@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="selectiveEncode",
    input={
        @HandlerInput(name="value", type=String.class, required=true ),
        @HandlerInput(name="delim", type=String.class),
        @HandlerInput(name="type", type=String.class)},
    output={
        @HandlerOutput(name="result", type=String.class)}
    )
    public static void selectiveEncode(HandlerContext handlerCtx) {
        
        String value = (String) handlerCtx.getInputValue("value");
        String delim = (String) handlerCtx.getInputValue("delim");
        String encType = (String) handlerCtx.getInputValue("type");
		String encodedString = GuiUtil.encode(value, delim, encType);
        handlerCtx.setOutputValue("result", encodedString);
   } 
    
    /**
     *	<p> This method kills the session, and logs out </p>
     *      Server Domain Attributes Page.</p>
     *	<p> Input value: "page" -- Type: <code>java.lang.String</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="logout")
    public static void logout(HandlerContext handlerCtx) {
	handlerCtx.getFacesContext().getExternalContext().invalidateSession();
    }

    /**
     *	<p> This method looks at the port of the admin-listener to generate the href to show to user so that </p>
     *      when server restart, user can click on that link to access GUI again.  We need to do that since the </p>
     *      port maybe changed from the current one when server restart.
     *	<p> Output value: "url" -- Type: <code>java.lang.String</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="getRestartURL",
    output={
        @HandlerOutput(name="url", type=String.class)}
    )
    public static void getRestartURL(HandlerContext handlerCtx) {
        String port = ""+ V3AMXUtil.getAdminPort();
        String security = (String) V3AMX.getInstance().getAdminListener().findProtocol().attributesMap().get("SecurityEnabled");
        String url = security.equals("true")? "https" : "http";
        url = url + "://" + GuiUtil.getSessionValue("serverName")+":" + port;
        handlerCtx.setOutputValue("url", url);
    }

    /**
     *	<p> This method sets the required attribute of a UI component .
     *	<p> Input value: "id" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "required" -- Type: <code>java.lang.String</code></p>
     *	@param	handlerCtx	The HandlerContext.
     */
    @Handler(id="setComponentRequired",
    input={
        @HandlerInput(name="id",     type=String.class, required=true),
        @HandlerInput(name="required",     type=String.class, required=true)
    })
    public static void setComponentRequired(HandlerContext handlerCtx) {
        String id = (String) handlerCtx.getInputValue("id");
        String required = (String) handlerCtx.getInputValue("required");
        UIComponent viewRoot = handlerCtx.getFacesContext().getViewRoot();
        if (viewRoot == null) return;
        try {
            UIInput targetComponent = (UIInput) viewRoot.findComponent(id);
            if (targetComponent != null ){
                targetComponent.setRequired(Boolean.valueOf(required));
            }
            
        }catch(Exception ex){
            //Cannot find component, do nothing.
        }
    }
    
    
    /**
     *  <p> Test if a particular attribute exists.
     *      It will look at request scope, then page, then session.
     */
    @Handler(id="testExists",
    input={
        @HandlerInput(name="attr", type=String.class, required=true )},
    output={
        @HandlerOutput(name="defined", type=Boolean.class)}
    )
    public static void testExists(HandlerContext handlerCtx) {
        String attr = (String) handlerCtx.getInputValue("attr");
        if(GuiUtil.isEmpty(attr)){
            handlerCtx.setOutputValue("defined", false);
        }else{
            handlerCtx.setOutputValue("defined", true);
        }
    }

    /**
     *	<p> This handler returns the requestParameter value based on the key.
     *	    If it doesn't exists, then it will look at the request
     *	    attribute.  If there is no request attribute, it will return the
     *	    default, if specified.</p>
     *
     *	<p> This method will "html escape" any &lt;, &gt;, or &amp; characters
     *	    that appear in a String from the QUERY_STRING.  This is to help
     *	    prevent XSS vulnerabilities.</p>
     *
     * 	<p> Input value: "key" -- Type: <code>String</code></p>
     *
     *	<p> Output value: "value" -- Type: <code>String</code></p>
     *
     */
    @Handler(id="getRequestValue",
    input={
        @HandlerInput(name="key", type=String.class, required=true),
        @HandlerInput(name="default", type=String.class)},
    output={
        @HandlerOutput(name="value", type=Object.class)}
    )
    public static void getRequestValue(HandlerContext handlerCtx) {
        String key = (String) handlerCtx.getInputValue("key");
        Object defaultValue = handlerCtx.getInputValue("default");
        Object value = handlerCtx.getFacesContext().getExternalContext().getRequestParameterMap().get(key);
        if ((value == null) || "".equals(value)) {
            value = handlerCtx.getFacesContext().getExternalContext().getRequestMap().get(key);
            if ((value == null) && (defaultValue != null)){
                value = defaultValue;
            }
        } else {
	    // For URLs, the following could be used, but it URLEncodes  the
	    // values, which are not ideal for displaying in HTML... so I will
	    // instead call htmlEscape()
	    //value = GuiUtil.encode(value, "#=@%+;-&_.?:/()", "UTF-8");

	    // Only need to do this for QUERY_STRING values...
	    value = Util.htmlEscape((String) value);
        }
        handlerCtx.setOutputValue("value", value);
    }
   
    /**
     *	This method adds two long integers together.  The 2 longs should be
     *	stored in "long1" and "long2".  The result will be stored as "result".
     */
    @Handler(id="longAdd",
    input={
        @HandlerInput(name="Long1", type=Long.class, required=true ),
        @HandlerInput(name="Long2", type=Long.class, required=true )},
    output={
        @HandlerOutput(name="LongResult", type=Long.class)}
    )    
    public void longAdd(HandlerContext handlerCtx) {
        Long result = new Long(0);
        try{
            // Get the inputs
            Long long1 = (Long)handlerCtx.getInputValue("Long1");
            Long long2 = (Long)handlerCtx.getInputValue("Long2");
            // Add the 2 numbers together
            result = new Long(long1.longValue()+long2.longValue());
        }catch(Exception ex){
            GuiUtil.getLogger().warning("Exception in longAdd, return 0 ");
        }
	// Set the result
	handlerCtx.setOutputValue("LongResult", result);
    }
    
    /**
     * <p> Returns the current system time formatted<p>
     * <p> Output value: "Time" -- Type: <code>String</code></p>
     *
     */
    @Handler(id="getCurrentTime",
    output={
        @HandlerOutput(name="CurrentTime", type=String.class)}
    )
    public void getCurrentTime(HandlerContext handlerCtx) {
        Date d = new Date(System.currentTimeMillis());
        DateFormat dateFormat = DateFormat.getDateTimeInstance(
                DateFormat.MEDIUM, DateFormat.MEDIUM, handlerCtx.getFacesContext().getViewRoot().getLocale());
        String currentTime = dateFormat.format(d);
        handlerCtx.setOutputValue("CurrentTime", currentTime);
    }
    
    /**
     * <p> Returns the restart required status<p>
     * <p> Output value: "RestartRequired" -- Type: <code>java.lang.Boolean</code></p>
     *
     */
    @Handler(id="checkRestart",
    output={
        @HandlerOutput(name="RestartRequired", type=Boolean.class),
        @HandlerOutput(name="unprocessedChanges", type=List.class)}
    )
    public void checkRestart(HandlerContext handlerCtx) {
        List<Object[]> changes = V3AMX.getInstance().getDomainRoot().getExt().getSystemStatus().getRestartRequiredChanges();
        handlerCtx.setOutputValue("RestartRequired", (changes.size() > 0));
        handlerCtx.setOutputValue("unprocessedChanges", changes); 
    }


    /**
     * <p> Test if anonymous user login is allowed <p>
     * <p> Output value: "loginUser" -- Type: <code>java.lang.String</code> the anonymous user name </p>
     * <p> Output value: "byPass" -- Type: <code>java.lang.Boolean</code>if GUI should bypass login form </p>
     *
     */
    @Handler(id="testLoginBypass",
    output={
        @HandlerOutput(name="byPass", type=Boolean.class),
        @HandlerOutput(name="loginUser", type=String.class)}
    )
    public void testLoginBypass(HandlerContext handlerCtx) {
        String user=null;
        try{
            user = V3AMX.getInstance().getRealmsMgr().getAnonymousUser();
        }catch(Exception ex){
            ex.printStackTrace();
            GuiUtil.getLogger().severe("Cannot determine anonymous login.  Login enforced.");
            user=null;
        }
        handlerCtx.setOutputValue("byPass", (user==null)? Boolean.FALSE: Boolean.TRUE);
        handlerCtx.setOutputValue("loginUser", (user==null) ? "" : user);
    }

    /**
     * <p> This handler sets a property on an object which is stored in an existing key
     * For example "advance.lazyConnectionEnlistment".  <strong>Note</strong>:  This does
     * <em>not</em> evaluate the EL expression.  Its value (e.g., "#{advance.lazyConnectionEnlistment}")
     * is passed as is to the EL API.
     */
    @Handler(id = "setValueExpression",
        input = {
            @HandlerInput(name = "expression", type = String.class, required = true),
            @HandlerInput(name = "value", type = Object.class, required = true)
    })
    public static void setValueExpression(HandlerContext handlerCtx) {
        MiscUtil.setValueExpression((String) handlerCtx.getHandler().getInputValue("expression"), 
                (Object) handlerCtx.getInputValue("value"));
    }
    
    /**
     *	<p> This handler checks if particular feature is supported  </p>
     *
     *  <p> Output value: "supportCluster" -- Type: <code>Boolean</code>/</p>
     *  <p> Output value: "supportHADB" -- Type: <code>Boolean</code>/</p>
     *	@param	handlerCtx	The HandlerContext.
     */
//    @Handler(id="checkSupport",
//    output={
//        @HandlerOutput(name="supportCluster", type=Boolean.class),
//        @HandlerOutput(name="supportHADB", type=Boolean.class)})
//        public static void checkSupport(HandlerContext handlerCtx) {
//            handlerCtx.setOutputValue("supportCluster", false);
//            handlerCtx.setOutputValue("supportHADB", false);
//    }

    /**
     *	<p> This handler allows the "partialRequest" flag to be set.  This
     *	    was added to work-a-round a bug in JSF where the behavior was
     *	    inconsistent between FF and other browsers.  Namely it recognized
     *	    redirects as "partial" requets in other browsers due to the
     *	    header being preserved across the redirect, but not in FF.</p>
     */
    @Handler(id="setPartialRequest",
	input={
	    @HandlerInput(name="value", type=Boolean.class, required=true)})
    public static void setPartialRequest(HandlerContext context) {
	boolean isPartial = (Boolean) context.getInputValue("value");
	context.getFacesContext().getPartialViewContext().setPartialRequest(isPartial);
    }

    /**
     *	<p> This handler is different than JSFT's default navigate handler in
     *	    that it forces the request to NOT be a "partial request".  The
     *	    effect is that no wrapping of the response will be done.  This is
     *	    normally done in JSF2 in order to work with the jsf.js JS code
     *	    that handles the response.  In the Admin Console, we typically do
     *	    not use this JS, so this is not desirable behavior.</p>
     *
     *	<p> Input value: "page" -- Type: <code>Object</code> (should be a
     *	    <code>String</code> or a <code>UIViewRoot</code>).</p>
     *
     *	<p> See JSFTemplating's built-in navigate handler for more info.</p>
     *
     *	@param	context	The {@link HandlerContext}.
     */
    @Handler(id="gf.navigate",
	input={
	    @HandlerInput(name="page", type=Object.class, required=true)
	})
    public static void navigate(HandlerContext context) {
	context.getFacesContext().getPartialViewContext().setPartialRequest(false);
	NavigationHandlers.navigate(context);
    }

    /**
     *	<p> This handler redirects to the given page.</p>
     *
     *	<p> Input value: "page" -- Type: <code>String</code></p>
     *
     *	@param	context	The {@link HandlerContext}.
     */
    @Handler(id="gf.redirect",
	input={
	    @HandlerInput(name="page", type=String.class, required=true)
	})
    public static void redirect(HandlerContext context) {
	String page = (String) context.getInputValue("page");
	FacesContext ctx = context.getFacesContext();
	page = handleBareAttribute(ctx, page);
	//if (ctx.getPartialViewContext().isPartialRequest()) {
	    // FIXME: I should be able to call setPartialRequest(false),
	    // FIXME: however, isAjaxRequest will still return true, and the
	    // FIXME: following line will not work correctly (it'll wrap it in
	    // FIXME: <xml> stuff and send it to the client):
	    // FIXME:   ctx.getExternalContext().redirect(page);
	    // FIXME: Work-a-round: call servlet api's directly
	//}
	try {
	    // FIXME: Should be: ctx.getExternalContext().redirect(page);  See FIXME above.
	    ((HttpServletResponse) ctx.getExternalContext().getResponse()).sendRedirect(page);
	} catch (IOException ex) {
	    throw new RuntimeException(
		"Unable to redirect to page '" + page + "'!", ex);
	}
	ctx.responseComplete();
    }

    /**
     * If the bare attribute is found in the query string and the value is "true",
     * then add "bare=true" to the specified url string.
     * @param url
     * @return
     */
    private static String handleBareAttribute(FacesContext ctx, String url) {
	// Get Page Session...
	UIViewRoot root = ctx.getViewRoot();
	Map<String, Serializable> pageSession =
	    PageSessionResolver.getPageSession(ctx, root);
	if (pageSession == null) {
	    pageSession = PageSessionResolver.createPageSession(ctx, root);
	}
        String request = (String) ctx.getExternalContext().getRequestParameterMap().get("bare");
	if (request != null) {
	    // It was specified, use this.
	    if (request.equalsIgnoreCase("true")) {
		url = addQueryStringParam(url, "bare", "true");
		request = "true";
	    } else {
		request = "false";
	    }
	    pageSession.put("bare", request);
	} else {
	    // Get the Page Session Map
	    Object pageSessionValue = pageSession.get("bare");
	    if (Boolean.TRUE.equals(pageSessionValue)) {
		url = addQueryStringParam(url, "bare", "true");
	    } else {
		pageSession.put("bare", "false");
	    }
	}
	return url;
    }

    /**
     * Add the name/value pair to the given url.
     * @param url
     * @param name
     * @param value
     * @return
     */
    private static String addQueryStringParam(String url, String name, String value) {
        String sep = "?";
        // If a query string exists (i.e., the url already has "?foo=bar", then we
        // want to append to that string rather than starting a new one
        if (url.indexOf("?") > -1) {
            sep = "&";
        }
        String insert = sep + name + "=" + value; // TODO: HTML encode this

        // Should the url have a hash in it, we need the query string (addition) to
        // be inserted before that.
        int hash = url.indexOf("#");
        if (hash > -1) {
            url = url.substring(0, hash-1) + insert + url.substring(hash);
        } else {
            url = url + insert;
        }
        return url;
    }

    private static final int INDEX=0;
    
}
