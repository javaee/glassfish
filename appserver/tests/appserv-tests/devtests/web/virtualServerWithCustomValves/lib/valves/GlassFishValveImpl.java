package valves;

import java.io.IOException;
import javax.servlet.ServletException;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.glassfish.web.valve.GlassFishValve;

public class GlassFishValveImpl implements GlassFishValve {

    public String getInfo() {
        return getClass().getName();
    }

    public int invoke(Request request, Response response)
            throws IOException, ServletException {
        ((org.apache.catalina.connector.Request) request).setAttribute(
            "ATTR_3", "VALUE_3");
        return INVOKE_NEXT;
    }

    public void postInvoke(Request request, Response response)
            throws IOException, ServletException {
        // Deliberate no-op
    }
}

