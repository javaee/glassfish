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


package com.sun.persistence.api.deployment;

/**
 * This class merges a node into a tree.
 *
 * @author Servesh Singh
 * @version 1.0
 */
public interface MergeManager {

    /**
     * It merges an Entity node to Descriptor tree
     *
     * @param beanTree       descriptor tree
     * @param annotateEntity node
     */
    public EntityDescriptor mergeEntity(
            ClassDescriptor beanTree,
            EntityDescriptor annotateEntity)
            throws MergeConflictException;

    /**
     * It merges NamedQuery node to descriptor tree
     *
     * @param beanTree           descriptor tree
     * @param annotateNamedQuery named-query node
     */
    public NamedQueryDescriptor mergeNamedQuery(
            ClassDescriptor beanTree,
            NamedQueryDescriptor annotateNamedQuery)
            throws MergeConflictException;

    /**
     * It merges NamedQuery node to property tree
     *
     * @param property
     * @param annotateNamedQuery named-query    node
     */
    public NamedQueryDescriptor mergeNamedQuery(
            PropertyDescriptor property,
            NamedQueryDescriptor annotateNamedQuery)
            throws MergeConflictException;

    /**
     * It merges array of NamedQuery node to descriptor tree
     *
     * @param beanTree             descriptor tree
     * @param annotateNamedQueries array of named query node
     */
    public NamedQueryDescriptor[] mergeNamedQueries(
            ClassDescriptor beanTree,
            NamedQueryDescriptor[] annotateNamedQueries)
            throws MergeConflictException;

    /**
     * It merges array of NamedQuery node to property tree
     *
     * @param property
     * @param annotateNamedQueries array of named query node
     */
    public NamedQueryDescriptor[] mergeNamedQueries(
            PropertyDescriptor property,
            NamedQueryDescriptor[] annotateNamedQueries)
            throws MergeConflictException;

    /**
     * It merges Table node to descriptor tree
     *
     * @param beanTree       descriptor tree
     * @param annotatedTable table node
     */
    public TableDescriptor mergeTable(
            ClassDescriptor beanTree,
            TableDescriptor annotatedTable)
            throws MergeConflictException;

    /**
     * It merges SecondaryTable node to descriptor tree
     *
     * @param beanTree       descriptor tree
     * @param annotatedTable secondary-table node
     */
    public SecondaryTableDescriptor mergeSecondaryTable(
            ClassDescriptor beanTree,
            SecondaryTableDescriptor annotatedTable)
            throws MergeConflictException;

    /**
     * It merges array of SecondaryTable nodes to descriptor tree
     *
     * @param beanTree       descriptor tree
     * @param annotateTables array of secondary-table node
     */
    public SecondaryTableDescriptor[] mergeSecondaryTables(
            ClassDescriptor beanTree,
            SecondaryTableDescriptor[] annotateTables)
            throws MergeConflictException;

    /**
     * It merges a join column to descriptor tree
     *
     * @param beanTree            descriptor  tree
     * @param annotatedJoinColumn join-column node
     */
    public JoinColumnDescriptor mergeJoinColumn(
            ClassDescriptor beanTree,
            JoinColumnDescriptor annotatedJoinColumn)
            throws MergeConflictException;

    /**
     * It merges join column to property tree
     *
     * @param property
     * @param annotatedJoinColumn join-column node
     */
    public JoinColumnDescriptor mergeJoinColumn(
            PropertyDescriptor property,
            JoinColumnDescriptor annotatedJoinColumn)
            throws MergeConflictException;

    /**
     * It merges an array of join columns to descriptor tree
     *
     * @param beanTree            descriptor tree
     * @param annotateJoinColumns array of join-column node
     */
    public JoinColumnDescriptor[] mergeJoinColumns(
            ClassDescriptor beanTree,
            JoinColumnDescriptor[] annotateJoinColumns)
            throws MergeConflictException;

    /**
     * It merges an array of join columns to property tree
     *
     * @param property
     * @param annotateJoinColumns array of join-column node
     */
    public JoinColumnDescriptor[] mergeJoinColumns(
            PropertyDescriptor property,
            JoinColumnDescriptor[] annotateJoinColumns)
            throws MergeConflictException;

    /**
     * It merges Inheritance node to descriptor tree
     *
     * @param beanTree            descriptor tree
     * @param annotateInheritance inheriatnce node
     */
    public InheritanceDescriptor mergeInheritance(
            ClassDescriptor beanTree,
            InheritanceDescriptor annotateInheritance)
            throws MergeConflictException;

    /**
     * It merges an Inheritance join column node to descriptor tree
     *
     * @param beanTree                       descriptor tree
     * @param annotatedInheritanceJoinColumn inheriatnce-join-column node
     */
    public InheritanceJoinColumnDescriptor mergeInheritanceJoinColumn(
            ClassDescriptor beanTree,
            InheritanceJoinColumnDescriptor annotatedInheritanceJoinColumn)
            throws MergeConflictException;

    /**
     * It merges an array of Inheritance join column node to descriptor tree
     *
     * @param beanTree                       descriptor tree
     * @param annotateInheritanceJoinColumns array of inheriatnce-join-column
     */
    public InheritanceJoinColumnDescriptor[] mergeInheritanceJoinColumns(
            ClassDescriptor beanTree,
            InheritanceJoinColumnDescriptor[] annotateInheritanceJoinColumns)
            throws MergeConflictException;

    /**
     * It merges descriminator column node to descriptor tree
     *
     * @param beanTree                    descriptor tree
     * @param discriminatorColumnAnnotate descriminator-column node
     */
    public DiscriminatorColumnDescriptor mergeDiscriminatorColumn(
            ClassDescriptor beanTree,
            DiscriminatorColumnDescriptor discriminatorColumnAnnotate)
            throws MergeConflictException;

    /**
     * It merges embeddable node to descriptor tree
     *
     * @param beanTree   descriptor tree
     * @param embeddable embeddable node
     */
    public EmbeddableDescriptor mergeEmbeddable(
            ClassDescriptor beanTree,
            EmbeddableDescriptor embeddable)
            throws MergeConflictException;

    /**
     * It merges GeneratedId Table node to descriptor tree
     *
     * @param beanTree         tree
     * @param generatedIdTable node
     */
    public GeneratedIdTableDescriptor mergeGeneratedIdTable(
            ClassDescriptor beanTree,
            GeneratedIdTableDescriptor generatedIdTable)
            throws MergeConflictException;

    /**
     * It merges sequence generator node to descriptor tree
     *
     * @param beanTree          descriptor tree
     * @param sequenceGenerator sequence-generator node
     */
    public SequenceGeneratorDescriptor mergeSequenceGenerator(
            ClassDescriptor beanTree,
            SequenceGeneratorDescriptor sequenceGenerator)
            throws MergeConflictException;

    /**
     * It merges sequence generator node to property tree
     *
     * @param property          tree
     * @param sequenceGenerator sequence-generator node
     */
    public SequenceGeneratorDescriptor mergeSequenceGenerator(
            PropertyDescriptor property,
            SequenceGeneratorDescriptor sequenceGenerator)
            throws MergeConflictException;

    /**
     * It merges sequence generator node to descriptor tree
     *
     * @param beanTree       descriptor tree
     * @param tableGenerator table-generator node
     */
    public TableGeneratorDescriptor mergeTableGenerator(
            ClassDescriptor beanTree,
            TableGeneratorDescriptor tableGenerator)
            throws MergeConflictException;

    /**
     * It merges sequence generator node to property tree
     *
     * @param property       tree
     * @param tableGenerator table-generator node
     */
    public TableGeneratorDescriptor mergeTableGenerator(
            PropertyDescriptor property,
            TableGeneratorDescriptor tableGenerator)
            throws MergeConflictException;

    /**
     * It merges Id node to property tree
     *
     * @param property tree
     * @param id       id node
     */
    public IdDescriptor mergeId(PropertyDescriptor property, IdDescriptor id)
            throws MergeConflictException;

    /**
     * It merges embeddedId node to property tree
     *
     * @param property tree
     * @param id       embedded-id node
     */
    public EmbeddedIdDescriptor mergeEmbeddedId(
            PropertyDescriptor property,
            EmbeddedIdDescriptor id)
            throws MergeConflictException;

    /**
     * It merges embedded node to property tree
     *
     * @param property tree
     * @param embedded embedded node
     */
    public EmbeddedDescriptor mergeEmbedded(
            PropertyDescriptor property,
            EmbeddedDescriptor embedded)
            throws MergeConflictException;

    /**
     * It merges column node to property tree
     *
     * @param property tree
     * @param col      column node
     */
    public ColumnDescriptor mergeColumn(
            PropertyDescriptor property,
            ColumnDescriptor col)
            throws MergeConflictException;

    /**
     * It merges association node to property tree
     *
     * @param property    tree
     * @param associtaion associtaion node
     */
    public AssociationTableDescriptor mergeAssociationTable(
            PropertyDescriptor property,
            AssociationTableDescriptor associtaion)
            throws MergeConflictException;

    /**
     * It creates a new property if that property is absent
     *
     * @param entityBeanClass descriptor tree
     * @param propertyNode    property to be merged
     */
    public PropertyDescriptor mergeProperty(
            ClassDescriptor entityBeanClass,
            PropertyDescriptor propertyNode)
            throws MergeConflictException;

    /**
     * Merge a mapping node into this property.
     *
     * @param propertyNode
     * @param mappingNode
     * @throws MergeConflictException
     */
    public MappingDescriptor mergeMapping(
            PropertyDescriptor propertyNode,
            MappingDescriptor mappingNode)
            throws MergeConflictException;
}