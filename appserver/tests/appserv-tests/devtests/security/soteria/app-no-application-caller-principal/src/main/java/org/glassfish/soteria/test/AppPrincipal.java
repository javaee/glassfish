package org.glassfish.soteria.test;

import java.security.Principal;

/**
 * Created by vinay on 7/8/17.
 */
public class AppPrincipal implements Principal {
    String name;

    public AppPrincipal(String name){
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
