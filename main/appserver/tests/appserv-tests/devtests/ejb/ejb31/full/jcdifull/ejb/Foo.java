package com.acme;

import javax.enterprise.context.*;

@RequestScoped
public class Foo {

    static int count = 0;

    int id = 0;

    public Foo() {
	id = count;
	count++;
    }

    public String toString() {
	return "Foo" + id;
    }

}