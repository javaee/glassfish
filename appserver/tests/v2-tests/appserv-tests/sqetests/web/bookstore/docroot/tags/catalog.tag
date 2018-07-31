<%--

    DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

    Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.

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

