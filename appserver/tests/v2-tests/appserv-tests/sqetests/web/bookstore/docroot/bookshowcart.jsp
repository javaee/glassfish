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


<c:if test="${param.Clear == 'clear'}">
    <font color="red" size="+2"><strong> 
    <fmt:message key="CartCleared"/> 
    </strong><br>&nbsp;<br></font>
</c:if>


<c:if test="${param.Remove != '0'}">
    <c:set var="bid" value="${param.Remove}"/>
    <jsp:setProperty name="bookDB" property="bookId" value="${bid}" />
    <c:set var="removedBook" value="${bookDB.bookDetails}" />
    <font color="red" size="+2"><fmt:message key="CartRemoved"/><em>${removedBook.title}</em>.
 
    <br>&nbsp;<br> 
    </font>
</c:if>

<c:if test="${sessionScope.cart.numberOfItems > 0}"> 
    <font size="+2"><fmt:message key="CartContents"/>
    ${sessionScope.cart.numberOfItems}
    <c:if test="${sessionScope.cart.numberOfItems == 1}"> 
      <fmt:message key="CartItem"/>.
    </c:if>
    <c:if test="${sessionScope.cart.numberOfItems > 1}"> 
    <fmt:message key="CartItems"/>.
    </c:if>
    </font><br>&nbsp;

    <table summary="layout"> 
    <tr> 
    <th align=left><fmt:message key="ItemQuantity"/></TH> 
    <th align=left><fmt:message key="ItemTitle"/></TH> 
    <th align=left><fmt:message key="ItemPrice"/></TH> 

    </tr>

    <c:forEach var="item" items="${sessionScope.cart.items}">
    		<c:set var="book" value="${item.item}" />

        <tr> 
        <td align="right" bgcolor="#ffffff"> 
        ${item.quantity}
        </td> 

        <td bgcolor="#ffffaa"> 
        <c:url var="url" value="/bookdetails" >
          <c:param name="bookId" value="${book.bookId}" />
      		<c:param name="Clear" value="0" />
        </c:url>
        <strong><a href="${url}">${book.title}</a></strong> 
        </td> 

        <td bgcolor="#ffffaa" align="right"> 
        <fmt:formatNumber value="${book.price}" type="currency"/>&nbsp;</td>  
        </td> 

        <td bgcolor="#ffffaa"> 
        <c:url var="url" value="/bookshowcart" >
          <c:param name="Remove" value="${book.bookId}" />
        </c:url>
        <strong><a href="${url}"><fmt:message key="RemoveItem"/></a></strong> 
        </td></tr>

    </c:forEach>

    <tr><td colspan="5" bgcolor="#ffffff"> 
    <br></td></tr> 

    <tr> 
    <td colspan="2" align="right" bgcolor="#ffffff"> 
    <fmt:message key="Subtotal"/></td> 
    <td bgcolor="#ffffaa" align="right"> 
    <fmt:formatNumber value="${sessionScope.cart.total}" type="currency"/>    </td>
    <td><br></td>
    </tr></table> 

    <p>&nbsp;<p>
    <c:url var="url" value="/bookcatalog" >
      <c:param name="Add" value="" />
    </c:url>
    <strong><a href="${url}"><fmt:message key="ContinueShopping"/></a>&nbsp;&nbsp;&nbsp;  
    <c:url var="url" value="/bookcashier" />
    <a href="${url}"><fmt:message key="Checkout"/></a>&nbsp;&nbsp;&nbsp; 
    <c:url var="url" value="/bookshowcart" >
      <c:param name="Clear" value="clear" />
      <c:param name="Remove" value="0" />
    </c:url>
    <a href="${url}"><fmt:message key="ClearCart"/></a></strong>
</c:if>

<c:if test="${sessionScope.cart.numberOfItems <= 0}"> 
    <font size="+2"><fmt:message key="CartEmpty"/></font> 
    <br>&nbsp;<br> 
    <c:url var="url" value="/bookcatalog" >
      <c:param name="Add" value="" />
    </c:url>
    <strong><a href="${url}"><fmt:message key="Catalog"/></a></strong>
</c:if>

</body>
</html>

