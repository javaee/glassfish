package test;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionListener;
import javax.servlet.http.HttpSessionEvent;

public class MyHttpSessionListener implements HttpSessionListener {

    /**
     * Receives notification that a session has been created.
     *
     * @param hse The HttpSessionEvent
     */
    public void sessionCreated(HttpSessionEvent hse) {
        // Do nothing
    }

    /**
     * Receives notification that a session is about to be invalidated.
     *
     * @param hse The HttpSessionEvent
     */
    public void sessionDestroyed(HttpSessionEvent hse) {

        HttpSession session = hse.getSession();
        ServletContext sc = session.getServletContext();
        sc.setAttribute("successHttpSessionListener", new Object());
    }

}
