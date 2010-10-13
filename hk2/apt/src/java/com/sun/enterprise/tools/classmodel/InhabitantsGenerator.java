package com.sun.enterprise.tools.classmodel;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.glassfish.hk2.classmodel.reflect.AnnotatedElement;
import org.glassfish.hk2.classmodel.reflect.AnnotationModel;
import org.glassfish.hk2.classmodel.reflect.AnnotationType;
import org.glassfish.hk2.classmodel.reflect.ClassModel;
import org.glassfish.hk2.classmodel.reflect.InterfaceModel;
import org.glassfish.hk2.classmodel.reflect.ParsingContext;
import org.glassfish.hk2.classmodel.reflect.Type;
import org.glassfish.hk2.classmodel.reflect.Types;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.ContractProvided;
import org.jvnet.hk2.annotations.FactoryFor;
import org.jvnet.hk2.annotations.InhabitantAnnotation;
import org.jvnet.hk2.annotations.RunLevel;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.classmodel.InhabitantsFeed;
import org.jvnet.hk2.component.classmodel.InhabitantsParsingContextGenerator;

import com.sun.enterprise.tools.InhabitantsDescriptor;
import com.sun.hk2.component.Holder;
import com.sun.hk2.component.InhabitantParser;
import com.sun.hk2.component.InhabitantsParser;
import com.sun.hk2.component.IntrospectionScanner;

/**
 * Generates <tt>/META-INF/inhabitants/*</tt> based on comma-delimited list
 * of jars and directories passed in as arguments.
 * 
 * @author Jeff Trent
 * 
 * @since 3.1
 */
public class InhabitantsGenerator {
  public static final String PARAM_INHABITANT_FILE = "inhabitants.target.file";
  public static final String PARAM_INHABITANTS_SOURCE_FILES = "inhabitants.source.files";

  private final InhabitantsDescriptor descriptor;
  private final InhabitantsParsingContextGenerator ipcGen;
  
  public InhabitantsGenerator() {
    this(null);
  }
  
  public InhabitantsGenerator(InhabitantsDescriptor descriptor) {
    this.ipcGen = InhabitantsParsingContextGenerator.create(null);
    if (null != descriptor) {
      this.descriptor = descriptor;
    } else {
      this.descriptor = new InhabitantsDescriptor();
      this.descriptor.setComment("by " + getClass().getCanonicalName());
    }
  }
  
  public void add(List<File> sourceFiles) throws IOException {
    for (File file : sourceFiles) {
      add(file);
    }
  }

  public void add(File file) throws IOException {
    ipcGen.addFileOrDirectory(file);
  }
  
  public void generate(File targetInhabitantFile) throws IOException {
    targetInhabitantFile.getParentFile().mkdirs();

    PrintWriter w = new PrintWriter(targetInhabitantFile, "UTF-8");
    try {
      generate(w, targetInhabitantFile.getName());
    } finally {
      w.close();
    }
  }
  
  @SuppressWarnings("unchecked")
  public void generate(PrintWriter writer, String habitatName) throws IOException {
    descriptor.clear();

    InhabitantsParserDescriptorWriter ip = new InhabitantsParserDescriptorWriter(descriptor);
    InhabitantsFeed feed = InhabitantsFeed.create(new Habitat(), ip);
    // TODO: the standard machinery (w/o TemporaryIntrospectionScanner) should just work
    feed.populate(ipcGen, 
        (Collection)Collections.singleton(
            new TemporaryIntrospectionScanner(descriptor)));
    
    // i/o the descriptor(s) out
    descriptor.write(writer);
  }

  public static void main(String [] args) throws Exception {
    String classpathDebug = System.getProperty("java.class.path");
    System.out.println(InhabitantsGenerator.class.getSimpleName() + " classpath is " + classpathDebug);

    String arg = System.getProperty(PARAM_INHABITANT_FILE);
    File targetInhabitantFile = new File(arg);
    
    List<File> sourceFiles = new ArrayList<File>();
    arg = System.getProperty(PARAM_INHABITANTS_SOURCE_FILES);
    String [] sourceFileNames = arg.split(",: \t");
    for (String sourceFile : sourceFileNames) {
      File source = new File(sourceFile);
      if (source.exists()) {
        sourceFiles.add(source);
      } else {
        System.err.println("WARNING: can't find " + sourceFile);
      }
    }
    
    if (sourceFiles.isEmpty()) {
      System.err.println("WARNING: nothing to do");
    }
    
    InhabitantsGenerator generator = new InhabitantsGenerator();
    generator.add(sourceFiles);
    generator.generate(targetInhabitantFile);
  }

  
  private static class InhabitantsParserDescriptorWriter extends InhabitantsParser {

    private final InhabitantsDescriptor descriptor;
    
    public InhabitantsParserDescriptorWriter(InhabitantsDescriptor descriptor) {
      super(null);
      this.descriptor = descriptor;
    }

    /**
     * The idea is to put the inhabitant into the descriptors instead of the habitat here
     */
    @Override
    protected void add(Inhabitant<?> i, InhabitantParser parser) {
      // TODO: do something once we fix the kludge, taking out TemporaryIntrospectionScanner
    }
    
    /**
     * The idea is to merge the inhabitant index into the descriptors instead of the habitat here
     */
    @Override
    protected void addIndex(Inhabitant<?> i, String typeName, String name) {
      // TODO: do something once we fix the kludge, taking out TemporaryIntrospectionScanner
    }
  }

  
  /**
   * TODO: Temporary kludge until I find out why the main code path doesn't work
   */
  private static class TemporaryIntrospectionScanner implements IntrospectionScanner {

    private final InhabitantsDescriptor descriptor;
    
    TemporaryIntrospectionScanner(InhabitantsDescriptor descriptor) {
      this.descriptor = descriptor;
    }
    
    @Override
    public void parse(ParsingContext context, Holder<ClassLoader> loader) {
      Types types = context.getTypes();
      
      // TODO: This should have contained InhabitantAnnotation but it didn't 
//      Type notThere = types.getBy(InhabitantAnnotation.class.getName());
      
      AnnotationType at = types.getBy(AnnotationType.class, Service.class.getName());

      // TODO: how can this be null --- but it is from time to time?
      if (null != at) {
        // TODO: This should have contained InhabitantAnnotation but it didn't 
//        Collection<AnnotationModel> notThere2 = at.getAnnotations();
        
        Collection<AnnotatedElement> coll = at.allAnnotatedTypes();
        for (AnnotatedElement ae : coll) {
          process(ae, types);
        }
      }
    }

    protected void process(AnnotatedElement ae, Types types) {
      if (ClassModel.class.isInstance(ae)) {
        String service = ae.getName();
        
        ClassModel classModel = ClassModel.class.cast(ae);
        Collection<String> contracts = getContracts(classModel, types);
        Collection<String> annotations = getAnnotations(classModel, types);

        AnnotationModel am = ae.getAnnotation(Service.class.getCanonicalName());
        Object nameObj = am.getValues().get("name");
        String name = (null == nameObj) ? null : nameObj.toString();
        
        Map<String, String> mm = null;
        Object metaObj = am.getValues().get("metadata");
        if (null != metaObj) {
          String meta = metaObj.toString();
          String [] split = meta.split(",");
          for (String entry : split) {
            String [] split2 = entry.split("=");
            if (2 == split2.length) {
              if (null == mm) {
                mm = new LinkedHashMap<String, String>();
              }
              mm.put(split2[0], split2[1]);
            }
          }
        }

        // add it to the descriptors
        descriptor.putAll(service, contracts, annotations, name, mm);
      }
    }

    protected Collection<String> getContracts(ClassModel classModel, Types types) {
      Collection<String> contracts = new ArrayList<String>();

      Collection<InterfaceModel> ifModels = classModel.getInterfaces();
      for (InterfaceModel ifModel : ifModels) {
        if (null != ifModel) {
          AnnotationModel am = ifModel.getAnnotation(Contract.class.getCanonicalName());
          if (null != am) {
            contracts.add(ifModel.getName());
          }
        }
      }
      
      Collection<AnnotationModel> amColl = classModel.getAnnotations();
      for (AnnotationModel am : amColl) {
        AnnotationType at = am.getType();
        String name = at.getName();
        if (name.equals(ContractProvided.class.getName())) {
          Object t = am.getValues().get("value");
          if (null != t) {
            String c = clean(t.toString());
            contracts.add(c);
          }
        }
      }
      
      return contracts;
    }
    
    protected Collection<String> getAnnotations(ClassModel classModel, Types types) {
      Collection<String> contracts = new ArrayList<String>();
      
      // TODO: getting annotations off of FactoryFor does NOT show @Contract, and it should!
//      AnnotationType type = types.getBy(AnnotationType.class, name);

      Collection<AnnotationModel> amColl = classModel.getAnnotations();
      for (AnnotationModel am : amColl) {
        AnnotationType at = am.getType();
        String name = at.getName();
        if (name.equals(FactoryFor.class.getName())) {
          Object t = am.getValues().get("value");
          String c = clean(t.toString());
          contracts.add(name + ":" + c);
        } else if (name.equals(RunLevel.class.getName())) {
          contracts.add(name);
        } else {
          Collection<AnnotationModel> subAnn = am.getType().getAnnotations();
          for (AnnotationModel z : subAnn) {
            name = z.getType().getName();
            if (name.equals(RunLevel.class.getName())) {
              Object debug = z.getValues();
              contracts.add(name);
            } else {
              // check meta-annotations one level up
              Collection<AnnotationModel> subAnn2 = z.getType().getAnnotations();
              for (AnnotationModel z1 : subAnn2) {
                Object debug = z1.getValues();
                name = z1.getType().getName();
                if (name.equals(RunLevel.class.getName())) {
                  contracts.add(name);
                }
              }
            }
          }
        }
      }
      
      return contracts;
    }
    
    // TODO: WTF
    private String clean(String mangled) {
      if (mangled.startsWith("L")) {
        mangled = mangled.substring(1);
      }
      if (mangled.endsWith(";")) {
        mangled = mangled.substring(0, mangled.length()-1);
      }
      return mangled.replace("/", ".");
    }
    
  }
}
