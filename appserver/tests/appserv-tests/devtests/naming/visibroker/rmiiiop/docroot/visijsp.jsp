<%--

    DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

    Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.

    The contents of this file are subject to the terms of either the GNU
    General Public License Version 2 only ("GPL") or the Common Development
    and Distribution License("CDDL") (collectively, the "License").  You
    may not use this file except in compliance with the License.  You can
    obtain a copy of the License at
    https://oss.oracle.com/licenses/CDDL+GPL-1.1
    or LICENSE.txt.  See the License for the specific
    language governing permissions and limitations under the License.

    When distributing the software, include this License Header Notice in each
    file and include the License file at LICENSE.txt.

    GPL Classpath Exception:
    Oracle designates this particular file as subject to the "Classpath"
    exception as provided by Oracle in the GPL Version 2 section of the License
    file that accompanied this code.

    Modifications:
    If applicable, add the following below the License Header, with the fields
    enclosed by brackets [] replaced by your own identifying information:
    "Portions Copyright [year] [name of copyright owner]"

    Contributor(s):
    If you wish your version of this file to be governed by only the CDDL or
    only the GPL Version 2, indicate your decision by adding "[Contributor]
    elects to include this software in this distribution under the [CDDL or GPL
    Version 2] license."  If you don't indicate a single choice of license, a
    recipient has the option to distribute your version of this file under
    either the CDDL, the GPL Version 2 or to extend the choice of license to
    its licensees as provided above.  However, if you add GPL Version 2 code
    and therefore, elected the GPL Version 2 license, then the option applies
    only if the new code is made subject to such option by the copyright
    holder.

--%>

<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="javax.naming.*,javax.rmi.*,java.util.*,org.omg.CosNaming.*,org.omg.CosNaming.NamingContextPackage.*" %>
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
    
  
    try {
      	// Initialize the ORB.
	Properties p1 = new Properties();

        p1.put("org.omg.CORBA.ORBClass","com.inprise.vbroker.orb.ORB");
        p1.put("org.omg.CORBA.ORBSingletonClass","com.inprise.vbroker.orb.ORBSingleton");
      
	org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init( new String[]{"",""}, p1 );  
    
	// Get the manager Id
        byte[] managerId = "RMIBankManager".getBytes();
        // Locate an account manager. Give the full POA name and the servant ID.
        Bank.AccountManager manager = 
	Bank.AccountManagerHelper.bind(orb, "/rmi_bank_poa", managerId);
        // Use any number of argument pairs to indicate name,balance of accounts to create
	String[] args = new String[0];
        if (args.length == 0 || args.length % 2 != 0) {
            args = new String[2];
	    args[0] = "Jack B. Quick";
            args[1] = "123.23";
        }
        int i = 0;
        while (i < args.length) {
            String name = args[i++];
            float balance;
            try {
              balance = new Float(args[i++]).floatValue();
            } catch (NumberFormatException n) {
              balance = 0;
            }
            Bank.AccountData data = new Bank.AccountData(name, balance);
            Bank.Account account = manager.create(data);
            out.println
              ("Created account for " + name + " with opening balance of $" + balance);
        } 
    
        java.util.Hashtable accounts = manager.getAccounts();
    
        for (java.util.Enumeration e = accounts.elements(); e.hasMoreElements();) {
            Bank.Account account = Bank.AccountHelper.narrow((org.omg.CORBA.Object)e.nextElement());
            String name = account.name();
            float balance = account.getBalance();
            out.println("Current balance in " + name + "'s account is $" + balance);
            out.println("Crediting $10 to " + name + "'s account.");
            account.setBalance(balance + (float)10.0);
            balance = account.getBalance();
            out.println("New balance in " + name + "'s account is $" + balance);
        }
    } catch (java.rmi.RemoteException e) {
      out.println(e);
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
