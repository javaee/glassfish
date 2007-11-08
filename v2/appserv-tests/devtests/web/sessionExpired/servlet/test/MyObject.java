package test;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionBindingEvent;

public class MyObject implements HttpSessionBindingListener {

    /*
     * Notifies the object that it is being bound to a session, and
     * identifies the session.
     *
     * @param event The event that identifies the session 
     */
    public void valueBound(HttpSessionBindingEvent event) {
        // do nothing
    }
    
    /*
     * Notifies the object that it is being unbound from a session, and
     * identifies the session.
     *
     * @param event The event that identifies the session 
     */
    public void valueUnbound(HttpSessionBindingEvent event) {

        HttpSession session = event.getSession();
        ServletContext sc = session.getServletContext();
        sc.setAttribute("successHttpSessionBindingListener", new Object());
    }

}

