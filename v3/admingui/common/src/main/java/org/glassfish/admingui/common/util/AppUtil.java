/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admingui.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.management.ObjectName;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.deployment.client.DFDeploymentProperties;

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

    public static boolean isApplicationEnabled(AMXProxy application){
        Map<String, Object> attrs = application.attributesMap();
        String enabled = (String) attrs.get("Enabled");
        String appName = (String) attrs.get("Name");
        if ( "true".equals(enabled)){
           AMXProxy appRef = V3AMX.getInstance().getApplicationRef("server", appName);
           String appRefEnabled = (String) appRef.attributesMap().get("Enabled");
           return ("true".equals(appRefEnabled));
        }
        return false;
    }
    
    public static boolean isApplicationEnabled(String appObjectName){
        return isApplicationEnabled(V3AMX.objectNameToProxy(appObjectName));
    }

    public static boolean isLifecycle(ObjectName objName){
        AMXProxy amx = V3AMX.objectNameToProxy(objName.toString());
        String isLife = (String) V3AMX.getPropValue(amx, DFDeploymentProperties.IS_LIFECYCLE);
        return (isLife == null) ? false : true;
    }

    static final public List sniffersHide = new ArrayList();
    static {
        sniffersHide.add("security");
    }

    static public final String PROP_IS_COMPOSITE = "isComposite";

}
