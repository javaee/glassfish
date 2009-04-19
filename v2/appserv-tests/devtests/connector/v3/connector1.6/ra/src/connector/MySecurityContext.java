package connector;


import org.glassfish.security.common.PrincipalImpl;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.Subject;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.callback.PasswordValidationCallback;
import javax.security.auth.message.callback.GroupPrincipalCallback;
import javax.resource.spi.work.SecurityContext;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;


public class MySecurityContext  extends SecurityContext {

    protected String userName;
    protected String password;
    protected String principalName;
    protected boolean translationRequired;
    protected Subject subject;

    public MySecurityContext(String userName, String password, String principalName, boolean translationRequired) {
        this.userName = userName;
        this.password = password;
        this.principalName = principalName;
        this.translationRequired = translationRequired;
    }

    public boolean isTranslationRequired() {
        return translationRequired;
    }

    public void setupSecurityContext(CallbackHandler callbackHandler, Subject execSubject, Subject serviceSubject) {

        //execSubject.getPublicCredentials().add(new Group("employee"));
        List<Callback> callbacks = new ArrayList<Callback>();


        CallerPrincipalCallback cpc = new CallerPrincipalCallback(execSubject, new PrincipalImpl(principalName));
        debug("setting caller principal callback with principal : " + principalName);
        callbacks.add(cpc);
        
/*
        GroupPrincipalCallback gpc = new GroupPrincipalCallback(execSubject, null);
        callbacks.add(gpc);
*/

        PasswordValidationCallback pvc = null;

        if (!translationRequired) {
            pvc = new PasswordValidationCallback(execSubject, userName,
                    password.toCharArray());
            debug("setting password validation callback with user [ " + userName + " ] + password [ " + password + " ]");
            callbacks.add(pvc);
        }

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

        if (!translationRequired) {
            if (!pvc.getResult()) {
                debug("Password validation callback failure for user : " + userName);
                //throw new RuntimeException("Password validation callback failed for user " + userName);
                //TODO need to throw exception later (once spec defines it) and fail setup security context
            } else {
                debug("Password validation callback succeded for user : " + userName);
            }
        }
    }

    protected void addCallbackHandlers(List<Callback> callbacks, Subject execSubject) {
        //do nothing
        //hook to test Dupilcate Inflow Context behavior
    }

    public Subject getSubject() {
        if (translationRequired) {
            if (subject == null) {
                subject = new Subject();
                subject.getPrincipals().add(new PrincipalImpl(principalName));
                debug("setting translation required for principal : " + principalName);
            }
            return subject;
        } else {
            return null;
        }
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
