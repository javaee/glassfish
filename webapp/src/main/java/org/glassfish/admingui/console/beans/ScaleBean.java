/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admingui.console.beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.bean.*;
import org.glassfish.admingui.console.rest.RestUtil;


@ManagedBean(name="scaleBean")
@ViewScoped
public class ScaleBean implements Serializable {

    private String minScale = "1";
    private String maxScale = "2";
    private String endpoint = "";
    private String threshold = "80";
    private String sampleInterval = "5";
    private String enabled = "true";

    @ManagedProperty(value="#{environmentBean.envName}")
    private String envName;


    public ScaleBean() {
    }

    public String getEnvName(){
        return envName;
    }

    public void setEnvName(String en){
        envName = en;
        endpoint = REST_URL + "/elastic-services/elasticservice/" + envName;
    }

    public String getMinScale(){
        String elasticEndpoint = REST_URL + "/elastic-services/elasticservice/" + envName;
        if (RestUtil.doesProxyExist(elasticEndpoint)){
            minScale = (String) RestUtil.getAttributesMap(elasticEndpoint).get("min");
        }
        return minScale;
    }

    public void setMinScale(String mm) {
        minScale = mm;
    }

    public String getMaxScale(){
        if (RestUtil.doesProxyExist(endpoint)){
            maxScale = (String) RestUtil.getAttributesMap(endpoint).get("max");
        }
        return maxScale;
    }

    public void setMaxScale(String mm) {
        maxScale = mm;
    }

    public String getSampleInterval(){
        try{
            String alertEndpoint = endpoint + "/alerts/alert/";
            List<String> alertList = RestUtil.getChildNameList(alertEndpoint);
            sampleInterval = (String) RestUtil.getAttributesMap(alertEndpoint + "/" + alertList.get(0)).get("sampleInterval");
        }catch(Exception ex){
        }
        return sampleInterval;
    }

    public void setSampleInterval(String sampleI) {
        sampleInterval = sampleI;
    }

    public String getThreshold(){
        try{
            String alertEndpoint = endpoint + "/alerts/alert/";
            List<String> alertList = RestUtil.getChildNameList(alertEndpoint);
            Map payload = new HashMap();
            payload.put("service" , envName);
            payload.put("alert", alertList.get(0));
            //Map alertAttrs = RestUtil.restRequest(endpoint+"/describe-memory-alert", null, "GET", null, null, true);
            //threshold =  (String) alertAttrs.get("threshold");
        }catch(Exception ex){
        }
        return threshold;
    }

    public void setThreshold(String th) {
        threshold = th;
    }

    public String getEnabled(){
        try{
            String alertEndpoint = endpoint + "/alerts/alert/";
            List<String> alertList = RestUtil.getChildNameList(alertEndpoint);
            Map payload = new HashMap();
            payload.put("service" , envName);
            payload.put("alert", alertList.get(0));
            //Map alertAttrs = RestUtil.restRequest(endpoint+"/describe-memory-alert", null, "GET", null, null, true);
            //enabled =  (String) alertAttrs.get("enabled");
        }catch(Exception ex){
        }
        return enabled;
    }

    public void setEnabled(String en) {
        enabled = en;
    }

    public String saveScaling(){
        try{
            Map payload = new HashMap();
            payload.put("min", minScale);
            payload.put("max", maxScale);
            RestUtil.restRequest(endpoint, payload, "POST", null, null, true);

//            Comment out for now until create-memory-alert is ready from backend.
//            String alertEndpoint = endpoint + "/alerts/alert/";
//            List<String> alertList = RestUtil.getChildNameList(alertEndpoint);
//            Map payload2 = new HashMap();
//            payload2.put("service", envName);
//            payload2.put("threshold", threshold);
//            payload2.put("sample-interval", sampleInterval);
//            payload2.put("alert", alertList.get(0));
//            payload2.put("enabled", enabled);
//            RestUtil.restRequest(alertEndpoint+"/create-memory-alert", payload2, "POST", null, null, true);
        }catch(Exception ex){
        }
        return null;
    }

    private final static String REST_URL = "http://localhost:4848/management/domain";

}
