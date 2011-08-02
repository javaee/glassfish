/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.plugins.jsf;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import org.glassfish.admingui.plugins.PluginService;
import org.jvnet.hk2.component.Habitat;

/**
 *
 * @author jdlee
 */
public class PluginUtil {
    public static final String HABITAT_ATTRIBUTE = "org.glassfish.servlet.habitat";
    private static Habitat habitat;
    
    public static Habitat getHabitat() {
        if (habitat == null) {
            ServletContext servletCtx = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
            // Get the Habitat from the ServletContext
            habitat = (Habitat) servletCtx.getAttribute(HABITAT_ATTRIBUTE);
        }
        
        return habitat;
    }

    public static PluginService getPluginService() {
        return getHabitat().getComponent(PluginService.class);
    }
}
