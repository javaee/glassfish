import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SetHeadersServlet extends HttpServlet {

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws ServletException, IOException {

        // The Cache-Control header often takes multiple values
        response.setHeader("Cache-Control","no-cache");

        // We have to use addHeader( ) to prevent setHeader( ) from replacing
        // the existing value.
        response.addHeader("Cache-Control","no-store");

        // We now have multiple values for the Cache-Control header.
        // If we try to replace the existing values with setHeader( ), only
        // one is replaced, contrary to the requirements of SRV.5.2
        // (second paragraph)
        response.setHeader("Cache-Control","public");
    }

}
