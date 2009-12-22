/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package test;

import javax.servlet.ServletContext;
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

@Path("/bundleinstaller")
public class BundleInstaller {

    @Context ServletContext ctx;

    String returnMessage = "FAIL";

    @POST
    public String installBundle(@FormParam("installUrl") String installUrl) {
        BundleContext bundleContext = (BundleContext) ctx.getAttribute("osgi-bundlecontext");
        try {
            if (installUrl != null) {
                //Examples.
                //String installURL = "file:///space/v3work/v3/tests/osgi-javaee/test1/generated/test1.war";
                //String installURL = "reference:file:/space/v3work/v3/tests/osgi-javaee/test6";
                Bundle bundle = bundleContext.installBundle(installUrl);
                returnMessage = "Bundle deployed with ID : " + bundle.getBundleId();
                bundle.start();
                returnMessage = returnMessage + " Started : PASS";
                //Save current BundleId.
                ctx.setAttribute("bundleId", bundle.getBundleId());
            } else {
                returnMessage = "Please specify Installation Type and Bundle Path : FAIL";
            }
        } catch (Exception ex) {
            returnMessage = "Exception installing the bundle : FAIL";
            ex.printStackTrace();
        }
        return returnMessage;
    }
}
