package com.sun.enterprise.tools.apt;

import com.sun.hk2.component.InhabitantsFile;
import com.sun.hk2.component.InhabitantsScanner;
import com.sun.hk2.component.KeyValuePairParser;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;

/**
 * Inhabitants descriptor as a map from the class name to its line.
 *
 * @author Kohsuke Kawaguchi
 */
final class InhabitantsDescriptor extends HashMap<String,String> {
    private boolean dirty = false;
    public InhabitantsDescriptor() {
    }

    public InhabitantsDescriptor(File f) throws IOException {
        load(f);
    }

    /**
     * Loads an existing file.
     */
    public void load(File f) throws IOException {
        InhabitantsScanner scanner = new InhabitantsScanner(new FileInputStream(f),f.getPath());
        for (KeyValuePairParser kvpp : scanner)
            put(kvpp.find(InhabitantsFile.CLASS_KEY),kvpp.getLine());
    }


    public String put(String key, String value) {
        dirty = true;
        return super.put(key, value);
    }

    public String remove(Object key) {
        dirty = true;
        return super.remove(key);
    }

    /**
     * Writes the descriptor to a file.
     */
    public void write(File outputDir, AnnotationProcessorEnvironment env,String habitatName) {
        if(!dirty)  return; // no need to write.

        try {
            File out = new File(new File(outputDir,InhabitantsFile.PATH),habitatName);
            out.getParentFile().mkdirs();
            PrintWriter w = new PrintWriter(out,"UTF-8");

            w.println("# generated on "+new Date().toGMTString());
            for (String line : values()) {
                w.println(line);
            }
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
            env.getMessager().printError("Failed to write inhabitants file "+habitatName);
        }
    }
}
