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

import com.sun.org.apache.jdo.model.ModelException;
import com.sun.org.apache.jdo.model.java.JavaType;
import com.sun.org.apache.jdo.model.jdo.*;
import com.sun.persistence.api.deployment.*;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is responsible for mapping descriptor object graph
 * {@link DeploymentUnit} to {@link JDOModel}. This class takes a JDOModel
 * as input in {@link #map(DeploymentUnit, JDOModel)} ), because it does not
 * have the intelligence to figure out which JDOModel to instantiate.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class JDOModelMapper {
    /*
     * It uses an inner class {@link JDOClassMapper} that knows how to map
     * each class in DeploymentUnit to a corresponsind JDOClass.
     * This class operates in two phases:
     * a) in first phase, it creates all the JDOClasses and fields etc.
     * b) in second phase, we set the inverse relationship, because during
     * first pass, all JDOClasses may not be initialized.
     *
     * TODO:
     * 1) Process Transient node once that is available in PDOL
     * 2) Add supprt for embeddable types when such support is
     * available in JDOModel?
     * 3) Move setOwner and setDFG code to a common method once FetchType
     * is available in RelationshipMappingDescriptor.
     */

    /* The JDOModel that is getting processed in the current visitor context */
    private JDOModel jdoModel;

    /* The DeploymentUnit that is being mapped */
    private DeploymentUnit du;

    /* We set the inverse relationship in a second phase, because during
     * firs pass, all JDOClasses may not be initialized.
     * This member is used to collect all the bidirectional relationships.
     */
    private Map<RelationalMappingDescriptor, JDORelationship>
            nonOwningRelationships;

    /**
     * This method maps the DeploymentUnit to a JDOModel.
     * This is the only public interface of this class.
     * The result of this operation gets stored in the passed JDOModel.
     *
     * @param du       the deployment unit to be used for mapping
     * @param jdoModel the target JDOModel to be populated. Please note, this
     *                 method takes a JDOModel, rather than creating one
     *                 itself, because it does not have the intelligence to
     *                 figure out which JDOModel to instantiate.
     * @throws DeploymentException
     */
    public static void map(DeploymentUnit du, JDOModel jdoModel)
            throws DeploymentException {
        // create a new instance to do the work.
        new JDOModelMapper(du, jdoModel).map();
    }

    /**
     * Create a new JDOModelMapper.
     * Usage Note: Create one instance of JDOModelMapper per each invocation of
     * {@link #map()}.
     *
     * @param du       the deployment unit to be used for mapping
     * @param jdoModel the target JDOModel to be populated.
     */
    private JDOModelMapper(DeploymentUnit du, JDOModel jdoModel) {
        this.jdoModel = jdoModel;
        this.du = du;
    }

    /**
     * This method maps the DeploymentUnit to a JDOModel.
     * The result of this operation gets stored in the JDOModel
     * that was earlier passed to this class' constructor.
     *
     * @throws DeploymentException
     */
    private void map() throws DeploymentException {
        nonOwningRelationships =
                new HashMap<RelationalMappingDescriptor, JDORelationship>();
        for (ClassDescriptor c : du.getPersistenceJar().getClassDescriptor()) {
            // for each class, we need a new JDOClassMapper as that is a
            // stateful object and can not be reused.
            c.accept(new TreeWalker(new JDOClassMapper()));
        }

        // Let's set owners for those who were skipped earlier.
        setOwners();
    }

    private void setOwners() throws DeploymentException {
        for (RelationalMappingDescriptor relationship :
                nonOwningRelationships.keySet()) {
            RelationalMappingDescriptor inverseRelationship =
                    relationship.getInverse();
            // since this list contains only bidirectionals, hence this assert.
            assert(inverseRelationship != null);
            PropertyDescriptor inverseProperty = inverseRelationship.parent();
            ClassDescriptor inverseClass = inverseProperty.parent();
            JDOClass inverseJDOClass =
                    jdoModel.getJDOClass(inverseClass.getName());
            JDOField inverseJDOField =
                    inverseJDOClass.getField(inverseProperty.getName());
            JDORelationship inverseJDORelationship =
                    inverseJDOField.getRelationship();
            try {
                nonOwningRelationships.get(relationship)
                        .setMappedBy(inverseJDORelationship);
            } catch (ModelException e) {
                throw new DeploymentException(e);
            }
        }
    }

    // inner class that does all the mapping
    class JDOClassMapper extends EmptyVisitor {
        /* The JDOClass that is getting processed in the current context */
        private JDOClass jdoClass;

        /* The JdoField that is getting processed in the current context */
        private JDOField jdoField;

        /* Indicates whether to create JDOProperty or JDOField */
        private AccessType accessType;

        @Override public void visitClassDescriptor(ClassDescriptor node)
                throws DeploymentException {
            try {
                jdoClass = jdoModel.createJDOClass(node.getName());
            } catch (ModelException e) {
                new DeploymentException(e);
            }
        }

        @Override public void visitEntityDescriptor(EntityDescriptor node)
                throws DeploymentException {
            try {
                jdoClass.setShortName(node.getName());
                accessType = node.getAccess();
            } catch (ModelException e) {
                throw new DeploymentException(e);
            }
        }

        // TODO: Add code here when we have IdClass support in PDOL
        // IdClass is not yet supported in persistence_ORM.xsd.
//        @Override public void visitIdClassDescriptor(IdClassDescriptor node)
//                throws DeploymentException {
//        }

        @Override public void visitPropertyDescriptor(PropertyDescriptor node)
                throws DeploymentException {
            try {
                switch (accessType) {
                    case FIELD:
                        jdoField = jdoClass.createJDOField(node.getName());
                        break;
                    case PROPERTY:
                        jdoField = jdoClass.createJDOProperty(node.getName());
                        break;
                }
                // This can be overriden when we visit a Transient node.
                jdoField.setPersistenceModifier(PersistenceModifier.PERSISTENT);
            } catch (ModelException e) {
                throw new DeploymentException(e);
            }
        }

        @Override public void visitIdDescriptor(IdDescriptor node)
                throws DeploymentException {
            try {
                // primary key fields are not in the default fetch group =>
                // explicitly set it to false here to overwrite a previous
                // setting from visitBasicDescriptor
                jdoField.setDefaultFetchGroup(false);
                jdoField.setPrimaryKey(true);
            } catch (ModelException e) {
                throw new DeploymentException(e);
            }
        }

        @Override public void visitEmbeddedIdDescriptor(
                EmbeddedIdDescriptor node)
                throws DeploymentException {
            try {
                jdoField.setEmbedded(true);
                // primary key fields are not in the default fetch group =>
                // explicitly set it to false here to overwrite a previous
                // setting from visitBasicDescriptor
                jdoField.setDefaultFetchGroup(false);
                jdoField.setPrimaryKey(true);
            } catch (ModelException e) {
                throw new DeploymentException(e);
            }
        }

        @Override public void visitEmbeddedDescriptor(EmbeddedDescriptor node)
                throws DeploymentException {
            try {
                jdoField.setEmbedded(true);
            } catch (ModelException e) {
                throw new DeploymentException(e);
            }
        }

        @Override public void visitOneToOneDescriptor(OneToOneDescriptor node)
                throws DeploymentException {
            try {
                JDOReference jdoReference = jdoField.createJDOReference();
                setOwner(jdoReference, node);
                jdoField.setDefaultFetchGroup(
                        node.getFetch() == FetchType.EAGER);
            } catch (ModelException e) {
                throw new DeploymentException(e);
            }
        }

        @Override public void visitManyToOneDescriptor(
                ManyToOneDescriptor node)
                throws DeploymentException {
            visitOneToOneDescriptor(node); // rules are same as OneToOne
        }

        @Override public void visitOneToManyDescriptor(
                OneToManyDescriptor node)
                throws DeploymentException {
            try {
                JDOCollection jdoCollection = jdoField.createJDOCollection();
                JavaType elementType = jdoModel.getJavaModel().
                        getJavaType(node.getTargetEntity());
                jdoCollection.setElementType(elementType);
                setOwner(jdoCollection, node);
                jdoField.setDefaultFetchGroup(
                        node.getFetch() == FetchType.EAGER);

                // TODO: Do we have to set lower and upper bound?

                // EJB 3.0 spec does not yet support collection of embeddable types
                jdoCollection.setEmbeddedElement(false);
            } catch (ModelException e) {
                throw new DeploymentException(e);
            }
        }

        @Override public void visitManyToManyDescriptor(
                ManyToManyDescriptor node)
                throws DeploymentException {
            visitOneToManyDescriptor(node); // rules are same as OneToMany
        }

        @Override public void visitLobDescriptor(LobDescriptor node)
                throws DeploymentException {
            try {
                // TODO: Should there be a diffenet attribute in JDOField for Lob type?
                jdoField.setSerializable(true);
                jdoField.setDefaultFetchGroup(
                        node.getFetch() == FetchType.EAGER);
            } catch (ModelException e) {
                throw new DeploymentException(e);
            }
        }

        @Override public void visitBasicDescriptor(BasicDescriptor node)
                throws DeploymentException {
            try {
                jdoField.setDefaultFetchGroup(
                        node.getFetch() == FetchType.EAGER);
            } catch (ModelException e) {
                throw new DeploymentException(e);
            }
        }

        @Override public void visitSerializedDescriptor(
                SerializedDescriptor node)
                throws DeploymentException {
            try {
                jdoField.setSerializable(true);
                jdoField.setDefaultFetchGroup(
                        node.getFetch() == FetchType.EAGER);
            } catch (ModelException e) {
                throw new DeploymentException(e);
            }
        }

        /**
         * Set owning relationship in the JDORelationship if applicable.
         * A relation is said to have an owner if its mappedBy field is set.
         * The field/property name specified in mappedBy is the owner.
         * In this method, we just collect all the relationships whose
         * owner needs to be set. We set the owner in a second phase
         * because in this phase, all JDOClasses may not be initialized.
         *
         * @param jdoRelationship in which the owner will be set, if applicable.
         * @param node            descriptor node which provides owner
         *                        relationship information from descriptor.
         */
        private void setOwner(
                JDORelationship jdoRelationship,
                RelationalMappingDescriptor node) {
            if (!node.isOwningSide()) {
                assert(nonOwningRelationships.get(node) == null); // not yet visited
                nonOwningRelationships.put(node, jdoRelationship);
            }
        }
    } // end of JDOClassMapper

}
