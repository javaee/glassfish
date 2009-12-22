/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package test;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 *
 * @author mohit
 */
@Path("/bundleviewer")
public class BundleViewer {

    @Context ServletContext ctx;
    @Context HttpServletResponse resp;
    
    @GET
    public void getBundles() throws IOException {
        PrintWriter out = null;
        String [] bgcolors = {"#B5EAAA", "#C3FDB8"};
        try {
            BundleContext bundleContext = (BundleContext) ctx.getAttribute("osgi-bundlecontext");
            Bundle[] bundles = bundleContext.getBundles();
            resp.setContentType("text/html;charset=UTF-8");
            out = resp.getWriter();
            out.println("<form action=\"bundlecontroller\" method=\"POST\">");
            out.println("<table border=\"0\">");
            out.println("<tr bgcolor=\"#FFF8C6\">");
            out.println("<td></td>" +
                    "<td font color=\"black\" style=\"font-weight:bold\">BUNDLEID</td>" +
                    "<td font color=\"black\" style=\"font-weight:bold\">STATE</td>" +
                    "<td font color=\"black\" style=\"font-weight:bold\">BUNDLE SYMBOLIC NAME</td></tr>");
            
            try {
                for(int i=0; i<bundles.length; i++) {
                    out.println("<tr bgcolor=\""+ bgcolors[i%2] +"\">");
                    out.println("<td><input type=\"radio\" name=\"id\" value=\"" + bundles[i].getBundleId() +"\" /></td>");
                    out.println("<td>"+bundles[i].getBundleId() +"</td>");
                    out.println("<td>"+getState(bundles[i].getState()) +"</td>");
                    out.println("<td>"+bundles[i].getSymbolicName() +"</td>");
                    out.println("</tr>");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            out.println("<input type=\"submit\" name=\"bsubmit\" value=\"Start\">");
            out.println("<input type=\"submit\" name=\"bsubmit\" value=\"Stop\">");
            out.println("<input type=\"submit\" name=\"bsubmit\" value=\"Uninstall\">");
            out.println("</table></form>");
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            out.close();
        }
    }

    private String getState(int state) {
        String stateAsString = "";
        switch(state) {
            case Bundle.ACTIVE : stateAsString = "Active"; break;
            case Bundle.INSTALLED : stateAsString ="Installed"; break;
            case Bundle.RESOLVED : stateAsString = "Resolved"; break;
        }
        return stateAsString;
    }
}
