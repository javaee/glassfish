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
 * Visitor.java
 *
 * Created on March 18, 2005, 2:31 PM
 */


package com.sun.persistence.api.deployment;

/**
 * Visitor interface to support visitor pattern for the descriptor node tree.
 *
 * @author Sanjeeb Sahoo
 */
public interface Visitor {

    /**
     * Visits a PersistenceJarDescriptor
     * @param pjt  the PersistenceJarDescriptor to be visited.
     * @throws DeploymentException if something unexpected happens
     */
    public void visitPersistenceJarDescriptor(PersistenceJarDescriptor pjt)
            throws DeploymentException;

    /**
     * Visits a ClassDescriptor
     * @param c  the ClassDescriptor to be visited.
     * @throws DeploymentException if something unexpected happens
     */
    public void visitClassDescriptor(ClassDescriptor c)
            throws DeploymentException;

    /**
     * Visits an EntityDescriptor
     * @param e  the EntityDescriptor to be visited.
     * @throws DeploymentException if something unexpected happens
     */
    public void visitEntityDescriptor(EntityDescriptor e)
            throws DeploymentException;

    /**
     * Visits a NamedQueryDescriptor
     * @param namedQuery  the NamedQueryDescriptor to be visited.
     * @throws DeploymentException if something unexpected happens
     */
    public void visitNamedQueryDescriptor(NamedQueryDescriptor namedQuery)
            throws DeploymentException;

    /**
     * Visits a CTableDescriptor
     * @param table  the TableDescriptor to be visited.
     * @throws DeploymentException if something unexpected happens
     */
    public void visitTableDescriptor(TableDescriptor table)
            throws DeploymentException;

    /**
     * Visits a SecondaryTableDescriptor
     * @param st  the SecondaryTableDescriptor to be visited.
     * @throws DeploymentException if something unexpected happens
     */
    public void visitSecondaryTableDescriptor(SecondaryTableDescriptor st)
            throws DeploymentException;

    /**
     * Visits a UniqueConstraintDescriptor
     * @param uc  the UniqueConstraintDescriptor to be visited.
     * @throws DeploymentException if something unexpected happens
     */
    public void visitUniqueConstraintDescriptor(UniqueConstraintDescriptor uc)
            throws DeploymentException;

    /**
     * Visits a JoinColumnDescriptor
     * @param jc  the JoinColumnDescriptor to be visited.
     * @throws DeploymentException if something unexpected happens
     */
    public void visitJoinColumnDescriptor(JoinColumnDescriptor jc)
            throws DeploymentException;

    /**
     * Visits a InheritanceDescriptor
     * @param inheritance  the InheritanceDescriptor to be visited.
     * @throws DeploymentException if something unexpected happens
     */
    public void visitInheritanceDescriptor(InheritanceDescriptor inheritance)
            throws DeploymentException;

    /**
     * Visits a InheritanceJoinColumnDescriptor
     * @param inheritanceJC  the InheritanceJoinColumnDescriptor to be visited.
     * @throws DeploymentException if something unexpected happens
     */
    public void visitInheritanceJoinColumnDescriptor(
            InheritanceJoinColumnDescriptor inheritanceJC)
            throws DeploymentException;

    /**
     * Visits a DiscriminatorColumnDescriptor
     * @param dc  the DiscriminatorColumnDescriptor to be visited.
     * @throws DeploymentException if something unexpected happens
     */
    public void visitDiscriminatorColumnDescriptor(
            DiscriminatorColumnDescriptor dc) throws DeploymentException;

    /**
     * Visits a EmbeddableDescriptor
     * @param embeddable  the EmbeddableDescriptor to be visited.
     * @throws DeploymentException if something unexpected happens
     */
    public void visitEmbeddableDescriptor(EmbeddableDescriptor embeddable)
            throws DeploymentException;

    /**
     * Visits a GeneratedIdTableDescriptor
     * @param genIdTable  the GeneratedIdTableDescriptor to be visited.
     * @throws DeploymentException if something unexpected happens
     */
    public void visitGeneratedIdTableDescriptor(
            GeneratedIdTableDescriptor genIdTable) throws DeploymentException;

    /**
     * Visits a SequenceGeneratorDescriptor
     * @param sg  the SequenceGeneratorDescriptor to be visited.
     * @throws DeploymentException if something unexpected happens
     */
    public void visitSequenceGeneratorDescriptor(
            SequenceGeneratorDescriptor sg) throws DeploymentException;

    /**
     * Visits a TableGeneratorDescriptor
     * @param tg  the TableGeneratorDescriptor to be visited.
     * @throws DeploymentException if something unexpected happens
     */
    public void visitTableGeneratorDescriptor(TableGeneratorDescriptor tg)
            throws DeploymentException;

    /**
     * Visits a PropertyDescriptor
     * @param p  the PropertyDescriptor to be visited.
     * @throws DeploymentException if something unexpected happens
     */
    public void visitPropertyDescriptor(PropertyDescriptor p)
            throws DeploymentException;

    /**
     * Visits a IdDescriptor
     * @param id  the IdDescriptor to be visited.
     * @throws DeploymentException if something unexpected happens
     */
    public void visitIdDescriptor(IdDescriptor id) throws DeploymentException;

    /**
     * Visits a EmbeddedIdDescriptor
     * @param embeddedId  the EmbeddedIdDescriptor to be visited.
     * @throws DeploymentException if something unexpected happens
     */
    public void visitEmbeddedIdDescriptor(EmbeddedIdDescriptor embeddedId)
            throws DeploymentException;

    /**
     * Visits a EmbeddedDescriptor
     * @param embedded  the EmbeddedDescriptor to be visited.
     * @throws DeploymentException if something unexpected happens
     */
    public void visitEmbeddedDescriptor(EmbeddedDescriptor embedded)
            throws DeploymentException;

    /**
     * Visits a ColumnDescriptor
     * @param column  the ColumnDescriptor to be visited.
     * @throws DeploymentException if something unexpected happens
     */
    public void visitColumnDescriptor(ColumnDescriptor column)
            throws DeploymentException;

    /**
     * Visits a BasicDescriptor
     * @param m  the BasicDescriptor to be visited.
     * @throws DeploymentException if something unexpected happens
     */
    public void visitBasicDescriptor(BasicDescriptor m)
            throws DeploymentException;

    /**
     * Visits a SerializedDescriptor
     * @param m  the SerializedDescriptor to be visited.
     * @throws DeploymentException if something unexpected happens
     */
    public void visitSerializedDescriptor(SerializedDescriptor m)
            throws DeploymentException;

    /**
     * Visits a LobDescriptor
     * @param m  the LobDescriptor to be visited.
     * @throws DeploymentException if something unexpected happens
     */
    public void visitLobDescriptor(LobDescriptor m) throws DeploymentException;

    /**
     * Visits a OneToOneDescriptor
     * @param m  the OneToOneDescriptor to be visited.
     * @throws DeploymentException if something unexpected happens
     */
    public void visitOneToOneDescriptor(OneToOneDescriptor m)
            throws DeploymentException;

    /**
     * Visits a OneToManyDescriptor
     * @param m  the OneToManyDescriptor to be visited.
     * @throws DeploymentException if something unexpected happens
     */
    public void visitOneToManyDescriptor(OneToManyDescriptor m)
            throws DeploymentException;

    /**
     * Visits a ManyToOneDescriptor
     * @param m  the ManyToOneDescriptor to be visited.
     * @throws DeploymentException if something unexpected happens
     */
    public void visitManyToOneDescriptor(ManyToOneDescriptor m)
            throws DeploymentException;

    /**
     * Visits a ManyToManyDescriptor
     * @param m  the ManyToManyDescriptor to be visited.
     * @throws DeploymentException if something unexpected happens
     */
    public void visitManyToManyDescriptor(ManyToManyDescriptor m)
            throws DeploymentException;

    /**
     * Visits a AssociationTableDescriptor
     * @param aTable  the AssociationTableDescriptor to be visited.
     * @throws DeploymentException if something unexpected happens
     */
    public void visitAssociationTableDescriptor(
            AssociationTableDescriptor aTable) throws DeploymentException;

    /**
     * Visits a AttributeOverrideDescriptor
     * @param n  the AttributeOverrideDescriptor to be visited.
     * @throws DeploymentException if something unexpected happens
     */
    public void visitAttributeOverrideDescriptor(
            AttributeOverrideDescriptor n)
            throws DeploymentException;

}
