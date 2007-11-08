package test;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class ServletTest extends HttpServlet{

    private String status = "DelegateTest::FAIL";
    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        System.out.println("[Servlet.init]");        
        
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("[Servlet.doGet]");
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {        
        System.out.println("[Servlet.doPost]");
        
        response.setContentType("text/html");
        
        DelegateTest delegate = null;
        try{
            Class clazz = Class.forName("test.DelegateTest");
            delegate = (DelegateTest)clazz.newInstance();
            clazz.newInstance();
        } catch (Exception ex){
            status = "DelegateTest::FAIL";
            ex.printStackTrace();
        }

        if (delegate != null){
            try{
                System.out.println("Delegate: " + delegate.getChildName());       
                status = "DelegateTest::PASS";
            } catch (Exception ex){
                status = "DelegateTest::FAIL";
                ex.printStackTrace();
            }
        }
        PrintWriter out = response.getWriter();
        out.println(status);

        try{
            Class clazz = Class.forName("javax.sql.rowset.BaseRowSet");

            if (clazz == null){
                status = "OverridableJavax::FAIL";
            } else {
                status = "OverridableJavax::PASS";
            }
            
        } catch (Exception ex){
            status = "OverridableJavax::FAIL";
            ex.printStackTrace();
        }
        out.println(status);
        
    }

}



