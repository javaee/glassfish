/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.enterprise.tools.classmodel;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

/**
 * Functional testing of InhabitantsSorter
 * 
 * @author Jeff Trent
 *
 */
public class InhabitantsSorterTest {

  private static final Logger logger = Logger.getAnonymousLogger();

  /**
   * Verifies sorting behavior and streaming to file
   */
  @Test
  public void testSortingAndIO() throws Exception {
    // setup test
    File testDir = new File(new File("."), "target/test-classes");
    File inputFile = new File(testDir, "META-INF/inhabitants/default");
    String original = InhabitantsGeneratorTest.callMain(inputFile, true, null, null, null);
    original = Utilities.sortInhabitantsDescriptor(original, true);
    
    File outputFile = new File(testDir, "META-INF/inhabitants/filtered");
    outputFile.delete();
    
    try {
      System.setProperty(Constants.PARAM_INHABITANT_SOURCE_FILE, inputFile.getAbsolutePath());
      System.setProperty(Constants.PARAM_INHABITANT_TARGET_FILE, outputFile.getAbsolutePath());
      
      InhabitantsSorter.main(null);
  
      assertTrue(outputFile.exists());
      
      FileInputStream fis = new FileInputStream(outputFile);
      String output = Utilities.sortInhabitantsDescriptor(InhabitantsGeneratorTest.toString(fis), false);
      fis.close();
      
      logger.log(Level.FINE, "original/expected=\n{0}and processed=\n{1}\n", new Object[] {original, output});
      
      assertEquals(original, output);
    } finally {
      System.clearProperty(InhabitantsGenerator.PARAM_INHABITANT_SOURCE_FILE);
      System.clearProperty(InhabitantsGenerator.PARAM_INHABITANT_TARGET_FILE);
    }
  }
  
  /**
   * Verifies raw sorting behavior
   */
  @Test
  public void testSorting() throws Exception {
    String original = "class=rls.test.model.ServiceWithTwoRunLevelAssignments,index=org.jvnet.hk2.annotations.RunLevel,runLevel=2,runLevel=55,aaa=java.lang.Void";
    String sorted = Utilities.sortInhabitantsDescriptor(original, true);
    String expected = "class=rls.test.model.ServiceWithTwoRunLevelAssignments,index=org.jvnet.hk2.annotations.RunLevel,aaa=java.lang.Void,runLevel=2,runLevel=55\n";
    assertEquals(expected, sorted);
  }
  
  /**
   * Verifies behavior when no source file to process
   */
  @Test
  public void testNothingToProcess() throws Exception {
    // setup test
    File testDir = new File(new File("."), "target/test-classes");
    File inputFile = new File(testDir, "META-INF/inhabitants/bogus");
    File outputDir = new File(testDir, "META-INF/inhabitants/subdir/");
    File outputFile = new File(outputDir, "filtered");
    outputFile.delete();
    
    try {
      // execute main test logic
      System.setProperty(Constants.PARAM_INHABITANT_SOURCE_FILE, inputFile.getAbsolutePath());
      System.setProperty(Constants.PARAM_INHABITANT_TARGET_FILE, outputFile.getAbsolutePath());
      
      InhabitantsSorter.main(null);
  
      assertFalse("output file should not exist", outputFile.exists());
      assertFalse("output directory should not exist", outputDir.exists());
    } finally {
      System.clearProperty(InhabitantsGenerator.PARAM_INHABITANT_SOURCE_FILE);
      System.clearProperty(InhabitantsGenerator.PARAM_INHABITANT_TARGET_FILE);
    }
  }
  
}
