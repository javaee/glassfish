/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.console;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ValueChangeEvent;
import org.apache.myfaces.trinidad.model.UploadedFile;
import org.glassfish.admingui.console.event.DragDropEvent;


/**
 *
 * @author anilam
 */
@ManagedBean
@SessionScoped
public class UploadBean {
    private UploadedFile _file;
    private String appName;
    private String desc;
    private String contextRoot;

    private String database;
    private String eeTemplate;
    private String loadBalancer;

    private List<String> javaEeTemplates = new ArrayList<String>() {{
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
    }

    public UploadedFile getFile() {
        return _file;
    }

    public void setFile(UploadedFile file) {
        _file = file;
    }

    public String doDeploy(){
       return "/demo/listApplications";
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

    public List<String> getJavaEeTemplates() {
        return javaEeTemplates;
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
}