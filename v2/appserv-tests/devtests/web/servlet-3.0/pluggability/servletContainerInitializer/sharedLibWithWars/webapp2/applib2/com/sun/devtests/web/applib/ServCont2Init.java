package com.sun.devtests.web.applib;

import java.util.*;
import javax.servlet.*;
import javax.servlet.annotation.*;

@HandlesTypes(WebFilter.class)
public class ServCont2Init implements ServletContainerInitializer {
    public ServCont2Init() {
	System.out.println("APP LIB 2 CONST");
    }
    public void onStartup(Set<Class<?>> c, ServletContext ctx) {
	ctx.setAttribute("APPLIB-2", "CALLED APPLIB-2");
    }
}
