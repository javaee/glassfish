<%--

    DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

    Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.

    The contents of this file are subject to the terms of either the GNU
    General Public License Version 2 only ("GPL") or the Common Development
    and Distribution License("CDDL") (collectively, the "License").  You
    may not use this file except in compliance with the License.  You can
    obtain a copy of the License at
    https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
    or packager/legal/LICENSE.txt.  See the License for the specific
    language governing permissions and limitations under the License.

    When distributing the software, include this License Header Notice in each
    file and include the License file at packager/legal/LICENSE.txt.

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

<!--
 The contents of this file are subject to the terms
 of the Common Development and Distribution License
 (the License).  You may not use this file except in
 compliance with the License.
 
 You can obtain a copy of the license at
 https://glassfish.dev.java.net/public/CDDLv1.0.html or
 glassfish/bootstrap/legal/CDDLv1.0.txt.
 See the License for the specific language governing
 permissions and limitations under the License.
 
 When distributing Covered Code, include this CDDL
 Header Notice in each file and include the License file
 at glassfish/bootstrap/legal/CDDLv1.0.txt.
 If applicable, add the following below the CDDL Header,
 with the fields enclosed by brackets [] replaced by
 you own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"
 
 Copyright 2006 Sun Microsystems, Inc. All rights reserved.
-->

<html>
<head><title>test page for 4642650</title></head>
<%
String url = response.encodeRedirectUrl("/web-relativeRedirectAllowed/jsp/test2.jsp");
System.err.println("URL: "+url);
response.sendRedirect(url);
%>
</html>
