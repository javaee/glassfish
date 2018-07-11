<%@ page import="javax.servlet.http.Part" %>

Beginning
<%
  for (Part p: request.getParts()) {

    out.write("Part: " + p.toString() + "<br/>\n");
    out.write("Part name: " + p.getName() + "<br/>\n");
    out.write("Size: " + p.getSize() + "<br/>\n");
    out.write("Content Type: " + p.getContentType() + "<br/>\n");
    out.write("Header Names:");
    for (String name: p.getHeaderNames()) {
        out.write(" " + name);
    }
    out.write("<br/><br/>\n");
/*
    java.io.InputStreamReader in =
      new java.io.InputStreamReader(p.getInputStream());

    int c = in.read();
    while (c != -1) {
      if (c == '\n') out.write("<br/>");
      out.write(c);
      c = in.read();
    }
*/
  }
%>
End

