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
 *
--%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ attribute name="bookDB" required="true" type="database.BookDB" %>
<%@ attribute name="color" required="true" %>

<%@ attribute name="normalPrice" fragment="true" %>
<%@ attribute name="onSale" fragment="true" %>
<%@ variable name-given="price" %>
<%@ variable name-given="salePrice" %>

<center>
<table summary="layout">
<c:forEach var="book" begin="0" items="${bookDB.books}">
  <tr>
  <c:set var="bookId" value="${book.bookId}" />
  <td bgcolor="${color}"> 
      <c:url var="url" value="/bookdetails" >
        <c:param name="bookId" value="${bookId}" />
      </c:url>
      <a href="${url}"><strong>${book.title}&nbsp;</strong></a></td> 
  <td bgcolor="${color}" rowspan=2>
    

  <c:set var="salePrice" value="${book.price * .85}" />
  <c:set var="price" value="${book.price}" />

    <c:choose>
      <c:when test="${book.onSale}" >
        <jsp:invoke fragment="onSale" />
      </c:when>
      <c:otherwise>
        <jsp:invoke fragment="normalPrice" />
      </c:otherwise>
    </c:choose>
    
  &nbsp;</td> 

  <td bgcolor="${color}" rowspan=2> 
  <c:url var="url" value="/bookcatalog" >
    <c:param name="Add" value="${bookId}" />
  </c:url> 
  <p><strong><a href="${url}">&nbsp;<fmt:message key="CartAdd"/>&nbsp;</a></td></tr> 

  <tr> 
  <td bgcolor="#ffffff"> 
  &nbsp;&nbsp;<fmt:message key="By"/> <em>${book.firstName}&nbsp;${book.surname}</em></td></tr>
</c:forEach>

</table>
</center>

