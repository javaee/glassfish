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

import com.sun.org.apache.jdo.util.I18NHelper;
import com.sun.persistence.api.deployment.*;

import java.util.Iterator;
import java.util.List;

/**
 * This class provides an implementation of {@link MergeConflictResolver}.
 * This class needs to be changed as the spec defines the rules for merging.
 *
 * @author Servesh Singh
 * @version 1.0
 */
public class MergeConflictResolverImpl implements MergeConflictResolver {

    /**
     * I18N message handler
     */
    private final static I18NHelper i18NHelper = I18NHelper.getInstance(
            LogHelperDeployment.class);

    public MergeConflictResolverImpl() {
    }

    /**
     * Enity annotation can not be overriden by XML
     *
     * @param beanTree       descriptor tree
     * @param annotateEntity entity node
     */
    public boolean verifyEntity(
            ClassDescriptor beanTree,
            EntityDescriptor annotateEntity) throws MergeConflictException {
        LogHelperDeployment.getLogger().fine(
                i18NHelper.msg("MSG_EnteringVerifyEntityMethod")); // NOI18N
        EntityDescriptor entity = beanTree.getEntity();
        if (annotateEntity != null && entity != null) {
            if (annotateEntity.getName() != null && entity.getName() == null) {
                throw new MergeConflictException(
                        i18NHelper.msg("EXC_EntityNameNotOverriden")); // NOI18N
            } else if (entity.getName() != null
                    && annotateEntity.getName() != null
                    && !entity.getName().equals((annotateEntity.getName()))) {
                throw new MergeConflictException(
                        i18NHelper.msg("EXC_EntityNameNotOverriden")); // NOI18N
            }
            if (annotateEntity.getAccess() != null &&
                    entity.getAccess() == null) {
                throw new MergeConflictException(i18NHelper.msg(
                        "EXC_EntityAccessNotOverriden")); // NOI18N
            } else if (entity.getAccess() != null
                    && annotateEntity.getAccess() != null
                    && !entity.getAccess().equals(annotateEntity.getAccess())) {
                throw new MergeConflictException(i18NHelper.msg(
                        "EXC_EntityAccessNotOverriden")); // NOI18N
            }
            if (annotateEntity.getEntityType() != null &&
                    entity.getEntityType() == null) {
                throw new MergeConflictException(i18NHelper.msg(
                        "EXC_EntityTypeNotOverriden")); // NOI18N
            } else if (entity.getEntityType() != null
                    && annotateEntity.getEntityType() != null
                    &&
                    !entity.getEntityType().equals(
                            annotateEntity.getEntityType())) {
                throw new MergeConflictException(i18NHelper.msg(
                        "EXC_EntityTypeNotOverriden")); // NOI18N
            }
            if (entity.getVersion().intValue() !=
                    annotateEntity.getVersion().intValue()) {
                throw new MergeConflictException(i18NHelper.msg(
                        "EXC_EntityVersionNotOverriden")); // NOI18N
            }
        }
        return true;
    }

    /**
     * It verifies the following things a) name has to be specified in named
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
            throws MergeConflictException {
        if (annotateNamedQuery.getName() == null ||
                annotateNamedQuery.getName().equals(""))
            throw new MergeConflictException(i18NHelper.msg(
                    "EXC_NameNeededNamedQueryAtClassLevel")); // NOI18N
        List namedQuery = beanTree.getNamedQuery();
        Iterator itr = namedQuery.iterator();
        NamedQueryDescriptor treeQuery = null;
        while (itr.hasNext()) {
            NamedQueryDescriptor query = (NamedQueryDescriptor) itr.next();
            if (query.getName() == null || query.getName().equals(""))
                throw new MergeConflictException(i18NHelper.msg(
                        "EXC_NameNeededNamedQueryAtClassLevel")); // NOI18N
            if (query.getName() != null &&
                    query.getName().equals(annotateNamedQuery.getName()))
                treeQuery = query;
        }
        if (treeQuery != null)
            matchNamedQuery(treeQuery, annotateNamedQuery);
        return true;
    }

    private void matchNamedQuery(
            NamedQueryDescriptor treeQuery,
            NamedQueryDescriptor annotateNamedQuery)
            throws MergeConflictException {
        if (annotateNamedQuery.getQueryString() != null &&
                treeQuery.getQueryString() == null) {
            throw new MergeConflictException(i18NHelper.msg(
                    "EXC_QueryStringNotNamedQuery", // NOI18N
                    annotateNamedQuery.getName()));
        } else if (treeQuery.getQueryString() != null
                && annotateNamedQuery.getQueryString() != null
                &&
                !annotateNamedQuery.getQueryString().equals(
                        treeQuery.getQueryString())) {
            throw new MergeConflictException(i18NHelper.msg(
                    "EXC_QueryStringNotNamedQuery", // NOI18N
                    annotateNamedQuery.getName()));
        }
        if (annotateNamedQuery.getResultType() != null &&
                treeQuery.getResultType() == null) {
            throw new MergeConflictException(i18NHelper.msg(
                    "EXC_ResultTypeNotNamedQuery", // NOI18N
                    annotateNamedQuery.getName()));
        } else if (treeQuery.getResultType() != null
                && annotateNamedQuery.getResultType() != null
                &&
                !annotateNamedQuery.getResultType().equals(
                        treeQuery.getResultType())) {
            throw new MergeConflictException(i18NHelper.msg(
                    "EXC_ResultTypeNotNamedQuery", // NOI18N
                    annotateNamedQuery.getName()));
        }
        if (annotateNamedQuery.getEjbInterfaceType() != null &&
                treeQuery.getEjbInterfaceType() == null) {
            throw new MergeConflictException(i18NHelper.msg(
                    "EXC_EjbInterfaceTypeNotNamedQuery", // NOI18N
                    annotateNamedQuery.getName()));
        } else if (treeQuery.getEjbInterfaceType() != null
                && annotateNamedQuery.getEjbInterfaceType() != null
                &&
                !annotateNamedQuery.getEjbInterfaceType().equals(
                        treeQuery.getEjbInterfaceType())) {
            throw new MergeConflictException(i18NHelper.msg(
                    "EXC_EjbInterfaceTypeNotNamedQuery", // NOI18N
                    annotateNamedQuery.getName()));
        }
    }

    /**
     * It verifies the following things a) QueryString of named query can not be
     * overriden by XML b) ResultType of named query can not be overriden by XML
     * c) EjbInterfaceType of named query can not be overriden by XML
     *
     * @param property           tree
     * @param annotateNamedQuery named-query node
     */
    public boolean verifyNamedQuery(
            PropertyDescriptor property,
            NamedQueryDescriptor annotateNamedQuery)
            throws MergeConflictException {
        List namedQuery = property.getNamedQuery();
        Iterator itr = namedQuery.iterator();
        NamedQueryDescriptor treeQuery = null;
        while (itr.hasNext()) {
            NamedQueryDescriptor query = (NamedQueryDescriptor) itr.next();
            if (query.getName() != null &&
                    query.getName().equals(annotateNamedQuery.getName()))
                treeQuery = query;
        }
        if (treeQuery != null)
            matchNamedQuery(treeQuery, annotateNamedQuery);
        return true;
    }

    /**
     * It verifies the following things a) name has to be specified in named
     * query array b) QueryString of named query can not be overriden by XML c)
     * ResultType of named query can not be overriden by XML d) EjbInterfaceType
     * of named query can not be overriden by XML
     *
     * @param beanTree             tree
     * @param annotateNamedQueries named-query node array
     */
    public boolean verifyNamedQueries(
            ClassDescriptor beanTree,
            NamedQueryDescriptor[] annotateNamedQueries)
            throws MergeConflictException {
        for (int i = 0; i < annotateNamedQueries.length; i++) {
            verifyNamedQuery(beanTree, annotateNamedQueries[i]);
        }
        return true;
    }

    /**
     * It verifies the following things a) name has to be specified in named
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
            throws MergeConflictException {
        for (int i = 0; i < annotateNamedQueries.length; i++) {
            verifyNamedQuery(property, annotateNamedQueries[i]);
        }
        return true;
    }

    /**
     * It verifies the table descriptor
     *
     * @param beanTree       descriptor tree
     * @param annotatedTable table node
     */
    public boolean verifyTable(
            ClassDescriptor beanTree,
            TableDescriptor annotatedTable) throws MergeConflictException {
        return true;
    }

    /**
     * It verifies the secondary table descriptor
     *
     * @param beanTree       descriptor tree
     * @param annotatedTable secondary-table node
     */
    public boolean verifySecondaryTable(
            ClassDescriptor beanTree,
            SecondaryTableDescriptor annotatedTable) {
        return true;
    }

    /**
     * It verifies the secondary tables descriptor
     *
     * @param beanTree       descriptor tree
     * @param annotateTables secondary-table node array
     */
    public boolean verifySecondaryTables(
            ClassDescriptor beanTree,
            SecondaryTableDescriptor[] annotateTables)
            throws MergeConflictException {
        return true;
    }

    /**
     * JoinColumn name must be specified at class level
     *
     * @param beanTree            descriptor tree
     * @param annotatedJoinColumn join-column node
     */
    public boolean verifyJoinColumn(
            ClassDescriptor beanTree,
            JoinColumnDescriptor annotatedJoinColumn)
            throws MergeConflictException {
        if (annotatedJoinColumn != null &&
                annotatedJoinColumn.getName().equals(""))
            throw new MergeConflictException(i18NHelper.msg(
                    "EXC_NameNeededJoinColumnAtClassLevel")); // NOI18N
        return true;
    }

    /**
     * It verifies the join column tables descriptor
     *
     * @param property            tree
     * @param annotatedJoinColumn join-column node
     */
    public boolean verifyJoinColumn(
            PropertyDescriptor property,
            JoinColumnDescriptor annotatedJoinColumn)
            throws MergeConflictException {
        return true;
    }

    /**
     * JoinColumn name must be specified
     *
     * @param beanTree            descriptor tree
     * @param annotateJoinColumns join-column node array
     */
    public boolean verifyJoinColumns(
            ClassDescriptor beanTree,
            JoinColumnDescriptor[] annotateJoinColumns)
            throws MergeConflictException {
        for (int i = 0; i < annotateJoinColumns.length; i++)
            verifyJoinColumn(beanTree, annotateJoinColumns[i]);
        return true;
    }

    /**
     * JoinColumn name must be specified
     *
     * @param property            tree
     * @param annotateJoinColumns join-column node array
     */
    public boolean verifyJoinColumns(
            PropertyDescriptor property,
            JoinColumnDescriptor[] annotateJoinColumns)
            throws MergeConflictException {
        for (int i = 0; i < annotateJoinColumns.length; i++) {
            if (annotateJoinColumns[i] != null &&
                    annotateJoinColumns[i].getName().equals(""))
                throw new MergeConflictException(i18NHelper.msg(
                        "EXC_NameJoinColumnAtPropertyNeededIfMoreThanOne")); // NOI18N
        }
        return true;
    }

    /**
     * It verifies the inheritance descriptor
     *
     * @param beanTree            descriptor tree
     * @param annotateInheritance inheritance node
     */
    public boolean verifyInheritance(
            ClassDescriptor beanTree,
            InheritanceDescriptor annotateInheritance)
            throws MergeConflictException {
        return true;
    }

    /**
     * It verifies the inheritance join column descriptor
     *
     * @param beanTree                       descriptor tree
     * @param annotatedInheritanceJoinColumn inheritance-join-column node
     */
    public boolean verifyInheritanceJoinColumn(
            ClassDescriptor beanTree,
            InheritanceJoinColumnDescriptor annotatedInheritanceJoinColumn)
            throws MergeConflictException {
        return true;
    }

    /**
     * InheritanceJoinColumn name must be specified
     *
     * @param beanTree                       descriptor tree
     * @param annotateInheritanceJoinColumns inheritance-join-column node array
     */
    public boolean verifyInheritanceJoinColumns(
            ClassDescriptor beanTree,
            InheritanceJoinColumnDescriptor[] annotateInheritanceJoinColumns)
            throws MergeConflictException {
        for (int i = 0; i < annotateInheritanceJoinColumns.length; i++) {
            if (annotateInheritanceJoinColumns[i] != null &&
                    annotateInheritanceJoinColumns[i].getName().equals(""))
                throw new MergeConflictException(i18NHelper.msg(
                        "EXC_NameInheritanceJoinColumnNeededIfMoreThanOne")); // NOI18N
        }
        return true;
    }

    /**
     * It verifies the discriminator column descriptor
     *
     * @param beanTree                    descriptor tree
     * @param discriminatorColumnAnnotate discriminator-column node
     */
    public boolean verifyDiscriminatorColumn(
            ClassDescriptor beanTree,
            DiscriminatorColumnDescriptor discriminatorColumnAnnotate)
            throws MergeConflictException {
        return true;
    }

    /**
     * Embeddable annotation can not be overriden by XML
     *
     * @param beanTree   descriptor tree
     * @param embeddable embeddable node
     */
    public boolean verifyEmbeddable(
            ClassDescriptor beanTree,
            EmbeddableDescriptor embeddable) throws MergeConflictException {
        EmbeddableDescriptor embeddableObject = beanTree.getEmbeddable();
        if (embeddable != null && embeddableObject != null) {
            if (embeddable.getAccess() != null &&
                    embeddableObject.getAccess() == null) {
                throw new MergeConflictException(i18NHelper.msg(
                        "EXC_EmbeddableAccessNotOverriden")); // NOI18N
            } else if (embeddableObject.getAccess() != null
                    && embeddable.getAccess() != null
                    &&
                    !embeddableObject.getAccess().equals(
                            embeddable.getAccess())) {
                throw new MergeConflictException(i18NHelper.msg(
                        "EXC_EmbeddableAccessNotOverriden")); // NOI18N
            }
        }
        return true;
    }

    /**
     * It verifies the GeneratedIdTable descriptor
     *
     * @param beanTree         descriptor tree
     * @param generatedIdTable generated-id-column node
     */
    public boolean verifyGeneratedIdTable(
            ClassDescriptor beanTree,
            GeneratedIdTableDescriptor generatedIdTable)
            throws MergeConflictException {
        return true;
    }

    /**
     * It verifies the sequenceGenerator descriptor
     *
     * @param beanTree          descriptor tree
     * @param sequenceGenerator sequence-generator node
     */
    public boolean verifySequenceGenerator(
            ClassDescriptor beanTree,
            SequenceGeneratorDescriptor sequenceGenerator)
            throws MergeConflictException {
        return true;
    }

    /**
     * It verifies the sequenceGenerator descriptor
     *
     * @param property          tree
     * @param sequenceGenerator sequence-generator node
     */
    public boolean verifySequenceGenerator(
            PropertyDescriptor property,
            SequenceGeneratorDescriptor sequenceGenerator)
            throws MergeConflictException {
        return true;
    }

    /**
     * It verifies the tableGenerator descriptor
     *
     * @param beanTree       descriptor tree
     * @param tableGenerator table-generator node
     */
    public boolean verifyTableGenerator(
            ClassDescriptor beanTree,
            TableGeneratorDescriptor tableGenerator)
            throws MergeConflictException {
        return true;
    }

    /**
     * It verifies the tableGenerator descriptor
     *
     * @param property       tree
     * @param tableGenerator table-generator node
     */
    public boolean verifyTableGenerator(
            PropertyDescriptor property,
            TableGeneratorDescriptor tableGenerator)
            throws MergeConflictException {
        return true;
    }

    /**
     * It verifies the Id descriptor
     *
     * @param property tree
     * @param id       id node
     */
    public boolean verifyId(PropertyDescriptor property, IdDescriptor id)
            throws MergeConflictException {
        return true;
    }

    /**
     * It verifies the EmbeddedId descriptor
     *
     * @param property tree
     * @param id       embedded-id node
     */
    public boolean verifyEmbeddedId(
            PropertyDescriptor property,
            EmbeddedIdDescriptor id)
            throws MergeConflictException {
        return true;
    }

    /**
     * It verifies the Embedded descriptor
     *
     * @param property tree
     * @param embedded embedded node
     */
    public boolean verifyEmbedded(
            PropertyDescriptor property,
            EmbeddedDescriptor embedded)
            throws MergeConflictException {
        return true;
    }

    /**
     * It verifies the Column descriptor
     *
     * @param property tree
     * @param col      column node
     */
    public boolean verifyColumn(
            PropertyDescriptor property,
            ColumnDescriptor col)
            throws MergeConflictException {
        return true;
    }

    /**
     * It verifies the Association Table descriptor
     *
     * @param property    tree
     * @param associtaion association-table node
     */
    public boolean verifyAssociationTable(
            PropertyDescriptor property,
            AssociationTableDescriptor associtaion)
            throws MergeConflictException {
        return true;
    }

    public boolean verifyProperty(
            ClassDescriptor entityBeanClass,
            PropertyDescriptor property) throws MergeConflictException {
        return true;
    }

    /**
     * Basic mapping can not be overriden by XML
     *
     * @param property tree
     * @param basic    basic node
     */
    public boolean verifyBasicMapping(
            PropertyDescriptor property,
            BasicDescriptor basic)
            throws MergeConflictException {
        MappingDescriptor mapping = property.getMapping();
        if (mapping != null) {
            if (mapping instanceof BasicDescriptor) {
                if (((BasicDescriptor) mapping).getFetch() != null
                        &&
                        !(((BasicDescriptor) mapping).getFetch().equals(
                                basic.getFetch()))) {
                    throw new MergeConflictException(i18NHelper.msg(
                            "EXC_BasicMappingNotOverriden", // NOI18N
                            property.getName()));
                }
                if (((BasicDescriptor) mapping).getFetch() == null
                        && basic.getFetch() != null) {
                    throw new MergeConflictException(i18NHelper.msg(
                            "EXC_BasicMappingNotOverriden", // NOI18N
                            property.getName()));
                }
            } else {
                throw new MergeConflictException(i18NHelper.msg(
                        "EXC_BasicMappingNotOverriden", property.getName())); // NOI18N
            }
        }
        return true;
    }

    /**
     * Serialized mapping can not be overriden by XML
     *
     * @param property   tree
     * @param serialized mapping node
     */
    public boolean verifySerializeMapping(
            PropertyDescriptor property,
            SerializedDescriptor serialized)
            throws MergeConflictException {
        MappingDescriptor mapping = property.getMapping();
        if (mapping != null) {
            if (mapping instanceof SerializedDescriptor) {
                if (((SerializedDescriptor) mapping).getFetch() != null
                        &&
                        !(((SerializedDescriptor) mapping).getFetch().equals(
                                serialized.getFetch()))) {
                    throw new MergeConflictException(i18NHelper.msg(
                            "EXC_SerializedMappingNotOverriden", // NOI18N
                            property.getName()));
                }
                if (((SerializedDescriptor) mapping).getFetch() == null
                        && serialized.getFetch() != null) {
                    throw new MergeConflictException(i18NHelper.msg(
                            "EXC_SerializedMappingNotOverriden", // NOI18N
                            property.getName()));
                }
            } else {
                throw new MergeConflictException(i18NHelper.msg(
                        "EXC_SerializedMappingNotOverriden", // NOI18N
                        property.getName()));
            }
        }
        return true;
    }

    /**
     * Lob mapping can not be overriden by XML
     *
     * @param property tree
     * @param lob      mapping node
     */
    public boolean verifyLobMapping(
            PropertyDescriptor property,
            LobDescriptor lob)
            throws MergeConflictException {
        MappingDescriptor mapping = property.getMapping();
        if (mapping != null) {
            if (mapping instanceof LobDescriptor) {
                if (((LobDescriptor) mapping).getFetch() != null
                        &&
                        !(((LobDescriptor) mapping).getFetch().equals(
                                lob.getFetch()))) {
                    throw new MergeConflictException(i18NHelper.msg(
                            "EXC_LobMappingNotOverriden", property.getName())); // NOI18N
                } else if (((LobDescriptor) mapping).getFetch() == null
                        && lob.getFetch() != null) {
                    throw new MergeConflictException(i18NHelper.msg(
                            "EXC_LobMappingNotOverriden", property.getName())); // NOI18N
                } else if (((LobDescriptor) mapping).getType() != null
                        &&
                        !(((LobDescriptor) mapping).getType().equals(
                                lob.getType()))) {
                    throw new MergeConflictException(i18NHelper.msg(
                            "EXC_LobMappingNotOverriden", property.getName())); // NOI18N
                } else if (((LobDescriptor) mapping).getType() == null
                        && lob.getType() != null) {
                    throw new MergeConflictException(i18NHelper.msg(
                            "EXC_LobMappingNotOverriden", property.getName())); // NOI18N
                }
            } else {
                throw new MergeConflictException(i18NHelper.msg(
                        "EXC_LobMappingNotOverriden", property.getName())); // NOI18N
            }
        }
        return true;
    }

    /**
     * oneToOne mapping can not be overriden by XML
     *
     * @param property tree
     * @param oneToOne mapping node
     */
    public boolean verifyOneToOneMapping(
            PropertyDescriptor property,
            OneToOneDescriptor oneToOne)
            throws MergeConflictException {
        MappingDescriptor mapping = property.getMapping();
        if (mapping != null) {
            if (mapping instanceof OneToOneDescriptor) {
                if (((OneToOneDescriptor) mapping).getTargetEntity() != null
                        &&
                        !(((OneToOneDescriptor) mapping).getTargetEntity()
                        .equals(oneToOne.getTargetEntity()))) {
                    throw new MergeConflictException(i18NHelper.msg(
                            "EXC_LobMappingNotOverriden", property.getName())); // NOI18N
                } else if (((OneToOneDescriptor) mapping).getTargetEntity() ==
                        null
                        && oneToOne.getTargetEntity() != null) {
                    throw new MergeConflictException(i18NHelper.msg(
                            "EXC_OneToOneMappingNotOverriden", // NOI18N
                            property.getName()));
                } else if (((OneToOneDescriptor) mapping).getFetch() != null
                        &&
                        !(((OneToOneDescriptor) mapping).getFetch().equals(
                                oneToOne.getFetch()))) {
                    throw new MergeConflictException(i18NHelper.msg(
                            "EXC_OneToOneMappingNotOverriden", // NOI18N
                            property.getName()));
                } else if (((OneToOneDescriptor) mapping).getFetch() == null
                        && oneToOne.getFetch() != null) {
                    throw new MergeConflictException(i18NHelper.msg(
                            "EXC_OneToOneMappingNotOverriden", // NOI18N
                            property.getName()));
                } else if (((OneToOneDescriptor) mapping).getMappedBy() !=
                        null
                        &&
                        !(((OneToOneDescriptor) mapping).getMappedBy().equals(
                                oneToOne.getMappedBy()))) {
                    throw new MergeConflictException(i18NHelper.msg(
                            "EXC_OneToOneMappingNotOverriden", // NOI18N
                            property.getName()));
                } else if (((OneToOneDescriptor) mapping).getMappedBy() ==
                        null
                        && oneToOne.getMappedBy() != null) {
                    throw new MergeConflictException(i18NHelper.msg(
                            "EXC_OneToOneMappingNotOverriden", // NOI18N
                            property.getName()));
                } else if (((OneToOneDescriptor) mapping).getCascade() == null
                        && oneToOne.getCascade() != null) {
                    throw new MergeConflictException(i18NHelper.msg(
                            "EXC_OneToOneMappingNotOverriden", // NOI18N
                            property.getName()));
                } else if (((OneToOneDescriptor) mapping).getCascade() != null
                        && oneToOne.getCascade() != null) {
                    List xmlCascadeList =
                            ((OneToOneDescriptor) mapping).getCascade();
                    List annotateCascadeList = oneToOne.getCascade();
                    if (xmlCascadeList.size() != annotateCascadeList.size())
                        throw new MergeConflictException(i18NHelper.msg(
                                "EXC_OneToOneMappingNotOverriden", // NOI18N
                                property.getName()));
                    for (int i = 0; i < xmlCascadeList.size(); i++) {
                        if (!xmlCascadeList.get(i).equals(
                                annotateCascadeList.get(i)))
                            throw new MergeConflictException(i18NHelper.msg(
                                    "EXC_OneToOneMappingNotOverriden", // NOI18N
                                    property.getName()));
                    }
                }
            } else {
                throw new MergeConflictException(i18NHelper.msg(
                        "EXC_OneToOneMappingNotOverriden", // NOI18N
                        property.getName()));
            }
        }
        return true;
    }

    /**
     * oneToMany mapping can not be overriden by XML
     *
     * @param property  tree
     * @param oneToMany mapping node
     */
    public boolean verifyOneToManyMapping(
            PropertyDescriptor property,
            OneToManyDescriptor oneToMany)
            throws MergeConflictException {
        MappingDescriptor mapping = property.getMapping();
        if (mapping != null) {
            if (mapping instanceof OneToManyDescriptor) {
                if (((OneToManyDescriptor) mapping).getTargetEntity() != null
                        &&
                        !(((OneToManyDescriptor) mapping).getTargetEntity()
                        .equals(oneToMany.getTargetEntity()))) {
                    throw new MergeConflictException(i18NHelper.msg(
                            "EXC_OneToManyMappingNotOverriden", // NOI18N
                            property.getName()));
                } else if (((OneToManyDescriptor) mapping).getTargetEntity() ==
                        null
                        && oneToMany.getTargetEntity() != null) {
                    throw new MergeConflictException(i18NHelper.msg(
                            "EXC_OneToManyMappingNotOverriden", // NOI18N
                            property.getName()));
                } else if (((OneToManyDescriptor) mapping).getFetch() != null
                        &&
                        !(((OneToManyDescriptor) mapping).getFetch().equals(
                                oneToMany.getFetch()))) {
                    throw new MergeConflictException(i18NHelper.msg(
                            "EXC_OneToManyMappingNotOverriden", // NOI18N
                            property.getName()));
                } else if (((OneToManyDescriptor) mapping).getFetch() == null
                        && oneToMany.getFetch() != null) {
                    throw new MergeConflictException(i18NHelper.msg(
                            "EXC_OneToManyMappingNotOverriden", // NOI18N
                            property.getName()));
                } else if (((OneToManyDescriptor) mapping).getMappedBy() !=
                        null
                        &&
                        !(((OneToManyDescriptor) mapping).getMappedBy().equals(
                                oneToMany.getMappedBy()))) {
                    throw new MergeConflictException(i18NHelper.msg(
                            "EXC_OneToManyMappingNotOverriden", // NOI18N
                            property.getName()));
                } else if (((OneToManyDescriptor) mapping).getMappedBy() ==
                        null
                        && oneToMany.getMappedBy() != null) {
                    throw new MergeConflictException(i18NHelper.msg(
                            "EXC_OneToManyMappingNotOverriden", // NOI18N
                            property.getName()));
                } else if (((OneToManyDescriptor) mapping).getCascade() ==
                        null
                        && oneToMany.getCascade() != null) {
                    throw new MergeConflictException(i18NHelper.msg(
                            "EXC_OneToManyMappingNotOverriden", // NOI18N
                            property.getName()));
                } else if (((OneToManyDescriptor) mapping).getCascade() !=
                        null
                        && oneToMany.getCascade() != null) {
                    List xmlCascadeList =
                            ((OneToManyDescriptor) mapping).getCascade();
                    List annotateCascadeList = oneToMany.getCascade();
                    if (xmlCascadeList.size() != annotateCascadeList.size())
                        throw new MergeConflictException(i18NHelper.msg(
                                "EXC_OneToManyMappingNotOverriden", // NOI18N
                                property.getName()));
                    for (int i = 0; i < xmlCascadeList.size(); i++) {
                        if (!xmlCascadeList.get(i).equals(
                                annotateCascadeList.get(i)))
                            throw new MergeConflictException(i18NHelper.msg(
                                    "EXC_OneToManyMappingNotOverriden", // NOI18N
                                    property.getName()));
                    }
                }
            } else {
                throw new MergeConflictException(i18NHelper.msg(
                        "EXC_OneToManyMappingNotOverriden", // NOI18N
                        property.getName()));
            }
        }
        return true;
    }

    /**
     * manyToOne mapping can not be overriden by XML
     *
     * @param property  tree
     * @param manyToOne mapping node
     */
    public boolean verifyManyToOneMapping(
            PropertyDescriptor property,
            ManyToOneDescriptor manyToOne)
            throws MergeConflictException {
        return verifyOneToOneMapping(property, manyToOne);
    }

    /**
     * manyToMany mapping can not be overriden by XML
     *
     * @param property   tree
     * @param manyToMany mapping node
     */
    public boolean verifyManyToManyMapping(
            PropertyDescriptor property,
            ManyToManyDescriptor manyToMany)
            throws MergeConflictException {
        return verifyOneToManyMapping(property, manyToMany);
    }

    public boolean verifyMapping(
            PropertyDescriptor propertyNode,
            MappingDescriptor mappingNode)
            throws MergeConflictException {
        // check to see if mapping type is same or not.
        return false; //TODO not yet implemented.
    }
}