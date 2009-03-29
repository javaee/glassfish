package com.sun.devtests.web.applib;

import java.util.*;
import javax.servlet.*;
import javax.servlet.annotation.*;

@HandlesTypes(WebServlet.class)
public class ServContInit implements ServletContainerInitializer {
    public ServContInit() {
	System.out.println("TEST APP LIB CONST");
    }
    public void onStartup(Set<Class<?>> c, ServletContext ctx) {
	System.out.println("TEST APP LIB onStartup");
	ctx.setAttribute("APPLIB-1", "CALLED APPLIB-1");
    }
}
