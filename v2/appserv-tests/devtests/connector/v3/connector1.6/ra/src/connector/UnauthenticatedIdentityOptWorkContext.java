package connector;


import org.glassfish.security.common.PrincipalImpl;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.Subject;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.resource.spi.work.SecurityContext;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;


public class UnauthenticatedIdentityOptWorkContext extends SecurityContext {

    private String userName;
    private String password;
    private String principalName;
    private boolean translationRequired;
    private Subject subject;
    private boolean principal;

    public UnauthenticatedIdentityOptWorkContext(boolean translationRequired, boolean principal, String principalName, String userName, String password) {
        this.translationRequired = translationRequired;
        this.principal = principal;
        this.userName = userName;
        this.password = password;
        this.principalName = principalName;
    }

    public boolean isTranslationRequired() {
        return translationRequired;
    }

    public void setupSecurityContext(CallbackHandler callbackHandler, Subject execSubject, Subject serviceSubject) {

        List<Callback> callbacks = new ArrayList<Callback>();

        //if (!translationRequired) {
            if (principal) {
                CallerPrincipalCallback cpc = null;
                if(principalName != null){
                    cpc = new CallerPrincipalCallback(execSubject, new PrincipalImpl(principalName));
                    callbacks.add(cpc);
                    debug("setting caller principal callback with principal : " + principalName);
                }else{
                    execSubject.getPrincipals().add(new PrincipalImpl(principalName));
                    debug("setting principal for execSubject : " + principalName);
                }
            } else {
                //empty execSubject
                //do nothing
            }
        //}
        addCallbackHandlers(callbacks, execSubject);

        Callback callbackArray[] = new Callback[callbacks.size()];
        try {
            callbackHandler.handle(callbacks.toArray(callbackArray));

        } catch (UnsupportedCallbackException e) {
            debug("exception occured : " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            debug("exception occured : " + e.getMessage());
        }
            //debug("Password validation callback succeded for user : " + userName);
    }

    protected void addCallbackHandlers(List<Callback> callbacks, Subject execSubject) {
        //do nothing
        //hook to test Dupilcate Inflow Context behavior
    }

    public Subject getSubject() {
        //if (translationRequired) {
            if (subject == null) {
                subject = new Subject();
                subject.getPrincipals().add(new PrincipalImpl(principalName));
                debug("setting translation required for principal : " + principalName);
            }
            return subject;
        /*} else {
            return null;
        }*/
    }

    public String toString() {
        StringBuffer toString = new StringBuffer("{");
        toString.append("userName : " + userName);
        toString.append(", password : " + password);
        toString.append(", principalName : " + principalName);
        toString.append(", translationRequired : " + translationRequired);
        toString.append("}");
        return toString.toString();
    }

    public void debug(String message) {
        System.out.println("JSR-322 [RA] [MySecurityContext]: " + message);
    }

}
