package servlet_endpoint;

import javax.jws.WebService;

@WebService
public class ServletHello {
	
    public String sayServletHello(String param) {
	return "WebSvcTest-Servlet-Hello " + param;
    }
}
