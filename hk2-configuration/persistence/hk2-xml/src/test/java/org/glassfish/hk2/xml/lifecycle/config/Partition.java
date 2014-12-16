/*
 *  Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 */
package org.glassfish.hk2.xml.lifecycle.config;

import java.beans.PropertyVetoException;

import javax.xml.bind.annotation.XmlAttribute;

//import javax.validation.constraints.NotNull;

public interface Partition extends PropertyBag, Auditable {

  @XmlAttribute(required=true /*, key=true */)
  // @NotNull
  String getId();
  void setId(String id);
  
  @XmlAttribute(required=true /*, key=false */)
  // @NotNull
  String getName();
  void setName(String value) throws PropertyVetoException;

  /*
  @DuckTyped
  Runtime getRuntime();

  class Duck {

    public static Runtime getRuntime(final Partition partition) {
      return partition.getParent(Runtime.class);
    }
  }
  */
}
