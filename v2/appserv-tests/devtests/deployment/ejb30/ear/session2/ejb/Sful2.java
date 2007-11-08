/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1asdev.ejb.ejb30.hello.session2;

import javax.ejb.Remote;

@Remote
public interface Sful2
{
    public String hello();

    // Associated with an @Remove method that has retainIfException=true.    
    // If argument is true, the method will throw an exception, which should
    // keep the bean from being removed.  If argument is false, the bean
    // should stll be removed.
    public void removeRetainIfException(boolean throwException) 
        throws Exception;

    // Associated with an @Remove method that has retainIfException=false.    
    // Whether the argument is true or false, the bean should still be
    // removed.
    public void removeNotRetainIfException(boolean throwException) 
        throws Exception;

    // Associated with an @Remove method that has retainIfException=true
    // and throws a system exception if param = true.
    // retainIfException only applies to application exceptions, so
    // this bean should always be removed.
    public void removeMethodThrowSysException(boolean throwException);
        
}
