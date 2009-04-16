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

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import com.sun.jsftemplating.util.FileUtil;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.component.UIComponent;
import org.glassfish.admingui.common.factories.NavigationNodeFactory;

/**
 *
 * @author jasonlee
 */
public class PluginHandlers {

    /**
     *	<p> This handler is used for the navigation nodes that request content
     *	    from an external URL.  This handler pulls the "real url" from from
     *	    the component specified by the <code>compId</code> parameter (this
     *	    necessarily depends on the presence of the navigation container in
     *	    the view for the component look up to work).  Once the component
     *	    has been found, the url is retrieved from the attribute map, and
     *	    its contents retrieved.  If <code>processPage</code> is true, the
     *	    URL contents are interpretted and the resulting component(s) are
     *	    added to the component tree (This feature is not currently
     *	    supported)..  Otherwise, the contents are returned in the output
     *	    parameter <code>pluginPage</code> to be output as-is on the
     *	    page.</p>
     *
     * @param handlerCtx    The <code>HandlerContext</code>.
     */
    @Handler(id = "retrievePluginPageContents",
             input = {@HandlerInput(name = "compId", type = String.class, required = true)},
             output = {@HandlerOutput(name = "pluginPage", type = String.class)})
    public static void retrievePluginPageContents(HandlerContext handlerCtx) {
        String id = (String) handlerCtx.getInputValue("compId");
        UIComponent comp = handlerCtx.getFacesContext().getViewRoot().findComponent(id);
        String urlContents = "";
        if (comp != null) {
	    String url = url = (String) comp.getAttributes().get(NavigationNodeFactory.REAL_URL);
            try {
		// Read from the URL...
                URL contentUrl = FileUtil.searchForFile(url, "");
                urlContents = new String(FileUtil.readFromURL(contentUrl));

                // FIXME: Implement processPage support
		/*
		if (processPage) {
		    // probably do something like what includeIntegrations does
		    ...
		}
		*/
            } catch (IOException ex) {
                Logger.getLogger(PluginHandlers.class.getName()).log(Level.SEVERE, "Unable to read url: " + url, ex);
            }
        }

	// Set the content to output...
        handlerCtx.setOutputValue("pluginPage", urlContents);
    }
}
