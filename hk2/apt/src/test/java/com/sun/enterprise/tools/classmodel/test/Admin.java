package com.sun.enterprise.tools.classmodel.test;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(TYPE)
@org.jvnet.hk2.annotations.RunLevel(3)
public @interface Admin {}
