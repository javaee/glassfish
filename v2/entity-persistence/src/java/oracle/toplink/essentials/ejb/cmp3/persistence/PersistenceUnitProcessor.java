/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.ejb.cmp3.persistence;

import java.net.URL;
import java.net.URISyntaxException;
import java.net.JarURLConnection;
import java.util.Enumeration;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.persistence.spi.PersistenceUnitInfo;

import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;

import oracle.toplink.essentials.exceptions.PersistenceUnitLoadingException;
import oracle.toplink.essentials.exceptions.XMLParseException;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataProcessor;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.XMLConstants;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.parser.PersistenceContentHandler;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.parser.XMLException;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.parser.XMLExceptionHandler;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.logging.AbstractSessionLog;
import oracle.toplink.essentials.logging.SessionLog;
import oracle.toplink.essentials.ejb.cmp3.EntityManagerFactoryProvider;

/**
 * INTERNAL:
 * Utility Class that deals with persistence archives for EJB 3.0
 * Provides functions like searching for persistence archives, processing persistence.xml
 * and searching for Entities in a Persistence archive
 */
public class PersistenceUnitProcessor  {

    /**
     * Get a list of persitence units from the file or directory at the given url
     * PersistenceUnits are built based on the presence of persistence.xml in a META-INF directory
     * at the base of the URL
     * @param archive The url of a jar file or directory to check
     * @return
     */
    public static List<SEPersistenceUnitInfo> getPersistenceUnits(Archive archive, ClassLoader loader){
        return processPersistenceArchive(archive, loader);
    }

    /**
     * Go through the jar file for this PeristeneUnitProcessor and process any XML provided in it
     */
    public static List<SEPersistenceUnitInfo> processPersistenceArchive(Archive archive, ClassLoader loader){
        URL puRootURL = archive.getRootURL();
        try {
            InputStream pxmlStream = archive.getEntry("META-INF/persistence.xml"); // NOI18N
            return processPersistenceXML(puRootURL, pxmlStream, loader);
        } catch (IOException e) {
            throw PersistenceUnitLoadingException.exceptionLoadingFromUrl(puRootURL.toString(), e);
        }
    }

    /**
   * Build a persistence.xml file into a SEPersistenceUnitInfo object 
   * @param input 
   */
    private static List<SEPersistenceUnitInfo> processPersistenceXML(URL baseURL, InputStream input, ClassLoader loader){
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        spf.setValidating(true);
        
        XMLReader xmlReader = null;
        SAXParser sp = null;
        XMLExceptionHandler xmlErrorHandler = new XMLExceptionHandler();

        // create a SAX parser
        try {
            sp = spf.newSAXParser();
	        sp.setProperty(XMLConstants.SCHEMA_LANGUAGE, XMLConstants.XML_SCHEMA);
	    } catch (javax.xml.parsers.ParserConfigurationException exc){
	    	throw XMLParseException.exceptionCreatingSAXParser(baseURL, exc);
	    } catch (org.xml.sax.SAXException exc){
	    	throw XMLParseException.exceptionCreatingSAXParser(baseURL, exc);
	    }
	        
	    // create an XMLReader
	    try {
            xmlReader = sp.getXMLReader();
	        xmlReader.setErrorHandler(xmlErrorHandler);
        } catch (org.xml.sax.SAXException exc){
        	throw XMLParseException.exceptionCreatingXMLReader(baseURL, exc);
        }
       
        // attempt to load the schema from the classpath
        URL schemaURL = loader.getResource(XMLConstants.PERSISTENCE_SCHEMA_NAME);
        if (schemaURL != null) {
            try {
            	sp.setProperty(XMLConstants.JAXP_SCHEMA_SOURCE, schemaURL.toString());
            } catch (org.xml.sax.SAXException exc){
            	throw XMLParseException.exceptionSettingSchemaSource(baseURL, schemaURL, exc);
            }
        }

        PersistenceContentHandler myContentHandler = new PersistenceContentHandler();
        xmlReader.setContentHandler(myContentHandler);

        InputSource inputSource = new InputSource(input);
        try{
            xmlReader.parse(inputSource);
        } catch (IOException exc){
            throw PersistenceUnitLoadingException.exceptionProcessingPersistenceXML(baseURL, exc);
        } catch (org.xml.sax.SAXException exc){
        	// XMLErrorHandler will handle SAX exceptions
        }
        
        // handle any parse exceptions
        XMLException xmlError = xmlErrorHandler.getXMLException();
        if (xmlError != null) {
            throw PersistenceUnitLoadingException.exceptionProcessingPersistenceXML(baseURL, xmlError);
        }

        Iterator<SEPersistenceUnitInfo> persistenceInfos = myContentHandler.getPersistenceUnits().iterator();
        while (persistenceInfos.hasNext()){
            SEPersistenceUnitInfo info = persistenceInfos.next();
            info.setPersistenceUnitRootUrl(baseURL);           
        }
        return myContentHandler.getPersistenceUnits();
    }

    /**
   * Entries in a zip file are directory entries using slashes to separate them.
   * Build a class name using '.' instead of slash and removing the '.class' extension.
   * @param classEntryString 
   * @return 
   */
    public static String buildClassNameFromEntryString(String classEntryString){
        String classNameForLoader = classEntryString;
        if (classEntryString.endsWith(".class")){ // NOI18N
            classNameForLoader = classNameForLoader.substring(0, classNameForLoader.length() - 6);;
            classNameForLoader = classNameForLoader.replace("/", ".");              
        }
        return classNameForLoader;
    }

   /**
   * Build a set that contains all the class names at a URL
   * @return a Set of class name strings
   */
    public static Set<String> buildClassSet(PersistenceUnitInfo persistenceUnitInfo, ClassLoader loader){
        Set<String> set = new HashSet<String>();
        set.addAll(persistenceUnitInfo.getManagedClassNames());
        Iterator i = persistenceUnitInfo.getJarFileUrls().iterator();
        while (i.hasNext()) {
            set.addAll(getClassNamesFromURL((URL)i.next()));
        }
        if (!persistenceUnitInfo.excludeUnlistedClasses()){
            set.addAll(getClassNamesFromURL(persistenceUnitInfo.getPersistenceUnitRootUrl()));
        }
        set.addAll(buildPersistentClassSetFromXMLDocuments(persistenceUnitInfo, loader));        
        return set;
    }

    /**
     * Return a Set<String> of the classnames represented in the mapping files specified in info
     * @param info
     * @param loader
     * @return
     */
    private static Set<String> buildPersistentClassSetFromXMLDocuments(PersistenceUnitInfo info, ClassLoader loader){
        Set<String> classes = null;
        
        // Build a MetadataProcessor to search the mapped classes in orm xml documents
        // We hand in a null session since none of the functionality required uses a session
        MetadataProcessor processor = new MetadataProcessor(info, null, loader, false);
        processor.readMappingFiles(false);
        classes = processor.buildEntityClassSetFromXMLDocuments();
        
        return classes;
    }
    
    /**
     *  Search the classpath for persistence archives.  A persistence archive is defined as any
     *  part of the class path that contains a META-INF directory with a persistence.xml file in it. 
     *  Return a list of the URLs of those files.
     *  Use the current thread's context classloader to get the classpath.  We assume it is a URL class loader
     */
    public static Set<Archive> findPersistenceArchives(){
        ClassLoader threadLoader = Thread.currentThread().getContextClassLoader();
        return findPersistenceArchives(threadLoader);
    }


    /**
     *  Search the classpath for persistence archives. A persistence archive is defined as any
     *  part of the class path that contains a META-INF directory with a persistence.xml file in it.. 
     *  Return a list of {@link Archive} representing the root of those files.
     *  @param loader the class loader to get the class path from
     */
    public static Set<Archive> findPersistenceArchives(ClassLoader loader){
        Set<Archive> pars = new HashSet<Archive>();
        try {
            Enumeration<URL> resources = loader.getResources("META-INF/persistence.xml");
            while (resources.hasMoreElements()){
                URL pxmlURL = resources.nextElement();
                URL puRootURL = computePURootURL(pxmlURL);
                Archive archive = new ArchiveFactoryImpl().createArchive(puRootURL);
                pars.add(archive);
            }
        } catch (java.io.IOException exc){
            throw PersistenceUnitLoadingException.exceptionSearchingForPersistenceResources(loader, exc);
        } catch (URISyntaxException exc) {
            throw PersistenceUnitLoadingException.exceptionSearchingForPersistenceResources(loader, exc);
        }
        return pars;
    }

    public static URL computePURootURL(URL pxmlURL) throws IOException {
        String protocol = pxmlURL.getProtocol();
        if("file".equals(protocol)) { // NOI18N
            // e.g. file:/tmp/META-INF/persistence.xml
            assert(new File(pxmlURL.getFile()).isFile());
            return new URL(pxmlURL, ".."); // NOI18N
        } else if("jar".equals(protocol)) { // NOI18N
            // e.g. jar:file:/tmp/a_ear/b.jar!/META-INF/persistence.xml
            JarURLConnection conn =
                    JarURLConnection.class.cast(pxmlURL.openConnection());
            assert(conn.getJarEntry().getName().equals(
                    "META-INF/persistence.xml")); // NOI18N
            return conn.getJarFileURL();
        } else {
            // some other protocol,
            // e.g. bundleresource://21/META-INF/persistence.xml
            return new URL(pxmlURL, "../"); // NOI18N
        }
    }

    public static Set<String> getClassNamesFromURL(URL url) {
        Set<String> classNames = new HashSet<String>();
        Archive archive = null;
        try {
            archive = new ArchiveFactoryImpl().createArchive(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException("url = [" + url + "]", e);  // NOI18N
        } catch (IOException e) {
            throw new RuntimeException("url = [" + url + "]", e);  // NOI18N
        }
        for (Iterator<String> entries = archive.getEntries(); entries.hasNext();) {
            String entry = entries.next();
            if (entry.endsWith(".class")){ // NOI18N
                 classNames.add(buildClassNameFromEntryString(entry));
            }
        }
        return classNames;
    }

    public static Set<String> getEntityClassNamesFromURL(URL url, ClassLoader loader) {
        Set<String> entityClassNames = new HashSet<String>();
        for (String className: getClassNamesFromURL(url)){
            if (isEntity(className, loader, false)){
                entityClassNames.add(className);
            }
        }
        return entityClassNames;
    }

    /**
   *  Return whether the class with the given name is annotated with @Entity.
   * @param className 
   * @return 
   */
    public static boolean isEntity(String className, ClassLoader loader, boolean throwExceptionIfNotFound){
        Class candidateClass = null;
        try{
            candidateClass = loader.loadClass(className);
        } catch (ClassNotFoundException exc){
            if (throwExceptionIfNotFound){
                throw PersistenceUnitLoadingException.exceptionLoadingClassWhileLookingForAnnotations(className, exc);
            } else {
                AbstractSessionLog.getLog().log(AbstractSessionLog.WARNING, "persistence_unit_processor_error_loading_class", exc.getClass().getName(), exc.getLocalizedMessage() , className);
                return false;                
            }
        } catch (Exception exception){
            AbstractSessionLog.getLog().log(AbstractSessionLog.WARNING, "persistence_unit_processor_error_loading_class", exception.getClass().getName(), exception.getLocalizedMessage() , className);
            return false;
        }
        return isEntity(candidateClass);
    }
    /**
    *  Return whether a given class is annotated with @Entity.
    * @param candidateClass
    * @return 
    */
    public static boolean isEntity(Class candidateClass){
        if (candidateClass.isAnnotationPresent(javax.persistence.Entity.class)) {
            return true;
        }
        return false;
    }

    /**
     * Process the Object/relational metadata from XML and annotations
     * @param processor
     * @param privateClassLoader
     * @param session
     * @param throwExceptionOnFail
     */
    public static void processORMetadata(
            MetadataProcessor processor,
            ClassLoader privateClassLoader, 
            AbstractSession session,
            boolean throwExceptionOnFail){
        // DO NOT CHANGE the order of invocation of various methods.

        // build the list of mapping files and read them. Need to do this before
        // we start processing entities as the list of entity classes
        // depend on metadata read from mapping files.
        processor.readMappingFiles(throwExceptionOnFail);

        // process persistence unit metadata/defaults defined in
        // ORM XML instance documents in the persistence unit
        processor.processPersistenceUnitMetadata();

        //bug:2647 - need to find/process entities after the persistenceUnitMetadata to unsure defaults are overriden.  
        processor.buildEntityList();

        processor.processMappingFiles();

        processor.processAnnotations();
        
    }
    
    /**
     * Create a list of the entities that will be deployed.  This list is build from the information
     * provided in the PersistenceUnitInfo argument.
     * The list contains Classes specified in the PersistenceUnitInfo's class list and also
     * files that are annotated with @Entity, @Embeddable and @MappedSuperclass in
     * the jar files provided in the persistence info.
     * This list of classes will used by TopLink to build a deployment project and to 
     * decide what classes to weave.
     * @param loader
     * @return
     */
      public static Collection<Class> buildEntityList(MetadataProcessor processor,ClassLoader loader) {
        ArrayList<Class> entityList = new ArrayList<Class>();
        for (String className : processor.getProject().getEntityNames()) {
            try {
                Class entityClass = loader.loadClass(className);
                entityList.add(entityClass);
            } catch (ClassNotFoundException exc) {
                AbstractSessionLog.getLog().log(SessionLog.CONFIG,
                        "exception_loading_entity_class", className, exc);
            }
        }
        return entityList;
    }
}
