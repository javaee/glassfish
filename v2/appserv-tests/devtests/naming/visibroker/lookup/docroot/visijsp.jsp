<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="javax.naming.*,javax.rmi.*,java.util.*,HelloApp.*,org.omg.CosNaming.*,org.omg.CosNaming.NamingContextPackage.*" %>
<%--
The taglib directive below imports the JSTL library. If you uncomment it,
you must also add the JSTL library to the project. The Add Library... action
on Libraries node in Projects view can be used to add the JSTL 1.1 library.
--%>
<%--
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
--%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>

    <h1>JSP Page</h1>
    
    This is a test!!!
    
    <%
    
  

try{
     	out.println("Setting up JNDI provider...\n");
        	
        out.println("new InitialContext()...\n");

	Properties p1 = new Properties();
        p1.put( "org.omg.CORBA.ORBInitialHost", "localhost" );
        p1.put( "org.omg.CORBA.ORBInitialPort", "2510" );

        p1.put("org.omg.CORBA.ORBClass","com.inprise.vbroker.orb.ORB");
        p1.put("org.omg.CORBA.ORBSingletonClass","com.inprise.vbroker.orb.ORBSingleton");
      
	org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init( new String[]{"",""}, p1 );  

     

	// get the root naming context
	org.omg.CORBA.Object objRef = 
	  orb.resolve_initial_references("NameService");
	// Use NamingContextExt instead of NamingContext. This is 
	// part of the Interoperable naming Service.  
	NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
	  
	// resolve the Object Reference in Naming
	String name = "Hello";
	Hello hello = HelloHelper.narrow(ncRef.resolve_str(name));
        
        out.println(hello.sayHello());
        out.println("Done");
            
        hello.shutdown();
    }
    catch( Exception e )
    {
        System.out.println("ERROR");

        e.printStackTrace( System.out );
        java.io.PrintWriter pw = new java.io.PrintWriter( out );
        e.printStackTrace( pw );
        pw.flush();
    }
    
    %>
    <%--
    This example uses JSTL, uncomment the taglib directive above.
    To test, display the page like this: index.jsp?sayHello=true&name=Murphy
    --%>
    <%--
    <c:if test="${param.sayHello}">
        <!-- Let's welcome the user ${param.name} -->
        Hello ${param.name}!
    </c:if>
    --%>
    
    </body>
</html>
