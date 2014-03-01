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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.DynamicConfigurationListener;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.InstanceLifecycleEvent;
import org.glassfish.hk2.api.InstanceLifecycleListener;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.Self;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.Unqualified;
import org.glassfish.hk2.api.messaging.SubscribeTo;
import org.glassfish.hk2.api.messaging.Topic;
import org.glassfish.hk2.api.messaging.TopicDistributionService;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.InjecteeImpl;
import org.glassfish.hk2.utilities.reflection.ClassReflectionModel;
import org.glassfish.hk2.utilities.reflection.Pretty;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;
import org.glassfish.hk2.utilities.reflection.TypeChecker;
import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Optional;

/**
 * This is the default implementation of the TopicDistributionService.
 * 
 * @author jwells
 */
@Singleton
@Named(TopicDistributionService.HK2_DEFAULT_TOPIC_DISTRIBUTOR)
@ContractsProvided({TopicDistributionService.class, InstanceLifecycleListener.class, DynamicConfigurationListener.class})
public class DefaultTopicDistributionService implements
        TopicDistributionService, InstanceLifecycleListener, DynamicConfigurationListener {
    @Inject
    private ServiceLocator locator;
    
    private final ClassReflectionModel reflectionModel = new ClassReflectionModel();
    private final HashMap<ActiveDescriptor<?>, Set<Class<?>>> descriptor2Classes = new HashMap<ActiveDescriptor<?>, Set<Class<?>>>();
    private final HashMap<ActivatorClassKey, List<SubscriberInfo>> class2Methods = new HashMap<ActivatorClassKey, List<SubscriberInfo>>();
    
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final WriteLock wLock = readWriteLock.writeLock();
    private final ReadLock rLock = readWriteLock.readLock();
    
    private static void fire(Object message, Method subscription, SubscriberInfo subscriptionInfo, Object target, ServiceLocator locator) throws Throwable {
        Object arguments[] = new Object[subscriptionInfo.otherInjectees.length];
        
        List<ServiceHandle<?>> destroyMe = new LinkedList<ServiceHandle<?>>();
        try {
            for (int lcv = 0; lcv < subscriptionInfo.otherInjectees.length; lcv++) {
                InjecteeImpl injectee = subscriptionInfo.otherInjectees[lcv];
                if (injectee == null) {
                    arguments[lcv] = message;
                }
                else {
                    if (injectee.isSelf()) {
                        arguments[lcv] = injectee.getInjecteeDescriptor();
                        continue;
                    }
                    
                    ActiveDescriptor<?> injecteeDescriptor = locator.getInjecteeDescriptor(injectee);
                    if (injecteeDescriptor == null) {
                        if (injectee.isOptional()) {
                            arguments[lcv] = null;
                            continue;
                        }
                        else {
                            throw new IllegalStateException("Could not find injectee " + injectee + " for subscriber " +
                                Pretty.method(subscription) + " on class " + target.getClass().getName());
                        }
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
    
    private List<FireResults> handleDescriptorToClass(ActiveDescriptor<?> descriptor, Class<?> clazz, Type eventType, Topic<?> topic) {
        LinkedList<FireResults> retVal = new LinkedList<FireResults>();
        
        List<SubscriberInfo> subscribers = class2Methods.get(new ActivatorClassKey(descriptor, clazz));
        
        for (SubscriberInfo subscriberInfo : subscribers) {    
            Type subscriptionType = subscriberInfo.eventType;
            
            if (!TypeChecker.isRawTypeSafe(subscriptionType, eventType)) {
                // Not a type match
                continue;
            }
                
            if (!subscriberInfo.eventQualifiers.isEmpty()) {
                if (!ReflectionHelper.annotationContainsAll(topic.getTopicQualifiers(), subscriberInfo.eventQualifiers)) {
                    // The qualifiers do not match
                    continue;
                }
            }
            
            if ((subscriberInfo.unqualified != null) && !topic.getTopicQualifiers().isEmpty()) {
                if (subscriberInfo.unqualified.value().length == 0) {
                    // publisher must not have any qualifiers,
                    // but it DOES have some, so forget it!
                    continue;
                }
                else {
                    Set<Class<? extends Annotation>> topicQualifierClasses = new HashSet<Class<? extends Annotation>>();
                    for (Annotation topicQualifier : topic.getTopicQualifiers()) {
                        topicQualifierClasses.add(topicQualifier.annotationType());
                    }
                    
                    boolean found = false;
                    for (Class<? extends Annotation> verbotenQualifier : subscriberInfo.unqualified.value()) {
                        if (topicQualifierClasses.contains(verbotenQualifier)) {
                            found = true;
                            break;
                        }
                    }
                    
                    if (found) {
                        // Found one of the qualifiers we are not allowed to have!
                        continue;
                    }
                }
            }
            
            for (WeakReference<Object> targetReference : subscriberInfo.targets) {
                Object target = targetReference.get();
                retVal.add(new FireResults(subscriberInfo.method, subscriberInfo, target));
            }    
        }
        
        return retVal;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.messaging.TopicDistributionService#distributeMessage(org.glassfish.hk2.api.messaging.Topic, java.lang.Object)
     */
    @Override
    public Object distributeMessage(Topic<?> topic, Object message)
            throws MultiException {
        
        Type eventType = topic.getTopicType();
        
        LinkedList<FireResults> fireResults = new LinkedList<FireResults>();
        rLock.lock();
        try {
            for (Map.Entry<ActiveDescriptor<?>, Set<Class<?>>> d2cEntry : descriptor2Classes.entrySet()) {
                for (Class<?> clazz : d2cEntry.getValue()) {
                    fireResults.addAll(handleDescriptorToClass(d2cEntry.getKey(), clazz, eventType, topic));
                }
            }
        }
        finally {
            rLock.unlock();
        }
        
        // Do everything else outside the lock
        Set<SubscriberInfo> hasDeadReferences = new HashSet<SubscriberInfo>();
        
        MultiException errors = null;
        for (FireResults fireResult : fireResults) {
            if (fireResult.target == null) {
                hasDeadReferences.add(fireResult.subscriberInfo);
            }
            else {
                try {
                    fire(message,
                           fireResult.subscriberMethod,
                           fireResult.subscriberInfo,
                           fireResult.target,
                           locator);
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
        
        if (!hasDeadReferences.isEmpty()) {
            wLock.lock();
            try {
                for (SubscriberInfo sInfo : hasDeadReferences) {
                    Iterator<WeakReference<Object>> iterator = sInfo.targets.iterator();
                
                    while (iterator.hasNext()) {
                        WeakReference<Object> ref = iterator.next();
                        if (ref.get() == null) {
                            iterator.remove();
                        }
                    }
                }
            
            }
            finally {
                wLock.unlock();
            }
        }
        
        // TODO:  What do we do if errors is non-null?
        
        return null;
    }

    @Override
    public Filter getFilter() {
        return BuilderHelper.allFilter();
    }
    
    private void postProduction(InstanceLifecycleEvent lifecycleEvent) {
        ActiveDescriptor<?> descriptor = lifecycleEvent.getActiveDescriptor();
        Object target = lifecycleEvent.getLifecycleObject();
        if (target == null) return;
        
        Class<?> targetClass = target.getClass();
        
        Set<Class<?>> descriptorClazzes = descriptor2Classes.get(descriptor);
        List<SubscriberInfo> existingMethods = null;
        
        if (descriptorClazzes != null) {
            if (descriptorClazzes.contains(targetClass)) {
                existingMethods = class2Methods.get(new ActivatorClassKey(descriptor, targetClass));
            
                if (existingMethods != null) {
                    for (SubscriberInfo info : existingMethods) {
                        info.targets.add(new WeakReference<Object>(target));
                    }
                
                    return;
                }
            }
            else {
                descriptorClazzes.add(targetClass);
            }
        }
        else {
            descriptorClazzes = new HashSet<Class<?>>();
            descriptorClazzes.add(targetClass);
            
            descriptor2Classes.put(descriptor, descriptorClazzes);
        }
        
        existingMethods = new LinkedList<SubscriberInfo>();    
        class2Methods.put(new ActivatorClassKey(descriptor, targetClass), existingMethods);
        
        // Have not yet seen this descriptor, must now get the information on it
        Set<Method> allMethods = reflectionModel.getAllMethods(targetClass);
        
        for (Method method : allMethods) {
            Annotation paramAnnotations[][] =method.getParameterAnnotations();
            
            int foundPosition = -1;
            for (int position = 0; position < paramAnnotations.length; position++) {
                for (Annotation paramAnnotation : paramAnnotations[position]) {
                    if (SubscribeTo.class.equals(paramAnnotation.annotationType())) {
                        if (foundPosition != -1) {
                            throw new IllegalArgumentException("A method " + Pretty.method(method) + " on class " + method.getDeclaringClass().getName() +
                                    " has more than one @SubscribeTo annotation on its parameters");
                        }
                        
                        foundPosition = position;
                    }
                }
            }
                
            if (foundPosition == -1) {
                // Try next method
                continue;
            }
                
            // Found a method with exactly one SubscribeTo annotation!
            SubscriberInfo si = generateSubscriberInfo(descriptor, method, foundPosition, paramAnnotations);
            si.targets.add(new WeakReference<Object>(target));
            
            existingMethods.add(si);
        }
        
    }
    
    private static SubscriberInfo generateSubscriberInfo(ActiveDescriptor<?> injecteeDescriptor,
            Method subscriber, int subscribeToPosition, Annotation paramAnnotations[][]) {
        Type parameterTypes[] = subscriber.getGenericParameterTypes();
        
        // Get the event type
        Type eventType = parameterTypes[subscribeToPosition];
        
        // Get the event qualifiers and the Unqualified
        Set<Annotation> eventQualifiers = new HashSet<Annotation>();
        Unqualified eventUnqualified = null;
        
        Annotation subscribeToAnnotations[] = paramAnnotations[subscribeToPosition];
        for (Annotation possibleQualifier : subscribeToAnnotations) {
            if (ReflectionHelper.isAnnotationAQualifier(possibleQualifier)) {
                eventQualifiers.add(possibleQualifier);
            }
            
            if (Unqualified.class.equals(possibleQualifier.annotationType())) {
                eventUnqualified = (Unqualified) possibleQualifier;
            }
        }
        
        // Get the injectees for the other parameters
        InjecteeImpl injectees[] = new InjecteeImpl[parameterTypes.length];
        for (int lcv = 0; lcv < injectees.length; lcv++) {
            if (lcv == subscribeToPosition) {
                injectees[lcv] = null;
            }
            else {
                InjecteeImpl ii = new InjecteeImpl();
                
                ii.setRequiredType(parameterTypes[lcv]);
                
                Set<Annotation> parameterQualifiers = new HashSet<Annotation>();
                Annotation parameterAnnotations[] = paramAnnotations[lcv];
                boolean isOptional = false;
                boolean isSelf = false;
                Unqualified unqualified = null;
                for (Annotation possibleQualifier : parameterAnnotations) {
                    if (ReflectionHelper.isAnnotationAQualifier(possibleQualifier)) {
                        parameterQualifiers.add(possibleQualifier);
                    }
                    
                    if (Optional.class.equals(possibleQualifier.annotationType())) {
                        isOptional = true;
                    }
                    if (Self.class.equals(possibleQualifier.annotationType())) {
                        isSelf = true;
                    }
                    if (Unqualified.class.equals(possibleQualifier.annotationType())) {
                        unqualified = (Unqualified) possibleQualifier;
                    }
                }
                
                ii.setRequiredQualifiers(parameterQualifiers);
                ii.setPosition(lcv);
                ii.setParent(subscriber);
                ii.setOptional(isOptional);
                ii.setSelf(isSelf);
                ii.setUnqualified(unqualified);
                ii.setInjecteeDescriptor(injecteeDescriptor);
                
                injectees[lcv] = ii;
            }
        }
        
        return new SubscriberInfo(subscriber, eventType, eventQualifiers, eventUnqualified, injectees);
    }
    
    private void preDestruction(InstanceLifecycleEvent lifecycleEvent) {
        ActiveDescriptor<?> descriptor = lifecycleEvent.getActiveDescriptor();
        Object target = lifecycleEvent.getLifecycleObject();
        if (target == null) return;
        
        Set<Class<?>> classes = descriptor2Classes.get(descriptor);
        
        for (Class<?> clazz : classes) {
            List<SubscriberInfo> subscribers = class2Methods.get(new ActivatorClassKey(descriptor, clazz));
            
            for (SubscriberInfo subscriberInfo : subscribers) {
                Iterator<WeakReference<Object>> targetIterator = subscriberInfo.targets.iterator();
                
                while (targetIterator.hasNext()) {
                    WeakReference<Object> ref = targetIterator.next();
                    Object subscriberTarget = ref.get();
                    if (subscriberTarget == null) {
                        targetIterator.remove();
                    }
                    else if (subscriberTarget == target) {
                        targetIterator.remove();
                    }
                }
            }
        }
        
    }

    @Override
    public void lifecycleEvent(InstanceLifecycleEvent lifecycleEvent) {
        switch (lifecycleEvent.getEventType()) {
        case  POST_PRODUCTION:
            wLock.lock();
            try {
                postProduction(lifecycleEvent);
            }
            finally {
                wLock.unlock();
            }
            break;
        case PRE_DESTRUCTION:
            wLock.lock();
            try {
                preDestruction(lifecycleEvent);
            }
            finally {
                wLock.unlock();
            }
            break;
        default:
            return;
        }
    }
    
    @Override
    public void configurationChanged() {
        List<ActiveDescriptor<?>> allDescriptors = locator.getDescriptors(BuilderHelper.allFilter());
        
        wLock.lock();
        try {
            HashSet<ActiveDescriptor<?>> removeMe = new HashSet<ActiveDescriptor<?>>(descriptor2Classes.keySet());
            removeMe.removeAll(allDescriptors);
            
            for (ActiveDescriptor<?> parent : removeMe) {
                Set<Class<?>> clazzes = descriptor2Classes.remove(parent);
                
                if (clazzes == null) continue;
                
                for (Class<?> clazz : clazzes) {
                    class2Methods.remove(new ActivatorClassKey(parent, clazz));
                }
            }
        }
        finally {
            wLock.unlock();
        }
        
        
    }
    
    private static class SubscriberInfo {
        private final Method method;
        private final LinkedList<WeakReference<Object>> targets = new LinkedList<WeakReference<Object>>();
        private final Type eventType;
        private final Set<Annotation> eventQualifiers;
        private final Unqualified unqualified;
        private final InjecteeImpl otherInjectees[];  // There will be a null in the slot for the event
        
        private SubscriberInfo(Method method,
                Type eventType,
                Set<Annotation> eventQualifiers,
                Unqualified unqualified,
                InjecteeImpl otherInjectees[]) {
            this.method = method;
            this.eventType = eventType;
            this.eventQualifiers = eventQualifiers;
            this.unqualified = unqualified;
            this.otherInjectees = otherInjectees;
        }
    }
    
    private static class FireResults {
        private final Method subscriberMethod;
        private final SubscriberInfo subscriberInfo;
        private final Object target;
        
        private FireResults(Method subscriberMethod, SubscriberInfo subscriberInfo, Object target) {
            this.subscriberMethod = subscriberMethod;
            this.subscriberInfo = subscriberInfo;
            this.target = target;
        }
        
    }
    
    private static class ActivatorClassKey {
        private final ActiveDescriptor<?> descriptor;
        private final Class<?> clazz;
        private final int hashCode;
        
        private ActivatorClassKey(ActiveDescriptor<?> descriptor, Class<?> clazz) {
            this.descriptor = descriptor;
            this.clazz = clazz;
            this.hashCode = descriptor.hashCode() ^ clazz.hashCode();
        }
        
        public int hashCode() {
            return hashCode;
        }
        
        public boolean equals(Object o) {
            if (o == null) return false;
            if (!(o instanceof ActivatorClassKey)) return false;
            
            ActivatorClassKey other = (ActivatorClassKey) o;
            
            return descriptor.equals(other.descriptor) && clazz.equals(other.clazz) ; 
        }
    }

    
}
