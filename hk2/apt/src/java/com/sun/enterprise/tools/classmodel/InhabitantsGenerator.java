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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.classmodel.ClassPath;
import org.jvnet.hk2.component.classmodel.ClassPathAdvisor;
import org.jvnet.hk2.component.classmodel.FileCachingClassPathAdvisor;
import org.jvnet.hk2.component.classmodel.InhabitantsFeed;
import org.jvnet.hk2.component.classmodel.InhabitantsParsingContextGenerator;

import com.sun.enterprise.tools.InhabitantsDescriptor;
import com.sun.hk2.component.InhabitantsParser;

/**
 * Generates <tt>/META-INF/inhabitants/*</tt> based on comma-delimited list
 * of jars and directories passed in as arguments.
 * 
 * @see InhabitantFileBasedParser
 * 
 * @author Jeff Trent
 * @since 3.1
 */
public class InhabitantsGenerator extends Constants {

  private static final Logger logger = Logger.getLogger(InhabitantsGenerator.class.getName());
  
  /**
   * TODO: this should probably go directly into {@link InhabitantsParsingContextGenerator}
   */
  private static HashSet<String> IGNORE = new HashSet<String>();
  static {
    // need to determine if these are jdk jars or not too.
    IGNORE.add("rt.jar");
    IGNORE.add("tools.jar");
  };
  
  /**
   * The filter to control scope of inhabitants being created
   */
  private final CodeSourceFilter codeSourceFilter;
  
  /**
   * The parsing context
   */
  private final InhabitantsParsingContextGenerator ipcGen;

  /**
   * the descriptor to generator
   */
  private final InhabitantsDescriptor descriptor;

  /**
   * Construction with all of the parameters needed to generate.
   * 
   * @param descriptor optionally a preconfigured inhabitants descriptor
   * @param inhabitantsSourceFiles required set of inhabitants source files (directories | jars)
   * @param inhabitantsClassPath the fully qualified classpath in order to resolve class-model
   */
  public InhabitantsGenerator(InhabitantsDescriptor descriptor,
      ClassPath inhabitantsSourceFiles,
      ClassPath inhabitantsClassPath) {
    
    inhabitantsClassPath = filterIgnores(inhabitantsClassPath);
    
    // TODO:
//    // have caching on all of the time
//    ClassPathAdvisor advisor = new FileCachingClassPathAdvisor();
    ClassPathAdvisor advisor = null;
    
    logger.log(Level.FINE, "working classpath: {0}", inhabitantsClassPath);
    this.ipcGen = InhabitantsParsingContextGenerator.
          create(null, createExecutorService(), inhabitantsClassPath, advisor);
    
    if (null != descriptor) {
      this.descriptor = descriptor;
    } else {
      this.descriptor = new InhabitantsDescriptor();
      this.descriptor.setComment("by " + getClass().getName());
    }
  
    try {
      logger.log(Level.FINE, "Parsing: {0}", inhabitantsSourceFiles);
      ipcGen.parse(inhabitantsSourceFiles.getFileEntries());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    
    codeSourceFilter = new CodeSourceFilter(inhabitantsSourceFiles);
  }

  // temporary, until multi-threaded issues are resolved in class-model parsing
  private ExecutorService createExecutorService() {
    return null;
  }

  private ClassPath filterIgnores(ClassPath inhabitantsClassPath) {
    LinkedHashSet<File> newFiles = new LinkedHashSet<File>();

    Set<File> entries = new LinkedHashSet<File>(inhabitantsClassPath.getFileEntries());
    for (File file : entries) {
      if (!IGNORE.contains(file.getName())) {
        logger.log(Level.FINE, "accepting {0}", file);
        newFiles.add(file);
      } else {
        logger.log(Level.FINE, "filtering {0}", file);
      }
    }
    
    ClassPath newClassPath = ClassPath.create(null, newFiles);
    return newClassPath;
  }

  public void generate(File targetInhabitantFile, boolean sort) throws IOException {
    File parent = targetInhabitantFile.getParentFile();
    if (null != parent) {
      parent.mkdirs();
    }
    
    ByteArrayOutputStream out = null;
    PrintWriter w;
    if (sort) {
      out = new ByteArrayOutputStream();
      w = new PrintWriter(out);
    } else {
      w = new PrintWriter(targetInhabitantFile, "UTF-8");
    }
    
    try {
      generate(w);
    } finally {
      w.close();
    }
    
    if (descriptor.isEmpty()) {
      targetInhabitantFile.delete();
      return;
    }

    if (sort) {
      String sorterdInhabitants = Utilities.sortInhabitantsDescriptor(out.toString(), sort);
      FileOutputStream fos = new FileOutputStream(targetInhabitantFile);
      fos.write(sorterdInhabitants.getBytes());
      fos.close();
    }
  }
  
  public void generate(PrintWriter writer) throws IOException {
    descriptor.clear();

    InhabitantsParserDescriptorWriter ip = new InhabitantsParserDescriptorWriter();
    InhabitantsFeed feed = InhabitantsFeed.create(new Habitat(), ip);
    feed.populate(ipcGen);

    // flush the last inhabitant to the descriptor
    ip.flush();
    
    // i/o the descriptor(s) out
    descriptor.write(writer);
  }

  /**
   * @return Use parsing context generated by class-model representative of {@link #PARAM_INHABITANTS_CLASSPATH}
   */
  InhabitantsParsingContextGenerator getContextGenerator() {
    return ipcGen;
  }
  
  public static void main(String [] args) throws Exception {
    File targetInhabitantFile = getInhabitantFile(PARAM_INHABITANT_TARGET_FILE, false);
    ClassPath inhabitantsSourceFiles = getScopedInhabitantCodeSources();
    ClassPath inhabitantsClassPath = getFullInhabitantsClassPath();
    boolean sort = Boolean.getBoolean(PARAM_INHABITANTS_SORTED);
    
    if (inhabitantsSourceFiles.getEntries().isEmpty()) {
      System.err.println("WARNING: nothing to do!");
      return;
    }

    // can disable date here
    InhabitantsDescriptor descriptor = null;
//    InhabitantsDescriptor descriptor = new InhabitantsDescriptor();
//    descriptor.enableDateOutput(false);
    
    InhabitantsGenerator generator = new InhabitantsGenerator(descriptor, inhabitantsSourceFiles, inhabitantsClassPath);

    // sanity check --- can't sanity check any more since target classpath is reduced
//    InhabitantsParsingContextGenerator ipcGen = generator.getContextGenerator();
//    ParsingContext pc = ipcGen.getContext();
//    Types types = pc.getTypes();
//    AnnotationType ia = types.getBy(AnnotationType.class, InhabitantAnnotation.class.getName());
//    AnnotationType c = types.getBy(AnnotationType.class, Contract.class.getName());
//    if (null == ia || null == c) {
//      System.err.println("ERROR: HK2's auto-depends jar is an expected argument in " + PARAM_INHABITANTS_SOURCE_FILES);
//      return;
//    }
    
    generator.generate(targetInhabitantFile, sort);
  }

  static ClassPath getFullInhabitantsClassPath() {
    String arg;
    ClassPath inhabitantsClassPath = null;
    arg = System.getProperty(PARAM_INHABITANTS_CLASSPATH);
    if (null == arg || arg.isEmpty()) {
      inhabitantsClassPath = ClassPath.create(null, false);
      System.err.println("WARNING: sysprop " + PARAM_INHABITANTS_CLASSPATH + 
          " is missing; defaulting to system classpath; this may result in an invalid inhabitants file being created.");
      if (logger.isLoggable(Level.FINER)) {
        logger.log(Level.FINER, "classpath={0}", inhabitantsClassPath.getFileEntries());
      }
    } else {
      inhabitantsClassPath = ClassPath.create(null, arg);
    }
    return inhabitantsClassPath;
  }

  static ClassPath getScopedInhabitantCodeSources() {
    String arg;
    arg = System.getProperty(PARAM_INHABITANTS_SOURCE_FILES);
    if (null == arg || arg.isEmpty()) {
      System.err.println("ERROR: sysprop " + PARAM_INHABITANTS_SOURCE_FILES + " is expected");
      System.exit(-1);
    }
    List<File> sourceFiles = new ArrayList<File>();
    String [] sourceFileNames = arg.split(File.pathSeparator);
    for (String sourceFile : sourceFileNames) {
      File source = new File(sourceFile);
      if (source.exists()) {
        sourceFiles.add(source);
      } else {
        System.err.println("WARNING: can't find " + sourceFile);
      }
    }
    ClassPath inhabitantsSourceFiles = ClassPath.create(null, sourceFiles);
    return inhabitantsSourceFiles;
  }

  
  static File getInhabitantFile(String key, boolean mustExist) {
    String arg = System.getProperty(key);
    if (null == arg || arg.isEmpty()) {
      System.err.println("ERROR: sysprop " + key + " is expected");
      System.exit(-1);
    }
    File targetInhabitantFile = new File(arg);
    if (mustExist && !targetInhabitantFile.exists()) {
      System.err.println("ERROR: " + targetInhabitantFile + " does not exist");
      System.exit(-1);
    }
    return targetInhabitantFile;
  }

  
  /**
   * Marshals descriptor lines from parsed inhabitants.
   */
  private class InhabitantsParserDescriptorWriter extends InhabitantsParser {

    private Inhabitant<?> pendingInhabitant;
    private List<String> pendingUnamedContracts;
    
    private InhabitantsParserDescriptorWriter() {
      super(null);
    }
    
    /**
     * Writes out any last pending inhabitant
     */
    public void flush() {
      if (null != pendingInhabitant) {
        logger.log(Level.INFO, "adding inhabitant {0} with contracts {1}", new Object[] {pendingInhabitant, pendingUnamedContracts});
        descriptor.putAll(pendingInhabitant.typeName(), null, pendingUnamedContracts, null, pendingInhabitant.metadata());
        pendingUnamedContracts = null;
        pendingInhabitant = null;
      }
    }

    /**
     * Controls the filtering.  This decides whether add(i) or addIndex(...) is ultimately called.
     */
    @Override
    protected boolean isFilteredInhabitant(String typeName) {
      if (null == codeSourceFilter || codeSourceFilter.matches(typeName)) {
        logger.log(Level.FINE, "accepting {0}", typeName);
        return false; // true==(ignore it); false==(include it)
      } else {
        logger.log(Level.FINE, "filtering {0}", typeName);
        return true;
      }
    }

    /**
     * The idea is to put the inhabitant into the descriptors instead of the habitat here
     */
    @Override
    public void add(Inhabitant<?> i) {
//      System.out.println("add\t" + i + " " + i.metadata());

      // flush any previous inhabitant definition
      flush();
      
      pendingInhabitant = i;
    }
    
    /**
     * The idea is to merge the inhabitant index into the descriptors instead of the habitat here
     */
    @Override
    public void addIndex(Inhabitant<?> i, String typeName, String name) {
      // don't flush since we are building up the inhabitant descriptor definition
      
//      System.out.println("addIndex\t" + i + "; " + typeName + "; " + name);

      if (null == pendingUnamedContracts) {
        pendingUnamedContracts = new ArrayList<String>();
      }

      if (null == name) {
        pendingUnamedContracts.add(typeName);
      } else {
        pendingUnamedContracts.add(typeName + ":" + name);
      }
    }
  }


}
