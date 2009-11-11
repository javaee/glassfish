package extensions;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Check that TestExtensionBean is deployed and accessible,
 * and that all event are being observed.
 * 
 * @author paulsandoz
 * @author Santiago.PericasGeertsen@sun.com
 */
public class ExtensionBeanServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ExtensionBean te = get(ExtensionBean.class, getBM());

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            out.println("<html>");
            out.println("<body>");
            out.println("<h1>Extension bean " + te + "</h1>");
            out.println("<h1>" + te.bbd + " " + te.abd + " " + te.adv + " "
                    + te.pat + " " + te.pit + " " + te.pmb + "</h1>");
            out.println("</body>");
            out.println("</html>");
        } finally {
            out.close();
        }
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

    public <T> T get(Class<T> c, BeanManager bm) {
        Set<Bean<?>> bs = bm.getBeans(c);
        if (bs.isEmpty()) {
            System.out.println("CANNOT GET SET OF BEANS FOR " + c);
            return null;
        }
        Bean<?> b = bm.resolve(bs);

        CreationalContext<?> cc = bm.createCreationalContext(b);

        return c.cast(bm.getReference(b, c, cc));
    }

    public BeanManager getBM() {
        try {
            return (BeanManager) new InitialContext().lookup("java:comp/BeanManager");
        } catch (NamingException ex) {
            Logger.getLogger(ExtensionBeanServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
