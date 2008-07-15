<%@ page pageEncoding="UTF-8"%>
<%@ page import="java.io.*"%>
<HTML>
<HEAD>
<META http-equiv="Content-Type" content="text/html; charset=UTF-8">
<TITLE>request#getReader test.</TITLE>
</HEAD>
<BODY>
readLine.jsp is called.
<HR>

<%
            String expected = (String) session.getAttribute("expected");
            String formName = (String) session.getAttribute("formName");
            request.setCharacterEncoding("UTF-8");
            response.setContentType("text/html; charset=UTF-8");
            BufferedReader reader = request.getReader();
            StringBuffer sb = new StringBuffer();

            readLine(reader, sb);

            //outln(out,sb.toString());
            
            String boundary = null;
            String contentType = request.getContentType();
            if(contentType != null){
                int delim = contentType.indexOf("boundary=");
                boundary = contentType.substring(delim+9).trim();
            }
            expected = "--"+boundary+"\r\nContent-Disposition: form-data; name=\""+formName+"\"\r\n\r\n"+expected+"\r\n--"+boundary+"--\r\n";
            
            outln(out, "Content-Type:" + request.getContentType());
            outln(out, "Character Encoding:" + request.getCharacterEncoding());
            outln(out, "Content-Length:" + request.getContentLength());
            outln(out, "expected:" + expected.length());
            outln(out, "real read:" + sb.length());
            outln(out, "isSame:" + (sb.toString().equals(expected)));
%>

</BODY>
</HTML>
<%!void readLine(BufferedReader br, StringBuffer sb) throws IOException {
        String read = null;
        while ((read = br.readLine()) != null) {
            sb.append(read+"\r\n");
        }
    }

    void outln(JspWriter out, String str) throws IOException {
        out.println(str + "<BR>");
        System.out.println(str);
    }%>
