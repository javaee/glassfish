package connector;

import org.glassfish.security.common.Group;
import org.glassfish.security.common.PrincipalImpl;

import javax.security.auth.Subject;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.callback.PasswordValidationCallback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.resource.spi.work.SecurityContext;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;


public class MyPlainSecurityContext extends SecurityContext {
    private String userName;
    private String password;
    private String principalName;
    private Subject subject;
    private boolean translationRequired;
    public MyPlainSecurityContext(String userName, String password, String principalName, boolean translationRequired){
        this.userName = userName;
        this.password = password;
        this.principalName = principalName;
        this.translationRequired = translationRequired;
    }


    public void setupSecurityContext(CallbackHandler callbackHandler, Subject execSubject, Subject serviceSubject) {

        ////execSubject.getPublicCredentials().add(new Group("employee"));

        //execSubject.getPublicCredentials().add(new PrincipalImpl(principalName));
        execSubject.getPrincipals().add(new PrincipalImpl(principalName));
        List<Callback> callbacks = new ArrayList<Callback>();

        CallerPrincipalCallback cpc = new CallerPrincipalCallback(execSubject, new PrincipalImpl(principalName));

        debug("setting caller principal callback with principal : " + principalName);
        callbacks.add(cpc);
/*
        PasswordValidationCallback pvc = null;
        if(translationRequired){
        pvc = new PasswordValidationCallback(execSubject, userName,
                password.toCharArray());
        debug("setting password validation callback with user [ " + userName + " ] + password [ " + password + " ]");

        callbacks.add(pvc);
        }
*/
        addCallbackHandlers(callbacks, execSubject);

        Callback callbackArray[] = new Callback[callbacks.size()];
        try{
            callbackHandler.handle(callbacks.toArray(callbackArray));

        }catch(UnsupportedCallbackException e){
            debug("exception occured : " + e.getMessage());
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
            debug("exception occured : " + e.getMessage());
        }

/*        if(translationRequired){

        if(!pvc.getResult()){
            debug("Password validation callback failure for user : " + userName);
            //throw new RuntimeException("Password validation callback failed for user " + userName);
            //TODO need to throw exception later (once spec defines it) and fail setup security context
        }else{
            debug("Password validation callback succeded for user : " + userName);

        }

        }*/

    }

    protected void addCallbackHandlers(List<Callback> callbacks, Subject execSubject) {
        //do nothing
        //hook to test Dupilcate Inflow Context behavior
    }

    public String toString(){
        StringBuffer toString = new StringBuffer("{");
        toString.append("userName : " + userName);
        toString.append(", password : " + password);
        toString.append(", principalName : " + principalName);
        toString.append("}");
        return toString.toString();
    }

    public void debug(String message){
        System.out.println("JSR-322 [RA] [MyPlainSecurityContext]: " + message);
    }

}
