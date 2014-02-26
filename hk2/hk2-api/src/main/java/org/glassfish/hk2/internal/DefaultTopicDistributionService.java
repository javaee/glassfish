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
package org.glassfish.hk2.internal;

import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.InstanceLifecycleEvent;
import org.glassfish.hk2.api.InstanceLifecycleListener;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.messaging.Topic;
import org.glassfish.hk2.api.messaging.TopicDistributionService;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.InjecteeImpl;
import org.glassfish.hk2.utilities.reflection.Pretty;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;
import org.glassfish.hk2.utilities.reflection.TypeChecker;
import org.jvnet.hk2.annotations.ContractsProvided;

/**
 * This is the default implementation of the TopicDistributionService.
 * 
 * @author jwells
 */
@Singleton
@Named(TopicDistributionService.HK2_DEFAULT_TOPIC_DISTRIBUTOR)
@ContractsProvided({TopicDistributionService.class, InstanceLifecycleListener.class})
public class DefaultTopicDistributionService implements
        TopicDistributionService, InstanceLifecycleListener {
    @Inject
    private ServiceLocator locator;
    
    private final HashMap<ActiveDescriptor<?>, List<Method>> subscribers = new HashMap<ActiveDescriptor<?>, List<Method>>();
    private final HashMap<Method, SubscriberInfo> subscriberInfos = new HashMap<Method, SubscriberInfo>();
    
    private void fire(Object message, Method subscription, SubscriberInfo subscriptionInfo, Object target) throws Throwable {
        Object arguments[] = new Object[subscriptionInfo.otherInjectees.length];
        
        List<ServiceHandle<?>> destroyMe = new LinkedList<ServiceHandle<?>>();
        try {
            for (int lcv = 0; lcv < subscriptionInfo.otherInjectees.length; lcv++) {
                InjecteeImpl injectee = subscriptionInfo.otherInjectees[lcv];
                if (injectee == null) {
                    arguments[lcv] = message;
                }
                else {
                    ActiveDescriptor<?> injecteeDescriptor = locator.getInjecteeDescriptor(injectee);
                    if (injecteeDescriptor == null) {
                        throw new IllegalStateException("Could not find injectee " + injectee + " for subscriber " +
                          Pretty.method(subscription) + " on class " + target.getClass().getName());
                    }
                
                    ServiceHandle<?> handle = locator.getServiceHandle(injecteeDescriptor);
                    if (injecteeDescriptor.getScope().equals(PerLookup.class.getName())) {
                        // Only will live as long as the method
                        destroyMe.add(handle);
                    }
                    
                    arguments[lcv] = handle.getService();
                }
            }
            
            // OK, everything filled in!
            ReflectionHelper.invoke(target, subscription, arguments, locator.getNeutralContextClassLoader());
        }
        finally {
            for (ServiceHandle<?> dead : destroyMe) {
                dead.destroy();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.messaging.TopicDistributionService#distributeMessage(org.glassfish.hk2.api.messaging.Topic, java.lang.Object)
     */
    @Override
    public void distributeMessage(Topic<?> topic, Object message)
            throws MultiException {
        MultiException errors = null;
        Type eventType = topic.getTopicType();
        
        HashMap<ActiveDescriptor<?>, List<Method>> localSubscribers;
        synchronized (this) {
            localSubscribers = new HashMap<ActiveDescriptor<?>, List<Method>>(subscribers);
        }
        
        for (Map.Entry<ActiveDescriptor<?>, List<Method>> entry : localSubscribers.entrySet()) {
            for (Method subscriberMethod : entry.getValue()) {
                SubscriberInfo subscriberInfo = subscriberInfos.get(subscriberMethod);
                
                Type subscriptionType = subscriberInfo.eventType;
                
                if (!TypeChecker.isRawTypeSafe(eventType, subscriptionType)) {
                    // Not a type match
                    continue;
                }
                
                if (!subscriberInfo.eventQualifiers.isEmpty()) {
                    if (!ReflectionHelper.annotationContainsAll(topic.getTopicQualifiers(), subscriberInfo.eventQualifiers)) {
                        // The qualifiers do not match
                        continue;
                    }
                }
                
                Iterator<WeakReference<Object>> targetIterator = subscriberInfo.targets.iterator();
                
                while (targetIterator.hasNext()) {
                    WeakReference<Object> targetReference = targetIterator.next();
                    
                    Object target = targetReference.get();
                    if (target == null) {
                        // we are the only reference, remove
                        targetIterator.remove();
                        
                        continue;
                    }
                    
                    try {
                      fire(message, subscriberMethod, subscriberInfo, target);
                    }
                    catch (Throwable th) {
                        if (errors == null) {
                            errors = new MultiException(th);
                        }
                        else {
                            errors.addError(th);
                        }
                    }
                }
                
            }
        }

    }

    @Override
    public Filter getFilter() {
        return BuilderHelper.allFilter();
    }
    
    private void postProduction(InstanceLifecycleEvent lifecycleEvent) {
        ActiveDescriptor<?> descriptor = lifecycleEvent.getActiveDescriptor();
        Object target = lifecycleEvent.getLifecycleObject();
        
    }

    @Override
    public void lifecycleEvent(InstanceLifecycleEvent lifecycleEvent) {
        switch (lifecycleEvent.getEventType()) {
        case  POST_PRODUCTION:
            break;
        case PRE_DESTRUCTION:
            break;
        default:
            return;
        }
    }
    
    private static class SubscriberInfo {
        private LinkedList<WeakReference<Object>> targets;
        private Type eventType;
        private Set<Annotation> eventQualifiers;
        private InjecteeImpl otherInjectees[];  // There will be a null in the slot for the event
    }
}
