/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.console;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.glassfish.admingui.console.util.CommandUtil;

/**
 *
 * @author anilam
 */

@ManagedBean
@SessionScoped
public class Overview  {

    public List<Map> getServices() {
        List services =  CommandUtil.listServices(null, null, null);

        //In the overview page, we don't want to show clusterInstance type.
        Iterator iter = services.iterator();
        while (iter.hasNext()) {
            Map oneEntry = (Map)iter.next();
            if ("ClusterInstance".equals(oneEntry.get("serverType"))) {
                iter.remove();
            }
            if ("Cluster".equals(oneEntry.get("serverType"))){
                oneEntry.put("serverType", "JavaEE");
            }
        }
        return services;
    }

    public List<Map>getEnvironments(){
        return CommandUtil.getEnvironments();
    }
}