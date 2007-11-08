<?xml version="1.0"?>
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
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page"
  version="1.2">
<jsp:directive.page contentType="text/html"/>
<jsp:directive.page import="java.util.Date, java.util.Locale"/>
<jsp:directive.page import="java.text.*"/>

<jsp:declaration>
  String getDateTimeStr(Locale l) {
    DateFormat df = SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, l);
    return df.format(new Date());
  }
</jsp:declaration>

<html>
<head>
  <title>Example JSP in XML format</title>
</head>

<body>
This is the output of a simple JSP using XML format. 
<br />

<div>Use a jsp:scriptlet to loop from 1 to 10: </div>
<jsp:scriptlet>
// Note we need to declare CDATA because we don't escape the less than symbol
<![CDATA[
  for (int i = 1; i<=10; i++) {
    out.println(i);
    if (i < 10) {
      out.println(", ");
    }
  }
]]>
</jsp:scriptlet>

<!-- Because I omit br's end tag, declare it as CDATA -->
<![CDATA[
  <br><br>
]]>

<div align="left">
  Use a jsp:expression to write the date and time in the browser's locale: 
  <jsp:expression>getDateTimeStr(request.getLocale())</jsp:expression>
</div>


<jsp:text>
  &lt;p&gt;This sentence is enclosed in a jsp:text element.&lt;/p&gt;
</jsp:text>

</body>
</html>
</jsp:root>
