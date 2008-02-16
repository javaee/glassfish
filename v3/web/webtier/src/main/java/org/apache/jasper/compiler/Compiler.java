/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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
package org.apache.jasper.compiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.Options;
import org.apache.jasper.servlet.JspServletWrapper;

/**
 * Main JSP compiler class.
 *
 * @author Anil K. Vijendran
 * @author Mandar Raje
 * @author Pierre Delisle
 * @author Kin-man Chung
 * @author Remy Maucherat
 * @author Mark Roth
 */

public class Compiler {
    private static com.sun.org.apache.commons.logging.Log commonsLog =
        com.sun.org.apache.commons.logging.LogFactory.getLog(Compiler.class);
    private static com.sun.org.apache.commons.logging.Log noOpLog =
        new com.sun.org.apache.commons.logging.impl.NoOpLog();

    // ----------------------------------------------------------------- Static

    // ----------------------------------------------------- Instance Variables

    protected JspCompilationContext ctxt;

    private ErrorDispatcher errDispatcher;
    private PageInfo pageInfo;
    private JspServletWrapper jsw;
    private TagFileProcessor tfp;
    private JavaCompiler javaCompiler;
    private boolean jspcMode;
    private com.sun.org.apache.commons.logging.Log log;
    private SmapUtil smapUtil;
    private Options options;
    private Node.Nodes pageNodes;
    private long jspModTime;

    // ------------------------------------------------------------ Constructor

    // Compiler for parsing only
    public Compiler(JspCompilationContext ctxt, JspServletWrapper jsw) {
        this.jsw = jsw;
        this.ctxt = ctxt;
        this.jspcMode = false;
        this.options = ctxt.getOptions();
        this.log = commonsLog;
        this.smapUtil = new SmapUtil(ctxt);
        this.errDispatcher = new ErrorDispatcher(jspcMode);
        this.javaCompiler = new AntJavaCompiler();
        javaCompiler.init(ctxt, errDispatcher, jspcMode);
    }

    public Compiler(JspCompilationContext ctxt, JspServletWrapper jsw,
                    boolean jspcMode) {
        this.jsw = jsw;
        this.ctxt = ctxt;
        this.jspcMode = jspcMode;
        this.options = ctxt.getOptions();
        this.log = jspcMode? noOpLog: commonsLog;
        this.smapUtil = new SmapUtil(ctxt);
        this.errDispatcher = new ErrorDispatcher(jspcMode);
        initJavaCompiler();
    }


    // --------------------------------------------------------- Public Methods


    /** 
     * Compile the jsp file into equivalent servlet in java source
     */
    private void generateJava() throws Exception {
        
        long t1, t2, t3, t4;
        t1 = t2 = t3 = t4 = 0;

        if (log.isDebugEnabled()) {
            t1 = System.currentTimeMillis();
        }

        // Setup page info area
        pageInfo = new PageInfo(new BeanRepository(ctxt.getClassLoader(),
                                                   errDispatcher),
                                ctxt.getJspFile());

        JspConfig jspConfig = options.getJspConfig();
        JspProperty jspProperty =
            jspConfig.findJspProperty(ctxt.getJspFile());

        /*
         * If the current uri is matched by a pattern specified in
         * a jsp-property-group in web.xml, initialize pageInfo with
         * those properties.
         */
        pageInfo.setELIgnored(JspUtil.booleanValue(
                                            jspProperty.isELIgnored()));
        pageInfo.setScriptingInvalid(JspUtil.booleanValue(
                                            jspProperty.isScriptingInvalid()));
        pageInfo.setTrimDirectiveWhitespaces(JspUtil.booleanValue(
                                            jspProperty.getTrimSpaces()));
        pageInfo.setDeferredSyntaxAllowedAsLiteral(JspUtil.booleanValue(
                                            jspProperty.getPoundAllowed()));
        if (jspProperty.getIncludePrelude() != null) {
            pageInfo.setIncludePrelude(jspProperty.getIncludePrelude());
        }
        if (jspProperty.getIncludeCoda() != null) {
	    pageInfo.setIncludeCoda(jspProperty.getIncludeCoda());
        }
        if (options.isDefaultBufferNone() && pageInfo.getBufferValue() == null){
            // Set to unbuffered if not specified explicitly
            pageInfo.setBuffer(0);
        }

        String javaFileName = ctxt.getServletJavaFileName();
        ServletWriter writer = null;

        try {
            // Setup the ServletWriter
            Writer javaWriter = javaCompiler.getJavaWriter(
                                    javaFileName,
                                    ctxt.getOptions().getJavaEncoding());
            writer = new ServletWriter(new PrintWriter(javaWriter));
            ctxt.setWriter(writer);

            // Reset the temporary variable counter for the generator.
            JspUtil.resetTemporaryVariableName();

	    // Parse the file
	    ParserController parserCtl = new ParserController(ctxt, this);
	    pageNodes = parserCtl.parse(ctxt.getJspFile());

	    if (ctxt.isPrototypeMode()) {
                // generate prototype .java file for the tag file
                Generator.generate(writer, this, pageNodes);
                writer.close();
                writer = null;
                return;
            }

            // Validate and process attributes
            Validator.validate(this, pageNodes);

            if (log.isDebugEnabled()) {
                t2 = System.currentTimeMillis();
            }

            // Collect page info
            Collector.collect(this, pageNodes);

            // Compile (if necessary) and load the tag files referenced in
            // this compilation unit.
            tfp = new TagFileProcessor();
            tfp.loadTagFiles(this, pageNodes);

            if (log.isDebugEnabled()) {
                t3 = System.currentTimeMillis();
            }
        
            // Determine which custom tag needs to declare which scripting vars
            ScriptingVariabler.set(pageNodes, errDispatcher);

            // Optimizations by Tag Plugins
            TagPluginManager tagPluginManager = options.getTagPluginManager();
            tagPluginManager.apply(pageNodes, errDispatcher, pageInfo);

            // Optimization: concatenate contiguous template texts.
            TextOptimizer.concatenate(this, pageNodes);

            // Generate static function mapper codes.
            ELFunctionMapper.map(this, pageNodes);

            // generate servlet .java file
            Generator.generate(writer, this, pageNodes);
            writer.close();
            writer = null;

            // The writer is only used during the compile, dereference
            // it in the JspCompilationContext when done to allow it
            // to be GC'd and save memory.
            ctxt.setWriter(null);

            if (log.isDebugEnabled()) {
                t4 = System.currentTimeMillis();
                log.debug("Generated "+ javaFileName + " total="
                          + (t4-t1) + " generate=" + (t4-t3)
                          + " validate=" + (t2-t1));
            }

        } catch (Exception e) {
            if (writer != null) {
                try {
                    writer.close();
                    writer = null;
                } catch (Exception e1) {
                    // do nothing
                }
            }
            // Remove the generated .java file
            javaCompiler.doJavaFile(false);
            throw e;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception e2) {
                    // do nothing
                }
            }
        }
        
        // JSR45 Support
        if (! options.isSmapSuppressed()) {
            smapUtil.generateSmap(pageNodes);
        }

        // If any proto type .java and .class files was generated,
        // the prototype .java may have been replaced by the current
        // compilation (if the tag file is self referencing), but the
        // .class file need to be removed, to make sure that javac would
        // generate .class again from the new .java file just generated.
        tfp.removeProtoTypeFiles(ctxt.getClassFileName());
    }


    /** 
     * Compile the servlet from .java file to .class file
     */
    private void generateClass()
        throws FileNotFoundException, JasperException, Exception {

        long t1 = 0;
        if (log.isDebugEnabled()) {
            t1 = System.currentTimeMillis();
        }

        String javaFileName = ctxt.getServletJavaFileName();
        String classpath = ctxt.getClassPath(); 
        String sep = System.getProperty("path.separator");

        // Initializing classpath
        ArrayList<File> cpath = new ArrayList<File>();
        HashSet<String> paths = new HashSet<String>();

        // Process classpath, which includes system classpath from compiler
        // options, plus the context classpath from the classloader
        String sysClassPath = options.getSystemClassPath();
        if (sysClassPath != null) {
            StringTokenizer tokenizer = new StringTokenizer(sysClassPath, sep);
            while (tokenizer.hasMoreElements()) {
                String path = tokenizer.nextToken();
                if (! paths.contains(path)) {
                    paths.add(path);
                    cpath.add(new File(path));
                }
            }
        }
        StringTokenizer tokenizer = new StringTokenizer(classpath, sep);
        while (tokenizer.hasMoreElements()) {
            String path = tokenizer.nextToken();
            if (! paths.contains(path)) {
                paths.add(path);
                cpath.add(new File(path));
            }
        }
        if(log.isDebugEnabled()) {
            log.debug("Using classpath: " + sysClassPath + sep + classpath);
        }
        javaCompiler.setClassPath(cpath);
        
        // Set debug info
        javaCompiler.setDebug(options.getClassDebugInfo());

        // Initialize and set java extensions
        String exts = System.getProperty("java.ext.dirs");
        if (exts != null) {
            javaCompiler.setExtdirs(exts);
        }

        if (options.getCompilerTargetVM() != null) {
            javaCompiler.setTargetVM(options.getCompilerTargetVM());
        }

        if (options.getCompilerSourceVM() != null) {
            javaCompiler.setSourceVM(options.getCompilerSourceVM());
        }

        // Start java compilation
        JavacErrorDetail[] javacErrors =
            javaCompiler.compile(ctxt.getFullClassName(), pageNodes);

        if (javacErrors != null) {
            // If there are errors, always generate java files to disk.
            javaCompiler.doJavaFile(true);

            log.error("Error compiling file: " + javaFileName);
            errDispatcher.javacError(javacErrors);
        }

        if (log.isDebugEnabled()) {
            long t2 = System.currentTimeMillis();
            log.debug("Compiled " + javaFileName + " " + (t2-t1) + "ms");
        }

        // Save or delete the generated Java files, depending on the
        // value of "keepgenerated" attribute
        javaCompiler.doJavaFile(ctxt.keepGenerated());

        // JSR45 Support
        if (!ctxt.isPrototypeMode() && !options.isSmapSuppressed()) {
            smapUtil.installSmap();
        }

        // START CR 6373479
        if (jsw != null && jsw.getServletClassLastModifiedTime() <= 0) {
            jsw.setServletClassLastModifiedTime(
                javaCompiler.getClassLastModified());
        }
        // END CR 6373479

        if (options.getSaveBytecode()) {
            javaCompiler.saveClassFile(ctxt.getFullClassName(),
                                       ctxt.getClassFileName());
        }

        // On some systems, due to file caching, the time stamp for the updated
        // JSP file may actually be greater than that of the newly created byte
        // codes in the cache.  In such cases, adjust the cache time stamp to
        // JSP page time, to avoid unnecessary recompilations.
        ctxt.getRuntimeContext().adjustBytecodeTime(ctxt.getFullClassName(),
                                                    jspModTime);
    }

    /**
     * Compile the jsp file from the current engine context.  As an side-
     * effect, tag files that are referenced by this page are also compiled.
     *
     * @param compileClass If true, generate both .java and .class file
     *                     If false, generate only .java file
     */
    public void compile(boolean compileClass)
        throws FileNotFoundException, JasperException, Exception
    {
        try {
            // Create the output directory for the generated files
            // Always try and create the directory tree, in case the generated
            // directories were deleted after the server was started.
            ctxt.getOutputDir();
            ctxt.makeOutputDir();

            // If errDispatcher is nulled from a previous compilation of the
            // same page, instantiate one here.
            if (errDispatcher == null) {
                errDispatcher = new ErrorDispatcher(jspcMode);
            }
            generateJava();
            if (compileClass) {
                generateClass();
            }
            else {
                // If called from jspc to only compile to .java files,
                // make sure that .java files are written to disk.
                javaCompiler.doJavaFile(ctxt.keepGenerated());
            }
        } finally {
            if (tfp != null) {
                tfp.removeProtoTypeFiles(null);
            }
            // Make sure these object which are only used during the
            // generation and compilation of the JSP page get
            // dereferenced so that they can be GC'd and reduce the
            // memory footprint.
            tfp = null;
            errDispatcher = null;
            if (!jspcMode) {
                pageInfo = null;
            }
            pageNodes = null;
            if (ctxt.getWriter() != null) {
                ctxt.getWriter().close();
                ctxt.setWriter(null);
            }
        }
    }

    /**
     * This is a protected method intended to be overridden by 
     * subclasses of Compiler. This is used by the compile method
     * to do all the compilation. 
     */
    public boolean isOutDated() {
        return isOutDated( true );
    }

    /**
     * Determine if a compilation is necessary by checking the time stamp
     * of the JSP page with that of the corresponding .class or .java file.
     * If the page has dependencies, the check is also extended to its
     * dependeants, and so on.
     * This method can by overidden by a subclasses of Compiler.
     * @param checkClass If true, check against .class file,
     *                   if false, check against .java file.
     */
    public boolean isOutDated(boolean checkClass) {

        String jsp = ctxt.getJspFile();
	
        if (jsw != null
                && (ctxt.getOptions().getModificationTestInterval() > 0)) {
 
            if (jsw.getLastModificationTest()
                    + (ctxt.getOptions().getModificationTestInterval() * 1000) 
                    > System.currentTimeMillis()) {
                return false;
            } else {
                jsw.setLastModificationTest(System.currentTimeMillis());
            }
        }

        long jspRealLastModified = 0;
        // START PWC 6468930
        File targetFile;
        
        if (checkClass) {
            targetFile = new File(ctxt.getClassFileName());
        } else {
            targetFile = new File(ctxt.getServletJavaFileName());
        }
        
        // Get the target file's last modified time. File.lastModified()
        // returns 0 if the file does not exist.
        long targetLastModified = targetFile.lastModified();

        // Check cached class file
        if (checkClass) {
            JspRuntimeContext rtctxt = ctxt.getRuntimeContext();
            String className = ctxt.getFullClassName();
            long cachedTime = rtctxt.getBytecodeBirthTime(className);
            if (cachedTime > targetLastModified) {
                targetLastModified = cachedTime;
            } else {
                // Remove from cache, since the bytecodes from the file is more
                // current, so that JasperLoader won't load the cached version
                rtctxt.setBytecode(className, null);
            }
        }

        if (targetLastModified == 0L)
            return true;

        // Check if the jsp exists in the filesystem (instead of a jar
        // or a remote location). If yes, then do a File.lastModified()
        // to determine its last modified time. This is more performant 
        // (fewer stat calls) than the ctxt.getResource() followed by 
        // openConnection(). However, it only works for file system jsps.
        // If the file has indeed changed, then need to call URL.OpenConnection() 
        // so that the cache loads the latest jsp file
        if (jsw != null) {
            File jspFile = jsw.getJspFile();
            if (jspFile != null) {
                jspRealLastModified = jspFile.lastModified();
            }
        }
        if (jspRealLastModified == 0 ||
            targetLastModified < jspRealLastModified) {
        // END PWC 6468930
        try {
            URL jspUrl = ctxt.getResource(jsp);
            if (jspUrl == null) {
                ctxt.incrementRemoved();
                return false;
            }
            URLConnection uc = jspUrl.openConnection();
            jspRealLastModified = uc.getLastModified();
            uc.getInputStream().close();
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
        // START PWC 6468930
        }
        // END PWC 6468930
        /* PWC 6468930
        long targetLastModified = 0;
        File targetFile;
        
        if( checkClass ) {
            targetFile = new File(ctxt.getClassFileName());
        } else {
            targetFile = new File(ctxt.getServletJavaFileName());
        }
        
        if (!targetFile.exists()) {
            return true;
        }

        targetLastModified = targetFile.lastModified();
        */
        if (checkClass && jsw != null) {
            jsw.setServletClassLastModifiedTime(targetLastModified);
        }

        if (targetLastModified < jspRealLastModified) {
            // Remember JSP mod time
            jspModTime = jspRealLastModified;
            if( log.isDebugEnabled() ) {
                log.debug("Compiler: outdated: " + targetFile + " " +
                    targetLastModified );
            }
            return true;
        }

        // determine if source dependent files (e.g. includes using include
        // directives) have been changed.
        if( jsw==null ) {
            return false;
        }

        List depends = jsw.getDependants();
        if (depends == null) {
            return false;
        }

        Iterator it = depends.iterator();
        while (it.hasNext()) {
            String include = (String)it.next();
            try {
                URL includeUrl = ctxt.getResource(include);
                if (includeUrl == null) {
                    return true;
                }

                URLConnection includeUconn = includeUrl.openConnection();
                long includeLastModified = includeUconn.getLastModified();
                includeUconn.getInputStream().close();

                if (includeLastModified > targetLastModified) {
                    // START GlassFish 750
                    if (include.endsWith(".tld")) {
                        ctxt.clearTaglibs();
                        ctxt.clearTagFileJarUrls();
                    }
                    // END GlassFish 750
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return true;
            }
        }

        return false;

    }

    
    /**
     * Gets the error dispatcher.
     */
    public ErrorDispatcher getErrorDispatcher() {
	return errDispatcher;
    }


    /**
     * Gets the info about the page under compilation
     */
    public PageInfo getPageInfo() {
	return pageInfo;
    }


    /**
     * Sets the info about the page under compilation
     */
    public void setPageInfo(PageInfo pageInfo) {
	this.pageInfo = pageInfo;
    }


    public JspCompilationContext getCompilationContext() {
	return ctxt;
    }


    /**
     * Remove generated files
     */
    public void removeGeneratedFiles() {
        try {
            String classFileName = ctxt.getClassFileName();
            if (classFileName != null) {
                File classFile = new File(classFileName);
                if( log.isDebugEnabled() )
                    log.debug( "Deleting " + classFile );
                classFile.delete();
            }
        } catch (Exception e) {
            // Remove as much as possible, ignore possible exceptions
        }
        try {
            String javaFileName = ctxt.getServletJavaFileName();
            if (javaFileName != null) {
                File javaFile = new File(javaFileName);
                if( log.isDebugEnabled() )
                    log.debug( "Deleting " + javaFile );
                javaFile.delete();
            }
        } catch (Exception e) {
            // Remove as much as possible, ignore possible exceptions
        }
    }

    public void removeGeneratedClassFiles() {
        try {
            String classFileName = ctxt.getClassFileName();
            if (classFileName != null) {
                File classFile = new File(classFileName);
                if( log.isDebugEnabled() )
                    log.debug( "Deleting " + classFile );
                classFile.delete();
            }
        } catch (Exception e) {
            // Remove as much as possible, ignore possible exceptions
        }
    }

    /**
     * Get an instance of JavaCompiler.
     * If Running with Mustang (JDK1.6), use a Jsr199JavaCompiler that
     * supports JSR199,
     * else if eclipse's JDT compiler is avalable, use that.
     * The default is to use javac from ant.
     * NOTE: When the appserver can be built with and runs only with JDK1.6,
     * this should be changed to instantiate Jsr199JavaCompiler with new
     * operator directly.
     */
    private void initJavaCompiler() {
        Class c = getClassFor("javax.tools.ToolProvider");
        if (c != null) {
            // JDK1.6
            c = getClassFor("org.apache.jasper.compiler.Jsr199JavaCompiler");
            if (c != null) {
                try {
                    javaCompiler = (JavaCompiler) c.newInstance();
                } catch (Exception ex) {
                }
            }
        }
        if (javaCompiler == null) {
            c = getClassFor("org.eclipse.jdt.internal.compiler.Compiler");
            if (c != null) {
                c = getClassFor("org.apache.jasper.compiler.JDTJavaCompiler");
                if (c != null) {
                    try {
                        javaCompiler = (JavaCompiler) c.newInstance();
                    } catch (Exception ex) {
                    }
                }
            }
        }
        if (javaCompiler == null) {
            javaCompiler = new AntJavaCompiler();
        }

        javaCompiler.init(ctxt, errDispatcher, jspcMode);
    }

    private Class getClassFor(String className) {
        Class c = null;
        try {
            c = Class.forName(className, false, getClass().getClassLoader());
        } catch (ClassNotFoundException ex) {
        }
        return c;
    }
}
