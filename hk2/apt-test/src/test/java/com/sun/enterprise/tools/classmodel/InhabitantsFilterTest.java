package com.sun.enterprise.tools.classmodel;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

/**
 * Functional testing of InhabitantsFilter
 * 
 * @author Jeff Trent
 *
 */
public class InhabitantsFilterTest {

  private static final Logger logger = Logger.getAnonymousLogger();

  /**
   * Verifies filtering when essentially nothing gets filtered
   */
  @Test
  public void testMainWithFullVisibility() throws Exception {
    // setup test
    File testDir = new File(new File("."), "target/test-classes");
    File inputFile = new File(testDir, "META-INF/inhabitants/default");
    String original = InhabitantsGeneratorTest.callMain(inputFile, true, null, null, null);
    
    File outputFile = new File(testDir, "META-INF/inhabitants/filtered");
    outputFile.delete();
    
    try {
      // essentially unfiltered
      String inhabitantSources = InhabitantsGeneratorTest.toString(InhabitantsGeneratorTest.getTestClassPathEntries(true));

      // execute main test logic
      System.setProperty(Constants.PARAM_INHABITANT_SOURCE_FILE, inputFile.getAbsolutePath());
      System.setProperty(Constants.PARAM_INHABITANT_TARGET_FILE, outputFile.getAbsolutePath());
      System.setProperty(InhabitantsGenerator.PARAM_INHABITANTS_SOURCE_FILES, inhabitantSources);
      
      InhabitantsFilter.main(null);
  
      assertTrue(outputFile.exists());
      
      FileInputStream fis = new FileInputStream(outputFile);
      String output = Utilities.sortInhabitantsDescriptor(InhabitantsGeneratorTest.toString(fis), false);
      fis.close();
      
      logger.log(Level.INFO, "original/expected=\n{0}and processed=\n{1}\n", new Object[] {original, output});
      
      assertEquals(original, output);
    } finally {
      System.clearProperty(InhabitantsGenerator.PARAM_INHABITANT_SOURCE_FILE);
      System.clearProperty(InhabitantsGenerator.PARAM_INHABITANT_TARGET_FILE);
      System.clearProperty(InhabitantsGenerator.PARAM_INHABITANTS_SOURCE_FILES);
    }
  }

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
      // essentially unfiltered
      String inhabitantSources = InhabitantsGeneratorTest.toString(InhabitantsGeneratorTest.getTestClassPathEntries(true));

      // execute main test logic
      System.setProperty(Constants.PARAM_INHABITANT_SOURCE_FILE, inputFile.getAbsolutePath());
      System.setProperty(Constants.PARAM_INHABITANT_TARGET_FILE, outputFile.getAbsolutePath());
      System.setProperty(InhabitantsGenerator.PARAM_INHABITANTS_SOURCE_FILES, inhabitantSources);
      System.setProperty(InhabitantsGenerator.PARAM_INHABITANTS_SORTED, "true");
      
      InhabitantsFilter.main(null);
  
      assertTrue(outputFile.exists());
      
      FileInputStream fis = new FileInputStream(outputFile);
      String output = Utilities.sortInhabitantsDescriptor(InhabitantsGeneratorTest.toString(fis), false);
      fis.close();
      
      logger.log(Level.INFO, "original/expected=\n{0}and processed=\n{1}\n", new Object[] {original, output});
      
      assertEquals(original, output);
    } finally {
      System.clearProperty(InhabitantsGenerator.PARAM_INHABITANT_SOURCE_FILE);
      System.clearProperty(InhabitantsGenerator.PARAM_INHABITANT_TARGET_FILE);
      System.clearProperty(InhabitantsGenerator.PARAM_INHABITANTS_SOURCE_FILES);
      System.clearProperty(InhabitantsGenerator.PARAM_INHABITANTS_SORTED);
    }
  }
  
  /**
   * Verifies filtering when there is "real" filtering
   */
  @Test
  public void testMainWithRealFiltering() throws Exception {
    // setup test
    File testDir = new File(new File("."), "target/test-classes");
    File inputFile = new File(testDir, "META-INF/inhabitants/default");
    InhabitantsGeneratorTest.callMain(inputFile, true, null, null, null);
    
    File outputFile = new File(testDir, "META-INF/inhabitants/filtered");
    outputFile.delete();
    
    // execute main test logic
    try {
      // "real" filter
      String inhabitantSources = InhabitantsGeneratorTest.toString(InhabitantsGeneratorTest.getLocalModuleClassPathEntry());

      System.setProperty(Constants.PARAM_INHABITANT_SOURCE_FILE, inputFile.getAbsolutePath());
      System.setProperty(Constants.PARAM_INHABITANT_TARGET_FILE, outputFile.getAbsolutePath());
      System.setProperty(InhabitantsGenerator.PARAM_INHABITANTS_SOURCE_FILES, inhabitantSources);
      InhabitantsFilter.main(null);
      assertTrue(outputFile.exists());
      
      FileInputStream fis = new FileInputStream(outputFile);
      String output = InhabitantsGeneratorTest.toString(fis);
      fis.close();
      
      String expected = "class=com.sun.enterprise.tools.classmodel.test.local.LocalServiceInTestDir,index=java.io.Closeable\n";
      
      logger.log(Level.INFO, "expected=\n{0}and processed=\n{1}\n", new Object[] {expected, output});
      
      assertEquals(expected, output);
    } finally {
      System.clearProperty(InhabitantsGenerator.PARAM_INHABITANT_SOURCE_FILE);
      System.clearProperty(InhabitantsGenerator.PARAM_INHABITANT_TARGET_FILE);
      System.clearProperty(InhabitantsGenerator.PARAM_INHABITANTS_SOURCE_FILES);
    }
  }

}
