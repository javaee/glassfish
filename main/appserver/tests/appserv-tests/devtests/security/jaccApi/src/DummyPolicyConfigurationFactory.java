package com.sun.s1asdev.security.jaccapi;

import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyContextException;

public class DummyPolicyConfigurationFactory extends PolicyConfigurationFactory {
    public PolicyConfiguration 
            getPolicyConfiguration(String contextID, boolean remove)
    	    throws javax.security.jacc.PolicyContextException {
        return null;
    }

    public boolean inService(String contextID)
            throws javax.security.jacc.PolicyContextException {
        return false;
    }
}
