/*
 *  Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 */
package org.glassfish.hk2.xml.lifecycle.config;

import javax.xml.bind.annotation.XmlAttribute;

public interface Plugin extends Named, Auditable {
  @XmlAttribute
  String getType();
  void setType(String type);

  @XmlAttribute
  String getPath();
  void setPath(String path);

}
