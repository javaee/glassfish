import javax.servlet.*;

public class ServletRequestListener2 implements ServletRequestListener {

    public void requestInitialized(ServletRequestEvent sre) {
        ServletRequest sr = sre.getServletRequest();
        if (sr.getAttribute("name_1") == null) {
            throw new IllegalStateException(
                "Missing request attribute during requestInitialized");
        }
        sr.setAttribute("name_2", "value_2");
    }

    public void requestDestroyed(ServletRequestEvent sre) {
        ServletRequest sr = sre.getServletRequest();
        if (sr.getAttribute("name_1") == null) {
            throw new IllegalStateException(
                "Missing request attribute during requestDestroyed");
        }
        sr.removeAttribute("name_2");
    }

}
