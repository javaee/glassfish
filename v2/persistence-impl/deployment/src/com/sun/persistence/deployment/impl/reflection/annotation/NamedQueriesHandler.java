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

import com.sun.enterprise.deployment.annotation.AnnotationHandler;
import com.sun.persistence.api.deployment.DeploymentUnit;
import com.sun.persistence.api.deployment.MergeManager;
import com.sun.persistence.api.deployment.NamedQueryDescriptor;
import com.sun.persistence.api.deployment.PersistenceJarDescriptor;

import java.lang.annotation.Annotation;

import static com.sun.persistence.deployment.impl.reflection.annotation.AnnotationToDescriptorConverter.convert;

/**
 * This process NamedQueries annotation at package level
 *
 * @author Servesh Singh
 * @version 1.0
 */
public class NamedQueriesHandler extends NamedQueryHandler {

    public NamedQueriesHandler(MergeManager mergeManager) {
        super(mergeManager);
    }

    public Class<? extends Annotation> getAnnotationType() {
        return javax.persistence.NamedQueries.class;
    }

    protected void processAnnotation(Package pkg, DeploymentUnit du) {
        PersistenceJarDescriptor pjar = du.getPersistenceJar();
        if (pkg.isAnnotationPresent(javax.persistence.NamedQueries.class)) {
            NamedQueryDescriptor[] namedQueries = convert(
                    pkg.getAnnotation(javax.persistence.NamedQueries.class));
            for (int i = 0; i < namedQueries.length; i++) {
                pjar.getNamedQuery().add(namedQueries[i]);
            }
        }
    }
}
