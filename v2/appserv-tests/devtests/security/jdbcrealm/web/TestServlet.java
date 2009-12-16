package com.sun.security.devtests.jdbcrealm.simpleweb;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;
import java.util.Properties;


public class TestServlet extends HttpServlet {

	// Security role references.
	private static final String emp_secrole_ref   = "staff";
	private static final String admin_secrole_ref = "ADMIN";
	private static final String mgr_secrole_ref   = "Manager";

        String user="qwert";
    	PrintWriter out=null;
        Properties props=null;
        HttpServletRequest request=null;
        HttpSession session=null;



        public void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException 
	{
            this.request=request;
            this.session=request.getSession();
            out= response.getWriter();
            out.println("<br>Basic Authentication tests from Servlet: Test1,Test2,Test3 ");
            out.println("<br>Authorization test from Servlet: Test4,Test5-> HttpServletRequest.isUserInRole() authorization from Servlet.");
            
            test1();
            test2();
            test3();
            test4();
            test5();
            cleanup();

	}


        //Tests begin
	public void test1()
	{
                //Check the auth type - request.getAuthType()
                out.println("<br><br>Test1. Postive check for the correct authentication type");
                String authtype=request.getAuthType();
                if ("BASIC".equalsIgnoreCase(authtype) ){
                        out.println("<br>request.getAuthType() test Passed.");
                }else{
                        out.println("<br>request.getAuthType() test Failed!");
                }
                out.println("<br>Info:request.getAuthType() is= "+authtype);
        }
        //Test2
        public void test2(){
                Principal ruser = request.getUserPrincipal();
                out.println("<br><br>Test2. Positive check for the correct principal name");
                if (ruser != null){
                        out.println("<br>request.getUserPrincipal() test Passed.");
                }else{
                        out.println("<br>request.getUserPrincipal() test Failed!");
                }
                out.println("<br>Info:request.getUserPrincipal() is= "+((ruser!=null)?ruser.getName():"null"));

        }
        //Test3 - positive test for checking the user authentication
        //Check the remote user request.getRemoteUser()- get null if not authenticated
        public void test3(){
            out.println("<br><br>Test3. Positive check whether given user authenticated");
                String username=request.getRemoteUser();
                if (user.equals(username)){
                        out.println("<br>request.getRemoteUser() test Passed.");
                }else{
                        out.println("<br>request.getRemoteUser() test Failed!");
                }
                out.println("<br>Info:request.getRemoteUser() is= "+username);
        }
        //Test4 - positive test for checking the user's proper role
        public void test4(){
                out.println("<br><br>Test4.Positive check whether the user is in proper role");
                boolean isInProperRole=request.isUserInRole(emp_secrole_ref);
                if (isInProperRole){
                        out.println("<br>HttpServletRequest.isUserInRole() test Passed.");
                }else{
                        out.println("<br>HttpServletRequest.isUserInRole() test Failed!");
                }
                out.println("<br>Info:request.isUserInRole(\""+emp_secrole_ref+"\") is= "+isInProperRole);
	}

        //Test5 - Negative test for checking the user's proper role
        public void test5(){
                out.println("<br><br>Test5.Negative check whether the current user is any other other role");
                boolean isNotInOtherRole=request.isUserInRole(mgr_secrole_ref);
                if (!isNotInOtherRole){
                        out.println("<br>HttpServletRequest.isUserInRole() test Passed.");
                }else{
                        out.println("<br>HttpServletRequest.isUserInRole() test Failed!");
                }
                out.println("<br>Info:request.isUserInRole(\""+mgr_secrole_ref+"\") is= "+isNotInOtherRole);
	}
        //invalidate the session after running the test
        // Invalidate the session to make the test as independent and always ask for login
        public void cleanup(){
            try{
                session.invalidate();
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
}

