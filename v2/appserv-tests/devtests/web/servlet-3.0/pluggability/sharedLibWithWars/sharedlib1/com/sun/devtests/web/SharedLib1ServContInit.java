package com.sun.devtests.web;

import java.util.*;
import javax.servlet.*;
import javax.servlet.annotation.*;

@HandlesTypes(WebServlet.class)
public class SharedLib1ServContInit implements ServletContainerInitializer {
    public SharedLib1ServContInit() {
	System.out.println("SHARED LIB 1 CONST");
    }
    public void onStartup(Set<Class<?>> c, ServletContext ctx) {
	ctx.setAttribute("SHAREDLIB-1", "CALLED SHAREDLIB-1");
    }
}
