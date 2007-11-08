<%--
 * Copyright (c) 2003 Sun Microsystems, Inc.  All rights reserved.  U.S. 
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
 * Copyright (c) 2003 Sun Microsystems, Inc. Tous droits reserves.
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




