/*
 *  Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 */
package org.glassfish.hk2.xml.lifecycle.config;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import org.jvnet.hk2.annotations.Contract;

@Contract
public interface Associations {

  @XmlElement(name="association")
  void setAssociations(List<Association> associations);
  List<Association> getAssociations();

}
