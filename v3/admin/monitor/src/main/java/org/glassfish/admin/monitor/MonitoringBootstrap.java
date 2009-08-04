/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admin.monitor;

import java.beans.PropertyChangeEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jvnet.hk2.component.PostConstruct;
import org.glassfish.external.probe.provider.StatsProviderManager;
import org.glassfish.api.Startup;
import com.sun.enterprise.config.serverbeans.*;
import org.glassfish.flashlight.MonitoringRuntimeDataRegistry;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Singleton;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

import com.sun.enterprise.module.Module;
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
import org.glassfish.api.amx.MBeanListener;
import org.glassfish.flashlight.client.ProbeClientMediator;
import org.glassfish.flashlight.provider.ProbeProviderFactory;
/**
 *
 * @author abbagani
 */
@Service
@Scoped(Singleton.class)
public class MonitoringBootstrap implements Startup, PostConstruct, ModuleLifecycleListener, ConfigListener {

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
    @Inject(optional=true)
    ModuleMonitoringLevels config = null;
    @Inject(optional=true)
    MonitoringService monitoringService = null;
    @Inject
    private org.glassfish.flashlight.provider.ProbeRegistry probeRegistry;


    Map<String,Module> map = Collections.synchronizedMap(new WeakHashMap<String,Module>());

    private final boolean debug = false;
    private final String PROBE_PROVIDER_CLASS_NAMES = "probe-provider-class-names";
    private final String PROBE_PROVIDER_XML_FILE_NAMES = "probe-provider-xml-file-names";
    private final String DELIMITER = ",";
    private StatsProviderManagerDelegateImpl spmd;
    private boolean ddebug = false;

    public void postConstruct() {
        // Register as ModuleLifecycleListener
        registry.register(this);
        // Iterate thru existing modules
        for (Module m : registry.getModules()) {
            //mprint("***  State=" + m.getState().toString() + " - Module name = " + m.getName());
            //if (m.getState() == ModuleState.READY) {
                printd(" In startup, calling moduleStarted");
                moduleStarted(m);
            //}
        }

        //Set the StatsProviderManagerDelegate
        setStatsProviderManagerDelegate();
    }

    public void setStatsProviderManagerDelegate() {
        //Set the StatsProviderManagerDelegate
        spmd = new StatsProviderManagerDelegateImpl(pcm, probeRegistry, mrdr, domain, monitoringService);
        StatsProviderManager.setStatsProviderManagerDelegate(spmd);
        mprint(" StatsProviderManagerDelegate is assigned ********************");

        // Register listener for AMX DomainRoot loaded
        MBeanListener.listenForDomainRoot(ManagementFactory.getPlatformMBeanServer(), spmd);
    }

    public Lifecycle getLifecycle() {
        // This service stays running for the life of the app server, hence SERVER.
        return Lifecycle.SERVER;
    }

    public void moduleResolved(Module module) {
        printd(" In module resolved, but not registering, module = " + module.getName());
        //TODO - Should we call moduleStarted()?
    }

    public synchronized void moduleStarted(Module module) {
        if (module == null) return;
        String str = module.getName();
        //mprint("moduleStarted: " + str);
        printd(" moduleStarted : " + str);
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
                    printd("**************probe providers = " + cnames);
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
                        processProbeProviderXML(mcl, st.nextToken());
                    }
                }
            }
        }
    }

    private void printd(String pstring) {
        if (ddebug == true)
            System.out.println(" APK : " + pstring);
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

    private void processProbeProviderXML(ClassLoader mcl, String xname) {
        probeProviderFactory.processXMLProbeProviders(mcl, xname);
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
           if (event.getSource() instanceof MonitoringService) {
                String propName = event.getPropertyName();
                if (propName.equals("mbean-enabled")) {
                    StatsProviderRegistry spr = spmd.getStatsProviderRegistry();
                    if (event.getNewValue().toString().equals("true")) {
                        spr.registerAllGmbal();
                    } else {
                        spr.unregisterAllGmbal();
                    }
                }
           }
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
