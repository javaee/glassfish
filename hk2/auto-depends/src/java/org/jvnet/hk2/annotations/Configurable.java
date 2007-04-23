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

package org.jvnet.hk2.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A configurable {@link Service} is a special kind of {@link Contract} implementation
 * that should either be instantiated from a persistent stream (like a configuration
 * file) or programmatically through instantiation.
 *
 * Configurable services are not automatically promoted to components by HK2 but are
 * instantiated outsite of the {@link org.jvnet.hk2.component.ComponentManager} and added
 * to the associated scope.
 *
 * @author Jerome Dochez
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface Configurable {
    // tag interface for now, maybe we could speficy the configuration file here. tbd
}
