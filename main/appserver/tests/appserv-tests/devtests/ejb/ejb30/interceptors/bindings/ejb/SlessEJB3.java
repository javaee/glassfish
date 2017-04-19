/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.interceptors.bindings;


import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

@Stateless
// total ordering DCBA expressed in .xml
@Interceptors({InterceptorC.class, InterceptorD.class})
public class SlessEJB3 implements Sless3
{

    @ExcludeDefaultInterceptors
    public void dc() {}

    @ExcludeClassInterceptors
    public void ba() {}

    public void dcba() {}

    @ExcludeClassInterceptors
    @Interceptors({InterceptorE.class, InterceptorF.class})
    public void baef() {}

    @ExcludeDefaultInterceptors
    @ExcludeClassInterceptors
    @Interceptors({InterceptorE.class, InterceptorF.class})
    public void ef() {}

    @ExcludeDefaultInterceptors
    @Interceptors({InterceptorE.class, InterceptorF.class})
    public void dcef() {}
    
    @ExcludeDefaultInterceptors
    @ExcludeClassInterceptors
    public void nothing() {}

    @Interceptors({InterceptorE.class, InterceptorF.class})    
    public void dcbaef() {}

    // total ordering overridden in deployment descriptor
    @Interceptors({InterceptorE.class, InterceptorF.class})    
    public void abcdef() {}

    // binding described in deployment descriptor
    public void dcf() {}

    @AroundInvoke
    private Object aroundInvoke(InvocationContext ctx)
    {
        Common.checkResults(ctx);
        return null;
    }
    
}
