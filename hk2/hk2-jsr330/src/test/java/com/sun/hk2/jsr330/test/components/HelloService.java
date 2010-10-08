package com.sun.hk2.jsr330.test.components;

/**
 * Intentionally blending "proprietary" Hk2 annotations along side of
 * jsr-330 standard usage. (see the implementations of this for details).
 * 
 * @author Jeff Trent
 */
public interface HelloService {

  String sayHello();
  
}
