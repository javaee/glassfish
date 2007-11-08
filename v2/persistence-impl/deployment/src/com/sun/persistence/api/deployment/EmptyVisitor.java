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
 * EmptyVisitor.java
 *
 * Created on March 18, 2005, 2:31 PM
 */


package com.sun.persistence.api.deployment;

/**
 * This class provides a default implementation for {@link Visitor} interface.
 * The default implementation of each visit method is an empty method.
 * Other visitors subclass this class.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class EmptyVisitor implements Visitor {

    /**
     * {@inheritDoc}
     */
    public void visitPersistenceJarDescriptor(PersistenceJarDescriptor node)
            throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void visitClassDescriptor(ClassDescriptor node)
            throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void visitUniqueConstraintDescriptor(
            UniqueConstraintDescriptor node) throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void visitGeneratedIdTableDescriptor(
            GeneratedIdTableDescriptor node) throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void visitAssociationTableDescriptor(
            AssociationTableDescriptor node) throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void visitEmbeddableDescriptor(EmbeddableDescriptor node)
            throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void visitNamedQueryDescriptor(NamedQueryDescriptor node)
            throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void visitInheritanceJoinColumnDescriptor(
            InheritanceJoinColumnDescriptor node) throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void visitInheritanceDescriptor(InheritanceDescriptor node)
            throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void visitPropertyDescriptor(PropertyDescriptor node)
            throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void visitJoinColumnDescriptor(JoinColumnDescriptor node)
            throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void visitSequenceGeneratorDescriptor(
            SequenceGeneratorDescriptor node) throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void visitTableGeneratorDescriptor(TableGeneratorDescriptor node)
            throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void visitEntityDescriptor(EntityDescriptor node)
            throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void visitSecondaryTableDescriptor(SecondaryTableDescriptor node)
            throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void visitColumnDescriptor(ColumnDescriptor node)
            throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void visitIdDescriptor(IdDescriptor node)
            throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void visitTableDescriptor(TableDescriptor node)
            throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void visitDiscriminatorColumnDescriptor(
            DiscriminatorColumnDescriptor node) throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void visitEmbeddedIdDescriptor(EmbeddedIdDescriptor node)
            throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void visitEmbeddedDescriptor(EmbeddedDescriptor node)
            throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void visitOneToManyDescriptor(OneToManyDescriptor node)
            throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void visitOneToOneDescriptor(OneToOneDescriptor node)
            throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void visitManyToOneDescriptor(ManyToOneDescriptor node)
            throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void visitManyToManyDescriptor(ManyToManyDescriptor node)
            throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void visitLobDescriptor(LobDescriptor node)
            throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void visitBasicDescriptor(BasicDescriptor node)
            throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void visitSerializedDescriptor(SerializedDescriptor node)
            throws DeploymentException {
    }

    /**
     * {@inheritDoc}
     */
    public void visitAttributeOverrideDescriptor(
            AttributeOverrideDescriptor node) throws DeploymentException {
    }
}
