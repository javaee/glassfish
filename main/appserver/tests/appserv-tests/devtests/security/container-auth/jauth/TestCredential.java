import java.util.*;

import com.sun.enterprise.security.jauth.*;

public class TestCredential {

    String moduleClass;
    Map options;
    AuthPolicy requestPolicy;
    AuthPolicy responsePolicy;

    public TestCredential(String moduleClass,
			Map options,
			AuthPolicy requestPolicy,
			AuthPolicy responsePolicy) {
	this.moduleClass = moduleClass;
	this.options = options;
	this.requestPolicy = requestPolicy;
	this.responsePolicy = responsePolicy;
    }

    public boolean equals(Object o) {
	if (this == o) {
	    return true;
	}

	if (!(o instanceof TestCredential)) {
	    return false;
	}
	TestCredential that = (TestCredential)o;

	if (this.moduleClass.equals(that.moduleClass) &&
	    this.options.equals(that.options) &&
	    (this.requestPolicy == that.requestPolicy ||
		(this.requestPolicy != null &&
			this.requestPolicy.equals(that.requestPolicy))) &&
	    (this.responsePolicy == that.responsePolicy ||
		(this.responsePolicy != null &&
			this.responsePolicy.equals(that.responsePolicy)))) {
	    return true;
	}

	return false;
    }

    public int hashCode() {
	return moduleClass.hashCode() + options.hashCode();
    }
}
