package connector;

import javax.resource.spi.work.SecurityContext;
import javax.security.auth.Subject;
import javax.security.auth.message.callback.GroupPrincipalCallback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;


public class MyPlainSICWithGPC extends SecurityContext {

        private String groups[];
        public MyPlainSICWithGPC(String[] groupNames){
            this.groups = groupNames;
        }


        public void setupSecurityContext(CallbackHandler callbackHandler, Subject execSubject, Subject serviceSubject) {


            List<Callback> callbacks = new ArrayList<Callback>();

            GroupPrincipalCallback gpc = new GroupPrincipalCallback(execSubject, groups);

            debug("setting group principal callback with group : " + groups);
            callbacks.add(gpc);

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
        }

        protected void addCallbackHandlers(List<Callback> callbacks, Subject execSubject) {
            //do nothing
            //hook to test Dupilcate Inflow Context behavior
        }

        public String toString(){
            StringBuffer toString = new StringBuffer("{");
            for(String group : groups){
                toString.append(", groups : " + group);
            }
            toString.append("}");
            return toString.toString();
        }

        public void debug(String message){
            System.out.println("JSR-322 [RA] [MyPlainSICWithGPC]: " + message);
        }

}
