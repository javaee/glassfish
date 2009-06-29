/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admingui.common.util;

import com.sun.appserv.management.config.ApplicationConfig;
import com.sun.appserv.management.config.EngineConfig;
import com.sun.appserv.management.config.ModuleConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.glassfish.admin.amx.core.AMXProxy;

/**
 *
 * @author anilam
 */
public class AppUtil {

    public static List getAllSniffers(AMXProxy app){
        Map<String, AMXProxy> modules = app.childrenMap("module");
        List sniffersList = new ArrayList();
        for(AMXProxy oneModule: modules.values()){
            List<String> sniffers = getSnifferListOfModule(oneModule);
            for(String oneSniffer : sniffers){
                if (! sniffersList.contains(oneSniffer)){
                    sniffersList.add(oneSniffer);
                }
            }
        }
        return sniffersList;
    }


    public static List<String> getSnifferListOfModule(AMXProxy module){
        List sniffersList = new ArrayList();
        Map<String, AMXProxy> engines = module.childrenMap("engine");
        for (String oneSniffer: engines.keySet()){
            String sniffer = oneSniffer;
            if (sniffersHide.contains(sniffer) )
                continue;
            sniffersList.add(sniffer);
        }
        Collections.sort(sniffersList);
        return sniffersList;
    }

    //return the list of specified modules.
    public static List getAllModules(String type, List exist){

        Map<String, ApplicationConfig> appsConfig = AMXRoot.getInstance().getApplicationsConfig().getApplicationConfigMap();
        Map<String, ApplicationConfig> sysAppsConfig = AMXRoot.getInstance().getSystamApplicationsConfig().getApplicationConfigMap();
        
        List result = (exist == null) ? new ArrayList() :exist;
        List<ApplicationConfig> allappConfig = new ArrayList();
        for(ApplicationConfig sysa : sysAppsConfig.values()){
            allappConfig.add(sysa);
        }
        for(ApplicationConfig sysa : appsConfig.values()){
            allappConfig.add(sysa);
        }

        for (ApplicationConfig appConfig : allappConfig) {
            boolean ear = isComposite(appConfig);
            Map <String, ModuleConfig> mConfigs = appConfig.getModuleConfigMap();
            for (ModuleConfig mf : mConfigs.values()){
                boolean found=false;
                Map<String, EngineConfig> eConfigs = mf.getEngineConfigMap();
                for(EngineConfig ec : eConfigs.values()){
                    String sniffer = ec.getSniffer();
                    if(sniffer.equals(type)){
                        found=true;
                        if (ear){
                            result.add(appConfig.getName()+"#" + mf.getName());
                        }else{
                            result.add(appConfig.getName());
                        }
                    }
                    if (found==true) break;
                }
            }
        }
        return result;
    }


    public static boolean isComposite(ApplicationConfig appConfig){
        String composite = AMXUtil.getPropertyValue(appConfig, AppUtil.PROP_IS_COMPOSITE);
        if (GuiUtil.isEmpty(composite) || composite.equals("false")){
            return false;
        }else
            return true;
    }

    public static boolean isApplicationEnabled(AMXProxy application){
        Map<String, Object> attrs = application.attributesMap();
        String enabled = (String) attrs.get("Enabled");
        String appName = (String) attrs.get("Name");
        if ( "true".equals(enabled)){
           String objName = "v3:pp=/domain/servers/server[server],type=application-ref,name="+appName;
           AMXProxy appRef = V3AMX.objectNameToProxy(objName);
           String appRefEnabled = (String) appRef.attributesMap().get("Enabled");
           return ("true".equals(appRefEnabled));
        }
        return false;
    }
    
    public static boolean isApplicationEnabled(String appObjectName){
        return isApplicationEnabled(V3AMX.objectNameToProxy(appObjectName));
    }


    static final public List sniffersHide = new ArrayList();
    static {
        sniffersHide.add("security");
    }
    static public final String PROP_IS_COMPOSITE = "isComposite";


}
