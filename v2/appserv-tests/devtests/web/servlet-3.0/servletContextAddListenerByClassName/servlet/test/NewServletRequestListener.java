package test;

import java.io.*;
import javax.servlet.*;

public class NewServletRequestListener implements ServletRequestListener {

    public void requestInitialized(ServletRequestEvent sre) {
        sre.getServletRequest().setAttribute("abc", "def");
    }

    public void requestDestroyed(ServletRequestEvent sre) {
        // Do nothing
    }
}
