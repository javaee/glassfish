package com.acme;

import javax.ejb.*;
import javax.annotation.*;
import javax.interceptor.*;

import javax.inject.Inject;

public class DependentWithInjectedDataSource {

    @Resource(name="jdbc/__default")
    private javax.sql.DataSource ds;

    public DependentWithInjectedDataSource() {
	System.out.println("Constructed::DependentWithInjectedDataSource");
    }

    @PostConstruct
    public void init() {
        if (ds == null) {
            throw new IllegalStateException("ds is null in DependentWithInjectedDataSource");
        }
        System.out.println("Init::DependentWithInjectedDataSource");
    }

    public String toString() {
	return "DependentWithInjectedDataSource";
    }

    /**
     * This is here to ensure a true instance of this
     * object must be created
     */
    public void callMe() {
    }

}
