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
 * TreeWalker.java
 *
 * Created on March 18, 2005, 3:09 PM
 */


package com.sun.persistence.api.deployment;

/**
 * This class supplies the traversal strategy over a descriptor object graph.
 * Other visitors make use of it. Code snippet below shows how to use this class
 * to traverse an object graph,
 * <p> <blockquote><pre>
 *     SomeVisitor someVisitor;
 *     PersistenceJarDescriptor persistenceJarDescriptor;
 *     persistenceJarDescriptor.accept(new TreeWalker(someVisitor));
 * </pre></blockquote>
 *
 * @author Sanjeeb Sahoo
 * @version 1.0
 */
public class TreeWalker extends EmptyVisitor {

    /* the visitor which supplies actual code for visit method */
    private Visitor piggyBackedVisitor;

    /**
     * Create a new TreeWalker.
     *
     * @param piggyBackedVisitor whose visit method will be called
     */
    public TreeWalker(Visitor piggyBackedVisitor) {
        this.piggyBackedVisitor = piggyBackedVisitor;
    }

    /**
     * {@inheritDoc}
     */
    public void visitPersistenceJarDescriptor(PersistenceJarDescriptor pjt)
            throws DeploymentException {
        pjt.accept(piggyBackedVisitor);
        for (ClassDescriptor o : pjt.getClassDescriptor()) {
            o.accept(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void visitClassDescriptor(ClassDescriptor c)
            throws DeploymentException {
        c.accept(piggyBackedVisitor);
        DescriptorNode o;
        if ((o = c.getEntity()) != null) {
            o.accept(this);
        }
        if ((o = c.getEmbeddable()) != null) {
            o.accept(this);
        }
        if ((o = c.getTable()) != null) {
            o.accept(this);
        }
        for (DescriptorNode n : c.getSecondaryTable()) {
            n.accept(this);
        }
        for (DescriptorNode n : c.getJoinColumn()) {
            n.accept(this);
        }
        for (DescriptorNode n : c.getNamedQuery()) {
            n.accept(this);
        }
        if ((o = c.getInheritance()) != null) {
            o.accept(this);
        }
        for (DescriptorNode n : c.getInheritanceJoinColumn()) {
            n.accept(this);
        }
        if ((o = c.getDiscriminatorColumn()) != null) {
            o.accept(this);
        }
        if ((o = c.getGeneratedIdTable()) != null) {
            o.accept(this);
        }
        if ((o = c.getSequenceGenerator()) != null) {
            o.accept(this);
        }
        if ((o = c.getTableGenerator()) != null) {
            o.accept(this);
        }
        
        /*
         * Visit the properties after visiting other class level nodes 
         * like table, secondary table etc.
         */
        for (DescriptorNode n : c.getProperty()) {
            n.accept(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void visitEntityDescriptor(EntityDescriptor e)
            throws DeploymentException {
        e.accept(piggyBackedVisitor);
    }

    /**
     * {@inheritDoc}
     */
    public void visitEmbeddableDescriptor(EmbeddableDescriptor e)
            throws DeploymentException {
        e.accept(piggyBackedVisitor);
    }

    /**
     * {@inheritDoc}
     */
    public void visitPropertyDescriptor(PropertyDescriptor p)
            throws DeploymentException {
        p.accept(piggyBackedVisitor);
        DescriptorNode o;
        if ((o = p.getMapping()) != null) {
            o.accept(this);
        }
        /* 
         * Visit the id and embedded id after visiting the mapping. 
         * The id might need to override some settings from the basic
         * annotation.
         */
        if ((o = p.getId()) != null) {
            o.accept(this);
        }
        if ((o = p.getEmbeddedId()) != null) {
            o.accept(this);
        }
        if ((o = p.getEmbedded()) != null) {
            o.accept(this);
        }
        if ((o = p.getColumn()) != null) {
            o.accept(this);
        }
        for (DescriptorNode n : p.getJoinColumn()) {
            n.accept(this);
        }
        if ((o = p.getAssociationTable()) != null) {
            o.accept(this);
        }
        for (DescriptorNode n : p.getNamedQuery()) {
            n.accept(this);
        }
        if ((o = p.getSequenceGenerator()) != null) {
            o.accept(this);
        }
        if ((o = p.getTableGenerator()) != null) {
            o.accept(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void visitTableDescriptor(TableDescriptor e)
            throws DeploymentException {
        e.accept(piggyBackedVisitor);
        for (DescriptorNode n : e.getUniqueConstraint()) {
            n.accept(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void visitSecondaryTableDescriptor(SecondaryTableDescriptor e)
            throws DeploymentException {
        e.accept(piggyBackedVisitor);
        for (DescriptorNode n : e.getJoin()) {
            n.accept(this);
        }
        for (DescriptorNode n : e.getUniqueConstraint()) {
            n.accept(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void visitColumnDescriptor(ColumnDescriptor e)
            throws DeploymentException {
        e.accept(piggyBackedVisitor);
    }

    /**
     * {@inheritDoc}
     */
    public void visitJoinColumnDescriptor(JoinColumnDescriptor e)
            throws DeploymentException {
        e.accept(piggyBackedVisitor);
    }

    /**
     * {@inheritDoc}
     */
    public void visitNamedQueryDescriptor(NamedQueryDescriptor e)
            throws DeploymentException {
        e.accept(piggyBackedVisitor);
    }

    /**
     * {@inheritDoc}
     */
    public void visitInheritanceDescriptor(InheritanceDescriptor e)
            throws DeploymentException {
        e.accept(piggyBackedVisitor);
    }

    /**
     * {@inheritDoc}
     */
    public void visitInheritanceJoinColumnDescriptor(
            InheritanceJoinColumnDescriptor e) throws DeploymentException {
        e.accept(piggyBackedVisitor);
    }

    /**
     * {@inheritDoc}
     */
    public void visitDiscriminatorColumnDescriptor(
            DiscriminatorColumnDescriptor e) throws DeploymentException {
        e.accept(piggyBackedVisitor);
    }

    /**
     * {@inheritDoc}
     */
    public void visitGeneratedIdTableDescriptor(GeneratedIdTableDescriptor e)
            throws DeploymentException {
        e.accept(piggyBackedVisitor);
        DescriptorNode o;
        if ((o = e.getTable()) != null) {
            o.accept(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void visitSequenceGeneratorDescriptor(
            SequenceGeneratorDescriptor e)
            throws DeploymentException {
        e.accept(piggyBackedVisitor);
    }

    /**
     * {@inheritDoc}
     */
    public void visitTableGeneratorDescriptor(TableGeneratorDescriptor e)
            throws DeploymentException {
        e.accept(piggyBackedVisitor);
    }

    /**
     * {@inheritDoc}
     */
    public void visitEmbeddedIdDescriptor(EmbeddedIdDescriptor e)
            throws DeploymentException {
        e.accept(piggyBackedVisitor);
        for (DescriptorNode n : e.getValue()) {
            n.accept(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void visitUniqueConstraintDescriptor(UniqueConstraintDescriptor e)
            throws DeploymentException {
        e.accept(piggyBackedVisitor);
    }

    /**
     * {@inheritDoc}
     */
    public void visitIdDescriptor(IdDescriptor e) throws DeploymentException {
        e.accept(piggyBackedVisitor);
    }

    /**
     * {@inheritDoc}
     */
    public void visitEmbeddedDescriptor(EmbeddedDescriptor e)
            throws DeploymentException {
        e.accept(piggyBackedVisitor);
        for (DescriptorNode n : e.getValue()) {
            n.accept(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void visitAssociationTableDescriptor(AssociationTableDescriptor e)
            throws DeploymentException {
        e.accept(piggyBackedVisitor);
        DescriptorNode o;
        if ((o = e.getTable()) != null) {
            o.accept(this);
        }
        for (DescriptorNode n : e.getJoinColumn()) {
            n.accept(this);
        }
        for (DescriptorNode n : e.getInverseJoinColumn()) {
            n.accept(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void visitOneToManyDescriptor(OneToManyDescriptor e)
            throws DeploymentException {
        e.accept(piggyBackedVisitor);
    }

    /**
     * {@inheritDoc}
     */
    public void visitOneToOneDescriptor(OneToOneDescriptor e)
            throws DeploymentException {
        e.accept(piggyBackedVisitor);
    }

    /**
     * {@inheritDoc}
     */
    public void visitManyToOneDescriptor(ManyToOneDescriptor e)
            throws DeploymentException {
        e.accept(piggyBackedVisitor);
    }

    /**
     * {@inheritDoc}
     */
    public void visitManyToManyDescriptor(ManyToManyDescriptor e)
            throws DeploymentException {
        e.accept(piggyBackedVisitor);
    }

    /**
     * {@inheritDoc}
     */
    public void visitLobDescriptor(LobDescriptor e) throws DeploymentException {
        e.accept(piggyBackedVisitor);
    }

    /**
     * {@inheritDoc}
     */
    public void visitBasicDescriptor(BasicDescriptor e)
            throws DeploymentException {
        e.accept(piggyBackedVisitor);
    }

    /**
     * {@inheritDoc}
     */
    public void visitSerializedDescriptor(SerializedDescriptor e)
            throws DeploymentException {
        e.accept(piggyBackedVisitor);
    }
}
