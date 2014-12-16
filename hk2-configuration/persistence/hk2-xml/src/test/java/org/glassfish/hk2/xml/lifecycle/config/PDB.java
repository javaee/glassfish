/*
 *  Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 */
package org.glassfish.hk2.xml.lifecycle.config;

import javax.xml.bind.annotation.XmlAttribute;

//import javax.validation.Payload;

// @ReferenceConstraint(skipDuringCreation=false, payload=PDB.class)
public interface PDB extends Named, Payload, Auditable {

  @XmlAttribute
  String getId();

  void setId(String id);

  @XmlAttribute
  String getPdbStatus();

  void setPdbStatus(String status);

  /*
  @DuckTyped
  Service getService();

  class Duck {
    public static Service getService(final PDB pdb) throws TransactionFailure {
      return pdb.getParent(Service.class);
    }
  }
  */
}
