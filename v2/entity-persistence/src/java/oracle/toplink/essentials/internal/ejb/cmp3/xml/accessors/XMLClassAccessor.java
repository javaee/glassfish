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
package oracle.toplink.essentials.internal.ejb.cmp3.xml.accessors;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Map;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.ClassAccessor;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.MetadataAccessor;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.objects.MetadataAccessibleObject;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.objects.MetadataClass;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.objects.MetadataField;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.objects.MetadataMethod;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.columns.MetadataPrimaryKeyJoinColumns;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.listeners.MetadataEntityListener;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataDescriptor;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataHelper;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataPersistenceUnit;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataProcessor;

import oracle.toplink.essentials.internal.ejb.cmp3.xml.accessors.XMLBasicAccessor;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.accessors.XMLEmbeddedAccessor;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.accessors.XMLManyToManyAccessor;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.accessors.XMLManyToOneAccessor;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.accessors.XMLOneToManyAccessor;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.accessors.XMLOneToOneAccessor;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.accessors.XMLTransientAccessor;

import oracle.toplink.essentials.internal.ejb.cmp3.xml.columns.XMLColumn;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.columns.XMLDiscriminatorColumn;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.columns.XMLJoinColumns;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.columns.XMLPrimaryKeyJoinColumns;

import oracle.toplink.essentials.internal.ejb.cmp3.xml.listeners.XMLEntityClassListener;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.listeners.XMLEntityListener;

import oracle.toplink.essentials.internal.ejb.cmp3.xml.queries.XMLNamedNativeQuery;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.queries.XMLNamedQuery;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.queries.XMLSQLResultSetMapping;

import oracle.toplink.essentials.internal.ejb.cmp3.xml.sequencing.XMLSequenceGenerator;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.sequencing.XMLTableGenerator;

import oracle.toplink.essentials.internal.ejb.cmp3.xml.tables.XMLSecondaryTable;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.tables.XMLTable;

import oracle.toplink.essentials.internal.ejb.cmp3.xml.XMLConstants;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.XMLHelper;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An XML extended class accessor.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class XMLClassAccessor extends ClassAccessor implements XMLAccessor {
    protected Node m_node;
    protected XMLHelper m_helper;
    
    // These entity-mappings settings need to be available to all xml entities
    // in the given xml file. The are processed by the first entity in the file.
    protected static String m_entityMappingsAccess;
    protected static String m_entityMappingsSchema;
    protected static String m_entityMappingsCatalog;
    
    /**
     * INTERNAL:
     */
    public XMLClassAccessor(MetadataAccessibleObject accessibleObject, Node node, XMLHelper helper, MetadataProcessor processor, MetadataDescriptor descriptor) {
        super(accessibleObject, processor, descriptor);
        m_node = node;
        m_helper = helper;
    }
    
    /**
     * INTERNAL:
     * Create and return the appropriate accessor based on the given node. 
     */
    protected MetadataAccessor buildAccessor(Node node) {
        MetadataAccessibleObject accessibleObject;
        
        // Process the required name attribute.
        String attributeName = m_helper.getNodeValue(node, XMLConstants.ATT_NAME);
        
        // WIP - left to do here is perform validation on the accessors just
        // like the annotation processor does.
        if (m_descriptor.usesPropertyAccess()) {
            Method method = MetadataHelper.getMethodForPropertyName(attributeName, getJavaClass());
            
            if (method == null) {
                m_validator.throwUnableToDetermineClassForProperty(attributeName, getJavaClass());
            }
            
            accessibleObject = new MetadataMethod(method);
        } else {
            Field field = MetadataHelper.getFieldForName(attributeName, getJavaClass());
            
            if (field == null) {
                m_validator.throwUnableToDetermineClassForField(attributeName, getJavaClass());
            }
            
            accessibleObject = new MetadataField(field);
        }
        
        String nodeName = node.getLocalName();
        
        if (nodeName.equals(XMLConstants.ONE_TO_ONE)) {
            return new XMLOneToOneAccessor(accessibleObject, node, this);
        } else if (nodeName.equals(XMLConstants.MANY_TO_ONE)) {
            return new XMLManyToOneAccessor(accessibleObject, node, this);
        } else if (nodeName.equals(XMLConstants.ONE_TO_MANY)) {
            if (MetadataHelper.isSupportedCollectionClass(accessibleObject.getRawClass())) {
                return new XMLOneToManyAccessor(accessibleObject, node, this);        
            } else {
                m_validator.throwInvalidCollectionTypeForRelationship(getJavaClass(), accessibleObject.getRawClass(), getAttributeName());
                return null;
            }
        } else if (nodeName.equals(XMLConstants.MANY_TO_MANY)) {
            if (MetadataHelper.isSupportedCollectionClass(accessibleObject.getRawClass())) {
                return new XMLManyToManyAccessor(accessibleObject, node, this);
            } else {
                m_validator.throwInvalidCollectionTypeForRelationship(getJavaClass(), accessibleObject.getRawClass(), getAttributeName());
                return null;
            }
        } else if (nodeName.equals(XMLConstants.EMBEDDED)) {
            return new XMLEmbeddedAccessor(accessibleObject, node, this);
        } else if (nodeName.equals(XMLConstants.EMBEDDED_ID)) {
            return new XMLEmbeddedIdAccessor(accessibleObject, node, this);
        } else if (nodeName.equals(XMLConstants.TRANSIENT)) {
            return new XMLTransientAccessor(accessibleObject, node, this);
        } else {
            return new XMLBasicAccessor(accessibleObject, node, this);
        }
    }
    
    /**
     * INTERNAL:
     */
    public String getCatalog() {
        return XMLClassAccessor.m_entityMappingsCatalog;
    }
    
    /**
     * INTERNAL: (OVERRIDE)
     * Return the discriminator value for this accessor. If it is not defined 
     * call the parent to check for an annotation.
     */
    public String getDiscriminatorValue() {
        String discriminatorValue =  m_helper.getNodeTextValue(m_node, XMLConstants.DISCRIMINATOR_VALUE);
        
        if (discriminatorValue.equals("")) {
            return super.getDiscriminatorValue();
        } else {
            return discriminatorValue;
        }   
    }
    
    /**
     * INTERNAL:
     */
    public String getDocumentName() {
        return m_helper.getDocumentName();
    }
    
    /**
     * INTERNAL: (OVERRIDE)
     * Return the name of this entity class. If it is not defined call the
     * parent to check for an annotation.
     */
    public String getEntityName() {
        String entityName = m_helper.getNodeValue(m_node, XMLConstants.ATT_NAME);
        
        if (entityName.equals("")) {
            return super.getEntityName();
        } else {
            return entityName;
        }
    }
    
    /**
     * INTERNAL:
     */
    public XMLHelper getHelper() {
        return m_helper;
    }
    
    /**
     * INTERNAL: (OVERRIDE)
     * Return the inheritance strategy. This method should only be called
     * on the root of the inheritance hierarchy.
     */
    protected String getInheritanceStrategy() {
        Node inheritanceNode = m_helper.getNode(m_node, XMLConstants.INHERITANCE);
        
        if (inheritanceNode == null) {
            return super.getInheritanceStrategy();
        } else {
            return m_helper.getNodeValue(inheritanceNode, XMLConstants.ATT_STRATEGY);
        } 
    }
    
    /**
     * INTERNAL: (OVERRIDE)
     * Process the primary-key-join-column(s) elements.
     */    
    protected MetadataPrimaryKeyJoinColumns getPrimaryKeyJoinColumns(String sourceTableName, String targetTableName) {
        if (m_helper.nodeHasPrimaryKeyJoinColumns(m_node)) {
            return new XMLPrimaryKeyJoinColumns(m_node, m_helper, sourceTableName, targetTableName);
        } else {
            return super.getPrimaryKeyJoinColumns(sourceTableName, targetTableName);
        }
    }
    
    /**
     * INTERNAL:
     */
    public String getSchema() {
         return XMLClassAccessor.m_entityMappingsSchema;
    }
    
    /**
     * INTERNAL: (Override from ClassAccessor)
     * 
     * Return true if this class has an entity node.
     */
    protected boolean hasEntity(Class cls) {
        Node node = m_helper.locateEntityNode(cls);
        
        if (node != null) {
            return true;
        } else {
            return super.hasEntity(cls);
        }
    }
    
    /**
     * INTERNAL: (Override from ClassAccessor)
     * 
     * Return true if this class has an inheritance node.
     */
    protected boolean hasInheritance(Class entityClass) {
        Node node = m_helper.locateEntityNode(entityClass);
        
    	if (node != null && m_helper.hasNode(node, XMLConstants.INHERITANCE)) {
            return true;
    	} else {
            return super.hasInheritance(entityClass);
        }
    }
    
    /**
     * INTERNAL:
	 * Return true if this is an XML processing accessor.
     */
	public boolean isXMLAccessor() {
        return true;
    }
    
    /**
     * INTERNAL:
     * Process any entity tag specifics then call the parent process.
     */
    public void process() {
        // Process the metadata-complete attribute node.
        m_descriptor.setIgnoreAnnotations(m_helper.getNodeValue(m_node, XMLConstants.ATT_METADATA_COMPLETE, m_descriptor.ignoreAnnotations()));
            
        // Process the access attribute.
        m_descriptor.setAccess(m_helper.getNodeValue(m_node, XMLConstants.ATT_ACCESS, XMLClassAccessor.m_entityMappingsAccess));
        
        // Set the entity mapping schema value. The schema defaults to the
        // persistence unit schema if it is not set.
        m_descriptor.setSchema(XMLClassAccessor.m_entityMappingsSchema);
            
        // Set the entity mapping catalog value. The catalog defaults to the
        // persistence unit catalog if it is not set.
        m_descriptor.setCatalog(XMLClassAccessor.m_entityMappingsCatalog);
        
        super.process();
    }
    
    /**
     * INTERNAL: (OVERRIDE)
     * Fast track processing a ClassAccessor for the given descriptor. 
     * Inheritance root classes and embeddables may be fast tracked.
     */
    protected ClassAccessor processAccessor(MetadataDescriptor descriptor) {
        Node node = null;
        XMLHelper xmlhelper = null;
        
        node = m_helper.locateEntityNode(descriptor.getJavaClass());
        
        if(node!=null){
            xmlhelper = m_helper;
        } else {
            //Bug#2962 Cover the case when the given descriptor defined in the separate mapping XML file rather than 
            //the one referred by m_helper. 
            for (Map.Entry<URL, Document> urlToDocPair : m_project.getMappingFiles().entrySet()) {
                Document document = (Document)urlToDocPair.getValue();
                xmlhelper = new XMLHelper(document, urlToDocPair.getKey().getFile(), m_helper.getClassLoader());
                node = xmlhelper.locateEntityNode(descriptor.getJavaClass());
                if(node!=null){
                    break;
                }
            }
        }
        
        if (node != null) {
            XMLClassAccessor accessor = new XMLClassAccessor(new MetadataClass(descriptor.getJavaClass()), node, xmlhelper, m_processor, descriptor);
            descriptor.setClassAccessor(accessor);
            accessor.process();
            accessor.setIsProcessed();
            return accessor;
        } else {
            return super.processAccessor(descriptor);
        }
    }
    
    /**
     * INTERNAL: (OVERRIDE)
     * Process the accessors for the given class.
     */
    protected void processAccessors() {
        NodeList nodes = m_helper.getNodes(m_node, XMLConstants.ATTRIBUTES, XMLConstants.ALL_CHILDREN);
        
        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                processAccessor(buildAccessor(nodes.item(i)));
            }
        }
        
        super.processAccessors();
    }
    
    /**
     * INTERNAL: (OVERRIDE)
     * Process the association overrides for a class that inherits from a
     * mapped superclass. Once the association overrides are processed from
     * XML process the association overrides from annotations.
     */
    protected void processAssociationOverrides() {
        // Process the XML association override elements first.
        NodeList nodes = m_helper.getNodes(m_node, XMLConstants.ASSOCIATION_OVERRIDE);
        
        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                processAssociationOverride(m_helper.getNodeValue(node, XMLConstants.ATT_NAME), new XMLJoinColumns(node, m_helper));
            }
        }
        
        // Process the association override annotations second.
        super.processAssociationOverrides();
    }
    
    /**
     * INTERNAL: (OVERRIDE)
	 * Process the attribute overrides for a class that inherits from a
     * mapped superclass. Once the attribute overrides are processed from
     * XML process the attribute overrides from annotations.
	 */
    protected void processAttributeOverrides() {
        NodeList nodes = m_helper.getNodes(m_node, XMLConstants.ATTRIBUTE_OVERRIDE);
        
        if (nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                processAttributeOverride(new XMLColumn(nodes.item(i), m_helper, getAnnotatedElement()));
            }
        }
        
        
        // Now, Process the attribute override annotations second.
        super.processAttributeOverrides();
    }
    
    /**
     * INTERNAL:
     * Process an XML discriminator-column metadata. If we don't find a node
     * here, check for an annotation by calling the parent. It will default if 
     * no annotation is found.
     */
    protected void processDiscriminatorColumn() {
        Node node = m_helper.getNode(m_node, XMLConstants.DISCRIMINATOR_COLUMN);
        
        if (node == null) {
            super.processDiscriminatorColumn();
        } else {
            processDiscriminatorColumn(new XMLDiscriminatorColumn(node, m_helper));
        }
    }
    
    /**
     * INTERNAL: (OVERRIDE)
     * Process the entity class for lifecycle callback event methods.
     */
    public MetadataEntityListener processEntityEventListener(ClassLoader loader) {
        // Update the class loader.
        m_helper.setLoader(loader);
        
        // Create the listener.
        XMLEntityClassListener listener = new XMLEntityClassListener(getJavaClass());
            
        // Process the lifecycle callback events from XML.
        Method[] candidateMethods = MetadataHelper.getCandidateCallbackMethodsForEntityClass(getJavaClass());
        processLifecycleEvents(listener, m_node, m_helper, candidateMethods);
            
        // Check the entity class for lifecycle callback annotations.
        processCallbackMethods(candidateMethods, listener);
        
        // WIP - at this point we should turn the override ignore off for 
        // mapped superclasses ...
        return listener;
    }
    
    /**
     * INTERNAL: (OVERRIDE)
     * Process the entity listeners for this class accessor. Entity listeners
     * defined in XML will override those specified on the class.
     */
    public void processEntityListeners(Class entityClass, ClassLoader loader) {
        // Update the class loader.
        m_helper.setLoader(loader);
        
        NodeList nodes = m_helper.getNodes(m_node, new String[] {XMLConstants.ENTITY_LISTENERS, XMLConstants.ENTITY_LISTENER});
        
        if (nodes.getLength() > 0) {
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                
                // Build an xml entity listener.
                XMLEntityListener listener = new XMLEntityListener(m_helper.getClassForNode(node), entityClass);
                
                // Process the lifecycle callback events from XML.
                Method[] candidateMethods = MetadataHelper.getCandidateCallbackMethodsForEntityListener(listener);
                processLifecycleEvents(listener, node, m_helper, candidateMethods);
                
                // Process the candidate callback methods on this listener for
                // additional callback methods decorated with annotations.
                processCallbackMethods(candidateMethods, listener);
        
                // Add the listener to the descriptor.
                m_descriptor.addEntityListenerEventListener(listener);
            }
        } else {
            super.processEntityListeners(entityClass, loader);
        }
    }
    
    /**
     * INTERNAL:
     * Process the information contained in the entity-mappings node.
     */
    public void processEntityMappings() {
        MetadataPersistenceUnit persistenceUnit = m_project.getPersistenceUnit();
        
        if (persistenceUnit != null) {
            // Use the persistent unit defaults ..
            XMLClassAccessor.m_entityMappingsAccess = m_helper.getNodeTextValue(XMLConstants.ENTITY_MAPPINGS, XMLConstants.ACCESS, persistenceUnit.getAccess());
            XMLClassAccessor.m_entityMappingsSchema = m_helper.getNodeTextValue(XMLConstants.ENTITY_MAPPINGS, XMLConstants.SCHEMA, persistenceUnit.getSchema());
            XMLClassAccessor.m_entityMappingsCatalog = m_helper.getNodeTextValue(XMLConstants.ENTITY_MAPPINGS, XMLConstants.CATALOG, persistenceUnit.getCatalog());
        } else {
            XMLClassAccessor.m_entityMappingsAccess = m_helper.getNodeTextValue(XMLConstants.ENTITY_MAPPINGS, XMLConstants.ACCESS);
            XMLClassAccessor.m_entityMappingsSchema = m_helper.getNodeTextValue(XMLConstants.ENTITY_MAPPINGS, XMLConstants.SCHEMA);
            XMLClassAccessor.m_entityMappingsCatalog = m_helper.getNodeTextValue(XMLConstants.ENTITY_MAPPINGS, XMLConstants.CATALOG);
        }
        
        // Process the table-generator nodes.
        NodeList tableGeneratorNodes = m_helper.getNodes(XMLConstants.ENTITY_MAPPINGS, XMLConstants.TABLE_GENERATOR);
        
        if (tableGeneratorNodes != null) {
            for (int i = 0; i < tableGeneratorNodes.getLength(); i++) {
                processTableGenerator(tableGeneratorNodes.item(i));
            }
        }
        
        // Process the sequence-generator nodes.
        NodeList sequenceGeneratorNodes = m_helper.getNodes(XMLConstants.ENTITY_MAPPINGS, XMLConstants.SEQUENCE_GENERATOR);
        
        if (sequenceGeneratorNodes != null) {
            for (int i = 0; i < sequenceGeneratorNodes.getLength(); i++) {
                processSequenceGenerator(sequenceGeneratorNodes.item(i));
            }
        }
        
        // Process the named-query nodes.
        processNamedQueries(m_helper.getNodes(XMLConstants.ENTITY_MAPPINGS, XMLConstants.NAMED_QUERY));

        // Process the named-native-query nodes.
        processNamedNativeQueries(m_helper.getNodes(XMLConstants.ENTITY_MAPPINGS, XMLConstants.NAMED_NATIVE_QUERY));

        // Process the sql-result-set-mapping nodes.
        processSqlResultSetMappings(m_helper.getNodes(XMLConstants.ENTITY_MAPPINGS, XMLConstants.SQL_RESULT_SET_MAPPING));
    }
    
    /**
     * INTERNAL: (OVERRIDE)
     * Process the exclude-default-listeners tag if one is specified, otherwise,
     * ask the parent to look for an annotation.
     */
    protected void processExcludeDefaultListeners() {
        if (m_helper.hasNode(m_node, XMLConstants.EXCLUDE_DEFAULT_LISTENERS)) {
            m_descriptor.setExcludeDefaultListeners(true);
        } else {
            super.processExcludeDefaultListeners();
        }
    }
    
    /**
     * INTERNAL: (OVERRIDE)
     * Process the exclude-superclass-listeners tag if one is specified, 
     * otherwise, ask the parent to look for an annotation.
     */
    protected void processExcludeSuperclassListeners() {
        if (m_helper.hasNode(m_node, XMLConstants.EXCLUDE_SUPERCLASS_LISTENERS)) {
            m_descriptor.setExcludeSuperclassListeners(true);
        } else {
            super.processExcludeSuperclassListeners();
        } 
    }
    
    /**
     * INTERNAL: (OVERRIDE)
     * 
     * Process an id-class element
     */
    protected void processIdClass() {
        Node result = m_helper.getNode(m_node, XMLConstants.ID_CLASS);
        
        if (result == null) {
            // Check for an @IdClass annotation.
            super.processIdClass();
        } else {
            processIdClass(m_helper.getClassForNode(result), m_logger.IGNORE_ID_CLASS_ELEMENT);
        }
    }
    
    /**
     * INTERNAL: (OVERRIDE)
     * Process the mapped superclass class for lifecycle callback event methods.
     */
    public void processMappedSuperclassEventListener(MetadataEntityListener listener, Class entityClass, ClassLoader loader) {
        // Update the class loader
        m_helper.setLoader(loader);
        
        // Process the lifecycle callback events from XML.
        Method[] candidateMethods = MetadataHelper.getCandidateCallbackMethodsForMappedSuperclass(getJavaClass(), entityClass);
        processLifecycleEvents(listener, m_node, m_helper, candidateMethods);
        
        // Check for annotations on the mapped superclass now.
        processCallbackMethods(candidateMethods, listener);
    }
    
    /**
     * INTERNAL: (OVERRIDE)
     * Process the named native queries for the given class which could be an 
     * entity or a mapped superclass.
     */
    protected void processNamedNativeQueries() {
        // Process the named native query elements first.
        processNamedNativeQueries(m_helper.getNodes(m_node, XMLConstants.NAMED_NATIVE_QUERY));
        
        // Process the XML named native query annotations second.
        super.processNamedNativeQueries();
    }
    
    /**
     * INTERNAL:
     * Process named-queries at either the entity-mappings or entity level. The
     * queries will be stored in a hashmap - EntityManagerSetupImpl will call 
     * 'addNamedQueriesToSession()' after processing; this is when the queries
     * will be added to the session.
     */
    protected void processNamedNativeQueries(NodeList queryNodes) {
        if (queryNodes != null) {
            for (int i = 0; i < queryNodes.getLength(); i++) {
                // Ask the common processor to process what we found.
                processNamedNativeQuery(new XMLNamedNativeQuery(queryNodes.item(i), m_helper));
            }
        }
    }
    
    /**
     * INTERNAL: (OVERRIDE)
     * Process the named queries for the given class which could be an entity
     * or a mapped superclass.
     */
    protected void processNamedQueries() {
        // Process the XML named query elements first.
        processNamedQueries(m_helper.getNodes(m_node, XMLConstants.NAMED_QUERY));
        
        // Process the named query annotations second.
        super.processNamedQueries();
    }
    
    /**
     * INTERNAL:
     * Process named queries at either the entity-mapping or entity level. The 
     * queries will be stored in a hashmap - EntityManagerSetupImpl will call 
     * 'addNamedQueriesToSession()' after processing; this is when the queries
     * will be added to the session.
     */
    protected void processNamedQueries(NodeList queryNodes) {
        if (queryNodes != null) {
            for (int i = 0; i < queryNodes.getLength(); i++) {
                // Ask the common processor to process what we found.
                processNamedQuery(new XMLNamedQuery(queryNodes.item(i), m_helper));
            }
        }
    }
    
    /**
     * INTERNAL: (OVERRIDE)
     * Process secondary-table(s) for a given entity.
     */
    protected void processSecondaryTables() {
        NodeList secondaryTables = m_helper.getNodes(m_node, XMLConstants.SECONDARY_TABLE);
        
        if (secondaryTables == null) {
            // Check for a secondary table annotation(s).
            super.processSecondaryTables();
        } else {
            if (m_descriptor.ignoreTables()) {
                m_logger.logWarningMessage(m_logger.IGNORE_SECONDARY_TABLE_ELEMENT, getJavaClass());
            } else {
                for (int i = 0; i < secondaryTables.getLength(); i++) {
                    processSecondaryTable(new XMLSecondaryTable(secondaryTables.item(i), m_helper, m_logger));
                }
            }
        }
    }
    
    /**
     * INTERNAL: (OVERRIDE)
	 * Process this accessor's sequence-generator node into a common metadata 
     * sequence generator.
     */
    protected void processSequenceGenerator() {
        // Process the xml defined sequence generators first.
        processSequenceGenerator(m_helper.getNode(m_node, XMLConstants.SEQUENCE_GENERATOR));
        
        // Process the annotation defined sequence generators second.
        super.processSequenceGenerator();
    }
    
    /**
     * INTERNAL:
     * Process a sequence-generator node into a common metadata sequence 
     * generator.
     */
    protected void processSequenceGenerator(Node node) {
        if (node != null) {
            // Ask the common processor to process what we found.
            processSequenceGenerator(new XMLSequenceGenerator(node, m_helper));
        }
    }
    
    /**
     * INTERNAL: (OVERRIDE)
     * Process the sql result set mappings for the given class which could be an 
     * entity or a mapped superclass.
     */
    protected void processSqlResultSetMappings() {
        // Process the XML sql result set mapping elements first.
        processSqlResultSetMappings(m_helper.getNodes(m_node, XMLConstants.SQL_RESULT_SET_MAPPING));
        
        // Process the sql result set mapping query annotations second.
        super.processSqlResultSetMappings();
    }
    
    /**
     * INTERNAL:
     * Process sql-result-set-mappings and store them on the session.
     */
    protected void processSqlResultSetMappings(NodeList sqlResultSetNodes) {
        if (sqlResultSetNodes != null) {
        	int nodeCount = sqlResultSetNodes.getLength();
        
            for (int i = 0; i < nodeCount; i++) {
                // Ask the common processor to process what we found.
                processSqlResultSetMapping(new XMLSQLResultSetMapping(sqlResultSetNodes.item(i), m_helper));
            }
        }
    }
    
    /**
     * INTERNAL: (OVERRIDE)
     * Process table information for the given metadata descriptor.
     */
    protected void processTable() {
	    Node tableNode = m_helper.getNode(m_node, XMLConstants.TABLE);
            
	    if (tableNode != null) {
            if (m_descriptor.ignoreTables()) {
                m_logger.logWarningMessage(m_logger.IGNORE_TABLE_ELEMENT, getJavaClass());
            } else {
                processTable(new XMLTable(tableNode, m_helper, m_logger));
            }
	    } else {
            // Check for a table annotation. If no annotation is defined, the 
            // table will default.
            super.processTable();
	    }
    }
    
    /**
     * INTERNAL: (OVERRIDE)
     * Process the table generator for the given class which could be an entity
     * or a mapped superclass.
     */
    protected void processTableGenerator() {
        // Process the xml defined table generators first.
        processTableGenerator(m_helper.getNode(m_node, XMLConstants.TABLE_GENERATOR));
        
        // Process the annotation defined sequence generators second.
        super.processTableGenerator();
    }
    
    /**
     * INTERNAL:
     * Process a table-generator node into a common metadata table generator.
     */
    protected void processTableGenerator(Node node) {
        if (node != null) {
            // Ask the common processor to process what we found.
            processTableGenerator(new XMLTableGenerator(node, this));
        }
    }
}
    
