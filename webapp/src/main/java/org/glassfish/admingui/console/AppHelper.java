package org.glassfish.admingui.console;

import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean(eager = true)
@SessionScoped
public class AppHelper implements Serializable {

//    private String navType = "tree";
    private String contentPage = "welcome.xhtml";

    public String getNavType() {
        return "tree";
    }

    /*
    public void setNavType(String layoutType) {
        this.navType = layoutType;
    }

    public SelectItem[] getSupportedNavTypes() {
        return new SelectItem[]{
                    //            new SelectItem("accordion")
                    new SelectItem("tree"), new SelectItem("tabs")
                };
    }
    */

    public String getContentPage() {
        return contentPage;
    }

    public void setContentPage(String contentPage) {
        this.contentPage = contentPage;
    }
}