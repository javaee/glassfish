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

import com.sun.forte4j.modules.dbmodel.ColumnElement;
import com.sun.forte4j.modules.dbmodel.ColumnPairElement;
import com.sun.forte4j.modules.dbmodel.SchemaElement;
import com.sun.forte4j.modules.dbmodel.TableElement;
import com.sun.org.apache.jdo.model.ModelException;
import com.sun.org.apache.jdo.util.I18NHelper;
import com.sun.persistence.api.deployment.*;
import com.sun.persistence.api.model.mapping.*;
import com.sun.persistence.utility.logging.Logger;

import java.util.*;

import static com.sun.persistence.utility.StringHelper.isEmpty;
import static com.sun.forte4j.modules.dbmodel.DBIdentifier.create;

/**
 * This class is responsible for mapping a descriptor object graph
 * {@link DeploymentUnit} to mapping model {@link MappingModel}. This class
 * does not create the MappingModel itself, because as it does not have the
 * intelligence to decide which implementation of {@link MappingModel} to use.
 * So, it is supplied with an instance of {@link MappingModel} to populate.
 * This class is also supplied with a {@link SchemaElement} which provides 
 * the DB model.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class MappingModelMapper {

    /*
     * TODO:
     * 1) Change it to use Visitor pattern, if possible
     */

    /* The source DeploymentUnit which is getting mapped. */
    private DeploymentUnit du;

    /* The target MappingModel that is getting mapped into. */
    private MappingModel mappingModel;

    /* The SchemaElement that provides DB model */
    private SchemaElement se;

    private final static I18NHelper i18NHelper = I18NHelper.getInstance(
            LogHelperDeployment.class);

    private final static Logger logger = LogHelperDeployment.getLogger();

    /**
     * This method is responsible for doing the mapping. The result of this
     * operation gets stored in the input MappingModel object passed to this
     * method.
     *
     * @param du                  the source DeploymentUnit
     * @param se                  the SchemaElement that provides DB model
     * @param mappingModel        the target MappingModel that is getting
     *                            populated.
     * @throws DeploymentException  if something goes wrong.
     * @throws NullPointerException if any schema or table does not exist.
     */
    public static void map(
            DeploymentUnit du,
            SchemaElement se,
            MappingModel mappingModel)
            throws DeploymentException, NullPointerException {
        new MappingModelMapper(du, se, mappingModel).map();
    }

    /**
     * Construct a new MappingModelMapper. Usage Note: Create one instance of
     * MappingModelMapper for every model mapping.
     *
     * @param du                the source DeploymentUnit
     * @param se                the SchemaElement that provides DB model
     * @param mappingModel      the target MappingModel that is getting
     *                          populated.
     */
    private MappingModelMapper(
            DeploymentUnit du,
            SchemaElement se,
            MappingModel mappingModel) {
        this.du = du;
        this.mappingModel = mappingModel;
        this.se = se;
    }

    /**
     * This method is responsible for doing the mapping. The result of this
     * operation gets stored in the input MappingModel object passed to this
     * object during its construction.
     * Do NOT call this method more than once.
     *
     * @throws DeploymentException if something goes wrong.
     * @throws NullPointerException if any schema or table does not exist.
     */
    private void map() throws DeploymentException, NullPointerException {
        try {
            for (ClassDescriptor c : du.getPersistenceJar().getClassDescriptor()) {
                if (!c.isEntity()) continue; // don't bother about non entities.
                logger.fine(i18NHelper.msg(
                        "MSG_MappingModelMapperMappingClass", // NOI18N
                        c.getName()));
                MappingClass mc = mapEntityClass(c);
                mapSecondaryTables(c, mc);
                mapProperties(c, mc);
            }
        } catch (ModelException e) {
            throw new DeploymentException(e);
        }
    }

    /**
     * This method create a MappingClass and MappingTable for the given
     * ClassDescriptor.
     * @param c ClassDescriptor which is being processed
     * @return MappingClass that is created.
     * @throws ModelException
     */
    private MappingClass mapEntityClass(ClassDescriptor c)
            throws ModelException {
        MappingClass mc = mappingModel.createMappingClass(c.getName());

        TableElement pte = getTableElement(c.getTable());
        assert(pte!=null);
        mc.createPrimaryMappingTable(pte);
        return mc;
    }

    /**
     * Map all the secondary tables.
     * @param c ClassDescriptor that is getting processed.
     * @param mc MappingClass corresponding to this class
     * @throws ModelException
     */
    private void mapSecondaryTables(ClassDescriptor c, MappingClass mc)
            throws ModelException {
        MappingTable pmt = mc.getPrimaryMappingTable();
        TableElement pte = pmt.getTable();

        // for each secondary table, add a corresponding mapping table
        // to mapping class.
        for (SecondaryTableDescriptor std : c.getSecondaryTable()) {
            TableElement ste = getTableElement(std);
            MappingReferenceKey mrk = pmt.createMappingReferenceKey();
            for (JoinColumnDescriptor jc : std.getJoin()) {
                ColumnPairElement cpe = createCPE(ste, jc.getName(),
                        pte, jc.getReferencedColumnName());
                mrk.addColumnPair(cpe);
            }
        }
    }

    /**
     * Process all the properties.
     * @param c ClassDescriptor whose properties will be processed
     * @param mc MappingClass corresponding to this class.
     * @throws ModelException
     */
    private void mapProperties(ClassDescriptor c, MappingClass mc)
            throws ModelException {
        for (PropertyDescriptor property : c.getProperty()) {
            logger.fine(i18NHelper.msg(
                    "MSG_MappingModelMapperMappingProperty", // NOI18N
                    property.getName()));
            // for each property, create a mapping field and populate it
            // depending on the type of the property.
            MappingField mf = mc.createMappingField(property.getName());
            if (property.isSetVersion()) {
                mf.setVersion(property.isVersion());
            }
            if (property.isEmbedded()) {
                throw new UnsupportedOperationException(); // TODO
            } else if (property.isRelationshipProperty()) {
                mapRelationshipProperty(property, mf);
            } else {
                // this is a simple mapping
                mapNonRelationshipProperty(property, mf);
            }
        }
    }

    /**
     * In this method, we process non-relationship property only.
     * @param property provides details of this property
     * @param mf MappingField corresponding to this property
     * @throws ModelException
     */
    private void mapNonRelationshipProperty(
            PropertyDescriptor property,
            MappingField mf)
            throws ModelException {
        MappingClass mc = mf.getDeclaringMappingClass();

        // Which table this column belongs to? primary table or secondary table?
        ColumnDescriptor cd = property.getColumn();
        String secondary = cd.getSecondaryTable();
        TableElement te = (isEmpty(secondary) ?
                mc.getPrimaryMappingTable().getTable() :
                mc.getMappingTable(secondary).getTable());
        ColumnElement ce = te.getColumn(create(cd.getName()));
        assert(ce!=null);
        mf.addColumn(ce);
    }

    /**
     * In this method, we process relationship property only.
     * This method creates the MappingRelationship.
     * It also creates the ColumnPairElements if this mapping is the owner
     * of the relationship.
     *
     * @param property the PropertyDescriptor which is being mapped.
     * @param mf MappingField corresponding to this property.
     * @throws ModelException
     */
    private void mapRelationshipProperty(
            PropertyDescriptor property,
            MappingField mf)
            throws ModelException {
        //Irrespective of owner or not, we create MappingRelationship.
        MappingRelationship mr = mf.createMappingRelationship();

        // this is a safe cast as this is a relationship property
        RelationalMappingDescriptor mapping =
                RelationalMappingDescriptor.class.cast(property.getMapping());

        if(mapping.isOwningSide()) { // create CPE only for owning side.
            AssociationTableDescriptor atd = property.getAssociationTable();
            if (atd != null) {
                // relationships that use association table (a.k.a. JoinTable)
                // get special treatment because we need to set
                // associated ColumnPairElement in the MappingRelationship.
                mapAssociationTable(atd, mapping, mr);
            } else {
                // this property uses normal JoinColumns.
                mapJoinColumns(property.getJoinColumn(), mapping, mr);
            }
        }
    }

    /**
     * In this method, we create the ColumnPairElements and associated
     * ColumnPairElements to represent the relationship that use an association
     * table (a.k.a. join table).
     *
     * @param atd          provides details of AssociationTable
     * @param mapping      provides details about this mapping
     * @param mr           the MappingRelationship where ColumnPairElements will
     *                     be added
     * @throws ModelException
     */
    private void mapAssociationTable(
            AssociationTableDescriptor atd,
            RelationalMappingDescriptor mapping,
            MappingRelationship mr)
            throws ModelException {
        MappingClass mc = mr.getDeclaringMappingClass();
        TableElement sourcePTE = mc.getPrimaryMappingTable().getTable();
        TableElement ate = getTableElement(atd.getTable());
        MappingReferenceKey mrk = mr.createMappingReferenceKey(
                MappingRelationship.USAGE_JOIN);
        List<JoinColumnDescriptor> joinsForThisTable = atd.getJoinColumn();

        for (JoinColumnDescriptor jc : joinsForThisTable) {
            ColumnPairElement cpe = createCPE(sourcePTE,
                    jc.getReferencedColumnName(), ate, jc.getName());
            mrk.addColumnPair(cpe);
        }

        // Let's create the associated ColumnPairElements.
        // This involves identifying target TableElement.
        ClassDescriptor targetClassDescriptor = du.getPersistenceJar()
                .getClassDescriptor(mapping.getTargetEntity());
        TableElement targetTableElement = getTableElement(
                targetClassDescriptor.getTable());
        mrk = mr.createMappingReferenceKey(
            (mapping instanceof OneToManyDescriptor) ?
            MappingRelationship.USAGE_ELEMENT :
            MappingRelationship.USAGE_REFERENCE);

        List<JoinColumnDescriptor> joinsForTargetTable =
            atd.getInverseJoinColumn();
        for (JoinColumnDescriptor jc : joinsForTargetTable) {
            ColumnPairElement cpe = createCPE(ate, jc.getName(),
                    targetTableElement, jc.getReferencedColumnName());
            mrk.addColumnPair(cpe);
        }
    }

    /**
     * In this method, we create the ColumnPairElements for relationships that
     * use normal JoinColumns.
     *
     * @param joins        List of JoinColumnDescriptor which supplies join
     *                     information
     * @param mapping      provides details about this mapping
     * @param mr           the MappingRelationship where ColumnPairElements will
     *                     be added
     * @throws ModelException
     */
    private void mapJoinColumns(
            List<JoinColumnDescriptor> joins,
            RelationalMappingDescriptor mapping,
            MappingRelationship mr)
            throws ModelException {
        MappingClass mc = mr.getDeclaringMappingClass();
        TableElement sourcePTE = mc.getPrimaryMappingTable().getTable();
        ClassDescriptor referencedClassDescriptor = du.getPersistenceJar()
                .getClassDescriptor(mapping.getTargetEntity());
        TableElement referencedTable = getTableElement(
                referencedClassDescriptor.getTable());
        MappingReferenceKey mrk = mr.createMappingReferenceKey(
            (mapping instanceof OneToManyDescriptor) ?
            MappingRelationship.USAGE_ELEMENT :
            MappingRelationship.USAGE_REFERENCE);

        for (JoinColumnDescriptor jc : joins) {
            String secondary = jc.getSecondaryTable();
            TableElement localTable = isEmpty(secondary) ?
                    sourcePTE :
                    mc.getMappingTable(secondary).getTable();
            ColumnPairElement cpe = createCPE(localTable, jc.getName(),
                    referencedTable, jc.getReferencedColumnName());

            mrk.addColumnPair(cpe);
        }
    }

    /**
     * Look for the table whose details are found in input argument.
     * @param tableDescriptor containing schema name and table name.
     * @return the TableElement, null if not found.
     */
    private TableElement getTableElement(TableDescriptor tableDescriptor) {
        TableElement result = se.getTable(create(tableDescriptor.getName()));
        return result;
    }

    /**
     * Convenience method which is used to create {@link ColumnPairElement}
     * @param localTable the TableElement which contains the JoinColumn
     * @param referencedTable the TableElement that this JoinColumn references
     * @param localColumnName name of the local column.
     * @param referencedColumnName name of the referenced column
     * @return a ColumnPairElement
     */
    private ColumnPairElement createCPE(TableElement localTable,
                                        String localColumnName,
                                        TableElement referencedTable,
                                        String referencedColumnName) {
        // TODO: NameUtil should provide an API which takes all these args.
        String cpeName =
                localTable.getName().getFullName() + //full name includes schema
                "." + // NOI18N
                localColumnName +
                ";" + // NOI18N
                referencedTable.getName().getFullName() +
                "." + // NOI18N
                referencedColumnName;
        ColumnPairElement cpe = localTable.getColumnPair(create(cpeName));
        logger.fine(i18NHelper.msg("MSG_CreateCPE", cpeName, cpe)); // NOI18N
        assert(cpe != null);
        return cpe;
    }
}
