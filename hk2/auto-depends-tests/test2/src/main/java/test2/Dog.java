package test2;

import org.jvnet.hk2.annotations.Service;
import service_test.Animal;

/**
 * @author Kohsuke Kawaguchi
 */
@Service
public class Dog implements Animal {
    public String bark() {
        return "Bowwow";
    }
}
