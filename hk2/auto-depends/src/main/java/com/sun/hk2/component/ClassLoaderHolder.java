package com.sun.hk2.component;

/**
 * Contract representing holders for a ClassLoader
 * 
 * @author Jeff Trent
 * 
 * @since 3.1
 */
public interface ClassLoaderHolder {

  ClassLoader getClassLoader();
  
}
