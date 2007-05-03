package service_test;

import org.apache.commons.discovery.tools.DiscoverSingleton;

/**
 * @author Kohsuke Kawaguchi
 */
public class PunchInTest {
    public Animal get() {
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        return (Animal)DiscoverSingleton.find(Animal.class);
    }
}
