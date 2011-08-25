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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jvnet.hk2.component.classmodel.ClassPath;

import com.sun.enterprise.tools.InhabitantsDescriptor;

/**
 * Provides ability to filter an inhabitants file to only include what
 * is designated by a specified classpath.
 * 
 * @author Jeff Trent
 */
public class InhabitantsFilter extends Constants {

  private static Logger logger = Logger.getLogger(InhabitantsFilter.class.getName());
  
  protected InhabitantsFilter() {}

  /**
   * Requires {@link Constants#PARAM_INHABITANT_SOURCE_FILE}, {@link Constants#PARAM_INHABITANT_TARGET_FILE},
   * {@link Constants#PARAM_INHABITANTS_SOURCE_FILES}, and {@link Constants#PARAM_INHABITANTS_SORTED}
   * to be passed.
   * 
   * @param args not used
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    File sourceInhabitantFile = InhabitantsGenerator.getInhabitantFile(PARAM_INHABITANT_SOURCE_FILE, false);
    if (!sourceInhabitantFile.exists()) {
      logger.log(Level.INFO, "Nothing to do.");
      return;
    }
    File targetInhabitantFile = InhabitantsGenerator.getInhabitantFile(PARAM_INHABITANT_TARGET_FILE, false);
    ClassPath targetClassPath = InhabitantsGenerator.getScopedInhabitantCodeSources();
    CodeSourceFilter filter = new CodeSourceFilter(targetClassPath);
    logger.log(Level.FINE, "filter is {0}", filter);
    
    InhabitantsDescriptor inDescriptor = new InhabitantsDescriptor();
    logger.log(Level.FINE, "source file is {0}", sourceInhabitantFile);
    inDescriptor.load(sourceInhabitantFile);

    InhabitantsDescriptor outDescriptor = new InhabitantsDescriptor();
    outDescriptor.enableDateOutput(false);
    
    boolean sorted = Boolean.getBoolean(PARAM_INHABITANTS_SORTED);
    
    process(inDescriptor, outDescriptor, filter);
    writeInhabitantsFile(targetInhabitantFile, outDescriptor, sorted);
  }

  /**
   * Builds the outDescriptor based on filtered inDescriptor contents.
   * 
   * @param inDescriptor the source input descriptor
   * @param outDescriptor the destination descriptor
   * @param filter the filter
   */
  public static void process(InhabitantsDescriptor inDescriptor,
      InhabitantsDescriptor outDescriptor,
      CodeSourceFilter filter) {
    
    for (Entry<String, String> entry : inDescriptor.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      String clazz = classOf(value);
      if (null == filter || filter.matches(clazz)) {
        outDescriptor.put(key, value);
      }
    }
  }

  /**
   * Writes the inhabitants file to disk.
   * 
   * @param targetInhabitantFile
   * @param outDescriptor
   * @param sorted
   */
  public static void writeInhabitantsFile(
      File targetInhabitantFile,
      InhabitantsDescriptor outDescriptor,
      boolean sorted) throws FileNotFoundException, IOException {
    if (outDescriptor.isEmpty()) {
      targetInhabitantFile.delete();
    } else {
      logger.log(Level.INFO, "writing file {0}", targetInhabitantFile);
      
      if (!sorted) {
        outDescriptor.write(targetInhabitantFile);
      } else {
        File parent = targetInhabitantFile.getParentFile();
        if (null != parent) {
          parent.mkdirs();
        }
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter w = new PrintWriter(out);
        outDescriptor.write(w);
        w.close();
        
        String sorterdInhabitants = Utilities.sortInhabitantsDescriptor(out.toString(), sorted);
        FileOutputStream fos = new FileOutputStream(targetInhabitantFile);
        fos.write(sorterdInhabitants.getBytes());
        fos.close();
      }
    }
  }

  static String classOf(String value) {
    return Utilities.split(value).getOne("class");
  }

}
