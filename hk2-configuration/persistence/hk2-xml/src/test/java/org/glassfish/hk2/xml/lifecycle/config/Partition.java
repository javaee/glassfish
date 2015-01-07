/*
 *  Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 */
package org.glassfish.hk2.xml.lifecycle.config;

import java.beans.PropertyVetoException;

import javax.xml.bind.annotation.XmlAttribute;

import org.glassfish.hk2.xml.api.annotations.XmlIdentifier;
import org.jvnet.hk2.annotations.Contract;

//import javax.validation.constraints.NotNull;

@Contract
public interface Partition extends PropertyBag, Auditable {

  @XmlAttribute(required=true /*, key=true */)
  // @NotNull
  void setId(String id);
  String getId();
  
  
  @XmlAttribute(required=true /*, key=false */)
  @XmlIdentifier
  // @NotNull
  void setName(String value) throws PropertyVetoException;
  String getName();

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
