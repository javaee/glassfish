/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.console;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import org.apache.myfaces.trinidad.model.UploadedFile;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author anilam
 */
@ManagedBean
@SessionScoped
public class UploadBean {
    private UploadedFile _file;

    public void fileUploaded(ValueChangeEvent event) {
        System.out.println("in filUploade");
        UploadedFile file = (UploadedFile) event.getNewValue();
        if (file != null) {
            FacesContext context = FacesContext.getCurrentInstance();
            //FacesMessage message = new FacesMessage( "Successfully uploaded file " + file.getFilename() + " (" + file.getLength() + " bytes)");
            //context.addMessage(event.getComponent().getClientId(context), message);
            // Here's where we could call file.getInputStream()
            System.out.println("getFilename=" + file.getFilename());
            System.out.println("getLength=" + file.getLength());
            System.out.println("getContentType=" + file.getContentType());
        }
    }

    public UploadedFile getFile() {
        System.out.println("------------- in getFile");
        return _file;
    }

    public void setFile(UploadedFile file) {
        System.out.println("----- in setFile");
        _file = file;
    }

    public String doUpload() {
        System.out.println("---- in doUpload");
        UploadedFile file = getFile();
        // ... and process it in some way
        return "/demo/domain";
    }
}
