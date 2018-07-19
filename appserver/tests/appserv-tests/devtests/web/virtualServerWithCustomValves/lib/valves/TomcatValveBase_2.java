package valves;

import java.io.*;
import javax.servlet.*;
import org.apache.catalina.connector.*;
import org.apache.catalina.valves.*;

public class TomcatValveBase_2 extends ValveBase {

    public String getInfo() {
	return getClass().getName();
    }

    public void invoke(Request request, Response response)
            throws IOException, ServletException {
        request.setAttribute("ATTR_2", "VALUE_2");
        getNext().invoke(request, response);
    }
}
