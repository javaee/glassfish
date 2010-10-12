package com.sun.enterprise.tools.classmodel;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.glassfish.hk2.classmodel.reflect.AnnotatedElement;
import org.glassfish.hk2.classmodel.reflect.AnnotationModel;
import org.glassfish.hk2.classmodel.reflect.AnnotationType;
import org.glassfish.hk2.classmodel.reflect.ClassModel;
import org.glassfish.hk2.classmodel.reflect.InterfaceModel;
import org.glassfish.hk2.classmodel.reflect.Member;
import org.glassfish.hk2.classmodel.reflect.ParsingContext;
import org.glassfish.hk2.classmodel.reflect.Types;
import org.glassfish.hk2.classmodel.reflect.Type;
import org.glassfish.hk2.classmodel.reflect.impl.AnnotationTypeImpl;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.ContractProvided;
import org.jvnet.hk2.annotations.Service;
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

  private InhabitantsParsingContextGenerator ipcGen;
  
  public InhabitantsGenerator() {
    ipcGen = InhabitantsParsingContextGenerator.create(null);
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
    InhabitantsDescriptor descriptor = new InhabitantsDescriptor();
    InhabitantsParserDescriptorWriter ip = new InhabitantsParserDescriptorWriter(descriptor);
    InhabitantsFeed feed = InhabitantsFeed.create(null, ip);
    // TODO: the standard machinery (w/o TemporaryIntrospectionScanner) should just work
    feed.populate(ipcGen, 
        (Collection)Collections.singleton(
            new TemporaryIntrospectionScanner(descriptor)));
    
    // i/o the descriptor(s) out
    descriptor.write(writer);
  }

  public static void main(String [] args) throws Exception {
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

    @Override
    protected void add(Inhabitant<?> i, InhabitantParser parser) {
      // do nothing
      // TODO: do something once we fix the kludge, taking out TemporaryIntrospectionScanner
    }
    
    @Override
    protected void addIndex(Inhabitant<?> i, String typeName, String name) {
      // do nothing
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
      Type type = types.getBy(AnnotationType.class, Service.class.getName());
      if (AnnotationType.class.isInstance(type)) {
        AnnotationType at = AnnotationType.class.cast(type);
        Collection<AnnotatedElement> coll = at.allAnnotatedTypes();
        for (AnnotatedElement ae : coll) {
          process(ae);
        }
      }
    }

    protected void process(AnnotatedElement ae) {
      if (ClassModel.class.isInstance(ae)) {
        String service = ae.getName();
        
        ClassModel classModel = ClassModel.class.cast(ae);
        Collection<String> contracts = getContracts(classModel);


        // TODO:
        descriptor.putAll(service, contracts, null, null);
      }
    }

    protected Collection<String> getContracts(ClassModel classModel) {
      Collection<String> contracts = new ArrayList<String>();
      Collection<InterfaceModel> ifModels = classModel.getInterfaces();
      for (InterfaceModel ifModel : ifModels) {
        AnnotationModel am = ifModel.getAnnotation(Contract.class.getCanonicalName());
        if (null != am) {
          contracts.add(ifModel.getName());
        }
      }
      
      AnnotationModel am = classModel.getAnnotation(ContractProvided.class.getCanonicalName());
      if (null != am) {
        Object t = am.getValues().get("value");
        if (null != t) {
          String c = clean(t.toString());
          contracts.add(c);
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
