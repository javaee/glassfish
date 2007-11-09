/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.glassfish.api.container;


import org.glassfish.api.deployment.Deployer;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Annotation to describe a container implementation. Containers are identified using
 * their implementation name first, a more descriptive (human readable) name should
 * be given using the type attribute.
 *
 * @author Jerome Dochez
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Container {

    /**
     * Defines the short name for the container type. This name does not need to be unique
     * as the containers are indexed use the full name of the annotated class
     * @return the container type
     */
    String type();


    /**
     * Returns the deployer implementation for this container
     * 
     * @return the deployer class
     */               
    Class<? extends Deployer> deployerImpl();

    /**
     * Returns a string identifying the provider of the container
     *
     * @return the container provider
     */
    String provider() default "";

    /**
     * Returns a URL for the container's provider web site
     *  
     * @return
     */
    String infoSite() default "";


}
