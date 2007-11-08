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
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.PrintWriter;

import java.util.List;
import java.util.Iterator;
import java.util.Properties;

import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.sun.org.apache.jdo.impl.enhancer.ClassFileEnhancer;
import com.sun.org.apache.jdo.impl.enhancer.ClassFileEnhancerHelper;
import com.sun.org.apache.jdo.impl.enhancer.ClassFileEnhancerTimer;
import com.sun.org.apache.jdo.impl.enhancer.EnhancerFatalError;
import com.sun.org.apache.jdo.impl.enhancer.EnhancerOptions;
import com.sun.org.apache.jdo.impl.enhancer.EnhancerUserException;
import com.sun.org.apache.jdo.impl.enhancer.JdoMetaMain;
import com.sun.org.apache.jdo.impl.enhancer.OutputStreamWrapper;
import com.sun.org.apache.jdo.impl.enhancer.jdo.impl.EnhancerFilter;



/**
 * JDO command line enhancer.
 *
 * @author Martin Zaun
 */
public class EnhancerMain
    extends JdoMetaMain
{
    /**
     *  The options and arguments.
     */
    protected EnhancerOptions options;

    /**
     *  The byte code enhancer.
     */
    protected ClassFileEnhancer enhancer;

    /**
     * Creates an instance.
     */
    public EnhancerMain(PrintWriter out,
                        PrintWriter err)
    {
        this(out, err, new EnhancerOptions(out, err));
    }

    /**
     * Creates an instance.
     */
    public EnhancerMain(PrintWriter out,
                        PrintWriter err,
                        EnhancerOptions options)
    {
        super(out, err, options);
        this.options = options;
    }

    // ----------------------------------------------------------------------

    /**
     * Enhances all files entered in the command line.
     *
     * @param  classNames  List of class names.
     * @param  classFileNames  List of class file names.
     * @param  archiveFileNames  List of archive file names.
     */
    private int enhanceInputFiles(List classNames,
                                  List classFileNames,
                                  List archiveFileNames)
    {
        int res = 0;
        try {
            String name = null;
            for (Iterator i = archiveFileNames.iterator(); i.hasNext();) {
                try {
                    name = (String)i.next();
                    enhanceArchiveFile(name);
                } catch (EnhancerUserException ex) {
                    printlnErr("Error while enhancing " + name, ex, 
                               options.verbose.value);
                    res++;
                    continue;
                }
            }
            for (Iterator i = classFileNames.iterator(); i.hasNext();) {
                try {
                    name = (String)i.next();
                    enhanceClassFile(openFileInputStream(name));
                } catch (EnhancerUserException ex) {
                    printlnErr("Error while enhancing " + name, ex, 
                               options.verbose.value);
                    res++;
                    continue;
                }
            }
            for (Iterator i = classNames.iterator(); i.hasNext();) {
                try {
                    name = (String)i.next();
                    enhanceClassFile(openClassInputStream(name));
                } catch (EnhancerUserException ex) {
                    printlnErr("Error while enhancing " + name, ex, 
                               options.verbose.value);
                    res++;
                    continue;
                }
            }
        } catch (IOException ex) {
            printlnErr("IO Error while enhancing", ex, options.verbose.value);
            return ++res;
        } catch (EnhancerFatalError ex) {
            // enhancer is not anymore guaranteed to be consistent
            printlnErr("Fatal error while enhancing", ex, options.verbose.value);
            enhancer = null;
            return ++res;
        }
        return res;
    }

    /**
     * Enhances a classfile.
     *
     * @param  in  The input stream of the classfile.
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
                             getClassFileName(wrapper.getClassName()), temp);
        } finally {
            closeInputStream(in);
            closeOutputStream(out);
        }
    }

    /**
     * Enhances a archive file.
     *
     * @param  fileName  The filename of the archive file.
     */
    private void enhanceArchiveFile(String fileName)
        throws IOException, EnhancerUserException, EnhancerFatalError
    {
        ZipInputStream in = null;
        ZipOutputStream out = null;
        try {
            final File temp = File.createTempFile("enhancer", ".zip");
            in = new ZipInputStream(new BufferedInputStream(
                new FileInputStream(new File(fileName))));
            out = new ZipOutputStream(new BufferedOutputStream(
                new FileOutputStream(temp)));

            // enhance the archive file
            final boolean enhanced
                = ClassFileEnhancerHelper.enhanceZipFile(enhancer, in, out);

            // create the output file
            closeOutputStream(out);
            out = null;
            createOutputFile(enhanced, new File(fileName).getName(), temp);
        } finally {
            closeOutputStream(out);
            closeInputStream(in);
        }
    }

    /**
     * Creates a file object that represents the output archive file for
     * a given archive file to enhance.
     *
     * @param  archiveFileName  the input archive file name
     * @return  the output archive file
     */
    private File createArchiveOutputFile(String archiveFileName)
    {
        return new File(options.destDir.value,
                        new File(archiveFileName).getName());
    }

    /**
     * Creates the output file for an enhanced class- or archive file. If the
     * enhanced file is written back depends on the command line options.
     *
     * @param  enhanced  Has the input file been enhanced?
     * @param  fileName  The name of the output file.
     * @param  temp      The temp file, the output is written to.
     * @exception  IOException  If the file could not be created.
     */
    private void createOutputFile(boolean enhanced,
                                  String fileName,
                                  File temp)
        throws IOException
    {
        //noWrite or (not enhanced and not forceWrite)
        if (options.noWrite.value
            || (!enhanced && !options.forceWrite.value)) {
            temp.deleteOnExit();
            return;
        }

        // create file and its parent directory
        final File file = new File(options.destDir.value, fileName);
        final File dir = file.getAbsoluteFile().getParentFile();
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Error creating directory '"
                                  + dir.getAbsolutePath() + "'.");
        }

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
     * Closes an output stream.
     *
     * @param  out  the output stream
     */
    private void closeOutputStream(OutputStream out)
    {
        if (out != null) {
            try {
                out.close();
            } catch (IOException ex) {
                printlnErr("", ex, options.verbose.value);
            }
        }
    }

    // ----------------------------------------------------------------------

    /**
     * Initializes all components.
     */
    protected void init()
        throws EnhancerFatalError, EnhancerUserException
    {
        super.init();

        final Properties props = new Properties();
        if (options.verbose.value) {
            props.put(EnhancerFilter.VERBOSE_LEVEL,
                      EnhancerFilter.VERBOSE_LEVEL_VERBOSE);
        }
        
        if (options.doTiming.value) {
            props.put(EnhancerFilter.DO_TIMING_STATISTICS,
                      Boolean.TRUE.toString());
        }

        if (options.dumpClass.value) {
            props.put(EnhancerFilter.DUMP_CLASS,
                      Boolean.TRUE.toString());
        }

        if (options.noAugment.value) {
            props.put(EnhancerFilter.NO_AUGMENT,
                      Boolean.TRUE.toString());
        }

        if (options.noAnnotate.value) {
            props.put(EnhancerFilter.NO_ANNOTATE,
                      Boolean.TRUE.toString());
        }

        try {
            enhancer = new EnhancerFilter(jdoMeta, props, out, err);
            if (options.doTiming.value) {
                // wrap with timing byte-code enhancer
                enhancer = new ClassFileEnhancerTimer(enhancer);
            }
        } catch (EnhancerUserException ex) {
            printlnErr("Error while creating the enhancer", ex, 
                       options.verbose.value);
            throw ex;
        } catch (EnhancerFatalError ex) {
            // enhancer is not anymore guaranteed to be consistent
            printlnErr("Fatal error while creating the enhancer", ex, 
                       options.verbose.value);
            enhancer = null;
            throw ex;
        } catch (RuntimeException ex) {
            // enhancer is not anymore guaranteed to be consistent
            printlnErr("Internal error while creating the enhancer", ex, 
                       options.verbose.value);
            enhancer = null;
            throw new EnhancerFatalError(ex);
        }
    }

    /**
     * Run the enhancer.
     */
    protected int process()
    {
        return enhanceInputFiles(options.classNames,
                                 options.classFileNames,
                                 options.archiveFileNames);
    }

    // ----------------------------------------------------------------------

    /**
     * Runs this class
     */
    static public void main(String[] args)
    {
        final PrintWriter out = new PrintWriter(System.out, true);
        out.println("--> EnhancerMain.main()");
        //out.println("JDO RI Class-File Enhancer");
        final EnhancerMain main = new EnhancerMain(out, out);
        int res = main.run(args);
        //out.println("done.");
        out.println("<-- EnhancerMain.main(): exit = " + res);
        System.exit(res);
    }
}
