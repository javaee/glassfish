package org.glassfish.connectors.admin.cli;

import com.sun.enterprise.config.serverbeans.*;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;


import java.util.List;

public class WorkSecurityMapHelper {
    static boolean doesResourceAdapterNameExist(String raName, Resources resources) {
        //check if the resource adapter exists.If it does not then throw an exception.
        boolean doesRAExist = false;
        for (Resource resource : resources.getResources()) {
            if (resource instanceof WorkSecurityMap) {
                if (((WorkSecurityMap) resource).getResourceAdapterName().equals(raName)) {
                    doesRAExist = true;
                    break;
                }
            }
        }
        return doesRAExist;
    }

    static boolean doesMapNameExist(String raName, String mapname, Resources resources) {
        //check if the mapname exists for the given resource adapter name..
        List<WorkSecurityMap> maps = ConnectorsUtil.getWorkSecurityMaps(raName, resources);

        boolean doesMapNameExist = false;
        if (maps != null) {
            for (WorkSecurityMap wsm : maps) {
                String name = wsm.getName();
                if (name.equals(mapname)) {
                    doesMapNameExist = true;
                    break;
                }
            }
        }
        return doesMapNameExist;
    }

    static WorkSecurityMap getSecurityMap(String mapName, String raName, Resources resources) {
        List<WorkSecurityMap> maps = ConnectorsUtil.getWorkSecurityMaps(raName, resources);
        WorkSecurityMap map = null;
        if (maps != null) {
            for (WorkSecurityMap wsm : maps) {
                if (wsm.getName().equals(mapName)) {
                    map = wsm;
                    break;
                }
            }
        }
        return map;
    }

}
