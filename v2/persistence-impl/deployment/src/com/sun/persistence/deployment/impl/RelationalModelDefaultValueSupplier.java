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
import com.sun.persistence.utility.JavaTypeHelper;
import com.sun.persistence.utility.StringHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for setting default values in the relational model.
 * The persistence-api spec allows many descriptors (both in annotations as well
 * as XML DD) to be left unspecified. There are well defined rules to set
 * default values for them. e.g. if entity name is not set, the default value is
 * "unqualified name of the Java class". There are two kinds of models, viz: a)
 * logical model -- this models the object model of the entities and their
 * relationship. b) physical model -- this specifies the schema for the physical
 * data store which the logical model is mapped to. The persistence-api spec is
 * very carefully designed to separate these two models allowing non-relational
 * store to be used as well for a given logical model. The spec also defines
 * rules for mapping logical model to a relational database because that is the
 * most common case.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class RelationalModelDefaultValueSupplier {
    /*
     * Default values for relational model is populated in
     * two phases. In phase #1 {@link FirstPhaseDefaultManager},
     * we populate table names, column names and identify primary columns etc.
     * In phase #2 {@link SecondPhaseDefaultManager}, we populate cross reference
     * information like join columns, association tables etc.
     */

    private DeploymentUnit du;

    private ObjectFactory of = new ObjectFactory();

    private static final I18NHelper i18NHelper = I18NHelper.getInstance(
            LogHelperDeployment.class);

    private static final char UNDERSCORE = '_' ;

    public RelationalModelDefaultValueSupplier(DeploymentUnit du) {
        this.du = du;
    }

    public void populateORMDefaultValues()
            throws DeploymentException {
        LogHelperDeployment.getLogger().fine(
                i18NHelper.msg("MSG_PouplatingDefaultsForRelationl1")); // NOI18N
        du.getPersistenceJar().accept(
                new TreeWalker(new FirstPhaseDefaultManager()));
        LogHelperDeployment.getLogger().fine(
                i18NHelper.msg("MSG_PouplatingDefaultsForRelationl2")); // NOI18N
        du.getPersistenceJar().accept(
                new TreeWalker(new SecondPhaseDefaultManager()));
    }

    /**
     * O/R model information is populated in two phases, in phase #1, we go over
     * the entity model and populate table names, column names and identify
     * primary columns etc.
     */
    private class FirstPhaseDefaultManager extends EmptyVisitor {
        @Override public void visitEntityDescriptor(EntityDescriptor node)
                throws DeploymentException {
            try {
                ClassDescriptor c = node.parent();
                TableDescriptor table = c.getTable();
                if (table == null) {
                    table = of.createTableDescriptor();
                    c.setTable(table);
                }
                if (StringHelper.isEmpty(table.getName())) {
                    table.setName(JavaTypeHelper.getShortClassName(
                            node.getName())
                            .toUpperCase());
                }

                //cannonicalize the tree. if there is a JoinColumn at the top level,
                //then move it to each SecondaryTable node
                if (!c.getJoinColumn().isEmpty()) {
                    LogHelperDeployment.getLogger().finest(
                            "Cannonicalizing the tree. Moving JoinColumns to each SecondaryTable node"); // NOI18N
                    for (SecondaryTableDescriptor st : c.getSecondaryTable()) {
                        st.getJoin().addAll(c.getJoinColumn());
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override public void visitBasicDescriptor(BasicDescriptor m) {
            createOrUpdateColumn(m);
        }

        @Override public void visitSerializedDescriptor(
                SerializedDescriptor m) {
            createOrUpdateColumn(m);
        }

        @Override public void visitLobDescriptor(LobDescriptor m) {
            createOrUpdateColumn(m);
        }

        private void createOrUpdateColumn(MappingDescriptor m) {
            PropertyDescriptor p = m.parent();
            ColumnDescriptor column = p.getColumn();
            if (p.getColumn() == null) {
                column = of.createColumnDescriptor();
                column.setPrimaryKey(p.isId());
                p.setColumn(column);
            }
            if (StringHelper.isEmpty(column.getName())) {
                column.setName(p.getName().toUpperCase());
            }
        }

    }

    /**
     * In phase #2, we populate cross reference information like, join columns,
     * association table etc.
     */
    private class SecondPhaseDefaultManager extends EmptyVisitor {

        private static final String DEFAULT_GENERATED_ID_TABLE_NAME =
                "SUN_DEFAULT_GENERATEDID_TABLE"; // NOI18N

        @Override public void visitTableDescriptor(final TableDescriptor node)
                throws DeploymentException {
            /*
             * phase #1 should have set the name of table if this belongs to
             * entity node, else this probably belongs to association table node
             * in which case, visitAssociationTable() would have set the name or
             * this node belongs to GeneratedIdTable, in which case,
             * visitGeneratedIdTable() would have set the name.
             * Hence this assert.
             */
            assert(!StringHelper.isEmpty(node.getName()));
            if (StringHelper.isEmpty(node.getSchema())) node.setSchema("");
            if (StringHelper.isEmpty(node.getCatalog())) node.setCatalog("");
        }

        @Override public void visitSecondaryTableDescriptor(
                SecondaryTableDescriptor st) throws DeploymentException {
            try {
                ClassDescriptor c = st.parent();
                List<JoinColumnDescriptor> joinCols = st.getJoin();
                if (joinCols.isEmpty()) {
                    ColumnDescriptor[] pksOfPrimaryTable = getPKColumns(c);
                    for (ColumnDescriptor pk : pksOfPrimaryTable) {
                        //secondary table join col name is same pk of primary table
                        joinCols.add(createJoinColumn(pk.getName(), pk));
                    }
                } else if (joinCols.size() == 1) {
                    // When there is only one JoinColumn, we can
                    // set its name and referenced column name provided
                    // the primary table has only one PK column.
                    ColumnDescriptor[] pksOfPrimaryTable = getPKColumns(c);
                    assert(pksOfPrimaryTable.length == 1);
                    ColumnDescriptor pk = pksOfPrimaryTable[0];
                    JoinColumnDescriptor jc = joinCols.get(0);
                    if (StringHelper.isEmpty(jc.getName())) {
                        jc.setName(pk.getName());
                    }
                    if (StringHelper.isEmpty(jc.getReferencedColumnName())) {
                        jc.setReferencedColumnName(pk.getName());
                    }
                }
            } catch (Exception e) {
                throw new DeploymentException(e);
            }
        }

        /*
         * For OneToOne rel, there is no diffence in naming strategy for
         * join column between unidirectional and bidirectional case
         */
        @Override public void visitOneToOneDescriptor(OneToOneDescriptor m)
                throws DeploymentException {
            try {
                PropertyDescriptor p = PropertyDescriptor.class.cast(
                        m.parent());
                if (m.isOwningSide()) {
                    //for a 1:1 relationship, the owning side must have a join column
                    List<JoinColumnDescriptor> joinCols = p.getJoinColumn();
                    String propertyName = m.parent().getName();
                    if (joinCols.isEmpty()) {
                        ColumnDescriptor[] pksOfTargetTable = getPKColumns(m);
                        for (ColumnDescriptor pk : pksOfTargetTable) {
                            String jcName = buildName(propertyName, pk.getName());
                            joinCols.add(createJoinColumn(jcName, pk));
                        }
                    } else if (joinCols.size() == 1) {
                        // When there is only one JoinColumn, we can
                        // set its name and referenced column name provided
                        // the primary table has only one PK column.
                        ColumnDescriptor[] pksOfTargetTable = getPKColumns(m);
                        assert(pksOfTargetTable.length == 1);
                        ColumnDescriptor pk = pksOfTargetTable[0];
                        JoinColumnDescriptor jc = joinCols.get(0);
                        if (StringHelper.isEmpty(jc.getName())) {
                            String jcName = buildName(propertyName, pk.getName());
                            jc.setName(jcName);
                        }
                        if (StringHelper.isEmpty(jc.getReferencedColumnName())) {
                            jc.setReferencedColumnName(pk.getName());
                        }
                    }
                }
            } catch (Exception e) {
                throw new DeploymentException(e);
            }
        }

        /*
         * For ManyToOne rel, there is no diffence in naming strategy for
         * join column between unidirectional and bidirectional case
         */
        @Override public void visitManyToOneDescriptor(ManyToOneDescriptor m) {
            try {
                PropertyDescriptor p = PropertyDescriptor.class.cast(
                        m.parent());
                if (m.isOwningSide()) {
                    //for a n:1 relationship, the owning side must have a join column
                    List<JoinColumnDescriptor> joinCols = p.getJoinColumn();
                    String propertyName = m.parent().getName();
                    if (joinCols.isEmpty()) {
                        ColumnDescriptor[] pksOfTargetTable = getPKColumns(m);
                        for (ColumnDescriptor pk : pksOfTargetTable) {
                            String jcName = buildName(propertyName, pk.getName());
                            joinCols.add(createJoinColumn(jcName, pk));
                        }
                    } else if (joinCols.size() == 1) {
                        // When there is only one JoinColumn, we can
                        // set its name and referenced column name provided
                        // the primary table has only one PK column.
                        ColumnDescriptor[] pksOfTargetTable = getPKColumns(m);
                        assert(pksOfTargetTable.length == 1);
                        ColumnDescriptor pk = pksOfTargetTable[0];
                        JoinColumnDescriptor jc = joinCols.get(0);
                        if (StringHelper.isEmpty(jc.getName())) {
                            String jcName = buildName(propertyName, pk.getName());
                            jc.setName(jcName);
                        }
                        if (StringHelper.isEmpty(jc.getReferencedColumnName())) {
                            jc.setReferencedColumnName(pk.getName());
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override public void visitOneToManyDescriptor(OneToManyDescriptor m) {
            PropertyDescriptor p = m.parent();
            if (p.getJoinColumn() == null && m.isUnidirectional()) {
                //default mapping strategy for unidirectional 1:1 mapping is to use JoinTable
                if (p.getAssociationTable() == null) {
                    p.setAssociationTable(
                            of.createAssociationTableDescriptor());

                    // rest of info will be filled when we visit this node.
                }
            }
        }

        @Override public void visitManyToManyDescriptor(
                ManyToManyDescriptor m) {
            PropertyDescriptor p = m.parent();
            if (p.getAssociationTable() == null && m.isOwningSide()) {
                //owning side of a many-to-many rel defines the association table
                p.setAssociationTable(of.createAssociationTableDescriptor());

                //rest of info will be filled when we visit this node.
            }
        }

        @Override public void visitAssociationTableDescriptor(
                AssociationTableDescriptor at) {
            try {
                PropertyDescriptor p = PropertyDescriptor.class.cast(
                        at.parent());
                ClassDescriptor c = p.parent();
                assert(p.isRelationshipProperty()); //verifier assertion
                RelationalMappingDescriptor m =
                        RelationalMappingDescriptor.class.cast(p.getMapping());
                if (at.getTable() == null) {
                    at.setTable(of.createTableDescriptor());
                }
                if (StringHelper.isEmpty(at.getTable().getName())) {
                    at.getTable().setName(buildAssociationTableName(p));
                }
                String propertyName = m.parent().getName();
                {
                    //now JoinColumn
                    List<JoinColumnDescriptor> joinCols = at.getJoinColumn();
                    if (joinCols.isEmpty()) {
                        ColumnDescriptor[] pksOfSourceTable = getPKColumns(c);
                        for (ColumnDescriptor pk : pksOfSourceTable) {
                            String joinColumnName;
                            if (m.isBidirectional()) {
                                /* See rules in section #2.1.8.4 */
                                String otherSidePropertyName = m.getInverse()
                                        .parent()
                                        .getName();
                                joinColumnName =
                                        buildName(otherSidePropertyName,
                                        pk.getName());
                            } else {
                                /* See rules in section #2.1.8.5.2 */
                                joinColumnName =
                                        buildName(c.getEntity().getName(),
                                        pk.getName());
                            }
                            joinCols.add(createJoinColumn(joinColumnName, pk));
                        }
                    } else if (joinCols.size() == 1) {
                        ColumnDescriptor[] pksOfSourceTable = getPKColumns(c);
                        assert(pksOfSourceTable.length == 1);
                        JoinColumnDescriptor jc = joinCols.get(0);
                        if (StringHelper.isEmpty(jc.getName())) {
                            String joinColumnName;
                            if (m.isBidirectional()) {
                                String otherSidePropertyName = m.getMappedBy();
                                joinColumnName =
                                        buildName(otherSidePropertyName,
                                        pksOfSourceTable[0].getName());
                            } else {
                                joinColumnName =
                                        buildName(c.getEntity().getName(),
                                        pksOfSourceTable[0].getName());
                            }
                            jc.setName(joinColumnName);
                        }
                        if (StringHelper.isEmpty(jc.getReferencedColumnName())) {
                            jc.setReferencedColumnName(
                                    pksOfSourceTable[0].getName());
                        }
                    }
                }
                {
                    //now InverseJoinColumn
                    List<JoinColumnDescriptor> inverseJoinCols =
                            at.getInverseJoinColumn();
                    if (inverseJoinCols.isEmpty()) {
                        ColumnDescriptor[] pksOfTargetTable = getPKColumns(m);
                        for (ColumnDescriptor pk : pksOfTargetTable) {
                            inverseJoinCols.add(createJoinColumn(
                                    buildName(propertyName, pk.getName()), pk));
                        }
                    } else if (inverseJoinCols.size() == 1) {
                        ColumnDescriptor[] pksOfTargetTable = getPKColumns(m);
                        assert(pksOfTargetTable.length == 1);
                        JoinColumnDescriptor jc = inverseJoinCols.get(0);
                        if (StringHelper.isEmpty(jc.getName())) {
                            jc.setName(
                                    buildName(propertyName,
                                    pksOfTargetTable[0].getName()));
                        }
                        if (StringHelper.isEmpty(jc.getReferencedColumnName())) {
                            jc.setReferencedColumnName(
                                    pksOfTargetTable[0].getName());
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override public void visitColumnDescriptor(ColumnDescriptor column)
                throws DeploymentException {
            if (!column.isSetUnique()) column.setUnique(false);
            if (!column.isSetNullable()) column.setNullable(true);
            if (!column.isSetInsertable()) column.setInsertable(true);
            if (!column.isSetUpdatable()) column.setUpdatable(true);
            if (!column.isSetLength()) column.setLength(255);
            if (!column.isSetPrecision()) column.setPrecision(0);
            if (!column.isSetScale()) column.setScale(0);
        }

        @Override public void visitJoinColumnDescriptor(
                JoinColumnDescriptor node)
                throws DeploymentException {
            if (!node.isSetUnique()) node.setUnique(false);
            if (!node.isSetNullable()) node.setNullable(true);
            if (!node.isSetInsertable()) node.setInsertable(true);
            if (!node.isSetUpdatable()) node.setUpdatable(true);
        }

        @Override public void visitGeneratedIdTableDescriptor(
                GeneratedIdTableDescriptor node) throws DeploymentException {
            String tableName = node.getName();
            if(StringHelper.isEmpty(tableName)) {
                node.setName(DEFAULT_GENERATED_ID_TABLE_NAME);
            }
            TableDescriptor embeddedTable = node.getTable();
            if(embeddedTable == null) { // create if not there.
                embeddedTable = of.createTableDescriptor();
                node.setTable(embeddedTable);
            }
            if(StringHelper.isEmpty(embeddedTable.getName())) {
                embeddedTable.setName(node.getName());
            }
        }

        private String buildAssociationTableName(PropertyDescriptor p) {
            ClassDescriptor c = p.parent();
            String sourceTableName = c.getTable().getName();
            String targetEntity = RelationalMappingDescriptor.class.cast(
                    p.getMapping())
                    .getTargetEntity();
            String targetTableName = du.getPersistenceJar().getClassDescriptor(
                    targetEntity)
                    .getTable()
                    .getName();
            return (sourceTableName + '_' + targetTableName).toUpperCase();
        }

        private JoinColumnDescriptor createJoinColumn(
                String joinColumnName,
                ColumnDescriptor referencingColumn) {
            JoinColumnDescriptor jc = of.createJoinColumnDescriptor();
            jc.setName(joinColumnName);
            jc.setReferencedColumnName(referencingColumn.getName());
            jc.setNullable(true);
            jc.setInsertable(true);
            jc.setUpdatable(true);
            jc.setColumnDefinition(referencingColumn.getColumnDefinition());
            return jc;
        }

        private ColumnDescriptor[] getPKColumns(RelationalMappingDescriptor m)
                throws Exception {
            ClassDescriptor targetClass =
                    du.getPersistenceJar().getClassDescriptor(
                            m.getTargetEntity());
            return getPKColumns(targetClass);
        }

        private ColumnDescriptor[] getPKColumns(ClassDescriptor c)
                throws Exception {
            List<ColumnDescriptor> result = new ArrayList<ColumnDescriptor>();
            for (PropertyDescriptor p : c.getProperty()) {
                if (p.getEmbeddedId() != null) {
                    Object propertyType = getJavaType(p);
                    //its an embedded id, so return the column list for embeddable class
                    JavaModel javaModel = du.getJavaModel();
                    PersistenceJarDescriptor persistenceJar =
                            du.getPersistenceJar();
                    ClassDescriptor embeddableClass =
                            persistenceJar.getClassDescriptor(
                            javaModel.getName(propertyType));
                    List props = embeddableClass.getProperty();
                    ColumnDescriptor[] pkColumns =
                            new ColumnDescriptor[props.size()];
                    for (int i = 0; i < pkColumns.length; ++i) {
                        ColumnDescriptor column = PropertyDescriptor.class.cast(
                                props.get(i))
                                .getColumn();
                        assert(column != null);
                        pkColumns[i] = column;
                    }
                    return pkColumns;
//            }else if(c.getIdClass()!=null){
//            }
                } else if (p.getId() != null) {
                    result.add(p.getColumn());
                }
            }
            return result.toArray(new ColumnDescriptor[0]);
        }

        /**
         * @param property whose JavaType information is required
         * @return the javaType of this property.
         * @throws ClassNotFoundException
         */
        private Object getJavaType(PropertyDescriptor property)
                throws Exception {
            ClassDescriptor declaringClassDescriptor = property.parent();
            AccessType accessType = declaringClassDescriptor.getEntity()
                    .getAccess();
            PersistentPropertyIntrospector introspector =
                    PersistentPropertyIntrospectorFactoryImpl.getInstance()
                    .getIntrospector(du.getJavaModel());
            Object javaType = du.getJavaModel().getJavaType(
                    declaringClassDescriptor.getName());
            JavaModel.FieldOrProperty javaProperty = introspector.getPCProperty(
                    javaType,
                    accessType, property.getName());
            return javaProperty.getJavaType();
        }

    }

    private static String buildName(String s1, String s2){
        return (s1 + UNDERSCORE + s2).toUpperCase();
    }

}
