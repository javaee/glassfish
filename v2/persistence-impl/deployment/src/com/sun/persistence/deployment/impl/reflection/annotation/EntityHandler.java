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


package com.sun.persistence.deployment.impl.reflection.annotation;

import com.sun.enterprise.deployment.annotation.*;
import com.sun.enterprise.deployment.annotation.impl.HandlerProcessingResultImpl;
import com.sun.persistence.api.deployment.*;
import com.sun.persistence.deployment.impl.PersistentPropertyIntrospector;
import com.sun.persistence.deployment.impl.PersistentPropertyIntrospectorFactoryImpl;
import com.sun.persistence.deployment.impl.reflection.FieldOrPropertyImpl;
import com.sun.persistence.spi.deployment.DeploymentUnitContext;

import java.lang.annotation.Annotation;

import static com.sun.persistence.deployment.impl.reflection.annotation.AnnotationToDescriptorConverter.convert;

/**
 * This class process all the annotations at class level as well as property
 * level for a class which is annotated as @Entity
 *
 * @author Sanjeeb.Sahoo@Sun.COM and Servesh.Singh.Singh@Sun.COM
 * @version 1.0
 */
public class EntityHandler extends HandlerBase {

    public EntityHandler(MergeManager mergeManager) {
        super(mergeManager);
    }

    public Class<? extends Annotation> getAnnotationType() {
        return javax.persistence.Entity.class;
    }

    public HandlerProcessingResult processAnnotation(AnnotationInfo element)
            throws AnnotationProcessorException {
        try {
            logger.fine(
                    i18NHelper.msg("MSG_EntityHandlerProcessAnnotation")); // NOI18N
            DeploymentUnit du = element.getProcessingContext().getHandler(
                    DeploymentUnitContext.class).getDeploymentUnit();
            // this annotation target type is TYPE, so we can cast it to class
            Class<?> ae = Class.class.cast(element.getAnnotatedElement());
            processAnnotation(ae, du);
            HandlerProcessingResultImpl resultImpl = HandlerProcessingResultImpl.getDefaultResult(
                    getAnnotationType(), ResultType.PROCESSED);
            return resultImpl;
        } catch (DeploymentException de) {
            AnnotationProcessorException ape = new AnnotationProcessorException(
                    i18NHelper.msg("EXC_AnnotationProcessing")); // NOI18N
            ape.initCause(de);
            throw ape;
        }
    }

    public ClassDescriptor processAnnotation(
            Class<?> javaClass,
            DeploymentUnit du)
            throws DeploymentException {
        PersistenceJarDescriptor persistenceJarDescriptor = du.getPersistenceJar();
        ClassDescriptor bean = persistenceJarDescriptor.getClassDescriptor(
                javaClass.getName());
        if (bean == null) {
            bean = of.createClassDescriptor();
            bean.setName(javaClass.getName());
            du.getPersistenceJar().getClassDescriptor().add(bean);
        }
        if (javaClass.isAnnotationPresent(javax.persistence.Entity.class)) {
            logger.fine(i18NHelper.msg(
                    "MSG_EntityAnnotationProcessing", // NOI18N
                    javaClass.getName()));
            javax.persistence.Entity annotation = javaClass.getAnnotation(
                    javax.persistence.Entity.class);
            EntityDescriptor entity = convert(annotation);
            mergeManager.mergeEntity(bean, entity);
        }
        if (javaClass.isAnnotationPresent(javax.persistence.NamedQueries.class)) {
            logger.fine(i18NHelper.msg(
                    "MSG_NamedQueriesAnnotationProcessing", // NOI18N
                    javaClass.getName()));
            mergeManager.mergeNamedQueries(bean,
                    convert(javaClass.getAnnotation(
                            javax.persistence.NamedQueries.class)));
        } else if (javaClass.isAnnotationPresent(
                javax.persistence.NamedQuery.class)) {
            logger.fine(i18NHelper.msg(
                    "MSG_NamedQueryAnnotationProcessing", // NOI18N
                    javaClass.getName()));
            mergeManager.mergeNamedQuery(bean,
                    convert(javaClass.getAnnotation(
                            javax.persistence.NamedQuery.class)));
        }
        if (javaClass.isAnnotationPresent(javax.persistence.Table.class)) {
            logger.fine(i18NHelper.msg(
                    "MSG_TableAnnotationProcessing", // NOI18N
                    javaClass.getName()));
            mergeManager.mergeTable(bean,
                    convert(javaClass.getAnnotation(
                            javax.persistence.Table.class)));
        }
        if (javaClass.isAnnotationPresent(
                javax.persistence.SecondaryTables.class)) {
            logger.fine(i18NHelper.msg(
                    "MSG_SecondaryTablesAnnotationProcessing", // NOI18N
                    javaClass.getName()));
            mergeManager.mergeSecondaryTables(bean,
                    convert(javaClass.getAnnotation(
                            javax.persistence.SecondaryTables.class)));
        } else if (javaClass.isAnnotationPresent(
                javax.persistence.SecondaryTable.class)) {
            logger.fine(i18NHelper.msg(
                    "MSG_SecondaryTableAnnotationProcessing", // NOI18N
                    javaClass.getName()));
            mergeManager.mergeSecondaryTable(bean,
                    convert(javaClass.getAnnotation(
                            javax.persistence.SecondaryTable.class)));
        }
        if (javaClass.isAnnotationPresent(javax.persistence.JoinColumns.class)) {
            logger.fine(i18NHelper.msg(
                    "MSG_JoinColumnsAnnotationProcessing", // NOI18N
                    javaClass.getName()));
            mergeManager.mergeJoinColumns(bean,
                    convert(javaClass.getAnnotation(
                            javax.persistence.JoinColumns.class)));
        } else if (javaClass.isAnnotationPresent(
                javax.persistence.JoinColumn.class)) {
            logger.fine(i18NHelper.msg(
                    "MSG_JoinColumnAnnotationProcessing", // NOI18N
                    javaClass.getName()));
            mergeManager.mergeJoinColumn(bean,
                    convert(javaClass.getAnnotation(
                            javax.persistence.JoinColumn.class)));
        }
        if (javaClass.isAnnotationPresent(javax.persistence.Inheritance.class)) {
            logger.fine(i18NHelper.msg(
                    "MSG_InheritanceAnnotationProcessing", // NOI18N
                    javaClass.getName()));
            mergeManager.mergeInheritance(bean,
                    convert(javaClass.getAnnotation(
                            javax.persistence.Inheritance.class)));
        }
        if (javaClass.isAnnotationPresent(
                javax.persistence.InheritanceJoinColumns.class)) {
            logger.fine(i18NHelper.msg(
                    "MSG_InheritanceJoinColumnsAnnotationProcessing", // NOI18N
                    javaClass.getName()));
            mergeManager.mergeInheritanceJoinColumns(bean,
                    convert(
                            javaClass.getAnnotation(
                                    javax.persistence.InheritanceJoinColumns.class)));
        } else if (javaClass.isAnnotationPresent(
                javax.persistence.InheritanceJoinColumn.class)) {
            logger.fine(i18NHelper.msg(
                    "MSG_InheritanceJoinColumnAnnotationProcessing", // NOI18N
                    javaClass.getName()));
            mergeManager.mergeInheritanceJoinColumn(bean,
                    convert(
                            javaClass.getAnnotation(
                                    javax.persistence.InheritanceJoinColumn.class)));
        }
        if (javaClass.isAnnotationPresent(
                javax.persistence.DiscriminatorColumn.class)) {
            logger.fine(i18NHelper.msg(
                    "MSG_DiscriminatorColumnAnnotationProcessing", // NOI18N
                    javaClass.getName()));
            mergeManager.mergeDiscriminatorColumn(bean,
                    convert(
                            javaClass.getAnnotation(
                                    javax.persistence.DiscriminatorColumn.class)));
        }
        if (javaClass.isAnnotationPresent(
                javax.persistence.GeneratedIdTable.class)) {
            logger.fine(i18NHelper.msg(
                    "MSG_GeneratedIdTableAnnotationProcessing", // NOI18N
                    javaClass.getName()));
            mergeManager.mergeGeneratedIdTable(bean,
                    convert(javaClass.getAnnotation(
                            javax.persistence.GeneratedIdTable.class)));
        }
        if (javaClass.isAnnotationPresent(
                javax.persistence.SequenceGenerator.class)) {
            logger.fine(i18NHelper.msg(
                    "MSG_SequenceGeneratorAnnotationProcessing", // NOI18N
                    javaClass.getName()));
            mergeManager.mergeSequenceGenerator(bean,
                    convert(javaClass.getAnnotation(
                            javax.persistence.SequenceGenerator.class)));
        }
        if (javaClass.isAnnotationPresent(
                javax.persistence.TableGenerator.class)) {
            logger.fine(i18NHelper.msg(
                    "MSG_TableGeneratorAnnotationProcessing", // NOI18N
                    javaClass.getName()));
            mergeManager.mergeTableGenerator(bean,
                    convert(javaClass.getAnnotation(
                            javax.persistence.TableGenerator.class)));
        }
        processPersistentProperties(javaClass, bean, du);
        return bean;
    }

    //processes all the field/method annotations for this class and adds them to the bean graph
    private void processPersistentProperties(
            Class<?> c,
            ClassDescriptor entityBeanClass,
            DeploymentUnit du)
            throws DeploymentException {
        FieldOrPropertyImpl[] pps;
        try {
            PersistentPropertyIntrospector introspector =
                    PersistentPropertyIntrospectorFactoryImpl.getInstance()
                    .getIntrospector(du.getJavaModel());
            pps = (FieldOrPropertyImpl[]) introspector.getPCProperties(
                    du.getJavaModel().getJavaType(c.getName()),
                    getAccessType(c));
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
        for (FieldOrPropertyImpl javaProperty : pps) {
            PropertyDescriptor bean = of.createPropertyDescriptor();
            bean.setName(javaProperty.getName());
            bean = mergeManager.mergeProperty(entityBeanClass, bean);
            MappingDescriptor mapping = getMapping(javaProperty);
            if (mapping != null) {
                mergeManager.mergeMapping(bean, mapping);
            }
            logger.fine(i18NHelper.msg(
                    "MSG_ProcessAnnotationPreoperty", // NOI18N
                    javaProperty.getName(), entityBeanClass.getName()));
            if (javaProperty.isAnnotationPresent(javax.persistence.Id.class)) {
                mergeManager.mergeId(bean,
                        convert(javaProperty.getAnnotation(
                                javax.persistence.Id.class)));
            }
            if (javaProperty.isAnnotationPresent(
                    javax.persistence.EmbeddedId.class)) {
                mergeManager.mergeEmbeddedId(bean,
                        convert(javaProperty.getAnnotation(
                                javax.persistence.EmbeddedId.class)));
            }
            if (javaProperty.isAnnotationPresent(
                    javax.persistence.Embedded.class)) {
                mergeManager.mergeEmbedded(bean,
                        convert(javaProperty.getAnnotation(
                                javax.persistence.Embedded.class)));
            }
            if (javaProperty.isAnnotationPresent(
                    javax.persistence.Version.class)) {
                bean.setVersion(true);
            }
            if (javaProperty.isAnnotationPresent(
                    javax.persistence.Column.class)) {
                mergeManager.mergeColumn(bean,
                        convert(javaProperty.getAnnotation(
                                javax.persistence.Column.class)));
            }
            if (javaProperty.isAnnotationPresent(
                    javax.persistence.AssociationTable.class)) {
                mergeManager.mergeAssociationTable(bean,
                        convert(
                                javaProperty.getAnnotation(
                                        javax.persistence.AssociationTable.class)));
            }
            if (javaProperty.isAnnotationPresent(
                    javax.persistence.NamedQueries.class)) {
                mergeManager.mergeNamedQueries(bean,
                        convert(javaProperty.getAnnotation(
                                javax.persistence.NamedQueries.class)));
            } else if (javaProperty.isAnnotationPresent(
                    javax.persistence.NamedQuery.class)) {
                mergeManager.mergeNamedQuery(bean,
                        convert(javaProperty.getAnnotation(
                                javax.persistence.NamedQuery.class)));
            }
            if (javaProperty.isAnnotationPresent(
                    javax.persistence.JoinColumns.class)) {
                mergeManager.mergeJoinColumns(bean,
                        convert(javaProperty.getAnnotation(
                                javax.persistence.JoinColumns.class)));
            } else if (javaProperty.isAnnotationPresent(
                    javax.persistence.JoinColumn.class)) {
                mergeManager.mergeJoinColumn(bean,
                        convert(javaProperty.getAnnotation(
                                javax.persistence.JoinColumn.class)));
            }
            if (javaProperty.isAnnotationPresent(
                    javax.persistence.SequenceGenerator.class)) {
                mergeManager.mergeSequenceGenerator(bean,
                        convert(
                                javaProperty.getAnnotation(
                                        javax.persistence.SequenceGenerator.class)));
            }
            if (javaProperty.isAnnotationPresent(
                    javax.persistence.TableGenerator.class)) {
                mergeManager.mergeTableGenerator(bean,
                        convert(
                                javaProperty.getAnnotation(
                                        javax.persistence.TableGenerator.class)));
            }
        }
    }

    // handles Basic, 1:1 1:n n:1 n:n Lob Serialized annotations
    // i.e. the following set {
    //  javax.persistence.Basic.class, javax.persistence.Serialized.class,
    //  javax.persistence.Lob.class, javax.persistence.OneToOne.class,
    //  javax.persistence.ManyToOne.class, javax.persistence.OneToMany.class,
    //  javax.persistence.ManyToMany.class
    // }
    private MappingDescriptor getMapping(FieldOrPropertyImpl ae) {
        if (ae.isAnnotationPresent(javax.persistence.Basic.class)) {
            BasicDescriptor basic = convert(
                    ae.getAnnotation(javax.persistence.Basic.class));
            return basic;
        }
        if (ae.isAnnotationPresent(javax.persistence.Serialized.class)) {
            SerializedDescriptor serialized = convert(
                    ae.getAnnotation(javax.persistence.Serialized.class));
            return serialized;
        }
        if (ae.isAnnotationPresent(javax.persistence.Lob.class)) {
            LobDescriptor lob = convert(
                    ae.getAnnotation(javax.persistence.Lob.class));
            return lob;
        }
        if (ae.isAnnotationPresent(javax.persistence.OneToOne.class)) {
            OneToOneDescriptor oneToOne = (OneToOneDescriptor) convert(
                    ae.getAnnotation(javax.persistence.OneToOne.class));
            return oneToOne;
        }
        if (ae.isAnnotationPresent(javax.persistence.ManyToOne.class)) {
            ManyToOneDescriptor manyToOne = (ManyToOneDescriptor) convert(
                    ae.getAnnotation(javax.persistence.ManyToOne.class));
            return manyToOne;
        }
        if (ae.isAnnotationPresent(javax.persistence.OneToMany.class)) {
            OneToManyDescriptor oneToMany = (OneToManyDescriptor) convert(
                    ae.getAnnotation(javax.persistence.OneToMany.class));
            return oneToMany;
        }
        if (ae.isAnnotationPresent(javax.persistence.ManyToMany.class)) {
            ManyToManyDescriptor manyToMany = (ManyToManyDescriptor) convert(
                    ae.getAnnotation(javax.persistence.ManyToMany.class));
            return manyToMany;
        }
        return null;
    }

    private static AccessType getAccessType(java.lang.Class<?> c) {
        javax.persistence.AccessType access;
        if (c.isAnnotationPresent(javax.persistence.Entity.class)) {
            access = c.getAnnotation(javax.persistence.Entity.class).access();
        } else if (c.isAnnotationPresent(javax.persistence.Embeddable.class)) {
            access =
                    c.getAnnotation(javax.persistence.Embeddable.class).access();
        } else {
            throw new RuntimeException(c + i18NHelper.msg(
                    "EXC_NeitherEntityNorEmbeddable")); // NOI18N
        }
        return AccessType.valueOf(access.toString());
    }

}
