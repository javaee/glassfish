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
        Properties p2 = new Properties();
        p2.put( Context.INITIAL_CONTEXT_FACTORY, 
	        "com.sun.jndi.cosnaming.CNCtxFactory" );
Properties p1 = new Properties();
        p1.put( "org.omg.CORBA.ORBInitialHost", "localhost" );
        p1.put( "org.omg.CORBA.ORBInitialPort", "1345" );
  p1.put( "com.sun.CORBA.ORBServerPort","33701");
       p1.put( "com.sun.CORBA.POA.ORBPersistentServerPort","33701");
        p1.put("com.sun.CORBA.transport.ORBListenSocket", "");
//SSL:33702,SSL_MUTUALAUTH:33703");
       
org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init( new String[]{"",""}, p1 );

p2.put("java.naming.corba.orb", orb);
                
        InitialContext ic = new InitialContext( p2);
        
        out.println("Done");
        out.println("About to do lookup...");
              
        java.lang.Object o = ic.lookup("Hello" );
       out.println("Completed lookup!!");
           
        HelloApp.Hello hello = (HelloApp.Hello) PortableRemoteObject.narrow( o, HelloApp.Hello.class );

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
