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

<html>
  <head>
    <title>JSP 2.0 Expression Language - Basic Comparisons</title>
  </head>
  <body>
    <h1>JSP 2.0 Expression Language - Basic Comparisons</h1>
    <hr>
    This example illustrates basic Expression Language comparisons.
    The following comparison operators are supported:
    <ul>
      <li>Less-than (&lt; or lt)</li>
      <li>Greater-than (&gt; or gt)</li>
      <li>Less-than-or-equal (&lt;= or le)</li>
      <li>Greater-than-or-equal (&gt;= or ge)</li>
      <li>Equal (== or eq)</li>
      <li>Not Equal (!= or ne)</li>
    </ul>
    <blockquote>
      <u><b>Numeric</b></u>
      <code>
        <table border="1">
          <thead>
	    <td><b>EL Expression</b></td>
	    <td><b>Result</b></td>
	  </thead>
	  <tr>
	    <td>\${1 &lt; 2}</td>
	    <td>${1 < 2}</td>
	  </tr>
	  <tr>
	    <td>\${1 lt 2}</td>
	    <td>${1 lt 2}</td>
	  </tr>
	  <tr>
	    <td>\${1 &gt; (4/2)}</td>
	    <td>${1 > (4/2)}</td>
	  </tr>
	  <tr>
	    <td>\${1 &gt; (4/2)}</td>
	    <td>${1 > (4/2)}</td>
	  </tr>
	  <tr>
	    <td>\${4.0 &gt;= 3}</td>
	    <td>${4.0 >= 3}</td>
	  </tr>
	  <tr>
	    <td>\${4.0 ge 3}</td>
	    <td>${4.0 ge 3}</td>
	  </tr>
	  <tr>
	    <td>\${4 &lt;= 3}</td>
	    <td>${4 <= 3}</td>
	  </tr>
	  <tr>
	    <td>\${4 le 3}</td>
	    <td>${4 le 3}</td>
	  </tr>
	  <tr>
	    <td>\${100.0 == 100}</td>
	    <td>${100.0 == 100}</td>
	  </tr>
	  <tr>
	    <td>\${100.0 eq 100}</td>
	    <td>${100.0 eq 100}</td>
	  </tr>
	  <tr>
	    <td>\${(10*10) != 100}</td>
	    <td>${(10*10) != 100}</td>
	  </tr>
	  <tr>
	    <td>\${(10*10) ne 100}</td>
	    <td>${(10*10) ne 100}</td>
	  </tr>
	</table>
      </code>
      <br>
      <u><b>Alphabetic</b></u>
      <code>
        <table border="1">
          <thead>
	    <td><b>EL Expression</b></td>
	    <td><b>Result</b></td>
	  </thead>
	  <tr>
	    <td>\${'a' &lt; 'b'}</td>
	    <td>${'a' < 'b'}</td>
	  </tr>
	  <tr>
	    <td>\${'hip' &gt; 'hit'}</td>
	    <td>${'hip' > 'hit'}</td>
	  </tr>
	  <tr>
	    <td>\${'4' &gt; 3}</td>
	    <td>${'4' > 3}</td>
	  </tr>
	</table>
      </code>
    </blockquote>
  </body>
</html>
