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

<%@ page pageEncoding="UTF-8"%>
<%@ page import="java.io.*"%>
<HTML>
<HEAD>
<META http-equiv="Content-Type" content="text/html; charset=UTF-8">
<TITLE>request#getInputStream test.</TITLE>
</HEAD>
<BODY>
readLine.jsp is called.
<HR>

<%
            String expected = (String) session.getAttribute("expected");
            String formName = (String) session.getAttribute("formName");
            request.setCharacterEncoding("UTF-8");
            response.setContentType("text/html; charset=UTF-8");
            int contentLength = request.getContentLength();
            ByteArrayOutputStream baos = new ByteArrayOutputStream(contentLength);
            InputStream is = request.getInputStream();

            readInputStream(is, baos, contentLength);

            //outln(out,sb.toString());
            
            String boundary = null;
            String contentType = request.getContentType();
            if(contentType != null){
                int delim = contentType.indexOf("boundary=");
                boundary = contentType.substring(delim+9).trim();
                int semi = boundary.indexOf(';');
                if (semi != -1) {
                    boundary = boundary.substring(0, semi);
                }

            }
            expected = "--"+boundary+"\r\nContent-Disposition: form-data; name=\""+formName+"\"\r\n\r\n"+expected+"\r\n--"+boundary+"--\r\n";
            
            outln(out, "Content-Type:" + request.getContentType());
            outln(out, "Character Encoding:" + request.getCharacterEncoding());
            outln(out, "Content-Length:" + request.getContentLength());
            outln(out, "expected:" + expected.length());
            outln(out, "real byte read:" + baos.size());
            outln(out, "isSame:" + (baos.toString("UTF-8").equals(expected)));
%>

</BODY>
</HTML>
<%!void readInputStream(InputStream is, ByteArrayOutputStream baos, int contentLength) throws IOException {
        int bytesRead = -1;
        byte[] bytes = new byte[contentLength];
        while ((bytesRead = is.read(bytes, 0, contentLength)) != -1) {
            baos.write(bytes, 0, bytesRead);
        }
    }

    void outln(JspWriter out, String str) throws IOException {
        out.println(str + "<BR>");
        System.out.println(str);
    }%>
