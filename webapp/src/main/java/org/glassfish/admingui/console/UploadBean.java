/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.console;


import java.io.File;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import org.apache.myfaces.trinidad.model.UploadedFile;

import org.glassfish.admingui.console.util.FileUtil;

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
        System.out.println("------------- in getFile " + _file);
        return _file;
    }

    public void setFile(UploadedFile file) {
        System.out.println("----- in setFile");
        _file = file;
    }


    public String doDeploy(){
       System.out.println("----------- doDeploy");
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

}
