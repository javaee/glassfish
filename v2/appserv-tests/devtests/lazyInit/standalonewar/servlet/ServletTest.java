package test;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.xml.sax.InputSource;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;

// jaxp 1.0.1 imports
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.naming.*;
import javax.sql.*;
import java.sql.*;

public class ServletTest extends HttpServlet implements HttpSessionListener {

    private ServletContext context;
    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        System.out.println("[Servlet.init]");        
        context = config.getServletContext();
        System.out.println("[Servlet.init] " + context.getMajorVersion());
        
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("[Servlet.doGet]");
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {        
        System.out.println("[Servlet.doPost]");
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        request.getSession().setAttribute("FILTER-REQUEST", request.getSession().getAttribute("FILTER"));
        request.getSession().setAttribute("FILTER", "FAIL");        
        	
        RequestDispatcher rd = context.getRequestDispatcher("/ServletTest2");
        rd.forward(request, response);       
    }

    public void sessionCreated(javax.servlet.http.HttpSessionEvent httpSessionEvent) {
        System.out.println("[Servlet.sessionCreated]");
    }
    
    public void sessionDestroyed(javax.servlet.http.HttpSessionEvent httpSessionEvent) {
        System.out.println("[Servlet.sessionDestroyed]");
        System.out.println("Attributes: " + httpSessionEvent.getSession().getAttribute("test"));
    }
}



