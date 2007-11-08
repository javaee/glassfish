/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.sun.org.apache.jdo.enhancer;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.PrintWriter;
import java.io.FileReader;
import java.io.BufferedReader;

import java.util.Map;
import java.util.List;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;

import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipException;

import java.net.URL;

import com.sun.org.apache.jdo.impl.enhancer.ClassFileEnhancer;
import com.sun.org.apache.jdo.impl.enhancer.ClassFileEnhancerHelper;
import com.sun.org.apache.jdo.impl.enhancer.ClassFileEnhancerTimer;
import com.sun.org.apache.jdo.impl.enhancer.EnhancerFatalError;
import com.sun.org.apache.jdo.impl.enhancer.EnhancerUserException;
import com.sun.org.apache.jdo.impl.enhancer.OutputStreamWrapper;
import com.sun.org.apache.jdo.impl.enhancer.jdo.impl.EnhancerFilter;
import com.sun.org.apache.jdo.impl.enhancer.meta.EnhancerMetaData;
import com.sun.org.apache.jdo.impl.enhancer.meta.EnhancerMetaDataFatalError;
import com.sun.org.apache.jdo.impl.enhancer.meta.model.EnhancerMetaDataJDOModelImpl;
import com.sun.org.apache.jdo.impl.enhancer.meta.prop.EnhancerMetaDataPropertyImpl;
import com.sun.org.apache.jdo.impl.enhancer.meta.util.EnhancerMetaDataBaseModel;
import com.sun.org.apache.jdo.impl.enhancer.meta.util.EnhancerMetaDataTimer;
import com.sun.org.apache.jdo.impl.enhancer.util.CombinedResourceLocator;
import com.sun.org.apache.jdo.impl.enhancer.util.ListResourceLocator;
import com.sun.org.apache.jdo.impl.enhancer.util.PathResourceLocator;
import com.sun.org.apache.jdo.impl.enhancer.util.ResourceLocator;
import com.sun.org.apache.jdo.impl.enhancer.util.ResourceLocatorTimer;
import com.sun.org.apache.jdo.impl.enhancer.util.Support;





/**
 * Main is the starting point for the persistent filter tool.
 */
public class Main
    extends Support
{
    // return values of main()
    static public final int OK = 0;
    static public final int USAGE_ERROR = -1;
    static public final int METADATA_ERROR = -2;
    static public final int CLASS_LOCATOR_ERROR = -3;
    static public final int INTERNAL_ERROR = -4;

    /**
     *  The stream to write messages to.
     */
    private final PrintWriter out = new PrintWriter(System.out, true);

    /**
     *  The stream to write error messages to.
     */
    private final PrintWriter err = new PrintWriter(System.err, true);

    /**
     *  The command line options.
     */
    private final CmdLineOptions opts = new CmdLineOptions();

    /**
     *  The byte code enhancer.
     */
    private ClassFileEnhancer enhancer;

    /**
     *  The locator for classes.
     */
    protected ResourceLocator classLocator;

    /**
     *  The metadata for the enhancer.
     */
    private EnhancerMetaData jdoMetaData;

    /**
     * Construct a filter tool instance
     */
    public Main()
    {}

    // ----------------------------------------------------------------------

    /**
     * This is where it all starts.
     */
    public static void main(String[] argv)
    {
        int res;
        final Main main = new Main();

        //@olsen: added support for timing statistics
        try {
            res = main.process(argv);
        } catch (RuntimeException ex) {
            main.out.flush();
            main.err.println("Internal error while postprocessing: "
                             + ex.getMessage());
            ex.printStackTrace(main.err);
            main.err.flush();
            res = INTERNAL_ERROR;
        } finally {
            //@olsen: added support for timing statistics
            if (main.opts.doTiming) {
                Support.timer.print();
            }
        }
        System.exit(res);
    }

    /**
     * Process command line options and run enhancer.
     */
    public int process(String[] argv)
    {
        int res;

        if ((res = opts.processArgs(argv)) != OK) {
            printMessage("aborted with errors.");
            return res;
        }

        //@olsen: added support for timing statistics
        try {
            if (opts.doTiming) {
                timer.push("Main.process(String[])");
            }

            if ((res = createEnhancer()) != OK) {
                printMessage("aborted with errors.");
                return res;
            }

            if ((res = enhanceInputFiles(opts.classNames,
                                         opts.classFileNames,
                                         opts.zipFileNames,
                                         opts.jdoFileNames)) != OK) {
                printMessage("aborted with errors.");
                return res;
            }

            printMessage("done.");
            return 0;
        } finally {
            if (opts.doTiming) {
                timer.pop();
            }
        }
    }

    // ----------------------------------------------------------------------

    /**
     *  A class for holding the command line options.
     */
    private class CmdLineOptions
    {
        final List classNames = new ArrayList();        
        final List classFileNames = new ArrayList();        
        final List zipFileNames = new ArrayList();        
        final List jdoFileNames = new ArrayList();        
        String sourcePath = null;
        String destinationDirectory = null;
        String propertiesFileName = null;
        boolean doTiming = false;
        boolean verbose = false;
        boolean quiet = false;
        boolean forceWrite = false;
        boolean noWrite = false;
        boolean dumpClass = false;
        boolean noAugment = false;
        boolean noAnnotate = false;

        /**
         * Print a usage message to System.err.
         */
        public void usage() {
            err.println("Usage: main <options> <arguments>...");
            err.println("Options:");
            err.println("  -h, --help               print usage message and exit gently");
            err.println("  -v, --verbose            print verbose messages");
            err.println("  -q, --quiet              supress warnings");
            err.println("  -s, --sourcepath <path>  source path for jdo and classfiles");
            err.println("  -d, --destdir <dir>      destination directory for output files");
            err.println("  -f, --force              overwrite output files");
            err.println("  -n, --nowrite            never write output files");
            err.println("  -t, --timing             do timing messures");
            err.println();
            err.println("Debugging Options:");
            err.println("      --properties <file>  use property file for meta data");
            err.println("      --dumpclass          print out disassembled code of classes");
            err.println("      --noaugment          do not enhance for persistence-capability");
            err.println("      --noannotate         do not enhance for persistence-awareness");
            err.println();
            err.println("Arguments:");
            //err.println("  <class>      the fully qualified name of a Java class");
            err.println("  <jdofile>    the name of a .jdo file");
            err.println("  <classfile>  the name of a .class file");
            //err.println("  <zipfile>    the name of a .zip or .jar file");
            err.println();
            err.println("Returns a non-zero value in case of errors.");
        }

        /**
         * Process command line options.
         */
        protected int processArgs(String[] argv)
        {
            final Collection inputNames = new ArrayList();
            for (int i = 0; i < argv.length; i++) {
                final String arg = argv[i];
                if (arg.equals("-h")
                    || arg.equals("--help")) {
                    usage();
                    return OK;
                }
                if (arg.equals("-v")
                    || arg.equals("--verbose")) {
                    verbose = true;
                    quiet = false;
                    continue;
                }
                if (arg.equals("-q")
                    || arg.equals("--quiet")) {
                    quiet = true;
                    verbose = false;
                    continue;
                }
                if (arg.equals("-t") ||
                    arg.equals("--timing")) {
                    doTiming = true;
                    continue;
                }
                if (arg.equals("-f")
                    || arg.equals("--force")) {
                    forceWrite = true;
                    noWrite = false;
                    continue;
                }
                if (arg.equals("-n")
                    || arg.equals("--nowrite")) {
                    noWrite = true;
                    forceWrite = false;
                    continue;
                }
                if (arg.equals("--dumpclass")) {
                    dumpClass = true;
                    continue;
                }
                if (arg.equals("--noaugment")) {
                    noAugment = true;
                    continue;
                }
                if (arg.equals("--noannotate")) {
                    noAnnotate = true;
                    continue;
                }
                if (arg.equals("-s")
                    || arg.equals("--sourcepath")) {
                    if (argv.length - i < 2) {
                        printError("Missing argument to the -s/--sourcepath option", null);
                        usage();
                        return USAGE_ERROR;
                    }
                    sourcePath = argv[++i];
                    continue;
                }
                if (arg.equals("-d")
                    || arg.equals("--destdir")) {
                    if (argv.length - i < 2) {
                        printError("Missing argument to the -d/-destdir option", null);
                        usage();
                        return USAGE_ERROR;
                    }
                    destinationDirectory = argv[++i];
                    continue;
                }
                if (arg.equals("--properties")) {
                    if (argv.length - i < 2) {
                        printError("Missing argument to the --properties option", null);
                        usage();
                        return USAGE_ERROR;
                    }
                    propertiesFileName = argv[++i];
                    continue;
                }
                if (arg.length() > 0 && arg.charAt(0) == '-') {
                    printError("Unrecognized option:" + arg, null);
                    usage();
                    return USAGE_ERROR;
                }
                if (arg.length() == 0) {
                    printMessage("Ignoring empty command line argument.");
                    continue;
                }

                inputNames.add(arg);
            }

            // group input file arguments
            for (Iterator names = inputNames.iterator(); names.hasNext();) {
                final String name = (String)names.next();
                if (isJdoFileName(name)) {
                    jdoFileNames.add(name);
                } else if (isClassFileName(name)) {
                    classFileNames.add(name);
                } else if (isZipFileName(name)) {
                    zipFileNames.add(name);
                } else {
                    classNames.add(name);
                }
            }

            if (verbose) {
                printArgs();
            }
            return checkArgs();
        }

        /**
         * Check command line options.
         */
        protected int checkArgs()
        {
            // at least one class must be specified
            if (classNames.isEmpty()
                && classFileNames.isEmpty()
                && zipFileNames.isEmpty()) {
                final String msg
                    = "No classes specified";
                printError(msg, null);
                usage();
                return USAGE_ERROR;
            }

            // at least one meta-data source must be specified for classfiles
            if (classFileNames.size() > 0
                && (jdoFileNames.isEmpty()
                    && propertiesFileName == null
                    && sourcePath == null)) {
                final String msg
                    = "No JDO meta-data source specified for class files";
                printError(msg, null);
                usage();
                return USAGE_ERROR;
            }

            // either jdo files or jdo properties specified
            if (!jdoFileNames.isEmpty() && propertiesFileName != null) {
                final String msg
                    = "Cannot have both jdo files and properties specified";
                printError(msg, null);
                usage();
                return USAGE_ERROR;
            }

            return OK;
        }

        /**
         * Print command line options.
         */
        protected void printArgs()
        {
            out.println("Enhancer: options:");
            out.println("    verbose = " + verbose);
            out.println("    quiet = " + quiet);
            out.println("    forceWrite = " + forceWrite);
            out.println("    noWrite = " + noWrite);
            out.println("    sourcePath = " + sourcePath);
            out.println("    destinationDirectory = " + destinationDirectory);
            out.println("    propertiesFileName = " + propertiesFileName);
            out.println("    doTiming = " + doTiming);
            out.println("    classNames = {");
            for (Iterator i = classNames.iterator(); i.hasNext();) {
                out.println("        " + i.next());
            }
            out.println("    }");
            out.println("    jdoFileNames = {");
            for (Iterator i = jdoFileNames.iterator(); i.hasNext();) {
                out.println("        " + i.next());
            }
            out.println("    }");
            out.println("    classFileNames = {");
            for (Iterator i = classFileNames.iterator(); i.hasNext();) {
                out.println("        " + i.next());
            }
            out.println("    }");
            out.println("    zipFileNames = {");
            for (Iterator i = zipFileNames.iterator(); i.hasNext();) {
                out.println("        " + i.next());
            }
            out.println("    }");
            out.println("    dumpClass = " + dumpClass);
            out.println("    noAugment = " + noAugment);
            out.println("    noAnnotate = " + noAnnotate);
        }
    }

    private int initClassLocator()
    {
        final boolean verbose = opts.verbose;
        final List classFileNames = opts.classFileNames;
        final List zipFileNames = opts.zipFileNames;
        final String sourcePath = opts.sourcePath;
        try {
            final List locators = new ArrayList();

            // create resource locator for specified class files
            if (classFileNames != null && !classFileNames.isEmpty()) {
                final ResourceLocator classes
                    = new ListResourceLocator(out, verbose, classFileNames);
                if (verbose) {
                    out.println("Class Locator: using class files: {");
                    for (Iterator i = classFileNames.iterator(); i.hasNext();) {
                        out.println("    " + i.next());
                    }
                    out.println("}");
                }
                locators.add(classes);
            }

            // create resource locator for specified zip files
            if (zipFileNames != null && !zipFileNames.isEmpty()) {
                final StringBuffer s = new StringBuffer();
                final Iterator i = zipFileNames.iterator();
                s.append(i.next());
                while (i.hasNext()) {
                    s.append(File.pathSeparator + i.next());
                }
                final ResourceLocator zips
                    = new PathResourceLocator(out, verbose, s.toString());
                if (verbose)
                    out.println("Class Locator: using jar/zip files: "
                                + s.toString());
                locators.add(zips);
            }

            // create resource locator for specified source path
            if (sourcePath != null && sourcePath.length() > 0) {
                final ResourceLocator path
                    = new PathResourceLocator(out, verbose, sourcePath);
                if (verbose)
                    out.println("Class Locator: using source path: "
                                + sourcePath);
                locators.add(path);
            }

            // print warning if no classes specified
            affirm(!locators.isEmpty());
            //if (locators.isEmpty()) {
            //    printWarning(getI18N("enhancer.using_no_classes"));
            //}

            // init class locators
            classLocator
                = new CombinedResourceLocator(out, verbose, locators);

            // wrap with timing class locator
            if (opts.doTiming) {
                classLocator = new ResourceLocatorTimer(classLocator);
            }
        } catch (IOException ex) {
            printError("Cannot initialize resource locator for classes", ex);
            return CLASS_LOCATOR_ERROR;
        }
        return OK;
    }

    private int initEnhancerMetaData()
    {
        final boolean verbose = opts.verbose;
        final String propertiesFileName = opts.propertiesFileName;
        final List jdoFileNames = opts.jdoFileNames;
        final List zipFileNames = opts.zipFileNames;
        final String sourcePath = opts.sourcePath;
        try {
            if (propertiesFileName != null) {
                jdoMetaData
                    = new EnhancerMetaDataPropertyImpl(out, verbose,
                                                       propertiesFileName);
            } else {
                jdoMetaData
                    = new EnhancerMetaDataJDOModelImpl(out, verbose,
                                                       jdoFileNames,
                                                       zipFileNames,
                                                       sourcePath);
            }

            // wrap with timing meta data object
            if (opts.doTiming) {
                jdoMetaData = new EnhancerMetaDataTimer(jdoMetaData);
            }
        } catch (EnhancerMetaDataFatalError ex) {
            printError("Cannot initialize JDO meta-data source", ex);
            return METADATA_ERROR;
        }
        return OK;
    }
    
    private int createEnhancer()
    {
        int res0 = initClassLocator();
        if (res0 < 0) {
            return res0;
        }
        affirm(classLocator != null);

        int res = initEnhancerMetaData();
        if (res < 0) {
            return res;
        }
        affirm(jdoMetaData != null);

        final Properties props = new Properties();
        if (opts.verbose) {
            props.put(EnhancerFilter.VERBOSE_LEVEL,
                      EnhancerFilter.VERBOSE_LEVEL_VERBOSE);
        }
        
        if (opts.doTiming) {
            props.put(EnhancerFilter.DO_TIMING_STATISTICS,
                      Boolean.TRUE.toString());
        }

        if (opts.dumpClass) {
            props.put(EnhancerFilter.DUMP_CLASS,
                      Boolean.TRUE.toString());
        }

        if (opts.noAugment) {
            props.put(EnhancerFilter.NO_AUGMENT,
                      Boolean.TRUE.toString());
        }

        if (opts.noAnnotate) {
            props.put(EnhancerFilter.NO_ANNOTATE,
                      Boolean.TRUE.toString());
        }

        try {            
            enhancer = new EnhancerFilter(jdoMetaData, props, out, err);
            if (opts.doTiming) {
                // wrap with timing byte-code enhancer
                enhancer = new ClassFileEnhancerTimer(enhancer);
            }
            return 0;
        } catch (EnhancerUserException ex) {
            printError("Error while creating the enhancer", ex);
            return -1;
        } catch (EnhancerFatalError ex) {
            // enhancer is not anymore guaranteed to be consistent
            printError("Fatal error while creating the enhancer", ex);
            enhancer = null;
            return -1;
        }
    }

    // ----------------------------------------------------------------------

    /**
     *  Enhances all files entered in the command line.
     *
     *  @param  classNames  List of class names.
     *  @param  classFileNames  List of class file names.
     *  @param  zipFileNames  List of zip file names.
     *  @param  jdoFileNames  List of jdo file names.
     */
    private int enhanceInputFiles(List classNames,
                                  List classFileNames,
                                  List zipFileNames,
                                  List jdoFileNames)
    {
        int res = 0;
        try {
            String name = null;
            for (Iterator i = zipFileNames.iterator(); i.hasNext();) {
                try {
                    name = (String)i.next();
                    enhanceZipFile(name);
                } catch (EnhancerUserException ex) {
                    printError("Error while enhancing " + name, ex);
                    res++;
                    continue;
                }
            }
            for (Iterator i = classFileNames.iterator(); i.hasNext();) {
                try {
                    name = (String)i.next();
                    enhanceClassFile(openFileInputStream(name));
                } catch (EnhancerUserException ex) {
                    printError("Error while enhancing " + name, ex);
                    res++;
                    continue;
                }
            }
            for (Iterator i = classNames.iterator(); i.hasNext();) {
                try {
                    name = (String)i.next();
                    enhanceClassFile(openClassInputStream(name));
                } catch (EnhancerUserException ex) {
                    printError("Error while enhancing " + name, ex);
                    res++;
                    continue;
                }
            }
        } catch (IOException ex) {
            printError("IO Error while enhancing", ex);
            return ++res;
        } catch (EnhancerFatalError ex) {
            // enhancer is not anymore guaranteed to be consistent
            printError("Fatal error while enhancing", ex);
            enhancer = null;
            return ++res;
        }
        return res;
    }

    /**
     *  Enhances a classfile.
     *
     *  @param  in  The input stream of the classfile.
     */
    private void enhanceClassFile(InputStream in)
        throws IOException, EnhancerUserException, EnhancerFatalError
    {
        OutputStream out = null;
        try {
            final File temp = File.createTempFile("enhancer", ".class");
            out = new BufferedOutputStream(new FileOutputStream(temp));

            //enhance
            final OutputStreamWrapper wrapper = new OutputStreamWrapper(out);
            final boolean enhanced = enhancer.enhanceClassFile(in, wrapper);

            closeOutputStream(out);
            out = null;
            createOutputFile(enhanced,
                             createClassFileName(wrapper.getClassName()),
                             temp);
        } finally {
            closeInputStream(in);
            closeOutputStream(out);
        }
    }

    /**
     *  Enhances a zipfile.
     *
     *  @param  filename  The filename of the zipfile.
     */
    private void enhanceZipFile(String filename)
        throws IOException, EnhancerUserException, EnhancerFatalError
    {
        ZipInputStream in = null;
        ZipOutputStream out = null;
        try {
            final File temp = File.createTempFile("enhancer", ".zip");
            in = new ZipInputStream(new BufferedInputStream(
                new FileInputStream(new File(filename))));
            out = new ZipOutputStream(new BufferedOutputStream(
                new FileOutputStream(temp)));

            //enhance the zipfile
            final boolean enhanced
                = ClassFileEnhancerHelper.enhanceZipFile(enhancer, in, out);

            //create the output file
            closeOutputStream(out);
            out = null;
            createOutputFile(enhanced, new File(filename).getName(), temp);
        } finally {
            closeOutputStream(out);
            closeInputStream(in);
        }
    }

    /**
     *  Opens an input stream for the given filename
     *
     *  @param  filename  The name of the file.
     *  @return  The input stream.
     *  @exception  FileNotFoundException  If the file could not be found.
     */
    static private InputStream openFileInputStream(String filename)
        throws FileNotFoundException
    {
     	return new BufferedInputStream(new FileInputStream(new File(filename)));
    }

    /**
     * Opens an input stream for the given classname. The input stream is
     * created via an URL that is obtained by the value of the sourcepath
     * option and zip/jar file arguments.
     * 
     * @param  classname  The name of the class (dot-notation).
     * @return  The input stream.
     * @exception IOException If an I/O error occured.
     */
    private InputStream openClassInputStream(String classname)
        throws IOException
    {
        final String resourcename = createClassFileName(classname);
        return classLocator.getInputStreamForResource(resourcename);
    }

    /**
     *  Creates a file object that represents the output zipfile for a given
     *  zipfile to enhance.
     *
     *  @param  zipfilename  The input zipfile name.
     *  @return  The output zipfile name.
     */
    private File createZipOutputFile(String zipfilename)
    {
        return new File(opts.destinationDirectory,
                        new File(zipfilename).getName());
    }

    /**
     *  Creates the output file for an enhaced class- or zipfile. If the
     *  enhanced file is written back depends on the command line options.
     *
     *  @param  enhanced  Has the input file been enhanced?
     *  @param  filename  The name of the output file.
     *  @param  temp      The temp file, the output is written to.
     *  @exception  IOException  If the file could not be created.
     */
    private void createOutputFile(boolean  enhanced,
                                  String   filename,
                                  File     temp)
        throws IOException
    {
        //noWrite or (not enhanced and not forceWrite)
        if (opts.noWrite || (!enhanced && !opts.forceWrite)) {
            temp.deleteOnExit();
            return;
        }

        File file = new File(opts.destinationDirectory, filename);
        createPathOfFile(file);
        file.delete();  //delete old file if exists
        boolean renamed = temp.renameTo(file);
        if (!renamed) {
            //@dave: empirical evidence shows that renameTo does not allow for
            // crossing filesystem boundaries.  If it fails, try "by hand".
            InputStream in = null;
            OutputStream out = null;
            try {
                in = new FileInputStream(temp);
                out = new FileOutputStream(file);
                int PAGESIZE = 4096; // Suggest a better size?
                byte data[] = new byte[PAGESIZE];
                while (in.available() > 0) {
                    int numRead = in.read(data, 0, PAGESIZE);
                    out.write(data, 0, numRead);
                }
                renamed = true;
            } catch (IOException ex) {
                throw new IOException("Could not rename temp file '" +
                                      temp.getAbsolutePath() +
                                      "' to '" + file.getAbsolutePath()
                                      + "': " + ex);
            } finally {
                closeInputStream(in);
                closeOutputStream(out);
            }
            if (renamed) {
                temp.delete();  //delete temporary file
            }
            else {
                throw new IOException("Could not rename temp file '" +
                                      temp.getAbsolutePath() +
                                      "' to '" + file.getAbsolutePath() + "'.");
            }
        }
    }

    /**
     *  Closes an input stream.
     *
     *  @param  in  The input stream.
     */
    private void closeInputStream(InputStream in)
    {
        if (in != null) {
            try {
                in.close();
            } catch (IOException ex) {
                printError(null, ex);
            }
        }
    }

    /**
     *  Closes an output stream.
     *
     *  @param  out  The output stream.
     */
    private void closeOutputStream(OutputStream out)
    {
        if (out != null) {
            try {
                out.close();
            } catch (IOException ex) {
                printError(null, ex);
            }
        }
    }

    /**
     *  Tests if a filename is a classfile name (by testing if the filename
     *  ends with <code>".class"</code>).
     *
     *  @param  filename  The name of the file.
     *  @return  Do we have a potential classfile?
     */
    static private boolean isClassFileName(String filename)
    {
        return filename.toLowerCase().endsWith(".class");
    }

    /**
     *  Tests if a filename is a zipfile (by testing if the filename
     *  ends with <code>".zip"</code> or <code>".jar"</code>).
     *
     *  @param  filename  The name of the file.
     */
    static private boolean isZipFileName(String filename)
    {
        final int n = filename.length();
        if (n < 5) {
            return false;
        }
        final String ext = filename.substring(n - 4);
        return ext.equalsIgnoreCase(".zip") || ext.equalsIgnoreCase(".jar");
    }

    /**
     *  Tests if a filename is a jdo file name (by testing if the filename
     *  ends with <code>".jdo"</code>).
     *
     *  @param  filename  The name of the file.
     *  @return  Do we have a potential jdo file?
     */
    static private boolean isJdoFileName(String filename)
    {
        return filename.toLowerCase().endsWith(".jdo");
    }

    /**
     *  Creates a filename from a classname.
     *  This is done by replacing <code>'.'</code> by <code>'/'</code>.
     *
     *  @param  classname  The classname.
     *  @return  The filename.
     */
    static private String createClassFileName(String classname)
    {
        return classname.replace('.', '/') + ".class";
    }

    /**
     *  Creates only the path of the given file.
     *
     *  @param  file  The file.
     *  @exception  IOException  If an error occured.
     */
    static private void createPathOfFile(File file)
        throws IOException
    {
        File dir = file.getAbsoluteFile().getParentFile();
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Error creating directory '"
                                  + dir.getAbsolutePath() + "'.");
        }
    }

    // ----------------------------------------------------------------------

    /**
     *  Prints out an error.
     *
     *  @param  msg  The error message (can be <code>null</code>).
     *  @param  ex   An optional exception (can be <code>null</code>).
     */
    private void printError(String msg,
                            Throwable ex)
    {
        out.flush();
        if (msg != null) {
            err.println(msg);
        }
        if (ex != null) {
            if (opts.verbose) {
                ex.printStackTrace(err);
            }
            else {
                err.println(ex.toString());
            }
        }
    }

    /**
     *  Prints out a message.
     *
     *  @param  msg  The message.
     */
    private void printMessage(String msg)
    {
        out.println(msg);
    }
}
