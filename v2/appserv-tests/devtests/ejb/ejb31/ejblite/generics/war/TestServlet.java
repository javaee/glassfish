package com.sun.s1as.devtests.ejb.generics;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.annotation.*;
import javax.ejb.*;

@javax.servlet.annotation.WebServlet(urlPatterns = "/TestServlet")
public class TestServlet extends HttpServlet {
    @EJB
    private TestBean testBean;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        testBean.doSomething(null);
        testBean.doSomething2(null);
        testBean.doSomething3();
        testBean.doSomething4(null);
        out.println(testBean.hello());
        out.println("Successfully called methods on " + testBean); 
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
}
        
