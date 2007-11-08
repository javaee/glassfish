/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.interceptors.session;

import javax.interceptor.InvocationContext;
import javax.interceptor.AroundInvoke;
import javax.ejb.PrePassivate;
import javax.ejb.PostActivate;
import javax.ejb.EJB;
import javax.annotation.Resource;
import javax.ejb.SessionContext;

public class BaseLifecycleInterceptor
	implements java.io.Serializable {

    protected int basePrePassivateCount = 0;
    protected int basePostActivateCount = 0;


    @EJB Sless sless;
    @Resource SessionContext sessionCtx;

    @PrePassivate
    private void prePassivate(InvocationContext ctx) {
	basePrePassivateCount++;
    }

    @PostActivate
    private void postActivate(InvocationContext ctx) {
	basePostActivateCount++;
    }

}
