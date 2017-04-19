import java.util.*;

import com.sun.enterprise.security.jauth.*;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;

public class CommonModule implements ClientAuthModule, ServerAuthModule {

    protected AuthPolicy requestPolicy;
    protected AuthPolicy responsePolicy;
    protected CallbackHandler handler;
    protected Map options;

    protected TestCredential cred;

    protected CommonModule() { }

    public void initialize(AuthPolicy requestPolicy,
			AuthPolicy responsePolicy,
			CallbackHandler handler,
			Map options) {
	this.requestPolicy = requestPolicy;
	this.responsePolicy = responsePolicy;
	this.handler = handler;
	this.options = options;
    }

    public void secureRequest(AuthParam param,
				Subject subject,
				Map sharedState)
		throws AuthException {
	if (cred == null) {
	    cred = new TestCredential(this.getClass().getName(),
				options,
				requestPolicy,
				responsePolicy);
	}
	subject.getPublicCredentials().add(cred);
    }

    public void validateResponse(AuthParam param,
				Subject subject,
				Map sharedState)
		throws AuthException {
	if (cred == null) {
	    cred = new TestCredential(this.getClass().getName(),
				options,
				requestPolicy,
				responsePolicy);
	}
	subject.getPublicCredentials().add(cred);
    }

    public void validateRequest(AuthParam param,
				Subject subject,
				Map sharedState)
		throws AuthException {
	if (cred == null) {
	    cred = new TestCredential(this.getClass().getName(),
				options,
				requestPolicy,
				responsePolicy);
	}
	subject.getPublicCredentials().add(cred);
    }

    public void secureResponse(AuthParam param,
				Subject subject,
				Map sharedState)
		throws AuthException {
	if (cred == null) {
	    cred = new TestCredential(this.getClass().getName(),
				options,
				requestPolicy,
				responsePolicy);
	}
	subject.getPublicCredentials().add(cred);
    }

    public void disposeSubject(Subject subject,
				Map sharedState)
		throws AuthException {
	if (cred != null) {
	    subject.getPublicCredentials().remove(cred);
	}
    }
}

