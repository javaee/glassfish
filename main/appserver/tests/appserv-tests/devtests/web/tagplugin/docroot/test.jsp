<%--

    DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

    Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.

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

<html>
<%-- A test for tag plugins for <c:if>, <c:forEach>, and <c:choose> --%>

Testing tag plugins for &lt;c:if>, &lt;c:forEach>, and &lt;c:choose>
<br/>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="count" value="1"/>

<c:forEach var="index" begin="1" end="3">
  <c:choose>
    <c:when test="${index==3}">
      <c:set var="count" value="${count+30}"/>
    </c:when>
    <c:when test="${index==1}">
      <c:set var="count" value="${count+10}"/>
    </c:when>
    <c:otherwise>
      <c:set var="count" value="${count+100}"/>
    </c:otherwise>
  </c:choose>
</c:forEach>

Count is ${count}, should be 141
<br/>
<c:if test="${count==141}">
  Tag Plugin Test: PASS
</c:if>
</html>
