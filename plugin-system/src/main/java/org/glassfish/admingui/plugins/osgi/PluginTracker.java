/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.plugins.osgi;

import java.net.URL;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import org.glassfish.admingui.plugins.PluginService;
import org.jvnet.hk2.component.Habitat;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.util.tracker.BundleTracker;

/**
 *
 * @author jdlee
 */
class PluginTracker extends BundleTracker {

    public static final String HEADER_PLUGIN = "Console-Plugin";
    private PluginService ps;

    public PluginTracker(BundleContext context) {
        super(context, Bundle.RESOLVED | Bundle.STARTING | Bundle.ACTIVE, null);
        System.out.println("***** Starting PluginTracker");
        ps = getPluginService();
    }

    @Override
    public Object addingBundle(Bundle bundle, BundleEvent event) {
        if (bundle.getHeaders().get(HEADER_PLUGIN) != null) {
            try {
                bundle.start();
                Enumeration e = bundle.findEntries("/", "*.class", true);
                while (e.hasMoreElements()) {
                    final String className = ((URL) e.nextElement()).getFile().substring(1).replaceAll("/", ".").replaceAll(".class", "");
                    try {
                        bundle.loadClass(className);
                        ps.addClass(className);
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(PluginTracker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (BundleException ex) {
                Logger.getLogger(PluginTracker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return super.addingBundle(bundle, event);
    }
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