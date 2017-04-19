<%--

    DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

    Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.

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

<%@ page pageEncoding="UTF-8"%>
<%@ page import="java.io.*"%>
<HTML>
<HEAD>
<META http-equiv="Content-Type" content="text/html; charset=UTF-8">
<TITLE>request#getReader test.</TITLE>
</HEAD>
<BODY>

<%!//static char JP[] = "あいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわゐゑをん"
   static char JP[] = "\u3068\u4eba\u6587"
            .toCharArray();

    static char ASCII[] = "abcdefghijklmnopqrstuvwxyz".toCharArray();

    static String formName = "n";%>

<%
            response.setContentType("text/html; charset=UTF-8");

            int size = 8192;
            String sizeS = request.getParameter("size");
            if (sizeS != null) {
                size = Integer.parseInt(sizeS);
            }

            boolean isAscii = true;
            String ascii = request.getParameter("ascii");
            if (ascii != null) {
                isAscii = Boolean.parseBoolean(ascii);
            }

            char[] chars = isAscii ? ASCII : JP;

            StringBuffer sb = new StringBuffer(size + formName.length() + 1
                    + chars.length);
            while (sb.length() < size) {
                sb.append(chars);
            }
            if (sb.length() > size) {
                sb.delete(size, sb.length());
            }
%>

<FORM method="POST" action="<%= response.encodeURL("readLine.jsp") %>" enctype="multipart/form-data">
request#getReader()#readLine test<BR>
<input type="text" name="<%= formName %>" value="<%= sb.toString() %>" />
<input type="submit" value="send" /></FORM>

<FORM method="POST" action="<%= response.encodeURL("read.jsp") %>" enctype="multipart/form-data">
request#getReader()#read() test<BR>
<input type="text" name="<%= formName %>" value="<%= sb.toString() %>" />
<input type="submit" value="send" /></FORM>

<FORM method="POST" action="<%= response.encodeURL("readCharB.jsp") %>" enctype="multipart/form-data" >
request#getReader()#read(char[1]) test<BR>
<input type="text" name="<%= formName %>" value="<%= sb.toString() %>" />
<input type="submit" value="send" /></FORM>

<FORM method="POST" action="<%= response.encodeURL("readInputStream.jsp") %>" enctype="multipart/form-data" >
request#getInputStream()#read(bytes[]) test<BR>
<input type="text" name="<%= formName %>" value="<%= sb.toString() %>" />
<input type="submit" value="send" /></FORM>

<%
            session.setAttribute("expected", sb.toString());
            session.setAttribute("formName", formName);
%>
</BODY>
</HTML>
