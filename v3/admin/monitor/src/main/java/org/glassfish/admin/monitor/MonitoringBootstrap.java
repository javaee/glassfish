/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admin.monitor;

import java.beans.PropertyChangeEvent;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jvnet.hk2.component.PostConstruct;
import org.glassfish.external.probe.provider.StatsProviderManager;
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

import java.io.File;
import java.io.FilenameFilter;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;
import org.glassfish.api.amx.MBeanListener;
import org.glassfish.api.event.Events;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.monitoring.ContainerMonitoring;
import org.glassfish.flashlight.client.ProbeClientMediator;
import org.glassfish.flashlight.impl.provider.FlashlightProbeProviderFactory;
import org.glassfish.flashlight.provider.ProbeProviderFactory;
import org.glassfish.internal.api.Init;
/**
 *
 * @author abbagani
 */
@Service
@Scoped(Singleton.class)
public class MonitoringBootstrap implements Init, PostConstruct, EventListener, ModuleLifecycleListener, ConfigListener {

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
    @Inject
    Events events;

    @Inject(optional=true)
    MonitoringService monitoringService = null;
    @Inject
    private org.glassfish.flashlight.provider.ProbeRegistry probeRegistry;


    Map<String,Module> map = Collections.synchronizedMap(new WeakHashMap<String,Module>());

    private static final String INSTALL_ROOT_URI_PROPERTY_NAME = "com.sun.aas.installRootURI";

    private final boolean debug = false;
    private final String PROBE_PROVIDER_CLASS_NAMES = "probe-provider-class-names";
    private final String PROBE_PROVIDER_XML_FILE_NAMES = "probe-provider-xml-file-names";
    private final String DELIMITER = ",";
    private StatsProviderManagerDelegateImpl spmd;
    private boolean ddebug = false;

    public void postConstruct() {
        // Register as ModuleLifecycleListener
        events.register(this);
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


    public void event(Event event) {
        if (event.is(EventTypes.SERVER_READY)) {
            mprint("***************************************");
            mprint("**** SERVER_READY event received *****");
            mprint("***************************************");
            discoverXMLProviders();
        }
    }

    public void setStatsProviderManagerDelegate() {
        //Set the StatsProviderManagerDelegate
        spmd = new StatsProviderManagerDelegateImpl(pcm, probeRegistry, mrdr, domain, monitoringService);
        StatsProviderManager.setStatsProviderManagerDelegate(spmd);
        mprint(" StatsProviderManagerDelegate is assigned ********************");

        // Register listener for AMX DomainRoot loaded
        MBeanListener.listenForDomainRoot(ManagementFactory.getPlatformMBeanServer(), spmd);
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
                        processProbeProviderXML(mcl, st.nextToken(), true);
                    }
                }
            }
        }
    }

    private void discoverXMLProviders() {
        try {
            URI xmlProviderDirStr = new URI(System.getProperty(INSTALL_ROOT_URI_PROPERTY_NAME) + "/" + "lib" + "/" + "monitor");
            printd("ProviderXML's Dir = " + xmlProviderDirStr.getPath());
            File xmlProviderDir = new File(xmlProviderDirStr.getPath());
            //File scriptFile = new File ("/space/GFV3_BLD/glassfish/domains/domain1/applications/scripts/InvokeJavaFromJavascript.js");
            printd("ProviderXML's Dir exists = " + xmlProviderDir.exists());
            printd("ProviderXML's Dir path - " + xmlProviderDir.getAbsolutePath());
            loadXMLProviders(xmlProviderDir);
        } catch (URISyntaxException ex) {
            Logger.getLogger(MonitoringBootstrap.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadXMLProviders(File xmlProvidersDir) {
        // Creates a filter which will return only xml files
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }
        };
        // Retrieves all the provider XML's
        File[] files = xmlProvidersDir.listFiles(filter);
        if (files == null)
            return;
        Map<String, File> providerMap = new HashMap();

        for (File file : files) {
            printd("Found the provider xml - " + file.getAbsolutePath());
            int index = file.getName().indexOf("-:");
            mprint ( "index = " + index);
            if (index != -1) {
                String moduleName = file.getName().substring(0,index);
                providerMap.put(moduleName, file);
                mprint(" The provider xml belongs to - \"" + moduleName + "\"");
                for (Module module:map.values()) {
                    if (module.getName().contains("grizzly"))
                        mprint (" module = \"" + module.getName() + "\"");
                }
                if (!map.containsKey(moduleName)) {
                    Logger.getLogger(MonitoringBootstrap.class.getName()).log(Level.SEVERE,
                            "Couldn't find the module, when loading the monitoring providers " +
                            "from XML directory : " + moduleName);
                    continue;
                }
                mprint (" Module found (containsKey)");
                Module module = map.get(moduleName);
                if (module == null) {
                    Logger.getLogger(MonitoringBootstrap.class.getName()).log(Level.SEVERE,
                            "Couldn't find the module, when loading the monitoring providers " +
                            "from XML directory : " + moduleName);
                } else {
                    ClassLoader mcl = module.getClassLoader();
                    mprint("ModuleClassLoader = " + mcl);
                    mprint("XML File path = " + file.getAbsolutePath());
                    processProbeProviderXML(mcl, file.getAbsolutePath(), false);
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

            probeProviderFactory.getProbeProvider(cls);

        } catch (InstantiationException ex) {
            Logger.getLogger(MonitoringBootstrap.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(MonitoringBootstrap.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void processProbeProviderXML(ClassLoader mcl, String xname, boolean inBundle) {
        probeProviderFactory.processXMLProbeProviders(mcl, xname, inBundle);
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
        StatsProviderRegistry spr = spmd.getStatsProviderRegistry();

        for (PropertyChangeEvent event : propertyChangeEvents) {
            // let's get out of here ASAP if it is not our stuff!!
            if(event == null)
                continue;

            String propName = event.getPropertyName();
            Object oldVal = event.getOldValue();
            Object newVal = event.getNewValue();

            if(newVal == null || newVal.equals(oldVal))
                continue;   // no change!!

            if(!ok(propName))
                continue;

            if (event.getSource() instanceof ContainerMonitoring) {
                ContainerMonitoring cm = (ContainerMonitoring)event.getSource();
                boolean newEnabled = parseLevelsBoolean(newVal.toString());
                
                // complications!  What if old is null? we fake out the rest of
                // the logic to think it changed...
                boolean oldEnabled = (oldVal == null) ? !newEnabled : parseLevelsBoolean(oldVal.toString());
                
                if((newEnabled != oldEnabled) && (spr != null)) // no spr -- means no work can be done.
                    handleLevelChange(cm.getName(), newEnabled);
            }
            else if(event.getSource() instanceof MonitoringService) {
                // we don't want to get fooled because config allows ANY string.
                // e.g. "false" --> "foo" --> "fals" are all NOT changes!
                // so we convert to boolean and then compare...
                boolean newEnabled = Boolean.parseBoolean(newVal.toString());
                boolean oldEnabled = (oldVal == null) ? !newEnabled : Boolean.parseBoolean(oldVal.toString());

                if(newEnabled != oldEnabled)
                    handleServiceChange(spr, propName, newEnabled);
            }
        }

       return null;
    }

    private void handleLevelChange(String propName, boolean enabled) {
        if(!ok(propName))
            return;

        StatsProviderRegistry spr = spmd.getStatsProviderRegistry();

        if(spr == null)
            return; // nothing to do!

        spr.setConfigEnabled(propName, enabled);

        if (enabled)
            spr.enableStatsProvider(propName);
        else
            spr.disableStatsProvider(propName);
    }

    private void handleServiceChange(StatsProviderRegistry spr, String propName, boolean enabled) {
        if(!ok(propName))
            return;

        if (propName.equals("mbean-enabled")) {
            if(spr == null) // required!
                return;

            if(enabled)
                spr.registerAllGmbal();
            else
                spr.unregisterAllGmbal();
        }
        else if(propName.equals("dtrace-enabled")) {
            //TODO
            probeProviderFactory.dtraceEnabledChanged(enabled);
        }
        else if(propName.equals("monitoring-enabled")) {
            // TODO
            probeProviderFactory.monitoringEnabledChanged(enabled);
        }
    }
    
    private boolean ok(String s) {
        return s != null && s.length() > 0;
    }

    private boolean parseLevelsBoolean(String s) {
        if (ok(s) && s.equals("OFF"))
            return false;

        return true;
    }
}
