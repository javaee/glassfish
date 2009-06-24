/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admin.monitor;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.probe.provider.StatsProviderManagerDelegate;
import org.jvnet.hk2.component.PostConstruct;
import org.glassfish.probe.provider.StatsProviderManager;
import org.glassfish.api.Startup;
import com.sun.enterprise.config.serverbeans.*;
import org.glassfish.flashlight.MonitoringRuntimeDataRegistry;

import org.glassfish.internal.api.Init;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Singleton;

import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModuleState;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.ModuleLifecycleListener;

import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Collections;
import java.util.StringTokenizer;
import org.glassfish.flashlight.provider.ProbeProviderFactory;
/**
 *
 * @author abbagani
 */
@Service
@Scoped(Singleton.class)
public class MonitoringBootstrap implements Startup, PostConstruct, Init, ModuleLifecycleListener {

    @Inject
    private MonitoringRuntimeDataRegistry mrdr;
    @Inject
    private Domain domain;
    @Inject
    ModulesRegistry registry;
    @Inject
    protected ProbeProviderFactory probeProviderFactory;

    Map<String,Module> map = Collections.synchronizedMap(new WeakHashMap<String,Module>());

    private final boolean debug = false;
    private final String PROBE_PROVIDER_CLASS_NAMES = "probe-provider-class-names";
    private final String PROBE_PROVIDER_XML_FILE_NAMES = "probe-provider-xml-file-names";
    private final String DELIMITER = ",";

    public void postConstruct() {
        //Set the StatsProviderManagerDelegate
        StatsProviderManagerDelegate spmd = new StatsProviderManagerDelegateImpl(mrdr, domain);
        StatsProviderManager.setStatsProviderManagerDelegate(spmd);
        //System.out.println("StatsProviderManagerDelegate is assigned ********************");

        mprint("addon init postConstruct() ...");
        // Register as ModuleLifecycleListener
        registry.register(this);
        // Iterate thru existing modules
        for (Module m : registry.getModules()) {
            //mprint("***  State=" + m.getState().toString() + " - Module name = " + m.getName());
            //if (m.getState() == ModuleState.READY) {
                moduleStarted(m);
            //}
        }

    }

    public Lifecycle getLifecycle() {
        // This service stays running for the life of the app server, hence SERVER.
        return Lifecycle.SERVER;
    }

    public synchronized void moduleStarted(Module module) {
        if (module == null) return;
        String str = module.getName();
        //mprint("moduleStarted: " + str);
        if (!map.containsKey(str)) {
            map.put(str, module);
            addProvider(module);
        }
    }

    public synchronized void moduleStopped(Module module) {
        if (module == null) return;
        String str = module.getName();
        //mprint("moduleStopped: " + str);
        if (map.containsKey(str)) {
            map.remove(str);
            removeProvider(module);
        }
    }

    private void addProvider(Module module) {
        String mname = module.getName();
        //mprint("addProvider for " + mname + "...");
        ClassLoader mcl = module.getClassLoader();
        //get manifest entries and process
        ModuleDefinition md = module.getModuleDefinition();
        Manifest mf = null;
        if (md != null) {
            mf = md.getManifest();
        }
        if (mf != null) {
            Attributes attrs = mf.getMainAttributes();
            String cnames = null;
            String xnames = null;
            if (attrs != null) {
                cnames = attrs.getValue(PROBE_PROVIDER_CLASS_NAMES);
                if (cnames != null)
                    mprint("**************cnames = " + cnames);
                if (cnames != null) {
                    StringTokenizer st = new StringTokenizer(cnames, DELIMITER);
                    while (st.hasMoreTokens()) {
                        try {
                            processProbeProviderClass(mcl.loadClass(st.nextToken()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                xnames = attrs.getValue(PROBE_PROVIDER_XML_FILE_NAMES);
                if (xnames != null) {
                    mprint("xnames = " + xnames);
                    StringTokenizer st = new StringTokenizer(xnames, DELIMITER);
                    while (st.hasMoreTokens()) {
                        processProbeProviderXML(st.nextToken());
                    }
                }
            }
        }
    }

    private void removeProvider(Module module) {
        mprint("removeProvider ...");
    }

    private void processProbeProviderClass(Class cls) {
        mprint("processProbeProviderClass for " + cls);
        try {
            //System.out.println(" ************* Class = " + cls.getCanonicalName());
            if (!((cls.getCanonicalName().equals("org.glassfish.scripting.jruby.monitor.JRubyProbeProvider")) ||
                  (cls.getCanonicalName().equals("org.glassfish.scripting.jruby.monitor.JRubyRuntimePoolProvider"))))
                probeProviderFactory.getProbeProvider(cls);
            //
            // to implement
            //
        } catch (InstantiationException ex) {
            Logger.getLogger(MonitoringBootstrap.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(MonitoringBootstrap.class.getName()).log(Level.SEVERE, null, ex);
        }
        //
        // to implement
        //
    }

    private void processProbeProviderXML(String xname) {
        mprint("processProbeProviderXML for " + xname);
        //
        // to implement
        //
    }

    private void mprint(String str) {
        if (debug) {
            System.out.println("MSR: " + str);
        }
    }
}
