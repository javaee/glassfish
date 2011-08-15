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
        add("Step 1");
        add("Step 2");
        add("Step 3");
        add("Step 4");
        add("Step 5");
    }});
    private List<String> stepPages = Collections.unmodifiableList(new ArrayList<String>(){{ 
        add("/demo/wizard/step1.xhtml");
        add("/demo/wizard/step2.xhtml");
        add("/demo/wizard/step3.xhtml");
        add("/demo/wizard/step4.xhtml");
        add("/demo/wizard/step5.xhtml");
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