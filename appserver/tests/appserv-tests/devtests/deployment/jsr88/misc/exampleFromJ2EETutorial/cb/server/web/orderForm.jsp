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

<form action="${pageContext.request.contextPath}/orderForm" method=post>
<center>

<table cellpadding=4 cellspacing=2 border=0>

<tr>
<td colspan=4><fmt:message key="OrderInstructions"/></td>
</tr>

<tr>
<td colspan=4>
&nbsp;</td>
</tr>

<tr bgcolor="#CC9999">
<td align="center" colspan=4><font size=5><b><fmt:message key="OrderForm"/><b></font></td>
</tr>

<tr bgcolor="#CC9999">
<td align=center><B><fmt:message key="Coffee"/></B></td>
<td align=center><B><fmt:message key="Price"/></B></td>
<td align=center><B><fmt:message key="Quantity"/></B></td>
<td align=center><B><fmt:message key="Total"/></B></td>
</tr>

<c:forEach var="sci" items="${sessionScope.cart.items}" >
<tr bgcolor="#CC9999">
<td>${sci.item.coffeeName}</td>
<td align=right>\$${sci.item.retailPricePerPound}</td>
<td align=center><input type="text" name="${sci.item.coffeeName}_pounds" value="${sci.pounds}" size="3"  maxlength="3"></td> 
<td align=right>\$${sci.price}</td>
</tr>
</c:forEach>

<tr>
<td>&nbsp;</td>
<td> 
<a href="${pageContext.request.contextPath}/checkoutForm?firstName=Coffee&lastName=Lover&email=jane@home&areaCode=123&phoneNumber=456-7890&street=99&city=Somewhere&state=CA&zip=95050&CCNumber=1234-2345-5678&CCOption=0"><fmt:message key='Checkout'/></a>
</td>
<td><input type="submit" value="<fmt:message key='Update'/>"></td>
<td align=right>\$${sessionScope.cart.total}</td>
<td>&nbsp;</td>
</tr>

<tr>
<td colspan=5>${requestScope.orderError}</td>
</tr>


</table>
</center>
</form>




