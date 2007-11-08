package test;

import javax.servlet.*;
import javax.servlet.http.*;

public class StaticListener implements ServletContextAttributeListener {

    public static boolean removeAttribute = false;

	public void attributeAdded(ServletContextAttributeEvent evt) {
		System.out.println("\n\nAdded a servlet context attribute: " + evt.getName() + " ("+ evt.getValue() +")");
	}

	public void attributeRemoved(ServletContextAttributeEvent evt) {
		System.out.println("\n\nRemoved a servlet context attribute: " + evt.getName() + " ("+ evt.getValue() +")");

        System.out.println("removeAttribute: " + removeAttribute);

        removeAttribute = true;
	}

	public void attributeReplaced(ServletContextAttributeEvent evt) {
		System.out.println("\n\nReplaced a servlet context attribute: " + evt.getName() + " ("+ evt.getValue() +")");
	}
}



