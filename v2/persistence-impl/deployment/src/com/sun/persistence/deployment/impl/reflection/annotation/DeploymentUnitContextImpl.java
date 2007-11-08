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
 * DeploymentUnitContextImpl.java
 *
 * Created on March 24, 2005, 2:14 PM
 */


package com.sun.persistence.deployment.impl.reflection.annotation;

import com.sun.enterprise.deployment.annotation.context.AnnotationContext;
import com.sun.persistence.api.deployment.DeploymentUnit;
import com.sun.persistence.spi.deployment.DeploymentUnitContext;

/**
 * This is a persistence context which holds the reference to descriptor tree
 *
 * @author Sanjeeb Sahoo
 * @version 1.0
 */
public class DeploymentUnitContextImpl extends AnnotationContext
        implements DeploymentUnitContext {

    private DeploymentUnit du;

    /**
     * Creates a new instance of DeploymentUnitContext
     */
    public DeploymentUnitContextImpl(DeploymentUnit du) {
        this.du = du;
    }

    public DeploymentUnit getDeploymentUnit() {
        return du;
    }

}
