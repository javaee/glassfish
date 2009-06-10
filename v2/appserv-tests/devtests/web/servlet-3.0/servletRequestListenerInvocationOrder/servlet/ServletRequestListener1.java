import javax.servlet.*;

public class ServletRequestListener1 implements ServletRequestListener {

    public void requestInitialized(ServletRequestEvent sre) {
        ServletRequest sr = sre.getServletRequest();
        if (sr.getAttribute("name_2") != null) {
            throw new IllegalStateException(
                "Unexpected request attribute during requestInitialized");
        }
        sr.setAttribute("name_1", "value_1");
    }

    public void requestDestroyed(ServletRequestEvent sre) {
        ServletRequest sr = sre.getServletRequest();
        if (sr.getAttribute("name_2") != null) {
            throw new IllegalStateException(
                "Unexpected request attribute during requestDestroyed");
        }
        sr.removeAttribute("name_1");
    }

}
