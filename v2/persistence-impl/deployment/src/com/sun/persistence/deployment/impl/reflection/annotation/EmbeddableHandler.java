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
 * level for a class which is annotated as @Embedded
 *
 * @author Servesh Singh
 * @version 1.0
 */
public class EmbeddableHandler extends HandlerBase {

    public EmbeddableHandler(MergeManager mergeManager) {
        super(mergeManager);
    }

    /**
     * Creates a new instance of WebMethodHandler
     */
    public Class<? extends Annotation> getAnnotationType() {
        return javax.persistence.Embeddable.class;
    }

    public HandlerProcessingResult processAnnotation(AnnotationInfo element)
            throws AnnotationProcessorException {
        try {
            logger.fine(
                    i18NHelper.msg("MSG_EmbeddableHandlerProcessAnnotation")); // NOI18N
            DeploymentUnit du = element.getProcessingContext().getHandler(
                    DeploymentUnitContext.class)
                    .getDeploymentUnit();
            Class<?> ae = Class.class.cast(element.getAnnotatedElement());
            processAnnotation(ae, du);
            HandlerProcessingResultImpl resultImpl =
                    HandlerProcessingResultImpl.getDefaultResult(
                            getAnnotationType(), ResultType.PROCESSED);
            return resultImpl;
        } catch (DeploymentException de) {
            AnnotationProcessorException ape = new AnnotationProcessorException(
                    i18NHelper.msg("EXC_AnnotationProcessing")); // NOI18N
            ape.initCause(de);
            throw ape;
        }
    }

    public com.sun.persistence.api.deployment.ClassDescriptor processAnnotation(
            Class<?> javaClass, DeploymentUnit du)
            throws DeploymentException {
        PersistenceJarDescriptor persistenceJarDescriptor =
                du.getPersistenceJar();
        ClassDescriptor bean = persistenceJarDescriptor.getClassDescriptor(
                javaClass.getName());
        if (bean == null) {
            bean = of.createClassDescriptor();
            bean.setName(javaClass.getName());
            du.getPersistenceJar().getClassDescriptor().add(bean);
        }
        if (javaClass.isAnnotationPresent(javax.persistence.Embeddable.class))
            mergeManager.mergeEmbeddable(bean,
                    convert(javaClass.getAnnotation(
                            javax.persistence.Embeddable.class)));
        if (javaClass.isAnnotationPresent(javax.persistence.Table.class)) {
            mergeManager.mergeTable(bean,
                    convert(javaClass.getAnnotation(
                            javax.persistence.Table.class)));
        }
        processPersistentProperties(javaClass, bean, du);
        return bean;
    }

    //processes all the field/method annotations for this class and
    // adds them to the bean graph
    private void processPersistentProperties(
            Class<?> javaClass,
            ClassDescriptor entityBeanClass,
            DeploymentUnit du)
            throws DeploymentException {
        FieldOrPropertyImpl[] pps;
        try {
            PersistentPropertyIntrospector introspector =
                    PersistentPropertyIntrospectorFactoryImpl.getInstance()
                    .getIntrospector(du.getJavaModel());
            pps = (FieldOrPropertyImpl[]) introspector.getPCProperties(
                    du.getJavaModel().getJavaType(javaClass.getName()),
                    getAccessType(javaClass));
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
            if (javaProperty.isAnnotationPresent(
                    javax.persistence.Column.class)) {
                mergeManager.mergeColumn(bean,
                        convert(javaProperty.getAnnotation(
                                javax.persistence.Column.class)));
            }
        }
    }

    //handles Basic, Serialized, and Lob annotations,
    // as embeddable class can only use those mappings.
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
