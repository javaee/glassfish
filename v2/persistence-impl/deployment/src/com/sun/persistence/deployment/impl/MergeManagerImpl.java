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


package com.sun.persistence.deployment.impl;

import com.sun.persistence.api.deployment.*;

import java.util.List;

/**
 * This class merges a node into a tree.
 *
 * @author Servesh Singh
 * @version 1.0
 */
public class MergeManagerImpl implements MergeManager {

    private MergeConflictResolver verifier;

    public MergeManagerImpl(MergeConflictResolver verifier) {
        this.verifier = verifier;
    }

    /**
     * It merges an Entity node to Descriptor tree
     *
     * @param beanTree       descriptor tree
     * @param annotateEntity node
     */
    public EntityDescriptor mergeEntity(
            ClassDescriptor beanTree,
            EntityDescriptor annotateEntity)
            throws MergeConflictException {
        verifier.verifyEntity(beanTree, annotateEntity);
        if (annotateEntity != null)
            beanTree.setEntity(annotateEntity);
        return beanTree.getEntity();
    }

    /**
     * It merges NamedQuery node to descriptor tree. It will be a Union Merge
     * because if user gives @NamedQuery and @NamedQueries together then we
     * don't know whether beanTree is populated from XML or from annotation.
     *
     * @param beanTree           descriptor tree
     * @param annotateNamedQuery named-query node
     */
    public NamedQueryDescriptor mergeNamedQuery(
            ClassDescriptor beanTree,
            NamedQueryDescriptor annotateNamedQuery)
            throws MergeConflictException {
        verifier.verifyNamedQuery(beanTree, annotateNamedQuery);
        for (NamedQueryDescriptor nquery : beanTree.getNamedQuery()) {
            if (nquery.getName().equals(annotateNamedQuery.getName())) {
                return nquery;
            }
        }
        beanTree.getNamedQuery().add(annotateNamedQuery);
        return annotateNamedQuery;
    }

    /**
     * It merges NamedQuery node to property tree. It will be a Union Merge
     *
     * @param property
     * @param annotateNamedQuery named-query node
     */
    public NamedQueryDescriptor mergeNamedQuery(
            PropertyDescriptor property,
            NamedQueryDescriptor annotateNamedQuery)
            throws MergeConflictException {
        verifier.verifyNamedQuery(property, annotateNamedQuery);
        for (NamedQueryDescriptor nquery : property.getNamedQuery()) {
            if (nquery.getName().equals(annotateNamedQuery.getName())) {
                return nquery;
            }
        }
        property.getNamedQuery().add(annotateNamedQuery);
        return annotateNamedQuery;
    }

    /**
     * It merges array of NamedQuery node to descriptor tree
     *
     * @param beanTree             descriptor tree
     * @param annotateNamedQueries array of named query node
     */
    public NamedQueryDescriptor[] mergeNamedQueries(
            ClassDescriptor beanTree,
            NamedQueryDescriptor[] annotateNamedQueries)
            throws MergeConflictException {
        verifier.verifyNamedQueries(beanTree, annotateNamedQueries);
        NamedQueryDescriptor[] result = new NamedQueryDescriptor[annotateNamedQueries.length];
        for (int i = 0; i < annotateNamedQueries.length; i++) {
            result[i] = mergeNamedQuery(beanTree, annotateNamedQueries[i]);
        }
        return result;
    }

    /**
     * It merges array of NamedQuery node to property tree
     *
     * @param property
     * @param annotateNamedQueries array of named query node
     */
    public NamedQueryDescriptor[] mergeNamedQueries(
            PropertyDescriptor property,
            NamedQueryDescriptor[] annotateNamedQueries)
            throws MergeConflictException {
        verifier.verifyNamedQueries(property, annotateNamedQueries);
        NamedQueryDescriptor[] result = new NamedQueryDescriptor[annotateNamedQueries.length];
        for (int i = 0; i < annotateNamedQueries.length; i++) {
            result[i] = mergeNamedQuery(property, annotateNamedQueries[i]);
        }
        return result;
    }

    /**
     * It merges Table node to descriptor tree It ignores the table specified by
     * annotation
     *
     * @param beanTree       descriptor tree
     * @param annotatedTable table node
     */
    public TableDescriptor mergeTable(
            ClassDescriptor beanTree,
            TableDescriptor annotatedTable)
            throws MergeConflictException {
        verifier.verifyTable(beanTree, annotatedTable);
        TableDescriptor table = beanTree.getTable();
        if (table == null) {
            beanTree.setTable(annotatedTable);
        }
        return beanTree.getTable();
    }

    /**
     * It merges SecondaryTable node to descriptor tree It ignores the
     * SecondaryTables mentioned in annotation
     *
     * @param beanTree       descriptor tree
     * @param annotatedTable secondary-table node
     */
    public SecondaryTableDescriptor mergeSecondaryTable(
            ClassDescriptor beanTree,
            SecondaryTableDescriptor annotatedTable)
            throws MergeConflictException {
        verifier.verifySecondaryTable(beanTree, annotatedTable);
        List tables = beanTree.getSecondaryTable();
        if (tables.size() == 0 && annotatedTable != null) {
            beanTree.getSecondaryTable().add(annotatedTable);
            return annotatedTable;
        }
        return null; //TODO check if this will cause any problem
    }

    /**
     * It merges array of SecondaryTable nodes to descriptor tree
     *
     * @param beanTree       descriptor tree
     * @param annotateTables array of secondary-table node
     */
    public SecondaryTableDescriptor[] mergeSecondaryTables(
            ClassDescriptor beanTree,
            SecondaryTableDescriptor[] annotateTables)
            throws MergeConflictException {
        verifier.verifySecondaryTables(beanTree, annotateTables);
        List tables = beanTree.getSecondaryTable();
        if (tables.size() == 0) {
            for (int i = 0; i < annotateTables.length; i++) {
                beanTree.getSecondaryTable().add(annotateTables[i]);
            }
            return annotateTables;
        }
        return null; //TODO check if this will cause any problem
    }

    /**
     * It merges a join column to descriptor tree
     *
     * @param beanTree            descriptor  tree
     * @param annotatedJoinColumn join-column node
     */
    public JoinColumnDescriptor mergeJoinColumn(
            ClassDescriptor beanTree,
            JoinColumnDescriptor annotatedJoinColumn)
            throws MergeConflictException {
        verifier.verifyJoinColumn(beanTree, annotatedJoinColumn);
        List joinColumns = beanTree.getJoinColumn();
        if (joinColumns.size() == 0 && annotatedJoinColumn != null) {
            beanTree.getJoinColumn().add(annotatedJoinColumn);
            return annotatedJoinColumn;
        }
        return null; //TODO check if this will cause any problem
    }

    /**
     * It merges join column to property tree It ignores the join column
     * specified in annotation
     *
     * @param property
     * @param annotatedJoinColumn join-column node
     */
    public JoinColumnDescriptor mergeJoinColumn(
            PropertyDescriptor property,
            JoinColumnDescriptor annotatedJoinColumn)
            throws MergeConflictException {
        verifier.verifyJoinColumn(property, annotatedJoinColumn);
        List joinColumns = property.getJoinColumn();
        if (joinColumns.size() == 0 && annotatedJoinColumn != null) {
            property.getJoinColumn().add(annotatedJoinColumn);
            return annotatedJoinColumn;
        }
        return null; //TODO check if this will cause any problem
    }

    /**
     * It merges an array of join columns to descriptor tree It ignores the join
     * column specified in annotation
     *
     * @param beanTree            descriptor tree
     * @param annotateJoinColumns array of join-column node
     */
    public JoinColumnDescriptor[] mergeJoinColumns(
            ClassDescriptor beanTree,
            JoinColumnDescriptor[] annotateJoinColumns)
            throws MergeConflictException {
        verifier.verifyJoinColumns(beanTree, annotateJoinColumns);
        List joinColumns = beanTree.getJoinColumn();
        if (joinColumns.size() == 0) {
            for (int i = 0; i < annotateJoinColumns.length; i++) {
                beanTree.getJoinColumn().add(annotateJoinColumns[i]);
            }
            return annotateJoinColumns;
        }
        return null; //TODO check if this will cause any problem
    }

    /**
     * It merges an array of join columns to property tree It ignores the join
     * column specified in annotation
     *
     * @param property
     * @param annotateJoinColumns array of join-column node
     */
    public JoinColumnDescriptor[] mergeJoinColumns(
            PropertyDescriptor property,
            JoinColumnDescriptor[] annotateJoinColumns)
            throws MergeConflictException {
        verifier.verifyJoinColumns(property, annotateJoinColumns);
        List joinColumns = property.getJoinColumn();
        if (joinColumns.size() == 0) {
            for (int i = 0; i < annotateJoinColumns.length; i++) {
                property.getJoinColumn().add(annotateJoinColumns[i]);
            }
            return annotateJoinColumns;
        }
        return null; //TODO check if this will cause any problem
    }

    /**
     * It merges Inheritance node to descriptor tree Inheritance annotation
     * informatiion is ignored.
     *
     * @param beanTree            descriptor  tree
     * @param annotateInheritance inheriatnce node
     */
    public InheritanceDescriptor mergeInheritance(
            ClassDescriptor beanTree,
            InheritanceDescriptor annotateInheritance)
            throws MergeConflictException {
        verifier.verifyInheritance(beanTree, annotateInheritance);
        InheritanceDescriptor inheritance = beanTree.getInheritance();
        if (inheritance == null) {
            beanTree.setInheritance(annotateInheritance);
        }
        return beanTree.getInheritance();
    }

    /**
     * It merges an Inheritance join column node to descriptor tree It ignores
     * the annotation information of inheritance join column
     *
     * @param beanTree                       descriptor tree
     * @param annotatedInheritanceJoinColumn inheriatnce-join-column node
     */
    public InheritanceJoinColumnDescriptor mergeInheritanceJoinColumn(
            ClassDescriptor beanTree,
            InheritanceJoinColumnDescriptor annotatedInheritanceJoinColumn)
            throws MergeConflictException {
        verifier.verifyInheritanceJoinColumn(beanTree,
                annotatedInheritanceJoinColumn);
        List inheritanceColumns = beanTree.getInheritanceJoinColumn();
        if (inheritanceColumns.size() == 0 &&
                annotatedInheritanceJoinColumn != null) {
            beanTree.getInheritanceJoinColumn().add(
                    annotatedInheritanceJoinColumn);
            return annotatedInheritanceJoinColumn;
        }
        return null; //TODO check if this will cause any problem
    }

    /**
     * It merges an array of Inheritance join column node to descriptor tree It
     * ignores the annotation information of inheritance join column
     *
     * @param beanTree                       descriptor tree
     * @param annotateInheritanceJoinColumns array of inheriatnce-join-column
     */
    public InheritanceJoinColumnDescriptor[] mergeInheritanceJoinColumns(
            ClassDescriptor beanTree,
            InheritanceJoinColumnDescriptor[] annotateInheritanceJoinColumns)
            throws MergeConflictException {
        verifier.verifyInheritanceJoinColumns(beanTree,
                annotateInheritanceJoinColumns);
        if ((beanTree.getInheritanceJoinColumn().size() == 0) &&
                (annotateInheritanceJoinColumns.length > 0)) {
            for (int i = 0; i < annotateInheritanceJoinColumns.length; i++) {
                beanTree.getInheritanceJoinColumn().add(
                        annotateInheritanceJoinColumns[i]);
            }
            return annotateInheritanceJoinColumns;
        }
        return null; //TODO check if this will cause any problem
    }

    /**
     * It merges descriminator column node to descriptor tree Ignores the
     * annotation information if XML contains such information
     *
     * @param beanTree                    descriptor tree
     * @param discriminatorColumnAnnotate descriminator-column node
     */
    public DiscriminatorColumnDescriptor mergeDiscriminatorColumn(
            ClassDescriptor beanTree,
            DiscriminatorColumnDescriptor discriminatorColumnAnnotate)
            throws MergeConflictException {
        verifier.verifyDiscriminatorColumn(beanTree,
                discriminatorColumnAnnotate);
        DiscriminatorColumnDescriptor discriminatorColumn =
                beanTree.getDiscriminatorColumn();
        if (discriminatorColumn == null) {
            beanTree.setDiscriminatorColumn(discriminatorColumnAnnotate);
        }
        return beanTree.getDiscriminatorColumn();
    }

    /**
     * It merges embeddable node to descriptor tree It ignores the XML
     * information
     *
     * @param beanTree   descriptor tree
     * @param embeddable embeddable node
     */
    public EmbeddableDescriptor mergeEmbeddable(
            ClassDescriptor beanTree,
            EmbeddableDescriptor embeddable)
            throws MergeConflictException {
        verifier.verifyEmbeddable(beanTree, embeddable);
        if (embeddable != null)
            beanTree.setEmbeddable(embeddable);
        return beanTree.getEmbeddable();
    }

    /**
     * It merges GeneratedId Table node to descriptor tree It ignores the
     * annotation info if XML contains such info
     *
     * @param beanTree         descriptor tree
     * @param generatedIdTable generated-id-table node
     */
    public GeneratedIdTableDescriptor mergeGeneratedIdTable(
            ClassDescriptor beanTree,
            GeneratedIdTableDescriptor generatedIdTable)
            throws MergeConflictException {
        verifier.verifyGeneratedIdTable(beanTree, generatedIdTable);
        GeneratedIdTableDescriptor GeneratedIdTableNode =
                beanTree.getGeneratedIdTable();
        if (GeneratedIdTableNode == null) {
            beanTree.setGeneratedIdTable(generatedIdTable);
        }
        return beanTree.getGeneratedIdTable();
    }

    /**
     * It merges sequence generator node to descriptor tree It ignores the
     * annotation info if XML contains such info
     *
     * @param beanTree          descriptor tree
     * @param sequenceGenerator sequence-generator node
     */
    public SequenceGeneratorDescriptor mergeSequenceGenerator(
            ClassDescriptor beanTree,
            SequenceGeneratorDescriptor sequenceGenerator)
            throws MergeConflictException {
        verifier.verifySequenceGenerator(beanTree, sequenceGenerator);
        SequenceGeneratorDescriptor sequenceGeneratorNode =
                beanTree.getSequenceGenerator();
        if (sequenceGeneratorNode == null) {
            beanTree.setSequenceGenerator(sequenceGenerator);
        }
        return beanTree.getSequenceGenerator();
    }

    /**
     * It merges sequence generator node to property tree It ignores the
     * annotation info if XML contains such info
     *
     * @param property          tree
     * @param sequenceGenerator sequence-generator node
     */
    public SequenceGeneratorDescriptor mergeSequenceGenerator(
            PropertyDescriptor property,
            SequenceGeneratorDescriptor sequenceGenerator)
            throws MergeConflictException {
        verifier.verifySequenceGenerator(property, sequenceGenerator);
        if (property.getSequenceGenerator() == null) {
            property.setSequenceGenerator(sequenceGenerator);
        }
        return property.getSequenceGenerator();
    }

    /**
     * It merges sequence generator node to descriptor tree It ignores the
     * annotation info if XML contains such info
     *
     * @param beanTree       descriptor tree
     * @param tableGenerator table-generator node
     */
    public TableGeneratorDescriptor mergeTableGenerator(
            ClassDescriptor beanTree,
            TableGeneratorDescriptor tableGenerator)
            throws MergeConflictException {
        verifier.verifyTableGenerator(beanTree, tableGenerator);
        TableGeneratorDescriptor tableGeneratorNode =
                beanTree.getTableGenerator();
        if (tableGeneratorNode == null) {
            beanTree.setTableGenerator(tableGenerator);
        }
        return beanTree.getTableGenerator();
    }

    /**
     * It merges sequence generator node to property tree It ignores the
     * annotation info if XML contains such info
     *
     * @param property       tree
     * @param tableGenerator table-generator node
     */
    public TableGeneratorDescriptor mergeTableGenerator(
            PropertyDescriptor property,
            TableGeneratorDescriptor tableGenerator)
            throws MergeConflictException {
        verifier.verifyTableGenerator(property, tableGenerator);
        if (property.getTableGenerator() == null) {
            property.setTableGenerator(tableGenerator);
        }
        return property.getTableGenerator();
    }

    /**
     * It merges Id node to property tree It ignores the Id specified in
     * annotation
     *
     * @param property tree
     * @param id       node
     */
    public IdDescriptor mergeId(PropertyDescriptor property, IdDescriptor id)
            throws MergeConflictException {
        verifier.verifyId(property, id);
        if (property.getId() == null)
            property.setId(id);
        return property.getId();
    }

    /**
     * It merges embeddedId node to property tree It ignores the embeddedId
     * specified in XML
     *
     * @param property tree
     * @param id       embedded-id node
     */
    public EmbeddedIdDescriptor mergeEmbeddedId(
            PropertyDescriptor property,
            EmbeddedIdDescriptor id)
            throws MergeConflictException {
        verifier.verifyEmbeddedId(property, id);
        if (id != null)
            property.setEmbeddedId(id);
        return property.getEmbeddedId();
    }

    /**
     * It merges embedded node to property tree It ignores the embeddedObject
     * specified in XML
     *
     * @param property tree
     * @param embedded embedded node
     */
    public EmbeddedDescriptor mergeEmbedded(
            PropertyDescriptor property,
            EmbeddedDescriptor embedded)
            throws MergeConflictException {
        verifier.verifyEmbedded(property, embedded);
        if (embedded != null)
            property.setEmbedded(embedded);
        return property.getEmbedded();
    }

    /**
     * It merges column node to property tree It ignores the column specified in
     * XML
     *
     * @param property tree
     * @param col      column   node
     */
    public ColumnDescriptor mergeColumn(
            PropertyDescriptor property,
            ColumnDescriptor col)
            throws MergeConflictException {
        verifier.verifyColumn(property, col);
        if (col != null)
            property.setColumn(col);
        return property.getColumn();
    }

    /**
     * It merges associtaion node to property tree
     *
     * @param property    tree
     * @param association associtaion node
     */
    public AssociationTableDescriptor mergeAssociationTable(
            PropertyDescriptor property,
            AssociationTableDescriptor association)
            throws MergeConflictException {
        verifier.verifyAssociationTable(property, association);
        if (property.getAssociationTable() == null) {
            property.setAssociationTable(association);
        }
        return property.getAssociationTable();
    }

    /**
     * It creates a new property if that property is absent
     *
     * @param entityBeanClass descriptor tree
     * @param property        property to be merged.
     */
    public PropertyDescriptor mergeProperty(
            ClassDescriptor entityBeanClass,
            PropertyDescriptor property)
            throws MergeConflictException {
        verifier.verifyProperty(entityBeanClass, property);
        PropertyDescriptor propertyInXML = entityBeanClass.getProperty(
                property.getName());
        if (propertyInXML == null) {
            entityBeanClass.getProperty().add(property);
            return property;
        }
        return entityBeanClass.getProperty(property.getName());
    }

    public MappingDescriptor mergeMapping(
            PropertyDescriptor propertyNode,
            MappingDescriptor mappingNode)
            throws MergeConflictException {
        verifier.verifyMapping(propertyNode, mappingNode);
        if (propertyNode.getMapping() == null) { // only when the property is newly created
            propertyNode.setMapping(mappingNode);
        }
        return propertyNode.getMapping();
    }

}
