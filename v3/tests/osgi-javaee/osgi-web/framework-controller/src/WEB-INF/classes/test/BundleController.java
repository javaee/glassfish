/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package test;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 *
 * @author mohit
 */

@Path("/bundlecontroller")
public class BundleController {

    @Context ServletContext ctx;
    @Context HttpServletResponse resp;
    
    @POST
    public void bundleController(@FormParam("id") String bundleId, @FormParam("bsubmit") String requestType) {
        System.out.println("Controller :" + requestType);
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
                if (bundle != null) {
                    if (requestType.equalsIgnoreCase("Stop") && bundle.getState() == bundle.ACTIVE) {
                        bundle.stop();
                    } else if (requestType.equalsIgnoreCase("Start") &&
                            (bundle.getState() == bundle.RESOLVED || bundle.getState() == bundle.INSTALLED)) {
                        bundle.start();
                    } else if (requestType.equalsIgnoreCase("Uninstall")) {
                        bundle.uninstall();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                resp.sendRedirect(resp.encodeRedirectURL("bundleviewer"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
