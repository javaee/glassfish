/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
 *
 * @author jdlee
 */

@ManagedBean
@ViewScoped
public class WizardBean {
    private int step = 0;
    private List<String> stepLabels = Collections.unmodifiableList(new ArrayList<String>(){{ 
        add("Upload archive");
        add("Pick Virtualization Template");
        add("Configure Database");
        add("Configure Load Balancer");
        add("Configure Elasticity Settings");
    }});
    private List<String> stepPages = Collections.unmodifiableList(new ArrayList<String>(){{ 
        add("/demo/wizard/upload.xhtml");
        add("/demo/wizard/template.xhtml");
        add("/demo/wizard/database.xhtml");
        add("/demo/wizard/lb.xhtml");
        add("/demo/wizard/elasticity.xhtml");
    }});

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public List<String> getStepLabels() {
        return stepLabels;
    }

    public List<String> getStepPages() {
        return stepPages;
    }
    
    public String previous() {
        step--;
        return null;
    }
    
    public String next() {
        step++;
        return null;
    }
}