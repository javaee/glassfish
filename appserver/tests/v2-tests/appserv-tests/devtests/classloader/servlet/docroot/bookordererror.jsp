<%--
 * Copyright (c) 2002 Sun Microsystems, Inc.  All rights reserved.  U.S. 
 * Government Rights - Commercial software.  Government users are subject 
 * to the Sun Microsystems, Inc. standard license agreement and 
 * applicable provisions of the FAR and its supplements.  Use is subject 
 * to license terms.  
 * 
 * This distribution may include materials developed by third parties. 
 * Sun, Sun Microsystems, the Sun logo, Java and J2EE are trademarks 
 * or registered trademarks of Sun Microsystems, Inc. in the U.S. and 
 * other countries.  
 * 
 * Copyright (c) 2002 Sun Microsystems, Inc. Tous droits reserves.
 * 
 * Droits du gouvernement americain, utilisateurs gouvernementaux - logiciel
 * commercial. Les utilisateurs gouvernementaux sont soumis au contrat de 
 * licence standard de Sun Microsystems, Inc., ainsi qu'aux dispositions 
 * en vigueur de la FAR (Federal Acquisition Regulations) et des 
 * supplements a celles-ci.  Distribue par des licences qui en 
 * restreignent l'utilisation.
 * 
 * Cette distribution peut comprendre des composants developpes par des 
 * tierces parties. Sun, Sun Microsystems, le logo Sun, Java et J2EE 
 * sont des marques de fabrique ou des marques deposees de Sun 
 * Microsystems, Inc. aux Etats-Unis et dans d'autres pays.
 *'
--%>
<%@ page errorPage="/template/errorpage.jsp" %>
<html>
<head>
<title>Order Error</title>
</head>
<body  bgcolor="#FFFFFF">
<center> 
<hr>
<br>&nbsp;
<h1> 
<font size="+3" color="#CC0066">Duke's </font> 
<img src="template/duke.books.gif">
<font size="+3" color="black">Bookstore</font> 
</h1> 
<br>&nbsp;
<hr>
</center>
<h3><fmt:message key="OrderError"/></h3><br>


<c:remove var="cart" scope="session" />
<c:url var="url" value="/bookstore" />
<strong><a href="${url}"><fmt:message key="ContinueShopping"/></a>&nbsp;&nbsp;&nbsp;</strong>  

<center><em>Copyright &copy; 2003 Sun Microsystems, Inc. </em></center>
</body>
</html>
