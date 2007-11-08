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
package oracle.toplink.essentials.internal.ejb.cmp3.metadata;

import java.io.InputStream;
import java.io.IOException;

import java.net.URISyntaxException;
import java.net.URL;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.spi.PersistenceUnitInfo;

import oracle.toplink.essentials.ejb.cmp3.persistence.Archive;
import oracle.toplink.essentials.ejb.cmp3.persistence.ArchiveFactoryImpl;
import oracle.toplink.essentials.ejb.cmp3.persistence.PersistenceUnitProcessor;

import oracle.toplink.essentials.exceptions.PersistenceUnitLoadingException;
import oracle.toplink.essentials.exceptions.ValidationException;

import oracle.toplink.essentials.internal.ejb.cmp3.EntityManagerSetupImpl;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataLogger;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.ClassAccessor;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.objects.MetadataClass;

import oracle.toplink.essentials.internal.ejb.cmp3.xml.accessors.XMLClassAccessor;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.XMLConstants;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.XMLHelper;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.XMLValidator;

import oracle.toplink.essentials.internal.sessions.AbstractSession;

import oracle.toplink.essentials.logging.AbstractSessionLog;
import oracle.toplink.essentials.logging.SessionLog;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The object/relational metadata processor for the EJB3.0 specification. 
 * 
 * @author Guy Pelletier, Sanjeeb.Sahoo@Sun.COM
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class MetadataProcessor {
    /*
     * Design Pattern in use: Builder pattern
     * EntityManagerSetupImpl, MetadataProcessor and MetadataProject
     * play the role of director, builder and product respectively.
     */
    protected ClassLoader m_loader;
    protected MetadataLogger m_logger;
    protected MetadataProject m_project;
    protected MetadataValidator m_validator;
    protected AbstractSession m_session;

    /**
     * INTERNAL:
     * Called from EntityManagerSetupImpl. The 'real' EJB 3.0 processing
     * that includes XML and annotations.
     */
    public MetadataProcessor(PersistenceUnitInfo puInfo, AbstractSession session, ClassLoader loader, boolean enableLazyForOneToOne) {
        m_loader = loader;
        m_session = session;
        m_logger = new MetadataLogger(session);
        m_project = new MetadataProject(puInfo, session, enableLazyForOneToOne);
    }
    
    /**
     * INTERNAL:
     * Called from RelationshipWeaverTestSuite. Use this constructor to avoid
     * XML processing.
     * @deprecated
     */
    public MetadataProcessor(AbstractSession session, ClassLoader loader, Collection<Class> entities, boolean enableLazyForOneToOne) {
        m_loader = loader;
        m_project = new MetadataProject(null, session, enableLazyForOneToOne);
        m_session = session;
        Collection<String> entityNames = new HashSet<String>(entities.size());
        for (Class entity : entities) {
            m_project.addDescriptor(new MetadataDescriptor(entity));
            entityNames.add(entity.getName());
        }
        m_project.setEntityNames(entityNames);
        m_logger = new MetadataLogger(session);
    }
    
    /**
     * INTERNAL: 
     * Method to place EntityListener's on the descriptors from the given 
     * session. This call is made from the EntityManagerSetup deploy call.
     */
    public void addEntityListeners() {
        for (MetadataDescriptor descriptor: m_project.getDescriptors()) {
            // Process all descriptors that are in our project.
            ClassAccessor accessor = descriptor.getClassAccessor();
            
            descriptor.setJavaClass(descriptor.getClassDescriptor().getJavaClass());
            // The class loader has changed, update the class stored for
            // our class accessor and its list of mapped superclasses.
            accessor.setAnnotatedElement(descriptor.getJavaClass());
            accessor.clearMappedSuperclasses();            
            
            accessor.processListeners(m_loader);
        }
    }
    
    /**
     * INTERNAL:
     * Method to place NamedQueries and NamedNativeQueries on the given session. 
     * This call is made from the EntityManagerSetup deploy call.
     */
    public void addNamedQueries() {
        m_project.processNamedQueries(m_validator);
        m_project.processNamedNativeQueries(m_loader);
    }
    
    /**
     * INTERNAL:
     * Return a set of class names for each entity found in the xml
     * descriptor instance document.
     */
    private static Set<String> buildEntityClassSetFromXMLDocument(Document document, String fileName, ClassLoader loader) {
        XMLHelper helper = new XMLHelper(document, fileName, loader);
        
        // Process the package node.
        String defaultPkg = helper.getNodeValue(new String[] {XMLConstants.ENTITY_MAPPINGS, XMLConstants.PACKAGE, XMLConstants.TEXT});

        // Handle entities only. Mapped superclasses and embeddables are
        // discovered and processed separately.
        HashSet<String> classSet = new HashSet<String>();
        classSet.addAll(buildEntityClassSetForNodeList(helper, XMLConstants.ENTITY, defaultPkg));

        return classSet;
    }
    
    /**
     * INTERNAL:
     * The class name of each node in the node list will be added to the 
     * provided collection.
     */
    private static Set<String> buildEntityClassSetForNodeList(XMLHelper helper, String xPath, String defaultPkg) {
    	HashSet<String> classNames = new HashSet<String>();
        NodeList nodes = helper.getNodes(XMLConstants.ENTITY_MAPPINGS, xPath);
    	int nodeCount = nodes.getLength();
        
        for (int i = 0; i < nodeCount; i++) {
            // Process the required class attribute node.
            classNames.add(XMLHelper.getFullyQualifiedClassName(helper.getNode(nodes.item(i), XMLConstants.ATT_CLASS).getNodeValue(), defaultPkg));
        }
        
        return classNames;
    }
    
    /** 
     * INTERNAL:
	 * Return the logger used by the processor.
	 */
	public MetadataLogger getLogger() {
        return m_logger;
    }
	
    /**
     * INTERNAL:
     */
     public MetadataProject getProject() {
         return m_project;
     }
    
    /** 
     * INTERNAL:
	 * Return the validator used by the processor.
	 */
	public MetadataValidator getValidator() {
        return m_validator;
    }
	
    /**
     * INTERNAL:
     * Called from RelationshipWeaverTestSuite which uses only annotations
     * and no XML.
     */
    public void processAnnotations() {
        // Set the correct contextual validator.
        m_validator = new MetadataValidator();

        // take a copy of the collection to avoid concurrent modification exception
        // that would result when embeddables are added lazily.
        for (MetadataDescriptor descriptor:
             m_project.getDescriptors().toArray(new MetadataDescriptor[]{})) {
            // Process all descriptors that are in our project.
            ClassAccessor accessor = descriptor.getClassAccessor();
                
            // If there is no accessor on this descriptor then it has not been
            // processed yet. Create one and process it.
            if (accessor == null) {
                accessor = new ClassAccessor(new MetadataClass(descriptor.getJavaClass()), this, descriptor);
                descriptor.setClassAccessor(accessor);
                accessor.process();
            }
        } 
        
        // Process the project and anything that was deferred like
        // sequencing and relationship mappings and we are done.
        m_project.process();
    }
    
    /**
     * INTERNAL:
     * Process persistence unit metadata and defaults, and apply them to each 
     * entity in the collection. Any conflicts in elements defined in multiple 
     * documents will cause an exception to be thrown.  The first instance 
     * encountered wins, i.e. any conflicts between PU metadata definitions in 
     * multiple instance documents will cause an exception to be thrown.  The 
     * one exception to this rule is default listeners: all default listeners 
     * found will be added to a list in the order that they are read from the 
     * instance document(s). 
     */
     public void processPersistenceUnitMetadata() {
        // For each orm xml instance document, process persistence unit
        // metadata/defaults and mapped superclasses.
        for (Map.Entry<URL, Document> mfDocPair : m_project.getMappingFiles().entrySet()) {
            // Initialize a helper for navigating the instance document.
            XMLHelper helper = new XMLHelper(mfDocPair.getValue(), mfDocPair.getKey().getFile(), m_loader);

            // Store all mapped-superclasses.
            NodeList nodes = helper.getNodes(XMLConstants.ENTITY_MAPPINGS, XMLConstants.MAPPED_SUPERCLASS);

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                Class cls = helper.getNodeValue(nodes.item(i), XMLConstants.ATT_CLASS, void.class);
                m_project.addMappedSuperclass(cls, node, helper);
            }

            // Store all embeddable classes.
            nodes = helper.getNodes(XMLConstants.ENTITY_MAPPINGS, XMLConstants.EMBEDDABLE);

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                Class cls = helper.getNodeValue(nodes.item(i), XMLConstants.ATT_CLASS, void.class);
                m_project.addEmbeddable(cls, node, helper);
            }

            // Look for a persistence-unit-metadata node.
            Node persistenceUnitMetadataNode = helper.getNode(new String[] {XMLConstants.ENTITY_MAPPINGS, XMLConstants.PU_METADATA});

            if (persistenceUnitMetadataNode != null) {
                MetadataPersistenceUnit persistenceUnit = new MetadataPersistenceUnit();

                // Process the xml-mapping-metadata-complete tag.
                persistenceUnit.setIsMetadataComplete(helper.getNode(persistenceUnitMetadataNode, XMLConstants.METADATA_COMPLETE) != null);

                // process persistence unit defaults
                Node persistenceUnitDefaultsNode = helper.getNode(persistenceUnitMetadataNode, XMLConstants.PU_DEFAULTS);

                if (persistenceUnitDefaultsNode != null) {
                    // Process the persistence unit access.
                    persistenceUnit.setAccess(helper.getNodeTextValue(persistenceUnitDefaultsNode, XMLConstants.ACCESS));

                    // Process the persitence unit schema.
                    persistenceUnit.setSchema(helper.getNodeTextValue(persistenceUnitDefaultsNode, XMLConstants.SCHEMA));

                    // Process the persistence unit catalog.
                    persistenceUnit.setCatalog(helper.getNodeTextValue(persistenceUnitDefaultsNode, XMLConstants.CATALOG));

                    // Process the persistence unit cascade-persist.
                    persistenceUnit.setIsCascadePersist(helper.getNode(persistenceUnitDefaultsNode, XMLConstants.CASCADE_PERSIST) != null);

                    // Process the default entity-listeners. No conflict
                    // checking will be done, that is, any and all
                    // default listeners will be added to the project.
                    NodeList listenerNodes = helper.getNodes(persistenceUnitDefaultsNode, XMLConstants.ENTITY_LISTENERS, XMLConstants.ENTITY_LISTENER);
                    if (listenerNodes != null) {
                        m_project.addDefaultListeners(listenerNodes, helper);
                    }
                }

                // Add the metadata persistence unit to the project if
                // there is no conflicting metadata (from other
                // persistence unit metadata)
                MetadataPersistenceUnit existingPersistenceUnit = m_project.getPersistenceUnit();
                if (existingPersistenceUnit != null) {
                    if (! existingPersistenceUnit.equals(persistenceUnit)) {
                        (new XMLValidator()).throwPersistenceUnitMetadataConflict(existingPersistenceUnit.getConflict());
                    }
                } else {
                    m_project.setPersistenceUnit(persistenceUnit);
                }
            }
        }
    }
    
    /**
     * INTERNAL:
	 * Use this method to set the correct class loader that should be used
     * during processing.
	 */
	public void setClassLoader(ClassLoader loader) {
        m_loader = loader;
    }

    /**
     * This method is responsible for figuring out list of mapping files
     * to be read for a persistence unit and storing that list in
     * {@link MetadataProject}.
     * @param throwExceptionOnFail
     */
    public void readMappingFiles(boolean throwExceptionOnFail) {
        // Initialize the correct contextual objects.
        m_validator = new XMLValidator();

        // step #1: discover all the standard XML mapping files.
        Map<URL, Document> list = readStandardMappingFiles();

        // step #2: add URLs corresponding to explicitly specified files
        list.putAll(readExplicitlySpecifiedMappingFiles(throwExceptionOnFail));
        m_project.setMappingFiles(list);
    }

    private Map<URL,Document> readStandardMappingFiles() {
        Map<URL, Document> list = new HashMap<URL, Document>();
        final PersistenceUnitInfo puInfo = m_project.getPUInfo();
        Collection<URL> rootUrls = new HashSet<URL>(puInfo.getJarFileUrls());
        rootUrls.add(puInfo.getPersistenceUnitRootUrl());
        final String ormXMLFile = "META-INF/orm.xml";
        for(URL rootURL : rootUrls) {
            logMessage("Searching for default mapping file in " + rootURL); // NOI18N
            URL ormURL = null;
            InputStream stream = null;
            try {
                Archive m_par = null;
                m_par = new ArchiveFactoryImpl().createArchive(rootURL);
                ormURL = m_par.getEntryAsURL(ormXMLFile);
                stream = m_par.getEntry(ormXMLFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            if (stream != null){
                logMessage("Found a default mapping file at " + ormURL + " for root URL " + rootURL); // NOI18N
                try {
                    Document document = XMLHelper.parseDocument(stream, ormURL.getFile(), m_loader);
                    list.put(ormURL, document);
                } finally {
                    try{
                        stream.close();
                    } catch (IOException e) {}
                }
            }
        }
        return list;
    }

    private Map<URL, Document> readExplicitlySpecifiedMappingFiles(
            boolean throwExceptionOnFail) {
        Map<URL, Document> list = new HashMap<URL, Document>();
        final PersistenceUnitInfo puInfo = m_project.getPUInfo();
        for (String mf : puInfo.getMappingFileNames()) {
            try {
                Enumeration<URL> mfURLs = m_loader.getResources(mf);
                if (mfURLs.hasMoreElements()) {
                    URL nextURL = mfURLs.nextElement();
                    if(mfURLs.hasMoreElements()) {
                        handleORMException(ValidationException.nonUniqueMappingFileName(puInfo.getPersistenceUnitName(), mf), mf, throwExceptionOnFail);
                    }
                    InputStream stream = null;
                    stream = nextURL.openStream();
                    Document document = XMLHelper.parseDocument(stream, nextURL.getFile(), m_loader);
                    list.put(nextURL, document);
                    try{
                        stream.close();
                    } catch (IOException e) {}
                } else {
                    handleORMException(ValidationException.mappingFileNotFound(puInfo.getPersistenceUnitName(), mf), mf, throwExceptionOnFail);
                }
            } catch (IOException e) {
                handleORMException(
                        PersistenceUnitLoadingException.exceptionLoadingORMXML(mf, e),
                        mf, throwExceptionOnFail);
            }
        }
        return list;
    }

    /**
     *  Handle an exception that occured while processing ORM xml
     */
    private void handleORMException(
            RuntimeException e,
            String mf,
            boolean throwException){
        if (m_session == null){
            // Metadata processor is mainly used with a session.
            // Java SE bootstraping uses some functions such as ORM processing without
            // a session.  In these cases, it is impossible to get the
            // session to properly handle the exception.  As a result we
            // log an error.  The same code will be called later in the bootstrapping
            // code and the error will be handled then.
            AbstractSessionLog.getLog().log(SessionLog.CONFIG,
                    EntityManagerSetupImpl.ERROR_LOADING_XML_FILE,
                    new Object[] {mf, e});
        } else if (!throwException) {
            // fail quietly
            m_session.log(SessionLog.CONFIG,
                    SessionLog.EJB_OR_METADATA,
                    EntityManagerSetupImpl.ERROR_LOADING_XML_FILE,
                    new Object[] {mf, e});
        } else {
            // fail loudly
            m_session.handleException(e);
        }
    }

    /**
     * This method is responsible for discovering all the entity classes
     * for this PU and adding corresponding MetadataDescriptor in the
     * MetadataProject. Don't call this method more than once.
     */
    public void buildEntityList() {
        Set<String> classNames = buildEntityClassSetFromAnnotations();

        // append the list of entity classes that are defined in the XML descriptor
        classNames.addAll(buildEntityClassSetFromXMLDocuments());

        m_project.setEntityNames(classNames);

        // Add a metadata descriptor to the project for every entity class.
        // Any persistence unit metadata/defaults will be applied
        for (String className : classNames) {
            try {
                m_project.addDescriptor(
                        new MetadataDescriptor(m_loader.loadClass(className)));
            } catch (ClassNotFoundException e) {
                AbstractSessionLog.getLog().log(SessionLog.WARNING,
                        "exception_loading_entity_class", className, e);
            }
        }
    }

    /**
     * Return a set of class names that are annotated as either @Entity
     * from the base URL of this PersistenceUnit
     */
    private Set<String> buildEntityClassSetFromAnnotations() {
        Set<String> set = new HashSet<String>();
        PersistenceUnitInfo puInfo = m_project.getPUInfo();

        for (String className : puInfo.getManagedClassNames()) {
            if (PersistenceUnitProcessor.isEntity(className, m_loader, true)) {
                set.add(className);
            }
        }

        for (URL url : puInfo.getJarFileUrls()) {
            set.addAll(PersistenceUnitProcessor.getEntityClassNamesFromURL(url, m_loader));
        }

        if (!puInfo.excludeUnlistedClasses()){
            set.addAll(PersistenceUnitProcessor.getEntityClassNamesFromURL(
                    puInfo.getPersistenceUnitRootUrl(), m_loader));
        }
       return set;

    }

    /**
     * INTERNAL:
     * Return a set of class names for each entity found in the
     * list of xml descriptor instance documents to be processed by the
     * MetadataProcessor.
     *
     * @return
     */
    public Set<String> buildEntityClassSetFromXMLDocuments() {
        HashSet<String> classSet = new HashSet<String>();
        for (Map.Entry<URL, Document> urlToDoc :
                m_project.getMappingFiles().entrySet()) {
            classSet.addAll(buildEntityClassSetFromXMLDocument(
                    urlToDoc.getValue(), urlToDoc.getKey().getFile(), m_loader));
        }
        return classSet;
    }

    /**
     * INTERNAL:
     * Process metadata found in all the mapping files for this PU.
     * @see #processMappingFile(org.w3c.dom.Document, String)
     */
    public void processMappingFiles() {
        for (Map.Entry<URL, Document> urlToDocPair : m_project.getMappingFiles().entrySet()) {
            processMappingFile(urlToDocPair.getValue(), urlToDocPair.getKey().getFile());
        }
    }

    /**
     * Process the xml and fill in the project. Process the entity-mappings
     * information then process the entities.
     */
    private void processMappingFile(Document document, String fileName) {
        if (m_project.hasDescriptors()) {
            XMLHelper helper = new XMLHelper(document, fileName, m_loader);

            // Process the entity mappings ... this is a crude way of doing
            // things ... but hey ... the clock is ticking ...
            MetadataDescriptor desc = m_project.getDescriptors().iterator().next();
            XMLClassAccessor dummyAccessor = new XMLClassAccessor(new MetadataClass(desc.getJavaClass()), null, helper, this, desc);
            dummyAccessor.processEntityMappings();

            // Process the entity nodes for this xml document.
            NodeList entityNodes = helper.getNodes(XMLConstants.ENTITY_MAPPINGS, XMLConstants.ENTITY);

            if (entityNodes != null) {
                for (int i = 0; i < entityNodes.getLength(); i++) {
                    Node entityNode = entityNodes.item(i);
                    Class entityClass = helper.getClassForNode(entityNode);
                    MetadataDescriptor descriptor = m_project.getDescriptor(entityClass);

                    // Process all descriptors that are in our project.
                    ClassAccessor accessor = descriptor.getClassAccessor();

                    // If there is no accessor on this descriptor then it has not
                    // been processed yet. Create one and process it.
                    if (accessor == null) {
                        accessor = new XMLClassAccessor(new MetadataClass(descriptor.getJavaClass()), entityNode, helper, this, descriptor);
                        descriptor.setClassAccessor(accessor);
                        accessor.process();
                    }
                }
            }
        } else {
            // WIP - log a warning that we have no entities to process ...
        }
    }

    /**
     * This method frees up resources acquired by this object.
     */
    public void cleanup() {
        m_project.cleanup();
        m_loader = null;
        m_project = null;
        m_session = null;
    }

    /**
     * Log an untranslated message to the TopLink log at FINER level.
     * @param msg message to be logged
     */
    private void logMessage(String msg) {
        if (m_session == null){
            AbstractSessionLog.getLog().log(SessionLog.FINER, msg);
        } else {
            m_session.logMessage(msg);
        }
    }
}
