package com.acme;

import javax.ejb.*;
import javax.annotation.*;
import javax.interceptor.*;

import javax.inject.Inject;

@Singleton
@Startup
public class SingletonStartupWithInjectedDependent {

    private final DependentWithInjectedDataSource service;

    @Inject
    public SingletonStartupWithInjectedDependent(DependentWithInjectedDataSource service) {
        this.service = service;
	System.out.println("Constructed::SingletonStartupWithInjectedDependent");
    }

    @PostConstruct
    public void init() {
        service.callMe();
        System.out.println("Init::SingletonStartupWithInjectedDependent");
    }

    public String toString() {
	return "SingletonStartupWithInjectedDependent";
    }

}
