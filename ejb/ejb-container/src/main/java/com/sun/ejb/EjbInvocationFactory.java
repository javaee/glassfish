package com.sun.ejb;

/**
 * @author Mahesh Kannan
 *         Date: Jan 30, 2008
 */
public class EjbInvocationFactory {

    private String compEnvId;

    private Container container;

    public EjbInvocationFactory(String compEnvId, Container container) {
        this.compEnvId = compEnvId;
        this.container = container;
    }

    public EjbInvocation create() {
        return new EjbInvocation(compEnvId, container);
    }

    public <C extends ComponentContext> EjbInvocation create(Object ejb, C ctx) {
        EjbInvocation ejbInv = new EjbInvocation(compEnvId, container);
        ejbInv.ejb = ejb;
        ejbInv.context = ctx;

        return ejbInv;
    }
}
