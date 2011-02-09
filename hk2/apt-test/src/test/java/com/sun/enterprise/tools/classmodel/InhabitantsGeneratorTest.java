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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import org.glassfish.hk2.classmodel.reflect.AnnotationType;
import org.glassfish.hk2.classmodel.reflect.ParsingContext;
import org.glassfish.hk2.classmodel.reflect.Types;
import org.junit.Ignore;
import org.junit.Test;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.InhabitantAnnotation;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.classmodel.ClassPath;
import org.jvnet.hk2.component.classmodel.InhabitantsParsingContextGenerator;

import com.sun.enterprise.tools.InhabitantsDescriptor;

/**
 * Tests the introspective type of InhabitantsGenerator.
 * 
 * Note: to run this in eclipse, you may need to regenerate the project
 * (eclipse:clean eclipse:eclipse) so that the proper jar references are placed
 * in the project.
 * 
 * @author Jeff Trent
 */
public class InhabitantsGeneratorTest {

  private static final Logger logger = Logger.getAnonymousLogger();

//  @Ignore
  @Test
  public void sanityTest() throws Exception {
    ArrayList<File> testDir = getTestClassPathEntries(false);

    ClassPath classPath = ClassPath.create(null, testDir);
    InhabitantsGenerator generator = new InhabitantsGenerator(null, classPath, classPath);

    InhabitantsParsingContextGenerator ipcGen = generator.getContextGenerator();
    ParsingContext pc = ipcGen.getContext();
    assertNotNull(pc);

    Types types = pc.getTypes();

    AnnotationType ia = types.getBy(AnnotationType.class,
        InhabitantAnnotation.class.getName());
    AnnotationType s = types.getBy(AnnotationType.class, Service.class.getName());
    AnnotationType c = types.getBy(AnnotationType.class, Contract.class.getName());

    assertNotNull("@InhabitantAnnotation not found", ia);
    assertNotNull("Service not found", s);
    assertNotNull("@Contract not found", c);
  }

  /**
   * Another sanity type test
   */
  @Ignore // TODO: sanity test disabled because auto-depends is no longer required in the inhabitants file set
  @Test
  public void autoDependsIsRequired() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(out, true);

    PrintStream old = System.err;
    Properties oldSysProps = System.getProperties();
    try {
      System.setErr(ps);
      // ArrayList<File> testDir = getTestClassPathEntries();
      ClassPath classPath = ClassPath.create(null, (ArrayList<File>) null);

      // first, another sanity check
      InhabitantsGenerator generator = new InhabitantsGenerator(null, classPath, classPath);
      // generator.add(testDir);

      InhabitantsParsingContextGenerator ipcGen = generator
          .getContextGenerator();
      ParsingContext pc = ipcGen.getContext();
      assertNotNull(pc);

      Types types = pc.getTypes();
      AnnotationType ia = types.getBy(AnnotationType.class,
          InhabitantAnnotation.class.getName());
      AnnotationType c = types.getBy(AnnotationType.class, Contract.class.getName());

      assertNull("@InhabitantAnnotation not found", ia);
      assertNull("@Contract not found", c);

      // real heart of test starts here
      File testDir = new File(new File("."), "target/test-classes");
      File outputFile = new File(testDir, "META-INF/inhabitants/default");

      System.setProperty(InhabitantsGenerator.PARAM_INHABITANT_TARGET_FILE, outputFile
          .getAbsolutePath());
      System.setProperty(InhabitantsGenerator.PARAM_INHABITANTS_SOURCE_FILES,
          testDir.getAbsolutePath());
      System.setProperty(InhabitantsGenerator.PARAM_INHABITANTS_CLASSPATH,
          testDir.getAbsolutePath());
      InhabitantsGenerator.main(null);

      String errTxt = clean(out.toString());
      assertEquals("ERROR: HK2's auto-depends jar is an expected argument in "
          + InhabitantsGenerator.PARAM_INHABITANTS_SOURCE_FILES + "\n", errTxt);
    } finally {
      System.setErr(old);
      System.clearProperty(InhabitantsGenerator.PARAM_INHABITANT_TARGET_FILE);
      System.clearProperty(InhabitantsGenerator.PARAM_INHABITANTS_SOURCE_FILES);
      System.clearProperty(InhabitantsGenerator.PARAM_INHABITANTS_CLASSPATH);
      System.setProperties(oldSysProps);
      ps.close();
    }
  }

  /**
   * Another sanity type test
   */
//  @Ignore
  @Test
  public void workingClassPathRecommended() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(out, true);

    PrintStream old = System.err;
    Properties oldSysProps = System.getProperties();
    try {
      System.setErr(ps);

      File testDir = new File(new File("."), "target/test-classes");
      File outputFile = new File(testDir, "META-INF/inhabitants/default");

      System.setProperty(InhabitantsGenerator.PARAM_INHABITANT_TARGET_FILE, 
          outputFile.getAbsolutePath());
      System.setProperty(InhabitantsGenerator.PARAM_INHABITANTS_SOURCE_FILES,
          toString(getTestClassPathEntries(false)));
      // System.setProperty(InhabitantsGenerator.PARAM_INHABITANTS_CLASSPATH,
      System.clearProperty(InhabitantsGenerator.PARAM_INHABITANTS_CLASSPATH);
      
      // testDir.getAbsolutePath());
      InhabitantsGenerator.main(null);

      String errTxt = out.toString();
      assertTrue(errTxt + " is unexpected", errTxt
          .startsWith("WARNING: sysprop "
              + InhabitantsGenerator.PARAM_INHABITANTS_CLASSPATH
              + " is missing; defaulting to system classpath"));
    } finally {
      System.setErr(old);
      System.clearProperty(InhabitantsGenerator.PARAM_INHABITANT_TARGET_FILE);
      System.clearProperty(InhabitantsGenerator.PARAM_INHABITANTS_SOURCE_FILES);
      System.clearProperty(InhabitantsGenerator.PARAM_INHABITANTS_CLASSPATH);
      System.setProperties(oldSysProps);
      ps.close();
    }
  }

  /**
   * auto-depends modules is required to be passed into the generator or else an
   * error will occur.
   * 
   * if the "output" only contains code from
   * <code>com/sun/enterprise/tools/classmodel/test/local</code> then there is a
   * problem in auto-depends filtering out jars with habitats during
   * introspection. test-inhabitant-generator.jar should also be present.
   * 
   * this test looks at the case where the classpath is only partially specified
   * resulting in a reduced view of the inhabitants.
   */
//  @Ignore
  @Test
  public void testReducedScopeHabitatFileGeneration() throws IOException {
    ArrayList<File> testDir = getTestClassPathEntries(false);

    InhabitantsDescriptor descriptor = new InhabitantsDescriptor();
    descriptor.enableDateOutput(false);

    ClassPath classPath = ClassPath.create(null, testDir);
    InhabitantsGenerator generator = new InhabitantsGenerator(descriptor,
        classPath, classPath);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintWriter writer = new PrintWriter(out);

    generator.generate(writer);
    writer.close();

    String output = clean(out.toString());
    String expected = expected(false);
    assertNotNull(output);
    logger.info("Output: \n" + output);
    logger.info("Expected: \n" + expected);
    logger.info("testDir is: " + testDir);
    assertEquals("output (see javadoc comments):\n" + output, expected, output);
  }

  /**
   * this test, akin to the above, looks at the case where the classpath is
   * fully specified resulting in all of the correctly modeled inhabitants.
   */
//  @Ignore
  @Test
  public void testFullHabitatFileGeneration() throws IOException {
    ArrayList<File> inhabitantSources = getTestClassPathEntries(false);
    ArrayList<File> testingClassPath = getTestClassPathEntries(true);

    InhabitantsDescriptor descriptor = new InhabitantsDescriptor();
    descriptor.enableDateOutput(false);

    ClassPath inhabitantSourcesClassPath = ClassPath.create(null, inhabitantSources);
    ClassPath workingClassPath = ClassPath.create(null, testingClassPath);
    InhabitantsGenerator generator = new InhabitantsGenerator(descriptor,
        inhabitantSourcesClassPath, workingClassPath);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintWriter writer = new PrintWriter(out);

    generator.generate(writer);
    writer.close();

    String output = clean(out.toString());
    String expected = expected(true);
    assertNotNull(output);
    logger.info("Output: \n" + output);
    logger.info("Expected: \n" + expected);
    assertEquals("output (see javadoc comments):\n" + output + "\nexpected:\n" + expected +
        "\ninhabitant sources=" + inhabitantSourcesClassPath + "\n\nworking classpath=" + workingClassPath + "\n",
        expected, output);
  }

  /**
   * If {@link #testHabitatFileGeneration()} fails, then this guy will also
   * always fail.
   */
//  @Ignore
  @Test
  public void testMain() throws Exception {
    File testDir = new File(new File("."), "target/test-classes");
    File outputFile = new File(testDir, "META-INF/inhabitants/default");

    String output = callMain(outputFile, true, null, null, null);
    String expected = expected(true);
    assertEquals(output + " was not found to contain:\n" + expected, expected, output);
  }

  /**
   * If there are no inhabitants then there should be no generated file
   */
//  @Ignore
  @Test
  public void testMainWithNoInhabitants() throws Exception {
    File testDir = new File(new File("."), "target/test-classes");
    File outputDir = new File(testDir, "META-INF/inhabitants/");
    File outputFile = new File(outputDir, "default");

    String inhabitantSources = toString(getAutoDependsClassPathEntries());
    String workingClassPath = toString(getTestClassPathEntries(true));

    String output = callMain(outputFile, false, inhabitantSources, workingClassPath, true);
    assertNull(output);
    
    if (outputFile.exists()) {
      FileInputStream fis = new FileInputStream(outputFile);
      try {
        assertFalse("expect NOT to find: " + outputFile
            + "; but did containing:\n[" + toString(fis) + "]\nthis is surprising because\ninhabitant sources=" + inhabitantSources
            + "\n and working classpath=" + workingClassPath + "\n", outputFile.exists());
        assertFalse("output directory should not exist", outputDir.exists());
      } finally {
        fis.close();
      }
    }
  }

  /**
   * strictly for sort testing
   */
//  @Ignore
  @Test
  public void testMainWithSorting() throws Exception {
    File testDir = new File(new File("."), "target/test-classes");
    File outputFile = new File(testDir, "META-INF/inhabitants/default");

    String output = callMain(outputFile, true, null, null, true);
    String expected = Utilities.sortInhabitantsDescriptor(expected(true), true);
    assertTrue(output + " was not found to contain:\n" + expected, output.contains(expected));
  }
  
  /**
   * same test as {@link #testMain()} with a reversed classpath
   */
//  @Ignore
  @Test
  public void testMainWithReversedClassPath() throws Exception {
    File testDir = new File(new File("."), "target/test-classes");
    File outputFile = new File(testDir, "META-INF/inhabitants/default");

    ArrayList<File> testClassPathEntries = getTestClassPathEntries(true);
    Collections.reverse(testClassPathEntries);
    String workingClassPath = toString(testClassPathEntries);

    String output = callMain(outputFile, true, null, workingClassPath, true);
    String expected = expected(true, true, true);
    assertEquals(output + " was not found to contain:\n" + expected, expected, output);
  }

  static String callMain(File outputFile, boolean expectOutput, String inhabitantSources, String workingClassPath, Boolean sort) throws Exception {
    outputFile.delete();
    
    inhabitantSources = (null == inhabitantSources) ? toString(getTestClassPathEntries(false)) : inhabitantSources;
    workingClassPath = (null == workingClassPath) ? toString(getTestClassPathEntries(true)) : workingClassPath;
    
    System.setProperty(InhabitantsGenerator.PARAM_INHABITANT_TARGET_FILE, outputFile.getAbsolutePath());
    System.setProperty(InhabitantsGenerator.PARAM_INHABITANTS_SOURCE_FILES, inhabitantSources);
    System.setProperty(InhabitantsGenerator.PARAM_INHABITANTS_CLASSPATH, workingClassPath);
    if (null != sort) {
      System.setProperty(InhabitantsGenerator.PARAM_INHABITANTS_SORTED, sort.toString());
    }
    InhabitantsGenerator.main(null);

    assertEquals("expect output:" + outputFile, expectOutput, outputFile.exists());

    if (!outputFile.exists()) {
      return null;
    }
    
    FileInputStream fis = new FileInputStream(outputFile);
    try {
      String output = Utilities.sortInhabitantsDescriptor(toString(fis), false);
      return output;
    } finally {
      fis.close();
  
      System.clearProperty(InhabitantsGenerator.PARAM_INHABITANT_TARGET_FILE);
      System.clearProperty(InhabitantsGenerator.PARAM_INHABITANTS_SOURCE_FILES);
      System.clearProperty(InhabitantsGenerator.PARAM_INHABITANTS_CLASSPATH);
      System.clearProperty(InhabitantsGenerator.PARAM_INHABITANTS_SORTED);
    }
  }

  static String toString(InputStream is) throws IOException {
    StringBuilder sb = new StringBuilder();
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    String line;
    while (null != (line = reader.readLine())) {
      sb.append(line).append("\n");
    }
    return sb.toString();
  }

  /**
   * Compares APT generation to class-model, introspection generation.
   */
//  @Ignore
  @Test
  public void testAgainstAptGenerator() throws Exception {
    // generate the habitat file
    {
      File testDir = new File(new File("."), "target/test-classes");
      File outputFile = new File(testDir, "META-INF/inhabitants/default");

      String output = callMain(outputFile, true, null, null, true);
      assertNotNull(output);
    }

    // test it
    ClassLoader cl = new URLClassLoader(toURL(getTestClassPathEntries(false)),
        new ClassLoader(null) {
          @Override
          protected URL findResource(String name) {
            return null;
          }
        });

    Enumeration<URL> en = cl.getResources("META-INF/inhabitants/default");
    int count = 0;
    while (en.hasMoreElements()) {
      URL url = en.nextElement();
      String name = url.getPath().toString();
      if (!name.contains("test-rls")) {
        count++;

        InputStream is = (InputStream) url.getContent();
        String output = Utilities.sortInhabitantsDescriptor(toString(is), true);
        is.close();

        boolean fromClassModelIntrospection = 
          name.contains("apt-test/target/test-classes/META-INF/inhabitants/default");
        String expected = expected(true, fromClassModelIntrospection, true);

        assertEquals(count + ": expected " + url + " to contain output:\n"
            + expected + "\nbut instead was:\n" + output
            + "\nfrom introspection: " + fromClassModelIntrospection, expected,
            output);
      }
    }

    assertEquals("inhabitants files found", 2, count);
  }

  @SuppressWarnings("deprecation")
  private URL[] toURL(ArrayList<File> testClassPathEntries) throws IOException {
    URL urls[] = new URL[testClassPathEntries.size()];
    int i = 0;
    for (File file : testClassPathEntries) {
      urls[i++] = file.toURL();
    }
    return urls;
  }

  private String clean(String string) throws IOException {
    return Utilities.sortInhabitantsDescriptor(string.replace("\r", ""), false);
  }

  static String toString(ArrayList<File> list) {
    StringBuilder sb = new StringBuilder();
    for (File file : list) {
      if (sb.length() > 0) {
        sb.append(File.pathSeparator);
      }
      sb.append(file.getAbsolutePath());
    }
    return sb.toString();
  }

  static ArrayList<File> getTestClassPathEntries(boolean worldView) {
    ArrayList<File> entries = new ArrayList<File>();

    ClassPath classpath = ClassPath.create(null, false);
    Set<String> cpSet = classpath.getEntries();
    for (String entry : cpSet) {
      if (false) {
        entries.add(new File(entry));
      } else {
        if (entry.contains("test-inhabitant-generator")) {
          entries.add(new File(entry));
        } else if (entry.contains("auto-depends-")
            && !entry.contains("auto-depends-plugin")) {
          if (worldView) {
            entries.add(new File(entry));
          } else if (!entry.contains("test-inhabitant-gen-ifaces")) {
            entries.add(new File(entry));
          }
        } else if (entry.contains("test-classes")) {
          entries.add(new File(entry));
        } else if (entry.contains("hk2-core") && worldView) {
          entries.add(new File(entry));
        }
      }
    }

    if (entries.isEmpty()) {
      throw new RuntimeException("can't find test-classes in " + cpSet);
    }

    logger.info("test classpath for worldview=" + worldView + " is "
        + ClassPath.create(null, entries));

    return entries;
  }

  static ArrayList<File> getAutoDependsClassPathEntries() {
    ArrayList<File> entries = new ArrayList<File>();

    ClassPath classpath = ClassPath.create(null, false);
    Set<String> cpSet = classpath.getEntries();
    for (String entry : cpSet) {
      if (entry.contains("auto-depends-")
          && !entry.contains("auto-depends-test")) {
        entries.add(new File(entry));
      }
    }

    if (entries.isEmpty()) {
      throw new RuntimeException("can't find test-classes in " + cpSet);
    }

    logger.info("auto-depends classpath is " + entries);

    return entries;
  }

  static ArrayList<File> getLocalModuleClassPathEntry() {
    ArrayList<File> entries = new ArrayList<File>();

    ClassPath classpath = ClassPath.create(null, false);
    Set<String> cpSet = classpath.getEntries();
    for (String entry : cpSet) {
      if (entry.contains("apt-test")) {
        entries.add(new File(entry));
      }
    }

    if (entries.isEmpty()) {
      throw new RuntimeException("can't find test-classes in " + cpSet);
    }

    logger.info("this module's classpath is " + entries);

    return entries;
  }
  
  static String expected(boolean worldViewClassPath) throws IOException {
    return expected(worldViewClassPath, true, false);
  }

  static String expected(boolean worldViewClassPath, boolean fromClassModel, boolean sort) throws IOException {
    StringBuilder sb = new StringBuilder();

    sb.append("class=com.sun.enterprise.tools.classmodel.test.RunLevelCloseableService,index=java.io.Closeable:closeable,index=org.jvnet.hk2.annotations.RunLevel,runLevel=3\n");
    sb.append("class=com.sun.enterprise.tools.classmodel.test.AService,index=com.sun.enterprise.tools.classmodel.test.AContract:aservice,a=1,b=2\n");
    sb.append("class=com.sun.enterprise.tools.classmodel.test.FactoryForCService,index=org.jvnet.hk2.annotations.FactoryFor:com.sun.enterprise.tools.classmodel.test.CService\n");
    sb.append("class=com.sun.enterprise.tools.classmodel.test.CService\n");
    sb.append("class=com.sun.enterprise.tools.classmodel.test.BService,index=com.sun.enterprise.tools.classmodel.test.BContract\n");
    
    // this demonstrates how invalid habitat files can be formed if the working classpath is not correct
    if (worldViewClassPath) {
      // world view (or working) classpath has full visibility so that class-model generates
      // the true habitat
      sb.append("class=com.sun.enterprise.tools.classmodel.test.ServiceWithExternalContract,index=com.sun.enterprise.tools.classmodel.test.external.ExternalContract\n");
      sb.append("class=com.sun.enterprise.tools.classmodel.test.ServiceWithAbstractBaseHavingExternalContract,index=com.sun.enterprise.tools.classmodel.test.external.ExternalContract\n");
      sb.append("class=com.sun.enterprise.tools.classmodel.test.RunLevelCloseableServiceWithExternalAnnotation,index=org.jvnet.hk2.annotations.RunLevel,runLevel=3\n");
      sb.append("class=com.sun.enterprise.tools.classmodel.test.JDBCService,index=com.sun.enterprise.tools.classmodel.test.external.ServerService:jdbc,index=org.jvnet.hk2.annotations.RunLevel,runLevel=2\n");
    } else {
      // without world-view, the external contracts in the
      // inhabitants-gen-ifaces jar are not considered
      sb.append("class=com.sun.enterprise.tools.classmodel.test.ServiceWithExternalContract\n");
      sb.append("class=com.sun.enterprise.tools.classmodel.test.ServiceWithAbstractBaseHavingExternalContract\n");
      sb.append("class=com.sun.enterprise.tools.classmodel.test.RunLevelCloseableServiceWithExternalAnnotation\n");
      sb.append("class=com.sun.enterprise.tools.classmodel.test.JDBCService\n");
    }

    if (fromClassModel) {
      sb.append("class=com.sun.enterprise.tools.classmodel.test.local.LocalServiceInTestDir,index=java.io.Closeable\n");
      sb.append("class=rls.test.model.ServiceOtherToY,index=org.jvnet.hk2.annotations.RunLevel,runLevel=-1\n");
      sb.append("class=rls.test.model.ServiceDerivedX,index=rls.test.model.ContractX:derived,index=org.jvnet.hk2.annotations.RunLevel,runLevel=-1\n");
      sb.append("class=rls.test.model.ServiceYSpecial,index=rls.test.model.ContractY\n");
      sb.append("class=rls.test.infra.MultiThreadedInhabitantActivator,index=org.jvnet.hk2.component.InhabitantActivator\n");
      sb.append("class=rls.test.model.ServiceBaseX,index=rls.test.model.ContractX:base,index=org.jvnet.hk2.annotations.RunLevel,runLevel=-1\n");
      sb.append("class=rls.test.model.ServiceY1,index=rls.test.model.ContractY,index=org.jvnet.hk2.annotations.RunLevel,runLevel=-1\n");
      sb.append("class=rls.test.model.ServiceY2,index=rls.test.model.ContractY,index=org.jvnet.hk2.annotations.RunLevel,runLevel=-1\n");
      sb.append("class=rls.test.model.ServiceZ\n");
      sb.append("class=rls.test.infra.RandomInhabitantSorter,index=org.jvnet.hk2.component.InhabitantSorter\n");
      if (worldViewClassPath) {
        sb.append("class=rls.test.RlsTest,index=com.sun.enterprise.module.bootstrap.ModuleStartup\n");
        sb.append("class=test1.Start,index=com.sun.enterprise.module.bootstrap.ModuleStartup\n");
        sb.append("class=com.sun.enterprise.tools.classmodel.test.MyModuleStartup,index=com.sun.enterprise.tools.classmodel.test.MyBaseModuleStartupContract:startup,index=com.sun.enterprise.module.bootstrap.ModuleStartup:startup\n");
      } else {
        sb.append("class=rls.test.RlsTest\n");
        sb.append("class=test1.Start\n");
        sb.append("class=com.sun.enterprise.tools.classmodel.test.MyModuleStartup,index=com.sun.enterprise.tools.classmodel.test.MyBaseModuleStartupContract:startup\n");
      }
    } else {
      // TODO: core is not part of the classpath so the habitat is correct when
      // the classpath is not right - expected unfortunately
      sb.append("class=test1.Start,index=com.sun.enterprise.module.bootstrap.ModuleStartup\n");
      sb.append("class=com.sun.enterprise.tools.classmodel.test.MyModuleStartup,index=com.sun.enterprise.module.bootstrap.ModuleStartup:startup,index=com.sun.enterprise.tools.classmodel.test.MyBaseModuleStartupContract:startup\n");
    }

    return Utilities.sortInhabitantsDescriptor(sb.toString(), sort);
  }

}
