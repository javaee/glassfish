/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.console;

import com.sun.faces.application.NavigationHandlerImpl;
import javax.faces.context.FacesContext;

/**
 *
 * @author jdlee
 */
public class NavigationHandler extends NavigationHandlerImpl {

    @Override
    public void handleNavigation(FacesContext context, String fromAction, String outcome) {
        if ((fromAction != null) && (outcome != null)) {
            AppHelper appHelper = (AppHelper)context.getExternalContext().getSessionMap().get("appHelper");
            appHelper.setContentPage(outcome + ".xhtml");
            fromAction = outcome = "/index.xhtml";
        }
        super.handleNavigation(context, fromAction, outcome);
    }

}
