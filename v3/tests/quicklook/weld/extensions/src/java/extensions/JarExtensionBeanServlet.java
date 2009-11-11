package extensions;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Check that ExtensionBean is deployed and accessible
 * in JAR file.
 * 
 * @author paulsandoz
 * @author Santiago.PericasGeertsen@sun.com
 */
public class JarExtensionBeanServlet extends ExtensionBeanServlet {

    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        jar.ExtensionBean te = get(jar.ExtensionBean.class, getBM());

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            out.println("<html>");
            out.println("<body>");
            out.println("<h1>Jar Extension bean " + te + "</h1>");
            out.println("<h1>" + te.bbd + " " + te.abd + " " + te.adv + " "
                    + te.pat + " " + te.pit + " " + te.pmb  + "</h1>");
            out.println("</body>");
            out.println("</html>");
        } finally {
            out.close();
        }
    }

}
