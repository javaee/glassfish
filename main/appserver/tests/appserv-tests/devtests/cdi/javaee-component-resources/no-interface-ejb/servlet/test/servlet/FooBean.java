package test.servlet;

import javax.inject.Inject;

import test.beans.TestBeanInterface;
import test.beans.artifacts.InjectViaAtEJB;
import test.beans.artifacts.NoInterfaceBeanView;

public class FooBean {

    @Inject
    @InjectViaAtEJB
    @NoInterfaceBeanView
    TestBeanInterface testBeanEJB;
    
    public TestBeanInterface getBean(){
        return testBeanEJB;
    }

}
