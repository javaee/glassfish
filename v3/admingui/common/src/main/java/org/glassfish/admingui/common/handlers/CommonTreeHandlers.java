/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.admingui.common.handlers;
        
import com.sun.appserv.management.config.ApplicationConfig;
import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import org.glassfish.admingui.common.util.AMXUtil;

import org.glassfish.admingui.common.tree.FilterTreeEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;


/**
 *
 * @author anilam
 */
public class CommonTreeHandlers {
    
    /**
     *	<p> Default Constructor.</p>
     */
    public CommonTreeHandlers() {
    }
    
    
    /**
     *  <p> This handler filters out all the system apps from the list of objName available
     *      through the event object, based on the object-type attribute.
     */
     @Handler(id="filterSystemApps",
     input={
        @HandlerInput(name="appType", type=String.class, required=true)})
    public static Object filterSystemApps(HandlerContext handlerCtx) {
        FilterTreeEvent event = (FilterTreeEvent) handlerCtx.getEventObject();
        List orig = event.getChildObjects();
        ArrayList result = new ArrayList();
        
        if ( orig == null || orig.size() <=0 )
            return orig;
        String appType = (String)handlerCtx.getInputValue("appType");
        for(Object oneChild: orig ){
            if (oneChild instanceof ApplicationConfig){
                if (AMXUtil.isAppType( (ApplicationConfig) oneChild, appType)){
                    result.add(oneChild);
                }
            }
        }
        return result;
    }

     @Handler(id="removeWidgetsFromTree",
     input={
        @HandlerInput(name="tree", type=UIComponent.class, required=true)})
    public static void removeWidgetTreeHack(HandlerContext handlerCtx) {
	// Hack 4.3 WS tree so that it doesn't use widget components...
	UIComponent treeComp = (UIComponent) handlerCtx.getInputValue("tree");
	doHack(treeComp);
    }

    /**
     *	Ugly hack, remove asap.
     */
    private static void doHack(UIComponent currComp) {
	// ImageHyperlink hidden child (getImageFacet())
	UIComponent hackComp = (UIComponent) currComp.getAttributes().get("imageFacet");
	if (hackComp != null) {
	    hackComp.setRendererType(hackComp.getFamily());
	    doHack(hackComp);
	}

	// Content hyperlink...
	hackComp = (UIComponent) currComp.getAttributes().get("contentHyperlink");
	if (hackComp != null) {
	    hackComp.setRendererType(hackComp.getFamily());
	    doHack(hackComp);
	}

	// Turner: (NOTE: only calling the next method to see if I should replace the image)
	FacesContext ctx = FacesContext.getCurrentInstance();
	hackComp = (UIComponent) currComp.getAttributes().get("turnerImageHyperlink");
	if (hackComp != null) {
	    // Replace with glassfish.ImageHyperlink (see our faces-config.xml)
	    hackComp = ctx.getApplication().createComponent("com.sun.webui.jsf.IconHyperlink");
	    hackComp.setId(currComp.getId().concat("_turner"));
	    hackComp.setRendererType(hackComp.getFamily());
	    currComp.getFacets().put("_turner", hackComp);
	    doHack(hackComp);
	}

	// Node Image: (NOTE: only calling the next method to see if I should replace the image)
	hackComp = (UIComponent) currComp.getAttributes().get("nodeImageHyperlink");
	if (hackComp != null) {
	    // Replace with glassfish.ImageHyperlink (see our faces-config.xml)
	    hackComp = ctx.getApplication().createComponent("com.sun.webui.jsf.ImageHyperlink");
	    hackComp.setId(currComp.getId().concat("_image"));
	    hackComp.setRendererType(hackComp.getFamily());
	    currComp.getFacets().put("_image", hackComp);
	    doHack(hackComp);
	}

	// imageKeys
	List<UIComponent> hackList = (List<UIComponent>) currComp.getAttributes().get("imageKeys");
	if (hackList != null) {
	    for (UIComponent child : hackList) {
		// WS component family values match their HTML renderer types...
		// so copy family to rendererType
		child.setRendererType(child.getFamily());
		doHack(child);
	    }
	}

	// Facets and children (recurses)
	Iterator<UIComponent> it = currComp.getFacetsAndChildren();
	UIComponent child = null;
	while (it.hasNext()) {
	    child = it.next();
	    if (child.getClass().getName().equals("com.sun.webui.jsf.component.TreeNode")) {
		// We have a TreeNode, do some special stuff...
		// imageKeys
		hackList = (List<UIComponent>) currComp.getAttributes().get("imageKeys");
		for (UIComponent treeImage : hackList) {
		    // WS component family values match their HTML renderer types...
		    // so copy family to rendererType
		    treeImage.setRendererType(treeImage.getFamily());
		}
	    } else {
		// WS component family values match their HTML renderer types...
		// so copy family to rendererType
		child.setRendererType(child.getFamily());
		/* Not sure how we get to this code-path in Deep's impl... */
	    }
	    doHack(child);
	}
    }
}
