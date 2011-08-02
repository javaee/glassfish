/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.plugins.jsf;

import java.net.MalformedURLException;
import java.net.URL;
import javax.faces.context.ExternalContext;
import javax.faces.context.ExternalContextWrapper;

/**
 *
 * @author jdlee
 */
public class PluginExternalContext extends ExternalContextWrapper {
    private ExternalContext wrapped;

    public PluginExternalContext() {
        
    }
    
    public PluginExternalContext(ExternalContext wrapped) {
        this.wrapped = wrapped;
    }

    public URL getResource(String path) throws MalformedURLException {
        URL url = Thread.currentThread().getContextClassLoader().getResource(path.substring(1));
        if (url == null) {
            url = getWrapped().getResource(path);
        }

        return url;
    }

    @Override
    public ExternalContext getWrapped() {
        return wrapped;
    }
}