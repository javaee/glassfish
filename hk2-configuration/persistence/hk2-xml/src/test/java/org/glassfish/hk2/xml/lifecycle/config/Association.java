/*
 *  Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 */
package org.glassfish.hk2.xml.lifecycle.config;

import javax.xml.bind.annotation.XmlElement;

public interface Association extends Auditable {
  
  @XmlElement(required=true /*, reference=true */)
  Partition1 getPartition1();
  void setPartition1(Partition1 partition);
  
  @XmlElement(required=true /* reference=true */)
  Partition2 getPartition2();
  void setPartition2(Partition2 partition);

}
