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
 *
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
