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
package org.jvnet.hk2.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.glassfish.hk2.api.messaging.Topic;
import org.glassfish.hk2.api.messaging.TopicDistributionService;
import org.glassfish.hk2.utilities.NamedImpl;

/**
 * @author jwells
 *
 */
public class TopicImpl<T> implements Topic<T> {
    private final ServiceLocatorImpl locator;
    private final Type topicType;
    private final Set<Annotation> requiredQualifiers;
    
    /* package */ TopicImpl(ServiceLocatorImpl locator,
            Type topicType,
            Set<Annotation> requiredQualifiers) {
        this.locator = locator;
        this.topicType = topicType;
        this.requiredQualifiers = Collections.unmodifiableSet(requiredQualifiers);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.messaging.Topic#publish(java.lang.Object)
     */
    @Override
    public void publish(T message) {
        if (message == null) throw new IllegalArgumentException();
        
        TopicDistributionService distributor = locator.getService(TopicDistributionService.class);
            
        if (distributor == null) {
             throw new IllegalStateException("There is no implementation of the TopicDistributionService to distribute the message");
        }
            
        distributor.distributeMessage(this, message);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.messaging.Topic#named(java.lang.String)
     */
    @Override
    public Topic<T> named(String name) {
        return qualifiedWith(new NamedImpl(name));
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.messaging.Topic#ofType(java.lang.reflect.Type)
     */
    @Override
    public <U> Topic<U> ofType(Type type) {
        return new TopicImpl<U>(locator, type, requiredQualifiers);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.messaging.Topic#qualifiedWith(java.lang.annotation.Annotation[])
     */
    @Override
    public Topic<T> qualifiedWith(Annotation... qualifiers) {
        HashSet<Annotation> moreAnnotations = new HashSet<Annotation>(requiredQualifiers);
        for (Annotation qualifier : qualifiers) {
            moreAnnotations.add(qualifier);
        }
        
        return new TopicImpl<T>(locator, topicType, moreAnnotations);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.messaging.Topic#getTopicType()
     */
    @Override
    public Type getTopicType() {
        return topicType;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.messaging.Topic#getTopicQualifiers()
     */
    @Override
    public Set<Annotation> getTopicQualifiers() {
        return requiredQualifiers;
    }

}
