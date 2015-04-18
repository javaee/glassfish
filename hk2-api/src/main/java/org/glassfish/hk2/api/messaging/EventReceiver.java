/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.api.messaging;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

import org.glassfish.hk2.api.Metadata;

/**
 * This qualifier must be placed on any hk2 descriptor that can
 * receive events.  This includes event receiver classes automatically
 * analyzed by hk2, or any {@link org.glassfish.hk2.api.Factory#provide()}
 * methods automatically analyzed by hk2 or any user-defined
 * {@link org.glassfish.hk2.api.Descriptor} who can receive events
 * 
 * @author jwells
 *
 */
@Documented
@Retention(RUNTIME)
@Qualifier
@Target({TYPE, METHOD})
public @interface EventReceiver {
    public static final String EVENT_RECEIVER_TYPES = "org.glassfish.hk2.messaging.eventReceiverTypes";
    
    /**
     * A list of event types that this service may receive.  The
     * default value of an empty array represents any event type.
     * Be warned that if the default value is used that any event
     * being fired will cause the descriptor with this qualifier
     * to get reified (classloaded) which may be expensive.  In order
     * to have a more efficient application it is better to fill
     * this value in with all the event types this service might
     * receive
     * 
     * @return A list of the classes that might be received as
     * topic events.  If the empty set then this class might
     * receive any topic event
     */
    @Metadata(EVENT_RECEIVER_TYPES)
    public Class<?>[] value() default {};

}
