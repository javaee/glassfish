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
package com.sun.enterprise.tools.classmodel;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jvnet.hk2.component.classmodel.ClassPath;

/**
 * CodeSourceFilter is used for determining if classes are in the ClassPath.
 * 
 * @author Jeff Trent
 */
public class CodeSourceFilter {

  private final Logger logger = Logger.getLogger(CodeSourceFilter.class.getName());
  
  private final ClassPath filter;

  // classes to code source
  private final HashMap<String, File> classes = new HashMap<String, File>();

  public CodeSourceFilter(ClassPath filter) {
    this.filter = filter;
    try {
      if (null != filter) {
        initialize();
      }
    } catch (IOException e) {
      Logger.getAnonymousLogger().log(Level.FINE, "error occurred", e);
    }
  }

  public String toString() {
    return (new StringBuilder()).append(getClass().getSimpleName()).append(":")
        .append(classes.toString()).toString();
  }

  /**
   * @return true if the given className is present in the ClassPath
   */
  public boolean matches(String className) {
    return classes.containsKey(className);
  }

  /**
   * @return the code source of the given className is present in the ClassPath, or null if not found
   */
  public File codeSourceOf(String className) {
    return classes.get(className);
  }
  
  private void initialize() throws IOException {
    for (File file : filter.getFileEntries()) {
      if (file.exists()) {
        if (file.isFile()) {
          indexJar(new JarFile(file), file);
        } else if (file.isDirectory()) {
          indexDir("", file);
        }
      }
    }
  }

  private void indexJar(JarFile jarFile, File file) throws IOException {
    JarEntry entry;
    for (Enumeration<JarEntry> en = jarFile.entries(); en.hasMoreElements(); ) {
      entry = (JarEntry) en.nextElement();
      index(entry.getName().replace("/", "."), file);
    }
    jarFile.close();
  }

  private void indexDir(String baseName, File directory) {
    File files[] = directory.listFiles();
    if (null == files) {
      return;
    }
    
    for (File file : files) {
      if (file.isHidden()) {
        continue;
      }
      
      if (file.isDirectory()) {
        indexDir((new StringBuilder()).append(baseName).append(
            baseName.isEmpty() ? "" : ".").append(file.getName()).toString(),
            file);
      } else {
        index((new StringBuilder()).append(baseName).append(
            baseName.isEmpty() ? "" : ".").append(file.getName()).toString(),
            directory);
      }
    }
  }

  private void index(String name, File codeSource) {
    if (name.endsWith(".class") && !isnum(name.charAt(0))) {
      name = name.substring(0, name.length() - 6);
      File oldFile = classes.put(name, codeSource);
      if (null != oldFile) {
        // first in the classpath always wins
        classes.put(name, oldFile);
        logger.log(Level.WARNING, "duplicate class: {0} in {1} and {2}", new Object[] {name, oldFile, codeSource});
      }
    }
  }

  private boolean isnum(char ch) {
    return ch >= '0' && ch <= '9';
  }
}
