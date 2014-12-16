/*
 *  Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 */
package org.glassfish.hk2.xml.lifecycle.config;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public interface Associations {

  @XmlElement(name="*")
  List<Association> getAssociations();
  void setAssociations(List<Association> associations);

}
