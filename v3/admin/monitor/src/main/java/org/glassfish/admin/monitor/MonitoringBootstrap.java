/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admin.monitor;

import java.beans.PropertyChangeEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.probe.provider.StatsProviderManagerDelegate;
import org.jvnet.hk2.component.PostConstruct;
import org.glassfish.probe.provider.StatsProviderManager;
import org.glassfish.api.Startup;
import org.glassfish.api.amx.MBeanListener;
//import org.glassfish.api.event.Events;
//import org.glassfish.api.event.EventListener;
//import org.glassfish.api.event.EventListener.Event;
//import org.glassfish.api.event.EventTypes;
//import org.glassfish.api.event.RestrictTo;
import com.sun.enterprise.config.serverbeans.*;
import org.glassfish.flashlight.MonitoringRuntimeDataRegistry;

import org.glassfish.internal.api.Init;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Singleton;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModuleState;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.ModuleLifecycleListener;

import java.lang.management.ManagementFactory;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Collections;
import java.util.StringTokenizer;
import org.glassfish.flashlight.client.ProbeClientMediator;
import org.glassfish.flashlight.provider.ProbeProviderFactory;
/**
 *
 * @author abbagani
 */
@Service
@Scoped(Singleton.class)
public class MonitoringBootstrap implements Startup, PostConstruct, Init, ModuleLifecycleListener, ConfigListener {

    @Inject
    private MonitoringRuntimeDataRegistry mrdr;
    @Inject
    private Domain domain;
    @Inject
    ModulesRegistry registry;
    @Inject
    protected ProbeProviderFactory probeProviderFactory;
    @Inject
    protected ProbeClientMediator pcm;
    //@Inject Events events;
    @Inject(optional=true)
    ModuleMonitoringLevels config = null;

    Map<String,Module> map = Collections.synchronizedMap(new WeakHashMap<String,Module>());

    private final boolean debug = false;
    private final String PROBE_PROVIDER_CLASS_NAMES = "probe-provider-class-names";
    private final String PROBE_PROVIDER_XML_FILE_NAMES = "probe-provider-xml-file-names";
    private final String DELIMITER = ",";
    private StatsProviderManagerDelegateImpl spmd;

    public void postConstruct() {
        //Set the StatsProviderManagerDelegate
        spmd = new StatsProviderManagerDelegateImpl(pcm, mrdr, domain, config);
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

        //events.register(this);

        // Register listener for AMX DomainRoot loaded
        MBeanListener.listenForDomainRoot(ManagementFactory.getPlatformMBeanServer(), spmd);
    }

    public Lifecycle getLifecycle() {
        // This service stays running for the life of the app server, hence SERVER.
        return Lifecycle.SERVER;
    }

    public void moduleResolved(Module module) {
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
            //if (!((cls.getCanonicalName().equals("org.glassfish.scripting.jruby.monitor.JRubyProbeProvider")) ||
            //      (cls.getCanonicalName().equals("org.glassfish.scripting.jruby.monitor.JRubyRuntimePoolProvider"))))
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

    /*public void event(Event event) {
        if (event.name().equals(EventTypes.PREPARE_SHUTDOWN_NAME)) {
            spmd.unregisterAll();
        }
    }*/

    public UnprocessedChangeEvents changed(PropertyChangeEvent[] propertyChangeEvents) {
       for (PropertyChangeEvent event : propertyChangeEvents) {
           if (event.getSource() instanceof ModuleMonitoringLevels) {
                String propName = event.getPropertyName();
                boolean enabled = getEnabledValue(event.getNewValue().toString());
                StatsProviderRegistry spr = spmd.getStatsProviderRegistry();
                if (spr != null) {
                    spr.setConfigEnabled(propName, enabled);
                    if (enabled) {
                        spr.enableStatsProvider(propName);
                    } else {
                        spr.disableStatsProvider(propName);
                    }
                }
           }
           //For change in mbean-enabled attribute register/unregister gmbal for enabled config elements
           //if (event.getSource() instanceof MbeanEnabled) {
           //for (String configElement : config.getElements()) {
                //if (!configElement.getValue().equals("OFF")) {
                    //if (mbeanEnabled) {
                        //spmd.getStatsProviderRegistry().registerGmbal(configElement);
                    //} else {
                        //spmd.getStatsProviderRegistry().unregisterGmbal(configElement);
                    //}
                //}
           //}
           //}
       }
       return null;
    }

    private boolean getEnabledValue(String enabledStr) {
        if ("OFF".equals(enabledStr)) {
            return false;
        }
        return true;
    }
}
