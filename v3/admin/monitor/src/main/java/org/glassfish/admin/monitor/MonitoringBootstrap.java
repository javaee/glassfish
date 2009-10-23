/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admin.monitor;

import java.beans.PropertyChangeEvent;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.external.probe.provider.StatsProviderInfo;
import org.jvnet.hk2.component.*;
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
import com.sun.enterprise.module.ModuleState;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.ModuleLifecycleListener;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.logging.LogDomains;

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

import org.glassfish.external.amx.AMXGlassfish;

import org.glassfish.api.event.Events;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.monitoring.ContainerMonitoring;
import org.glassfish.flashlight.client.ProbeClientMediator;
import org.glassfish.flashlight.provider.ProbeProviderFactory;
import org.glassfish.flashlight.provider.ProbeRegistry;
import org.glassfish.internal.api.Init;
/**
 *
 * @author abbagani
 */
@Service
@Scoped(Singleton.class)
public class MonitoringBootstrap implements Init, PostConstruct, PreDestroy, EventListener, ModuleLifecycleListener, ConfigListener {
    @Inject
    private MonitoringRuntimeDataRegistry mrdr;
    @Inject
    private Domain domain;
    @Inject
    private ModulesRegistry registry;
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
    private static final Logger logger =
        LogDomains.getLogger(MonitoringBootstrap.class, LogDomains.MONITORING_LOGGER);
    public final static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(MonitoringBootstrap.class);


    private final String PROBE_PROVIDER_CLASS_NAMES = "probe-provider-class-names";
    private final String PROBE_PROVIDER_XML_FILE_NAMES = "probe-provider-xml-file-names";
    private final String DELIMITER = ",";
    private StatsProviderManagerDelegateImpl spmd;
    private boolean monitoringEnabled;
    private boolean hasDiscoveredXMLProviders = false;

    public void postConstruct() {
        // wbn: This value sticks for the life of the bootstrapping.  If the user changes it
        // somehow during bootstrapping we would have some problems so we just get the value
        // and run with it...

        if(monitoringService != null)
            monitoringEnabled = Boolean.parseBoolean(monitoringService.getMonitoringEnabled());
        else
            monitoringEnabled = false;

        //Don't listen for any events and dont process any probeProviders or statsProviders (dont set delegate)
        if (!monitoringEnabled)
                return;

        // Register as ModuleLifecycleListener
        events.register(this);

        boolean isDiscoverXMLProbeProviders = false;
        enableMonitoringForProbeProviders(isDiscoverXMLProbeProviders);
    }

    private void discoverProbeProviders() {
        // Iterate thru existing modules
        logger.log(Level.INFO,
                    localStrings.getLocalString("discoveringProbeProviders",
                                                    "Discovering the ProbeProviders"));
        for (Module m : registry.getModules()) {
            if ((m.getState() == ModuleState.READY) || (m.getState() == ModuleState.RESOLVED)) {
                printd( " In (discoverProbeProviders) ModuleState - " + m.getState() + " : " + m.getName());
                verifyModule(m);
            }
        }
    }

    public void preDestroy() {
        //We need to do the cleanup for preventing errors from server starting in Embedded mode
        ProbeRegistry.cleanup();
        if (spmd != null) {
            spmd = new StatsProviderManagerDelegateImpl(pcm, probeRegistry, mrdr, domain, monitoringService);
            StatsProviderManager.setStatsProviderManagerDelegate(spmd);
        }
    }

    public void event(Event event) {
        if (event.is(EventTypes.SERVER_READY)) {
            String msg = localStrings.getLocalString("discoveringXmlProbeProviders",
                                        "Discovering the XML ProbeProviders from lib/monitor");
            // Process the XMLProviders in lib/monitor dir. Should be the last thing to do in server startup.
            logger.log(Level.INFO, msg);
            discoverXMLProviders();
        }
    }

    public void setStatsProviderManagerDelegate() {
        // only run the code one time!
        if(spmd != null)
            return;

        //Set the StatsProviderManagerDelegate, so we can start processing the StatsProviders
        spmd = new StatsProviderManagerDelegateImpl(pcm, probeRegistry, mrdr, domain, monitoringService);
        StatsProviderManager.setStatsProviderManagerDelegate(spmd);
        printd(" StatsProviderManagerDelegate is assigned ********************");

        // Register listener for AMX DomainRoot loaded
        final AMXGlassfish amxg = AMXGlassfish.DEFAULT;
        amxg.listenForDomainRoot(ManagementFactory.getPlatformMBeanServer(), spmd);
    }

    public void moduleResolved(Module module) {
        if (module == null) return;
        verifyModule(module);
    }

    public synchronized void moduleStarted(Module module) {
        if (module == null) return;
        verifyModule(module);
    }

    private synchronized void verifyModule(Module module) {
        if (module == null) return;
        String str = module.getName();
        if (!map.containsKey(str)) {
            map.put(str, module);
            addProvider(module);
        }
    }

    public synchronized void moduleStopped(Module module) {
        if (module == null) return;
        String str = module.getName();
        //Cannot really remove the Provider b'cos of a bug in BTrace. We should just not reprocess the module
        /*
        if (map.containsKey(str)) {
            map.remove(str);
            removeProvider(module);
        }
        */
    }

    private void addProvider(Module module) {
        printd(" Adding the Provider - verified the module");
        String mname = module.getName();
        //printd("addProvider for " + mname + "...");
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
                            String clStr = st.nextToken();
                            if (clStr != null)
                                clStr = clStr.trim();
                            if (mcl != null)
                                processProbeProviderClass(mcl.loadClass(clStr));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                xnames = attrs.getValue(PROBE_PROVIDER_XML_FILE_NAMES);
                if (xnames != null) {
                    printd("xnames = " + xnames);
                    StringTokenizer st = new StringTokenizer(xnames, DELIMITER);
                    while (st.hasMoreTokens()) {
                        processProbeProviderXML(mcl, st.nextToken(), true);
                    }
                }
            }
        }
        handleFutureStatsProviders();
    }

    public void handleFutureStatsProviders() {
        // we just registered a Probe Provider
        // If there are any future items -- let's try to register them again.

        if(FutureStatsProviders.isEmpty())
            return; // Performance note -- this should be the case almost always

        List<StatsProviderInfo> removeList = new ArrayList<StatsProviderInfo>();
        Iterator<StatsProviderInfo> it = FutureStatsProviders.iterator();

        // the iterator does not allow the remove operation - thus the complexity!
        while(it.hasNext()) {
            StatsProviderInfo spInfo = it.next();
            try {
                spmd.tryToRegister(spInfo);
                removeList.add(spInfo);
            }
            catch(RuntimeException re) {
                // no probe registered yet...
            }
        }

        for(StatsProviderInfo spInfo : removeList) {
            FutureStatsProviders.remove(spInfo);
        }
    }

    private void discoverXMLProviders() {
        // Dont process if already discovered, Ideally we should do this whenever a new XML is dropped in lib/monitor
        if (hasDiscoveredXMLProviders)
            return;

        try {
            URI xmlProviderDirStr = new URI(System.getProperty(INSTALL_ROOT_URI_PROPERTY_NAME) + "/" + "lib" + "/" + "monitor");
            printd("ProviderXML's Dir = " + xmlProviderDirStr.getPath());
            File xmlProviderDir = new File(xmlProviderDirStr.getPath());
            //File scriptFile = new File ("/space/GFV3_BLD/glassfish/domains/domain1/applications/scripts/InvokeJavaFromJavascript.js");
            printd("ProviderXML's Dir exists = " + xmlProviderDir.exists());
            printd("ProviderXML's Dir path - " + xmlProviderDir.getAbsolutePath());
            loadXMLProviders(xmlProviderDir);
            hasDiscoveredXMLProviders = true;
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
            if (index != -1) {
                String moduleName = file.getName().substring(0,index);
                providerMap.put(moduleName, file);
                printd(" The provider xml belongs to - \"" + moduleName + "\"");
                for (Module module:map.values()) {
                    if (module.getName().contains("grizzly"))
                        printd(" module = \"" + module.getName() + "\"");
                }
                if (!map.containsKey(moduleName)) {
                    continue;
                }
                printd (" Module found (containsKey)");
                Module module = map.get(moduleName);
                if (module == null) {
                    logger.log(Level.SEVERE,
                            localStrings.getLocalString("monitoringMissingModuleFromXmlProbeProviders",
                                        "Couldn't find the module, when loading the monitoring providers " +
                                        "from XML directory : {0}", moduleName));
                } else {
                    ClassLoader mcl = module.getClassLoader();
                    printd("ModuleClassLoader = " + mcl);
                    printd("XML File path = " + file.getAbsolutePath());
                    processProbeProviderXML(mcl, file.getAbsolutePath(), false);
                }

            }

        }
    }

    private void removeProvider(Module module) {
        //Cannot really remove the Provider b'cos of a bug in BTrace (Cannot re-retransform).
        // We should just not reprocess the module, next time around
        printd("removeProvider ...");
    }

    private void processProbeProviderClass(Class cls) {
        printd("processProbeProviderClass for " + cls);
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

    /*public void event(Event event) {
        if (event.name().equals(EventTypes.PREPARE_SHUTDOWN_NAME)) {
            spmd.unregisterAll();
        }
    }*/

    public UnprocessedChangeEvents changed(PropertyChangeEvent[] propertyChangeEvents) {
        printd(" spmd = " + spmd);
        StatsProviderRegistry spr = (spmd == null) ? null : spmd.getStatsProviderRegistry();
        printd("spr = " + spr);
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

            if (event.getSource() instanceof ModuleMonitoringLevels) {
                String newEnabled = newVal.toString().toUpperCase();
                String oldEnabled = (oldVal == null) ? "OFF" : oldVal.toString().toUpperCase();
                printd(localStrings.getLocalString("levelChangeEventReceived",
                                "Level change event received, {0} New Level = {1}, Old Level = {2}",
                                propName, newEnabled, oldEnabled));
                if ((!newEnabled.equals(oldEnabled)) && (spr != null)) {
                    handleLevelChange(propName, newEnabled);
                }
            }
            else if (event.getSource() instanceof ContainerMonitoring) {
                ContainerMonitoring cm = (ContainerMonitoring)event.getSource();

                String newEnabled = newVal.toString().toUpperCase();
                String oldEnabled = (oldVal == null) ? "OFF" : oldVal.toString().toUpperCase();
                printd(localStrings.getLocalString("levelChangeEventReceived",
                                "Level change event received, {0} New Level = {1}, Old Level = {2}",
                                propName, newEnabled, oldEnabled));
                if ((!newEnabled.equals(oldEnabled)) && (spr != null)) {
                    handleLevelChange(cm.getName(), newEnabled);
                }
            }
            else if(event.getSource() instanceof MonitoringService) {
                // we don't want to get fooled because config allows ANY string.
                // e.g. "false" --> "foo" --> "fals" are all NOT changes!
                // so we convert to boolean and then compare...
                boolean newEnabled = Boolean.parseBoolean(newVal.toString());
                boolean oldEnabled = (oldVal == null) ? !newEnabled : Boolean.parseBoolean(oldVal.toString());
                printd(localStrings.getLocalString("levelChangeEventReceived",
                                "Level change event received, {0} New Level = {1}, Old Level = {2}",
                                propName, newEnabled, oldEnabled));

                if(newEnabled != oldEnabled)
                    handleServiceChange(spr, propName, newEnabled);
            }
        }

       return null;
    }

    private void handleLevelChange(String propName, String enabledStr) {
        printd("In handleLevelChange(), spmd = " + spmd + "  Enabled="+enabledStr);
        if(!ok(propName))
            return;

        if(spmd == null)
            return; // nothing to do!

        if (parseLevelsBoolean(enabledStr)) {
            logger.log(Level.INFO,
                    localStrings.getLocalString("enableStatsMonitoring",
                            "Enabling the monitoring for all the stats with level = {0}", enabledStr));
            spmd.enableStatsProviders(propName);
        } else {
            localStrings.getLocalString("disableStatsMonitoring",
                    "Disabling the monitoring for all the stats");
            spmd.disableStatsProviders(propName);
        }
    }

    private void handleServiceChange(StatsProviderRegistry spr, String propName, boolean enabled) {
        if(!ok(propName))
            return;

        if (propName.equals("mbean-enabled")) {
            if(spr == null) // required!
                return;

            if(enabled) {
                logger.log(Level.INFO,
                        localStrings.getLocalString("mbeanEnabled",
                            "mbean-enabled flag is turned on. Enabling all the MBeans"));
                spmd.registerAllGmbal();
            } else {
                logger.log(Level.INFO,
                        localStrings.getLocalString("mbeanDisabled",
                            "mbean-enabled flag is turned off. Disabling all the MBeans"));
                spmd.unregisterAllGmbal();
            }
        }
        else if(propName.equals("dtrace-enabled")) {
            probeProviderFactory.dtraceEnabledChanged(enabled);
        }
        else if(propName.equals("monitoring-enabled")) {
            //This we do it so we can (un)expose probes as DTrace
            probeProviderFactory.monitoringEnabledChanged(enabled);

            if(enabled) {
                logger.log(Level.INFO,
                        localStrings.getLocalString("monitoringEnabled",
                            "monitoring-enabled flag is turned on. Enabling all the Probes and Stats"));
                // TODO attach btrace agent dynamically
                enableMonitoringForProbeProviders(true);
                //Lets do the catch up for all the statsProviders (we might have ignored the module level changes earlier) s
                spmd.updateAllStatsProviders();
            } else { // if disabled
                logger.log(Level.INFO,
                        localStrings.getLocalString("monitoringDisabled",
                            "monitoring-enabled flag is turned off. Disabling all the Stats"));
                disableMonitoringForProbeProviders();
                spmd.disableAllStatsProviders();
            }
        }
    }
    
    private void enableMonitoringForProbeProviders(boolean isDiscoverXMLProviders) {
        //Process all ProbeProviders from modules loaded
        discoverProbeProviders();
        //Start listening to any new Modules that are coming in now
        registry.register(this);
        //Don't do this the first time, since we need to wait till the server starts
        // We should try to do this in a seperate thread, as we dont want to get held up in server start
        if (isDiscoverXMLProviders) {
            //Process all XMLProbeProviders from lib/monitor directory
            discoverXMLProviders();
        }
        //Start registering the cached StatsProviders and also those that are coming in new
        setStatsProviderManagerDelegate();
    }

    private void disableMonitoringForProbeProviders() {
        //Cannot do a whole lot here. The providers that are registered will remain registered.
        //Just disable the StatsProviders, so you remove the listening overhead
        registry.unregister(this);
    }

    private boolean ok(String s) {
        return s != null && s.length() > 0;
    }

    private boolean parseLevelsBoolean(String s) {
        if (ok(s) && s.equals("OFF"))
            return false;

        return true;
    }

    private void printd(String pstring) {
        if (logger.isLoggable(Level.FINEST))
            logger.log(Level.FINEST, pstring);
    }

}