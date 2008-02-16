

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


package org.apache.catalina.startup;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.servlet.ServletException;

import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.util.SchemaResolver;
import org.apache.catalina.util.StringManager;
import com.sun.org.apache.commons.digester.Digester;
import org.xml.sax.InputSource;

/**
 * Startup event listener for a <b>Context</b> that configures the properties
 * of that Context, and the associated defined servlets.
 *
 * @author Craig R. McClanahan
 * @author Jean-Francois Arcand
 * @author Costin Manolache
 */
public final class TldConfig  {

    // Names of JARs that are known not to contain any TLDs with listeners
    private static HashSet<String> noTldListeners;

    private static com.sun.org.apache.commons.logging.Log log=
        com.sun.org.apache.commons.logging.LogFactory.getLog( TldConfig.class );

    private static final String FILE_URL_PREFIX = "file:";
    private static final int FILE_URL_PREFIX_LEN = FILE_URL_PREFIX.length();

    // START CR 6402120   
    /**
     * The variable that indicates whether or not to create/use a serialized
     * cache of TLD listeners.
     */
    private static boolean cacheListeners = true;
    // END CR 6402120   

    // Names of system TLD URIs
    private static HashSet<String> systemTldUris = new HashSet<String>();
    private static HashSet<String> systemTldUrisJsf = new HashSet<String>();

    static {
        systemTldUrisJsf.add("http://java.sun.com/jsf/core");
        systemTldUrisJsf.add("http://java.sun.com/jsf/html");
        systemTldUris.add("http://java.sun.com/jsp/jstl/core");
    }

    // ----------------------------------------------------- Instance Variables

    /**
     * The Context we are associated with.
     */
    private Context context = null;


    /**
     * The string resources for this package.
     */
    private static final StringManager sm =
        StringManager.getManager(Constants.Package);

    /**
     * The <code>Digester</code> we will use to process tag library
     * descriptor files.
     */
    private Digester tldDigester = null;

    /**
     * True if the TLD currently being scanned is locally bundled, false 
     * otherwise
     */
    private boolean isCurrentTldLocal = false;

    /**
     * The URI of the TLD currently being scanned
     */
    private String currentTldUri;

    /**
     * Attribute value used to turn on/off TLD validation
     */
     private boolean tldValidation = false;
     
    /**
     * Attribute value used to turn on/off TLD  namespace awarenes.
     */
    private boolean tldNamespaceAware = false;

    
    private boolean rescan=true;
    
    // START SJSAS 8.1 5049111   
    /**
     * Scan the parent when searching for TLD listeners.
     */
    private static boolean scanParent = false;
    // END SJSAS 8.1 5049111    

    private ArrayList listeners=new ArrayList();

    // START GlassFish 747
    private HashMap<String, String[]> tldUriToLocationMap =
            new HashMap<String, String[]>();
    private String currentTldResourcePath;
    private String currentTldJarFile;
    private String currentTldJarEntryName;
    // END GlassFish 747

    private boolean useMyFaces;


    // --------------------------------------------------------- Public Methods


    public void setUseMyFaces(boolean useMyFaces) {
        this.useMyFaces = useMyFaces;
    }


    /**
     * Sets the list of JAR files that are known not to contain any
     * TLDs that declare servlet listeners.
     *
     * Only shared JAR files (that is, those loaded by a delegation parent
     * of the webapp's classloader) will be checked against this list.
     *
     * @param jarNames List of comma-separated names of JAR files that are 
     * known not to contain any TLDs that declare servlet listeners
     */
    public static void setNoTldListeners(String jarNames) {
        if (jarNames != null) {
            if (noTldListeners == null) {
                noTldListeners = new HashSet<String>();
            } else {
                noTldListeners.clear();
            }
            StringTokenizer tokenizer = new StringTokenizer(jarNames, ",");
            while (tokenizer.hasMoreElements()) {
                noTldListeners.add(tokenizer.nextToken());
            }
        }
    }

    /**
     * Sets the list of JAR files that are known not to contain any
     * TLDs that declare servlet listeners.
     *
     * Only shared JAR files (that is, those loaded by a delegation parent
     * of the webapp's classloader) will be checked against this list.
     *
     * @param set HashSet containing the names of JAR file known not to
     * contain any TLDs that declare servlet listeners
     */
    public static void setNoTldListeners(HashSet set) {
        noTldListeners = set;
    }
    
    // START SJSAS 8.1 5049111   
    /**
     * Scan the parent when searching for TLD listeners.
     */
    public static void setScanParentTldListener(boolean scan){
        scanParent = scan;
    }
    
    public static boolean getScanParentTldListener(){
        return scanParent;
    }
    // END SJSAS 8.1 5049111     
    
    // START CR 6402120
    /**
     * Sets the flag that indicates whether to create/use a serialized cache of
     * listeners
     *
     * @param cache true to create/use a listener cache, false otherwise
     */
    public static void setCacheListeners(boolean cache) {
        cacheListeners = cache;
    } 
 
    /**
     * Indicates if a serialized cache of listeners is to be created and used
     *
     * @return true if the listener cache file is to be used, false otherwise.
     */
    public static boolean isCacheListeners() {
        return cacheListeners;
    }

    /**
     * @deprecated Provided for backwards compatibility only. Use
     * setCacheListeners instead.
     */
    public static void setSingleProcess(boolean isSingleProcess) {
        cacheListeners = isSingleProcess;
    }
    // END CR 6402120    
 
    /**
     * Set the validation feature of the XML parser used when
     * parsing xml instances.
     * @param xmlValidation true to enable xml instance validation
     */
    public void setTldValidation(boolean tldValidation){
        this.tldValidation = tldValidation;
    }

    /**
     * Get the server.xml <host> attribute's xmlValidation.
     * @return true if validation is enabled.
     *
     */
    public boolean getTldValidation(){
        return tldValidation;
    }

    /**
     * Get the server.xml <host> attribute's xmlNamespaceAware.
     * @return true if namespace awarenes is enabled.
     *
     */
    public boolean getTldNamespaceAware(){
        return tldNamespaceAware;
    }


    /**
     * Set the namespace aware feature of the XML parser used when
     * parsing xml instances.
     * @param xmlNamespaceAware true to enable namespace awareness
     */
    public void setTldNamespaceAware(boolean tldNamespaceAware){
        this.tldNamespaceAware = tldNamespaceAware;
    }    


     public boolean isRescan() {
        return rescan;
    }

    public void setRescan(boolean rescan) {
        this.rescan = rescan;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
        this.useMyFaces = ((StandardContext) context).isUseMyFaces();
    }

    public void addApplicationListener( String s ) {
        if (log.isDebugEnabled()) {
            log.debug( "Add tld listener " + s);
        }
        if ((isCurrentTldLocal
                    && !systemTldUris.contains(currentTldUri)
                    && (!systemTldUrisJsf.contains(currentTldUri)
                        || useMyFaces))
                || (!isCurrentTldLocal
                    && (!systemTldUrisJsf.contains(currentTldUri)
                        || !useMyFaces))) {
            listeners.add(s);
        }
    }

    public void setTldUri(String uri) {
        this.currentTldUri = uri;
        // START GlassFish 747
        /*
         * Add the mapping for the given URI only if
         * - the corresponding TLD is local, and
         * - the URI is not one of the standard (i.e., JSTL or JSF) ones, and
         * - the URI is not already mapped (this check is necessary because 
         *   taglibs specified in web.xml are supposed to take precedence)
         */
        if (isCurrentTldLocal
                && !systemTldUris.contains(currentTldUri)
                && (!systemTldUrisJsf.contains(currentTldUri)
                    || useMyFaces)
                && tldUriToLocationMap.get(currentTldUri) == null) {
            String[] currentTldLocation = new String[2];
            if (currentTldResourcePath != null) {
                currentTldLocation[0] = currentTldResourcePath;
            } else if (currentTldJarFile != null
                    && currentTldJarEntryName != null) {
                currentTldLocation[0] = "file:" + currentTldJarFile;
                currentTldLocation[1] = currentTldJarEntryName;
            }
            tldUriToLocationMap.put(currentTldUri, currentTldLocation);
        }
        // END GlassFish 747
    }

    public String[] getTldListeners() {
        String result[]=new String[listeners.size()];
        listeners.toArray(result);
        return result;
    }


    /**
     * Scan for and configure all tag library descriptors found in this
     * web application.
     *
     * @exception Exception if a fatal input/output or parsing error occurs
     */
    public void execute() throws Exception {

        
        long t1=System.currentTimeMillis();

        File tldCache=null;

        /* CR 6402120   
        if (context instanceof StandardContext) {
        */
        // START CR 6402120
        if (log.isDebugEnabled()) { 
            log.debug("Create/use TLD listener cache? "
                      + (isCacheListeners()));
        }

        // If multiple JVMs are running, then do not create tldCache.ser 
        // file as it will cause exceptions on the server side 
        // because of unsynchronized access of tldCache.ser file.
        if ((context instanceof StandardContext) && isCacheListeners()) {
        // END CR 6402120   
            File workDir= (File)
                ((StandardContext)context).getServletContext().getAttribute(Globals.WORK_DIR_ATTR);
            tldCache=new File( workDir, "tldCache.ser");
        }

        // Option to not rescan
        if( ! rescan ) {
            // find the cache
            if( tldCache!= null && tldCache.exists()) {
                try {
                    processCache(tldCache);
                    return;
                } catch (Throwable t) {
                    log.warn("Error scanning " + tldCache, t);
                }
            }
        }

        /*
         * Acquire the list of TLD resource paths, possibly embedded in JAR
         * files, to be processed
         */
        Set resourcePaths = tldScanResourcePaths();              
        Map jarPaths = getJarPaths();
        
        // Check to see if we can use cached listeners
        if (tldCache != null && tldCache.exists()) {
            long lastModified = getLastModified(resourcePaths, jarPaths);
            if (lastModified < tldCache.lastModified()) {
                try {
                    processCache(tldCache);
                    return;
                } catch (Throwable t) {
                    log.warn("Error scanning " + tldCache, t);
                }
            }
        }

        // Scan each accumulated resource path for TLDs to be processed
        Iterator<String> paths = resourcePaths.iterator();
        while (paths.hasNext()) {
            tldScanTld(paths.next());
        }
        if (jarPaths != null) {
            Iterator<JarPathElement> elems = jarPaths.values().iterator();
            while (elems.hasNext()) {
                JarPathElement elem = elems.next();
                tldScanJar(elem.getJarFile(), elem.getIsLocal());
            }
        }

        String list[] = getTldListeners();

        if( tldCache!= null ) {
            log.debug( "Saving tld cache: " + tldCache + " " + list.length);
            try {
                FileOutputStream out=new FileOutputStream(tldCache);
                ObjectOutputStream oos=new ObjectOutputStream( out );
                oos.writeObject( list );
                oos.close();
            } catch( IOException ex ) {
                ex.printStackTrace();
            }
        }

        if( log.isDebugEnabled() )
            log.debug( "Adding tld listeners:" + list.length);
        for( int i=0; list!=null && i<list.length; i++ ) {
            context.addApplicationListener(list[i]);
        }

        long t2=System.currentTimeMillis();
        if( context instanceof StandardContext ) {
            ((StandardContext)context).setTldScanTime(t2-t1);
        }

        // START GlassFish 747
        context.getServletContext().setAttribute(
            Globals.JSP_TLD_URI_TO_LOCATION_MAP,
            tldUriToLocationMap);
        // END GlassFish 747
    }

    // -------------------------------------------------------- Private Methods

    /*
     * Returns the last modification date of the given sets of resources.
     *
     * @param resourcePaths
     * @param jarPaths
     *
     * @return Last modification date
     */
    private long getLastModified(Set resourcePaths, Map jarPaths)
            throws Exception {

        long lastModified = 0;

        Iterator paths = resourcePaths.iterator();
        while (paths.hasNext()) {
            String path = (String) paths.next();
            URL url = context.getServletContext().getResource(path);
            if (url == null) {
                log.debug( "Null url "+ path );
                break;
            }
            long lastM = url.openConnection().getLastModified();
            if (lastM > lastModified) lastModified = lastM;
            if (log.isDebugEnabled()) {
                log.debug( "Last modified " + path + " " + lastM);
            }
        }

        if (jarPaths != null) {
            Iterator<JarPathElement> elems = jarPaths.values().iterator();
            while (elems.hasNext()) {
                JarPathElement elem = elems.next();
                File jarFile = elem.getJarFile();
                long lastM = jarFile.lastModified();
                if (lastM > lastModified) lastModified = lastM;
                if (log.isDebugEnabled()) {
                    log.debug("Last modified " + jarFile.getAbsolutePath()
                              + " " + lastM);
                }
            }
        }

        return lastModified;
    }

    /**
     * Reads the cache of listeners specified in TLD files.
     */
    private void processCache(File tldCache ) throws Exception {
        FileInputStream in=new FileInputStream(tldCache);
        ObjectInputStream ois=new ObjectInputStream( in );
        String list[]=(String [])ois.readObject();
        if( log.isDebugEnabled() ) {
            log.debug("Reusing tldCache " + tldCache + " " + list.length);
        }
        for( int i=0; list!=null && i<list.length; i++ ) {
            // Load the listener class. Failure to do so is an indication
            // that the cache has become stale, in which case it must be
            // ignored. See GlassFish Issue 2653.
            context.getLoader().getClassLoader().loadClass(list[i]);
            context.addApplicationListener(list[i]);
        }
        ois.close();
    }

    /**
     * Create (if necessary) and return a Digester configured to process a tag
     * library descriptor, looking for additional listener classes to be
     * registered.
     */
    private Digester createTldDigester() {

        /* SJSAS 6384538
        return DigesterFactory.newDigester(tldValidation, 
                                           tldNamespaceAware, 
                                           new TldRuleSet());
        */
        // START SJSAS 6384538
        return DigesterFactory.newDigester(false, 
                                           tldNamespaceAware, 
                                           new TldRuleSet());
        // END SJSAS 6384538
    }

    /**
     * Scans all TLD entries in the given JAR for application listeners.
     *
     * @param file JAR file whose TLD entries are scanned for application
     * listeners
     */
    private void tldScanJar(File file) throws Exception {
        tldScanJar(file, false);
    }

    private void tldScanJar(File file, boolean isLocal) throws Exception {

        JarFile jarFile = null;
        String name = null;

        String jarPath = file.getAbsolutePath();

        try {
            jarFile = new JarFile(file);
            Enumeration entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = (JarEntry) entries.nextElement();
                name = entry.getName();
                if (!name.startsWith("META-INF/")) {
                    continue;
                }
                if (!name.endsWith(".tld")) {
                    continue;
                }
                if (log.isTraceEnabled()) {
                    log.trace("  Processing TLD at '" + name + "'");
                }
                // START GlassFish 747
                currentTldJarFile = jarPath;
                currentTldJarEntryName = name;
                // END GlassFish 747
                try {
                    tldScanStream(new InputSource(jarFile.getInputStream(entry)),
                                  isLocal);
                } catch (Exception e) {
                    log.error(sm.getString("contextConfig.tldEntryException",
                                           name, jarPath, context.getPath()),
                              e);
                }
            }
        } catch (Exception e) {
            log.error(sm.getString("contextConfig.tldJarException",
                                   jarPath, context.getPath()),
                      e);
        } finally {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (Throwable t) {
                    // Ignore
                }
            }
        }
    }

    /**
     * Scan the TLD contents in the specified input stream, and register
     * any application event listeners found there.  <b>NOTE</b> - It is
     * the responsibility of the caller to close the InputStream after this
     * method returns.
     *
     * @param resourceStream InputStream containing a tag library descriptor
     *
     * @exception Exception if an exception occurs while scanning this TLD
     */
    private void tldScanStream(InputSource resourceStream)
            throws Exception {
        tldScanStream(resourceStream, false);
    }

    private void tldScanStream(InputSource resourceStream, boolean isLocal)
            throws Exception {

        if (tldDigester == null){
            tldDigester = createTldDigester();
        }
        
        synchronized (tldDigester) {
            try {
                tldDigester.push(this);
                isCurrentTldLocal = isLocal;
                tldDigester.parse(resourceStream);
            } finally {
                isCurrentTldLocal = false;
                currentTldUri = null;
                // START GlassFish 747
                currentTldJarFile = null;
                currentTldJarEntryName = null;
                currentTldResourcePath = null;
                // END GlassFish 747
                tldDigester.push(null);
                tldDigester.clear();
            }
        }

    }

    /**
     * Scan the TLD contents at the specified resource path, and register
     * any application event listeners found there.
     *
     * @param resourcePath Resource path being scanned
     *
     * @exception Exception if an exception occurs while scanning this TLD
     */
    private void tldScanTld(String resourcePath) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug(" Scanning TLD at resource path '" + resourcePath + "'");
        }

        InputStream tldStream =
            context.getServletContext().getResourceAsStream(resourcePath);
        if (tldStream == null) {
            throw new ServletException
                (sm.getString("contextConfig.tldResourcePath",
                              resourcePath));
        }

        InputSource inputSource = new InputSource(tldStream);

        // START GlassFish 747
        currentTldResourcePath = resourcePath;
        // END GlassFish 747

        try {
            tldScanStream(inputSource, true);
        } catch (Exception e) {
             throw new ServletException
                 (sm.getString("contextConfig.tldFileException", resourcePath,
                               context.getPath()),
                  e);
        } 

    }

    /**
     * Accumulate and return a Set of resource paths to be analyzed for
     * tag library descriptors.  Each element of the returned set will be
     * the context-relative path to either a tag library descriptor file,
     * or to a JAR file that may contain tag library descriptors in its
     * <code>META-INF</code> subdirectory.
     *
     * @exception IOException if an input/output error occurs while
     *  accumulating the list of resource paths
     */
    private Set tldScanResourcePaths() throws IOException {
        if (log.isTraceEnabled()) {
            log.trace(" Accumulating TLD resource paths");
        }
        Set resourcePaths = new HashSet();

        // Accumulate resource paths explicitly listed in the web application
        // deployment descriptor
        if (log.isTraceEnabled()) {
            log.trace("  Scanning <taglib> elements in web.xml");
        }
        String taglibs[] = context.findTaglibs();
        for (int i = 0; i < taglibs.length; i++) {
            String resourcePath = context.findTaglib(taglibs[i]);
            // FIXME - Servlet 2.4 DTD implies that the location MUST be
            // a context-relative path starting with '/'?
            if (!resourcePath.startsWith("/")) {
                resourcePath = "/WEB-INF/" + resourcePath;
            }
            if (log.isTraceEnabled()) {
                log.trace("   Adding path '" + resourcePath +
                    "' for URI '" + taglibs[i] + "'");
            }
            // START GlassFish 747
            tldUriToLocationMap.put(taglibs[i],
                                    new String[] { resourcePath, null});
            // END GlassFish 747
            resourcePaths.add(resourcePath);
        }

        DirContext resources = context.getResources();
        if (resources != null) {
            tldScanResourcePathsWebInf(resources, "/WEB-INF", resourcePaths);
        }

        // Return the completed set
        return (resourcePaths);

    }

    /*
     * Scans the web application's subdirectory identified by rootPath,
     * along with its subdirectories, for TLDs.
     *
     * Initially, rootPath equals /WEB-INF. /WEB-INF/tags and any of its
     * subdirectories are excluded from the search, as per the JSP spec.
     *
     * @param resources The web application's resources
     * @param rootPath The path whose subdirectories are to be searched for
     * TLDs
     * @param tldPaths The set of TLD resource paths to add to
     */
    private void tldScanResourcePathsWebInf(DirContext resources,
                                            String rootPath,
                                            Set tldPaths) 
            throws IOException {

        if (log.isTraceEnabled()) {
            log.trace("  Scanning TLDs in " + rootPath + " subdirectory");
        }

        try {
            NamingEnumeration items = resources.list(rootPath);
            while (items.hasMoreElements()) {
                NameClassPair item = (NameClassPair) items.nextElement();
                String resourcePath = rootPath + "/" + item.getName();
                if (resourcePath.startsWith("/WEB-INF/tags")) {
                    continue;
                }
                if (resourcePath.endsWith(".tld")) {
                    if (log.isTraceEnabled()) {
                        log.trace("   Adding path '" + resourcePath + "'");
                    }
                    tldPaths.add(resourcePath);
                } else {
                    tldScanResourcePathsWebInf(resources, resourcePath,
                                               tldPaths);
                }
            }
        } catch (NamingException e) {
            ; // Silent catch: it's valid that no /WEB-INF directory exists
        }
    }

    /**
     * Returns a map of the paths to all JAR files that are accessible to the
     * webapp and will be scanned for TLDs and their listeners.
     *
     * The map always includes all the JARs under WEB-INF/lib, as well as
     * shared JARs in the classloader delegation chain of the webapp's
     * classloader.
     *
     * The latter constitutes a Tomcat-specific extension to the TLD search
     * order defined in the JSP spec. It allows tag libraries packaged as JAR
     * files to be shared by web applications by simply dropping them in a 
     * location that all web applications have access to (e.g.,
     * <CATALINA_HOME>/common/lib).
     *
     * The set of shared JARs to be scanned for TLDs is narrowed down by
     * the <tt>noTldListeners</tt> class variable, which contains the names
     * of JARs that are known not to contain any TLDs that declare servlet
     * listeners.
     *
     * @return Map of JAR file paths
     */
    private Map getJarPaths() {

        HashMap jarPathMap = null;

        ClassLoader webappLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader loader = webappLoader;
        while (loader != null) {
            if (loader instanceof URLClassLoader) {
                URL[] urls = ((URLClassLoader) loader).getURLs();
                for (int i=0; i<urls.length; i++) {
                    // Expect file URLs
                    // This is definitely not as clean as using JAR URLs either
                    // over file or the custom jndi handler, but a lot less
                    // buggy overall
                    File file = new File(
                            RequestUtil.URLDecode(urls[i].getFile()));
                    try {
                        file = file.getCanonicalFile();
                    } catch (IOException e) {
                        // Ignore
                    }
                    if (!file.exists()) {
                        continue;
                    }
                    String path = file.getAbsolutePath();
                    if (!path.endsWith(".jar")) {
                        continue;
                    }
                    /*
                     * Scan all JARs from WEB-INF/lib, plus any shared JARs
                     * that are not known not to contain any TLDs with
                     * listeners
                     */
                    if (loader == webappLoader
                            || noTldListeners == null
                            || !noTldListeners.contains(file.getName())) {
                        JarPathElement elem = new JarPathElement(
                                file, loader == webappLoader);
                        if (jarPathMap == null) {
                            jarPathMap = new HashMap();
                            jarPathMap.put(path, elem);
                        } else if (!jarPathMap.containsKey(path)) {
                            jarPathMap.put(path, elem);
                        }
                    }
                }
            }
            
            // START SJSAS 8.1 5049111
            if ( scanParent || context.isJsfApplication() ) {
            // END SJSAS 8.1 5049111    
                loader = loader.getParent();
            // START SJSAS 8.1 5049111
            } else {
                loader = null;
            }
            // END SJSAS 8.1 5049111                
        }

        return jarPathMap;
    }
}

class JarPathElement {

    private File jarFile;
    private boolean isLocal;

    public JarPathElement(File jarFile, boolean isLocal) {
        this.jarFile = jarFile;
        this.isLocal = isLocal;        
    }

    public File getJarFile() {
        return jarFile;
    }

    public boolean getIsLocal() {
        return isLocal;
    }
}
