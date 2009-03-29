package com.sun.devtests.web;

import java.util.*;
import javax.servlet.*;
import javax.servlet.annotation.*;

public class SharedLib2ServContInit implements ServletContainerInitializer {
    public SharedLib2ServContInit() {
	System.out.println("SHARED LIB 2 CONST");
    }
    public void onStartup(Set<Class<?>> c, ServletContext ctx) {
	ctx.setAttribute("SHAREDLIB-2", "CALLED SHAREDLIB-2");
    }
}
