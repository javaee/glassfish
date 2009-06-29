package com.acme;

import javax.annotation.*;


@ManagedBean("ManagedBeanNoInt")
public class ManagedBeanNoInt extends ManagedBeanSuper {

    static private int numInstances = 0;

    public int getNumInstances() {
	return numInstances;
    }

    @PostConstruct
    private void init() {
	numInstances++;
    }

    @PreDestroy
    private void destroy() {
    }

}