/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.console;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
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
        return services;
    }

    public List<Map>getEnvironments(){
        List envs = new ArrayList();
        
        Map m1 = new HashMap();
        m1.put("name", "mycluster");
        m1.put("instanceCount", "2");
        envs.add(m1);
        return envs;
    }
}