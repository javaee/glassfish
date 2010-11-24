package com.sun.hk2.component;

import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.MultiMap;

/**
 * @deprecated Use ConstructorCreator instead
 */
public class ConstructorWomb<T> extends ConstructorCreator<T> {

  public ConstructorWomb(Class<T> type, Habitat habitat, MultiMap<String,String> metadata) {
    super(type, habitat, metadata);
//    singletonScope = habitat.singletonScope;
  }
  
}
