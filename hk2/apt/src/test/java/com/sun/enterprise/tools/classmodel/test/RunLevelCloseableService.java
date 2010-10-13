package com.sun.enterprise.tools.classmodel.test;

import java.io.Closeable;
import java.io.IOException;

import org.jvnet.hk2.annotations.ContractProvided;
import org.jvnet.hk2.annotations.Service;

@Admin
@Service(name="closeable")
@ContractProvided(Closeable.class)
public class RunLevelCloseableService implements Closeable {

  @Override
  public void close() throws IOException {
  }

}
