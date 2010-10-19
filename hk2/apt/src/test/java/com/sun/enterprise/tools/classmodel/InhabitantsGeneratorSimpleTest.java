package com.sun.enterprise.tools.classmodel;

import static org.junit.Assert.*;

import org.glassfish.hk2.classmodel.reflect.ParsingContext;
import org.glassfish.hk2.classmodel.reflect.Types;
import org.junit.Test;
import org.jvnet.hk2.component.classmodel.ClassPath;
import org.jvnet.hk2.component.classmodel.InhabitantsParsingContextGenerator;

/**
 * See {@link InhabitantsGeneratorTest} in apt-test for the majority
 * of functional testing.
 *  
 * @author Jeff Trent
 */
public class InhabitantsGeneratorSimpleTest {

  /**
   * Testing a classpath that only has a single invalid entry
   */
  @Test
  public void testBogusClassPath() {
    ClassPath classPath = ClassPath.create(null, "target/test-classes/test.xml");
    InhabitantsGenerator generator = new InhabitantsGenerator(null, classPath, classPath);

    InhabitantsParsingContextGenerator ipcGen = generator.getContextGenerator();
    ParsingContext pc = ipcGen.getContext();
    assertNotNull(pc);
    
    Types types = pc.getTypes();
    assertEquals("all types", 0, types.getAllTypes().size());
  }
  
}
