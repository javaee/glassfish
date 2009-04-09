package com.sun.devtests.web;

import java.util.*;
import javax.servlet.*;
import javax.servlet.annotation.*;

@HandlesTypes(WebFilter.class)
public class SharedLib3ServContInit implements ServletContainerInitializer {
    public SharedLib3ServContInit() {
	System.out.println("SHARED LIB 3 CONST");
    }
    public void onStartup(Set<Class<?>> c, ServletContext ctx) {
	ctx.setAttribute("SHAREDLIB-3", "CALLED EARSHAREDLIB-3");
    }
}
