package connector;

import javax.resource.spi.work.WorkContextLifecycleListener;


public class MySecurityContextWithListener extends MySecurityContext implements WorkContextLifecycleListener {

    public MySecurityContextWithListener(String userName, String password,
                                               String principalName, boolean translationRequired){
        super(userName, password, principalName, translationRequired);
    }

    public void contextSetupComplete() {
        debug("Context setup completed " + this.toString() );
    }

    public void contextSetupFailed(String string) {
        debug("Context setup failed with the following message : " + string + " for security-inflow-context " +
                this.toString());
    }

    public void debug(String message){
        System.out.println("JSR-322 [RA] [MySecurityContextWithListener]: " + message);
    }

}
