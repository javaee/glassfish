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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;

/**
 *
 * @author jasonlee
 */
public class PluginHandlers {

    @Handler(id = "retrievePluginPageContents",
             input = {@HandlerInput(name = "pluginId", type = String.class, required = true)},
             output = {@HandlerOutput(name = "pluginPage", type = String.class)})
    public static void retrievePluginPageContents(HandlerContext handlerCtx) {
        String id = (String) handlerCtx.getInputValue("pluginId");
        UIViewRoot viewRoot = handlerCtx.getFacesContext().getViewRoot();
        UIComponent comp = handlerCtx.getFacesContext().getViewRoot().findComponent(id);
        String url = "";
        if (comp != null) {
            url = (String) comp.getAttributes().get("realUrl");
        }

        String urlContents = "";
        try {
            URL contentUrl = FileUtil.searchForFile(url, "");
            urlContents = new String(FileUtil.readFromURL(contentUrl));
        } catch (IOException ex) {
            Logger.getLogger(PluginHandlers.class.getName()).log(Level.SEVERE, null, ex);
        }

        handlerCtx.setOutputValue("pluginPage",urlContents);
    }
}