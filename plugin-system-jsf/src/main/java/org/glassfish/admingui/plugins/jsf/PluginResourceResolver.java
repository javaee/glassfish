/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.plugins.jsf;

import java.net.URL;
import javax.faces.view.facelets.ResourceResolver;

/**
 *
 * @author jdlee
 */
public class PluginResourceResolver extends ResourceResolver {
    private ResourceResolver parent;

    public PluginResourceResolver(ResourceResolver parent) {
        this.parent = parent;
    }

    @Override
    public URL resolveUrl(String path) {
        URL url = null;
        try {
            url = url = Thread.currentThread().getContextClassLoader().getResource(path.substring(1));
            if (url == null) {
                url = parent.resolveUrl(path);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return url;
    }
}
