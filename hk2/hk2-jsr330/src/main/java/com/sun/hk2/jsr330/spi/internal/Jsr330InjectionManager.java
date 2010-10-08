package com.sun.hk2.jsr330.spi.internal;

import java.lang.reflect.Method;

import org.jvnet.hk2.component.InjectionManager;

/**
 * Custom InjectionManager for Jsr-330 special handling
 * 
 * @author Jeff Trent
 * 
 * @since 3.1
 */
public class Jsr330InjectionManager extends InjectionManager {

  @Override
  protected boolean allowInjection(Method method, Class<?>[] paramTypes) {
    // let it all ride on black
    return true;
  }
  
  @Override
  protected void error_InjectMethodIsNotVoid(Method method) {
    // yeah, so what
  }
  
}
