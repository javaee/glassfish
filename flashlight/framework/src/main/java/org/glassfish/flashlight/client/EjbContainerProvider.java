package org.glassfish.flashlight.client;

import org.glassfish.flashlight.provider.annotations.ProbeName;
import org.glassfish.flashlight.provider.annotations.ProbeParam;

import java.lang.reflect.Method;

/**
 * @author Mahesh Kannan
 *         Date: Jul 20, 2008
 */
public interface EjbContainerProvider<K, V> {

    @ProbeName("entry")
        public void namedEntry(
            @ProbeParam("method") Method m, @ProbeParam("beanName") String beanName);
    

}
