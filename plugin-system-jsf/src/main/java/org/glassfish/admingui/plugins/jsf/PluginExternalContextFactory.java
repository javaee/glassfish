/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.plugins.jsf;

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import javax.faces.context.ExternalContextFactory;

/**
 *
 * @author jdlee
 */
public class PluginExternalContextFactory extends ExternalContextFactory {
    private ExternalContextFactory parent;

    public PluginExternalContextFactory (ExternalContextFactory parent) {
        super();
        this.parent = parent;
    }

    @Override
    public ExternalContext getExternalContext(Object context, Object request, Object response) throws FacesException {
        return new PluginExternalContext(getWrapped().getExternalContext(context, request, response));
    }

    @Override
    public ExternalContextFactory getWrapped() {
        return parent;
    }
}