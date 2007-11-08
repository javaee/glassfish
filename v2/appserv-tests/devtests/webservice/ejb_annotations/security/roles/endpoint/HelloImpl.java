package endpoint;

import javax.jws.WebService;
import javax.ejb.Stateless;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;

@WebService
@Stateless
public class HelloImpl {

    @RolesAllowed(value={"webservicetester"})
    public String roleBased(String who) {
        return "WebSvcTest-Hello " + who;
    }
    
    @DenyAll
    public String denyAll(String who) {
        return "WebSvcTest-Hello " + who;
    }

   @PermitAll
    public String permitAll(String who) {
        return "WebSvcTest-Hello " + who;
    }    
}
