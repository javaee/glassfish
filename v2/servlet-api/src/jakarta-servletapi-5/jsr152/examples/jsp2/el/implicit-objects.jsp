<%--
  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

  Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.

  Portions Copyright Apache Software Foundation.

  The contents of this file are subject to the terms of either the GNU
  General Public License Version 2 only ("GPL") or the Common Development
  and Distribution License("CDDL") (collectively, the "License").  You
  may not use this file except in compliance with the License. You can obtain
  a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
  or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
  language governing permissions and limitations under the License.

  When distributing the software, include this License Header Notice in each
  file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
  Sun designates this particular file as subject to the "Classpath" exception
  as provided by Sun in the GPL Version 2 section of the License file that
  accompanied this code.  If applicable, add the following below the License
  Header, with the fields enclosed by brackets [] replaced by your own
  identifying information: "Portions Copyrighted [year]
  [name of copyright owner]"

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
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
  <head>
    <title>JSP 2.0 Expression Language - Implicit Objects</title>
  </head>
  <body>
    <h1>JSP 2.0 Expression Language - Implicit Objects</h1>
    <hr>
    This example illustrates some of the implicit objects available 
    in the Expression Lanaguage.  The following implicit objects are 
    available (not all illustrated here):
    <ul>
      <li>pageContext - the PageContext object</li>
      <li>pageScope - a Map that maps page-scoped attribute names to 
          their values</li>
      <li>requestScope - a Map that maps request-scoped attribute names 
          to their values</li>
      <li>sessionScope - a Map that maps session-scoped attribute names 
          to their values</li>
      <li>applicationScope - a Map that maps application-scoped attribute 
          names to their values</li>
      <li>param - a Map that maps parameter names to a single String 
          parameter value</li>
      <li>paramValues - a Map that maps parameter names to a String[] of 
          all values for that parameter</li>
      <li>header - a Map that maps header names to a single String 
          header value</li>
      <li>headerValues - a Map that maps header names to a String[] of 
          all values for that header</li>
      <li>initParam - a Map that maps context initialization parameter 
          names to their String parameter value</li>
      <li>cookie - a Map that maps cookie names to a single Cookie object.</li>
    </ul>

    <blockquote>
      <u><b>Change Parameter</b></u>
      <form action="implicit-objects.jsp" method="GET">
	  foo = <input type="text" name="foo" value="${fn:escapeXml(param["foo"])}">
          <input type="submit">
      </form>
      <br>
      <code>
        <table border="1">
          <thead>
	    <td><b>EL Expression</b></td>
	    <td><b>Result</b></td>
	  </thead>
	  <tr>
	    <td>\${param.foo}</td>
	    <td>${fn:escapeXml(param.foo)}&nbsp;</td>
	  </tr>
	  <tr>
	    <td>\${param["foo"]}</td>
	    <td>${fn:escapeXml(param["foo"])}&nbsp;</td>
	  </tr>
	  <tr>
	    <td>\${header["host"]}</td>
	    <td>${fn:escapeXml(header["host"])}&nbsp;</td>
	  </tr>
	  <tr>
	    <td>\${header["accept"]}</td>
	    <td>${fn:escapeXml(header["accept"])}&nbsp;</td>
	  </tr>
	  <tr>
	    <td>\${header["user-agent"]}</td>
	    <td>${fn:escapeXml(header["user-agent"])}&nbsp;</td>
	  </tr>
	</table>
      </code>
    </blockquote>
  </body>
</html>
