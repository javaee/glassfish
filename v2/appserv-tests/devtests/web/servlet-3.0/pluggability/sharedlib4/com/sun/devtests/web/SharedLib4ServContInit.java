package com.sun.devtests.web;

import java.util.*;
import javax.servlet.*;
import javax.servlet.annotation.*;

@HandlesTypes(javax.jws.WebService.class)
public class SharedLib4ServContInit implements ServletContainerInitializer {
    public SharedLib4ServContInit() {
	System.out.println("SHARED LIB 4 CONST");
    }
    public void onStartup(Set<Class<?>> c, ServletContext ctx) {
	ctx.setAttribute("SHAREDLIB-4", "CALLED SHAREDLIB-4");
    }
}
