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
 * This interface defines the contract between a conflict resolver and {@link
 * MergeManager}. For every merge method in MergeManager, there is a
 * corresponding verify method.
 *
 * @author Servesh Singh
 * @version 1.0
 */
public interface MergeConflictResolver {

    /**
     * Enity annotation can not be overriden by XML
     *
     * @param beanTree       descriptor tree
     * @param annotateEntity entity node
     */
    public boolean verifyEntity(
            ClassDescriptor beanTree,
            EntityDescriptor annotateEntity)
            throws MergeConflictException;

    /**
     * It Verifies the following things a) name has to be specified in named
     * query at class level b) QueryString of named query can not be overriden
     * by XML c) ResultType of named query can not be overriden by XML d)
     * EjbInterfaceType of named query can not be overriden by XML
     *
     * @param beanTree           descriptor tree
     * @param annotateNamedQuery named-query node
     */
    public boolean verifyNamedQuery(
            ClassDescriptor beanTree,
            NamedQueryDescriptor annotateNamedQuery)
            throws MergeConflictException;

    /**
     * It Verifies the following things a) QueryString of named query can not be
     * overriden by XML b) ResultType of named query can not be overriden by XML
     * c) EjbInterfaceType of named query can not be overriden by XML
     *
     * @param property           tree
     * @param annotateNamedQuery named-query node
     */
    public boolean verifyNamedQuery(
            PropertyDescriptor property,
            NamedQueryDescriptor annotateNamedQuery)
            throws MergeConflictException;

    /**
     * It Verifies the following things a) name has to be specified in named
     * query array b) QueryString of named query can not be overriden by XML c)
     * ResultType of named query can not be overriden by XML d) EjbInterfaceType
     * of named query can not be overriden by XML
     *
     * @param beanTree             descriptor tree
     * @param annotateNamedQueries named-query node array
     */
    public boolean verifyNamedQueries(
            ClassDescriptor beanTree,
            NamedQueryDescriptor[] annotateNamedQueries)
            throws MergeConflictException;

    /**
     * It Verifies the following things a) name has to be specified in named
     * query array b) QueryString of named query can not be overriden by XML c)
     * ResultType of named query can not be overriden by XML d) EjbInterfaceType
     * of named query can not be overriden by XML
     *
     * @param property             tree
     * @param annotateNamedQueries named-query node array
     */
    public boolean verifyNamedQueries(
            PropertyDescriptor property,
            NamedQueryDescriptor[] annotateNamedQueries)
            throws MergeConflictException;

    /**
     * It verifies the table descriptor
     *
     * @param beanTree       descriptor tree
     * @param annotatedTable table node
     */
    public boolean verifyTable(
            ClassDescriptor beanTree,
            TableDescriptor annotatedTable)
            throws MergeConflictException;

    /**
     * It verifies the secondary table descriptor
     *
     * @param beanTree       descriptor tree
     * @param annotatedTable secondary-table node
     */
    public boolean verifySecondaryTable(
            ClassDescriptor beanTree,
            SecondaryTableDescriptor annotatedTable)
            throws MergeConflictException;

    /**
     * It verifies the secondary tables descriptor
     *
     * @param beanTree       descriptor tree
     * @param annotateTables secondary-table node array
     */
    public boolean verifySecondaryTables(
            ClassDescriptor beanTree,
            SecondaryTableDescriptor[] annotateTables)
            throws MergeConflictException;

    /**
     * JoinColumn name must be specified at class level
     *
     * @param beanTree            descriptor tree
     * @param annotatedJoinColumn node
     */
    public boolean verifyJoinColumn(
            ClassDescriptor beanTree,
            JoinColumnDescriptor annotatedJoinColumn)
            throws MergeConflictException;

    /**
     * It verifies the join column tables descriptor
     *
     * @param property            tree
     * @param annotatedJoinColumn join-column node
     */
    public boolean verifyJoinColumn(
            PropertyDescriptor property,
            JoinColumnDescriptor annotatedJoinColumn)
            throws MergeConflictException;

    /**
     * JoinColumn name must be specified
     *
     * @param beanTree            descriptor tree
     * @param annotateJoinColumns join-column node array
     */
    public boolean verifyJoinColumns(
            ClassDescriptor beanTree,
            JoinColumnDescriptor[] annotateJoinColumns)
            throws MergeConflictException;

    /**
     * JoinColumn name must be specified
     *
     * @param property            tree
     * @param annotateJoinColumns join-column node array
     */
    public boolean verifyJoinColumns(
            PropertyDescriptor property,
            JoinColumnDescriptor[] annotateJoinColumns)
            throws MergeConflictException;

    /**
     * It verifies the inheritance descriptor
     *
     * @param beanTree            descriptor tree
     * @param annotateInheritance inheritance node
     */
    public boolean verifyInheritance(
            ClassDescriptor beanTree,
            InheritanceDescriptor annotateInheritance)
            throws MergeConflictException;

    /**
     * It verifies the inheritance join column descriptor
     *
     * @param beanTree                       descriptor tree
     * @param annotatedInheritanceJoinColumn inheritance-join-column node
     */
    public boolean verifyInheritanceJoinColumn(
            ClassDescriptor beanTree,
            InheritanceJoinColumnDescriptor annotatedInheritanceJoinColumn)
            throws MergeConflictException;

    /**
     * InheritanceJoinColumn name must be specified
     *
     * @param beanTree                       descriptor tree
     * @param annotateInheritanceJoinColumns inheritance-join-column node array
     */
    public boolean verifyInheritanceJoinColumns(
            ClassDescriptor beanTree,
            InheritanceJoinColumnDescriptor[] annotateInheritanceJoinColumns)
            throws MergeConflictException;

    /**
     * It verifies the discriminator column descriptor
     *
     * @param beanTree                    descriptor tree
     * @param discriminatorColumnAnnotate discriminator-column node
     */
    public boolean verifyDiscriminatorColumn(
            ClassDescriptor beanTree,
            DiscriminatorColumnDescriptor discriminatorColumnAnnotate)
            throws MergeConflictException;

    /**
     * Embeddable annotation can not be overriden by XML
     *
     * @param beanTree   descriptor tree
     * @param embeddable embeddable node
     */
    public boolean verifyEmbeddable(
            ClassDescriptor beanTree,
            EmbeddableDescriptor embeddable)
            throws MergeConflictException;

    /**
     * It verifies the GeneratedIdTable descriptor
     *
     * @param beanTree         descriptor tree
     * @param generatedIdTable generated-id-column node
     */
    public boolean verifyGeneratedIdTable(
            ClassDescriptor beanTree,
            GeneratedIdTableDescriptor generatedIdTable)
            throws MergeConflictException;

    /**
     * It verifies the sequenceGenerator descriptor
     *
     * @param beanTree          tree
     * @param sequenceGenerator node
     */
    public boolean verifySequenceGenerator(
            ClassDescriptor beanTree,
            SequenceGeneratorDescriptor sequenceGenerator)
            throws MergeConflictException;

    /**
     * It verifies the sequenceGenerator descriptor
     *
     * @param property          tree
     * @param sequenceGenerator node
     */
    public boolean verifySequenceGenerator(
            PropertyDescriptor property,
            SequenceGeneratorDescriptor sequenceGenerator)
            throws MergeConflictException;

    /**
     * It verifies the tableGenerator descriptor
     *
     * @param beanTree       descriptor tree
     * @param tableGenerator table-generator node
     */
    public boolean verifyTableGenerator(
            ClassDescriptor beanTree,
            TableGeneratorDescriptor tableGenerator)
            throws MergeConflictException;

    /**
     * It verifies the tableGenerator descriptor
     *
     * @param property       tree
     * @param tableGenerator table-generator node
     */
    public boolean verifyTableGenerator(
            PropertyDescriptor property,
            TableGeneratorDescriptor tableGenerator)
            throws MergeConflictException;

    /**
     * It verifies the Id descriptor
     *
     * @param property tree
     * @param id       id node
     */
    public boolean verifyId(PropertyDescriptor property, IdDescriptor id)
            throws MergeConflictException;

    /**
     * It verifies the EmbeddedId descriptor
     *
     * @param property tree
     * @param id       embedded-id node
     */
    public boolean verifyEmbeddedId(
            PropertyDescriptor property,
            EmbeddedIdDescriptor id)
            throws MergeConflictException;

    /**
     * It verifies the Embedded descriptor
     *
     * @param property tree
     * @param embedded embedded node
     */
    public boolean verifyEmbedded(
            PropertyDescriptor property,
            EmbeddedDescriptor embedded)
            throws MergeConflictException;

    /**
     * It verifies the Column descriptor
     *
     * @param property tree
     * @param col      column node
     */
    public boolean verifyColumn(
            PropertyDescriptor property,
            ColumnDescriptor col)
            throws MergeConflictException;

    /**
     * It verifies the Association Table descriptor
     *
     * @param property    tree
     * @param associtaion association-table node
     */
    public boolean verifyAssociationTable(
            PropertyDescriptor property,
            AssociationTableDescriptor associtaion)
            throws MergeConflictException;

    public boolean verifyProperty(
            ClassDescriptor entityBeanClass,
            PropertyDescriptor property)
            throws MergeConflictException;

    /**
     * Basic mapping can not be overriden by XML
     *
     * @param property tree
     * @param basic    mapping node
     */
    public boolean verifyBasicMapping(
            PropertyDescriptor property,
            BasicDescriptor basic)
            throws MergeConflictException;

    /**
     * Serialized mapping can not be overriden by XML
     *
     * @param property   tree
     * @param serialized mapping node
     */
    public boolean verifySerializeMapping(
            PropertyDescriptor property,
            SerializedDescriptor serialized)
            throws MergeConflictException;

    /**
     * Lob mapping can not be overriden by XML
     *
     * @param property tree
     * @param lob      mapping node
     */
    public boolean verifyLobMapping(
            PropertyDescriptor property,
            LobDescriptor lob)
            throws MergeConflictException;

    /**
     * oneToOne mapping can not be overriden by XML
     *
     * @param property tree
     * @param oneToOne node
     */
    public boolean verifyOneToOneMapping(
            PropertyDescriptor property,
            OneToOneDescriptor oneToOne)
            throws MergeConflictException;

    /**
     * oneToMany mapping can not be overriden by XML
     *
     * @param property  tree
     * @param oneToMany mapping node
     */
    public boolean verifyOneToManyMapping(
            PropertyDescriptor property,
            OneToManyDescriptor oneToMany)
            throws MergeConflictException;

    /**
     * manyToOne mapping can not be overriden by XML
     *
     * @param property  tree
     * @param manyToOne mapping node
     */
    public boolean verifyManyToOneMapping(
            PropertyDescriptor property,
            ManyToOneDescriptor manyToOne)
            throws MergeConflictException;

    /**
     * manyToMany mapping can not be overriden by XML
     *
     * @param property   tree
     * @param manyToMany node
     */
    public boolean verifyManyToManyMapping(
            PropertyDescriptor property,
            ManyToManyDescriptor manyToMany)
            throws MergeConflictException;

    public boolean verifyMapping(
            PropertyDescriptor propertyNode,
            MappingDescriptor mappingNode)
            throws MergeConflictException;
}