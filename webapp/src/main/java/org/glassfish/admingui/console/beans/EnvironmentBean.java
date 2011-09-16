/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admingui.console.beans;

import java.io.Serializable;
import javax.faces.bean.*;
import javax.faces.context.FacesContext;
import java.util.*;
import org.glassfish.admingui.console.rest.RestUtil;


@ManagedBean(name="environmentBean")
@ViewScoped
public class EnvironmentBean implements Serializable {

    private String envName;
    //public List<String> URLs;
    private List<Map> applications = null;
    private List<Map> instances = null;
    private List<String> instanceNames = null;
    private String dummy = "";


    public EnvironmentBean() {
        Map requestMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        envName = (String)requestMap.get("envName");
    }

    public String getEnvName(){
        return envName;
    }

    public EnvironmentBean(String envName) {
        this.envName = envName;
    }

    public List<Map> getApplications() {
        if (applications == null){
            applications = new ArrayList();
            try {
                List<String> appsNameList = RestUtil.getChildNameList(REST_URL + "/clusters/cluster/" + envName + "/application-ref");
                for (String oneApp : appsNameList) {
                    String contextRoot = (String) RestUtil.getAttributesMap(REST_URL + "/applications/application/" + oneApp).get("contextRoot");
                    Map aMap = new HashMap();
                    aMap.put("appName", oneApp);
                    aMap.put("contextRoot", contextRoot);
                    applications.add(aMap);
                }
            } catch (Exception ex) {
            }
        }
        return applications;
    }

    //For instances, we want this to get call everytime the page is loaded.  Since this is in a tab set, so, even when declared @viewScope
    //this bean will still be cached.  However, since this is for generating table data,  this method will get called multipe times when the
    //page is loaded.  We want to avoid that too.
    
    public List<Map> getInstances() {
        if (instances == null){
            try {
                instances = new ArrayList();
                Map attrs = new HashMap();
                attrs.put("whichtarget", envName);
                List<Map> iList =  RestUtil.getListFromREST(REST_URL+"/list-instances", attrs, "instanceList");
                for(Map oneI : iList){
                    if("running".equals(((String)oneI.get("status")).toLowerCase())){
                        oneI.put("statusImage", "/images/running_small.gif");
                    }else{
                        oneI.put("statusImage", "/images/not-running_small.png");
                    }
                    instances.add(oneI);
                }
            } catch (Exception ex) {
            }
        }
        return instances;
    }

    public List<String> getInstanceNames(){
        if (instanceNames == null){
            instanceNames = new ArrayList();
            List<Map> imap = this.getInstances();
            for(Map oneInstance : imap){
                instanceNames.add((String)oneInstance.get("name"));
            }
        }
        return instanceNames;
    }


    public String getDummy(){
        instances = null;
        instanceNames = null;
        return dummy;
    }

    private final static String REST_URL = "http://localhost:4848/management/domain";

}
