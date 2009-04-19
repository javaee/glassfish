package connector;

import org.glassfish.security.common.PrincipalImpl;

import javax.security.auth.callback.Callback;
import javax.security.auth.Subject;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.callback.PasswordValidationCallback;
import java.util.List;

public class RogueSecurityContextWithListener extends MySecurityContextWithListener{
    public RogueSecurityContextWithListener(String userName, String password, String principalName) {
        super(userName, password, principalName, true); //with translationRequired
    }

    protected void addCallbackHandlers(List<Callback> callbacks, Subject execSubject) {

        //when translation required is ON, PasswordValidationCallback can't be used
        PasswordValidationCallback pvc = null;
        pvc = new PasswordValidationCallback(execSubject, userName, password.toCharArray());
            debug("setting password validation callback with user [ " + userName + " ] + password [ " + password + " ]");
            callbacks.add(pvc);
        debug("setting Password Principal Callback for : Case-II - translation required");

/*
        String principalName = "xyz";

        CallerPrincipalCallback cpc = new CallerPrincipalCallback(execSubject, new PrincipalImpl(principalName));
        debug("setting caller principal callback with principal : " + principalName);
*/
        callbacks.add(pvc);
    }

    public void debug(String message){
        System.out.println("JSR-322 [RA] [RogueSecurityContextWithListener]: " + message);
    }
}
