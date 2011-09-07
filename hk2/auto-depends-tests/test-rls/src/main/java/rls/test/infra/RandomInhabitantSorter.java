/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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
package rls.test.infra;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.InhabitantSorter;

@Service
public class RandomInhabitantSorter implements InhabitantSorter {
  private static boolean called;
  
  private final static String DO_RANDOM = "com.oracle.hk2.debug.RandomizeServerServices";
  private final static String DO_RANDOM_SEED_FILE = "com.oracle.hk2.debug.RandomizeServerServices.filename";
  private final static String DO_RANDOM_SEED_DEFAULT_FILE = "target/test-classes/RandomServerServiceSeed";
  private final static String DO_RANDOM_SEED = "com.oracle.hk2.debug.RandomizeServerServices.seed";
  private final static int HIGH_ORDER_BITMASK = 0x7fffffff;
  
  private final boolean doRandom =
    System.getProperty(DO_RANDOM, Boolean.TRUE.toString()).
    equalsIgnoreCase(Boolean.TRUE.toString());
  
  private final Random random;
  
  public RandomInhabitantSorter() {
    if (!doRandom) {
      random = null;
      return;
    }
    
    String randomFileString =
      System.getProperty(DO_RANDOM_SEED_FILE, DO_RANDOM_SEED_DEFAULT_FILE);
    File randomFile = new File(randomFileString);
    
    long randomSeed;
    
    boolean writeOutputFile = true;
    String userPropertySeed = System.getProperty(DO_RANDOM_SEED);
    if (userPropertySeed != null) {
      // The user property seed trumps all
      try {
        randomSeed = Long.parseLong(userPropertySeed);
      }
      catch (NumberFormatException nfe) {
        randomSeed = System.currentTimeMillis();
        
        System.out.println(
            "Could not parse the user random seed property whose value is " +
            userPropertySeed + ".  Using seed " + randomSeed);
      }
    }
    else if (randomFile.exists()) {
      try {
        randomSeed = readRandomFile(randomFile);
        writeOutputFile = false;
      }
      catch (IOException ioe) {
        randomSeed = System.currentTimeMillis();
        
        System.err.println("Could not read server service randomizer file " +
          randomFile.getAbsolutePath() + ".  The seed used in this run is " +
          randomSeed);
      }
    }
    else {
      // No random file or property, generate a "uniquish" random number
      randomSeed = System.currentTimeMillis();
    }
    
    random = new Random(randomSeed);
    
    System.out.println("Warning:  The order in which the server services are started has been randomized " +
        "with seed " + randomSeed + ".  The seed can be found in file " + randomFile.getAbsolutePath());
    
    if (!writeOutputFile) return;  // Same file, no need to re-write it
    
    try {
      writeNewRandomFile(randomFile, randomSeed);
    }
    catch (IOException ioe) {
      System.err.println("Could not write server service randomizer file " +
        randomFile.getAbsolutePath() + ".  The seed used in this run is " +
        randomSeed);
    }
  }
  
  /**
   * Reads the randomizer file and returns the number found within
   * 
   * @param randomFile The file to read
   * @return The number found in the file
   * @throws IOException If the file could not be read for some reason
   */
  private static long readRandomFile(File randomFile) throws IOException {
    long retVal;
    
    if (!randomFile.canRead()) {
      retVal = System.currentTimeMillis();
      
      System.err.println("Could not read server service randomizer file " +
          randomFile.getAbsolutePath() + ".  The seed used in this run is " +
          retVal);
      
      return retVal;
    }
    
    BufferedReader reader = new BufferedReader(new FileReader(randomFile));
    try {
      String line = reader.readLine();
    
      try {
        retVal = Long.parseLong(line);
      }
      catch (NumberFormatException nfe) {
        retVal = System.currentTimeMillis();
      
        System.err.println("Could not parse the data server service randomizer file " +
          randomFile.getAbsolutePath() + ".  The seed used in this run is " +
          retVal);
      }
    }
    finally {
      reader.close();
    }
    
    return retVal;
  }
  
  /**
   * Writes out the seed used in this run to a file
   * @param randomFile The file to write the seed out to
   * @param seed The seed used in this run
   * @throws IOException If we could not write the file
   */
  private static void writeNewRandomFile(File randomFile, long seed) throws IOException {
    if (randomFile.exists()) {
      randomFile.delete();
    }
    
    randomFile.createNewFile();
    
    PrintWriter writer = null;
    try {
      writer = new PrintWriter(randomFile);
      writer.println("" + seed);
    }
    finally {
      if (writer != null) writer.close();
    }
  }

  /**
   * @see org.jvnet.hk2.component.InhabitantSorter#sort(java.util.List)
   */
  @Override
  public List<Inhabitant<?>> sort(List<Inhabitant<?>> arg0) {
    setCalled();

    if (!doRandom) return arg0;
    if (arg0 == null || arg0.size() <= 0) return arg0;
    
    List<Inhabitant<?>> original = new LinkedList<Inhabitant<?>>(arg0);
    List<Inhabitant<?>> retVal = new LinkedList<Inhabitant<?>>();
    
    while (original.size() > 0) {
      int removeMe = (random.nextInt() & HIGH_ORDER_BITMASK) % original.size();
      
      retVal.add(original.remove(removeMe));
    }
    
    return retVal;
  }

  private static void setCalled() {
      called = true;
  }
  
  public static boolean wasCalled() {
      return called;
  }
  
}
