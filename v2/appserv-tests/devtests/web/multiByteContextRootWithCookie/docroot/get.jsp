<%  String result = null;
    if (session != null) {
        result = "abc=" + session.getAttribute("abc");
    }
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
        for (Cookie cookie : cookies) {
            if ("myid".equals(cookie.getName())) {
                result += "; myid=" + cookie.getValue();
            }
        }
    }
    out.println(result);
%>
