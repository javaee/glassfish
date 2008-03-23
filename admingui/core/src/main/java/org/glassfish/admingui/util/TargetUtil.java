/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

/*
 * TargetUtil.java
 *
 * Created on Nov 04, 2006, 2:43 PM
 */

package org.glassfish.admingui.util;

import java.util.*;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.config.DeployedItemRefConfig;
import com.sun.appserv.management.config.DeployedItemRefConfigCR;
import com.sun.appserv.management.config.ClusterConfig;
import com.sun.appserv.management.config.ClusteredServerConfig;
import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.ResourceRefConfig;
import com.sun.appserv.management.config.ResourceRefConfigCR;
import com.sun.appserv.management.config.Enabled;
import com.sun.appserv.management.config.StandaloneServerConfig;
import com.sun.appserv.management.config.ServerRefConfig;
import com.sun.enterprise.util.SystemPropertyConstants; 


public class TargetUtil {

    private TargetUtil() {} //dummy constructor, all static methods.
    
    /************ Get and Set enable status of resource ****************/
    
    /*
     * Determine the real status of a resource, look at both resource object itself and the resource reference 
     */
    static public boolean isResourceEnabled(Enabled resourceObject, String target)
    {
	boolean master = resourceObject.getEnabled();
	if (!master) return false;		// no need to look at ref if master is off.
	String name = ((AMX)resourceObject).getName();
	ResourceRefConfig ref = getResourceRef(name, target);
	if (ref == null)
	    return master;
	return ref.getEnabled();
    }

    /*
     * Returns the list of list of Virtual Server in the given target
     */
    static public Set getVirtualServers(String targetName)
    {
		ConfigConfig config = AMXRoot.getInstance().getConfigByInstanceOrClusterName(targetName);
		Set<String> vsNames = config.getHTTPServiceConfig().getVirtualServerConfigMap().keySet();
		return vsNames;
    }

    /*
     * Sets the comma separated list of associated Virtual Server in a given target
     */
    static public void setVirtualServers(String appName, String targetName, String virtualServers)
    {
		DeployedItemRefConfig appRef = getDeployedItemRefObject(appName, targetName);
		appRef.setVirtualServers(virtualServers);
    }

    /*
     * Returns the list of associated Virtual Server in the given target
     */
    static public String getAssociatedVS(String appName, String targetName)
    {
		DeployedItemRefConfig appRef = getDeployedItemRefObject(appName, targetName);
		String vs = appRef.getVirtualServers();
		return vs;
    }

	/*


    /*
     * Set the enabled status of a resource.  Use the resource-ref to control the status.
     */
    static public void setResourceEnabled(Enabled resource, String target, boolean enabledFlag)
    {

        if (enabledFlag)
	    resource.setEnabled(true);
        //We use the ref to control enable status
        String name = ((AMX)resource).getName();
	List<Map<String, ResourceRefConfig>> allResourceRefs = getAllResourceRefConfig(target);
        for(Map<String, ResourceRefConfig> oneResourceMap : allResourceRefs){
            ResourceRefConfig ref = oneResourceMap.get(name);
            ref.setEnabled(enabledFlag);
        }
    }


	/*
	 * Given the name of a resource and the target, return the resource-ref object
	 */
	static public ResourceRefConfig getResourceRef(String name, String target){
	    StandaloneServerConfig server = AMXRoot.getInstance().getDomainConfig().getStandaloneServerConfigMap().get(target);
	    ResourceRefConfig ref = null;
            if (server == null){
		ClusterConfig cluster = AMXRoot.getInstance().getDomainConfig().getClusterConfigMap().get(target);
		ref = (cluster == null) ? null :  cluster.getResourceRefConfigMap().get(name);
	    }else{
		ref = server.getResourceRefConfigMap().get(name);
	    }
            return ref;
	}
        
        
        /*
	 * Given the name of a  target, return the resource-ref object of this target.
         * If the target is a cluster, all the resource-ref object of that clusteredInstance will also be included.
	 */
	static public List<Map<String, ResourceRefConfig>> getAllResourceRefConfig(String target){
            List<Map<String, ResourceRefConfig>> allResourceRefs = new ArrayList();
            
            if (isCluster(target)){
                ClusterConfig cluster = AMXRoot.getInstance().getDomainConfig().getClusterConfigMap().get(target);
                allResourceRefs.add(cluster.getResourceRefConfigMap());
                //For every server in this cluster, we have to change the source ref status also.
                Map<String,ClusteredServerConfig> clusteredServerConfigMap = cluster.getClusteredServerConfigMap();
                Collection <ClusteredServerConfig> csConfigs = clusteredServerConfigMap.values();
                for(ClusteredServerConfig csConfig : csConfigs){
                    allResourceRefs.add(csConfig.getResourceRefConfigMap());
                }
            }else{
                StandaloneServerConfig server = AMXRoot.getInstance().getDomainConfig().getStandaloneServerConfigMap().get(target);
                allResourceRefs.add(server.getResourceRefConfigMap());
            }
            return allResourceRefs;
        }
                
        
        /*
	 * Given the name of a resource and the target, create the resource-ref object
	 */
	static public void createResourceRef(String name, String targetName){
            ResourceRefConfigCR target = getResoruceRefConfigCR(targetName);
            if (target != null)
                target.createResourceRefConfig(name);
	}
        
        /*
	 * Given the name of a resource and the target, create the resource-ref object with the enabled flag.
	 */
	static public void createResourceRef(String name, String targetName, boolean enabled){
            ResourceRefConfigCR target = getResoruceRefConfigCR(targetName);
            if (target != null)
                target.createResourceRefConfig(name, enabled);
	}

	/* Given two collections x, and y removes y from x, and returns a new
	*  collection. The parameters are expected to be List for now, ideally
	*  it should have been a Set. Leaving it as List, bcz our handler methods
	*  deal with List to add the options in addRemove component.
	*/

	static public Set<String> XMinusY(Collection<String> X, Collection<String> Y) {
		Set<String> s = Collections.synchronizedSet(new HashSet<String>(X));
		if(Y != null && Y.size() != 0) {
			for(String str : Y) {
				s.remove(str);
			}
		}
		return s;
	}
        
        /*
	 * Given the name of a resource and the target, delete the resource-ref object
	 */
	static public void removeResourceRef(String name, String targetName){
            ResourceRefConfigCR target = getResoruceRefConfigCR(targetName);
            if (target != null)
                target.removeResourceRefConfig(name);
	}
        
        
        static public ResourceRefConfigCR getResoruceRefConfigCR(String targetName){
            ResourceRefConfigCR target = AMXRoot.getInstance().getDomainConfig().getStandaloneServerConfigMap().get(targetName);
            if (target == null){
		target = AMXRoot.getInstance().getDomainConfig().getClusterConfigMap().get(targetName);
                if (target == null){
                    //TODO Log Error, cannot find such target
                    return null;
                }
            }
            return target;
            
        }
        
    /************ Get and Set enable status of application ****************/
        
    /*
     * Determine the real status of an application, look at both resource object itself and the resource reference 
     */
    static public boolean isApplicationEnabled(Enabled app, String target)
    {
	return isApplicationEnabled(app, target, false);
    }
    
    static public boolean isApplicationEnabled(Enabled app, String target, Boolean forLB)
    {
	boolean master = app.getEnabled();
	if (!master) return false;		// no need to look at ref if master is off.
	String name = ((AMX)app).getName();
        DeployedItemRefConfig ref = getDeployedItemRefObject(name, target);
        if (forLB){
            if (ref != null)
                return ref.getLBEnabled();
            else
                return false;
        }
	if (ref == null)
	    return master;
	return ref.getEnabled();
    }


    /*
     * Set the enabled status of an application.  Use the application-ref to control the status.
     */
    static public void setApplicationEnabled(Enabled app, String target, boolean enabledFlag){
        setApplicationEnabled(app, target, enabledFlag, false);
    }
    
    static public void setApplicationEnabled(Enabled app, String target, boolean enabledFlag, boolean forLB)
    {
	//We use the ref to control enable status
	String name = ((AMX)app).getName();
	DeployedItemRefConfig ref = getDeployedItemRefObject(name, target);
        if (forLB){
            if (ref != null){
                    ref.setLBEnabled(enabledFlag);
            }
            return;
        }
        
	if (enabledFlag)
	    app.setEnabled(true);
	if (ref != null)
	    ref.setEnabled((Boolean)enabledFlag);
    }

	/*
	 * Given the name of an application and the target, return the application-ref object
	 */
	static public DeployedItemRefConfig getDeployedItemRefObject(String name, String target){
	    StandaloneServerConfig server = AMXRoot.getInstance().getDomainConfig().getStandaloneServerConfigMap().get(target);
	    DeployedItemRefConfig ref = null;
            if (server == null){
		ClusterConfig cluster = AMXRoot.getInstance().getDomainConfig().getClusterConfigMap().get(target);
		ref = (cluster == null) ? null :  cluster.getDeployedItemRefConfigMap().get(name);
	    }else{
		ref = server.getDeployedItemRefConfigMap().get(name);
	    }
	    return ref;
	}
        
         /*
	 * Given the name of an application  and the target, create the application-ref object
	 */
	static public void createDeployedItemRefObject(String name, String targetName){
	    DeployedItemRefConfigCR target = AMXRoot.getInstance().getDomainConfig().getStandaloneServerConfigMap().get(targetName);
            if (target == null){
		target = AMXRoot.getInstance().getDomainConfig().getClusterConfigMap().get(targetName);
                if (target == null){
                    //TODO log error
                    return;
                }
            }
            target.createDeployedItemRefConfig(name);
	}
         /*
	 * Given the name of an application  and the target, deletes the application-ref object
	 */
	static public void removeDeployedItemRefObject(String name, String targetName){
	    DeployedItemRefConfigCR target = AMXRoot.getInstance().getDomainConfig().getStandaloneServerConfigMap().get(targetName);
            if (target == null){
		target = AMXRoot.getInstance().getDomainConfig().getClusterConfigMap().get(targetName);
                if (target == null){
                    //TODO log error
                    return;
                }
            }
            target.removeDeployedItemRefConfig(name);
	}
     
     /*
      * return the enabled status suitable to display at the top level applications and resources list page.
      */
     public static String getEnabledStatus(Enabled app, boolean isApp){
        if (AMXRoot.getInstance().isEE()){
            List<String> targetList = getDeployedTargets(((AMX)app), isApp);
            if(targetList.size() == 0) 
                return convertStatusSummary(APP_NO_TARGET);
            int total = 0;
            for(String target : targetList){
                if (isApp){
                    if(isApplicationEnabled(app, target))
                        total++;
                }else{
                    if(isResourceEnabled(app, target))
                        total++;
                }
            }
            if(total == targetList.size())  return convertStatusSummary(APP_ALL_ENABLED);
            if(total == 0 ) return convertStatusSummary(APP_ALL_DISABLED);
            return convertStatusSummary(total);
        }
        boolean status = (isApp) ? TargetUtil.isApplicationEnabled(app, "server") :
                                   TargetUtil.isResourceEnabled(app, "server");
        
        return Boolean.toString(status);
    }
    
    public static List<String> getDeployedTargets(AMX app, boolean isApp){
        String appName = app.getName();
        return getDeployedTargets(appName, isApp);
    }
        
    public static List<String> getDeployedTargets(String appName, boolean isApp){
        List<String> targetList = new ArrayList();
        String objectName = (isApp) ?  "com.sun.appserv:type=applications,category=config" : "com.sun.appserv:type=resources,category=config";
        if(! AMXRoot.getInstance().isEE()){
            targetList.add("server");
        }else{
            String[] params = new String[] {appName};
            String[] types = new String[] {"java.lang.String"};
            //TODO-V3
            //ObjectName[] refs = (ObjectName[]) JMXUtil.invoke(objectName , "listReferencees", params, types);
            //for(int i=0;  i<refs.length; i++){
            //    targetList.add(refs[i].getKeyProperty("name"));
            //}
        }
        return targetList;
    }
    
    public static String convertStatusSummary(int statusSummary){
        String disp = null;
        if (statusSummary == APP_ALL_ENABLED){
            disp = GuiUtil.getMessage("deploy.allEnabled") ;
        }else
        if (statusSummary == APP_ALL_DISABLED){
            disp =GuiUtil.getMessage("deploy.allDisabled");
        }else
        if (statusSummary == APP_NO_TARGET){
            disp =GuiUtil.getMessage("deploy.noTarget");
        }else{
            disp = GuiUtil.getMessage("deploy.someEnabled", new String[]{""+statusSummary}) ;
        }
        return disp;
    }
    
     
    public static String getNumberLBInstancesByTarget(String cluster){
        Map<String,ServerRefConfig> serverRefMap = AMXRoot.getInstance().getDomainConfig().getClusterConfigMap().get(cluster).getServerRefConfigMap();
        Collection <ServerRefConfig> refs = serverRefMap.values();
        int totalCount = serverRefMap.size();
        if (totalCount == 0) return GuiUtil.getMessage("loadBalancer.noInstance");
        int totalEnabled = 0;
        for(ServerRefConfig ref : refs){
            if (ref.getLBEnabled())
                totalEnabled++;
        }
        return GuiUtil.getMessage("loadBalancer.numLBInstance", new Object[]{""+totalEnabled, ""+totalCount});
    }
    
    
    public static String getDomainRoot() {
        String domainRoot = System.getProperty(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY);
        return domainRoot;
    }
    
    public static boolean isCluster(String name){
        return (AMXRoot.getInstance().getDomainConfig().getClusterConfigMap().get(name) == null) ? false: true;
    }
    
    public static final int APP_NO_TARGET = -2;
    public static final int APP_ALL_ENABLED = -1;
    public static final int APP_ALL_DISABLED = 0;
}

