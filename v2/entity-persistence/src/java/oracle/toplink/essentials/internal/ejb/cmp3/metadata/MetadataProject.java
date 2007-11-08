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

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.spi.PersistenceUnitInfo;

import oracle.toplink.essentials.sequencing.Sequence;
import oracle.toplink.essentials.sequencing.TableSequence;
import oracle.toplink.essentials.sequencing.NativeSequence;

import oracle.toplink.essentials.sessions.Project;
import oracle.toplink.essentials.sessions.DatasourceLogin;
import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.exceptions.ValidationException;
import oracle.toplink.essentials.queryframework.EJBQLPlaceHolderQuery;

import oracle.toplink.essentials.internal.helper.DatabaseTable;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

import oracle.toplink.essentials.internal.ejb.cmp3.EJBQueryImpl;
import oracle.toplink.essentials.internal.ejb.cmp3.base.QueryHintsHandler;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.MetadataAccessor;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.RelationshipAccessor;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.queries.MetadataQueryHint;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.queries.MetadataNamedQuery;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.queries.MetadataNamedNativeQuery;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.sequencing.MetadataGeneratedValue;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.sequencing.MetadataTableGenerator;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.sequencing.MetadataSequenceGenerator;

import oracle.toplink.essentials.internal.ejb.cmp3.xml.XMLHelper;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;

/**
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class MetadataProject {
    // persistence unit that is represented by this project
    protected PersistenceUnitInfo m_PUInfo;

    // names of all the entity in this PU
    protected Collection<String> m_entityNames = new HashSet<String>();

    // URL to Document object map of all the mapping files for this PU.
    // The reason for using URL instead of name is that name is not unique.
    protected Map<URL, Document> m_mappingFiles = new HashMap<URL, Document>();

    // The session we are currently processing for.
    protected AbstractSession m_session;

    // Boolean to specify if we should weave for value holders.
    protected boolean m_enableLazyForOneToOne;

    // Persistence unit metadata for this project.
    protected MetadataPersistenceUnit m_persistenceUnit;

    // List of mapped-superclasses found in XML for this project/persistence unit.
    protected HashMap<String, Node> m_mappedSuperclassNodes;
    protected HashMap<String, XMLHelper> m_mappedSuperclasses;

    // List of embeddables found in XML for this project/persistence unit.
    protected HashMap<String, Node> m_embeddableNodes;
    protected HashMap<String, XMLHelper> m_embeddables;

    // All the descriptors for this project.
    protected HashMap<String, MetadataDescriptor> m_allDescriptors;

    // Descriptors that have relationships.
    protected HashSet<MetadataDescriptor> m_descriptorsWithRelationships;

    // Named queries for this project.
    protected HashMap<String, MetadataNamedQuery> m_namedQueries;

    // NamedNativeQueries for this project.
    protected HashMap<String, MetadataNamedNativeQuery> m_namedNativeQueries;

    // Sequencing metadata.
    protected HashMap<Class, MetadataGeneratedValue> m_generatedValues;
    protected HashMap<String, MetadataTableGenerator> m_tableGenerators;
    protected HashMap<String, MetadataSequenceGenerator> m_sequenceGenerators;

    // Default listeners that need to be applied to each entity in the
    // persistence unit (unless they exclude them).
    protected HashMap<XMLHelper, NodeList> m_defaultListeners;

    /**
     * INTERNAL:
     */
    public MetadataProject(PersistenceUnitInfo puInfo, AbstractSession session, boolean enableLazyForOneToOne) {
        m_PUInfo = puInfo;
        m_session = session;
        m_enableLazyForOneToOne = enableLazyForOneToOne;

        m_defaultListeners = new HashMap<XMLHelper, NodeList>();

        m_namedQueries = new HashMap<String, MetadataNamedQuery>();
        m_namedNativeQueries = new HashMap<String, MetadataNamedNativeQuery>();

        m_mappedSuperclassNodes = new HashMap<String, Node>();
        m_mappedSuperclasses = new HashMap<String, XMLHelper>();

        m_embeddableNodes = new HashMap<String, Node>();
        m_embeddables = new HashMap<String, XMLHelper>();

        m_allDescriptors = new HashMap<String, MetadataDescriptor>();
        m_descriptorsWithRelationships = new HashSet<MetadataDescriptor>();

        m_generatedValues = new HashMap<Class, MetadataGeneratedValue>();
        m_tableGenerators = new HashMap<String, MetadataTableGenerator>();
        m_sequenceGenerators = new HashMap<String, MetadataSequenceGenerator>();
    }
    
    /**
     * INTERNAL:
     */
    public void addDefaultListeners(NodeList nodes, XMLHelper helper) {
        m_defaultListeners.put(helper, nodes);
    }

    /**
     * INTERNAL:
     * This method will add the descriptor to the actual TopLink project as
     * well if it has not already been added.
     */
    public void addDescriptor(MetadataDescriptor descriptor) {
        // Set the persistence unit defaults (if there are any) on the descriptor.

        if (m_persistenceUnit != null) {
            descriptor.setAccess(m_persistenceUnit.getAccess());
            descriptor.setSchema(m_persistenceUnit.getSchema());
            descriptor.setCatalog(m_persistenceUnit.getCatalog());
            descriptor.setIsCascadePersist(m_persistenceUnit.isCascadePersist());
            descriptor.setIgnoreAnnotations(m_persistenceUnit.isMetadataComplete());
        }

        Project project = getSession().getProject();
        ClassDescriptor descriptorOnProject = MetadataHelper.findDescriptor(project, descriptor.getJavaClass());

        if (descriptorOnProject == null) {
            project.addDescriptor(descriptor.getClassDescriptor());
        } else {
            descriptor.setDescriptor(descriptorOnProject);
        }

        m_allDescriptors.put(descriptor.getJavaClassName(), descriptor);
    }

    /**
     * INTERNAL:
     */
    public void addGeneratedValue(MetadataGeneratedValue metadatageneratedvalue, Class entityClass) {
        m_generatedValues.put(entityClass, metadatageneratedvalue);
    }

    /**
     * INTERNAL:
     * Add a mapped-superclass that we found in an XML document.
     */
    public void addMappedSuperclass(Class mappedSuperclass, Node node, XMLHelper helper) {
        m_mappedSuperclasses.put(mappedSuperclass.getName(), helper);
        m_mappedSuperclassNodes.put(mappedSuperclass.getName(), node);
    }

    /**
     * INTERNAL:
     * Add an embeddable that we found in an XML document.
     */
    public void addEmbeddable(Class embeddable, Node node, XMLHelper helper) {
        m_embeddables.put(embeddable.getName(), helper);
        m_embeddableNodes.put(embeddable.getName(), node);
    }

    /**
     * INTERNAL:
     */
    public void addNamedNativeQuery(MetadataNamedNativeQuery namedNativeQuery) {
        m_namedNativeQueries.put(namedNativeQuery.getName(), namedNativeQuery);
    }

    /**
     * INTERNAL:
     */
    public void addNamedQuery(MetadataNamedQuery namedQuery) {
        m_namedQueries.put(namedQuery.getName(), namedQuery);
    }

    /**
     * INTERNAL:
     */
    public void addRelationshipDescriptor(MetadataDescriptor descriptor) {
        m_descriptorsWithRelationships.add(descriptor);
    }

    /**
     * INTERNAL:
     */
    public void addSequenceGenerator(MetadataSequenceGenerator sequenceGenerator) {
        m_sequenceGenerators.put(sequenceGenerator.getName(), sequenceGenerator);
    }

    /**
     * INTERNAL:
     */
    public void addTableGenerator(MetadataTableGenerator tableGenerator) {
        m_tableGenerators.put(tableGenerator.getName(), tableGenerator);
    }
    
    /**
     * INTERNAL:
     * This method frees up resources acquired by this object.
     */
    public void cleanup() {
        // get rid of the DOM trees.
        m_mappingFiles.clear();
    }

    /**
     * INTERNAL:
     */
    public boolean containsDescriptor(Class cls) {
        return m_allDescriptors.containsKey(cls.getName());
    }

    /**
     * INTERNAL:
     */
    public boolean enableLazyForOneToOne() {
        return m_enableLazyForOneToOne;
    }
    
    /**
     * INTERNAL:
     */
    public HashMap<XMLHelper, NodeList> getDefaultListeners() {
        return m_defaultListeners;
    }

    /**
     * INTERNAL:
     */
    public MetadataDescriptor getDescriptor(Class cls) {
        if (cls == null) {
            return null;
        } else {
            MetadataDescriptor descriptor = m_allDescriptors.get(cls.getName());
            if (descriptor == null) {
                throw ValidationException.classNotListedInPersistenceUnit(cls.getName());
            } else {
                return descriptor;
            }
        }
    }
    
    /**
     * INTERNAL:
     */
    public Collection<MetadataDescriptor> getDescriptors() {
        return m_allDescriptors.values();
    }
    
    /**
     * INTERNAL:
     */
    public XMLHelper getMappedSuperclassHelper(Class cls) {
        return m_mappedSuperclasses.get(cls.getName());
    }
    
    /**
     * INTERNAL:
     */
    public Node getMappedSuperclassNode(Class cls) {
        return m_mappedSuperclassNodes.get(cls.getName());
    }
    
    /**
     * INTERNAL:
     */
    public Map<URL, Document> getMappingFiles() {
        return Collections.unmodifiableMap(m_mappingFiles);
    }
    
    /**
     * INTERNAL:
     */
    public XMLHelper getEmbeddableHelper(Class cls) {
        return m_embeddables.get(cls.getName());
    }

    /**
     * INTERNAL:
     */
    public Node getEmbeddableNode(Class cls) {
        return m_embeddableNodes.get(cls.getName());
    }
    
    /**
     * INTERNAL:
     */
    public Collection<String> getEntityNames() {
        return Collections.unmodifiableCollection(m_entityNames);
    }

    /**
     * INTERNAL:
     */
    public MetadataNamedNativeQuery getNamedNativeQuery(String name) {
        return m_namedNativeQueries.get(name);
    }
    
    /**
     * INTERNAL:
     */
    public MetadataNamedQuery getNamedQuery(String name) {
        return m_namedQueries.get(name);
    }
    
    /** 
     * INTERNAL:
     * Set the classes for processing.
     */
    public MetadataPersistenceUnit getPersistenceUnit() {
        return m_persistenceUnit;
    }
    
    /**
     * INTERNAL:
     */
    public PersistenceUnitInfo getPUInfo() {
        return m_PUInfo;
    }
    
    /**
     * INTERNAL:
     */
    public HashSet<MetadataDescriptor> getRelationshipDescriptors() {
        return m_descriptorsWithRelationships;
    }
    
    /**
     * INTERNAL:
     */
    public MetadataSequenceGenerator getSequenceGenerator(String name) {
        return m_sequenceGenerators.get(name);
    }

    /**
     * INTERNAL:
     */
    public Collection<MetadataSequenceGenerator> getSequenceGenerators() {
        return m_sequenceGenerators.values();
    }
    
    /**
     * INTERNAL:
     */
    public AbstractSession getSession() {
        return m_session;
    }
    
    /**
     * INTERNAL:
     */
    public MetadataTableGenerator getTableGenerator(String name) {
        return m_tableGenerators.get(name);
    }

    /**
     * INTERNAL:
     */
    public Collection<MetadataTableGenerator> getTableGenerators() {
        return m_tableGenerators.values();
    }
    
    /**
     * INTERNAL:
     */
    public boolean hasConflictingSequenceGenerator(MetadataSequenceGenerator sequenceGenerator) {
        if (hasSequenceGenerator(sequenceGenerator.getName())) {
            return ! getSequenceGenerator(sequenceGenerator.getName()).equals(sequenceGenerator);
        } else {
            return false;
        }
    }

    /**
     * INTERNAL:
     */
    public boolean hasConflictingTableGenerator(MetadataTableGenerator tableGenerator) {
        if (hasTableGenerator(tableGenerator.getName())) {
            return ! getTableGenerator(tableGenerator.getName()).equals(tableGenerator);
        } else {
            return false;
        }
    }
    
    /**
     * INTERNAL:
     */
    public boolean hasDescriptors() {
        return ! m_allDescriptors.isEmpty();
    }
    
    /**
     * INTERNAL:
     */
    public boolean hasMappedSuperclass(Class cls) {
        return m_mappedSuperclasses.containsKey(cls.getName());
    }
    
    /**
     * INTERNAL:
     */
    public boolean hasEmbeddable(Class cls) {
        return m_embeddables.containsKey(cls.getName());
    }

    /**
     * INTERNAL:
     */
    public boolean hasNamedNativeQuery(String name) {
        return m_namedNativeQueries.containsKey(name);
    }
    
    /**
     * INTERNAL:
     */
    public boolean hasNamedQuery(String name) {
        return m_namedQueries.containsKey(name);
    }
    
    /** 
     * INTERNAL:
     * Set the classes for processing.
     */
    public boolean hasPersistenceUnit() {
        return m_persistenceUnit != null;
    }

    /**
     * INTERNAL:
     */
    public boolean hasSequenceGenerator(String name) {
        return getSequenceGenerator(name) != null;
    }

    /**
     * INTERNAL:
     */
    public boolean hasTableGenerator(String name) {
        return getTableGenerator(name) != null;
    }

    /**
     * INTERNAL:
     * 
     * Stage 2 processing. That is, it does all the extra processing that 
     * couldn't be completed in the original metadata accessor processing.
	 */
	public void process() {
        processSequencing();
        processRelationshipDescriptors();
    }
    
    /**
     * INTERNAL:
     * Process the named native queries we found and add them to the given
     * session.
     */
    public void processNamedNativeQueries(ClassLoader loader) {
        for (MetadataNamedNativeQuery query : m_namedNativeQueries.values()) {
            HashMap<String, String> hints = processQueryHints(query.getHints(), query.getName());

            Class resultClass = query.getResultClass();

            if (resultClass != void.class) {
                resultClass = MetadataHelper.getClassForName(resultClass.getName(), loader);
                m_session.addQuery(query.getName(), EJBQueryImpl.buildSQLDatabaseQuery(resultClass, query.getEJBQLString(), hints));
            } else { 
                String resultSetMapping = query.getResultSetMapping();

                if (! resultSetMapping.equals("")) {
                    m_session.addQuery(query.getName(), EJBQueryImpl.buildSQLDatabaseQuery(resultSetMapping, query.getEJBQLString(), hints));
                } else {
                    // Neither a resultClass or resultSetMapping is specified so place in a temp query on the session
                    m_session.addQuery(query.getName(), EJBQueryImpl.buildSQLDatabaseQuery(query.getEJBQLString(), hints));
                }
            }
        }
    }

    /**
     * INTERNAL:
     * Process the named queries we found and add them to the given session.
     */
    public void processNamedQueries(MetadataValidator validator) {
        for (MetadataNamedQuery query : m_namedQueries.values()) {
            try {
                HashMap<String, String> hints = processQueryHints(query.getHints(), query.getName());
                m_session.addEjbqlPlaceHolderQuery(new EJBQLPlaceHolderQuery(query.getName(), query.getEJBQLString(), hints));
            } catch (Exception exception) {
                validator.throwErrorProcessingNamedQueryAnnotation(query.getClass(), query.getName(), exception);
            }
        }
    }
    
    /**
     * INTERNAL:
     * Process a list of MetadataQueryHint.
     */	
    protected HashMap<String, String> processQueryHints(List<MetadataQueryHint> hints, String queryName) {
        HashMap<String, String> hm = new HashMap<String, String>();

        for (MetadataQueryHint hint : hints) {
            QueryHintsHandler.verify(hint.getName(), hint.getValue(), queryName, m_session);
            hm.put(hint.getName(), hint.getValue());
        } 
		
        return hm;
    } 
    
    /**
     * INTERNAL:
     * Process the related descriptors.
     */
    protected void processRelationshipDescriptors() {
        for (MetadataDescriptor descriptor : (HashSet<MetadataDescriptor>) getRelationshipDescriptors()) {
            for (RelationshipAccessor accessor : (Collection<RelationshipAccessor>) descriptor.getRelationshipAccessors()) {
                accessor.processRelationship();
            }
        }
    }
    
    /**
     * INTERNAL:
     * Process the sequencing information.
     */
    protected void processSequencing() {
        if (! m_generatedValues.isEmpty()) {
            DatasourceLogin login = m_session.getProject().getLogin();
            
            // Generators referenced from Id should have correct type
            for (MetadataGeneratedValue generatedValue : m_generatedValues.values()) {
                String type = generatedValue.getStrategy();
                String generatorName = generatedValue.getGenerator();
                
                if (type.equals(MetadataConstants.TABLE)) {
                    MetadataSequenceGenerator sequenceGenerator = m_sequenceGenerators.get(generatorName);
                    
                    if (sequenceGenerator != null) {
                        // WIP
                    }
                } else if (type.equals(MetadataConstants.SEQUENCE) || type.equals(MetadataConstants.IDENTITY)) {
                    MetadataTableGenerator tableGenerator = m_tableGenerators.get(generatorName);
                    
                    if (tableGenerator != null) {
                        // WIP
                    }
                }
            }
    
            Sequence defaultAutoSequence = null;
            TableSequence defaultTableSequence = new TableSequence(MetadataConstants.DEFAULT_TABLE_GENERATOR);
            NativeSequence defaultObjectNativeSequence = new NativeSequence(MetadataConstants.DEFAULT_SEQUENCE_GENERATOR, false);
            NativeSequence defaultIdentityNativeSequence = new NativeSequence(MetadataConstants.DEFAULT_IDENTITY_GENERATOR, 1, true);
            
            // Sequences keyed on generator names.
            Hashtable<String, Sequence> sequences = new Hashtable<String, Sequence>();
            
            for (MetadataSequenceGenerator sequenceGenerator : m_sequenceGenerators.values()) {
                String sequenceGeneratorName = sequenceGenerator.getName();
                String seqName = (sequenceGenerator.getSequenceName().equals("")) ? sequenceGeneratorName : sequenceGenerator.getSequenceName();
                NativeSequence sequence = new NativeSequence(seqName, sequenceGenerator.getAllocationSize(), false);
                sequences.put(sequenceGeneratorName, sequence);
                
                if (sequenceGeneratorName.equals(MetadataConstants.DEFAULT_AUTO_GENERATOR)) {
                    // SequenceGenerator defined with DEFAULT_AUTO_GENERATOR.
                    // The sequence it defines will be used as a defaultSequence.
                    defaultAutoSequence = sequence;
                } else if (sequenceGeneratorName.equals(MetadataConstants.DEFAULT_SEQUENCE_GENERATOR)) {
                    // SequenceGenerator deinfed with DEFAULT_SEQUENCE_GENERATOR.
                    // All sequences of GeneratorType SEQUENCE 
                    // referencing non-defined generators will use a clone of 
                    // the sequence defined by this generator.
                    defaultObjectNativeSequence = sequence;
                }
            }

            for (MetadataTableGenerator tableGenerator : m_tableGenerators.values()) {
                String tableGeneratorName = tableGenerator.getName();
                String seqName = (tableGenerator.getPkColumnValue().equals("")) ? tableGeneratorName : tableGenerator.getPkColumnValue();
                TableSequence sequence = new TableSequence(seqName, tableGenerator.getAllocationSize(), tableGenerator.getInitialValue());
                sequences.put(tableGeneratorName, sequence);

                //bug 2647: pull schema and catalog defaults from the persistence Unit if they are not defined.  
                String catalogName = tableGenerator.getCatalog();
                String schemaName = tableGenerator.getSchema();
                if (this.getPersistenceUnit()!=null){
                    catalogName = catalogName.length()>0? catalogName: this.getPersistenceUnit().getCatalog();
                    schemaName = schemaName.length()>0? schemaName: this.getPersistenceUnit().getSchema();
                }

                // Get the database table from the @TableGenerator values.
                // In case tableGenerator.table().equals("") default sequence 
                // table name will be extracted from sequence and used, see 
                // TableSequence class.
                sequence.setTable(new DatabaseTable(MetadataHelper.getFullyQualifiedTableName(tableGenerator.getTable(), sequence.getTableName(), catalogName, schemaName)));
                
                // Process the @UniqueConstraints for this table.
                for (String[] uniqueConstraint : tableGenerator.getUniqueConstraints()) {
                    sequence.getTable().addUniqueConstraints(uniqueConstraint);
                }
                
                if (! tableGenerator.getPkColumnName().equals("")) {
                    sequence.setNameFieldName(tableGenerator.getPkColumnName());
                }
                    
                if (! tableGenerator.getValueColumnName().equals("")) {
                    sequence.setCounterFieldName(tableGenerator.getValueColumnName());
                }

                if (tableGeneratorName.equals(MetadataConstants.DEFAULT_AUTO_GENERATOR)) {
                    // TableGenerator defined with DEFAULT_AUTO_GENERATOR.
                    // The sequence it defines will be used as a defaultSequence.
                    defaultAutoSequence = sequence;
                } else if (tableGeneratorName.equals(MetadataConstants.DEFAULT_TABLE_GENERATOR)) {
                    // SequenceGenerator defined with DEFAULT_TABLE_GENERATOR. 
                    // All sequences of GenerationType TABLE referencing non-
                    // defined generators will use a clone of the sequence 
                    // defined by this generator.
                    defaultTableSequence = sequence;
                }
            }

            // Finally loop through descriptors and set sequences as required into 
            // Descriptors and Login
            boolean usesAuto = false;
            for (Class entityClass : m_generatedValues.keySet()) {
                MetadataDescriptor descriptor = m_allDescriptors.get(entityClass.getName());
                MetadataGeneratedValue generatedValue = m_generatedValues.get(entityClass);
                String generatorName = generatedValue.getGenerator();
                Sequence sequence = null;

                if (! generatorName.equals("")) {
                    sequence = sequences.get(generatorName);
                }
                
                if (sequence == null) {
                    if (generatedValue.getStrategy().equals(MetadataConstants.TABLE)) {
                        if (generatorName.equals("")) {
                            sequence = defaultTableSequence;
                        } else {
                            sequence = (Sequence)defaultTableSequence.clone();
                            sequence.setName(generatorName);
                        }
                    } else if (generatedValue.getStrategy().equals(MetadataConstants.SEQUENCE)) {
                        if (generatorName.equals("")) {
                            sequence = defaultObjectNativeSequence;
                        } else {
                            sequence = (Sequence)defaultObjectNativeSequence.clone();
                            sequence.setName(generatorName);
                        }
                    } else if (generatedValue.getStrategy().equals(MetadataConstants.IDENTITY)) {
                        if (generatorName.equals("")) {
                            sequence = defaultIdentityNativeSequence;
                        } else {
                            sequence = (Sequence)defaultIdentityNativeSequence.clone();
                            sequence.setName(generatorName);
                        }
                    }
                }

                if (sequence != null) {
                    descriptor.setSequenceNumberName(sequence.getName());
                    login.addSequence(sequence);
                } else {
                    // this must be generatedValue.getStrategy().equals(MetadataConstants.AUTO)
                    usesAuto = true;
                    String seqName;
                    if (generatorName.equals("")) {
                        if (defaultAutoSequence != null) {
                            seqName = defaultAutoSequence.getName();
                        } else {
                            seqName = MetadataConstants.DEFAULT_AUTO_GENERATOR; 
                        }
                    } else {
                        seqName = generatorName;
                    }
                    descriptor.setSequenceNumberName(seqName);
                }
            }
            if(usesAuto) {
                if (defaultAutoSequence != null) {
                    login.setDefaultSequence(defaultAutoSequence);
                }
            }
        }
    }
    
    /**
     * INTERNAL:
     */
    public void setEntityNames(Collection<String> entityNames) {
        m_entityNames.clear();
        m_entityNames.addAll(entityNames);
    }
    
    /**
     * INTERNAL:
     */
    public void setMappingFiles(Map<URL, Document> mappingFiles) {
        m_mappingFiles.clear();
        m_mappingFiles.putAll(mappingFiles);
    }

    /** 
     * INTERNAL:
     * Set the classes for processing.
     */
    public void setPersistenceUnit(MetadataPersistenceUnit persistenceUnit) {
        m_persistenceUnit = persistenceUnit;
    }
}

