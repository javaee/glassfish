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
 * LogicalModelDefaultValueSupplier.java
 *
 * Created on March 22, 2005, 1:57 PM
 */


package com.sun.persistence.deployment.impl;

import com.sun.org.apache.jdo.util.I18NHelper;
import com.sun.persistence.api.deployment.*;
import com.sun.persistence.api.deployment.JavaModel.FieldOrProperty;
import com.sun.persistence.utility.JavaTypeHelper;
import com.sun.persistence.utility.StringHelper;

import java.io.Serializable;
import java.sql.Blob;
import java.sql.Clob;

/**
 * This class is responsible for populating default values in the logical model.
 * The persistence-api spec allows many descriptors (both in annotations as well
 * as XML DD) to be left unspecified. There are well defined rules to set
 * default values for them. e.g. if entity name is not set, the default value is
 * "unqualified name of the Java class". There are two kinds of models, viz: a)
 * logical model -- this models the object model of the entities and their
 * relationship. b) Relational model -- this specifies the relational schema the
 * logical model is mapped to. The persistence-api spec is very carefully
 * designed to separate these two models allowing non-relational store to be
 * used as well for a given entity model.
 *
 * @author Sanjeeb Sahoo
 * @version 1.0
 * @see #populateEntityModelDefaultValues()
 */
public class LogicalModelDefaultValueSupplier extends EmptyVisitor {
    /*
     * TODO
     * a) Error Checking: replace assert by affirm or something similar.
     * b) Servicability: add better logging.
     * c) Optimization: Replace TreeWalker by a specialised iterator
     * which iterates only over the entity model only. That way we only visit
     * nodes of our interest.
     */
    /* This class is itself a Visitor so that it can make use of
     * TreeWalker which provides traversal logic over the descriptor graph.
     */

    private DeploymentUnit du;

    private ObjectFactory of = new ObjectFactory();

    private static final I18NHelper i18NHelper = I18NHelper.getInstance(
            LogHelperDeployment.class);

    /**
     * Creates a new instance of LogicalModelDefaultValueSupplier
     *
     * @param du the deployment unit which will be populated with default
     *           values
     */
    public LogicalModelDefaultValueSupplier(DeploymentUnit du) {
        this.du = du;
    }

    /**
     * This method is responsible for putting default values. Repeated
     * invocation of this method will not change the deployment unit.
     *
     * @throws DeploymentException
     */
    public void populateEntityModelDefaultValues() throws DeploymentException {
        LogHelperDeployment.getLogger().fine(
                i18NHelper.msg("MSG_PopulatingDefaultsForEntityModel")); // NOI18N
        du.getPersistenceJar().accept(new TreeWalker(this));
    }

    @Override public void visitEntityDescriptor(EntityDescriptor entity)
            throws DeploymentException {
        try {
            ClassDescriptor c = entity.parent();
            addMissingProperties(c);
            String clsName = c.getName();
            assert(clsName.length() != 0);
            if (StringHelper.isEmpty(entity.getName())) {
                entity.setName(JavaTypeHelper.getShortClassName(clsName));
            }
            if (entity.getAccess() == null && entity.getVersion() >= 3) {
                entity.setAccess(AccessType.PROPERTY);
            }
            if (entity.getEntityType() == null && entity.getVersion() >= 3) {
                entity.setEntityType(EntityType.CMP);
            }
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }

    @Override public void visitEmbeddableDescriptor(EmbeddableDescriptor node)
            throws DeploymentException {
        try {
            ClassDescriptor c = node.parent();
            addMissingProperties(c);
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }

    private void addMissingProperties(ClassDescriptor classDescriptor)
            throws Exception {
        FieldOrProperty[] javaProperties = getPCProperties(classDescriptor);
        for (int i = 0; i < javaProperties.length; ++i) {
            String propertyName = javaProperties[i].getName();
            for (PropertyDescriptor propertyDescriptor : classDescriptor.getProperty()) {
                if (propertyName.equals(propertyDescriptor.getName())) {
                    // null entries which are common
                    javaProperties[i] = null;
                    break;
                }
            }
        }
        for (FieldOrProperty prop : javaProperties) {
            if (prop == null) {
                // this property already exist in classDescriptor
                continue;
            }
            PropertyDescriptor p = of.createPropertyDescriptor();
            p.setName(prop.getName());
            //the rest of the entries wil be populated when we visit this property.
            classDescriptor.getProperty().add(p);
        }
    }

    @Override public void visitPropertyDescriptor(PropertyDescriptor p)
            throws DeploymentException {
        LogHelperDeployment.getLogger().entering(this.getClass().getName(),
                "visitPropertyDescriptor",  // NOI18N
                new Object[]{p.getName(),
                             p.parent().getName()});
        try {
            assert(!StringHelper.isEmpty(p.getName()));
            //decide if it is an id
            if (p.getId() == null && p.getEmbeddedId() == null &&
                    isMemberOfIdClass(p)) {
                p.setId(of.createIdDescriptor());
            }
            if (p.getEmbedded() == null && p.getMapping() == null) {
                createNonRelationalMapping(p);
            }
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }

    /**
     * Follows rules specified in section #2.1.6 of EJB 3.0 Persistence-API
     * spec
     *
     * @param property
     * @throws Exception
     */
    private void createNonRelationalMapping(PropertyDescriptor property)
            throws Exception {
        ClassDescriptor declaringClassDescriptor = property.parent();
        JavaModel javaModel = du.getJavaModel();
        Object propertyType = getJavaType(property);
        if (javaModel.isBasic(propertyType)) {
            BasicDescriptor basic = of.createBasicDescriptor();
            basic.setFetch(FetchType.EAGER);
            property.setMapping(basic);
            return;
        } else if (declaringClassDescriptor.isEmbeddable()) {
            property.setEmbedded(of.createEmbeddedDescriptor());
            return;
        } else if (javaModel.isAssignable(
                javaModel.getJavaType(Clob.class.getName()), propertyType)) {
            LobDescriptor lob = of.createLobDescriptor();
            lob.setType(LobType.CLOB);
            lob.setFetch(FetchType.LAZY);
            property.setMapping(lob);
        } else if (javaModel.isAssignable(
                javaModel.getJavaType(Blob.class.getName()), propertyType)) {
            LobDescriptor lob = of.createLobDescriptor();
            lob.setType(LobType.BLOB);
            lob.setFetch(FetchType.LAZY);
            property.setMapping(lob);
        } else if (javaModel.isAssignable(
                javaModel.getJavaType(Serializable.class.getName()),
                propertyType)) {
            SerializedDescriptor serialized = of.createSerializedDescriptor();
            serialized.setFetch(FetchType.EAGER);
            property.setMapping(serialized);
        } else {
            // Should this be caught by verifier in an earlier step?
            throw new DeploymentException(
                    i18NHelper.msg("EXC_NotAbleToDefaultMappingForProperty", // NOI18N
                            property.getName(),
                            declaringClassDescriptor.getName()));
        }
    }

    @Override public void visitOneToManyDescriptor(OneToManyDescriptor m)
            throws DeploymentException {
        visitRelationalMapping(m);
    }

    @Override public void visitOneToOneDescriptor(OneToOneDescriptor m)
            throws DeploymentException {
        visitRelationalMapping(m);
    }

    @Override public void visitManyToOneDescriptor(ManyToOneDescriptor m)
            throws DeploymentException {
        visitRelationalMapping(m);
    }

    @Override public void visitManyToManyDescriptor(ManyToManyDescriptor m)
            throws DeploymentException {
        visitRelationalMapping(m);
    }

    private void visitRelationalMapping(RelationalMappingDescriptor m)
            throws DeploymentException {
        try {
            PropertyDescriptor p = m.parent();
            if (StringHelper.isEmpty(m.getTargetEntity())) {
                m.setTargetEntity(getTargetEntityForRelationship(p)
                        .getName());
            }
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }

    private ClassDescriptor getTargetEntityForRelationship(
            PropertyDescriptor propertyDescriptor)
            throws Exception {
        ClassDescriptor c = propertyDescriptor.parent();
        String propName = propertyDescriptor.getName();
        JavaModel javaModel = du.getJavaModel();
        FieldOrProperty javaProperty = getPCProperty(propertyDescriptor);
        Object propertyType = javaProperty.getJavaType();
        if (javaModel.isAssignable(
                javaModel.getJavaType(java.util.Collection.class.getName()),
                propertyType)) {
            Object componentType = javaModel.getCollectionComponentType(
                    javaProperty);
            if (componentType == null) {
                throw new DeploymentException(i18NHelper.msg(
                        "EXC_NonGenerifiedCollectionRequireTargetEntityName", // NOI18N
                        propName, c.getName()));
            }
            return c.parent().getClassDescriptor(
                    javaModel.getName(componentType));
        } else {
            return c.parent().getClassDescriptor(
                    javaModel.getName(propertyType));
        }
    }

    /**
     * Decide if a property is also a member of IdClass (if any) of its
     * declaring class.
     *
     * @param property which will be looked up in its declaring class's IdClass
     * @return true if such a property exist in IdClass, else false.
     * @throws Exception to indicate something unexpected happened.
     */
    private boolean isMemberOfIdClass(PropertyDescriptor property)
            throws Exception {
        return false; // TODO see comments below
//        to be replaced by following code once support for IdClass in schema.
//        ClassDescriptor declaringClass = property.parent();
//        ClassDescriptor idClassDescriptor = null;
//        AccessType idClassAccessType = AccessType.PROPERTY;
//        FieldOrProperty fieldOrPropertyInIdClass = du.getJavaModel().getProperty(idClassDescriptor.getName(), idClassAccessType, property.getName())
//        return  fieldOrPropertyInIdClass != null;
    }

    /**
     * @param property whose JavaType information is required
     * @return the javaType of this property.
     * @throws ClassNotFoundException
     */
    private Object getJavaType(PropertyDescriptor property)
            throws Exception {
        return getPCProperty(property).getJavaType();
    }

    private FieldOrProperty[] getPCProperties(ClassDescriptor c)
            throws Exception {
        AccessType accessType = AccessType.PROPERTY;
        if(c.isEntity()) {
            accessType = c.getEntity().getAccess();
        } else if(c.isEmbeddable()) {
            accessType = c.getEmbeddable().getAccess();
        } else {
            assert(false); // not sure why we should reach here.
        }
        return PersistentPropertyIntrospectorFactoryImpl.getInstance()
                .getIntrospector(du.getJavaModel())
                .getPCProperties(du.getJavaModel().getJavaType(c.getName()),
                        accessType);
    }

    private FieldOrProperty getPCProperty(PropertyDescriptor p)
            throws Exception {
        ClassDescriptor c = p.parent();
        return PersistentPropertyIntrospectorFactoryImpl.getInstance()
                .getIntrospector(du.getJavaModel())
                .getPCProperty(du.getJavaModel().getJavaType(c.getName()),
                        c.getEntity().getAccess(), p.getName());
    }

}
