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
 * DeploymentUnitContext.java
 *
 * Created on March 7, 2005, 9:20 AM
 */


package com.sun.persistence.spi.deployment;

import com.sun.enterprise.deployment.annotation.AnnotatedElementHandler;
import com.sun.persistence.api.deployment.DeploymentUnit;

/**
 * This context holds the reference to deployment unit that is getting built by
 * the annotation processor. We register handlers with annotation processor for
 * annotation types of our interest. While parsing the classe in jar file, when
 * annotation processor comes across such annotations, it calls back our
 * annotation handler with an {@link com.sun.enterprise.deployment.annotation.AnnotationInfo}.
 * This AnnotationInfo object encapsulates two things, viz: a) an {@link
 * java.lang.reflect.AnnotatedElement} -- this provides the actual annotation
 * encountered. b) a {@link DeploymentUnitContext} -- this provides the context
 * for processing the annotation.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 * @see AnnotatedElementHandler
 */
public interface DeploymentUnitContext extends AnnotatedElementHandler {
    public DeploymentUnit getDeploymentUnit();
}
