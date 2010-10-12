package com.sun.enterprise.tools.classmodel;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Set;

import org.junit.Test;
import org.jvnet.hk2.component.classmodel.ClassPathHelper;

/**
 * Tests the introspective type of InhabitantsGenerator.
 * 
 * @author Jeff Trent
 */
public class InhabitantsGeneratorTest {

  @Test
  public void testEnv() throws IOException {
    ArrayList<File> testDir = getTestClassPathEntries();
    
    InhabitantsGenerator ipcGen = new InhabitantsGenerator();
    ipcGen.add(testDir);
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintWriter writer = new PrintWriter(out);
    
    ipcGen.generate(writer, null);
    writer.close();
    
    String output = out.toString();
    assertNotNull(output);
  }

  public ArrayList<File> getTestClassPathEntries() {
    ArrayList<File> entries = new ArrayList<File>();
    
    ClassPathHelper classpath = ClassPathHelper.create(null, false);
    Set<String> cpSet = classpath.getEntries();
    for (String entry : cpSet) {
      if (entry.contains("test-classes")) {
        entries.add(new File(entry));
//      } else if (entry.contains("auto-depends")) {
//        entries.add(new File(entry));
      }
//      entries.add(new File(entry));
    }
    
    if (entries.isEmpty()) {
      throw new RuntimeException("can't find tes-classes in " + cpSet);
    }

    System.out.println("classpath is " + entries);
    
    return entries;
  }
}
