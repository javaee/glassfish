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
package org.jvnet.hk2.component.classmodel;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link ClassPath}.
 * 
 * @author Mason Taube
 * @author Jeff Trent
 */
public class ClassPathTest {

  private static final String JAR = ".jar";
  private static final String TMP_FILE_PREFIX = "hk2";

  private File dir1;

  private File jar1;
  private File jar2;
  private File jar3;

  @Before
  public void createTestFiles() throws Exception {
      jar1 = File.createTempFile(TMP_FILE_PREFIX, JAR);
      jar2 = File.createTempFile(TMP_FILE_PREFIX, JAR);
      jar3 = File.createTempFile(TMP_FILE_PREFIX, JAR);

      dir1 = File.createTempFile(TMP_FILE_PREFIX, null);

      dir1.delete();
      dir1.mkdir();

      jar1.deleteOnExit();
      jar2.deleteOnExit();
      jar3.deleteOnExit();
      dir1.deleteOnExit();
  }

  private void populateTestJar(File jar, Manifest mf) throws Exception {
      JarOutputStream jos = null;

      try {
          jos = new JarOutputStream(new FileOutputStream(jar), mf);
          jos.flush();
      } finally {
          jos.close();
      }
  }

  @Test
  public void testEmptyClasspath() throws Exception {
      Set<String> cpSet;

      ClassPath cpHelper = ClassPath.create(null, "");
      cpSet = cpHelper.getEntries();

      assertTrue("unexpected classpath entry: " + cpSet, cpSet.isEmpty());

      ClassPath cpHelper2 = ClassPath.create(null, (String)null);
      cpSet = cpHelper2.getEntries();

      assertTrue("unexpected classpath entry: " + cpSet, cpSet.isEmpty());
  }
  
  @Test
  public void testEquals() {
    ClassPath cpHelper = ClassPath.create(null, "");
    ClassPath cpHelper2 = ClassPath.create(null, (String)null);
    assertTrue(cpHelper.equals(cpHelper2));
    assertEquals(cpHelper.hashCode(), cpHelper2.hashCode());

    ClassPath cpHelper3 = ClassPath.create(null, true);
    assertFalse(cpHelper.equals(cpHelper3));
  }

  @Test
  public void testFindJarsInClasspathNoManifestClasspath() throws Exception {
      // create some jars with empty manifests
      final Manifest emptyManifest = new Manifest();

      populateTestJar(jar1, emptyManifest);
      populateTestJar(jar2, emptyManifest);
      populateTestJar(jar3, emptyManifest);

      final String classpath = jar1.getAbsolutePath() + File.pathSeparator + jar2.getAbsolutePath()
              + File.pathSeparator + jar3.getAbsolutePath() + File.pathSeparator + dir1.getAbsolutePath();

      ClassPath cpHelper = ClassPath.create(null, classpath);
      Set<String> cpSet = cpHelper.getEntries();

      assertTrue("missing classpath jar", cpSet.contains(jar1.getAbsolutePath()));
      assertTrue("missing classpath jar", cpSet.contains(jar2.getAbsolutePath()));
      assertTrue("missing classpath jar", cpSet.contains(jar3.getAbsolutePath()));
      assertTrue("missing classpath dir", cpSet.contains(dir1.getAbsolutePath()));
  }

  @Test
  public void testFindJarsInClasspathWithManifestClasspath() throws Exception {
      // set up manifests so that jar1 contains a mf classpath entry
      // referencing jar2 which contains a classpath entry referencing jar3.

      // classpath: jar1;dir1
      //             |
      //             +- jar2
      //                  |
      //                  +- jar3
      final Manifest mf = new Manifest();

      mf.getMainAttributes().put(Attributes.Name.CLASS_PATH, jar2.getName());
      mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

      populateTestJar(jar1, mf);

      mf.getMainAttributes().put(Attributes.Name.CLASS_PATH, jar3.getName());

      populateTestJar(jar2, mf);

      populateTestJar(jar3, new Manifest());

      final String classpath = jar1.getAbsolutePath() + File.pathSeparator + dir1.getAbsolutePath();

      ClassPath cpHelper = ClassPath.create(null, classpath);
      Set<String> cpSet = cpHelper.getEntries();

      assertTrue("missing classpath jar", cpSet.contains(jar1.getAbsolutePath()));
      assertTrue("missing classpath jar", cpSet.contains(jar2.getAbsolutePath()));
      assertTrue("missing classpath jar", cpSet.contains(jar3.getAbsolutePath()));
      assertTrue("missing classpath dir", cpSet.contains(dir1.getAbsolutePath()));
  }

  @Test
  public void testFindJarsInClasspathWithLongManifestClasspath() throws Exception {
      // set up manifests so that jar1 contains a mf classpath entry
      // referencing jar2 and jar3. Jar3 contains a mf classpath entry for
      // jar1
      
      //               v------------+
      // classpath: jar1;dir1       |
      //             |              |
      //             +- jar2, jar3--+
      final Manifest mf = new Manifest();

      mf.getMainAttributes().put(Attributes.Name.CLASS_PATH,
              jar2.getName() + "             " + jar3.getName() + "   ");
      mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

      populateTestJar(jar1, mf);

      populateTestJar(jar2, new Manifest());
      
      mf.getMainAttributes().put(Attributes.Name.CLASS_PATH, jar1.getName());

      populateTestJar(jar3, mf);

      final String classpath = jar1.getAbsolutePath() + File.pathSeparator + dir1.getAbsolutePath();
      ClassPath cpHelper = ClassPath.create(null, classpath);
      Set<String> cpSet = cpHelper.getEntries();
      
      assertTrue("missing classpath jar", cpSet.contains(jar1.getAbsolutePath()));
      assertTrue("missing classpath jar", cpSet.contains(jar2.getAbsolutePath()));
      assertTrue("missing classpath jar", cpSet.contains(jar3.getAbsolutePath()));
      assertTrue("missing classpath dir", cpSet.contains(dir1.getAbsolutePath()));
  }
  
}
