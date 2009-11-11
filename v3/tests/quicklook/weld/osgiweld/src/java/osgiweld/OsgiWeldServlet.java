package osgiweld;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Test integrity of OSGi Weld Module.
 * 
 * @author Santiago.PericasGeertsen@sun.com
 */
public class OsgiWeldServlet extends HttpServlet {

    private static List<Attributes.Name> ATTRS =
            Arrays.asList(new Attributes.Name("Export-Package"),
                          new Attributes.Name("Import-Package"),
                          new Attributes.Name("Private-Package"));

    protected void processRequest(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException
    {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String result = "OK";
        try {
            String gfhome = System.getProperty("com.sun.aas.instanceRoot");
            // Soft test, ignore if can't find module
            if (gfhome != null) {
                String jarFile = gfhome + File.separator + ".."
                        + File.separator + ".." + File.separator
                        + "modules" + File.separator + "weld-osgi-bundle.jar";
                System.out.println("Weld Osgi module = " + jarFile);
                JarFile jar = new JarFile(jarFile);
                Manifest manifest = jar.getManifest();
                Set<Object> keys = manifest.getMainAttributes().keySet();
                // Make sure all attrs are there
                if (!keys.containsAll(ATTRS)) {
                    result = "ERROR";
                }
            } else {
                System.out.println("Unable to find Weld module");
            }
        } catch (Exception e) {
            result = "ERROR";
        }

        out.println("<html>");
        out.println("<body>");
        out.println("<h1>" + result + "</h1>");
        out.println("</body>");
        out.println("</html>");
        out.close();
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
