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

package org.glassfish.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Param is a parameter to a command. This annotation can be placed on a field or
 * setter method to identify the parameters of a command and have those parameters
 * injected by the system before the command is executed.
 *
 * The system will check that all non optional parameters are satisfied before invoking
 * the command.
 *
 * @author Jerome Dochez
 */
@Retention(RUNTIME)
@Target({METHOD,FIELD})
public @interface Param {

    /**
     * Retuns the name of the parameter as it has be specified by the client when invoking
     * the command. By default the name is deducted from the name of the annotated element.
     * If the annotated element is a field, it is the filed name.
     * If the annoated element is a mehod, it is the JavaBeans property name from the setter
     * method name
     *
     * @return the parameter name.
     */
    public String name() default "";

    /**
     * Returns a list of comma separated acceptable values for this parameter. The system
     * will check that one of the value is used before invoking the command.
     *
     * @return the list of comma separated acceptable values
     */
    public String acceptableValues() default "";

    /**
     * Returns true if the parameter is optional to the successful invocation of the command
     * @return true if the parameter is optional
     */
    public boolean optional() default false;

    /**
     * Returns the short name associated with the parameter so that the user can specify
     * -p as well as -password when invoking the command.
     *
     * @return the parameter short name
     */
    public String shortName() default "";

    /**
     * Returns true if this is the primary parameter for the command which mean that the
     * client does not have to pass the parameter name but just the value to the command.
     *
     * @return true if this is the primary command parameter.
     */
    public boolean primary() default false;

}
