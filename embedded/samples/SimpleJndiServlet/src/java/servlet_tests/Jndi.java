/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package servlet_tests;

import javax.naming.spi.NamingManager;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;
import javax.naming.NamingException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.directory.InitialDirContext;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 *	A Test for JNDI
 */

/**
 *
 * @author Jian (James) Cai
 * @version 0.1
 * 5/19/2001
 */

public class Jndi extends HttpServlet {

    public void init() throws ServletException {
        Context ctx = null;
        try {
            ctx = new InitialContext();
            //ctx.lookup("java:/comp");
            System.out.println("initialized successfully in JndiNUCServlet init()");
        } catch (NamingException e) {
            e.printStackTrace();
            System.out.println("Cannot create context in init()"+ e);
            throw new ServletException(e);
        }
    }

    public void destroy() {
        Context ctx = null;
        try {
            ctx = new InitialContext();
            //ctx.lookup("java:/comp");
            System.out.println("initialized successfully in JndiNUCServlet destroy()");
        } catch (NamingException e) {
            e.printStackTrace();
            System.out.println("Cannot create context in destroy()"+ e);
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {


        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

        out.println("QQQQQQQQQQQQQQQQQQQQQQQQQQQ");
        //NamingManager.getInitialContext();
        StringBuffer sb = new StringBuffer();
        boolean ok = true;
        Object value = null;

        // Look up the initial context provided by our servlet container
        Context initContext = null;
        try {
            initContext = new InitialContext();
            out.println(initContext);
        } catch (NamingException e) {
            ok = false;
        }

        Context envContext = null;
        try {
            if (ok) {
                value = initContext.lookup("java:comp/env");
                envContext = (Context) value;
                if (envContext == null) {
                    out.println("  Missing envContext.");
                    ok = false;
                }
            }
        } catch (ClassCastException e) {
            out.println("  envContext class is ");
            out.println(value.getClass().getName());
            ok = false;
        } catch (NamingException e) {

            out.println("  Cannot create envContext.");
            ok = false;
        }

        // Attempt to add a new binding to our environment context
        try {
            if (ok) {
                envContext.bind("newEntry", "New Value");
                out.println("  Allowed bind().");
                ok=false;
                value = envContext.lookup("newEntry");
                if (value != null)
                {
                    ok= false;
                    out.println("  Allowed lookup() of added entry.");
                }
            }
        } catch (Throwable e) {
            out.println("Add binding - "+ e);
        }

        // Attempt to change the value of an existing binding
        try {
            if (ok) {
                envContext.rebind("stringEntry", "Changed Value");
                out.println("  Allowed rebind().");
                ok= false;
                value = envContext.lookup("stringEntry");
                if ((value != null) &&
                    (value instanceof String) &&
                    "Changed Value".equals((String) value))
                {
                    ok=false;
                    out.println("  Allowed lookup() of changed entry.");
                }
            }
        } catch (Throwable e) {
            out.println("Change binding - "+ e);
        }

        // Attempt to delete an existing binding
        try {
            if (ok) {
                envContext.unbind("byteEntry");
                out.println("  Allowed unbind().");
                ok=false;
                value = envContext.lookup("byteEntry");
                if (value == null)
                {
                    out.println("  Allowed unbind of deleted entry.");
                    ok = false;
                }
            }
        } catch (Throwable e) {
            out.println("Delete binding - "+ e);
        }

        // Report our ultimate success or failure
        if (ok)
            out.println("JndiNUC Test PASSED");
        else {
            out.print("JndiNUC Test FAILED -");
            out.println(sb);
        }

        // Add wrapper messages as required

    }

}

