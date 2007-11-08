/**
 * This servlet, which is the target of the forwarded request, retrieves
 * the Shift-JIS encoded query parameter that was added by the origin servlet.
 */
package test;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.text.Collator;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletTest2 extends HttpServlet {
    
    Collator japCollator = Collator.getInstance(Locale.JAPANESE);
    
    public void doPost (HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // Retrieve the query param added by the origin servlet
        String japName = req.getParameter("japaneseName");

        PrintWriter out = res.getWriter();

        if (japCollator.compare(japName,"\u3068\u4eba\u6587") == 0){
            out.println("MultiByteValue::PASS");
        } else {
            out.println("MultiByteValue::FAIL");
        }

        out.close();
    }

    public void doGet (HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {    
        doPost (req,res);
    }
}
