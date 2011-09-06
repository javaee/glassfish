/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.console;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ValueChangeEvent;
import org.apache.myfaces.trinidad.model.UploadedFile;
import org.glassfish.admingui.console.event.DragDropEvent;
import org.glassfish.admingui.console.rest.RestUtil;
import org.glassfish.admingui.console.util.FileUtil;
import org.glassfish.admingui.console.util.GuiUtil;
import org.glassfish.admingui.console.util.TargetUtil;


/**
 *
 * @author anilam
 */
@ManagedBean
@SessionScoped
public class UploadBean {
    private UploadedFile _file;
    private File tmpFile;
    private String appName;
    private String desc;
    private String contextRoot;

    private String database;
    private String eeTemplate;
    private String loadBalancer;

    private List<String> eeTemplates = new ArrayList<String>() {{
        add("GlassFish Small");
        add("GlassFish Medium");
        add("GlassFish Large");
    }};

    private List<String> databases = new ArrayList<String>() {{
        add("Derby");
        add("MySQL");
        add("Oracle");
    }};

    private List<String> loadBalancers = new ArrayList<String>() {{
        add("foo");
        add("bar");
        add("baz");
    }};

    public void fileUploaded(ValueChangeEvent event) {
        System.out.println("------ in filUploaded");
        UploadedFile file = (UploadedFile) event.getNewValue();
        try{
            if (file != null) {
                //FacesContext context = FacesContext.getCurrentInstance();
                //FacesMessage message = new FacesMessage( "Successfully uploaded file " + file.getFilename() + " (" + file.getLength() + " bytes)");
                //context.addMessage(event.getComponent().getClientId(context), message);
                // Here's where we could call file.getInputStream()
                System.out.println("getFilename=" + file.getFilename());
                System.out.println("getLength=" + file.getLength());
                System.out.println("getContentType=" + file.getContentType());
                File tf = FileUtil.inputStreamToFile(file.getInputStream(), file.getFilename());
                tmpFile = tf;
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }


    public UploadedFile getFile() {
        return _file;
    }

    public void setFile(UploadedFile file) {
        _file = file;
    }

    public String doDeploy(){
        System.out.println("=================== doDeploy()");
        Map payload = new HashMap();
        payload.put("id", this.tmpFile.getAbsolutePath());
        if (!GuiUtil.isEmpty(this.appName)){
            payload.put("name", this.appName);
        }
        if (!GuiUtil.isEmpty(this.contextRoot)){
            payload.put("contextroot", this.contextRoot);
        }
        /*
        if (!GuiUtil.isEmpty(this.desc)){
            payload.put("name", this.desc);
        }
         *
         */
        try {
            RestUtil.restRequest("http://localhost:4848/management/domain/applications/application", payload, "post", null, null, false, true);
            return "/demo/listApplications";
        } catch (Exception ex) {
            return null;
        }
    }

    public String getAppName(){
        return appName;
    }
    public void setAppName(String nm){
        this.appName = nm;
    }

    public String getDescription(){
        return desc;
    }
    public void setDescription(String description){
        this.desc = description;
    }

    public String getContextRoot(){
        return contextRoot;
    }
    public void setContextRoot(String ctxRoot){
        this.contextRoot = ctxRoot;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getEeTemplate() {
        return eeTemplate;
    }

    public void setEeTemplate(String eeTemplate) {
        this.eeTemplate = eeTemplate;
    }

    public String getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(String loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public List<String> getEeTemplates() {
        return eeTemplates;
    }

    public List<String> getDatabases() {
        return databases;
    }

    public List<String> getLoadBalancers() {
        return loadBalancers;
    }

    public String databaseDropListener(DragDropEvent event) {
        String value = (String) event.getData();
        if (database != null) {
            databases.add(database);
        }
        database = value;
        databases.remove(database);
        Collections.sort(databases);

        return null;
    }

    public String loadBalancerDropListener(DragDropEvent event) {
        String value = (String) event.getData();
        if (loadBalancer != null) {
            loadBalancers.add(loadBalancer);
        }
        loadBalancer = value;
        loadBalancers.remove(loadBalancer);
        Collections.sort(loadBalancers);

        return null;
    }

    public String eeTemplateDropListener(DragDropEvent event) {
        String value = (String) event.getData();
        if (eeTemplate != null) {
            eeTemplates.add(eeTemplate);
        }
        eeTemplate = value;
        eeTemplates.remove(eeTemplate);
        Collections.sort(eeTemplates);

        return null;
    }
}
