<%--

    DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

    Copyright (c) 2003-2017 Oracle and/or its affiliates. All rights reserved.

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

<c:remove var="cart" scope="session"/>
<center>
<table cellpadding=4 cellspacing=2 border=0>
<tr>
<td align=center colspan=3><fmt:message key="OrderConfirmed"/></td>
</tr>
<tr>
<td colspan=3>&nbsp;</td>
</tr>
<tr bgcolor="#CC9999">
<td rowspan=2 align=center><b><fmt:message key="ShipDate"/></b>
<td colspan=2 align=center><b><fmt:message key="Items"/></b>
</tr>
<tr bgcolor="#CC9999">
<td><b><fmt:message key="Coffee"/></b></td>
<td><b><fmt:message key="Pounds"/></b></td>
</tr>
<c:forEach var="oc" items="${requestScope.checkoutFormBean.orderConfirmations.items}" >
  <tr bgcolor="#CC9999">
  <td rowspan=${fn:length(oc.orderBean.lineItems)} align=center><fmt:formatDate value="${oc.confirmationBean.shippingDate.time}" type="date" dateStyle="full" /></td>
  <c:forEach var="item" items="${oc.orderBean.lineItems}" >
    <td bgcolor="#CC9999">${item.coffeeName}</td>
    <td bgcolor="#CC9999" align=right>${item.pounds}</td>
  </tr>
  </c:forEach>
</c:forEach>
<tr>
<td colspan=3>&nbsp;</td>
</tr>
<tr>
<td align=center colspan=3><a href="${pageContext.request.contextPath}/orderForm"><fmt:message key="ContinueShopping"/></a>
<tr>
<td colspan=3>&nbsp;</td>
</tr>
</table>
</center>
