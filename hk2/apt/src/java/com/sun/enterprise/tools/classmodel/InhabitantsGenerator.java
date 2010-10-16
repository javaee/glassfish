/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2010 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.hk2.classmodel.reflect.AnnotationType;
import org.glassfish.hk2.classmodel.reflect.ParsingContext;
import org.glassfish.hk2.classmodel.reflect.Types;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.InhabitantAnnotation;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.classmodel.ClassPath;
import org.jvnet.hk2.component.classmodel.InhabitantsFeed;
import org.jvnet.hk2.component.classmodel.InhabitantsParsingContextGenerator;

import com.sun.enterprise.tools.InhabitantsDescriptor;
import com.sun.hk2.component.InhabitantParser;
import com.sun.hk2.component.InhabitantsParser;

/**
 * Generates <tt>/META-INF/inhabitants/*</tt> based on comma-delimited list
 * of jars and directories passed in as arguments.
 * <p/>
 * The implementation strategy is to use the full {@link #PARAM_INHABITANTS_CLASSPATH}
 * to resolve classes using the {@link InhabitantsParsingContextGenerator}, checking
 * each inhabitant being added against a {@link CodeSourceFilter} built from the
 * {@link #PARAM_INHABITANTS_SOURCE_FILES} passed in as arguments.
 * <p/>
 * Upon matching filter matching (i.e., the inhabitants type name must reside in one of
 * the codesources specified) then the inhabitant is written to the {@link InhabitantsDescriptor}
 * and written out.
 * 
 * @see InhabitantFileBasedParser
 * 
 * @author Jeff Trent
 * @since 3.1
 */
public class InhabitantsGenerator {

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
   * This is the inhabitants file built.
   * <p>
   * Passed as a system property.
   */
  public static final String PARAM_INHABITANT_FILE = "inhabitants.target.file";
  
  /**
   * This is the source files (jars | directories) to introspect and build a habitat for. 
   * <p>
   * Passed as a system property.
   */
  public static final String PARAM_INHABITANTS_SOURCE_FILES = "inhabitants.source.files";
  
  /**
   * This is the working classpath the introspection machinery will use to resolve
   * referenced contracts and annotations.  <b>Without this you may see a bogus
   * inhabitants file being generated.</b>  The indicator for this is a habitat with
   * only class names and missing indicies.
   * <p>
   * Passed as a system property.
   */
  public static final String PARAM_INHABITANTS_CLASSPATH = "inhabitants.classpath";

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
    
    this.ipcGen = InhabitantsParsingContextGenerator.create(null);
    
    if (null != descriptor) {
      this.descriptor = descriptor;
    } else {
      this.descriptor = new InhabitantsDescriptor();
      this.descriptor.setComment("by " + getClass().getCanonicalName());
    }
  
    try {
      ipcGen.parse(inhabitantsClassPath.getFileEntries());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    
    codeSourceFilter = new CodeSourceFilter(inhabitantsSourceFiles);
  }
  
  private ClassPath filterIgnores(ClassPath inhabitantsClassPath) {
    LinkedHashSet<File> newFiles = new LinkedHashSet<File>();

    Set<File> entries = new LinkedHashSet<File>(inhabitantsClassPath.getFileEntries());
    for (File file : entries) {
      if (!IGNORE.contains(file.getName())) {
        newFiles.add(file);
      } else {
        Logger.getAnonymousLogger().log(Level.FINE, "ignoring {0}", file);
      }
    }
    
    ClassPath newClassPath = ClassPath.create(null, newFiles);
    return newClassPath;
  }

  public void generate(File targetInhabitantFile) throws IOException {
    targetInhabitantFile.getParentFile().mkdirs();

    PrintWriter w = new PrintWriter(targetInhabitantFile, "UTF-8");
    try {
      generate(w);
    } finally {
      w.close();
    }
  }
  
  @SuppressWarnings("unchecked")
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
//    String classpath = System.getProperty("java.class.path");
//    System.out.println(InhabitantsGenerator.class.getSimpleName() + " classpath is " + classpath);

    String arg = System.getProperty(PARAM_INHABITANT_FILE);
    if (null == arg || arg.isEmpty()) {
      System.err.println("ERROR: sysprop " + PARAM_INHABITANT_FILE + " is expected");
      System.exit(-1);
    }
    File targetInhabitantFile = new File(arg);
    
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

    ClassPath inhabitantsClassPath = null;
    arg = System.getProperty(PARAM_INHABITANTS_CLASSPATH);
    if (null == arg || arg.isEmpty()) {
      inhabitantsClassPath = ClassPath.create(null, false);
      System.err.println("WARNING: sysprop " + PARAM_INHABITANTS_CLASSPATH + 
          " is missing; defaulting to classpath=" + inhabitantsClassPath.getFileEntries() + 
          " - this may result in an invalid inhabitants file being created!");
    } else {
      inhabitantsClassPath = ClassPath.create(null, arg);
    }
    
    if (sourceFiles.isEmpty()) {
      System.err.println("WARNING: nothing to do!");
      return;
    }

    InhabitantsGenerator generator = new InhabitantsGenerator(null, inhabitantsSourceFiles, inhabitantsClassPath);

    // sanity check
    InhabitantsParsingContextGenerator ipcGen = generator.getContextGenerator();
    ParsingContext pc = ipcGen.getContext();
    Types types = pc.getTypes();
    AnnotationType ia = types.getBy(AnnotationType.class, InhabitantAnnotation.class.getName());
    AnnotationType c = types.getBy(AnnotationType.class, Contract.class.getName());
    if (null == ia || null == c) {
      System.err.println("ERROR: HK2's auto-depends jar is an expected argument in " + PARAM_INHABITANTS_CLASSPATH);
      return;
    }
    
    generator.generate(targetInhabitantFile);
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
        descriptor.putAll(pendingInhabitant.typeName(), null, pendingUnamedContracts, null, pendingInhabitant.metadata());
        pendingUnamedContracts = null;
        pendingInhabitant = null;
      }
    }

    /**
     * Controls the filtering.  This decides whether add(i) or addIndex(...) is ultimately called.
     */
    @Override
    protected void add(Inhabitant<?> i, InhabitantParser parser) {
      assert(!i.isInstantiated());
      String typeName = i.typeName();
      if (codeSourceFilter.matches(typeName)) {
        super.add(i, parser);
        assert(!i.isInstantiated());
      } else {
        Logger.getAnonymousLogger().log(Level.FINE, "filtering out {0}", i);
      }
    }

    /**
     * The idea is to put the inhabitant into the descriptors instead of the habitat here
     */
    @Override
    protected void add(Inhabitant<?> i) {
//      System.out.println("add\t" + i + " " + i.metadata());

      // flush any previous inhabitant definition
      flush();
      
      pendingInhabitant = i;
    }
    
    /**
     * The idea is to merge the inhabitant index into the descriptors instead of the habitat here
     */
    @Override
    protected void addIndex(Inhabitant<?> i, String typeName, String name) {
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
