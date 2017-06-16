package connector;

import java.security.Principal;


public class MyPrincipal implements Principal {

    private String name;
    /**
     * Returns the name of this principal.
     *
     * @return the name of this principal.
     */
    public String getName() {
        return name;
    }

    public MyPrincipal(String name){
        this.name = name;
    }
}
