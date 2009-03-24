import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.valves.ValveBase;

public class SetRequestedSessionCookiePathValve extends ValveBase {

    public String getInfo() {
        return getClass().getName();
    }

    public int invoke(Request request, Response response)
            throws IOException, ServletException {
        String path = ((HttpServletRequest) request.getRequest()).getContextPath();
        request.setRequestedSessionCookiePath(path + path);
        return INVOKE_NEXT;
    }

    public void postInvoke(Request request, Response response)
            throws IOException, ServletException {
        // Deliberate no-op
    }
}

