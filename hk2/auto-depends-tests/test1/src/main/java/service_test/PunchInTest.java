package service_test;

import org.apache.commons.discovery.tools.DiscoverSingleton;

/**
 * Used by test2 to test the classloader punch-in for /META-INF/services
 * @author Kohsuke Kawaguchi
 */
public class PunchInTest {
    public Animal get() {
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        return (Animal)DiscoverSingleton.find(Animal.class);
    }
}
