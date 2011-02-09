/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2011 Oracle and/or its affiliates. All rights reserved.
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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jvnet.hk2.component.Habitat;

/**
 * Helper for creating classpath approximating SE behavior.
 * 
 * @author Jerome Dochez
 * @author Jeff Trent
 */
public abstract class ClassPath {

  private static Logger logger = Logger.getLogger(ClassPath.class.getName());
  
  // classpath is the one that was passed in originally
  public final LinkedHashSet<String> classPathEntries = new LinkedHashSet<String>();
  
  // classpath is expanded to include manifest jar entries, etc.
  public final LinkedHashSet<String> expandedClassPathEntries = new LinkedHashSet<String>();

  /**
   * Creates a ClassPathHelper instance.
   * 
   * @param h reserved for future use
   * @param allowTestClassPath true if surefire.test.class.path is considered
   * 
   * @return the ClassPathHelper
   */
  public static ClassPath create(Habitat h, boolean allowTestClassPath) {
    return new ClassPath(allowTestClassPath) {};
  }
  
  public static ClassPath create(Habitat h, String classPath) {
    return new ClassPath(classPath) {};
  }
  
  public static ClassPath create(Habitat h, Collection<File> classPath) {
    return new ClassPath(classPath) {};
  }
  
  protected ClassPath(boolean allowTestClassPath) {
    String classPath = (allowTestClassPath) ?  System.getProperty("surefire.test.class.path") : null;
    if (null == classPath) {
      classPath = System.getProperty("java.class.path");
    }
    initialize(classPath);
  }
  
  public ClassPath(String classPath) {
    initialize(classPath);
  }

  public ClassPath(Collection<File> classPath) {
    if (null!= classPath) {
      for (File file : classPath) {
        initialize(file.getAbsolutePath());
      }
    }
  }
  
  protected void initialize(String classPath) {
    if (classPath != null) {
      String[] filenames = classPath.split(File.pathSeparator);

      for (String filename : filenames) {
        if (!filename.equals("")) {
          final File classpathEntry = new File(filename);
          logger.log(Level.FINE, "adding cpEntry={0}", classpathEntry);
          classPathEntries.add(classpathEntry.getAbsolutePath());
          addTransitiveJars(expandedClassPathEntries, classpathEntry);
        }
      }
    }
  }

  @Override
  public String toString() {
    return "ClassPath-" + System.identityHashCode(this) + "=" + expandedClassPathEntries;
  }
  
  @Override
  public int hashCode() {
    return expandedClassPathEntries.hashCode();
  }
  
  @Override
  public boolean equals(Object another) {
    if (ClassPath.class.isInstance(another)) {
      return expandedClassPathEntries.equals(((ClassPath)another).expandedClassPathEntries);
    }
    return false;
  }
  
  /**
   * Find all jars referenced directly and indirectly via a classpath
   * specification typically drawn from java.class.path or
   * surefire.test.class.path System properties.
   * 
   * This will attempt to expand all manifest classpath entries.
   *
   * @return the set of entries in the classpath
   */
  public Set<String> getEntries() {
    return Collections.unmodifiableSet(expandedClassPathEntries);
  }
  
  /**
   * @see #getEntries() 
   */
  public Set<File> getFileEntries() {
    LinkedHashSet<File> fileEntries = new LinkedHashSet<File>();
    
    for (String fileName : expandedClassPathEntries) {
      File file = new File(fileName);
      if (!file.exists()) {
        logger.log(Level.FINE, "warning: {0} does not exist.", fileName);
      }
      fileEntries.add(file);
    }
    
    return Collections.unmodifiableSet(fileEntries);
  }

  /**
   * @return the original classpath as specified (i.e., without transitive manifest dependencies)
   */
  @SuppressWarnings("deprecation")
  public URL[] getRawURLs() throws IOException {
    ArrayList<URL> urls = new ArrayList<URL>(classPathEntries.size());

    for (String fileName : classPathEntries) {
      File file = new File(fileName);
      if (file.exists()) {
        urls.add(file.toURL());
      } else {
        logger.log(Level.FINE, "warning: {0} does not exist.", fileName);
      }
    }
    
    return urls.toArray(new URL[] {});
  }

  /**
   * Add provided File and all of its transitive manifest classpath entries to
   * the provided set
   * 
   * @param cpSet
   *          a Set to hold classpath entries
   * @param classpathFile
   *          File to transitively add to set
   */
  private static void addTransitiveJars(Set<String> cpSet, final File classpathFile) {
    cpSet.add(classpathFile.getAbsolutePath());

    if (classpathFile.exists()) {
      try {
        if (classpathFile.isFile()) {
          JarFile jarFile = null;
          Manifest mf;
          try {
            jarFile = new JarFile(classpathFile);
            mf = jarFile.getManifest();
          } finally {
            if (jarFile != null) {
              jarFile.close();
            }
          }

          // manifest may contain additional classpath
          if (mf != null) {
            String additionalClasspath = mf.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);

            if (additionalClasspath != null) {
              for (String classpathEntry : additionalClasspath.split(" ")) {
                if (!classpathEntry.equals("")) {
                  File mfClasspathFile = new File(classpathFile.getParent(), classpathEntry.trim());

                  if (mfClasspathFile.exists()
                      && !cpSet.contains(mfClasspathFile.getAbsolutePath())) {
                    logger.log(Level.FINE, "adding transitive cpEntry={0}", mfClasspathFile);
                    addTransitiveJars(cpSet, mfClasspathFile);
                  } else {
                    logger.log(Level.FINE, "skipping cpEntry={0}", mfClasspathFile);
                  }
                }
              }
            }
          }
        }
      } catch (Exception ex) {
        logger.log(Level.FINE, "an error occurred", ex);
      }
    } else {
      logger.log(Level.FINE, "cpEntry={0} does not exist", classpathFile);
    }
  }

}
