<!--
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
-->
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<html>
  <head>
    <title>JSP 2.0 Examples - Display Products Tag File</title>
  </head>
  <body>
    <h1>JSP 2.0 Examples - Display Products Tag File</h1>
    <hr>
    <p>This JSP page invokes a tag file that displays a listing of 
    products.  The custom tag accepts two fragments that enable
    customization of appearance.  One for when the product is on sale
    and one for normal price.</p>
    <p>The tag is invoked twice, using different styles</p>
    <hr>
    <h2>Products</h2>
    <tags:displayProducts>
      <jsp:attribute name="normalPrice">
	Item: ${name}<br/>
	Price: ${price}
      </jsp:attribute>
      <jsp:attribute name="onSale">
	Item: ${name}<br/>
	<font color="red"><strike>Was: ${origPrice}</strike></font><br/>
	<b>Now: ${salePrice}</b>
      </jsp:attribute>
    </tags:displayProducts>
    <hr>
    <h2>Products (Same tag, alternate style)</h2>
    <tags:displayProducts>
      <jsp:attribute name="normalPrice">
	<b>${name}</b> @ ${price} ea.
      </jsp:attribute>
      <jsp:attribute name="onSale">
	<b>${name}</b> @ ${salePrice} ea. (was: ${origPrice})
      </jsp:attribute>
    </tags:displayProducts>
  </body>
</html>
