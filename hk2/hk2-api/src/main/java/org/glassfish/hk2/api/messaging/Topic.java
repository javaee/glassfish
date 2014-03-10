/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * @author jwells
 *
 */
public interface Topic<T> {
    /**
     * Publishes a message to all subscribers
     * 
     * @param message The non-null message to send to all current subscribers
     * @throws IllegalStateException If there is no implementation of
     * {@link TopicDistributionService} to do the distribution of the message
     */
    public void publish(T message);
    
    /**
     * Returns an Topic that is further qualified
     * with the given name
     * 
     * @param name The value field of the Named annotation parameter.  Must
     * not be null
     * @return A topic further qualified with the given name
     */
    public Topic<T> named(String name);
    
    /**
     * Returns an Topic that is of the given type.  This type
     * must be more specific than the type of this Topic
     * 
     * @param type The type to restrict the returned Topic to
     * @return A Topic restricted to only producing messages of the given type
     */
    public <U> Topic<U> ofType(Type type);
    
    /**
     * A set of qualifiers to further restrict this Topic to.
     * 
     * @param qualifiers The qualifiers to further restrict this Topic to
     * @return An Topic restricted with the given qualifiers
     */
    public Topic<T> qualifiedWith(Annotation... qualifiers);
    
    /**
     * Gets the type of the topic, in order to match the message
     * to subscribers
     * 
     * @return the Type of this topic.  Will not return null
     */
    public Type getTopicType();
    
    /**
     * The qualifiers associated with this Topic.  Messages
     * should only be distributed to subscribers that have
     * matching qualifiers
     * 
     * @return the non-null but possibly empty set of
     * qualifiers associated with this Topic
     */
    public Set<Annotation> getTopicQualifiers();
}
