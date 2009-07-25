package connector;

import javax.resource.spi.work.WorkContextLifecycleListener;


public class MySecurityContextWithListener extends MySecurityContext implements WorkContextLifecycleListener {

    public MySecurityContextWithListener(String userName, String password,
                                               String principalName, boolean translationRequired, boolean expectSuccess, boolean expectPVSuccess){
        super(userName, password, principalName, translationRequired, expectSuccess, expectPVSuccess);
    }

    public void contextSetupComplete() {
        debug("Context setup completed " + this.toString() );
        if(!expectSuccess){
            throw new Error("Container has completed context setup which is not expected");
        }
    }

    public void contextSetupFailed(String string) {
        debug("Context setup failed with the following message : " + string + " for security-inflow-context " +
                this.toString());
        if(expectSuccess){
            throw new Error("Container has not completed context setup");
        }
    }

    public void debug(String message){
        System.out.println("JSR-322 [RA] [MySecurityContextWithListener]: " + message);
    }

}
