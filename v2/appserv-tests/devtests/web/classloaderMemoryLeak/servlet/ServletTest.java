package test;

import javax.servlet.http.*;
import javax.servlet.*;
import java.io.IOException;
import java.io.PrintWriter;


public class ServletTest extends HttpServlet {
     private TestValue testValue;
     private static long curMemory = 0L;
     public void init(ServletConfig servletConfig) throws ServletException {
         super.init(servletConfig);
         testValue = TestHolder.TEST_VALUE;
            
     }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        curMemory = usedMemory();
        
        out.write("USAGE:" + curMemory);
        out.flush();
        out.close();
    }


    /**
     * Predict the current memory 
     */
    private static long usedMemory(){
        System.out.println("gc()");
        Runtime.getRuntime().gc();
        Runtime.getRuntime().gc();

        return Runtime.getRuntime().totalMemory () 
            - Runtime.getRuntime().freeMemory ();
    }    
}



