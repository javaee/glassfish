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

    @EJB
    private TypeVariableBean<Integer> typeVariableBean;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        testBean.doSomething(null);
        testBean.doSomething2(null);
        testBean.doSomething3();
        testBean.doSomething4(new Object());
        testBean.doSomething4("");
        testBean.doSomething4(Integer.valueOf(1));
        testBean.doSomething5(null);
        testBean.doSomething6(null);
        out.println(testBean.hello());
        out.println("Successfully called methods on " + testBean); 

        out.println(typeVariableBean.hello("some text"));
        out.println(typeVariableBean.hello(10));
        out.println("Injected TypeVariableBean: " + typeVariableBean.toString());
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
        
