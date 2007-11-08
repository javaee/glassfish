/**
 * This servlet forwards the request to another servlet and adds a
 * Shift_JIS encoded parameter.
 */

package test;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletTest extends HttpServlet {

    public void doPost (HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // Forward the request
        RequestDispatcher rd = getServletContext().getRequestDispatcher(
                        "/ServletTest2?japaneseName=\u3068\u4eba\u6587");
        rd.forward(req, res);
    }

    public void doGet (HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {    
        doPost (req,res);
    }
}
