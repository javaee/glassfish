/*
 *  Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 */
package org.glassfish.hk2.xml.lifecycle.config;

import java.beans.PropertyVetoException;

import javax.xml.bind.annotation.XmlAttribute;

//import javax.validation.Payload;
//import javax.validation.constraints.NotNull;

// @ReferenceConstraint(skipDuringCreation=false, payload=PartitionRef.class)
public interface PartitionRef extends PropertyBag, Payload, Auditable {
	
  /**
   * Id of the referenced partition.
   *
   * @return name
   */
  @XmlAttribute(required=true /*, key=true */)
  // @NotNull
  // @ReferenceConstraint.RemoteKey(message="{resourceref.invalid.configref}", type=Partition.class)
  public String getId();
  public void setId(String value) throws PropertyVetoException;

  /**
   * Name of the runtime of the referenced partition.
   *
   * @return name
   */
  @XmlAttribute(required=true /* , key=false */)
  // @NotNull
  // @ReferenceConstraint.RemoteKey(message="{resourceref.invalid.configref}", type=Runtime.class)
  public String getRuntimeRef();
  public void setRuntimeRef(String value) throws PropertyVetoException;

  /*
  @DuckTyped
  Runtime getRuntime();

  @DuckTyped
  Environment getEnvironment();

  class Duck {

    public static Runtime getRuntime(final PartitionRef partitionRef) {
      LifecycleConfigBean bean = (LifecycleConfigBean) Dom.unwrap(partitionRef);
      ServiceLocator serviceLocator = bean.getHabitat();
      return serviceLocator.getService(Runtime.class, partitionRef.getRuntimeRef());
    }

    public static Environment getEnvironment(final PartitionRef partitionRef) {
      return partitionRef.getParent(Environment.class);
    }
  }
  */
}
