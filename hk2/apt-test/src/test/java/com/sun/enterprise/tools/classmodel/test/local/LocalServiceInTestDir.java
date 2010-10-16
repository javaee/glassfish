package com.sun.enterprise.tools.classmodel.test.local;

import java.io.Closeable;
import java.io.IOException;

import org.jvnet.hk2.annotations.ContractProvided;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.tools.classmodel.InhabitantsGeneratorTest;

/**
 * The intent is for this class to be loaded by introspection of the test directory.
 * 
 * @see InhabitantsGeneratorTest
 * 
 * @author Jeff Trent
 */
@Service
@ContractProvided(Closeable.class)
public class LocalServiceInTestDir implements Closeable {

  @Override
  public void close() throws IOException {
    // TODO Auto-generated method stub
    
  }

}
