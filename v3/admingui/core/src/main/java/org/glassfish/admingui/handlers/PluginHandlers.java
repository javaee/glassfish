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

import org.glassfish.admingui.plugin.ConsolePluginService;
import org.glassfish.admingui.plugin.IntegrationPoint;

import org.jvnet.hk2.component.Habitat;

import java.lang.reflect.Method;
import java.util.List;

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
     *	<p> Obtain the ConsolePluginService, however, we treat it as an Object
     *	    since it is loaded by a different ClassLoader.</p>
     */
    private static Object getPluginService(FacesContext ctx) {
	ServletContext servletCtx = (ServletContext)
	    (ctx.getExternalContext()).getContext();
	Habitat habitat = (Habitat)
	    servletCtx.getAttribute("com.sun.appserv.jsf.habitat");
//	System.out.println("Habitat:" + habitat);

	return habitat.getByType(ConsolePluginService.class);
    }


    /**
     *
     *	@param	context	The <code>HandlerContext</code>.
     */
    @Handler(id="getIntegrationPoints",
    	input={
            @HandlerInput(name="key", type=String.class, required=true)},
        output={
            @HandlerOutput(name="points", type=List.class)})
    public static void getIntegrationPoints(HandlerContext handlerCtx) {
	String key = (String) handlerCtx.getInputValue("key");
	List<IntegrationPoint> value =
	    getIntegrationPoints(handlerCtx.getFacesContext(), key);
	handlerCtx.setOutputValue("points", value);
    }

    /**
     *
     */
    public static List<IntegrationPoint> getIntegrationPoints(FacesContext context, String key) {
	Object value = null;
	try {
//System.out.println("" + org.glassfish.admingui.util.AMXUtil.getDomainRoot());
	    Object obj = getPluginService(context);
	    Method meth = obj.getClass().getMethod("getIntegrationPoints", String.class);
	    value = meth.invoke(obj, key);
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	return (List<IntegrationPoint>) value;
    }
}
