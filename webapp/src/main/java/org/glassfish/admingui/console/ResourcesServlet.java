/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.admingui.console;

import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author jdlee
 */
@WebServlet(name="ResourcesServlet",
        urlPatterns={"*.jpg", "*.png", "*.gif"})
public class ResourcesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        resp.setHeader("Content-Type", req.getServletContext().getMimeType(path));
        InputStream is = req.getServletContext().getResourceAsStream(path);
        if (is == null) {
            is = getClass().getResourceAsStream(path);
        }
        
        if (is != null) {
            byte[] buffer = new byte[8192];
            int read = is.read(buffer);
            while (read > 0) {
                resp.getOutputStream().write(buffer, 0, read);
                read = is.read(buffer);
            }
            
            is.close();
        }
//        super.doGet(req, resp);
    }
}
