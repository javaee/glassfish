/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 * LogMessageInfo annotation definition.
 *
 * message: The message to log.
 * comment: A comment which appears above the message in the
 *          LogMessages.properties file.  Useful for localization.
 * level:   The log level.  (default: INFO)
 * cause:   Describes what caused this message to be generated.
 * action:  Describes what the user/admin can do to resolve the problem.
 * pkg:     A java package name where the annotation processor will
 *          store the LogMessages.properties file.  By default the
 *          package the annotation is used in is used.
 * publish: Boolean value indicates whether this log message should be
 *          published in the Error Reference guide. (default: true)
 *
 *  Example:
 *
 *     @LogMessageInfo(
 *              message = "This is the log message to be localized.",
 *              commetn = "This is a comment about the above message.",
 *              level = "WARNING",
 *              cause = "This describes the cause of the problem...",
 *              action = "This describes the action to fix the problem...",
 *              publish = false)
 *     private static final String EJB005 = "AS-EJB-00005";
 *
 */


@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface LogMessageInfo {
    String message();
    String comment() default "";
    String level() default "INFO";
    String cause();
    String action();
    String pkg() default "";
    boolean publish() default true;
}
