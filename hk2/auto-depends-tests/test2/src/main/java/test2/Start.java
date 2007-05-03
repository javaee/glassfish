package test2;

import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;
import junit.framework.Assert;
import org.jvnet.hk2.annotations.Service;
import service_test.PunchInTest;
import service_test.Animal;

/**
 * @author Kohsuke Kawaguchi
 */
@Service
public class Start extends Assert implements ModuleStartup {
    public void setStartupContext(StartupContext context) {
    }

    public void run() {
        // make sure that the test can't find us from the context classloader
        Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());

        Animal animal = new PunchInTest().get();
        System.out.println(animal.bark());
        assert animal.getClass()==Dog.class;

        assert Animal.class.getClassLoader()!=Dog.class.getClassLoader();
        assert Dog.class.getClassLoader()==getClass().getClassLoader();
    }
}
