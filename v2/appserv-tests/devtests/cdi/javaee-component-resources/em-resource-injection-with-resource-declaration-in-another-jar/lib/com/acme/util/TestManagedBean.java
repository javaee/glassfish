package com.acme.util;

import javax.annotation.ManagedBean;
import javax.inject.Inject;

@ManagedBean
public class TestManagedBean {
    @Inject
    private TestDependentBeanInLib tdbl;
    
    public boolean isInjectionSuccessful(){
        return tdbl != null;
    }

}
