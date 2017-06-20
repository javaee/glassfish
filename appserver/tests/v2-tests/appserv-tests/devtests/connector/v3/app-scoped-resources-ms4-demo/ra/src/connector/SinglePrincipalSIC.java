package connector;

import javax.resource.spi.work.SecurityContext;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.Callback;
import javax.security.auth.Subject;
import java.security.Principal;
import java.util.List;
import java.util.ArrayList;


public class SinglePrincipalSIC extends SecurityContext{

    private Principal p = null;
    public SinglePrincipalSIC(Principal p){
        this.p = p;
    }
    public void setupSecurityContext(CallbackHandler callbackHandler, Subject executionSubject, Subject serviceSubject) {

        executionSubject.getPrincipals().add(p);

        try {
            List<Callback> callbacks = new ArrayList<Callback>();
            Callback callbackArray[] = new Callback[callbacks.size()];
            callbackHandler.handle(callbacks.toArray(callbackArray));
        } catch (Exception e) {
            debug(e.toString());
        }
    }

    public void debug(String message) {
        System.out.println("JSR-322 [RA] [SinglePrincipalSIC]: " + message);
    }

}
