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
 * DeploymentUnit.java
 *
 * Created on March 21, 2005, 5:56 PM
 */


package com.sun.persistence.api.deployment;

/**
 * This interface represents a unit of deployment. It encapsulates a descriptor
 * object graph {@link #getPersistenceJar()} as well as the JavaModel. It can
 * represent a deployment unit both at development time or runtime. So it uses
 * {@link JavaModel} instead of directly using java.lang.ClassLoader.
 *
 * @author Sanjeeb Sahoo
 */
public interface DeploymentUnit {

    /**
     * @return the descriptor object graph for this deployment unit.
     */
    PersistenceJarDescriptor getPersistenceJar();

    /**
     * @return the JavaModel for this deployment unit.
     */
    JavaModel getJavaModel();
}