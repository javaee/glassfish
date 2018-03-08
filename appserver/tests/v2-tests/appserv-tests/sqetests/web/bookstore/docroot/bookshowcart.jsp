<%--

    DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

    Copyright (c) 2002-2018 Oracle and/or its affiliates. All rights reserved.

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

