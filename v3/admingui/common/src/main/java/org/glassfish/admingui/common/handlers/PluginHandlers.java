/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
     * This handler is used for the navigation nodes that request content from
     * an external URL.  This handler pulls the "real url" from from the component
     * specified by the <code>compId</code> parameter (This necessarily depends on
     * the presence of the navigation container in the view for the component look
     * up to work).  Once the component has been found, the url is retrieved from
     * the attribute map, and it's contents retrieved via the two FileUtil methods
     * below.  If <code>processPage</code> is true, the URL contents are processed
     * by the console run time and the resulting component(s) are added to the
     * component tree (This feature is not currently supported)..  Otherwise, the '
     * contents are returned in the output parameter <code>pluginPage</code> to
     * be rendered as is on the page.
     * @param handlerCtx
     */
    @Handler(id = "retrievePluginPageContents",
             input = {@HandlerInput(name = "compId", type = String.class, required = true)},
             output = {@HandlerOutput(name = "pluginPage", type = String.class)})
    public static void retrievePluginPageContents(HandlerContext handlerCtx) {
        String id = (String) handlerCtx.getInputValue("compId");
        UIComponent comp = handlerCtx.getFacesContext().getViewRoot().findComponent(id);
        String urlContents = "";
        if (comp != null) {
            try {
                String url = url = (String) comp.getAttributes().get(NavigationNodeFactory.REAL_URL);

                // Implement processPage support
                URL contentUrl = FileUtil.searchForFile(url, "");
                urlContents = new String(FileUtil.readFromURL(contentUrl));
            } catch (IOException ex) {
                Logger.getLogger(PluginHandlers.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        handlerCtx.setOutputValue("pluginPage", urlContents);
    }
}