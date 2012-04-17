package com.sun.enterprise.module.bootstrap;

import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ErrorService;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.MultiException;

@Singleton
public class DefaultErrorService implements ErrorService {
	@Override
	public void failureToReify(ActiveDescriptor<?> descriptor,
			Injectee injectee, MultiException me) throws MultiException {
		System.err.println(descriptor);
		System.err.println(injectee);
		me.printStackTrace();
    }
	
}
