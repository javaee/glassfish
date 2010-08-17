package connector;

import org.glassfish.security.common.PrincipalImpl;

import javax.resource.spi.work.SecurityContext;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.Callback;
import javax.security.auth.Subject;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import java.util.List;
import java.util.ArrayList;

/**
 * To check Unauthenticated SIC with a null principal or null name
 */
public class UnauthenticatedSecurityContext_NullPrincipalOrName extends SecurityContext {

    private String principalName;
    private boolean translationRequired;
    private boolean nullPrincipal;

    public UnauthenticatedSecurityContext_NullPrincipalOrName(boolean translationRequired, String principalName, boolean nullPrincipal) {
        this.translationRequired = translationRequired;
        this.principalName = principalName;
        this.nullPrincipal = nullPrincipal;
    }

    public void setupSecurityContext(CallbackHandler callbackHandler, Subject subject, Subject subject1) {
        try {
            List<Callback> callbacks = new ArrayList<Callback>();

            CallerPrincipalCallback cpc;
            if (nullPrincipal) {
                PrincipalImpl p = null;
                cpc = new CallerPrincipalCallback(new Subject(), p);
            } else {
                String name = null;
                cpc = new CallerPrincipalCallback(new Subject(), name);
            }
            callbacks.add(cpc);
            Callback callbackArray[] = new Callback[callbacks.size()];
            callbackHandler.handle(callbacks.toArray(callbackArray));
        } catch (Exception e) {
            debug(e.toString());
        }
    }

    public void debug(String message) {
        System.out.println("JSR-322 [RA] [UnauthenticatedSecurityContext_NullPrincipalOrName]: " + message);
    }

}
