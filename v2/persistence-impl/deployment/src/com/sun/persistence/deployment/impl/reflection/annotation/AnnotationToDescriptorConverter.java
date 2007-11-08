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

/*
 *
 * Created on February 1, 2005, 1:06 PM
 */


package com.sun.persistence.deployment.impl.reflection.annotation;

import com.sun.persistence.api.deployment.*;
import com.sun.persistence.deployment.impl.LogHelperDeployment;

import javax.persistence.*;
import java.util.Collections;

/**
 * This class coverts the annotation to the descriptor node in the tree.
 *
 * @author Sanjeeb Sahoo
 */
public class AnnotationToDescriptorConverter {

    private static ObjectFactory of = new ObjectFactory();

    public static UniqueConstraintDescriptor convert(
            UniqueConstraint from) {
        if (from == null) return null;
        UniqueConstraintDescriptor to = of.createUniqueConstraintDescriptor();
        Collections.addAll(to.getColumnName(), from.columnNames());
        to.setPrimary(from.primary());
        return to;
    }

    public static UniqueConstraintDescriptor[] convert(
            UniqueConstraint[] froms) {
        if (froms == null) return null;
        UniqueConstraintDescriptor[] tos =
                new UniqueConstraintDescriptor[froms.length];
        int i = 0;
        for (UniqueConstraint from : froms) {
            tos[i++] = convert(from);
        }
        return tos;
    }

    public static EntityDescriptor convert(Entity from) {
        if (from == null) return null;
        EntityDescriptor to = of.createEntityDescriptor();
        to.setEntityType(com.sun.persistence.api.deployment.EntityType.valueOf(
                from.entityType().toString()));
        to.setAccess(com.sun.persistence.api.deployment.AccessType.valueOf(
                from.access().toString()));
        to.setVersion(from.version());
        return to;
    }

    public static TableDescriptor convert(Table from) {
        if (from == null) return null;
        TableDescriptor to = of.createTableDescriptor();
        to.setName(from.name());
        to.setCatalog(from.catalog());
        to.setSchema(from.schema());
        Collections.addAll(to.getUniqueConstraint(),
                convert(from.uniqueConstraints()));
        return to;
    }

    public static SecondaryTableDescriptor convert(
            SecondaryTable from) {
        if (from == null) return null;
        SecondaryTableDescriptor to = of.createSecondaryTableDescriptor();
        to.setName(from.name());
        to.setCatalog(from.catalog());
        to.setSchema(from.schema());
        Collections.addAll(to.getUniqueConstraint(),
                convert(from.uniqueConstraints()));
        Collections.addAll(to.getJoin(), convert(from.join()));
        return to;
    }

    public static SecondaryTableDescriptor[] convert(
            SecondaryTables froms) {
        if (froms == null) return null;
        SecondaryTableDescriptor[] tos =
                new SecondaryTableDescriptor[froms.value().length];
        int i = 0;
        for (SecondaryTable from : froms.value()) {
            tos[i++] = convert(from);
        }
        return tos;
    }

    public static ColumnDescriptor convert(Column from) {
        if (from == null) return null;
        ColumnDescriptor to = of.createColumnDescriptor();
        to.setName(from.name());
        to.setPrimaryKey(from.primaryKey());
        to.setUnique(from.unique());
        to.setNullable(from.nullable());
        to.setInsertable(from.insertable());
        to.setUpdatable(from.updatable());
        to.setColumnDefinition(from.columnDefinition());
        to.setSecondaryTable(from.secondaryTable());
        to.setLength(from.length());
        to.setPrecision(from.precision());
        to.setScale(from.scale());
        return to;
    }

    public static ColumnDescriptor[] convert(Column[] froms) {
        if (froms == null) return null;
        ColumnDescriptor[] tos = new ColumnDescriptor[froms.length];
        int i = 0;
        for (Column from : froms) {
            tos[i++] = convert(from);
        }
        return tos;
    }

    public static JoinColumnDescriptor convert(
            JoinColumn from) {
        if (from == null) return null;
        JoinColumnDescriptor to = of.createJoinColumnDescriptor();
        to.setName(from.name());
        to.setReferencedColumnName(from.referencedColumnName());
        to.setPrimaryKey(from.primaryKey());
        to.setUnique(from.unique());
        to.setNullable(from.nullable());
        to.setInsertable(from.insertable());
        to.setUpdatable(from.updatable());
        to.setColumnDefinition(from.columnDefinition());
        to.setSecondaryTable(from.secondaryTable());
        return to;
    }

    public static JoinColumnDescriptor[] convert(
            JoinColumns froms) {
        if (froms == null) return null;
        JoinColumnDescriptor[] tos = new JoinColumnDescriptor[froms.value()
                .length];
        int i = 0;
        for (JoinColumn from : froms.value()) {
            tos[i++] = convert(from);
        }
        return tos;
    }

    public static JoinColumnDescriptor[] convert(
            JoinColumn[] froms) {
        if (froms == null) return null;
        JoinColumnDescriptor[] tos = new JoinColumnDescriptor[froms.length];
        int i = 0;
        for (JoinColumn from : froms) {
            tos[i++] = convert(from);
        }
        return tos;
    }

    public static AttributeOverrideDescriptor convert(
            AttributeOverride from) {
        if (from == null) return null;
        AttributeOverrideDescriptor to = of.createAttributeOverrideDescriptor();
        to.setName(from.name());
        Collections.addAll(to.getColumn(), convert(from.column()));
        return to;
    }

    public static AttributeOverrideDescriptor[] convert(
            AttributeOverride[] froms) {
        if (froms == null) return null;
        AttributeOverrideDescriptor[] tos =
                new AttributeOverrideDescriptor[froms.length];
        int i = 0;
        for (AttributeOverride from : froms) {
            tos[i++] = convert(from);
        }
        return tos;
    }

    public static IdDescriptor convert(Id from) {
        if (from == null) return null;
        IdDescriptor to = of.createIdDescriptor();
        to.setGenerate(com.sun.persistence.api.deployment.GeneratorType.valueOf(
                from.generate().toString()));
        to.setGenerator(from.generator());
        return to;
    }

    public static EmbeddedIdDescriptor convert(
            EmbeddedId from) {
        if (from == null) return null;
        EmbeddedIdDescriptor to = of.createEmbeddedIdDescriptor();
        Collections.addAll(to.getValue(), convert(from.value()));
        return to;
    }

    public static EmbeddableDescriptor convert(
            Embeddable from) {
        if (from == null) return null;
        EmbeddableDescriptor to = of.createEmbeddableDescriptor();
        to.setAccess(com.sun.persistence.api.deployment.AccessType.valueOf(
                from.access().toString()));
        return to;
    }

    public static EmbeddedDescriptor convert(Embedded from) {
        if (from == null) return null;
        EmbeddedDescriptor to = of.createEmbeddedDescriptor();
        Collections.addAll(to.getValue(), convert(from.value()));
        return to;
    }

    public static BasicDescriptor convert(Basic from) {
        if (from == null) return null;
        BasicDescriptor to = of.createBasicDescriptor();
        to.setFetch(com.sun.persistence.api.deployment.FetchType.valueOf(
                from.fetch().toString()));
        return to;
    }

    public static SerializedDescriptor convert(Serialized from) {
        if (from == null) return null;
        SerializedDescriptor to = of.createSerializedDescriptor();
        to.setFetch(com.sun.persistence.api.deployment.FetchType.valueOf(
                from.fetch().toString()));
        return to;
    }

    public static LobDescriptor convert(Lob from) {
        if (from == null) return null;
        LobDescriptor to = of.createLobDescriptor();
        to.setFetch(com.sun.persistence.api.deployment.FetchType.valueOf(
                from.fetch().toString()));
        to.setType(com.sun.persistence.api.deployment.LobType.valueOf(
                from.type().toString()));
        return to;
    }

    public static InheritanceDescriptor convert(
            Inheritance from) {
        if (from == null) return null;
        InheritanceDescriptor to = of.createInheritanceDescriptor();
        to.setStrategy(
                com.sun.persistence.api.deployment.InheritanceType.valueOf(
                from.strategy().toString()));
        to.setDiscriminatorType(
                com.sun.persistence.api.deployment.DiscriminatorType.valueOf(
                from.discriminatorType().toString()));
        to.setDiscriminatorValue(from.discriminatorValue());
        return to;
    }

    public static InheritanceJoinColumnDescriptor convert(
            InheritanceJoinColumn from) {
        if (from == null) return null;
        InheritanceJoinColumnDescriptor to =
                of.createInheritanceJoinColumnDescriptor();
        to.setName(from.name());
        to.setReferencedColumnName(from.referencedColumnName());
        to.setColumnDefinition(from.columnDefinition());
        return to;
    }

    public static InheritanceJoinColumnDescriptor[] convert(
            InheritanceJoinColumns froms) {
        if (froms == null) return null;
        InheritanceJoinColumnDescriptor[] tos =
                new InheritanceJoinColumnDescriptor[froms.value().length];
        int i = 0;
        for (InheritanceJoinColumn from : froms.value()) {
            tos[i++] = convert(from);
        }
        return tos;
    }

    public static DiscriminatorColumnDescriptor convert(
            DiscriminatorColumn from) {
        if (from == null) return null;
        DiscriminatorColumnDescriptor to =
                of.createDiscriminatorColumnDescriptor();
        to.setName(from.name());
        to.setNullable(from.nullable());
        to.setColumnDefinition(from.columnDefinition());
        to.setLength(from.length());
        return to;
    }

    private static com.sun.persistence.api.deployment.CascadeType[] convert(
            javax.persistence.CascadeType[] froms) {
        if (froms == null) return null;
        com.sun.persistence.api.deployment.CascadeType[] tos =
                new com.sun.persistence.api.deployment.CascadeType[froms.length];
        int i = 0;
        for (javax.persistence.CascadeType from : froms) {
            tos[i++] =
                    com.sun.persistence.api.deployment.CascadeType.valueOf(
                            from.toString());
        }
        return tos;
    }

    public static MappingDescriptor convert(OneToOne from) {
        if (from == null) return null;
        OneToOneDescriptor to = of.createOneToOneDescriptor();
        to.setTargetEntity(from.targetEntity());
        Collections.addAll(to.getCascade(), convert(from.cascade()));
        to.setFetch(com.sun.persistence.api.deployment.FetchType.valueOf(
                from.fetch().toString()));
        to.setMappedBy(from.mappedBy());
        //TODO        to.setOptional(from.optional());
        LogHelperDeployment.getLogger().warning(
                "MSG_NotProcessingSomeEntriesInOneToOne"); // NOI18N
        return to;
    }

    public static MappingDescriptor convert(ManyToOne from) {
        if (from == null) return null;
        ManyToOneDescriptor to = of.createManyToOneDescriptor();
        to.setTargetEntity(from.targetEntity());
        Collections.addAll(to.getCascade(), convert(from.cascade()));
        to.setFetch(com.sun.persistence.api.deployment.FetchType.valueOf(
                from.fetch().toString()));
        //TODO        to.setOptional(from.optional());
        LogHelperDeployment.getLogger().warning(
                "MSG_NotProcessingOptionalInManyToMany"); // NOI18N
        return to;
    }

    public static MappingDescriptor convert(OneToMany from) {
        if (from == null) return null;
        OneToManyDescriptor to = of.createOneToManyDescriptor();
        to.setTargetEntity(from.targetEntity());
        Collections.addAll(to.getCascade(), convert(from.cascade()));
        to.setFetch(com.sun.persistence.api.deployment.FetchType.valueOf(
                from.fetch().toString()));
        to.setMappedBy(from.mappedBy());
        return to;
    }

    public static MappingDescriptor convert(ManyToMany from) {
        if (from == null) return null;
        ManyToManyDescriptor to = of.createManyToManyDescriptor();
        to.setTargetEntity(from.targetEntity());
        Collections.addAll(to.getCascade(), convert(from.cascade()));
        to.setFetch(com.sun.persistence.api.deployment.FetchType.valueOf(
                from.fetch().toString()));
        //to.setIsInverse(from.isInverse()); no more valid as isInverse is removed from annotation
        to.setMappedBy(from.mappedBy());
        LogHelperDeployment.getLogger().warning("MSG_NotProcessingIsInverse"); // NOI18N
        return to;
    }

    public static AssociationTableDescriptor convert(
            AssociationTable from) {
        if (from == null) return null;
        AssociationTableDescriptor to = of.createAssociationTableDescriptor();
        to.setTable(convert(from.table()));
        Collections.addAll(to.getJoinColumn(), convert(from.joinColumns()));
        Collections.addAll(to.getInverseJoinColumn(),
                convert(from.inverseJoinColumns()));
        return to;
    }

    public static NamedQueryDescriptor convert(
            NamedQuery from) {
        if (from == null) return null;
        NamedQueryDescriptor to = of.createNamedQueryDescriptor();
        to.setName(from.name());
        to.setQueryString(from.queryString());
        to.setResultType(from.resultType());
        to.setEjbInterfaceType(
                com.sun.persistence.api.deployment.AccessMode.valueOf(
                from.ejbInterfaceType().toString()));
        return to;
    }

    public static NamedQueryDescriptor[] convert(
            NamedQueries froms) {
        if (froms == null) return null;
        NamedQueryDescriptor[] tos = new NamedQueryDescriptor[froms.value()
                .length];
        int i = 0;
        for (NamedQuery from : froms.value()) {
            tos[i++] = convert(from);
        }
        return tos;
    }

    public static GeneratedIdTableDescriptor convert(
            GeneratedIdTable from) {
        if (from == null) return null;
        GeneratedIdTableDescriptor to = of.createGeneratedIdTableDescriptor();
        to.setName(from.name());
        to.setTable(convert(from.table()));
        to.setPkColumnName(from.pkColumnName());
        to.setValueColumnName(from.valueColumnName());
        return to;
    }

    public static SequenceGeneratorDescriptor convert(
            SequenceGenerator from) {
        if (from == null) return null;
        SequenceGeneratorDescriptor to = of.createSequenceGeneratorDescriptor();
        to.setName(from.name());
        to.setSequenceName(from.sequenceName());
        to.setInitialValue(from.initialValue());
        to.setAllocationSize(from.allocationSize());
        return to;
    }

    public static TableGeneratorDescriptor convert(
            TableGenerator from) {
        if (from == null) return null;
        TableGeneratorDescriptor to = of.createTableGeneratorDescriptor();
        to.setName(from.name());
        to.setPkColumnValue(from.pkColumnValue());
        to.setAllocationSize(from.allocationSize());
        return to;
    }
}
