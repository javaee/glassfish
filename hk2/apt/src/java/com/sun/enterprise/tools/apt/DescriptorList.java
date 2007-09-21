package com.sun.enterprise.tools.apt;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;

import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
import java.io.File;
import java.io.IOException;

/**
 * List of {@link InhabitantsDescriptor}s.
 * This data structure needs to survive multiple rounds.
 * 
 * @author Kohsuke Kawaguchi
 */
final class DescriptorList {
    /**
     * Habitat name to its descriptor.
     */
    final Map<String,InhabitantsDescriptor> descriptors = new HashMap<String, InhabitantsDescriptor>();

    /**
     * Really ugly, but because there's no easy way to make sure we call {@link #loadExisting(AnnotationProcessorEnvironment)}
     * once and only once, we use the flag to remember if we already loaded existing stuff.
     */
    private boolean loaded;

    protected void loadExisting(AnnotationProcessorEnvironment env) {
        if(loaded)  return;
        loaded=true;

        String outDirectory = env.getOptions().get("-d");
        if (outDirectory==null) {
            outDirectory = System.getProperty("user.dir");
        }
        File outDir = new File(new File(outDirectory),"META-INF/inhabitants").getAbsoluteFile();

        if (!outDir.exists()) {
            return;
        }
        for (File file : outDir.listFiles()) {
            if(file.isDirectory())  continue;

            try {
                descriptors.put(file.getName(),new InhabitantsDescriptor(file));
            } catch (IOException e) {
                env.getMessager().printError(e.getMessage());
            }
        }
    }

    public void write(AnnotationProcessorEnvironment env) {
        String outDirectory = env.getOptions().get("-d");
        if(outDirectory==null)  outDirectory = System.getProperty("user.home");

        for (Entry<String, InhabitantsDescriptor> e : descriptors.entrySet()) {
            e.getValue().write(new File(outDirectory),env,e.getKey());
        }
    }

    public InhabitantsDescriptor get(String name) {
        InhabitantsDescriptor descriptor = descriptors.get(name);
        if(descriptor==null) {
            descriptor = new InhabitantsDescriptor();
            descriptors.put(name,descriptor);
        }
        return descriptor;
    }
}
