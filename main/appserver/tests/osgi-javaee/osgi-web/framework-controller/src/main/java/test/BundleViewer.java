/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

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
