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
import com.sun.persistence.api.deployment.MergeConflictResolver;
import com.sun.persistence.api.deployment.MergeManager;
import com.sun.persistence.deployment.impl.MergeConflictResolverImpl;
import com.sun.persistence.deployment.impl.MergeManagerImpl;

import javax.persistence.*;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a repository of all the handlers that are used during
 * annotation processing of EJB 3.0 Persistence API.
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class AnnotationHandlerRepository {

    private static AnnotationHandler[] annotationHandlers;

    static {
        final java.lang.Class[] annotationArray = {
            Table.class,
            SecondaryTables.class,
            SecondaryTable.class,
            JoinColumns.class,
            JoinColumn.class,
            Inheritance.class,
            InheritanceJoinColumns.class,
            InheritanceJoinColumn.class,
            DiscriminatorColumn.class,
            Id.class,
            EmbeddedId.class,
            Embedded.class,
            Version.class,
            Column.class,
            AssociationTable.class,
            Basic.class,
            Serialized.class,
            Lob.class,
            OneToOne.class,
            OneToMany.class,
            ManyToOne.class,
            ManyToMany.class};
        List<AnnotationHandler> handlerList = new ArrayList<AnnotationHandler>();
        MergeConflictResolver vh = new MergeConflictResolverImpl();
        MergeManager mergeMgr = new MergeManagerImpl(vh);
        handlerList.add(new EntityHandler(mergeMgr));
        handlerList.add(new EmbeddableHandler(mergeMgr));
        handlerList.add(new NamedQueryHandler(mergeMgr));
        handlerList.add(new NamedQueriesHandler(mergeMgr));
        handlerList.add(new SequenceGeneratorHandler(mergeMgr));
        handlerList.add(new TableGeneratorHandler(mergeMgr));
        handlerList.add(new GeneratedIdTableHandler(mergeMgr));
        for (int i = 0; i < annotationArray.length; i++) {
            final java.lang.Class cl = annotationArray[i];
            AnnotationHandler ah = new AnnotationHandler() {
                public java.lang.Class<? extends Annotation> getAnnotationType() {
                    return cl;
                }

                public HandlerProcessingResult processAnnotation(
                        AnnotationInfo element) {
                    return HandlerProcessingResultImpl.getDefaultResult(
                            getAnnotationType(), ResultType.PROCESSED);
                }

                public Class<? extends Annotation>[] getTypeDependencies() {
                    return null;
                }
            };
            handlerList.add(ah);
        }
        annotationHandlers = handlerList.toArray(new AnnotationHandler[0]);
    }

    /**
     * This is used by DOL in SJSAS.
     * DOL takes these handlers, registers with its annotation processor.
     * Then annotation processor calls these handlers when it encounters
     * the EJB 3.0 Persistence API annotations.
     *
     * @return a list of {@link AnnotationHandler},
     *         which DOL registers with the {@link AnnotationProcessor}
     *         along with other handlers.
     */
    public static AnnotationHandler[] getAnnotationHandlers() {
        return annotationHandlers;
    }

}
