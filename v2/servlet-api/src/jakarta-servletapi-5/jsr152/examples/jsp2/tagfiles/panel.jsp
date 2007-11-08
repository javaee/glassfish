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

<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<html>
  <head>
    <title>JSP 2.0 Examples - Panels using Tag Files</title>
  </head>
  <body>
    <h1>JSP 2.0 Examples - Panels using Tag Files</h1>
    <hr>
    <p>This JSP page invokes a custom tag that draws a 
    panel around the contents of the tag body.  Normally, such a tag 
    implementation would require a Java class with many println() statements,
    outputting HTML.  Instead, we can use a .tag file as a template,
    and we don't need to write a single line of Java or even a TLD!</p>
    <hr>
    <table border="0">
      <tr valign="top">
        <td>
          <tags:panel color="#ff8080" bgcolor="#ffc0c0" title="Panel 1">
	    First panel.<br/>
	  </tags:panel>
        </td>
        <td>
          <tags:panel color="#80ff80" bgcolor="#c0ffc0" title="Panel 2">
	    Second panel.<br/>
	    Second panel.<br/>
	    Second panel.<br/>
	    Second panel.<br/>
	  </tags:panel>
        </td>
        <td>
          <tags:panel color="#8080ff" bgcolor="#c0c0ff" title="Panel 3">
	    Third panel.<br/>
            <tags:panel color="#ff80ff" bgcolor="#ffc0ff" title="Inner">
	      A panel in a panel.
	    </tags:panel>
	    Third panel.<br/>
	  </tags:panel>
        </td>
      </tr>
    </table>
  </body>
</html>
