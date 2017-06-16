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
--%>

<jsp:useBean id="bookDB" class="database.BookDB" scope="page" >
  <jsp:setProperty name="bookDB" property="database" value="${bookDBAO}" />
</jsp:useBean>

<c:if test="${!empty param.bookId}">
  <c:set var="bid" value="${param.bookId}"/>
  <jsp:setProperty name="bookDB" property="bookId" value="${bid}" />
  <c:set var="book" value="${bookDB.bookDetails}" />
    <h2>${book.title}</h2>
    &nbsp;<fmt:message key="By"/> <em>${book.firstName}&nbsp;${book.surname}</em>&nbsp;&nbsp;
    (${book.year})<br> &nbsp; <br>
    <h4><fmt:message key="Critics"/></h4>
    <blockquote>${book.description}</blockquote>
    <c:set var="price" value="${book.price}" />		
    <c:if test="${book.onSale}" >
      <c:set var="price" value="${book.price * .85}" />	
    </c:if>
    <h4><fmt:message key="ItemPrice"/>: <fmt:formatNumber value="${price}" type="currency"/></h4>
    <c:url var="url" value="/bookcatalog" >
      <c:param name="Add" value="${bid}" />
    </c:url> 
    <p><strong><a href="${url}"><fmt:message key="CartAdd"/></a>&nbsp; &nbsp; &nbsp;
</c:if>


<c:url var="url" value="/bookcatalog" >
  <c:param name="Add" value="" />
</c:url>
<a href="${url}"><fmt:message key="ContinueShopping"/></a></p></strong>






