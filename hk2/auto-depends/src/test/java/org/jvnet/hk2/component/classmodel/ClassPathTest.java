package org.jvnet.hk2.component.classmodel;

import static org.junit.Assert.assertTrue;

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

      cpHelper = ClassPath.create(null, (String)null);
      cpSet = cpHelper.getEntries();

      assertTrue("unexpected classpath entry: " + cpSet, cpSet.isEmpty());
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
