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
   * Verifies sorting behavior
   */
  @Test
  public void testSorting() throws Exception {
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
      
      logger.log(Level.INFO, "original/expected=\n{0}and processed=\n{1}\n", new Object[] {original, output});
      
      assertEquals(original, output);
    } finally {
      System.clearProperty(InhabitantsGenerator.PARAM_INHABITANT_SOURCE_FILE);
      System.clearProperty(InhabitantsGenerator.PARAM_INHABITANT_TARGET_FILE);
    }
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
