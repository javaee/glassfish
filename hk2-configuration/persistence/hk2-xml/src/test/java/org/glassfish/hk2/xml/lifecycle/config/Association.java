/*
 *  Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 */
package org.glassfish.hk2.xml.lifecycle.config;

import javax.xml.bind.annotation.XmlElement;

public interface Association extends Auditable {
  
  @XmlElement(required=true /*, reference=true */)
  Partition getPartition1();
  void setPartition1(Partition partition);
  
  @XmlElement(required=true /* reference=true */)
  Partition getPartition2();
  void setPartition2(Partition partition);

}
