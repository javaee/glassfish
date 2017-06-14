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
