package com.acme;

import javax.ejb.*;
import javax.annotation.*;
import java.util.concurrent.TimeUnit;

@AccessTimeout(value=9876544, unit=TimeUnit.HOURS)
@Lock(LockType.READ)
public   class Super1 /*extends Super2*/ {

	public void super1() {}

	@Lock(LockType.WRITE)
	public void super11() {}

        @AccessTimeout(value=44544, unit=TimeUnit.HOURS)
	private void foobar() {}

        @AccessTimeout(value=44544, unit=TimeUnit.HOURS)
	    public String hello() { return ""; }

    }