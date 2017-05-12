[//]: # " DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER. "
[//]: # "  "
[//]: # " Copyright (c) 2013-2017 Oracle and/or its affiliates. All rights reserved. "
[//]: # "  "
[//]: # " The contents of this file are subject to the terms of either the GNU "
[//]: # " General Public License Version 2 only (''GPL'') or the Common Development "
[//]: # " and Distribution License(''CDDL'') (collectively, the ''License'').  You "
[//]: # " may not use this file except in compliance with the License.  You can "
[//]: # " obtain a copy of the License at "
[//]: # " https://oss.oracle.com/licenses/CDDL+GPL-1.1 "
[//]: # " or LICENSE.txt.  See the License for the specific "
[//]: # " language governing permissions and limitations under the License. "
[//]: # "  "
[//]: # " When distributing the software, include this License Header Notice in each "
[//]: # " file and include the License file at LICENSE.txt. "
[//]: # "  "
[//]: # " GPL Classpath Exception: "
[//]: # " Oracle designates this particular file as subject to the ''Classpath'' "
[//]: # " exception as provided by Oracle in the GPL Version 2 section of the License "
[//]: # " file that accompanied this code. "
[//]: # "  "
[//]: # " Modifications: "
[//]: # " If applicable, add the following below the License Header, with the fields "
[//]: # " enclosed by brackets [] replaced by your own identifying information: "
[//]: # " ''Portions Copyright [year] [name of copyright owner]'' "
[//]: # "  "
[//]: # " Contributor(s): "
[//]: # " If you wish your version of this file to be governed by only the CDDL or "
[//]: # " only the GPL Version 2, indicate your decision by adding ''[Contributor] "
[//]: # " elects to include this software in this distribution under the [CDDL or GPL "
[//]: # " Version 2] license.''  If you don't indicate a single choice of license, a "
[//]: # " recipient has the option to distribute your version of this file under "
[//]: # " either the CDDL, the GPL Version 2 or to extend the choice of license to "
[//]: # " its licensees as provided above.  However, if you add GPL Version 2 code "
[//]: # " and therefore, elected the GPL Version 2 license, then the option applies "
[//]: # " only if the new code is made subject to such option by the copyright "
[//]: # " holder. "

## Threaded Events Example

This example will enhance the existing default HK2 event mechanism to deliver events to subscribers on a different thread from
the publisher.

+ [The Strategy](threaded-events-example.html#The_Strategy)
+ [Testing](threaded-events-example.html#Testing)
+ [Conclusion](threaded-events-example.html#Conclusion)

### The Strategy

The strategy for modifying the default behavior of the HK2 default eventing mechanism is to write our own implementation of
[TopicDistributionService][topicdistributionservice] that uses the HK2 default service to delegate to in a different
thread.

Here is the implementation of the service.

```java
@Service @Singleton @Named @Rank(100)
public class ThreadedEventDistributor implements TopicDistributionService {
    @Inject @Named(TopicDistributionService.HK2_DEFAULT_TOPIC_DISTRIBUTOR)
    private TopicDistributionService defaultDistributor;

    @Override
    public void distributeMessage(Topic<?> topic, Object message) {
        Thread t = new Thread(new Distributor(topic, message), "TopicDistributor");
        t.setDaemon(true);
        
        t.start();
    }

    private final class Distributor implements Runnable {
        private final Topic<?> topic;
        private final Object message;
        
        private Distributor(Topic<?> topic, Object message) {
            this.topic = topic;
            this.message = message;
        }

        @Override
        public void run() {
            defaultDistributor.distributeMessage(topic, message);            
        }
    }
}
```java

Lets take a look at this service in detail.  It is a service so it is annotated with [@Service][service].  By default an
service annototated with [@Service][service] is put into the Singleton scope, so the @Singleton annotation is not necessary,
but does not hurt and leads to more understandable code.  The service is also annotated with @Named which is a nice practice
in case other implementations want to inject this specific [TopicDistributionService][topicdistributionservice] in order to
customize it.  Perhaps the most important annotation on this class is the [@Rank][rank] annotation, since that will cause
this service to have a higher rank than the default implementation.  This implies that when someone invokes the publish method
of [Topic][topic] that it will be this service that gets invoked and not the default one.

Now that we've assured ourselves that this implementation will get used rather than the default one we still want to delegate
the work of finding and distributing the message to the default implementation.  To do this we @Inject the
[TopicDistributionService][topicdistributionservice] with the specific name given to the HK2 default implementation.

Armed with the default implementation we can now look at the distributeMessage method.  All it needs to do is
start a new thread and give it the instance of the Distributor class with the Topic and message.  The Distributor
does nothing in its run method other than call the default HK2 implementation.  But now the default HK2 distributeMethod method
is being called on a separate thread than the caller.

This is a useful extension if it is known that the subscriber might do something that takes a long time in its method or if there
are other reasons to use a separate thread.

### Testing

The test code is very simple and just verifies that the subscriber is in fact on a different thread than the caller.  It does this
by recording the thread id in its object.  The test then verifies that the thread id of the publisher and the thread id of the
subscriber are different.

This is the subscriber:

```java
@Service @Singleton
public class EventSubscriberService {
    private final Object lock = new Object();
    private Long eventThreadId = null;
    
    /**
     * This is the method that should get called on a different thread
     * from the one the publisher used
     * 
     * @param event The event that was raised
     */
    @SuppressWarnings("unused")
    private void eventSubscriber(@SubscribeTo Event event) {
        synchronized (lock) {
            eventThreadId = Thread.currentThread().getId();
            
            lock.notifyAll();
        }
        
    }
    
    /**
     * Returns the thread-id upon which the event was raised
     * 
     * @return the thread-id of the thread on which the event was raised
     * @throws InterruptedException If the thread gets interrupted
     */
    public long getEventThread() throws InterruptedException {
        synchronized (lock) {
            while (eventThreadId == null) {
                lock.wait();
            }
            
            return eventThreadId;
        }
    }

}
```java

The publisher is very simple, just calling the publish method of the [Topic][topic] with a new Event:

```java
public class EventPublisherService {
    @Inject
    private Topic<Event> eventPublisher;
    
    /**
     * Publishes the event on the callers thread
     */
    public void publishEvent() {
        eventPublisher.publish(new Event());
    }
}
```java

Here we can see the test, which simply ensures that the thread id of the publisher is not the same as the thread id
of the subscriber:

```java
    @Test
    public void testEventDeliveredOnADifferentThread() throws InterruptedException {
        // Adds in the default event implementation
        ServiceLocatorUtilities.enableTopicDistribution(locator);
        
        ServiceLocatorUtilities.addClasses(locator,
                EventPublisherService.class,
                EventSubscriberService.class,
                ThreadedEventDistributor.class);
        
        EventSubscriberService subscriber = locator.getService(EventSubscriberService.class);
        EventPublisherService publisher = locator.getService(EventPublisherService.class);
        
        // This is my current thread, should  NOT be the same thread as the subscriber method call
        long myThreadId = Thread.currentThread().getId();
        
        // Publish the event
        publisher.publishEvent();
        
        long subscriberThreadId = subscriber.getEventThread();
        
        Assert.assertNotSame("Should have had different threadId from " + myThreadId,
                myThreadId, subscriberThreadId);
    }
```java

### Conclusion

In this example we have shown how to override and enhance the default HK2 messaging provider.  The quality of service
has been changed from being single threaded on the thread of the publisher to having the subscribers invoked on
a separate thread altogether.

It is left as an exercise for the reader to discover other qualities of service that can be modified in this way.

[topicdistributionservice]: apidocs/org/glassfish/hk2/api/messaging/TopicDistributionService.html
[topic]: apidocs/org/glassfish/hk2/api/messaging/Topic.html
[service]: apidocs/org/jvnet/hk2/annotations/Service.html
[rank]: apidocs/org/glassfish/hk2/api/Rank.html
