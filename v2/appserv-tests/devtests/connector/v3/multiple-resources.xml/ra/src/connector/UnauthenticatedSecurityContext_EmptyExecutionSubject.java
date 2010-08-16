package connector;

import javax.resource.spi.work.SecurityContext;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.Callback;
import javax.security.auth.Subject;
import java.util.List;
import java.util.ArrayList;

/**
 * To check Unauthenticated SIC with empty (untouched) execution subject
 */
public class UnauthenticatedSecurityContext_EmptyExecutionSubject extends SecurityContext {


    public UnauthenticatedSecurityContext_EmptyExecutionSubject() {
    }

    public void setupSecurityContext(CallbackHandler callbackHandler, Subject subject, Subject subject1) {
        try {
            List<Callback> callbacks = new ArrayList<Callback>();
            Callback callbackArray[] = new Callback[callbacks.size()];
            callbackHandler.handle(callbacks.toArray(callbackArray));
        } catch (Exception e) {
            debug(e.toString());
        }
    }

    public void debug(String message) {
        System.out.println("JSR-322 [RA] [UnauthenticatedSecurityContext_EmptyExecutionSubject]: " + message);
    }

}
