/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.plugins.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 *
 * @author jdlee
 */
public class Activator implements BundleActivator {
    private volatile PluginTracker tracker;

    @Override
    public void start(BundleContext ctx) throws Exception {
        tracker = new PluginTracker(ctx);
        tracker.open();
    }

    @Override
    public void stop(BundleContext ctx) throws Exception {
        tracker.close();
        tracker = null;
    }
}