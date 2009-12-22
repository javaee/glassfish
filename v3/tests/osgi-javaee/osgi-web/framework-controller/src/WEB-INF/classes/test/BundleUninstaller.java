/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package test;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 *
 * @author mohit
 */

@Path("/bundleuninstaller")
public class BundleUninstaller {

    @Context ServletContext ctx;
    String returnMessage = "FAIL";

    @POST
    public String uninstallBundle(@FormParam("bundleId") String bundleId) {
        long uninstallId = 0;
        BundleContext bundleContext = (BundleContext) ctx.getAttribute("osgi-bundlecontext");

        if(bundleId == null) {
            uninstallId = (Long) ctx.getAttribute("bundleId");
        } else {
            uninstallId = Long.parseLong(bundleId);
        }
        try {
            if(uninstallId != 0) {
                Bundle bundle = bundleContext.getBundle(uninstallId);
                bundle.stop();
                returnMessage = "Stopped Bundle : " + bundle.getSymbolicName();
                bundle.uninstall();
                returnMessage = returnMessage + " Uninstalled Bundle : PASS";
                //Unset current BundleId.
                ctx.setAttribute("bundleId", 0);
            } else {
                returnMessage = "Please specify the bundleId to be uninstalled : FAIL";
            }
        } catch (Exception ex) {
            returnMessage = "Exception while uninstalling bundle : FAIL";
            ex.printStackTrace();
        }
        return returnMessage;
    }

}
