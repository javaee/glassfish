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


<form action="${pageContext.request.contextPath}/checkoutAck" method=post>
<center>
<table cellpadding=4 cellspacing=2 border=0>
<tr>
<td colspan=2>
<p><fmt:message key="YourOrder"/> \$${sessionScope.cart.total}. <fmt:message key="CheckoutInstructions"/></td>
</tr>
<tr>
<td colspan=2>&nbsp;</td>
</tr>

<tr bgcolor="#CC9999">
<td align="center" colspan=2><font size=5><b><fmt:message key="CheckoutForm"/><b></font></td>
</tr>

<tr bgcolor="#CC9999">
<td valign=top> 
<B><fmt:message key="FirstName"/></B> 
<br>
<input type="text" name="firstName" value="${requestScope.checkoutFormBean.firstName}" size=15 maxlength=20>
<br><font size=2 color=black>${requestScope.checkoutFormBean.errors.firstName}</font>
</td>
<td  valign=top>
<B><fmt:message key="LastName"/></B>
<br>
<input type="text" name="lastName" value="${requestScope.checkoutFormBean.lastName}" size=15 maxlength=20>
<br><font size=2 color=black>${requestScope.checkoutFormBean.errors.lastName}</font>
</td>
</tr>

<tr bgcolor="#CC9999">
<td valign=top>
<B><fmt:message key="EMail"/></B> 
<br>
<input type="text" name="email" value="${requestScope.checkoutFormBean.email}" size=25  maxlength=125>
<br><font size=2 color=black>${requestScope.checkoutFormBean.errors.email}</font>
</td>
<td  valign=top>
<B><fmt:message key="PhoneNumber"/></B>
<br> 
<input type="text" name="areaCode" value="${requestScope.checkoutFormBean.areaCode}" size=3  maxlength=3>
<input type="text" name="phoneNumber" value="${requestScope.checkoutFormBean.phoneNumber}" size=8  maxlength=8>
<br><font size=2 color=black>${requestScope.checkoutFormBean.errors.phoneNumber}</font>
</td>
</tr>


<tr bgcolor="#CC9999">
<td valign=top>
<B><fmt:message key="Street"/></B> 
<br>
<input type="text" name="street" size=25 value="${requestScope.checkoutFormBean.street}"  maxlength=25>
<br><font size=2 color=black>${requestScope.checkoutFormBean.errors.street}</font>
</td>
<td  valign=top>
<B><fmt:message key="City"/></B>
<br>
<input type="text" name="city" size=25 value="${requestScope.checkoutFormBean.city}"  maxlength=25>
<br><font size=2 color=black>${requestScope.checkoutFormBean.errors.city}</font>
</td>
<br>
</tr>

<tr bgcolor="#CC9999">
<td valign=top>
<B><fmt:message key="State"/></B> 
<br>
<input type="text" name="state" size=2 value="${requestScope.checkoutFormBean.state}"  maxlength=2>
<br><font size=2 color=black>${requestScope.checkoutFormBean.errors.state}</font>
</td>
<td  valign=top>
<B><fmt:message key="Zip"/></B> 
<br>
<input type="text" name="zip" value="${requestScope.checkoutFormBean.zip}" size=5  maxlength=5>
<br><font size=2 color=black>${requestScope.checkoutFormBean.errors.zip}</font>
</td>
<br>
</tr>


<tr bgcolor="#CC9999">
<td valign=top>
<B><fmt:message key="CCOption"/></B> 
<br>
  <select name=CCOption> 
    <option value=0 <c:if test="${CCOption == 0}"> selected</c:if> >VISA</option>  	   
    <option value=1 <c:if test="${CCOption == 1}"> selected</c:if> >MasterCard</option>	   
    <option value=2 <c:if test="${CCOption == 2}"> selected</c:if> >American Express</option>
  </select>
</td>
<td  valign=top>
<B><fmt:message key="CCNumber"/></B> 
<br>
<input type="text" name="CCNumber" value="${requestScope.checkoutFormBean.CCNumber}" size=16  maxlength=16>
<br><font size=2 color=black>${requestScope.checkoutFormBean.errors.CCNumber}</font>
</td>
<br>
</tr>


<tr bgcolor="#CC9999">
<td colspan="2" align=center> 
<input type="submit" value="<fmt:message key='Submit'/>">&nbsp;&nbsp;&nbsp;&nbsp;<input type="reset" value="<fmt:message key='Reset'/>">
</td>
</tr>

</table>
</center>
</form>
