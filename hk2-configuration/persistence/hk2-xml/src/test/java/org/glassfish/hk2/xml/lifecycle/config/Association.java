/*
 *  Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 */
package org.glassfish.hk2.xml.lifecycle.config;

import javax.xml.bind.annotation.XmlAttribute;

public interface Association extends Auditable {
  
  @XmlAttribute(required=true /*, reference=true */)
  void setPartition1(Partition partition);
  Partition getPartition1();
  
  @XmlAttribute(required=true /* reference=true */)
  void setPartition2(Partition partition);
  Partition getPartition2();

}
