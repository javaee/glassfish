package com.sun.ejb.containers.interceptors;

import com.sun.ejb.containers.BaseContainer;

/**
 * @author Mahesh Kannan
 *         Date: Mar 10, 2008
 */
class CallbackChainImpl {

    protected BaseContainer container;
    protected CallbackInterceptor[] interceptors;
    protected int size;

    CallbackChainImpl(BaseContainer container,
                      CallbackInterceptor[] interceptors) {
        this.container = container;
        this.interceptors = interceptors;
        this.size = (interceptors == null) ? 0 : interceptors.length;
    }

    public Object invokeNext(int index, CallbackInvocationContext invContext)
            throws Throwable {

        Object result = null;

        if (index < size) {
            result = interceptors[index].intercept(invContext);
        }

        return result;
    }

    public String toString() {
        StringBuilder bldr = new StringBuilder("CallbackInterceptorChainImpl");
        for (CallbackInterceptor inter : interceptors) {
            bldr.append("\n\t\t").append(inter);
        }

        return bldr.toString();
    }
}
