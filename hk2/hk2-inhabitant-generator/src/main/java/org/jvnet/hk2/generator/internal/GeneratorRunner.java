/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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
package org.jvnet.hk2.generator.internal;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.glassfish.hk2.utilities.DescriptorImpl;
import org.objectweb.asm.ClassReader;

/**
 * @author jwells
 *
 */
public class GeneratorRunner {
    private final static String DOT_CLASS = ".class";
    private final static String META_INF = "META-INF";
    private final static String INHABITANTS = "hk2-locator";
    private final static String TARGET_HABITATS = "target-habitats";  //Should be same as ConfigMetadata.TARGET_HABITATS
    
    private final Utilities utilities;  // For caching
    private final String fileOrDirectory;
    private final String outjarName;
    private final String locatorName;
    private final boolean verbose;
    private final boolean noSwap;
    private final String outputDirectory;  // Not used in the JAR case
    private final boolean includeDate;

    /**
     * This initializes the GeneratorRunner with the values needed to run
     * 
     * @param fileOrDirectory The fileOrDirectory to inspect for services
     * @param outjarName The name of the jar file to create (can be the fileOrDirectory)
     * @param locatorName The name of the locator these files should be put into
     * @param verbose true if this should print information about progress
     * @param searchPath The path-separator delimited list of files or directories to search for
     *   contracts and qualifiers and various other annotations
     */
    public GeneratorRunner(String fileOrDirectory,
            String outjarName,
            String locatorName,
            boolean verbose,
            String searchPath,
            boolean noSwap,
            String outputDirectory,
            boolean includeDate) {
        this.fileOrDirectory = fileOrDirectory;
        this.outjarName = outjarName;
        this.locatorName = locatorName;
        this.verbose = verbose;
        this.noSwap = noSwap;
        this.outputDirectory = outputDirectory;
        utilities = new Utilities(verbose, searchPath);
        this.includeDate = includeDate;
    }
    
    /**
     * Does the work of writing out the inhabitants file to the proper location
     * 
     * @throws AssertionError On an error such as not being able to find the
     * proper file
     * @throws IOException On IO error
     */
    public void go() throws AssertionError, IOException {
        File toInspect = new File(fileOrDirectory);
        
        if (!toInspect.exists()) {
            throw new AssertionError("Could not find file: " + toInspect.getAbsolutePath());
        }
        
        List<DescriptorImpl> allDescriptors;
        if (toInspect.isDirectory()) {
            allDescriptors = findAllServicesFromDirectory(toInspect, toInspect);
            if (allDescriptors.isEmpty()) return;
            writeToDirectory(allDescriptors);
        }
        else {
            allDescriptors = findAllServicesFromJar(toInspect);
            writeToJar(toInspect, allDescriptors);
        }
        
    }
    
    private List<DescriptorImpl> findAllServicesFromDirectory(File directory, File parent) throws IOException {
        List<DescriptorImpl> retVal = new LinkedList<DescriptorImpl>();
        
        File subDirectories[] = directory.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
            
        });
        
        for (File subDirectory : subDirectories) {
            retVal.addAll(findAllServicesFromDirectory(subDirectory, parent));
        }
        
        // Now get all the class files from this directory itself
        File candidates[] = directory.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(DOT_CLASS);
            }
        });
        
        for (File candidate : candidates) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(candidate);
                
                LinkedList<DescriptorImpl> dis = createDescriptorIfService(fis, parent);
                retVal.addAll(dis);
            }
            finally {
                if (fis != null) {
                    try {
                        fis.close();
                    }
                    catch (IOException ioe) {
                        // ignore
                    }
                }
            }
        }
        
        return retVal;
    }


    private void writeToDirectory(List<DescriptorImpl> allDescriptors) throws IOException {
        Map<String, List<DescriptorImpl>> targetHabitatMap = new HashMap<String, List<DescriptorImpl>>();
        targetHabitatMap.put(locatorName, new ArrayList<DescriptorImpl>());
        for (DescriptorImpl d : allDescriptors) {
            List<String> habitats = d.getMetadata().get(TARGET_HABITATS);
            if (habitats != null) {
                StringTokenizer tokenizer = new StringTokenizer(habitats.get(0), ";");
                while (tokenizer.hasMoreTokens()) {
                    String habitatName = tokenizer.nextToken();
                    List<DescriptorImpl> descriptorsForHabitat = targetHabitatMap.get(habitatName);
                    if (descriptorsForHabitat == null) {
                        descriptorsForHabitat = new ArrayList<DescriptorImpl>();
                        targetHabitatMap.put(habitatName, descriptorsForHabitat);
                    }
                    descriptorsForHabitat.add(d);
                    System.out.println(d.getName() + " Will be an inhabitant of ==> " + habitatName);
                }
            } else {
                List<DescriptorImpl> descriptorsForHabitat = targetHabitatMap.get(locatorName);
                descriptorsForHabitat.add(d);
            }
        }

        for (String targetHabitatName : targetHabitatMap.keySet()) {
            List<DescriptorImpl> descriptors = targetHabitatMap.get(targetHabitatName);
            if (descriptors.size() == 0) {
                continue;
            }
            
            File inhabitantsDir = new File(outputDirectory);
            File outputFile = new File(inhabitantsDir, targetHabitatName);

            if (!inhabitantsDir.exists()) {
                inhabitantsDir.mkdirs();
            }

            File noSwapFile = null;
            boolean directWrite = false;
            if (noSwap || !outputFile.exists()) {
                directWrite = true;
                if (outputFile.exists()) {
                    if (!outputFile.delete()) {
                        throw new IOException("Could not delete existing inhabitant file " +
                                outputFile.getAbsolutePath() + " in the noSwap case");
                    }
                }

                noSwapFile = outputFile;
            }

            File writeMeFile = writeInhabitantsFile(descriptors, noSwapFile, inhabitantsDir);

            if (!directWrite) {
                // OK, now swap it
                if (outputFile.exists()) {
                    if (!outputFile.delete()) {
                        throw new IOException("Could not delete existing inhabitant file " + outputFile.getAbsolutePath());
                    }
                }

                String tmpFileAbsolutePath = writeMeFile.getAbsolutePath();
                if (verbose) {
                    System.out.println("Swapping " + tmpFileAbsolutePath + " to " + outputFile.getAbsolutePath());
                }

                if (!writeMeFile.renameTo(outputFile)) {
                    throw new IOException("Could not move generated inhabitant file " + tmpFileAbsolutePath +
                            " to " + outputFile.getAbsolutePath());
                }
            }

        }
    }
    
    private void writeToJar(File jarFile, List<DescriptorImpl> descriptors) throws IOException {
        File outjar = new File(outjarName);
        File writeMeFile = writeInhabitantsFile(descriptors, null, outjar.getParentFile());
        writeMeFile.deleteOnExit();
        
        byte buffer[] = new byte[1024];
        
        File tmpJarFile = File.createTempFile(jarFile.getName(), ".tmp", outjar.getParentFile());
        
        FileInputStream fis = new FileInputStream(jarFile);
        ZipInputStream zis = new ZipInputStream(fis);
        
        FileOutputStream fos = new FileOutputStream(tmpJarFile);
        ZipOutputStream zos = new ZipOutputStream(fos);
        
        ZipEntry zentry = zis.getNextEntry();
        while (zentry != null) {
            String entryName = zentry.getName();
            
            if (entryName.equals(META_INF + "/" + INHABITANTS + "/" + locatorName)) {
                // Don't write out the old one
                zentry = zis.getNextEntry();
                continue;
            }
            
            zos.putNextEntry(new ZipEntry(entryName));
            
            int len;
            while ((len = zis.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
            
            zentry = zis.getNextEntry();
        }
        
        zis.close();
        
        if (!descriptors.isEmpty()) {
            zos.putNextEntry(new ZipEntry(META_INF + "/" + INHABITANTS + "/" + locatorName));
        
            FileInputStream desc_os = new FileInputStream(writeMeFile);
            int len;
            while ((len = desc_os.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
            desc_os.close();
        }
        
        zos.close();
        
        // All went well, replace the JAR file with the new and improved jar file
        String tmpFileName = tmpJarFile.getAbsolutePath();
        
        if (verbose) {
            System.out.println("Swapping jar file " + tmpFileName + " to " + outjar.getAbsolutePath());
        }
        
        if (!tmpJarFile.renameTo(outjar)) {
            throw new IOException("Unable to swap generated JAR file " + tmpFileName + " to " + outjar.getAbsolutePath());
        }
    }
    
    private File writeInhabitantsFile(List<DescriptorImpl> descriptors, File noSwapFile, File outDir) throws IOException {
        File outFile;
        if (noSwapFile != null) {
            outFile = noSwapFile;
        }
        else {
            outFile = File.createTempFile(locatorName, ".tmp", outDir);
        }
        
        if (verbose) {
            System.out.println("Writing " + descriptors.size() + " entries to file " + outFile.getAbsolutePath());
        }
        
        FileOutputStream fos = new FileOutputStream(outFile);
        
        PrintWriter pw = new PrintWriter(fos);
        
        pw.println("#");
        if (includeDate) {
            pw.println("# Generated on " + new Date() + " by hk2-inhabitant-generator");
        }
        else {
            pw.println("# Generated by hk2-inhabitant-generator");
        }
        pw.println("#");
        pw.println();
        
        for (DescriptorImpl di : descriptors) {
            di.writeObject(pw);
        }
        
        pw.close();
        fos.close();
        
        if (verbose) {
            System.out.println("Wrote " + descriptors.size() + " entries to inhabitant file " + outFile.getAbsolutePath());
        }
        
        return outFile;
    }
    
    private List<DescriptorImpl> findAllServicesFromJar(File jar) throws IOException {
        List<DescriptorImpl> retVal = new LinkedList<DescriptorImpl>();
        
        JarFile jarFile = new JarFile(jar);
        
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            
            String entryName = entry.getName();
            if (!entryName.endsWith(DOT_CLASS)) continue;
            
            InputStream is = null;
            try {
                is = jarFile.getInputStream(entry);
                
                LinkedList<DescriptorImpl> dis = createDescriptorIfService(is, jar);
                retVal.addAll(dis);
            }
            finally {
                if (is != null) {
                    try {
                        is.close();
                    }
                    catch (IOException ioe) {
                        // ignore
                    }
                }
            }
            
        }
        
        return retVal;
    }
    
    private LinkedList<DescriptorImpl> createDescriptorIfService(InputStream is, File searchHere) throws IOException {
        ClassReader reader = new ClassReader(is);
        
        ClassVisitorImpl cvi = new ClassVisitorImpl(utilities, verbose, searchHere);
        
        reader.accept(cvi, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        
        return cvi.getGeneratedDescriptor();
    }
}
