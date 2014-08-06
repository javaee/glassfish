/*
 * Copyright (c) 2013, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.jvnet.testing.hk2mockito.fixture.service;

import javax.inject.Inject;
import javax.inject.Provider;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.testing.hk2mockito.fixture.BasicGreetingService;

/**
 *
 * @author Sharmarke Aden
 */
@Service
public class ProviderInjectedGreetingService {

    private final Provider<BasicGreetingService> collaborator;

    @Inject
    ProviderInjectedGreetingService(Provider<BasicGreetingService> collaborator) {
        this.collaborator = collaborator;
    }

    public String greet() {
        return collaborator.get().greet();
    }
}
