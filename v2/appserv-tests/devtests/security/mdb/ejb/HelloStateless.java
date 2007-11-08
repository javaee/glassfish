package com.sun.s1asdev.security.mdb;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;

// Hello1 interface is not annotated with @Local. If the
// bean only implements one interface it is assumed to be
// a local business interface.
@Stateless(description="my stateless bean description") 
public class HelloStateless implements Hello1 {

    @RolesAllowed("javaee")
    public void hello(String s) {
        System.out.println("HelloStateless: " + s);
    }

}
